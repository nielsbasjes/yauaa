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

import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.RandomDocumentPicks;
import org.elasticsearch.test.ESTestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;

public class YauaaProcessorTests extends ESTestCase {

    private static final String SOURCE_FIELD = "source_field";
    private static final String TARGET_FIELD = "target_field";

    @SuppressWarnings("unchecked")
    @Test
    public void testThatProcessorWorks() throws Exception {
        Map<String, Object> document = new HashMap<>();
        document.put(SOURCE_FIELD,
            "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/53.0.2785.124 Mobile Safari/537.36");
        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(random(), document);

        YauaaProcessor      processor = new YauaaProcessor(randomAlphaOfLength(10), SOURCE_FIELD, TARGET_FIELD, null, -1, -1, null);
        Map<String, Object> data      = processor.execute(ingestDocument).getSourceAndMetadata();

        MatcherAssert.assertThat(data, hasKey(TARGET_FIELD));

        Map<String, String> results = (Map<String, String>) data.get(TARGET_FIELD);

        MatcherAssert.assertThat(results, hasEntry("DeviceClass", "Phone"));
        MatcherAssert.assertThat(results, hasEntry("DeviceBrand", "Google"));
        MatcherAssert.assertThat(results, hasEntry("DeviceName", "Google Nexus 6"));
        MatcherAssert.assertThat(results, hasEntry("OperatingSystemClass", "Mobile"));
        MatcherAssert.assertThat(results, hasEntry("OperatingSystemName", "Android"));
        MatcherAssert.assertThat(results, hasEntry("OperatingSystemNameVersion", "Android 7.0"));
        MatcherAssert.assertThat(results, hasEntry("OperatingSystemNameVersionMajor", "Android 7"));
        MatcherAssert.assertThat(results, hasEntry("OperatingSystemVersion", "7.0"));
        MatcherAssert.assertThat(results, hasEntry("OperatingSystemVersionBuild", "NBD90Z"));
        MatcherAssert.assertThat(results, hasEntry("OperatingSystemVersionMajor", "7"));
        MatcherAssert.assertThat(results, hasEntry("LayoutEngineClass", "Browser"));
        MatcherAssert.assertThat(results, hasEntry("LayoutEngineName", "Blink"));
        MatcherAssert.assertThat(results, hasEntry("LayoutEngineNameVersion", "Blink 53.0"));
        MatcherAssert.assertThat(results, hasEntry("LayoutEngineNameVersionMajor", "Blink 53"));
        MatcherAssert.assertThat(results, hasEntry("LayoutEngineVersion", "53.0"));
        MatcherAssert.assertThat(results, hasEntry("LayoutEngineVersionMajor", "53"));
        MatcherAssert.assertThat(results, hasEntry("AgentClass", "Browser"));
        MatcherAssert.assertThat(results, hasEntry("AgentName", "Chrome"));
        MatcherAssert.assertThat(results, hasEntry("AgentNameVersion", "Chrome 53.0.2785.124"));
        MatcherAssert.assertThat(results, hasEntry("AgentNameVersionMajor", "Chrome 53"));
        MatcherAssert.assertThat(results, hasEntry("AgentVersion", "53.0.2785.124"));
        MatcherAssert.assertThat(results, hasEntry("AgentVersionMajor", "53"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExtraRules() throws Exception {
        Map<String, Object> document = new HashMap<>();
        document.put(SOURCE_FIELD,
            "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/53.0.2785.124 Mobile Safari/537.36");
        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(random(), document);

        List<String> fieldNames = Arrays.asList("DeviceClass", "DeviceBrand", "DeviceName", "AgentNameVersionMajor", "FirstProductName");
        Integer      cacheSize  = 10;
        Integer      preheat    = 10;
        String       extraRules = "config:\n- matcher:\n    extract:\n      - 'FirstProductName     : 1 :agent.(1)product.(1)name'\n";

        YauaaProcessor      processor = new YauaaProcessor(randomAlphaOfLength(10), SOURCE_FIELD, TARGET_FIELD, fieldNames, cacheSize, preheat, extraRules);
        Map<String, Object> data      = processor.execute(ingestDocument).getSourceAndMetadata();

        MatcherAssert.assertThat(data, hasKey(TARGET_FIELD));

        Map<String, String> results = (Map<String, String>) data.get(TARGET_FIELD);

        // The EXPLICITLY requested fields
        MatcherAssert.assertThat(results, hasEntry("FirstProductName", "Mozilla"));
        MatcherAssert.assertThat(results, hasEntry("DeviceClass", "Phone"));
        MatcherAssert.assertThat(results, hasEntry("DeviceBrand", "Google"));
        MatcherAssert.assertThat(results, hasEntry("DeviceName", "Google Nexus 6"));
        MatcherAssert.assertThat(results, hasEntry("AgentNameVersionMajor", "Chrome 53"));

        // The IMPLICITLY requested fields (i.e. partials of the actually requested ones)
        MatcherAssert.assertThat(results, hasEntry("AgentName", "Chrome"));
        MatcherAssert.assertThat(results, hasEntry("AgentVersion", "53.0.2785.124"));
        MatcherAssert.assertThat(results, hasEntry("AgentVersionMajor", "53"));

        // The NOT requested fields
        MatcherAssert.assertThat(results, not(hasKey("OperatingSystemClass")));
        MatcherAssert.assertThat(results, not(hasKey("OperatingSystemName")));
        MatcherAssert.assertThat(results, not(hasKey("OperatingSystemNameVersion")));
        MatcherAssert.assertThat(results, not(hasKey("OperatingSystemNameVersionMajor")));
        MatcherAssert.assertThat(results, not(hasKey("OperatingSystemVersion")));
        MatcherAssert.assertThat(results, not(hasKey("OperatingSystemVersionBuild")));
        MatcherAssert.assertThat(results, not(hasKey("OperatingSystemVersionMajor")));
        MatcherAssert.assertThat(results, not(hasKey("LayoutEngineClass")));
        MatcherAssert.assertThat(results, not(hasKey("LayoutEngineName")));
        MatcherAssert.assertThat(results, not(hasKey("LayoutEngineNameVersion")));
        MatcherAssert.assertThat(results, not(hasKey("LayoutEngineNameVersionMajor")));
        MatcherAssert.assertThat(results, not(hasKey("LayoutEngineVersion")));
        MatcherAssert.assertThat(results, not(hasKey("LayoutEngineVersionMajor")));
        MatcherAssert.assertThat(results, not(hasKey("AgentClass")));
        MatcherAssert.assertThat(results, not(hasKey("AgentNameVersion")));
    }


}
