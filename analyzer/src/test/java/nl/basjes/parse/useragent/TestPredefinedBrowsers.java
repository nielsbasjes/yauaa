/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2018 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.useragent;

import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestPredefinedBrowsers {

    private static final Logger LOG = LoggerFactory.getLogger(TestPredefinedBrowsers.class);

    @Test
    public void validateAllPredefinedBrowsers() {
        UserAgentAnalyzerTester uaa;
        uaa = UserAgentAnalyzerTester.newBuilder().immediateInitialization().build();
        LOG.info("==============================================================");
        LOG.info("Validating when getting all fields");
        LOG.info("--------------------------------------------------------------");
        assertTrue(uaa.runTests(false, true, null, false, true));
    }

    private void validateAllPredefinedBrowsersMultipleFields(Collection<String> fields) {
        LOG.info("==============================================================");
        LOG.info("Validating when ONLY asking for {}", fields.toString());
        LOG.info("--------------------------------------------------------------");
        UserAgentAnalyzerTester userAgentAnalyzer =
            UserAgentAnalyzerTester
                .newBuilder()
                .withoutCache()
                .withFields(fields)
                .hideMatcherLoadStats()
                .build();

        assertNotNull(userAgentAnalyzer);
        assertTrue(userAgentAnalyzer.runTests(false, true, fields, false, false));
    }

    @Test
    public void validate_DeviceClass_AgentNameVersionMajor() {
        Set<String> fields = new HashSet<>();
        fields.add("DeviceClass");
        fields.add("AgentNameVersionMajor");
        validateAllPredefinedBrowsersMultipleFields(fields);
    }

    @Test
    public void validate_DeviceClass_AgentNameVersionMajor_OperatingSystemVersionBuild() {
        Set<String> fields = new HashSet<>();
        fields.add("DeviceClass");
        fields.add("AgentNameVersionMajor");
        fields.add("OperatingSystemVersionBuild");
        validateAllPredefinedBrowsersMultipleFields(fields);
    }

    @Test
    public void makeSureWeDoNotHaveDuplicateTests() {
        UserAgentAnalyzerTester uaa = UserAgentAnalyzerTester.newBuilder().build();

        Map<String, List<String>> allTestInputs = new HashMap<>(2000);
        Set<String> duplicates = new HashSet<>();
        for (Map<String, Map<String, String>> testCase: uaa.getTestCases()) {
            String input = testCase.get("input").get("user_agent_string");
            String location = testCase.get("metaData").get("filename") + ":" + testCase.get("metaData").get("fileline");
            List<String> locations = allTestInputs.get(input);
            if (locations == null) {
                locations = new ArrayList<>();
            }
            locations.add(location);

            if (locations.size()>1) {
                duplicates.add(input);
            }
            allTestInputs.put(input, locations);
        }

        if (duplicates.size() == 0) {
            return; // We're done and all is fine.
        }

        StringBuilder sb = new StringBuilder(1024);
        for (String duplicate: duplicates) {
            sb
                .append("======================================================\n")
                .append("Testcase > ").append(duplicate).append("\n");
            int count = 0;
            for (String location: allTestInputs.get(duplicate)) {
                sb.append(">Location ").append(++count).append(".(").append(location).append(")\n");
            }
        }
        fail("Found "+ duplicates.size()+ " testcases multiple times: \n" + sb.toString());
    }
}
