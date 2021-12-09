/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2021 Niels Basjes
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

package nl.basjes.parse.useragent.profile;

import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.analyze.Analyzer;
import nl.basjes.parse.useragent.config.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This test is intended to see if there is a performance difference for cached hits
 * if there are a lot of uncached hits also.
 */
class TestConcurrentPerformance {
    private static final Logger LOG = LogManager.getLogger(TestConcurrentPerformance.class);

    public static class FireAllTestCases extends Thread {
        private Analyzer analyzer;
        private List<TestCase> testCases;

        FireAllTestCases(Analyzer analyzer, List<TestCase> testCases) {
            this.analyzer = analyzer;
            this.testCases = testCases;
        }

        public void run() {
            testCases.forEach(testCase -> analyzer.parse(testCase.getUserAgent()));
        }
    }

    public static class RunCachedTestCase extends Thread {
        private Analyzer analyzer;
        private String testCase;
        private long iterations;
        private long nanosUsed;

        RunCachedTestCase(Analyzer analyzer, String testCase, long iterations) {
            this.analyzer = analyzer;
            this.testCase = testCase;
            this.iterations = iterations;
        }

        public void run() {
            long start = System.nanoTime();
            for (long i = 0; i < iterations; i++) {
                analyzer.parse(testCase);
            }
            long stop = System.nanoTime();
            nanosUsed = stop-start;
        }

        public long getIterations() {
            return iterations;
        }

        public long getNanosUsed() {
            return nanosUsed;
        }
    }

    @Disabled
    @Test
    void testCachedMultiThreadedPerformance() throws InterruptedException { //NOSONAR: Do not complain about ignored performance test
        UserAgentAnalyzer uaa =
            UserAgentAnalyzer.newBuilder()
                .immediateInitialization()
                .keepTests()
                .build();

        // This testcase does not occur in the rest of the testcases (manually manipulated version for the Chrome part).
        String cachedUserAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.1.2.3.4.5.6 Safari/537.36";
        List<TestCase> testCases = uaa.getTestCases();
        // Make sure it is not in there.
        assertEquals(0, testCases.stream().filter(testCase -> testCase.getUserAgent().equals(cachedUserAgent)).count());

        long totalIterations = 0;
        long totalNanosUsed = 0;

        for (int i = 0; i < 10; i++) {
            LOG.info("Iteration {} : Start", i);

            FireAllTestCases  fireTests      = new FireAllTestCases(uaa, testCases);
            RunCachedTestCase cachedTestCase = new RunCachedTestCase(uaa, cachedUserAgent, 10_000_000);

            // Wipe the cache for the new run.
            uaa.clearCache();

            // Now parse and cache the precached useragent.
            uaa.parse(cachedUserAgent);

            // Start both
            fireTests.start();
            cachedTestCase.start();

            // Wait for both to finish
            fireTests.join();
            cachedTestCase.join();

            long iterations = cachedTestCase.getIterations();
            long nanosUsed = cachedTestCase.getNanosUsed();
            LOG.info("Iteration {} : Took {}ns ({}ms) = {}ns each", i, nanosUsed, (nanosUsed) / 1_000_000L, nanosUsed/iterations);

            if (i > 3) {
                totalIterations += iterations;
                totalNanosUsed += nanosUsed;
            }
        }

        LOG.info("Average    : {}ns ({}ms) = {}ns each", totalNanosUsed, (totalNanosUsed) / 1_000_000L, totalNanosUsed/totalIterations);
    }
}
