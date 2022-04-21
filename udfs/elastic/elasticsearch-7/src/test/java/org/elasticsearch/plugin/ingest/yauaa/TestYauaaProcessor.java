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

package org.elasticsearch.plugin.ingest.yauaa;

import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.apache.logging.log4j.LogManager;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.ingest.Processor.Factory;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static java.util.Collections.singletonMap;
import static nl.basjes.parse.useragent.UserAgent.USERAGENT_HEADER;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestYauaaProcessor {

    private static final String UA_FIELD = "source_field";
    private static final String PLATFORM_FIELD = "os";
    private static final String PLATFORM_VERSION_FIELD = "osversion";
    private static final String TARGET_FIELD = "target_field";

    @Test
    void testThatProcessorWorks() {
        Map<String, Object> document = new HashMap<>();
        document.put(UA_FIELD, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/100.0.4896.60 Safari/537.36");
        document.put(PLATFORM_FIELD, "\"macOS\"");
        document.put(PLATFORM_VERSION_FIELD, "\"12.3.1\"");

        Map<String, String> fieldToHeaderMapping = new TreeMap<>();
        fieldToHeaderMapping.put(UA_FIELD, USERAGENT_HEADER);
        fieldToHeaderMapping.put(PLATFORM_FIELD, "Sec-CH-UA-Platform");
        fieldToHeaderMapping.put(PLATFORM_VERSION_FIELD, "Sec-CH-UA-Platform-Version");

        IngestDocument ingestDocument = new IngestDocument("index", "type", "id", null, 42L, VersionType.EXTERNAL, document);

        UserAgentAnalyzer userAgentAnalyzer = UserAgentAnalyzer.newBuilder().build();
        YauaaProcessor      processor = new YauaaProcessor("tag", "description", fieldToHeaderMapping, TARGET_FIELD, userAgentAnalyzer);
        Map<String, Object> data      = processor.execute(ingestDocument).getSourceAndMetadata();

        MatcherAssert.assertThat(data, hasKey(TARGET_FIELD));

        Object targetFieldData = data.get(TARGET_FIELD);
        assertTrue(targetFieldData instanceof Map, "Wrong type");
        Map<?, ?> results = (Map<?, ?>) targetFieldData;

        assertHasKValue(results, "DeviceClass",                      "Desktop");
        assertHasKValue(results, "DeviceBrand",                      "Apple");
        assertHasKValue(results, "DeviceName",                       "Apple Macintosh");
        assertHasKValue(results, "DeviceCpu",                        "Intel");
        assertHasKValue(results, "DeviceCpuBits",                    "64");

        assertHasKValue(results, "OperatingSystemClass",             "Desktop");
        assertHasKValue(results, "OperatingSystemName",              "Mac OS");
        assertHasKValue(results, "OperatingSystemNameVersion",       "Mac OS 12.3.1");
        assertHasKValue(results, "OperatingSystemNameVersionMajor",  "Mac OS 12");
        assertHasKValue(results, "OperatingSystemVersion",           "12.3.1");
        assertHasKValue(results, "OperatingSystemVersionMajor",      "12");

        assertHasKValue(results, "LayoutEngineClass",                "Browser");
        assertHasKValue(results, "LayoutEngineName",                 "Blink");
        assertHasKValue(results, "LayoutEngineNameVersion",          "Blink 100.0");
        assertHasKValue(results, "LayoutEngineNameVersionMajor",     "Blink 100");
        assertHasKValue(results, "LayoutEngineVersion",              "100.0");
        assertHasKValue(results, "LayoutEngineVersionMajor",         "100");

        assertHasKValue(results, "AgentClass",                       "Browser");
        assertHasKValue(results, "AgentName",                        "Chrome");
        assertHasKValue(results, "AgentVersion",                     "100.0.4896.60");
        assertHasKValue(results, "AgentVersionMajor",                "100");
        assertHasKValue(results, "AgentNameVersion",                 "Chrome 100.0.4896.60");
        assertHasKValue(results, "AgentNameVersionMajor",            "Chrome 100");

        assertHasKValue(results, "UAClientHintPlatform",             "macOS");
        assertHasKValue(results, "UAClientHintPlatformVersion",      "12.3.1");

        assertHasKValue(results, "__SyntaxError__",                  "false");
    }

    @Test
    void testExtraRules() {
        Map<String, String> fieldToHeaderMapping = new TreeMap<>();
        fieldToHeaderMapping.put(UA_FIELD, USERAGENT_HEADER);

        Map<String, Object> document = new HashMap<>();
        document.put(UA_FIELD,
            "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/53.0.2785.124 Mobile Safari/537.36");
        IngestDocument ingestDocument = new IngestDocument("index", "type", "id", null, 42L, VersionType.EXTERNAL, document);

        UserAgentAnalyzer userAgentAnalyzer = UserAgentAnalyzer.newBuilder()
            .withFields(Arrays.asList("DeviceClass", "DeviceBrand", "DeviceName", "AgentNameVersionMajor", "FirstProductName"))
            .withCache(10)
            .preheat(10)
            .addYamlRule("config:\n- matcher:\n    extract:\n      - 'FirstProductName     : 1 :agent.(1)product.(1)name'\n")
            .build();
        YauaaProcessor      processor = new YauaaProcessor("tag", "description", fieldToHeaderMapping, TARGET_FIELD, userAgentAnalyzer);
        Map<String, Object> data      = processor.execute(ingestDocument).getSourceAndMetadata();

        MatcherAssert.assertThat(data, hasKey(TARGET_FIELD));

        Object targetFieldData = data.get(TARGET_FIELD);
        assertTrue(targetFieldData instanceof Map, "Wrong type");
        Map<?, ?> results = (Map<?, ?>) targetFieldData;

        // The EXPLICITLY requested fields
        assertHasKValue(results, "FirstProductName",        "Mozilla");
        assertHasKValue(results, "DeviceClass",             "Phone");
        assertHasKValue(results, "DeviceBrand",             "Google");
        assertHasKValue(results, "DeviceName",              "Google Nexus 6");
        assertHasKValue(results, "AgentNameVersionMajor",   "Chrome 53");

        // The IMPLICITLY requested fields (i.e. partials of the actually requested ones)
        assertHasKValue(results, "AgentName",               "Chrome");
        assertHasKValue(results, "AgentVersion",            "53.0.2785.124");
        assertHasKValue(results, "AgentVersionMajor",       "53");

        // The NOT requested fields
        assertHasNotKey(results, "OperatingSystemClass");
        assertHasNotKey(results, "OperatingSystemName");
        assertHasNotKey(results, "OperatingSystemNameVersion");
        assertHasNotKey(results, "OperatingSystemNameVersionMajor");
        assertHasNotKey(results, "OperatingSystemVersion");
        assertHasNotKey(results, "OperatingSystemVersionBuild");
        assertHasNotKey(results, "OperatingSystemVersionMajor");
        assertHasNotKey(results, "LayoutEngineClass");
        assertHasNotKey(results, "LayoutEngineName");
        assertHasNotKey(results, "LayoutEngineNameVersion");
        assertHasNotKey(results, "LayoutEngineNameVersionMajor");
        assertHasNotKey(results, "LayoutEngineVersion");
        assertHasNotKey(results, "LayoutEngineVersionMajor");
        assertHasNotKey(results, "AgentClass");
        assertHasNotKey(results, "AgentNameVersion");
    }

    private void assertHasKValue(Map<?, ?> results, String key, String value) {
        MatcherAssert.assertThat(results, hasEntry(key, value));
    }

    private void assertHasNotKey(Map<?, ?> results, String key) {
        MatcherAssert.assertThat(results, not(hasKey(key)));
    }

    @Test
    void testIngestPlugin() throws Exception {
        IngestYauaaPlugin plugin = new IngestYauaaPlugin();

        Map<String, Factory> processors = plugin.getProcessors(null);

        Factory yauaaFactory = processors.get("yauaa");

        Map<String, Object>  configuration = new HashMap<>();

        configuration.put("field",        UA_FIELD);
        configuration.put("target_field", TARGET_FIELD);
        configuration.put("fieldNames",   Arrays.asList("DeviceClass", "DeviceBrand", "DeviceName", "AgentNameVersionMajor", "FirstProductName"));
        configuration.put("cacheSize",    10);
        configuration.put("preheat",      10);
        configuration.put("extraRules",   "config:\n- matcher:\n    extract:\n      - 'FirstProductName     : 1 :agent.(1)product.(1)name'\n");

        Processor processor = yauaaFactory.create(processors, "tag", "description", configuration);

        Assertions.assertEquals("yauaa", processor.getType());

        Map<String, Object> document = new HashMap<>();
        document.put(UA_FIELD,
            "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/53.0.2785.124 Mobile Safari/537.36");
        IngestDocument ingestDocument = new IngestDocument("index", "type", "id", null, 42L, VersionType.EXTERNAL, document);

        Map<String, Object> data      = processor.execute(ingestDocument).getSourceAndMetadata();

        MatcherAssert.assertThat(data, hasKey(TARGET_FIELD));

        Object targetFieldData = data.get(TARGET_FIELD);
        assertTrue(targetFieldData instanceof Map, "Wrong type");
        Map<?, ?> results = (Map<?, ?>) targetFieldData;

        // The EXPLICITLY requested fields
        assertHasKValue(results, "FirstProductName",        "Mozilla");
        assertHasKValue(results, "DeviceClass",             "Phone");
        assertHasKValue(results, "DeviceBrand",             "Google");
        assertHasKValue(results, "DeviceName",              "Google Nexus 6");
        assertHasKValue(results, "AgentNameVersionMajor",   "Chrome 53");

        // The IMPLICITLY requested fields (i.e. partials of the actually requested ones)
        assertHasKValue(results, "AgentName",               "Chrome");
        assertHasKValue(results, "AgentVersion",            "53.0.2785.124");
        assertHasKValue(results, "AgentVersionMajor",       "53");

        // The NOT requested fields
        assertHasNotKey(results, "OperatingSystemClass");
        assertHasNotKey(results, "OperatingSystemName");
        assertHasNotKey(results, "OperatingSystemNameVersion");
        assertHasNotKey(results, "OperatingSystemNameVersionMajor");
        assertHasNotKey(results, "OperatingSystemVersion");
        assertHasNotKey(results, "OperatingSystemVersionBuild");
        assertHasNotKey(results, "OperatingSystemVersionMajor");
        assertHasNotKey(results, "LayoutEngineClass");
        assertHasNotKey(results, "LayoutEngineName");
        assertHasNotKey(results, "LayoutEngineNameVersion");
        assertHasNotKey(results, "LayoutEngineNameVersionMajor");
        assertHasNotKey(results, "LayoutEngineVersion");
        assertHasNotKey(results, "LayoutEngineVersionMajor");
        assertHasNotKey(results, "AgentClass");
        assertHasNotKey(results, "AgentNameVersion");

        LogManager.getLogger("TestYauaaProcessor").info("Complete set of returned results:{}", results);
    }

    @Test
    void testThatBadHeadersFail() {
        IngestYauaaPlugin plugin = new IngestYauaaPlugin();

        Map<String, Factory> processors = plugin.getProcessors(null);

        Factory yauaaFactory = processors.get("yauaa");

        Map<String, Object>  configuration = new HashMap<>();

        configuration.put("field_to_header_mapping", singletonMap("Something", "to unsupported header name"));
        configuration.put("target_field", TARGET_FIELD);
        configuration.put("fieldNames",   Arrays.asList("DeviceClass", "DeviceBrand", "DeviceName", "AgentNameVersionMajor", "FirstProductName"));
        configuration.put("cacheSize",    10);
        configuration.put("preheat",      10);
        configuration.put("extraRules",   "config:\n- matcher:\n    extract:\n      - 'FirstProductName     : 1 :agent.(1)product.(1)name'\n");

        assertThrows(IllegalArgumentException.class, () -> {
            Processor processor = yauaaFactory.create(processors, "tag", "description", configuration);
        });
    }

}
