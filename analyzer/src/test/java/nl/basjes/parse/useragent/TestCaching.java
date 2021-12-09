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

package nl.basjes.parse.useragent;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TestCaching {

    @Test
    void testSettingCaching() {
        UserAgentAnalyzer uaa = UserAgentAnalyzer
            .newBuilder()
            .withCache(42)
            .hideMatcherLoadStats()
            .withField("AgentUuid")
            .build();

        assertEquals(42, uaa.getCacheSize());

        uaa.disableCaching();
        assertEquals(0, uaa.getCacheSize());

        uaa.setCacheSize(42);
        assertEquals(42, uaa.getCacheSize());
    }

    @Test
    void testSettingNoCaching() throws IllegalAccessException {
        UserAgentAnalyzer uaa = UserAgentAnalyzer
            .newBuilder()
            .withoutCache()
            .hideMatcherLoadStats()
            .withField("AgentUuid")
            .build();

        assertEquals(0, uaa.getCacheSize());

        uaa.setCacheSize(42);
        assertEquals(42, uaa.getCacheSize());

        uaa.disableCaching();
        assertEquals(0, uaa.getCacheSize());
    }


    @Test
    void testCache() throws IllegalAccessException {
        String uuid = "11111111-2222-3333-4444-555555555555";
        String fieldName = "AgentUuid";

        UserAgentAnalyzer uaa = UserAgentAnalyzer
            .newBuilder()
            .withCache(1)
            .hideMatcherLoadStats()
            .withField(fieldName)
            .build();

        UserAgent agent;

        assertEquals(1, uaa.getCacheSize());

        agent = uaa.parse(uuid);
        assertEquals(uuid, agent.get(fieldName).getValue());
        assertEquals(agent, getCache(uaa).get(uuid));

        agent = uaa.parse(uuid);
        assertEquals(uuid, agent.get(fieldName).getValue());
        assertEquals(agent, getCache(uaa).get(uuid));

        uaa.disableCaching();
        assertEquals(0, uaa.getCacheSize());

        agent = uaa.parse(uuid);
        assertEquals(uuid, agent.get(fieldName).getValue());
        assertNull(getCache(uaa));
    }

    private Map<?, ?> getCache(UserAgentAnalyzer uaa) throws IllegalAccessException {
        Map<?, ?> actualCache = null;
        Object rawParseCache = FieldUtils.readField(uaa, "parseCache", true);
        if (rawParseCache instanceof Map<?, ?>) {
            actualCache = (Map<?, ?>) rawParseCache;
        }
        return actualCache;
    }

    @Test
    void testResultFromCacheMustBeIdentical() {
        String userAgent = "Mozilla/5.0 (compatible; coccocbot-image/1.0; +http://help.coccoc.com/searchengine)";
        UserAgentAnalyzer uaa = UserAgentAnalyzer
            .newBuilder()
            .withCache(10)
            .hideMatcherLoadStats()
            .build();

        // First time
        UserAgent agent1 = uaa.parse(userAgent);

        // Should come from cache
        UserAgent agent2 = uaa.parse(userAgent);

        // Both should be the same
        assertEquals(agent1.toYamlTestCase(), agent2.toYamlTestCase());
    }


}
