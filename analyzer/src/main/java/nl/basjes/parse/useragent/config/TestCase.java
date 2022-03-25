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

package nl.basjes.parse.useragent.config;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.analyze.Analyzer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.BooleanSupplier;

import static nl.basjes.parse.useragent.UserAgent.NULL_VALUE;
import static nl.basjes.parse.useragent.UserAgent.SYNTAX_ERROR;
import static nl.basjes.parse.useragent.UserAgent.USERAGENT_FIELDNAME;

public class TestCase implements Serializable {
    private final String userAgent;
    private final String testName;
    private final List<String> options;
    private final Map<String, String> metadata;
    private final Map<String, String> expected;

    public static class TestResult implements BooleanSupplier {
        private TestCase testCase;
        private boolean pass;
        private long parseDurationNS;
        private String errorReport;
        public TestCase getTestCase() {
            return testCase;
        }
        public boolean testPassed() {
            return pass;
        }
        public boolean testFailed() {
            return !pass;
        }
        public long getParseDurationNS() {
            return parseDurationNS;
        }
        public String getErrorReport() {
            return errorReport;
        }

        @Override
        public String toString() {
            return testCase + errorReport;
        }

        @Override
        public boolean getAsBoolean() {
            return pass;
        }
    }

    private static final Logger LOG = LogManager.getLogger(TestCase.class);

    // For Kryo ONLY
    @SuppressWarnings("unused")
    private TestCase() {
        this.userAgent = "<<Should never appear after deserialization>>";
        this.testName = "<<Should never appear after deserialization>>";
        this.options =  Collections.emptyList();
        this.metadata = Collections.emptyMap();
        this.expected = Collections.emptyMap();
    }

    public TestCase(String userAgent, String testName) {
        this.userAgent = userAgent;
        this.testName = testName;
        this.options = new ArrayList<>();
        this.metadata = new LinkedHashMap<>();
        this.expected = new LinkedHashMap<>();
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getTestName() {
        return testName;
    }

    public List<String> getOptions() {
        return options;
    }

    public void addOption(String option) {
        this.options.add(option);
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }

    public Map<String, String> getExpected() {
        return expected;
    }

    public void expect(String key, String value) {
        this.expected.put(key, value);
    }

    private String spaceFiller(int length) {
        return filler(length, ' ');
    }
    private String minFiller(int length) {
        return filler(length, '-');
    }
    private String filler(int length, char charr) {
        if (length <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(charr);
        }
        return sb.toString();
    }

    private String logLine(String field, int maxFieldLength,
                             String exp,    int maxExpectedLength,
                             String actual, int maxActualLength) {
        if (field == null) {
            field = NULL_VALUE;
        }
        if (exp == null) {
            exp = NULL_VALUE;
        }
        if (actual == null) {
            actual = NULL_VALUE;
        }
        return
            " | " + field   + spaceFiller(maxFieldLength    - field.length())   +
            " | " + exp     + spaceFiller(maxExpectedLength - exp.length())     +
            " | " + actual  + spaceFiller(maxActualLength   - actual.length())  +
            " |";
    }

    private String logSeparator(int maxFieldLength,
                                int maxExpectedLength,
                                int maxActualLength) {
        return
            " |-" + minFiller(maxFieldLength)     +
            "-+-" + minFiller(maxExpectedLength)  +
            "-+-" + minFiller(maxActualLength)    +
            "-|";
    }

    public TestResult verify(Analyzer analyzer) {
        long startTime = System.nanoTime();
        UserAgent result = analyzer.parse(userAgent);
        long endTime = System.nanoTime();

        TestResult testResult = new TestResult();
        testResult.testCase = this;
        testResult.parseDurationNS = endTime-startTime;

        TreeSet<String> combinedKeys = new TreeSet<>();
        combinedKeys.addAll(expected.keySet());
        combinedKeys.addAll(result.toMap().keySet());
        combinedKeys.remove(USERAGENT_FIELDNAME); // Remove the input "field" from the result set.
        combinedKeys.remove(SYNTAX_ERROR);

        boolean passed = true;

        StringBuilder sb = new StringBuilder("\n");

        int maxFieldLength  = combinedKeys     .stream().filter(Objects::nonNull).map(String::length).max(Integer::compareTo).orElse(0);
        int maxExpectLength = expected.values().stream().filter(Objects::nonNull).map(String::length).max(Integer::compareTo).orElse(0);
        int maxActualLength = result.toMap().entrySet().stream()
            .filter(entry -> !entry.getKey().equals(USERAGENT_FIELDNAME))
            .map(Map.Entry::getValue)
            .filter(Objects::nonNull)
            .map(String::length)
            .max(Integer::compareTo)
            .orElse(0);

        sb.append(logSeparator(maxFieldLength, maxExpectLength, maxActualLength)).append('\n');
        sb.append(logLine("Field", maxFieldLength, "Expected", maxExpectLength, "Actual", maxActualLength)).append('\n');
        sb.append(logSeparator(maxFieldLength, maxExpectLength, maxActualLength)).append('\n');

        for (String key : combinedKeys) {
            String expectedValue = expected.get(key);
            String actualValue = result.getValue(key);
            sb.append(logLine(key, maxFieldLength, expectedValue, maxExpectLength, actualValue, maxActualLength));
            if (expectedValue == null) {
                // If we do not expect anything it is ok to get a Default value.
                if (!result.get(key).isDefaultValue()) {
                    passed = false;
                    sb.append(" --> UNEXPECTED");
                }
            } else {
                if (!expectedValue.equals(actualValue)) {
                    passed = false;
                    sb.append(" --> !!! FAIL !!!");
                }
            }
            sb.append('\n');
        }

        sb.append(logSeparator(maxFieldLength, maxExpectLength, maxActualLength)).append('\n');

        testResult.pass = passed;
        testResult.errorReport = sb.toString();
        return testResult;
    }

    @Override
    public String toString() {
        return "TestCase{" +
            "userAgent='" + userAgent + '\'' +
            ", testName='" + testName + '\'' +
            ", options=" + options +
            ", metadata=" + metadata +
            ", expected=" + expected +
            '}';
    }
}
