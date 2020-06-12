/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2020 Niels Basjes
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

import org.elasticsearch.index.VersionType;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

@SuppressWarnings("unchecked")
public class YauaaProcessorTest {

    private static final String SOURCE_FIELD = "source_field";
    private static final String TARGET_FIELD = "target_field";

    @Test
    public void testThatProcessorWorks() {
        Map<String, Object> document = new HashMap<>();
        document.put(SOURCE_FIELD,
            "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/53.0.2785.124 Mobile Safari/537.36");
        IngestDocument ingestDocument = new IngestDocument("index", "type", "id", null, 42L, VersionType.EXTERNAL, document);

        YauaaProcessor      processor = new YauaaProcessor("tag", SOURCE_FIELD, TARGET_FIELD, null, -1, -1, null);
        Map<String, Object> data      = processor.execute(ingestDocument).getSourceAndMetadata();

        MatcherAssert.assertThat(data, hasKey(TARGET_FIELD));

        Map<String, String> results = (Map<String, String>) data.get(TARGET_FIELD);

        assertHasKValue(results, "DeviceClass",                      "Phone");
        assertHasKValue(results, "DeviceBrand",                      "Google");
        assertHasKValue(results, "DeviceName",                       "Google Nexus 6");
        assertHasKValue(results, "OperatingSystemClass",             "Mobile");
        assertHasKValue(results, "OperatingSystemName",              "Android");
        assertHasKValue(results, "OperatingSystemNameVersion",       "Android 7.0");
        assertHasKValue(results, "OperatingSystemNameVersionMajor",  "Android 7");
        assertHasKValue(results, "OperatingSystemVersion",           "7.0");
        assertHasKValue(results, "OperatingSystemVersionBuild",      "NBD90Z");
        assertHasKValue(results, "OperatingSystemVersionMajor",      "7");
        assertHasKValue(results, "LayoutEngineClass",                "Browser");
        assertHasKValue(results, "LayoutEngineName",                 "Blink");
        assertHasKValue(results, "LayoutEngineNameVersion",          "Blink 53.0");
        assertHasKValue(results, "LayoutEngineNameVersionMajor",     "Blink 53");
        assertHasKValue(results, "LayoutEngineVersion",              "53.0");
        assertHasKValue(results, "LayoutEngineVersionMajor",         "53");
        assertHasKValue(results, "AgentClass",                       "Browser");
        assertHasKValue(results, "AgentName",                        "Chrome");
        assertHasKValue(results, "AgentNameVersion",                 "Chrome 53.0.2785.124");
        assertHasKValue(results, "AgentNameVersionMajor",            "Chrome 53");
        assertHasKValue(results, "AgentVersion",                     "53.0.2785.124");
        assertHasKValue(results, "AgentVersionMajor",                "53");
    }

    @Test
    public void testExtraRules() {
        Map<String, Object> document = new HashMap<>();
        document.put(SOURCE_FIELD,
            "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/53.0.2785.124 Mobile Safari/537.36");
        IngestDocument ingestDocument = new IngestDocument("index", "type", "id", null, 42L, VersionType.EXTERNAL, document);

        List<String> fieldNames = Arrays.asList("DeviceClass", "DeviceBrand", "DeviceName", "AgentNameVersionMajor", "FirstProductName");
        Integer      cacheSize  = 10;
        Integer      preheat    = 10;
        String       extraRules = "config:\n- matcher:\n    extract:\n      - 'FirstProductName     : 1 :agent.(1)product.(1)name'\n";

        YauaaProcessor      processor = new YauaaProcessor("tag", SOURCE_FIELD, TARGET_FIELD, fieldNames, cacheSize, preheat, extraRules);
        Map<String, Object> data      = processor.execute(ingestDocument).getSourceAndMetadata();

        MatcherAssert.assertThat(data, hasKey(TARGET_FIELD));

        Map<String, String> results = (Map<String, String>) data.get(TARGET_FIELD);

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

    private void assertHasKValue(Map<String, String> results, String key, String value) {
        MatcherAssert.assertThat(results, hasEntry(key, value));
    }

    private void assertHasNotKey(Map<String, String> results, String key) {
        MatcherAssert.assertThat(results, not(hasKey(key)));
    }

    @Test
    public void testIngestPlugin() throws Exception {
        IngestYauaaPlugin plugin = new IngestYauaaPlugin();

        Map<String, Processor.Factory> processors = plugin.getProcessors(null);

        Processor.Factory yauaaFactory = processors.get("yauaa");

        Map<String, Object>  configuration = new HashMap<>();

        configuration.put("field",        SOURCE_FIELD);
        configuration.put("target_field", TARGET_FIELD);
        configuration.put("fieldNames",   Arrays.asList("DeviceClass", "DeviceBrand", "DeviceName", "AgentNameVersionMajor", "FirstProductName"));
        configuration.put("cacheSize",    10);
        configuration.put("preheat",      10);
        configuration.put("extraRules",   "config:\n- matcher:\n    extract:\n      - 'FirstProductName     : 1 :agent.(1)product.(1)name'\n");

        Processor processor = yauaaFactory.create(processors, "tag", configuration);

        assertEquals("yauaa", processor.getType());

        Map<String, Object> document = new HashMap<>();
        document.put(SOURCE_FIELD,
            "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/53.0.2785.124 Mobile Safari/537.36");
        IngestDocument ingestDocument = new IngestDocument("index", "type", "id", null, 42L, VersionType.EXTERNAL, document);

        Map<String, Object> data      = processor.execute(ingestDocument).getSourceAndMetadata();

        MatcherAssert.assertThat(data, hasKey(TARGET_FIELD));

        Map<String, String> results    = (Map<String, String>) data.get(TARGET_FIELD);

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

        LoggerFactory.getLogger("TestYauaaProcessor").info("Complete set of returned results:{}", results);
    }

}
