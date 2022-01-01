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

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.junit.jupiter.api.Test;

import static nl.basjes.parse.useragent.UserAgentAnalyzer.DEFAULT_USER_AGENT_MAX_LENGTH;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestBasics {

    @Test
    void testCacheSetter() {
        UserAgentAnalyzer userAgentAnalyzer = UserAgentAnalyzer.newBuilder().build();
        userAgentAnalyzer.loadResources("classpath*:SingleDummyMatcher.yaml");

        assertEquals(10000, userAgentAnalyzer.getCacheSize(), "Incorrect default cache size");

        userAgentAnalyzer.setCacheSize(50);
        assertEquals(50, userAgentAnalyzer.getCacheSize(), "Incorrect default cache size");

        userAgentAnalyzer.setCacheSize(50000);
        assertEquals(50000, userAgentAnalyzer.getCacheSize(), "Incorrect default cache size");

        userAgentAnalyzer.setCacheSize(-5);
        assertEquals(0, userAgentAnalyzer.getCacheSize(), "Incorrect default cache size");

        userAgentAnalyzer.setCacheSize(50);
        assertEquals(50, userAgentAnalyzer.getCacheSize(), "Incorrect default cache size");

        userAgentAnalyzer.setCacheSize(50000);
        assertEquals(50000, userAgentAnalyzer.getCacheSize(), "Incorrect default cache size");

        userAgentAnalyzer.setUserAgentMaxLength(555);
        assertEquals(555, userAgentAnalyzer.getUserAgentMaxLength(), "Incorrect user agent max length");
    }

    @Test
    void testUserAgentMaxLengthSetter() {
        UserAgentAnalyzer userAgentAnalyzer = UserAgentAnalyzer.newBuilder().build();
        userAgentAnalyzer.loadResources("classpath*:SingleDummyMatcher.yaml");

        assertEquals(DEFAULT_USER_AGENT_MAX_LENGTH, userAgentAnalyzer.getUserAgentMaxLength(), "Incorrect default user agent max length");

        userAgentAnalyzer.setUserAgentMaxLength(250);
        assertEquals(250, userAgentAnalyzer.getUserAgentMaxLength(), "Incorrect default user agent max length");

        userAgentAnalyzer.setUserAgentMaxLength(-100);
        assertEquals(DEFAULT_USER_AGENT_MAX_LENGTH, userAgentAnalyzer.getUserAgentMaxLength(), "Incorrect default user agent max length");
    }

}
