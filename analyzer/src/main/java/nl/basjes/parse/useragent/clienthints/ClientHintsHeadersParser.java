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

package nl.basjes.parse.useragent.clienthints;

import com.esotericsoftware.kryo.Kryo;
import com.github.benmanes.caffeine.cache.Caffeine;
import nl.basjes.parse.useragent.AbstractUserAgentAnalyzer.ClientHintsCacheInstantiator;
import nl.basjes.parse.useragent.clienthints.parsers.BrandListParser;
import nl.basjes.parse.useragent.clienthints.parsers.CHParser;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUa;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaArch;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaBitness;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaFullVersion;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaFullVersionList;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaMobile;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaModel;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaPlatform;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaPlatformVersion;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaWoW64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;

public class ClientHintsHeadersParser implements Serializable {
    public static final Logger LOG = LogManager.getFormatterLogger("CHParser");

    private final Map<String, CHParser> parsers;
    private List<String> supportedClientHintHeaders;
    private Map<String, Boolean> isSupportedClientHintHeader;
    public ClientHintsHeadersParser() {
        parsers = new LinkedCaseInsensitiveMap<>();
        addParser(new ParseSecChUa()); // Ordering matters: this and the "Full version list" write to the same fields.
        addParser(new ParseSecChUaArch());
        addParser(new ParseSecChUaBitness());
        addParser(new ParseSecChUaFullVersion()); // Deprecated header, may still be useful.
        addParser(new ParseSecChUaFullVersionList());
        addParser(new ParseSecChUaMobile());
        addParser(new ParseSecChUaModel());
        addParser(new ParseSecChUaPlatform());
        addParser(new ParseSecChUaPlatformVersion());

        // We can parse this, yet we do not have any use for it right now.
        addParser(new ParseSecChUaWoW64());

        supportedClientHintHeaders = parsers.values().stream().map(CHParser::inputField).distinct().collect(Collectors.toList());
        isSupportedClientHintHeader = new LinkedCaseInsensitiveMap<>();
        for (String header : supportedClientHintHeaders) {
            isSupportedClientHintHeader.put(header, TRUE);
        }
    }

    public List<String> supportedClientHintHeaders() {
        return supportedClientHintHeaders;
    }

    public boolean isSupportedClientHintHeader(String header) {
        return isSupportedClientHintHeader.containsKey(header);
    }

    private void addParser(CHParser parser) {
        String field = parser.inputField();
        if (parsers.containsKey(field)) {
            throw new IllegalStateException("We have two parsers for the same field (" + field + "): " +
                parsers.get(field).getClass().getSimpleName() +
                " and " +
                parser.getClass().getSimpleName());
        }
        parsers.put(field, parser);
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
        kryo.register(ClientHintsHeadersParser.class);
        kryo.register(ClientHintsHeadersParser.DefaultClientHintsCacheInstantiator.class);
        kryo.register(ClientHints.class);
        kryo.register(BrandListParser.class);
        kryo.register(CHParser.class);
        kryo.register(ParseSecChUa.class);
        kryo.register(ParseSecChUaArch.class);
        kryo.register(ParseSecChUaBitness.class);
        kryo.register(ParseSecChUaFullVersion.class);
        kryo.register(ParseSecChUaFullVersionList.class);
        kryo.register(ParseSecChUaMobile.class);
        kryo.register(ParseSecChUaModel.class);
        kryo.register(ParseSecChUaPlatform.class);
        kryo.register(ParseSecChUaPlatformVersion.class);
        kryo.register(ParseSecChUaWoW64.class);
    }

    /**
     * Tries to find as much usefull information from the client Headers as possible.
     * @param requestHeaders The full list of available request headers that may contain some useable Client Hints.
     * @return An instance of ClientHints. Possibly without anything in it.
     */
    public ClientHints parse(Map<String, String> requestHeaders) {
        ClientHints clientHints = new ClientHints();

        for (Map.Entry<String, String> headerEntry : requestHeaders.entrySet()) {
            String headerName = headerEntry.getKey();
            CHParser parser = parsers.get(headerName);
            if (parser != null) {
                parser.parse(requestHeaders, clientHints, headerName);
            }
        }
        return clientHints;
    }

    @Override
    public String toString() {
        return "ClientHintAnalyzer:" + getClass().getSimpleName();
    }

    private ClientHintsCacheInstantiator<?> clientHintsCacheInstantiator = new DefaultClientHintsCacheInstantiator<>();
    private int clientHintsCacheSize;

    static class DefaultClientHintsCacheInstantiator<T extends Serializable> implements ClientHintsCacheInstantiator<T> {
        public Map<String, T> instantiateCache(int cacheSize) {
            return Caffeine.newBuilder().maximumSize(cacheSize).<String, T>build().asMap();
        }
    }

    public int getCacheSize() {
        return clientHintsCacheSize;
    }

    public void setCacheSize(int newClientHintsCacheSize) {
        newClientHintsCacheSize = Math.max(newClientHintsCacheSize, 0);
        this.clientHintsCacheSize = newClientHintsCacheSize;
    }

    public void setCacheInstantiator(ClientHintsCacheInstantiator<?> newClientHintsCacheInstantiator) {
        this.clientHintsCacheInstantiator = newClientHintsCacheInstantiator;
    }

    public synchronized void initializeCache() {
        parsers.values().forEach(parser -> parser.initializeCache(clientHintsCacheInstantiator, clientHintsCacheSize));
    }

    public synchronized void clearCache() {
        parsers.values().forEach(CHParser::clearCache);
    }
}
