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
package nl.basjes.parse.useragent.benchmarks;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.UserAgentAnalyzer.UserAgentAnalyzerBuilder;
import nl.basjes.parse.useragent.config.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class TestSpeed {

    private static final Logger LOG = LogManager.getFormatterLogger();

    UserAgentAnalyzerBuilder createBaseBuilder() {
        return UserAgentAnalyzer
            .newBuilder()
            .immediateInitialization()
            .withField(UserAgent.DEVICE_CLASS)
            .withField(UserAgent.DEVICE_NAME)
            .withField(UserAgent.AGENT_NAME)
            .withField(UserAgent.AGENT_VERSION)
            .withField(UserAgent.OPERATING_SYSTEM_NAME)
            .withField(UserAgent.OPERATING_SYSTEM_VERSION)
            .withField(UserAgent.OPERATING_SYSTEM_CLASS);
    }

    @Test
    void runNormal() {
        run(createBaseBuilder().build());
    }

    void run(UserAgentAnalyzer analyzer) {
        LOG.warn("There are %d unique testcases.", analyzer.getNumberOfTestCases());

        LOG.warn("DISABLE CACHING");
        analyzer.clearCache();
        analyzer.setCacheSize(0);
        analyzer.setClientHintsCacheSize(0);

        LOG.warn("Preheating the JVM");
        runTests(analyzer, 10000);

        LOG.warn("RUN WITHOUT CACHE");
        runTests(analyzer, 50000);

        LOG.warn("ENABLE CACHING");
        analyzer.clearCache();
        analyzer.setCacheSize(10000);
        analyzer.setClientHintsCacheSize(10000);

        LOG.warn("FILLING CACHE");
        runTests(analyzer, analyzer.getTestCases());

        LOG.warn("RUN WITH CACHE");
        runTests(analyzer, 500000);
    }

    private final Random rand = new Random();

    private void runTests(UserAgentAnalyzer analyzer, int numberOfTests) {
        long numberOfTestCases = analyzer.getNumberOfTestCases();
        List<TestCase> testCases = new ArrayList<>(numberOfTests);
        while (testCases.size() < numberOfTests) {
            testCases.add(analyzer.getTestCases().get(rand.nextInt((int)numberOfTestCases)));
        }
        runTests(analyzer, testCases);
    }

    private void runTests(UserAgentAnalyzer analyzer, List<TestCase> testCases) {
        long start = System.currentTimeMillis();
        testCases.forEach(testCase -> analyzer.parse(testCase.getHeaders()));
        long stop = System.currentTimeMillis();

        LOG.info("Doing %6d testcases took %6d ms = average %6.6f ms/test",
            testCases.size(),
            stop-start,
            ((double)(stop-start)/ testCases.size()));
    }

}
