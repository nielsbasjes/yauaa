/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2023 Niels Basjes
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

import nl.basjes.parse.useragent.config.TestCase;
import nl.basjes.parse.useragent.config.TestCase.TestResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestTestCase {

    private static final Logger LOG = LogManager.getLogger(TestTestCase.class);

    private static final String USERAGENT =
        "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/53.0.2785.124 Mobile Safari/537.36";

    final UserAgentAnalyzer userAgentAnalyzer = UserAgentAnalyzer.newBuilder()
        .withFields(Arrays.asList("DeviceClass", "DeviceBrand", "DeviceName", "AgentNameVersionMajor"))
        .build();

    @Test
    void basicTestCase() {
        TestCase testCase = new TestCase(USERAGENT, "My test");

        testCase.addMetadata("Metadata", "Metadata value");
        testCase.addOption("only");
        testCase.addOption("verbose");

        // The EXPLICITLY requested fields
        testCase.expect("DeviceClass",                      "Phone");
        testCase.expect("DeviceBrand",                      "Google");
        testCase.expect("DeviceName",                       "Google Nexus 6");
        testCase.expect("AgentNameVersionMajor",            "Chrome 53");

        // The IMPLICITLY requested fields (i.e. partials of the actually requested ones)
        testCase.expect("AgentName",                        "Chrome");
        testCase.expect("AgentVersion",                     "53.0.2785.124");
        testCase.expect("AgentVersionMajor",                "53");

        // The NOT requested fields are not there
        testCase.expect("OperatingSystemClass",             null);
        testCase.expect("OperatingSystemName",              null);
        testCase.expect("OperatingSystemNameVersion",       null);
        testCase.expect("OperatingSystemNameVersionMajor",  null);
        testCase.expect("OperatingSystemVersion",           null);
        testCase.expect("OperatingSystemVersionBuild",      null);
        testCase.expect("OperatingSystemVersionMajor",      null);
        testCase.expect("LayoutEngineClass",                null);
        testCase.expect("LayoutEngineName",                 null);
        testCase.expect("LayoutEngineNameVersion",          null);
        testCase.expect("LayoutEngineNameVersionMajor",     null);
        testCase.expect("LayoutEngineVersion",              null);
        testCase.expect("LayoutEngineVersionMajor",         null);
        testCase.expect("AgentClass",                       null);
        testCase.expect("AgentNameVersion",                 null);

        TestResult testResult = testCase.verify(userAgentAnalyzer);
        assertTrue(testResult.testPassed(), testResult.getErrorReport());
        LOG.info("{}", testResult);
    }

    @Test
    void basicTestCaseFailOnUnexpected() {
        TestCase testCase = new TestCase(USERAGENT, "My test");

        testCase.addMetadata("Metadata", "Metadata value");
        testCase.addOption("only");
        testCase.addOption("verbose");

        // The EXPLICITLY requested fields
        testCase.expect("DeviceClass",                      "Phone");
        testCase.expect("DeviceBrand",                      "Google");
        testCase.expect("DeviceName",                       "Google Nexus 6");
        testCase.expect("AgentNameVersionMajor",            "Chrome 53");

        TestResult testResult = testCase.verify(userAgentAnalyzer);
        assertFalse(testResult.testPassed(), testResult.getErrorReport());
        LOG.info("{}", testResult);
    }

    @Test
    void basicTestCaseDoNotFailOnUnexpected() {
        TestCase testCase = new TestCase(USERAGENT, "My test");

        testCase.addMetadata("Metadata", "Metadata value");
        testCase.addOption("only");
        testCase.addOption("verbose");

        // The EXPLICITLY requested fields
        testCase.expect("DeviceClass",                      "Phone");
        testCase.expect("DeviceBrand",                      "Google");
        testCase.expect("DeviceName",                       "Google Nexus 6");
        testCase.expect("AgentNameVersionMajor",            "Chrome 53");

        TestResult testResult = testCase.verify(userAgentAnalyzer, false);
        assertTrue(testResult.testPassed(), testResult.getErrorReport());
        LOG.info("{}", testResult);
    }

    @Test
    void badInput() {
        TestCase testCase = new TestCase(USERAGENT, "My test");
        testCase.expect("DeviceClass",   "This will be wrong");
        TestResult testResult = testCase.verify(userAgentAnalyzer);
        assertFalse(testResult);
        LOG.error("{}", testResult);
    }

}
