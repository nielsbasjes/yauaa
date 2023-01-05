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

package nl.basjes.parse.useragent.utils;

import org.apache.hc.client5.http.psl.DomainType;
import org.apache.hc.client5.http.psl.PublicSuffixMatcher;
import org.apache.hc.client5.http.psl.PublicSuffixMatcherLoader;
import org.junit.jupiter.api.Test;

import static nl.basjes.parse.useragent.utils.HostnameExtracter.extractHostname;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TestHostnameExtracter {

    @Test
    void testHostnameExtractBadData(){
        assertNull(extractHostname(null));
        assertNull(extractHostname(""));
        assertNull(extractHostname(":::::"));
    }

    @Test
    void testHostnameExtract(){
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test"));
        assertEquals("www.basjes.nl", extractHostname("//www.basjes.nl/test"));
        assertEquals("www.basjes.nl", extractHostname("www.basjes.nl/test"));
        assertEquals("www.basjes.nl", extractHostname("//www.basjes.nl"));
        assertEquals("www.basjes.nl", extractHostname("www.basjes.nl"));
        assertNull(extractHostname("/index.html"));

        // Unable to determine if is is a hostname or a path name, so assume hostname
        assertEquals("index.html", extractHostname("index.html"));
    }

    @Test
    void testHostnameExtractQueryParams() {
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test?foo=foo"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test&foo=foo"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test?foo=foo&bar=bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test&foo=foo?bar=bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test?foo=foo?bar=bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test&foo=foo&bar=bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test????foo=bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test&&&&foo=bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test?&?&foo=bar"));

        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test?foo=foo#Foo#Bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test&foo=foo#Foo#Bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test?foo=foo&bar=bar#Foo#Bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test&foo=foo?bar=bar#Foo#Bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test?foo=foo?bar=bar#Foo#Bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test&foo=foo&bar=bar#Foo#Bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test????foo=bar#Foo#Bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test&&&&foo=bar#Foo#Bar"));
        assertEquals("www.basjes.nl", extractHostname("https://www.basjes.nl/test?&?&foo=bar#Foo#Bar"));
    }

    @Test
    void verifyHTTPCLIENT_2030(){
        // See https://issues.apache.org/jira/browse/HTTPCLIENT-2030
        PublicSuffixMatcher matcher = PublicSuffixMatcherLoader.getDefault();
        assertEquals("unknown", matcher.getDomainRoot("www.example.unknown"));
        assertNull(matcher.getDomainRoot("www.example.unknown", DomainType.ICANN));
    }

}
