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

package nl.basjes.parse.useragent.dissector;

import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.test.DissectorTester;
import nl.basjes.parse.core.test.TestRecord;
import nl.basjes.parse.httpdlog.HttpdLoglineParser;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestDissectUserAgent {

    @Test
    public void testUserAgentDissector() {
        DissectorTester
            .create()
            .withDissector(new UserAgentDissector())
            .withInput("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.82 Safari/537.36")
            .expect("STRING:device_class",             "Desktop")
            .expect("STRING:device_name",              "Linux Desktop")
            .expect("STRING:device_cpu",               "Intel x86_64")
            .expect("STRING:operating_system_class",   "Desktop")
            .expect("STRING:operating_system_name",    "Linux")
            .expect("STRING:operating_system_version", "Intel x86_64")
            .expect("STRING:layout_engine_class",      "Browser")
            .expect("STRING:layout_engine_name",       "Blink")
            .expect("STRING:layout_engine_version",    "48.0")
            .expect("STRING:agent_class",              "Browser")
            .expect("STRING:agent_name",               "Chrome")
            .expect("STRING:agent_version",            "48.0.2564.82")
            .checkExpectations();
    }

    @Test
    public void validateNameConversion() {
        UserAgentDissector uad = new UserAgentDissector();
        uad.ensureMappingsExistForFieldName("Foo");
        uad.ensureMappingsExistForFieldName("FooBar");
        uad.ensureMappingsExistForFieldName("FooBarBaz");

        assertEquals("foo",         uad.fieldNameToDissectionName("Foo"));
        assertEquals("foo_bar",     uad.fieldNameToDissectionName("FooBar"));
        assertEquals("foo_bar_baz", uad.fieldNameToDissectionName("FooBarBaz"));
        assertEquals("Foo",         uad.dissectionNameToFieldName("foo"));
        assertEquals("FooBar",      uad.dissectionNameToFieldName("foo_bar"));
        assertEquals("FooBarBaz",   uad.dissectionNameToFieldName("foo_bar_baz"));
    }

    @Test
    public void checkAllPossibleOutputs() {
        Parser<TestRecord> parser = new HttpdLoglineParser<>(TestRecord.class, "%{User-agent}i");
        parser.addDissector(new UserAgentDissector());

        final List<String> possible = DissectorTester
            .create()
            .withParser(parser)
            .getPossible();

        // Only checking a subset of all possible fields.

        // ===============================================================================================
        // The possible from the base parser
        assertPossible(possible, "HTTP.USERAGENT:request.user-agent");

        // Some basic fields
        assertPossible(possible, "STRING:request.user-agent.device_class");
        assertPossible(possible, "STRING:request.user-agent.device_name");
        assertPossible(possible, "STRING:request.user-agent.device_brand");
        assertPossible(possible, "STRING:request.user-agent.device_cpu");
        assertPossible(possible, "STRING:request.user-agent.device_cpu_bits");

        assertPossible(possible, "STRING:request.user-agent.operating_system_class");
        assertPossible(possible, "STRING:request.user-agent.operating_system_name");
        assertPossible(possible, "STRING:request.user-agent.operating_system_version");
        assertPossible(possible, "STRING:request.user-agent.operating_system_name_version");

        assertPossible(possible, "STRING:request.user-agent.layout_engine_class");
        assertPossible(possible, "STRING:request.user-agent.layout_engine_name");
        assertPossible(possible, "STRING:request.user-agent.layout_engine_version");
        assertPossible(possible, "STRING:request.user-agent.layout_engine_version_major");
        assertPossible(possible, "STRING:request.user-agent.layout_engine_name_version");
        assertPossible(possible, "STRING:request.user-agent.layout_engine_name_version_major");

        assertPossible(possible, "STRING:request.user-agent.agent_class");
        assertPossible(possible, "STRING:request.user-agent.agent_name");
        assertPossible(possible, "STRING:request.user-agent.agent_version");
        assertPossible(possible, "STRING:request.user-agent.agent_version_major");
        assertPossible(possible, "STRING:request.user-agent.agent_name_version");
        assertPossible(possible, "STRING:request.user-agent.agent_name_version_major");
        assertPossible(possible, "STRING:request.user-agent.agent_build");
        assertPossible(possible, "STRING:request.user-agent.agent_language");
        assertPossible(possible, "STRING:request.user-agent.agent_language_code");
        assertPossible(possible, "STRING:request.user-agent.agent_information_email");

        // Special: The url is a HTTP.URI wo we should get everything below it too!
        assertPossible(possible, "HTTP.URI:request.user-agent.agent_information_url");
        assertPossible(possible, "HTTP.PROTOCOL:request.user-agent.agent_information_url.protocol");
        assertPossible(possible, "HTTP.USERINFO:request.user-agent.agent_information_url.userinfo");
        assertPossible(possible, "HTTP.HOST:request.user-agent.agent_information_url.host");
        assertPossible(possible, "HTTP.PORT:request.user-agent.agent_information_url.port");
        assertPossible(possible, "HTTP.PATH:request.user-agent.agent_information_url.path");
        assertPossible(possible, "HTTP.QUERYSTRING:request.user-agent.agent_information_url.query");
        assertPossible(possible, "STRING:request.user-agent.agent_information_url.query.*");
        assertPossible(possible, "HTTP.REF:request.user-agent.agent_information_url.ref");

        // ===============================================================================================
        // The possible from the base parser (LAST variant)
        assertPossible(possible, "HTTP.USERAGENT:request.user-agent.last");

        // Some basic fields
        assertPossible(possible, "STRING:request.user-agent.last.device_class");
        assertPossible(possible, "STRING:request.user-agent.last.device_name");
        assertPossible(possible, "STRING:request.user-agent.last.device_brand");
        assertPossible(possible, "STRING:request.user-agent.last.device_cpu");
        assertPossible(possible, "STRING:request.user-agent.last.device_cpu_bits");

        assertPossible(possible, "STRING:request.user-agent.last.operating_system_class");
        assertPossible(possible, "STRING:request.user-agent.last.operating_system_name");
        assertPossible(possible, "STRING:request.user-agent.last.operating_system_version");
        assertPossible(possible, "STRING:request.user-agent.last.operating_system_name_version");

        assertPossible(possible, "STRING:request.user-agent.last.layout_engine_class");
        assertPossible(possible, "STRING:request.user-agent.last.layout_engine_name");
        assertPossible(possible, "STRING:request.user-agent.last.layout_engine_version");
        assertPossible(possible, "STRING:request.user-agent.last.layout_engine_version_major");
        assertPossible(possible, "STRING:request.user-agent.last.layout_engine_name_version");
        assertPossible(possible, "STRING:request.user-agent.last.layout_engine_name_version_major");

        assertPossible(possible, "STRING:request.user-agent.last.agent_class");
        assertPossible(possible, "STRING:request.user-agent.last.agent_name");
        assertPossible(possible, "STRING:request.user-agent.last.agent_version");
        assertPossible(possible, "STRING:request.user-agent.last.agent_version_major");
        assertPossible(possible, "STRING:request.user-agent.last.agent_name_version");
        assertPossible(possible, "STRING:request.user-agent.last.agent_name_version_major");
        assertPossible(possible, "STRING:request.user-agent.last.agent_build");
        assertPossible(possible, "STRING:request.user-agent.last.agent_language");
        assertPossible(possible, "STRING:request.user-agent.last.agent_language_code");
        assertPossible(possible, "STRING:request.user-agent.last.agent_information_email");

        // Special: The url is a HTTP.URI wo we should get everything below it too!
        assertPossible(possible, "HTTP.URI:request.user-agent.last.agent_information_url");
        assertPossible(possible, "HTTP.PROTOCOL:request.user-agent.last.agent_information_url.protocol");
        assertPossible(possible, "HTTP.USERINFO:request.user-agent.last.agent_information_url.userinfo");
        assertPossible(possible, "HTTP.HOST:request.user-agent.last.agent_information_url.host");
        assertPossible(possible, "HTTP.PORT:request.user-agent.last.agent_information_url.port");
        assertPossible(possible, "HTTP.PATH:request.user-agent.last.agent_information_url.path");
        assertPossible(possible, "HTTP.QUERYSTRING:request.user-agent.last.agent_information_url.query");
        assertPossible(possible, "STRING:request.user-agent.last.agent_information_url.query.*");
        assertPossible(possible, "HTTP.REF:request.user-agent.last.agent_information_url.ref");
    }

    private void assertPossible(List<String> possible, String element) {
        assertTrue("The output " + element + " is missing.", possible.contains(element));
    }

    @Test
    public void testExtractUrlFields() {
        Parser<TestRecord> parser = new HttpdLoglineParser<>(TestRecord.class, "%t \"%{User-agent}i\"");
        parser.addDissector(new UserAgentDissector());

        String testUri = "https://yauaa.basjes.nl:8080/something.html?aap=noot&mies=wim#zus";

        String testUserAgent =
            "Mozilla/5.0 (Linux; Android 6.0.1; Nexus 5X Build/MMB29P) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/41.0.2272.96 " +
            "Mobile Safari/537.36" +
            "(" + testUri + ")";

        String testLogLine = "[10/Aug/2012:23:55:11 +0200] \""+testUserAgent+"\"";

        DissectorTester
            .create()
            .withParser(parser)
            .withInput(testLogLine)
            // Did we get the field
            .expect("HTTP.USERAGENT:request.user-agent",                                testUserAgent)

            // Basic dissections
            .expect("STRING:request.user-agent.device_class",                           "Phone")
            .expect("STRING:request.user-agent.agent_name_version",                     "Chrome 41.0.2272.96")
            .expect("HTTP.URI:request.user-agent.agent_information_url",                testUri)

            // Further extractions from the URI we found
            .expect("HTTP.PROTOCOL:request.user-agent.agent_information_url.protocol",  "https")
            .expect("HTTP.HOST:request.user-agent.agent_information_url.host",          "yauaa.basjes.nl")
            .expect("HTTP.PORT:request.user-agent.agent_information_url.port",          "8080")
            .expect("HTTP.PATH:request.user-agent.agent_information_url.path",          "/something.html")
            .expect("HTTP.QUERYSTRING:request.user-agent.agent_information_url.query",  "&aap=noot&mies=wim")
            .expect("STRING:request.user-agent.agent_information_url.query.aap",        "noot")
            .expect("STRING:request.user-agent.agent_information_url.query.mies",       "wim")
            .expect("HTTP.REF:request.user-agent.agent_information_url.ref",            "zus")
            .checkExpectations();
    }


}
