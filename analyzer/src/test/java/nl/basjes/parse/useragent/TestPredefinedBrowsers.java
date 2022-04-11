/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2022 Niels Basjes
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
import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class TestPredefinedBrowsers {

    private static final Logger LOG = LogManager.getFormatterLogger(TestPredefinedBrowsers.class);

    @Test
    void validateAllPredefinedBrowsers() {
        UserAgentAnalyzerTester uaa;
        uaa = UserAgentAnalyzerTester.newBuilder().immediateInitialization().build();
        LOG.info("==============================================================");
        LOG.info("Validating when getting all fields");
        LOG.info("--------------------------------------------------------------");
        assertTrue(uaa.runTests(false, true, null, false, true));

        LOG.info("--------------------------------------------------------------");
        LOG.info("Running all tests again which should return the cached values");
        assertTrue(uaa.runTests(false, true, null, false, false));

        // Only here for ensuring the code being tested with "all fields".
        uaa.destroy();
    }

    private void validateAllPredefinedBrowsersMultipleFields(Collection<String> fields) {
        LOG.info("==============================================================");
        LOG.info("Validating when ONLY asking for %s", fields.toString());
        LOG.info("--------------------------------------------------------------");
        UserAgentAnalyzerTester userAgentAnalyzer =
            UserAgentAnalyzerTester
                .newBuilder()
                .withFields(fields)
                .hideMatcherLoadStats()
                .build();

        assertNotNull(userAgentAnalyzer);
        assertTrue(userAgentAnalyzer.runTests(false, true, fields, false, false));
        LOG.info("--------------------------------------------------------------");
        LOG.info("Running all tests again which should return the cached values");
        assertTrue(userAgentAnalyzer.runTests(false, true, fields, false, false));

        // Only here for ensuring the code being tested with "some fields".
        userAgentAnalyzer.destroy();
    }

    @Test
    void validate_DeviceClass_AgentNameVersionMajor() {
        Set<String> fields = new HashSet<>();
        fields.add("DeviceClass");
        fields.add("AgentNameVersionMajor");
        validateAllPredefinedBrowsersMultipleFields(fields);
    }

    @Test
    void validate_DeviceClass_AgentNameVersionMajor_OperatingSystemVersionBuild() {
        Set<String> fields = new HashSet<>();
        fields.add("DeviceClass");
        fields.add("AgentNameVersionMajor");
        fields.add("OperatingSystemVersionBuild");
        validateAllPredefinedBrowsersMultipleFields(fields);
    }

    @Test
    void validateAllPredefinedBrowsersViaTestCase() {
        UserAgentAnalyzerTester uaa;
        uaa = UserAgentAnalyzerTester.newBuilder().immediateInitialization().build();
        LOG.info("==============================================================");
        LOG.info("Validating when getting all fields");
        LOG.info("--------------------------------------------------------------");

        List<TestCase> testCases = uaa.getTestCases();
        List<TestResult> testResults = testCases.stream()
            .map(testCase -> testCase.verify(uaa))
            .collect(Collectors.toList());

        List<TestResult> failedTests = testResults.stream().filter(TestResult::testFailed).collect(Collectors.toList());
        assertEquals(0, failedTests.size(), failedTests.toString());

        long totalDurationNS = testResults.stream()
            .map(TestResult::getParseDurationNS)
            .reduce(0L, Long::sum);

        LOG.info("All %4d tests passed in %5dms (average %4.3fms = %5dns per testcase).",
            testCases.size(),
            (totalDurationNS/1_000_000),
            (totalDurationNS/1_000_000D/testCases.size()),
            (totalDurationNS/testCases.size())
        );

        LOG.info("--------------------------------------------------------------");
        LOG.info("Running all tests again which should return the cached values");
        List<TestResult> testResultsCached = testCases.stream()
            .map(testCase -> testCase.verify(uaa))
            .collect(Collectors.toList());

        assertEquals(0, testResultsCached.stream()
            .filter(TestResult::testFailed)
            .count());

        long totalDurationNSCached = testResultsCached.stream()
            .map(TestResult::getParseDurationNS)
            .reduce(0L, Long::sum);

        long averageDurationNSCached = totalDurationNSCached / testCases.size();
        LOG.info("All %4d tests passed in %5dms (average %4.3fms = %5dns per testcase) FROM CACHE.",
            testCases.size(),
            totalDurationNSCached/1_000_000,
            averageDurationNSCached/1_000_000D,
            averageDurationNSCached
        );

        // Normal: > 0.50ms per test
        // Cached: < 0.10ms per test
        long maxAverageNS = 100000;
        assertTrue(averageDurationNSCached < maxAverageNS,
            String.format("Too slow average cached retrieval: %d ns (~%4.3f ms). Max allowed average: %4.3f ms.",
                averageDurationNSCached,
                averageDurationNSCached/1_000_000D,
                maxAverageNS/1_000_000D)
        );

        // Only here for ensuring the code being tested with "all fields".
        uaa.destroy();
    }


    @Test
    void makeSureWeDoNotHaveDuplicateTests() {
        UserAgentAnalyzerTester uaa = UserAgentAnalyzerTester.newBuilder().build();

        Map<String, List<String>> allTestInputs = new HashMap<>(2000);
        Set<String> duplicates = new HashSet<>();
        for (TestCase testCase: uaa.getTestCases()) {

            Map<String, String> inputHeaders = new TreeMap<>(testCase.getHeaders());
            String input = inputHeaders.toString();
            String location = testCase.getMetadata().get("filename") + ":" + testCase.getMetadata().get("fileline");
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
            LOG.info("No duplicate tests were found.");
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
        fail("Found "+ duplicates.size()+ " testcases multiple times: \n" + sb);
    }
}
