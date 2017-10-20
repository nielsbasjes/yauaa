/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2017 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestBasics {

    @Test
    public void testCacheSetter() {
        UserAgentAnalyzer userAgentAnalyzer = new UserAgentAnalyzer("classpath*:AllFields-tests.yaml");

        assertEquals("Incorrect default cache size", 10000, userAgentAnalyzer.getCacheSize());

        userAgentAnalyzer.setCacheSize(50);
        assertEquals("Incorrect cache size", 50, userAgentAnalyzer.getCacheSize());

        userAgentAnalyzer.setCacheSize(50000);
        assertEquals("Incorrect cache size", 50000, userAgentAnalyzer.getCacheSize());

        userAgentAnalyzer.setCacheSize(-5);
        assertEquals("Incorrect cache size", 0, userAgentAnalyzer.getCacheSize());

        userAgentAnalyzer.setCacheSize(50);
        assertEquals("Incorrect cache size", 50, userAgentAnalyzer.getCacheSize());

        userAgentAnalyzer.setCacheSize(50000);
        assertEquals("Incorrect cache size", 50000, userAgentAnalyzer.getCacheSize());

        userAgentAnalyzer.setUserAgentMaxLength(555);
        assertEquals("Incorrect user agent max length", 555, userAgentAnalyzer.getUserAgentMaxLength());
    }

    @Test
    public void testUserAgentMaxLengthSetter() {
        UserAgentAnalyzer userAgentAnalyzer = new UserAgentAnalyzer("classpath*:AllFields-tests.yaml");

        assertEquals("Incorrect default user agent max length", Integer.MAX_VALUE, userAgentAnalyzer.getUserAgentMaxLength());

        userAgentAnalyzer.setUserAgentMaxLength(250);
        assertEquals("Incorrect default user agent max length", 250, userAgentAnalyzer.getUserAgentMaxLength());
    }

    @Test
    public void testUserAgentMaxLengthParsing() {
        UserAgentAnalyzer uaa = UserAgentAnalyzer.newBuilder()
            .withUserAgentMaxLength(67)
            .build();

        UserAgent userAgent =
            uaa.parse("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 ignored part of user agent");

        assertEquals("Trimmed user agent string", userAgent.getUserAgentString(), "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36");
    }
}
