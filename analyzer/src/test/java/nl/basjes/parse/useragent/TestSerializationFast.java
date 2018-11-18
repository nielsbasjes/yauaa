/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2018 Niels Basjes
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

import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class TestSerializationFast {

    private static final Logger LOG = LoggerFactory.getLogger(TestSerializationFast.class);

    protected static UserAgentAnalyzerTester uaa;

    private static List<String> allFields;

    @BeforeClass
    public static void serializeAndDeserializeUAA() throws IOException, ClassNotFoundException {
        LOG.info("==============================================================");
        LOG.info("Create");
        LOG.info("--------------------------------------------------------------");

        // The number of rules for the Brands is so large that it results in an OOM in Travis-CI
        allFields = UserAgentAnalyzer.newBuilder().build().getAllPossibleFieldNamesSorted();
        allFields.removeAll(Arrays.asList("DeviceName", "DevideClass"));

        uaa = UserAgentAnalyzerTester.newBuilder()
            .withFields(allFields)
            .hideMatcherLoadStats()
            .delayInitialization()
            .build();

        LOG.info("--------------------------------------------------------------");
        LOG.info("Serialize");
        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(uaa);
            out.flush();
            bytes = bos.toByteArray();
        }

        uaa = null;

        LOG.info("The UserAgentAnalyzer was serialized into {} bytes", bytes.length);
        LOG.info("--------------------------------------------------------------");
        LOG.info("Deserialize");
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

        try (ObjectInput in = new ObjectInputStream(bis)) {
            Object o = in.readObject();
            assertTrue(o instanceof UserAgentAnalyzerTester);
            uaa = (UserAgentAnalyzerTester) o;
        }

        LOG.info("Done");
        LOG.info("==============================================================");
    }

    @Test
    public void validateAllPredefinedBrowsers() {
        LOG.info("==============================================================");
        LOG.info("Validating when getting all fields");
        LOG.info("--------------------------------------------------------------");
        assertTrue(uaa.runTests(false, true, allFields, false, false));
    }

}
