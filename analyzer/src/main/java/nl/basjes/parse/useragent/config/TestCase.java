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

import nl.basjes.parse.useragent.Analyzer;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentStringMatchMaker;
import nl.basjes.parse.useragent.analyze.MatchMaker;
import nl.basjes.parse.useragent.utils.StringTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BooleanSupplier;

import static nl.basjes.parse.useragent.UserAgent.SYNTAX_ERROR;
import static nl.basjes.parse.useragent.UserAgent.USERAGENT_FIELDNAME;
import static nl.basjes.parse.useragent.UserAgent.USERAGENT_HEADER;


public class TestCase implements Serializable {
    private final Map<String, String> headers;
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

    // For Kryo ONLY
    @SuppressWarnings("unused")
    private TestCase() {
        this.headers = Collections.emptyMap();
        this.testName = "<<Should never appear after deserialization>>";
        this.options = Collections.emptyList();
        this.metadata = Collections.emptyMap();
        this.expected = Collections.emptyMap();
    }

    public TestCase(Map<String, String> headers, String testName) {
        this.headers = headers;
        this.testName = testName;
        this.options = new ArrayList<>();
        this.metadata = new LinkedHashMap<>();
        this.expected = new LinkedHashMap<>();
    }

    public TestCase(String userAgent, String testName) {
        this.headers = new TreeMap<>();
        this.headers.put(USERAGENT_HEADER, userAgent);
        this.testName = testName;
        this.options = new ArrayList<>();
        this.metadata = new LinkedHashMap<>();
        this.expected = new LinkedHashMap<>();
    }

    public String getUserAgent() {
        return this.headers.get(USERAGENT_HEADER);
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public void addHeader(String name, String value) {
        this.headers.put(name, value);
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

    public TestResult verify(Analyzer analyzer) {
        UserAgent result;
        long startTime = System.nanoTime();
        result = analyzer.parse(getHeaders());
        long endTime = System.nanoTime();

        TestResult testResult = new TestResult();
        testResult.testCase = this;
        testResult.parseDurationNS = endTime - startTime;

        TreeSet<String> combinedKeys = new TreeSet<>();
        combinedKeys.addAll(expected.keySet());
        combinedKeys.addAll(result.toMap().keySet());
        combinedKeys.remove(USERAGENT_FIELDNAME); // Remove the input "field" from the result set.
        combinedKeys.remove(SYNTAX_ERROR);

        boolean passed = true;

        StringBuilder sb = new StringBuilder("\n");

        sb.append(">>>>>>>>>>>>>> ").append(testName).append(" <<<<<<<<<<<<<<\n");
        StringTable inputTable = new StringTable().withHeaders("Header", "Value");
        getHeaders().forEach(inputTable::addRow);
        sb.append(inputTable).append('\n');

        StringTable resultTable = new StringTable().withHeaders("Field", "Expected", "Actual");

        for (String key : combinedKeys) {
            String expectedValue = expected.get(key);
            String actualValue = result.getValue(key);

            if (expectedValue == null && result.get(key).isDefaultValue()) {
                continue;
            }
            List<String> fields = new ArrayList<>();
            fields.add(key);
            fields.add(expectedValue);
            fields.add(actualValue);

            if (expectedValue == null) {
                // If we do not expect anything it is ok to get a Default value.
                if (!result.get(key).isDefaultValue()) {
                    passed = false;
                    fields.add(" --> UNEXPECTED");
                }
            } else {
                if (!expectedValue.equals(actualValue)) {
                    passed = false;
                    fields.add(" --> !!! FAIL !!!");
                }
            }
            resultTable.addRow(fields);
        }
        sb.append(resultTable);

        testResult.pass = passed;
        testResult.errorReport = sb.toString();
        return testResult;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================================================\n");
        sb.append("TestCase: >>>>>>>>>>>>>> ").append(testName).append(" <<<<<<<<<<<<<<\n");

        if (options != null && !options.isEmpty()) {
            StringTable optionsTable = new StringTable().withHeaders("Option");
            options.forEach(optionsTable::addRow);
            sb.append(optionsTable).append('\n');
        }
        if (metadata != null && !metadata.isEmpty()) {
            StringTable metadataTable = new StringTable().withHeaders("Metadata", "Value");
            metadata.forEach(metadataTable::addRow);
            sb.append(metadataTable).append('\n');
        }

        StringTable inputTable = new StringTable().withHeaders("Header", "Value");
        getHeaders().forEach(inputTable::addRow);
        sb.append(inputTable).append('\n');

        StringTable expectedTable = new StringTable().withHeaders("Field", "Expected Value");
        expected.forEach(expectedTable::addRow);
        sb.append(expectedTable).append('\n');
        sb.append("====================================================================================\n");

        return sb.toString();
    }
}
