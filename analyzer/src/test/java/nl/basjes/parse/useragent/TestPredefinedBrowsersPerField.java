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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class TestPredefinedBrowsersPerField {

    private static final Logger LOG = LoggerFactory.getLogger(TestPredefinedBrowsersPerField.class);

    @Parameters(name = "Test {index} -> Only field: \"{0}\"")
    public static Iterable<String> data() {
        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester();
        uaa.setShowMatcherStats(false);
        uaa.initialize();
        return uaa.getAllPossibleFieldNamesSorted();
    }

    @Parameter
    public String fieldName;

    @Test
    public void validateAllPredefinedBrowsersForField() {
        Set<String> singleFieldList = new HashSet<>();
        LOG.info("==============================================================");
        LOG.info("Validating when ONLY asking for {}", fieldName);
        LOG.info("--------------------------------------------------------------");
        UserAgentAnalyzerTester userAgentAnalyzer =
            UserAgentAnalyzerTester
                .newBuilder()
                .withoutCache()
                .withField(fieldName)
                .hideMatcherLoadStats()
                .build();

        singleFieldList.clear();
        singleFieldList.add(fieldName);
        assertNotNull(userAgentAnalyzer);
        assertTrue(userAgentAnalyzer.runTests(false, true, singleFieldList, false, false));
    }


}
