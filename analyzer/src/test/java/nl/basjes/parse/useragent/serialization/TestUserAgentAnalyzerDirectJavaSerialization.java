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

import nl.basjes.parse.useragent.UserAgentAnalyzerDirect;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestUserAgentAnalyzerDirectJavaSerialization extends AbstractAnalyzerSerializationTest<UserAgentAnalyzerDirect> {

    byte[] serialize(UserAgentAnalyzerDirect uaa) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(uaa);
            out.flush();
            return bos.toByteArray();
        }
    }

    UserAgentAnalyzerDirect deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

        try (ObjectInput in = new ObjectInputStream(bis)) {
            Object o = in.readObject();
            assertTrue(o instanceof UserAgentAnalyzerDirect);
            return (UserAgentAnalyzerDirect) o;
        }
    }

    @Override
    UserAgentAnalyzerDirect create() {
        UserAgentAnalyzerDirect.UserAgentAnalyzerDirectBuilder builder = UserAgentAnalyzerDirect.newBuilder();
        configureTestInstance(builder);
        return builder.build();
    }

}
