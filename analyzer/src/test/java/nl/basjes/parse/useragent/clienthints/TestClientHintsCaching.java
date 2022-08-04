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

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.clienthints.ClientHintsHeadersParser.DefaultClientHintsCacheInstantiator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Map;
import java.util.TreeMap;

import static nl.basjes.parse.useragent.UserAgent.DEVICE_CLASS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@TestMethodOrder(MethodOrderer.MethodName.class)
class TestClientHintsCaching {

    private static final Logger LOG = LogManager.getLogger(TestClientHintsCaching.class);

    // ------------------------------------------

    @Test
    void testBuilder() {
        UserAgentAnalyzer analyzer = UserAgentAnalyzer
            .newBuilder()
            .withClientHintCacheInstantiator(new DefaultClientHintsCacheInstantiator<>())
            .withoutClientHintsCache()
            .withClientHintsCache(1234)
            .withField(DEVICE_CLASS)
            .delayInitialization()
            .build();

        assertEquals(1234, analyzer.getClientHintsCacheSize());
        analyzer.disableCaching();
        assertEquals(0, analyzer.getClientHintsCacheSize());
        analyzer.setClientHintsCacheSize(4321);
        assertEquals(4321, analyzer.getClientHintsCacheSize());
    }

    // ------------------------------------------

    @Test
    void testCachingKey() {
        UserAgentAnalyzer uaa = UserAgentAnalyzer
            .newBuilder()
            .build();

        Map<String, String> requestHeaders1 = new TreeMap<>();
        requestHeaders1.put("User-Agent",                       "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.0.0 Safari/537.36");
        requestHeaders1.put("sec-ch-ua",                        "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"100\", \"Google Chrome\";v=\"100\"");
        requestHeaders1.put("sec-ch-ua-Arch",                   "\"x86\"");
        requestHeaders1.put("sec-ch-ua-Full-Version-List",      "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"100.0.4896.75\", \"Google Chrome\";v=\"100.0.4896.75\"");
        requestHeaders1.put("sec-ch-ua-Mobile",                 "?0");
        requestHeaders1.put("sec-ch-ua-Model",                  "\"\"");
        requestHeaders1.put("sec-ch-ua-Platform",               "\"Windows\"");
        requestHeaders1.put("sec-ch-ua-Platform-Version",       "\"0.1.0\"");
        requestHeaders1.put("sec-ch-ua-Wow64",                  "?0");
        requestHeaders1.put("A-non-ClientHintHeader",           "Something else");
        UserAgent userAgent1 = uaa.parse(requestHeaders1);

        requestHeaders1.put("Another-non-ClientHintHeader",     "More things");

        UserAgent userAgent2 = uaa.parse(requestHeaders1);

        // Both must be identical because the second one is from the cache.
        assertSame(userAgent1, userAgent2);

        // ------------------------------------------

        Map<String, String> requestHeaders2 = new TreeMap<>();
        // Shuffled the order of the headers and changed the case of the header names
        requestHeaders2.put("SEC-CH-UA-Platform-Version",       "\"0.1.0\"");
        requestHeaders2.put("A-non-ClientHintHeader",           "Something else");
        requestHeaders2.put("SEC-CH-UA",                        "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"100\", \"Google Chrome\";v=\"100\"");
        requestHeaders2.put("SEC-CH-UA-Mobile",                 "?0");
        requestHeaders2.put("SEC-CH-UA-Arch",                   "\"x86\"");
        requestHeaders2.put("SEC-CH-UA-Wow64",                  "?0");
        requestHeaders2.put("User-Agent",                       "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.0.0 Safari/537.36");
        requestHeaders2.put("SEC-CH-UA-Model",                  "\"\"");
        requestHeaders2.put("SEC-CH-UA-Full-Version-List",      "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"100.0.4896.75\", \"Google Chrome\";v=\"100.0.4896.75\"");
        requestHeaders2.put("SEC-CH-UA-Platform",               "\"Windows\"");
        UserAgent userAgent3 = uaa.parse(requestHeaders2);

        requestHeaders2.put("Another-non-ClientHintHeader",     "More things");

        UserAgent userAgent4 = uaa.parse(requestHeaders2);

        // All must be identical because they are all from the cache.
        assertSame(userAgent1, userAgent2);
        assertSame(userAgent1, userAgent3);
        assertSame(userAgent1, userAgent4);
        assertSame(userAgent2, userAgent3);
        assertSame(userAgent2, userAgent4);
    }


}
