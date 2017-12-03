/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2017 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.useragent;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestCaching {

    @Test
    public void testSettingCaching() throws IllegalAccessException {
        UserAgentAnalyzer uaa = UserAgentAnalyzer
            .newBuilder()
            .withCache(42)
            .hideMatcherLoadStats()
            .withField("AgentUuid")
            .build();

        assertEquals(42, uaa.getCacheSize());
        assertEquals(42, getAllocatedCacheSize(uaa));

        uaa.disableCaching();
        assertEquals(0, uaa.getCacheSize());
        assertEquals(0, getAllocatedCacheSize(uaa));

        uaa.setCacheSize(42);
        assertEquals(42, uaa.getCacheSize());
        assertEquals(42, getAllocatedCacheSize(uaa));
    }

    @Test
    public void testSettingNoCaching() throws IllegalAccessException {
        UserAgentAnalyzer uaa = UserAgentAnalyzer
            .newBuilder()
            .withoutCache()
            .hideMatcherLoadStats()
            .withField("AgentUuid")
            .build();

        assertEquals(0, uaa.getCacheSize());
        assertEquals(0, getAllocatedCacheSize(uaa));

        uaa.setCacheSize(42);
        assertEquals(42, uaa.getCacheSize());
        assertEquals(42, getAllocatedCacheSize(uaa));

        uaa.disableCaching();
        assertEquals(0, uaa.getCacheSize());
        assertEquals(0, getAllocatedCacheSize(uaa));
    }


    @Test
    public void testCache() throws IllegalAccessException {
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
        assertEquals(1, getAllocatedCacheSize(uaa));

        agent = uaa.parse(uuid);
        assertEquals(uuid, agent.get(fieldName).getValue());
        assertEquals(agent, getCache(uaa).get(uuid));

        agent = uaa.parse(uuid);
        assertEquals(uuid, agent.get(fieldName).getValue());
        assertEquals(agent, getCache(uaa).get(uuid));

        uaa.disableCaching();
        assertEquals(0, uaa.getCacheSize());
        assertEquals(0, getAllocatedCacheSize(uaa));

        agent = uaa.parse(uuid);
        assertEquals(uuid, agent.get(fieldName).getValue());
        assertEquals(null, getCache(uaa)    );
    }

    private LRUMap<?, ?> getCache(UserAgentAnalyzer uaa) throws IllegalAccessException {
        LRUMap<?, ?> actualCache = null;
        Object rawParseCache = FieldUtils.readField(uaa, "parseCache", true);
        if (rawParseCache instanceof LRUMap<?, ?>) {
            actualCache = (LRUMap) rawParseCache;
        }
        return actualCache;
    }

    private int getAllocatedCacheSize(UserAgentAnalyzer uaa ) throws IllegalAccessException {
        int actualCaceSize = -1;
        LRUMap<?, ?> cache = getCache(uaa);
        if (cache == null) {
            return 0;
        }
        return cache.maxSize();
    }



}
