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

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.config.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestCloneSharedConfig {

    private static final Logger LOG = LogManager.getLogger();

    @Test
    void testNew() {
        long current = getMemoryUsageAfterGC();

        LOG.info("Before : {}", current);
        long previous = current;

        // -----
        UserAgentAnalyzer uaa1 = createNew();
        current = getMemoryUsageAfterGC();
        LOG.info("After 1: {} --> DELTA = {} bytes", current, current-previous);
        previous = current;

        // -----
        UserAgentAnalyzer uaa2 = createNew();
        current = getMemoryUsageAfterGC();
        LOG.info("After 2: {} --> DELTA = {} bytes", current, current-previous);
        previous = current;

        // -----
        UserAgentAnalyzer uaa3 = createNew();
        current = getMemoryUsageAfterGC();
        LOG.info("After 3: {} --> DELTA = {} bytes", current, current-previous);
        previous = current;

        // -----
        UserAgentAnalyzer uaa4 = createNew();
        current = getMemoryUsageAfterGC();
        LOG.info("After 4: {} --> DELTA = {} bytes", current, current-previous);
//        previous = current;

        // -----
        verifyCorrect(uaa1);
        verifyCorrect(uaa2);
        verifyCorrect(uaa3);
        verifyCorrect(uaa4);
    }

    @Test
    void testClone() {
        long current = getMemoryUsageAfterGC();

        LOG.info("Before : {}", current);
        long previous = current;

        // -----
        UserAgentAnalyzer uaa1 = createNew();
        current = getMemoryUsageAfterGC();
        LOG.info("After 1: {} --> DELTA = {} bytes", current, current-previous);
        previous = current;

        // -----
        UserAgentAnalyzer uaa2 = clone(uaa1);
        current = getMemoryUsageAfterGC();
        LOG.info("After 2: {} --> DELTA = {} bytes", current, current-previous);
        previous = current;

        // -----
        UserAgentAnalyzer uaa3 = clone(uaa1);
        current = getMemoryUsageAfterGC();
        LOG.info("After 3: {} --> DELTA = {} bytes", current, current-previous);
        previous = current;

        // -----
        UserAgentAnalyzer uaa4 = clone(uaa1);
        current = getMemoryUsageAfterGC();
        LOG.info("After 4: {} --> DELTA = {} bytes", current, current-previous);
//        previous = current;

        // -----

        verifyCorrect(uaa1);
        verifyCorrect(uaa2);
        verifyCorrect(uaa3);
        verifyCorrect(uaa4);
    }

    private UserAgentAnalyzer createNew() {
        return UserAgentAnalyzer.newBuilder().hideMatcherLoadStats().showMinimalVersion().delayInitialization().build();
    }

    private UserAgentAnalyzer clone(UserAgentAnalyzer original) {
        return original.cloneWithSharedAnalyzerConfig(true, true);
    }

    private void verifyCorrect(UserAgentAnalyzer userAgentAnalyzer) {
        List<TestCase.TestResult> testResults = userAgentAnalyzer.getTestCases()
            .stream()
            .map(testCase -> testCase.verify(userAgentAnalyzer))
            .collect(Collectors.toList());

        List<TestCase.TestResult> failedTests = testResults
            .stream()
            .filter(TestCase.TestResult::testFailed)
            .collect(Collectors.toList());

        assertEquals(0, failedTests.size(), failedTests.toString());
    }

    public long getMemoryUsageAfterGC() {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        return runtime.totalMemory() - runtime.freeMemory();
    }

}
