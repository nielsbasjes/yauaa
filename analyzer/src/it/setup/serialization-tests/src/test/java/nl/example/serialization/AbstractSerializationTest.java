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

package nl.example.serialization;

import nl.basjes.parse.useragent.UserAgent;
import nl.example.Demo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractSerializationTest {

    private static final Logger LOG = LogManager.getLogger(AbstractSerializationTest.class);

    abstract byte[] serialize(Demo demo) throws IOException;

    abstract Demo deserialize(byte[] bytes) throws IOException, ClassNotFoundException;

    private void testParser(Demo demo) {
        String userAgent = "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36";

        UserAgent result  = demo.parse(userAgent);

        LOG.info("Result = \n{}", result);

        assertTrue(result.toXML().contains("<DeviceName>Google Nexus 6</DeviceName>"),
            "The parser must extract the correct DeviceName");
    }

    public void cycleWithTestBefore() throws IOException, ClassNotFoundException {
        doSerDeCycle(true);
    }

    public void cycleWithoutTestBefore() throws IOException, ClassNotFoundException {
        doSerDeCycle(false);
    }

    private void doSerDeCycle(boolean runTestsBefore) throws IOException, ClassNotFoundException {
        LOG.info("==============================================================");
        LOG.info("Create");
        LOG.info("--------------------------------------------------------------");

        Demo demoBefore = new Demo();

        if (runTestsBefore) {
            LOG.info("--------------------------------------------------------------");
            testParser(demoBefore);
        }

        // Make sure it has been initialized
        demoBefore.parse(null);
        String before = demoBefore.toString();

        LOG.info("--------------------------------------------------------------");
        LOG.info("Serialize");

        long   serializeStartNs = System.nanoTime();
        byte[] bytes            = serialize(demoBefore);
        long   serializeStopNs  = System.nanoTime();

        LOG.info("Serialize took {} ns ({} ms)", serializeStopNs - serializeStartNs, (serializeStopNs - serializeStartNs) / 1_000_000);
        LOG.info("The UserAgentAnalyzer was serialized into {} bytes", bytes.length);
        LOG.info("--------------------------------------------------------------");
        LOG.info("Deserialize");

        long deserializeStartNs = System.nanoTime();
        Demo demoAfter = deserialize(bytes);
        long deserializeStopNs = System.nanoTime();

        LOG.info("Done");
        LOG.info("Deserialize took {} ns ({} ms)", deserializeStopNs - deserializeStartNs, (deserializeStopNs - deserializeStartNs) / 1_000_000);

        demoAfter.parse(null);
        String after = demoAfter.toString();
        // To avoid getting >60MiB of error log if it goes wrong.
        assertEquals(before.length(), after.length());
        assertEquals(before, after);

        LOG.info("==============================================================");
        LOG.info("Validating when getting all fields");
        LOG.info("--------------------------------------------------------------");
        testParser(demoAfter);
        LOG.info("==============================================================");

    }

}
