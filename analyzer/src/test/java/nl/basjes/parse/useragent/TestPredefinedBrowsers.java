/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2017 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.useragent;

import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestPredefinedBrowsers {

    private static final Logger LOG = LoggerFactory.getLogger(TestPredefinedBrowsers.class);

    @Test
    public void validateAllPredefinedBrowsers() {
        UserAgentAnalyzerTester uaa;
        uaa = new UserAgentAnalyzerTester();
        uaa.setShowMatcherStats(false);
        uaa.initialize();
        LOG.info("==============================================================");
        LOG.info("Validating when getting all fields");
        LOG.info("--------------------------------------------------------------");
        assertTrue(uaa.runTests(false, true));
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


}
