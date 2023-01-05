/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2023 Niels Basjes
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
import com.github.benmanes.caffeine.cache.Caffeine;
import nl.basjes.parse.useragent.UserAgent.ImmutableUserAgent;
import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;
import org.apache.commons.collections4.map.LRUMap;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static nl.basjes.parse.useragent.UserAgent.USERAGENT_HEADER;

@DefaultSerializer(AbstractUserAgentAnalyzer.KryoSerializer.class)
public class AbstractUserAgentAnalyzer extends AbstractUserAgentAnalyzerDirect implements Serializable {
    public static final int DEFAULT_PARSE_CACHE_SIZE = 10000;

    protected int cacheSize = DEFAULT_PARSE_CACHE_SIZE;
    private transient Map<String, ImmutableUserAgent> parseCache;
    private CacheInstantiator cacheInstantiator = new DefaultCacheInstantiator();
    private transient ImmutableUserAgent nullAgent = null;
    protected boolean wasBuilt = false;

    protected AbstractUserAgentAnalyzer() {
        super();
    }

    @Override
    public synchronized void destroy() {
        super.destroy();
        if (parseCache != null) {
            parseCache.clear();
            parseCache = null;
        }
    }

    private void readObject(java.io.ObjectInputStream stream)
        throws java.io.IOException, ClassNotFoundException {
        stream.defaultReadObject();
        initializeCache();
    }

    /**
     * This is used to configure the provided Kryo instance if Kryo serialization is desired.
     * The expected type here is Object because otherwise the Kryo library becomes
     * a mandatory dependency on any project that uses Yauaa.
     * @param kryoInstance The instance of com.esotericsoftware.kryo.Kryo that needs to be configured.
     */
    public static void configureKryo(Object kryoInstance) {
        Kryo kryo = (Kryo) kryoInstance;
        kryo.register(AbstractUserAgentAnalyzer.class);
        kryo.register(DefaultCacheInstantiator.class);
        AbstractUserAgentAnalyzerDirect.configureKryo(kryo);
    }

    public static class KryoSerializer extends AbstractUserAgentAnalyzerDirect.KryoSerializer {
        public KryoSerializer(Kryo kryo, Class<?> type) {
            super(kryo, type);
        }

        @Override
        public void write(Kryo kryo, Output output, AbstractUserAgentAnalyzerDirect object) {
            super.write(kryo, output, object);
            output.writeInt(((AbstractUserAgentAnalyzer)object).cacheSize);
        }

        @Override
        public AbstractUserAgentAnalyzer read(Kryo kryo, Input input, Class<? extends AbstractUserAgentAnalyzerDirect> type) {
            final AbstractUserAgentAnalyzer uaa = (AbstractUserAgentAnalyzer) super.read(kryo, input, type);
            uaa.cacheSize = input.readInt();
            uaa.initializeCache();
            return uaa;
        }
    }

    public void disableCaching() {
        setCacheSize(0);
        setClientHintsCacheSize(0);
    }

    /**
     * Sets the new size of the parsing cache.
     * Note that this will also wipe the existing cache.
     *
     * @param newCacheSize The size of the new LRU cache. As size of 0 will disable caching.
     */
    public void setCacheSize(int newCacheSize) {
        cacheSize = Math.max(newCacheSize, 0);
        if (wasBuilt) {
            initializeCache();
        }
    }

    public void clearCache() {
        if (parseCache != null) {
            parseCache.clear();
        }
        clientHintsAnalyzer.clearCache();
    }

    public void setCacheInstantiator(CacheInstantiator newCacheInstantiator) {
        cacheInstantiator = newCacheInstantiator;
        if (wasBuilt) {
            initializeCache();
        }
    }

    synchronized void initializeCache() {
        if (cacheSize >= 1) {
            parseCache = cacheInstantiator.instantiateCache(cacheSize);
        } else {
            parseCache = null;
        }
        clientHintsAnalyzer.initializeCache();
    }

    public interface CacheInstantiator extends Serializable {
        /**
         * A single method that must create a new instance of the cache.
         * The returned instance MUST implement at least the {@link Map#get} and {@link Map#put}
         * methods in a threadsafe way if you intend to use this in a multithreaded scenario.
         * Yauaa only uses the put and get methods and in exceptional cases the clear method.
         * An implementation that does some kind of automatic cleaning of obsolete values is recommended (like LRU).
         * @param cacheSize is the size of the new cache (which will be >= 1)
         * @return Instance of the new cache.
         */
        Map<String, ImmutableUserAgent> instantiateCache(int cacheSize);
    }

    private static class DefaultCacheInstantiator implements CacheInstantiator {
        @Override
        public Map<String, ImmutableUserAgent> instantiateCache(int cacheSize) {
            return Caffeine.newBuilder().maximumSize(cacheSize).<String, ImmutableUserAgent>build().asMap();
        }
    }

    public int getCacheSize() {
        return cacheSize;
    }

    // =========================================================
    /**
     * Sets the new size of the client hints parsing cache.
     * Note that this will also wipe the existing cache.
     *
     * @param newCacheSize The size of the new LRU cache. As size of 0 will disable caching.
     */
    public void setClientHintsCacheSize(int newCacheSize) {
        clientHintsAnalyzer.setCacheSize(newCacheSize);
    }

    public int getClientHintsCacheSize() {
        return clientHintsAnalyzer.getCacheSize();
    }

    public void setClientHintsCacheInstantiator(ClientHintsCacheInstantiator<?> clientHintsCacheInstantiator) {
        clientHintsAnalyzer.setCacheInstantiator(clientHintsCacheInstantiator);
    }

    public interface ClientHintsCacheInstantiator<T extends Serializable> extends Serializable {
        /**
         * A single method that must create a new instance of the cache.
         * The returned instance MUST implement at least the {@link Map#get} and {@link Map#put}
         * methods in a threadsafe way if you intend to use this in a multithreaded scenario.
         * Yauaa only uses the put and get methods and in exceptional cases the clear method.
         * An implementation that does some kind of automatic cleaning of obsolete values is recommended (like LRU).
         * @param cacheSize is the size of the new cache (which will be >= 1)
         * @return Instance of the new cache.
         */
        Map<String, T> instantiateCache(int cacheSize);
    }

    // =========================================================

    @Nonnull
    @Override
    public ImmutableUserAgent parse(MutableUserAgent userAgent) {
        // Many caching implementations do not allow null keys and/or values
        if (userAgent == null || userAgent.getUserAgentString() == null) {
            synchronized (this) {
                if (nullAgent == null) {
                    nullAgent = super.parse(new MutableUserAgent((String) null));
                }
                return nullAgent;
            }
        }

        // Do we even have a cache?
        if (parseCache == null) {
            return super.parse(userAgent);
        }

        String cachingKey = userAgent.getUserAgentString();

        if (userAgent.getHeaders().size() > 1) {
            // We must sanitise all client headers because these are the caching key.
            Map<String, String> cleanedHeaders = new TreeMap<>(); // Ordered --> predicatable
            cleanedHeaders.put(USERAGENT_HEADER, userAgent.getUserAgentString());
            for (Map.Entry<String, String> entry : userAgent.getHeaders().entrySet()) {
                String headerName = entry.getKey();
                if (isSupportedClientHintHeader(headerName)) {
                    cleanedHeaders.put(headerName.toLowerCase(Locale.ROOT), entry.getValue());
                }
            }
            userAgent.setHeaders(cleanedHeaders);
            cachingKey = userAgent.getHeaders().toString();
        }

        // As the parse result is immutable it can safely be cached and returned as is
        return parseCache.computeIfAbsent(cachingKey, ua -> super.parse(userAgent));
    }

    @SuppressWarnings("unchecked") // For all the casts of 'this' to 'B'
    public abstract  static class AbstractUserAgentAnalyzerBuilder<UAA extends AbstractUserAgentAnalyzer, B extends AbstractUserAgentAnalyzerBuilder<UAA, B>>
            extends AbstractUserAgentAnalyzerDirectBuilder<UAA, B> {
        private final UAA uaa;

        protected AbstractUserAgentAnalyzerBuilder(UAA newUaa) {
            super(newUaa);
            this.uaa = newUaa;
        }

        // ------------------------------------------

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

        /**
         * Specify a custom class to create the cache.
         * Use this if the default Caffeine cache is unsuitable for your needs.
         * @param cacheInstantiator The class that will create a new cache instance when requested.
         * @return the current Builder instance.
         */
        public B withCacheInstantiator(CacheInstantiator cacheInstantiator) {
            failIfAlreadyBuilt();
            uaa.setCacheInstantiator(cacheInstantiator);
            return (B)this;
        }

        // ------------------------------------------

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
            uaa.setClientHintsCacheInstantiator(cacheInstantiator);
            return (B)this;
        }

        /**
         * Disable ClientHint caching.
         * @return the current Builder instance.
         */
        public B useJava8CompatibleCaching() {
            failIfAlreadyBuilt();
            // Caffeine is a Java 11+ library.
            // This is one is Java 8 compatible.
            return this
                .withCacheInstantiator(
                    (AbstractUserAgentAnalyzer.CacheInstantiator) size ->
                        Collections.synchronizedMap(new LRUMap<>(size)))
                .withClientHintCacheInstantiator(
                    (AbstractUserAgentAnalyzer.ClientHintsCacheInstantiator<?>) size ->
                        Collections.synchronizedMap(new LRUMap<>(size)));
        }

        // ------------------------------------------

        @SuppressWarnings("EmptyMethod") // We must override the method because of the generic return value.
        @Override
        public UAA build() {
            uaa.wasBuilt = true;
            uaa.initializeCache();
            return super.build();
        }
    }

    @Override
    public String toString() {
        return "UserAgentAnalyzer {\n" +
            "cacheSize=" + cacheSize +
            ",\n"+ super.toString()+"\n} ";
    }

}
