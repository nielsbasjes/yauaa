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

package nl.basjes.parse.useragent;

import nl.basjes.parse.useragent.AnalyzerUtilities.ParsedArguments;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static nl.basjes.parse.useragent.AnalyzerUtilities.parseArguments;
import static nl.basjes.parse.useragent.UserAgent.USERAGENT_HEADER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestArgumentParser {

    List<String> allAllowedFields   = Arrays.asList("Field1", "Field2", "Field3");
    List<String> allAllowedHeaders  = Arrays.asList(USERAGENT_HEADER, "Head1", "Head2", "Head3");

    private void assertEmpty(List<?> list) {
        assertEquals(Collections.EMPTY_LIST, list);
    }

    private void assertEmpty(Map<?, ?> map) {
        assertEquals(Collections.EMPTY_MAP, map);
    }

    @Test
    void testEmptyList() {
        String[] args = {};
        ParsedArguments parsedArguments = parseArguments(args, allAllowedFields, allAllowedHeaders);

        assertEmpty(parsedArguments.getRequestHeaders());
        assertEmpty(parsedArguments.getWantedFields());
    }

    @Test
    void testOnlyUserAgent() {
        String[] args = {
            "Mozilla"
        };
        ParsedArguments parsedArguments = parseArguments(args, allAllowedFields, allAllowedHeaders);

        Map<String, String> expectedHeaders = new TreeMap<>();
        expectedHeaders.put(USERAGENT_HEADER, "Mozilla");

        List<String> expectedWantedFields = new ArrayList<>();
        // expectedWantedFields is empty

        assertEquals(expectedHeaders, parsedArguments.getRequestHeaders());
        assertEquals(expectedWantedFields, parsedArguments.getWantedFields());
    }


    @Test
    void testOnlyUserAgentEmpty() {
        String[] args = {
            ""
        };
        ParsedArguments parsedArguments = parseArguments(args, allAllowedFields, allAllowedHeaders);

        Map<String, String> expectedHeaders = new TreeMap<>();
        expectedHeaders.put(USERAGENT_HEADER, "");

        List<String> expectedWantedFields = new ArrayList<>();
        // expectedWantedFields is empty

        assertEquals(expectedHeaders, parsedArguments.getRequestHeaders());
        assertEquals(expectedWantedFields, parsedArguments.getWantedFields());
    }

    @Test
    void testOnlyUserAgentNull() {
        String[] args = {
            null
        };
        ParsedArguments parsedArguments = parseArguments(args, allAllowedFields, allAllowedHeaders);

        Map<String, String> expectedHeaders = new TreeMap<>();
        expectedHeaders.put(USERAGENT_HEADER, null);

        List<String> expectedWantedFields = new ArrayList<>();
        // expectedWantedFields is empty

        assertEquals(expectedHeaders, parsedArguments.getRequestHeaders());
        assertEquals(expectedWantedFields, parsedArguments.getWantedFields());
    }

    @Test
    void testMixed() {
        String[] args = {
            "Mozilla",
            "Head1", "HeadValue1",
            "Field1",
            "Head2", "HeadValue2",
            "Field2"
        };
        ParsedArguments parsedArguments = parseArguments(args, allAllowedFields, allAllowedHeaders);

        Map<String, String> expectedHeaders = new TreeMap<>();
        expectedHeaders.put(USERAGENT_HEADER, "Mozilla");
        expectedHeaders.put("Head1", "HeadValue1");
        expectedHeaders.put("Head2", "HeadValue2");

        List<String> expectedWantedFields = new ArrayList<>();
        expectedWantedFields.add("Field1");
        expectedWantedFields.add("Field2");

        assertEquals(expectedHeaders,       parsedArguments.getRequestHeaders());
        assertEquals(expectedWantedFields,  parsedArguments.getWantedFields());
    }


    @Test
    void testMixedIgnoreTrailingNulls() {
        String[] args = {
            "Mozilla",
            "Head1", "HeadValue1",
            "Field1",
            "Head2", "HeadValue2",
            "Field2",
            null, null, null, null, null, null, null, null, null, null
        };
        ParsedArguments parsedArguments = parseArguments(args, allAllowedFields, allAllowedHeaders);

        Map<String, String> expectedHeaders = new TreeMap<>();
        expectedHeaders.put(USERAGENT_HEADER, "Mozilla");
        expectedHeaders.put("Head1", "HeadValue1");
        expectedHeaders.put("Head2", "HeadValue2");

        List<String> expectedWantedFields = new ArrayList<>();
        expectedWantedFields.add("Field1");
        expectedWantedFields.add("Field2");

        assertEquals(expectedHeaders,       parsedArguments.getRequestHeaders());
        assertEquals(expectedWantedFields,  parsedArguments.getWantedFields());
    }

    @Test
    void testHeaders() {
        String[] args = {
            "Mozilla",
            "Head1", "HeadValue1",
            "Head2", "HeadValue2"
        };
        ParsedArguments parsedArguments = parseArguments(args, allAllowedFields, allAllowedHeaders);

        Map<String, String> expectedHeaders = new TreeMap<>();
        expectedHeaders.put(USERAGENT_HEADER, "Mozilla");
        expectedHeaders.put("Head1", "HeadValue1");
        expectedHeaders.put("Head2", "HeadValue2");

        assertEquals(expectedHeaders,       parsedArguments.getRequestHeaders());
        assertEmpty(parsedArguments.getWantedFields());
    }

    @Test
    void testFields() {
        String[] args = {
            "Mozilla",
            "Field1",
            "Field2"
        };
        ParsedArguments parsedArguments = parseArguments(args, allAllowedFields, allAllowedHeaders);

        Map<String, String> expectedHeaders = new TreeMap<>();
        expectedHeaders.put(USERAGENT_HEADER, "Mozilla");

        List<String> expectedWantedFields = new ArrayList<>();
        expectedWantedFields.add("Field1");
        expectedWantedFields.add("Field2");

        assertEquals(expectedHeaders,       parsedArguments.getRequestHeaders());
        assertEquals(expectedWantedFields,  parsedArguments.getWantedFields());
    }


    @Test
    void testFields2() {
        String[] args = {
            "User-Agent", "Mozilla",
            "Field1",
            "Field2"
        };
        ParsedArguments parsedArguments = parseArguments(args, allAllowedFields, allAllowedHeaders);

        Map<String, String> expectedHeaders = new TreeMap<>();
        expectedHeaders.put(USERAGENT_HEADER, "Mozilla");

        List<String> expectedWantedFields = new ArrayList<>();
        expectedWantedFields.add("Field1");
        expectedWantedFields.add("Field2");

        assertEquals(expectedHeaders,       parsedArguments.getRequestHeaders());
        assertEquals(expectedWantedFields,  parsedArguments.getWantedFields());
    }


    @Test
    void testFields3() {
        String[] args = {
            "Field1",
            "User-Agent", "Mozilla",
            "Field2"
        };
        ParsedArguments parsedArguments = parseArguments(args, allAllowedFields, allAllowedHeaders);

        Map<String, String> expectedHeaders = new TreeMap<>();
        expectedHeaders.put(USERAGENT_HEADER, "Mozilla");

        List<String> expectedWantedFields = new ArrayList<>();
        expectedWantedFields.add("Field1");
        expectedWantedFields.add("Field2");

        assertEquals(expectedHeaders,       parsedArguments.getRequestHeaders());
        assertEquals(expectedWantedFields,  parsedArguments.getWantedFields());
    }

    @Test
    void testMixed2() {
        String[] args = {
            "Head2", "HeadValue2",
            "Field1",
            "User-Agent", "Mozilla",
            "Head1", "HeadValue1",
            "Field2"
        };
        ParsedArguments parsedArguments = parseArguments(args, allAllowedFields, allAllowedHeaders);

        Map<String, String> expectedHeaders = new TreeMap<>();
        expectedHeaders.put(USERAGENT_HEADER, "Mozilla");
        expectedHeaders.put("Head1", "HeadValue1");
        expectedHeaders.put("Head2", "HeadValue2");

        List<String> expectedWantedFields = new ArrayList<>();
        expectedWantedFields.add("Field1");
        expectedWantedFields.add("Field2");

        assertEquals(expectedHeaders,       parsedArguments.getRequestHeaders());
        assertEquals(expectedWantedFields,  parsedArguments.getWantedFields());
    }

    @Test
    void testMixedEmptyHeaders() {
        String[] args = {
            "Mozilla",
            "Head1", "",
            "Field1",
            "Head2", "",
            "Field2"
        };
        ParsedArguments parsedArguments = parseArguments(args, allAllowedFields, allAllowedHeaders);

        Map<String, String> expectedHeaders = new TreeMap<>();
        expectedHeaders.put(USERAGENT_HEADER, "Mozilla");
        expectedHeaders.put("Head1", "");
        expectedHeaders.put("Head2", "");

        List<String> expectedWantedFields = new ArrayList<>();
        expectedWantedFields.add("Field1");
        expectedWantedFields.add("Field2");

        assertEquals(expectedHeaders,       parsedArguments.getRequestHeaders());
        assertEquals(expectedWantedFields,  parsedArguments.getWantedFields());
    }

    @Test
    void testBADMissingHeaderValue1() {
        String[] args = {
            "Head2"
        };
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> {
            parseArguments(args, allAllowedFields, allAllowedHeaders);
        });
        String message = illegalArgumentException.getMessage();
        assertTrue(message.contains("Invalid last element in argument list"), message);
    }


    @Test
    void testBADMissingHeaderValue2() {
        String[] args = {
            "Mozilla",
            "Head1", "",
            "Field1",
            "Field2",
            "Head2"
        };
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> {
            parseArguments(args, allAllowedFields, allAllowedHeaders);
        });
        String message = illegalArgumentException.getMessage();
        assertTrue(message.contains("Invalid last element in argument list"), message);
    }


    @Test
    void testBADMissingHeaderValue3() {
        String[] args = {
            "Field1",
            "User-Agent",      "Mozilla",
            "Field2",
            "Head1",           "Linux",
            "Field3",
            "Head2"
        };
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> {
            parseArguments(args, allAllowedFields, allAllowedHeaders);
        });
        String message = illegalArgumentException.getMessage();
        assertTrue(message.contains("Invalid last element in argument list"), message);
    }


    @Test
    void testBADMixedUnknownEntry() {
        String[] args = {
            "Mozilla",
            "Head1", "",
            "Unknown",
            "Head2", "",
            "Field2"
        };
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> {
            parseArguments(args, allAllowedFields, allAllowedHeaders);
        });
        String message = illegalArgumentException.getMessage();
        assertTrue(message.contains("Unknown argument provided"), message);
    }


    @Test
    void testBADMixedEmptyEntry() {
        String[] args = {
            "Mozilla",
            "Head1", "",
            "",
            "Head2", "",
            "Field2"
        };
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> {
            parseArguments(args, allAllowedFields, allAllowedHeaders);
        });
        String message = illegalArgumentException.getMessage();
        assertTrue(message.contains("Empty argument provided"), message);
    }


    @Test
    void testBADMixedNullEntry() {
        String[] args = {
            "Mozilla",
            "Head1", "",
            null,
            null,
            null,
            null,
            null,
            "Head2", "",
            "Field2"
        };
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> {
            parseArguments(args, allAllowedFields, allAllowedHeaders);
        });
        String message = illegalArgumentException.getMessage();
        assertTrue(message.contains("Null argument provided"), message);
    }

}
