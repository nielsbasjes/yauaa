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

package nl.basjes.parse.useragent;

import nl.basjes.parse.useragent.AbstractUserAgentAnalyzer.CacheInstantiator;
import nl.basjes.parse.useragent.UserAgent.ImmutableUserAgent;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TestCaching {

    private static final Logger LOG = LogManager.getLogger(TestCaching.class);

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

        uaa.clearCache();
    }

    @Test
    void testSettingNoCaching() {
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
        assertEquals(agent1, agent2);
    }

    @Test
    void testCustomCacheImplementationInline() {
        String userAgent = "Mozilla/5.0 (Linux; Android 8.1.0; Pixel Build/OPM4.171019.021.D1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.98 Mobile Safari/537.36";
        UserAgentAnalyzer uaa = UserAgentAnalyzer
            .newBuilder()
            .withCacheInstantiator(
                new CacheInstantiator() {
                    @Override
                    public Map<String, ImmutableUserAgent> instantiateCache(int cacheSize) {
                        // The Map MUST be synchronized
                        return Collections.synchronizedMap(
                            // NOTE: A simple HashMap is BAD as it is not cleaned and will grow infinitely towards an OOM.
                            // This is ONLY for this test.
                            new LRUMap<String, ImmutableUserAgent>(cacheSize) {
                                @Override
                                public ImmutableUserAgent get(Object key) {
                                    LOG.info("Did a GET on {}", key);
                                    return super.get(key);
                                }

                                @Override
                                public ImmutableUserAgent put(String key, ImmutableUserAgent value) {
                                    LOG.info("Did a PUT on {}", key);
                                    return super.put(key, value);
                                }
                            }
                        );
                    }
                }
            )
            .withCache(10)
            .hideMatcherLoadStats()
            .build();

        // First time
        UserAgent agent1 = uaa.parse(userAgent);

        // Should come from cache
        UserAgent agent2 = uaa.parse(userAgent);

        // Both should be the same
        assertEquals(agent1, agent2);
    }

    private static class TestingCacheInstantiator implements CacheInstantiator {
        @Override
        public Map<String, ImmutableUserAgent> instantiateCache(int cacheSize) {
            return Collections.synchronizedMap(
                new LRUMap<String, ImmutableUserAgent>(cacheSize) {
                    @Override
                    public ImmutableUserAgent get(Object key) {
                        LOG.info("Did a GET on {}", key);
                        return super.get(key);
                    }

                    @Override
                    public ImmutableUserAgent put(String key, ImmutableUserAgent value) {
                        LOG.info("Did a PUT on {}", key);
                        return super.put(key, value);
                    }
                }
            );
        }
    }

    @Test
    void testCustomCacheImplementationClass() {
        String userAgent = "Mozilla/5.0 (Linux; Android 8.1.0; Pixel Build/OPM4.171019.021.D1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.98 Mobile Safari/537.36";
        UserAgentAnalyzer uaa = UserAgentAnalyzer
            .newBuilder()
            .withCacheInstantiator(new TestingCacheInstantiator())
            .withCache(10)
            .hideMatcherLoadStats()
            .build();

        // First time
        UserAgent agent1 = uaa.parse(userAgent);

        // Should come from cache
        UserAgent agent2 = uaa.parse(userAgent);

        // Both should be the same
        assertEquals(agent1, agent2);
    }

}
