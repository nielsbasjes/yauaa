/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2025 Niels Basjes
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

import com.esotericsoftware.kryo.DefaultSerializer;
import com.esotericsoftware.kryo.Kryo;

@DefaultSerializer(UserAgentAnalyzerDirect.KryoSerializer.class)
public final class UserAgentAnalyzerDirect extends AbstractUserAgentAnalyzerDirect {

    /**
     * Do not try to construct directly; Use the Builder you get from newBuilder()
     */
    private UserAgentAnalyzerDirect() {
    }

    public static UserAgentAnalyzerDirectBuilder newBuilder() {
        return new UserAgentAnalyzerDirectBuilder(new UserAgentAnalyzerDirect());
    }

    public static final class UserAgentAnalyzerDirectBuilder extends AbstractUserAgentAnalyzerDirectBuilder<UserAgentAnalyzerDirect, UserAgentAnalyzerDirectBuilder> {
        private UserAgentAnalyzerDirectBuilder(UserAgentAnalyzerDirect newUaa) {
            super(newUaa);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T extends AbstractUserAgentAnalyzerDirect> T createNewInstance() {
        return (T) new UserAgentAnalyzerDirect();
    }

    /**
     * This is used to configure the provided Kryo instance if Kryo serialization is desired.
     * The expected type here is Object because otherwise the Kryo library becomes
     * a mandatory dependency on any project that uses Yauaa.
     * @param kryoInstance The instance of com.esotericsoftware.kryo.Kryo that needs to be configured.
     */
    public static void configureKryo(Object kryoInstance) {
        Kryo kryo = (Kryo) kryoInstance;
        kryo.register(UserAgentAnalyzerDirect.class);
        AbstractUserAgentAnalyzer.configureKryo(kryo);
    }

}
