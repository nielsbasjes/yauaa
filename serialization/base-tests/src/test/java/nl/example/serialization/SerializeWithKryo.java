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

package nl.example.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import nl.example.Demo;

import java.nio.ByteBuffer;

public class SerializeWithKryo extends AbstractSerializationTest {

    // If the type Kryo is part o the method signature it has effects
    // on the serialization we are trying to test here.
    // So this returns Object !
    Object createKryo() {
        return new Kryo();
    }

    byte[] serialize(Demo demo) {
        Kryo kryo = (Kryo) createKryo();

        ByteBufferOutput byteBufferOutput = new ByteBufferOutput(1_000_000, -1);
        kryo.writeClassAndObject(byteBufferOutput, demo);

        ByteBuffer buf = byteBufferOutput.getByteBuffer();
        byte[] arr = new byte[buf.position()];
        buf.rewind();
        buf.get(arr);

        return arr;
    }

    Demo deserialize(byte[] bytes) {
        Kryo kryo = (Kryo) createKryo();
        ByteBufferInput byteBufferInput = new ByteBufferInput(bytes);
        return (Demo) kryo.readClassAndObject(byteBufferInput);
    }

}
