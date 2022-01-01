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

package nl.basjes.parse.useragent.serialization;

import nl.basjes.parse.useragent.AbstractUserAgentAnalyzer.CacheInstantiator;
import nl.basjes.parse.useragent.UserAgent.ImmutableUserAgent;
import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester;
import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester.UserAgentAnalyzerTesterBuilder;
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

    abstract byte[] serialize(UserAgentAnalyzerTester uaa) throws IOException;

    abstract UserAgentAnalyzerTester deserialize(byte[] bytes) throws IOException, ClassNotFoundException;

    @Test
    void serializeAndDeserializeFullNOTestsBeforeRealTests() throws IOException, ClassNotFoundException {
        serializeAndDeserializeUAA(true, false, false);
    }

    @Test
    void serializeAndDeserializeFullTestsBeforeRealTests() throws IOException, ClassNotFoundException {
        serializeAndDeserializeUAA(true, true, false);
    }

    @Test
    void serializeAndDeserializeFastRealTests() throws IOException, ClassNotFoundException {
        serializeAndDeserializeUAA(false, false, false);
    }

    @Test
    void serializeAndDeserializeFullNOTestsBeforeTestRules() throws IOException, ClassNotFoundException {
        serializeAndDeserializeUAA(true, false, true);
    }

    @Test
    void serializeAndDeserializeFullTestsBeforeTestRules() throws IOException, ClassNotFoundException {
        serializeAndDeserializeUAA(true, true, true);
    }

    @Test
    void serializeAndDeserializeFastTestRules() throws IOException, ClassNotFoundException {
        serializeAndDeserializeUAA(false, false, true);
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

    private void serializeAndDeserializeUAA(boolean immediate, boolean runTestsBefore, boolean useTestRules) throws IOException, ClassNotFoundException {
        LOG.info("==============================================================");
        LOG.info("Create");
        LOG.info("--------------------------------------------------------------");

        UserAgentAnalyzerTesterBuilder uaab = UserAgentAnalyzerTester
            .newBuilder()
            .keepTests()
            .withCacheInstantiator(new TestingCacheInstantiator())
            .withCache(1234)
            .hideMatcherLoadStats();

        if (useTestRules) {
            uaab.dropDefaultResources()
                .addResources("classpath*:AllSteps.yaml")
                .addResources("classpath*:AllFields-tests.yaml")
                .addResources("classpath*:AllPossibleSteps.yaml")
                .addResources("classpath*:IsNullLookup.yaml");
        }

        if (immediate) {
            uaab.immediateInitialization();
        }

        UserAgentAnalyzerTester uaaBefore = uaab.build();

        String uaaBeforeString = uaaBefore.toString();

        if (runTestsBefore) {
            LOG.info("--------------------------------------------------------------");
            assertTrue(uaaBefore.runTests(false, false, null, false, false), "Tests BEFORE serialization failed");

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
        UserAgentAnalyzerTester uaaAfter = deserialize(bytes);
        long deserializeStopNs = System.nanoTime();

        LOG.info("Done");
        LOG.info("Deserialize took {} ns ({} ms)", deserializeStopNs - deserializeStartNs, (deserializeStopNs - deserializeStartNs) / 1_000_000);

        String uaaAfterString = uaaAfter.toString();

        assertEquals(uaaBeforeString, uaaAfterString);

        assertEquals(1234, uaaAfter.getCacheSize());

        LOG.info("==============================================================");
        LOG.info("Validating when getting all fields");
        LOG.info("--------------------------------------------------------------");
        assertTrue(uaaAfter.runTests(false, false, null, false, false), "Tests AFTER serialization failed");
        LOG.info("==============================================================");
    }

}
