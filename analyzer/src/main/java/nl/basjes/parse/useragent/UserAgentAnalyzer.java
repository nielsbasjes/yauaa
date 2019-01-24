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

package nl.basjes.parse.useragent;

import com.esotericsoftware.kryo.DefaultSerializer;
import com.esotericsoftware.kryo.Kryo;
import org.apache.commons.collections4.map.LRUMap;

import java.io.Serializable;

@DefaultSerializer(UserAgentAnalyzer.KryoSerializer.class)
public class UserAgentAnalyzer extends UserAgentAnalyzerDirect implements Serializable {
    public static final int DEFAULT_PARSE_CACHE_SIZE = 10000;

    private int cacheSize = DEFAULT_PARSE_CACHE_SIZE;
    private LRUMap<String, UserAgent> parseCache = null;

    protected UserAgentAnalyzer() {
        super();
        initializeCache();
    }

    public static class KryoSerializer extends UserAgentAnalyzerDirect.KryoSerializer {
        public KryoSerializer(Kryo kryo, Class type) {
            super(kryo, type);
        }
    }

    public void disableCaching() {
        setCacheSize(0);
    }

    /**
     * Sets the new size of the parsing cache.
     * Note that this will also wipe the existing cache.
     *
     * @param newCacheSize The size of the new LRU cache. As size of 0 will disable caching.
     */
    public void setCacheSize(int newCacheSize) {
        cacheSize = newCacheSize > 0 ? newCacheSize : 0;
        initializeCache();
    }

    private void initializeCache() {
        if (cacheSize >= 1) {
            parseCache = new LRUMap<>(cacheSize);
        } else {
            parseCache = null;
        }
    }

    public int getCacheSize() {
        return cacheSize;
    }

    @Override
    public synchronized UserAgent parse(UserAgent userAgent) {
        if (userAgent == null) {
            return null;
        }
        userAgent.reset();

        if (parseCache == null) {
            return super.parse(userAgent);
        }

        String userAgentString = userAgent.getUserAgentString();
        UserAgent cachedValue = parseCache.get(userAgentString);
        if (cachedValue != null) {
            userAgent.clone(cachedValue);
        } else {
            cachedValue = new UserAgent(super.parse(userAgent));
            parseCache.put(userAgentString, cachedValue);
        }
        // We have our answer.
        return userAgent;
    }

    public static UserAgentAnalyzerBuilder<? extends UserAgentAnalyzer, ? extends UserAgentAnalyzerBuilder<?, ?>> newBuilder() {
        return new UserAgentAnalyzerBuilder<>(new UserAgentAnalyzer());
    }

    @SuppressWarnings("unchecked")
    public static class UserAgentAnalyzerBuilder<UAA extends UserAgentAnalyzer, B extends UserAgentAnalyzerBuilder<UAA, B>>
            extends UserAgentAnalyzerDirectBuilder<UAA, B> {
        private final UAA uaa;

        public UserAgentAnalyzerBuilder(UAA newUaa) {
            super(newUaa);
            this.uaa = newUaa;
        }

        /**
         * Specify a new cache size (0 = disable caching).
         * @param newCacheSize The new cache size value
         * @return the current Builder instance.
         */
        public B withCache(int newCacheSize) {
            failIfAlreadyBuilt();
            uaa.setCacheSize(newCacheSize);
            return (B)this;
        }

        /**
         * Disable caching.
         * @return the current Builder instance.
         */
        public B withoutCache() {
            failIfAlreadyBuilt();
            uaa.setCacheSize(0);
            return (B)this;
        }

        @SuppressWarnings("EmptyMethod") // We must override the method because of the generic return value.
        @Override
        public UAA build() {
            return super.build();
        }
    }
}
