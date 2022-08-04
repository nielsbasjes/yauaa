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

import nl.basjes.parse.useragent.AbstractUserAgentAnalyzerDirect.HeaderSpecification;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUa;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static nl.basjes.parse.useragent.analyze.UserAgentStringMatchMaker.DEFAULT_USER_AGENT_MAX_LENGTH;
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
    }

    @Test
    void testUserAgentMaxLengthSetter() {
        UserAgentAnalyzer userAgentAnalyzer;

        userAgentAnalyzer = UserAgentAnalyzer.newBuilder().build();
        userAgentAnalyzer.loadResources("classpath*:SingleDummyMatcher.yaml");
        assertEquals(DEFAULT_USER_AGENT_MAX_LENGTH, userAgentAnalyzer.getUserAgentMaxLength(), "Incorrect default user agent max length");

        userAgentAnalyzer = UserAgentAnalyzer.newBuilder().withUserAgentMaxLength(250).build();
        userAgentAnalyzer.loadResources("classpath*:SingleDummyMatcher.yaml");
        assertEquals(250, userAgentAnalyzer.getUserAgentMaxLength(), "Incorrect default user agent max length");

        userAgentAnalyzer = UserAgentAnalyzer.newBuilder().withUserAgentMaxLength(-100).build();
        userAgentAnalyzer.loadResources("classpath*:SingleDummyMatcher.yaml");
        assertEquals(DEFAULT_USER_AGENT_MAX_LENGTH, userAgentAnalyzer.getUserAgentMaxLength(), "Incorrect default user agent max length");
    }

    @Test
    void testUserAgentMaxLengthEnforcementNormal() {
        UserAgentAnalyzer userAgentAnalyzer;

        userAgentAnalyzer = UserAgentAnalyzer
            .newBuilder()
            .dropTests()
            .dropDefaultResources()
            .addResources("classpath*:SingleDummyMatcher.yaml")
            .immediateInitialization()
            .withUserAgentMaxLength(10000)
            .build();

        String userAgentString = "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36 " +
            "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
        UserAgent parsedAgent = userAgentAnalyzer.parse(userAgentString);

        // The requested fields
        assertEquals("Unknown", parsedAgent.getValue("HackerAttackVector"));
    }

    @Test
    void testUserAgentMaxLengthEnforcementOverflow() {
        UserAgentAnalyzer userAgentAnalyzer;

        userAgentAnalyzer = UserAgentAnalyzer
            .newBuilder()
            .dropTests()
            .dropDefaultResources()
            .addResources("classpath*:SingleDummyMatcher.yaml")
            .immediateInitialization()
            .withUserAgentMaxLength(100)
            .build();

        String userAgentString = "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36 " +
            "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
        UserAgent parsedAgent = userAgentAnalyzer.parse(userAgentString);

        // The requested fields
        assertEquals("Buffer overflow", parsedAgent.getValue("HackerAttackVector"));
    }

    @Test
    void testHeaderSpecifications() {
        UserAgentAnalyzer analyzer = UserAgentAnalyzer.newBuilder()
            .hideMatcherLoadStats().delayInitialization().build();
        Map<String, HeaderSpecification> allSupportedHeaders = analyzer.getAllSupportedHeaders();
        HeaderSpecification secChUaHeaderSpec = allSupportedHeaders.get(ParseSecChUa.HEADER_FIELD);

        assertEquals(ParseSecChUa.HEADER_FIELD, secChUaHeaderSpec.getHeaderName());
        assertEquals(ParseSecChUa.HEADER_SPEC, secChUaHeaderSpec.getSpecificationSummary());
        assertEquals(ParseSecChUa.HEADER_SPEC_URL, secChUaHeaderSpec.getSpecificationUrl());
    }

}
