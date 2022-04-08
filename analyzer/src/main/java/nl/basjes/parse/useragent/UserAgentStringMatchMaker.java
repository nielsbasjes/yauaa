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

package nl.basjes.parse.useragent;

import com.esotericsoftware.kryo.DefaultSerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import nl.basjes.parse.useragent.UserAgent.ImmutableUserAgent;
import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;
import nl.basjes.parse.useragent.clienthints.ClientHints;
import nl.basjes.parse.useragent.clienthints.ClientHintsAnalyzer;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static nl.basjes.parse.useragent.UserAgent.USERAGENT_HEADER;


@DefaultSerializer(UserAgentStringMatchMaker.KryoSerializer.class)
public abstract class UserAgentStringMatchMaker extends AbstractUserAgentAnalyzer implements Serializable {

    private ClientHintsAnalyzer clientHintsAnalyzer;

    protected UserAgentStringMatchMaker() {
        super();
        clientHintsAnalyzer = new ClientHintsAnalyzer();
    }

    /**
     * This is used to configure the provided Kryo instance if Kryo serialization is desired.
     * The expected type here is Object because otherwise the Kryo library becomes
     * a mandatory dependency on any project that uses Yauaa.
     *
     * @param kryoInstance The instance of com.esotericsoftware.kryo.Kryo that needs to be configured.
     */
    public static void configureKryo(Object kryoInstance) {
        Kryo kryo = (Kryo) kryoInstance;
        kryo.register(UserAgentStringMatchMaker.class);
        ClientHintsAnalyzer.configureKryo(kryoInstance);
        AbstractUserAgentAnalyzerDirect.configureKryo(kryo);
    }

    public static class KryoSerializer extends AbstractUserAgentAnalyzerDirect.KryoSerializer {
        public KryoSerializer(Kryo kryo, Class<?> type) {
            super(kryo, type);
        }

        @Override
        public void write(Kryo kryo, Output output, AbstractUserAgentAnalyzerDirect object) {
            super.write(kryo, output, object);
        }

        @Override
        public UserAgentStringMatchMaker read(Kryo kryo, Input input, Class<? extends AbstractUserAgentAnalyzerDirect> type) {
            return (UserAgentStringMatchMaker) super.read(kryo, input, type);
        }
    }

    @Override
    public void disableCaching() {
        super.disableCaching();
        setClientHintsCacheSize(0);
    }

    /**
     * Sets the new size of the parsing cache for the Client Hints.
     * Note that this will also wipe the existing cache.
     *
     * @param newClientHintsCacheSize The size of the new LRU cache. As size of 0 will disable caching.
     */
    public void setClientHintsCacheSize(int newClientHintsCacheSize) {
        clientHintsAnalyzer.setCacheSize(newClientHintsCacheSize);
        if (wasBuilt) {
            clientHintsAnalyzer.initializeCache();
        }
    }

    public int getClientHintCacheSize() {
        return clientHintsAnalyzer.getCacheSize();
    }

    @Override
    public void clearCache() {
        super.clearCache();
        clientHintsAnalyzer.clearCache();
    }

    public void setClientHintCacheInstantiator(ClientHintsCacheInstantiator<?> newClientHintsCacheInstantiator) {
        clientHintsAnalyzer.setCacheInstantiator(newClientHintsCacheInstantiator);
        if (wasBuilt) {
            clientHintsAnalyzer.initializeCache();
        }
    }

    @Override
    synchronized void initializeCache() {
        super.initializeCache();
        clientHintsAnalyzer.initializeCache();
    }

    @SuppressWarnings("unchecked") // For all the casts of 'this' to 'B'
    public abstract static class AbstractUserAgentClientHintsAnalyzerBuilder<UAA extends UserAgentStringMatchMaker, B extends AbstractUserAgentClientHintsAnalyzerBuilder<UAA, B>>
        extends AbstractUserAgentAnalyzerBuilder<UAA, B> {

        private final UAA uaa;

        protected AbstractUserAgentClientHintsAnalyzerBuilder(UAA newUaa) {
            super(newUaa);
            this.uaa = newUaa;
        }

        /**
         * Specify a new ClientHint cache size (0 = disable caching).
         * @param newCacheSize The new cache size value
         * @return the current Builder instance.
         */
        public B withClientHintsCache(int newCacheSize) {
            failIfAlreadyBuilt();
            uaa.setClientHintsCacheSize(newCacheSize);
            return (B)this;
        }

        /**
         * Disable ClientHint caching.
         * @return the current Builder instance.
         */
        public B withoutClientHintsCache() {
            failIfAlreadyBuilt();
            uaa.setClientHintsCacheSize(0);
            return (B)this;
        }

        /**
         * Specify a custom class to create the ClientHint  cache.
         * Use this if the default Caffeine cache is unsuitable for your needs.
         * @param cacheInstantiator The class that will create a new cache instance when requested.
         * @return the current Builder instance.
         */
        public B withClientHintCacheInstantiator(ClientHintsCacheInstantiator<?> cacheInstantiator) {
            failIfAlreadyBuilt();
            uaa.setClientHintCacheInstantiator(cacheInstantiator);
            return (B)this;
        }

        @SuppressWarnings("EmptyMethod") // We must override the method because of the generic return value.
        @Override
        public UAA build() {
            return super.build();
        }
    }

}
