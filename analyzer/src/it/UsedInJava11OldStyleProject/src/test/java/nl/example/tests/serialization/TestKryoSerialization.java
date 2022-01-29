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

package nl.example.tests.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

import java.nio.ByteBuffer;

class TestKryoSerialization extends AbstractSerializationTest {

    private Kryo getKryo() {
        Kryo             kryo             = new Kryo();
        UserAgentAnalyzer.configureKryo(kryo);

        // Now we KNOW in our code we have a custom CacheInstantiator that needs to be registered too for Kryo.
        kryo.register(TestingCacheInstantiator.class);

        // For debugging
        kryo.setRegistrationRequired(true);
        kryo.setWarnUnregisteredClasses(true);

        return kryo;
    }

    byte[] serialize(UserAgentAnalyzer uaa) {
        Kryo kryo = getKryo();

        ByteBufferOutput byteBufferOutput = new ByteBufferOutput(1_000_000, -1);
        kryo.writeClassAndObject(byteBufferOutput, uaa);

        ByteBuffer buf = byteBufferOutput.getByteBuffer();
        byte[] arr = new byte[buf.position()];
        buf.rewind();
        buf.get(arr);

        return arr;
    }

    UserAgentAnalyzer deserialize(byte[] bytes) {
        Kryo kryo = getKryo();

        ByteBufferInput byteBufferInput = new ByteBufferInput(bytes);
        return (UserAgentAnalyzer) kryo.readClassAndObject(byteBufferInput);
    }

}
