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

package nl.example.tests.serialization;

import nl.basjes.parse.useragent.AbstractUserAgentAnalyzer.CacheInstantiator;
import nl.basjes.parse.useragent.UserAgent.ImmutableUserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.UserAgentAnalyzer.UserAgentAnalyzerBuilder;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class AbstractSerializationTest {

    private static final Logger LOG = LogManager.getLogger(AbstractSerializationTest.class);

    abstract byte[] serialize(UserAgentAnalyzer uaa) throws IOException;

    abstract UserAgentAnalyzer deserialize(byte[] bytes) throws IOException, ClassNotFoundException;

    @Test
    void serializeAndDeserializeFullNOTestsBefore() throws IOException, ClassNotFoundException {
        serializeAndDeserializeUAA(true, false);
    }

    @Test
    void serializeAndDeserializeFullTestsBefore() throws IOException, ClassNotFoundException {
        serializeAndDeserializeUAA(true, true);
    }

    @Test
    void serializeAndDeserializeFast() throws IOException, ClassNotFoundException {
        serializeAndDeserializeUAA(false, false);
    }

    public static class TestingCacheInstantiator implements CacheInstantiator {
        @Override
        public Map<String, ImmutableUserAgent> instantiateCache(int cacheSize) {
            return new LRUMap<String, ImmutableUserAgent>(cacheSize) {
                @Override
                public synchronized ImmutableUserAgent get(Object key) {
                    return super.get(key);
                }

                @Override
                public synchronized ImmutableUserAgent put(String key, ImmutableUserAgent value) {
                    return super.put(key, value);
                }

                @Override
                public synchronized void clear() {
                    super.clear();
                }
            };
        }
    }

    private void serializeAndDeserializeUAA(boolean immediate, boolean runTestsBefore) throws IOException, ClassNotFoundException {
        LOG.info("==============================================================");
        LOG.info("Create");
        LOG.info("--------------------------------------------------------------");

        UserAgentAnalyzerBuilder uaab = UserAgentAnalyzer
            .newBuilder()
            .keepTests()
            .withCacheInstantiator(new TestingCacheInstantiator())
            .withCache(1234)
            .hideMatcherLoadStats();

        if (immediate) {
            uaab.immediateInitialization();
        }

        UserAgentAnalyzer uaaBefore = uaab.build();

        String uaaBeforeString = uaaBefore.toString();

        if (runTestsBefore) {
            LOG.info("--------------------------------------------------------------");
            uaaBefore.getTestCases().forEach(testCase -> assertTrue(testCase.verify(uaaBefore)));

            // Get rid of the data of the last tested useragent
            uaaBefore.reset();

            String uaaBeforeAfterTestsString = uaaBefore.toString();
            assertEquals(uaaBeforeString, uaaBeforeAfterTestsString);
        }

        LOG.info("--------------------------------------------------------------");
        LOG.info("Serialize");

        long   serializeStartNs = System.nanoTime();
        byte[] bytes            = serialize(uaaBefore);
        long   serializeStopNs  = System.nanoTime();

        LOG.info("Serialize took {} ns ({} ms)", serializeStopNs - serializeStartNs, (serializeStopNs - serializeStartNs) / 1_000_000);
        LOG.info("The UserAgentAnalyzer was serialized into {} bytes", bytes.length);
        LOG.info("--------------------------------------------------------------");
        LOG.info("Deserialize");

        long deserializeStartNs = System.nanoTime();
        UserAgentAnalyzer uaaAfter = deserialize(bytes);
        long deserializeStopNs = System.nanoTime();

        LOG.info("Done");
        LOG.info("Deserialize took {} ns ({} ms)", deserializeStopNs - deserializeStartNs, (deserializeStopNs - deserializeStartNs) / 1_000_000);

        String uaaAfterString = uaaAfter.toString();

        assertEquals(uaaBeforeString, uaaAfterString);

        assertEquals(1234, uaaAfter.getCacheSize());

        LOG.info("==============================================================");
        LOG.info("Validating when getting all fields");
        LOG.info("--------------------------------------------------------------");
        uaaBefore.getTestCases().forEach(testCase -> assertTrue(testCase.verify(uaaBefore)));
        LOG.info("==============================================================");
    }

}
