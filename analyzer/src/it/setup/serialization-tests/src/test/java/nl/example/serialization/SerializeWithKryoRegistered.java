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

import com.esotericsoftware.kryo.Kryo;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.example.Demo;

public class SerializeWithKryoRegistered extends SerializeWithKryo {
    // If the type Kryo is part of the method signature it has effects
    // on the serialization we are trying to test here.
    // So this returns Object !
    @Override
    Object createKryo() {
        Kryo kryo = (Kryo) super.createKryo();
        UserAgentAnalyzer.configureKryo(kryo);
        kryo.register(Demo.class);
        kryo.setRegistrationRequired(true);
        kryo.setWarnUnregisteredClasses(true);
        return kryo;
    }
}
