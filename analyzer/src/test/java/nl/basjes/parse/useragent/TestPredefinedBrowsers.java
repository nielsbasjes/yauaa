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

import nl.basjes.parse.useragent.classify.DeviceClass;
import nl.basjes.parse.useragent.classify.UserAgentClassifier;
import nl.basjes.parse.useragent.config.TestCase;
import nl.basjes.parse.useragent.config.TestCase.TestResult;
import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

        // Make sure all currently known DeviceClass values can be classified
        for (TestResult testResult : testResults) {
            assertNotEquals(
                DeviceClass.UNCLASSIFIED,
                UserAgentClassifier.getDeviceClass(testResult.getResult()),
                testResult.toString());
        }

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

    private static final class FileNameComparator implements Comparator<TestCase> {

        Map<String, Long> sortMap;

        FileNameComparator(Map<String, Long> sortMap) {
            this.sortMap = sortMap;
        }

        @Override
        public int compare(TestCase o1, TestCase o2) {
            long o1Score = sortMap.getOrDefault(o1.getMetadata().get("filename"), -1L);
            long o2Score = sortMap.getOrDefault(o2.getMetadata().get("filename"), -1L);
            if (o1Score < o2Score) {
                return 1;
            }
            return (o1Score == o2Score) ? 0 : -1;
        }
    }

    private static class Duplicated implements Comparable<Duplicated> {
        // The sorted list of tests that have duplicates
        private final List<TestCase>    duplicateTests;

        Duplicated(List<TestCase> duplicateTests, Map<String, Long> filesWithDuplicatesCounts) {
            this.duplicateTests            = duplicateTests;
            this.duplicateTests.sort(new FileNameComparator(filesWithDuplicatesCounts));
        }

        private String getFirstLocation(Duplicated duplicated) {
            TestCase testCase = duplicated.duplicateTests.get(0);
            String filename = testCase.getMetadata().get("filename");
            String linenumber = testCase.getMetadata().get("fileline");
            return filename + ':' + linenumber;
        }

        @Override
        public int compareTo(Duplicated other) {
            String myFirst    = getFirstLocation(this);
            String otherFirst = getFirstLocation(other);
            return myFirst.compareTo(otherFirst);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb
                .append("======================================================\n")
                .append("Testcase > ").append(duplicateTests.get(0).getHeaders().toString()).append("\n");
            int count = 0;

            for (TestCase testCase: duplicateTests) {
                Map<String, String> metadata = testCase.getMetadata();
                String location = metadata.get("filename") + ":" + metadata.get("fileline");
                sb.append(">Location ").append(++count).append(".(").append(location).append(")\n");
            }
            return sb.toString();
        }
    }

    @Test
    void makeSureWeDoNotHaveDuplicateTests() {
        UserAgentAnalyzerTester uaa = UserAgentAnalyzerTester.newBuilder().delayInitialization().build();

        Map<String, List<TestCase>> allTestCases = new TreeMap<>();
        // Group all testcases by their inputs
        for (TestCase testCase: uaa.getTestCases()) {
            String testInput = new TreeMap<>(testCase.getHeaders()).toString();
            allTestCases.computeIfAbsent(testInput, key -> new ArrayList<>()).add(testCase);
        }

        List<List<TestCase>> duplicatedTestCases = allTestCases
            .values()
            .stream()
            .filter(testCases -> testCases.size() > 1)
            .collect(Collectors.toList());

        if (duplicatedTestCases.size() == 0) {
            LOG.info("No duplicate tests were found.");
            return; // We're done and all is fine.
        }

        // Now we have the ones that are duplicated we are sorting this all for easier handling a larger number.

        // First determine which file has the most duplicates (used to sort the output)
        Map<String, Long> filesWithDuplicatesCounts = new TreeMap<>(
            duplicatedTestCases
                .stream()
                .flatMap(Collection::stream)
                .map(testCase -> testCase.getMetadata().get("filename"))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting())));

        fail("Found "+ duplicatedTestCases.size() + " testcases multiple times: \n" +
            duplicatedTestCases
                .stream()
                .map(list -> new Duplicated(list, filesWithDuplicatesCounts))
                .sorted(Comparator.naturalOrder())
                .map(Duplicated::toString)
                .collect(Collectors.joining()));

    }
}
