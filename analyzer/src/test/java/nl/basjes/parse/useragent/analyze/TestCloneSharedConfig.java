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

import nl.basjes.parse.useragent.AbstractUserAgentAnalyzerDirect;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.UserAgentAnalyzerDirect;
import nl.basjes.parse.useragent.config.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        // Must be the idential instance!
        assertSame(uaa1.getConfig(), uaa2.getConfig());
        assertSame(uaa1.getConfig(), uaa3.getConfig());
        assertSame(uaa1.getConfig(), uaa4.getConfig());

        verifyCorrect(uaa1);
        verifyCorrect(uaa2);
        verifyCorrect(uaa3);
        verifyCorrect(uaa4);
    }

    @Test
    void testCloneDirect() {
        UserAgentAnalyzerDirect uaad1 = UserAgentAnalyzerDirect.newBuilder().build();
        UserAgentAnalyzerDirect uaad2 = uaad1.cloneWithSharedAnalyzerConfig(false, true);

        assertSame(uaad1.getConfig(), uaad2.getConfig());

        verifyCorrect(uaad1);
        verifyCorrect(uaad2);
    }

    private UserAgentAnalyzer createNew() {
        return UserAgentAnalyzer.newBuilder().hideMatcherLoadStats().showMinimalVersion().delayInitialization().build();
    }

    private UserAgentAnalyzer clone(UserAgentAnalyzer original) {
        return original.cloneWithSharedAnalyzerConfig(false, true);
    }

    private void verifyCorrect(AbstractUserAgentAnalyzerDirect userAgentAnalyzer) {
        List<TestCase.TestResult> testResults = userAgentAnalyzer.getTestCases()
            .stream()
            .map(testCase -> testCase.verify(userAgentAnalyzer))
            .collect(Collectors.toList());

        List<TestCase.TestResult> failedTests = testResults
            .stream()
            .filter(TestCase.TestResult::testFailed)
            .collect(Collectors.toList());

        assertTrue(testResults.size() > 1000, "Not enough tests were run:" + testResults.size());
        assertEquals(0, failedTests.size(), failedTests.toString());
        LOG.info("Ran {} tests.", testResults.size());
    }

    public long getMemoryUsageAfterGC() {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        return runtime.totalMemory() - runtime.freeMemory();
    }

}
