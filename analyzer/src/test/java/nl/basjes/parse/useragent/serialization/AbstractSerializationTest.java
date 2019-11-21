/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2019 Niels Basjes
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

import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester;
import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester.UserAgentAnalyzerTesterBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public abstract class AbstractSerializationTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSerializationTest.class);

    abstract byte[] serialize(UserAgentAnalyzerTester uaa) throws IOException;

    abstract UserAgentAnalyzerTester deserialize(byte[] bytes) throws IOException, ClassNotFoundException;

    @Test
    public void serializeAndDeserializeFull() throws IOException, ClassNotFoundException {
        serializeAndDeserializeUAA(true);
    }

    @Test
    public void serializeAndDeserializeFast() throws IOException, ClassNotFoundException {
        serializeAndDeserializeUAA(false);
    }

    public void serializeAndDeserializeUAA(boolean immediate) throws IOException, ClassNotFoundException {
        LOG.info("==============================================================");
        LOG.info("Create");
        LOG.info("--------------------------------------------------------------");

        UserAgentAnalyzerTesterBuilder<?, ?> uaab = UserAgentAnalyzerTester
            .newBuilder()
            .keepTests()
            .dropDefaultResources()
            .addResources("classpath*:AllSteps.yaml")
            .addResources("classpath*:AllFields-tests.yaml")
            .addResources("classpath*:AllPossibleSteps.yaml")
            .hideMatcherLoadStats();

        if (immediate) {
            uaab.immediateInitialization();
        }

        UserAgentAnalyzerTester uaa = uaab.build();

        LOG.info("--------------------------------------------------------------");
        LOG.info("Serialize");

        long   serializeStartNs = System.nanoTime();
        byte[] bytes            = serialize(uaa);
        long   serializeStopNs  = System.nanoTime();

        LOG.info("Serialize took {} ns ({} ms)", serializeStopNs - serializeStartNs, (serializeStopNs - serializeStartNs) / 1_000_000);
        LOG.info("The UserAgentAnalyzer was serialized into {} bytes", bytes.length);
        LOG.info("--------------------------------------------------------------");
        LOG.info("Deserialize");

        long deserializeStartNs = System.nanoTime();
        uaa = deserialize(bytes);
        long deserializeStopNs = System.nanoTime();

        LOG.info("Done");
        LOG.info("Deserialize took {} ns ({} ms)", deserializeStopNs - deserializeStartNs, (deserializeStopNs - deserializeStartNs) / 1_000_000);
        LOG.info("==============================================================");
        LOG.info("Validating when getting all fields");
        LOG.info("--------------------------------------------------------------");
        assertTrue(uaa.runTests(false, false, null, false, false));
        LOG.info("==============================================================");
    }

}
