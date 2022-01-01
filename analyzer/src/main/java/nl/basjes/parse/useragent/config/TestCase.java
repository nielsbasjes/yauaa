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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static nl.basjes.parse.useragent.UserAgent.SYNTAX_ERROR;
import static nl.basjes.parse.useragent.UserAgent.USERAGENT_FIELDNAME;

public class TestCase implements Serializable {
    private final String userAgent;
    private final String testName;
    private final List<String> options;
    private final Map<String, String> metadata;
    private final Map<String, String> expected;

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

    public boolean verify(Analyzer analyzer) {
        UserAgent result = analyzer.parse(userAgent);

        TreeSet<String> combinedKeys = new TreeSet<>();
        combinedKeys.addAll(expected.keySet());
        combinedKeys.addAll(result.toMap().keySet());
        combinedKeys.remove(USERAGENT_FIELDNAME); // Remove the input "field" from the result set.
        combinedKeys.remove(SYNTAX_ERROR);

        for (String key : combinedKeys) {
            String expectedValue = expected.get(key);
            if (expectedValue == null) {
                // If we do not expect anything it is ok to get a Default value.
                if (!result.get(key).isDefaultValue()) {
                    return false;
                }
            } else {
                String actualValue = result.getValue(key);
                if (!expectedValue.equals(actualValue)) {
                    return false;
                }
            }
        }
        return true;
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
