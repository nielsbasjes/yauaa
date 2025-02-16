/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2025 Niels Basjes
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

package nl.basjes.parse.useragent.trino;

import io.trino.metadata.InternalFunctionBundle;
import io.trino.spi.type.MapType;
import io.trino.spi.type.TypeOperators;
import io.trino.sql.query.QueryAssertions;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static io.trino.spi.type.VarcharType.VARCHAR;
import static nl.basjes.parse.useragent.UserAgent.SYNTAX_ERROR;
import static nl.basjes.parse.useragent.UserAgent.UNKNOWN_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.UNKNOWN_VALUE;
import static nl.basjes.parse.useragent.UserAgent.UNKNOWN_VERSION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
class TestParseFunction {

    private QueryAssertions assertions;
    private List<String> allPossibleFieldNamesSorted;

    @BeforeAll
    public void setUp() {
        assertions = new QueryAssertions();
        assertions.addFunctions(InternalFunctionBundle.extractFunctions(new YauaaPlugin().getFunctions()));

        allPossibleFieldNamesSorted = UserAgentAnalyzer
            .newBuilder()
            .hideMatcherLoadStats()
            .withCache(0)
            .delayInitialization()
            .build()
            .getAllPossibleFieldNamesSorted();
    }

    @AfterAll
    public void teardown() {
        assertions.close();
        assertions = null;
    }

    private static String getDefaultValueForField(String fieldName) {
        if (fieldName.equals(SYNTAX_ERROR)) {
            return "false";
        }
        if (fieldName.equals("RemarkablePattern")) {
            return "Nothing remarkable found";
        }
        if (fieldName.contains("NameVersion")) {
            return UNKNOWN_NAME_VERSION;
        }
        if (fieldName.contains("Version")) {
            return UNKNOWN_VERSION;
        }
        return UNKNOWN_VALUE;
    }

    @Test
    void testParser() {
        Map<String, String> expected = new TreeMap<>();

        // We will get ALL possible fields, most are effectively "Unknown"
        for (String fieldName : allPossibleFieldNamesSorted) {
            expected.put(fieldName, getDefaultValueForField(fieldName));
        }

        String useragent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36";

        expected.put("DeviceClass",                     "Desktop");
        expected.put("DeviceName",                      "Linux Desktop");
        expected.put("DeviceBrand",                     "Unknown");
        expected.put("DeviceCpu",                       "Intel x86_64");
        expected.put("DeviceCpuBits",                   "64");

        expected.put("OperatingSystemClass",            "Desktop");
        expected.put("OperatingSystemName",             "Linux");
        expected.put("OperatingSystemVersion",          "??");
        expected.put("OperatingSystemVersionMajor",     "??");
        expected.put("OperatingSystemNameVersion",      "Linux ??");
        expected.put("OperatingSystemNameVersionMajor", "Linux ??");

        expected.put("LayoutEngineClass",               "Browser");
        expected.put("LayoutEngineName",                "Blink");
        expected.put("LayoutEngineVersion",             "98.0");
        expected.put("LayoutEngineVersionMajor",        "98");
        expected.put("LayoutEngineNameVersion",         "Blink 98.0");
        expected.put("LayoutEngineNameVersionMajor",    "Blink 98");

        expected.put("AgentClass",                      "Browser");
        expected.put("AgentName",                       "Chrome");
        expected.put("AgentVersion",                    "98.0.4758.102");
        expected.put("AgentVersionMajor",               "98");
        expected.put("AgentNameVersion",                "Chrome 98.0.4758.102");
        expected.put("AgentNameVersionMajor",           "Chrome 98");

        assertThat(assertions.function("parse_user_agent", "'"+useragent+"'"))
            .hasType(new MapType(VARCHAR, VARCHAR,  new TypeOperators()))
            .isEqualTo(expected);
    }
}
