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

package nl.basjes.parse.useragent.debug;

import com.esotericsoftware.kryo.DefaultSerializer;
import com.esotericsoftware.kryo.Kryo;

@DefaultSerializer(UserAgentAnalyzerTester.KryoSerializer.class)
public final class UserAgentAnalyzerTester extends AbstractUserAgentAnalyzerTester {

    public static UserAgentAnalyzerTesterBuilder newBuilder() {
        return new UserAgentAnalyzerTesterBuilder(new UserAgentAnalyzerTester()).keepTests();
    }

    public static final class UserAgentAnalyzerTesterBuilder extends AbstractUserAgentAnalyzerTesterBuilder<UserAgentAnalyzerTester, UserAgentAnalyzerTesterBuilder> {
        UserAgentAnalyzerTesterBuilder(UserAgentAnalyzerTester newUaa) {
            super(newUaa);
        }
    }

    /**
     * This is used to configure the provided Kryo instance if Kryo serialization is desired.
     * The expected type here is Object because otherwise the Kryo library becomes
     * a mandatory dependency on any project that uses Yauaa.
     * @param kryoInstance The instance of com.esotericsoftware.kryo.Kryo that needs to be configured.
     */
    public static void configureKryo(Object kryoInstance) {
        Kryo kryo = (Kryo) kryoInstance;
        kryo.register(UserAgentAnalyzerTester.class);
        AbstractUserAgentAnalyzerTester.configureKryo(kryo);
    }

}
