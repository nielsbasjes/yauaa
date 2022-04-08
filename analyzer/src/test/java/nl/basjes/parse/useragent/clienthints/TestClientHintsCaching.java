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

import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.clienthints.ClientHintsHeadersParser.DefaultClientHintsCacheInstantiator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static nl.basjes.parse.useragent.UserAgent.DEVICE_CLASS;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.MethodName.class)
class TestClientHintsCaching {

    private static final Logger LOG = LogManager.getFormatterLogger(TestClientHintsCaching.class);

    // ------------------------------------------

    @Test
    void testBuilder() {
        UserAgentAnalyzer analyzer = UserAgentAnalyzer
            .newBuilder()
            .withClientHintCacheInstantiator(new DefaultClientHintsCacheInstantiator<>())
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

}
