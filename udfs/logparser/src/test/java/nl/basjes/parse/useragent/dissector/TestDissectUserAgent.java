/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2016 Niels Basjes
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

package nl.basjes.parse.useragent.dissector;

import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Field;
import nl.basjes.parse.core.Parser;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestDissectUserAgent {
    public class TestRecordUserAgent {

        private final Map<String, String> results = new HashMap<>(32);

        @SuppressWarnings({"unused"}) // Used via reflection
        @Field({
            "STRING:device_class",
            "STRING:device_name",
            "STRING:device_cpu",

            "STRING:operating_system_class",
            "STRING:operating_system_name",
            "STRING:operating_system_version",

            "STRING:layout_engine_class",
            "STRING:layout_engine_name",
            "STRING:layout_engine_version",

            "STRING:agent_class",
            "STRING:agent_name",
            "STRING:agent_version",
        })
        public void setValue(final String name, final String value) {
            results.put(name, value);
        }
    }

    class UAParser extends Parser<TestRecordUserAgent> {
        public UAParser() {
            super(TestRecordUserAgent.class);
            Dissector userAgentDissector = new UserAgentDissector();
            addDissector(userAgentDissector);
            setRootType(userAgentDissector.getInputType());
        }
    }

    @Test
    public void testUserAgentDissector() throws Exception {
        UAParser uaParser = new UAParser();
        TestRecordUserAgent record = new TestRecordUserAgent();
        uaParser.parse(record, "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.82 Safari/537.36");

        Map<String, String> results = record.results;

        assertEquals("Desktop",       results.get("STRING:device_class"));
        assertEquals("Linux Desktop", results.get("STRING:device_name"));
        assertEquals("Intel x86_64",  results.get("STRING:device_cpu"));

        assertEquals("Desktop",       results.get("STRING:operating_system_class"));
        assertEquals("Linux",         results.get("STRING:operating_system_name"));
        assertEquals("Intel x86_64",  results.get("STRING:operating_system_version"));

        assertEquals("Browser",       results.get("STRING:layout_engine_class"));
        assertEquals("Blink",         results.get("STRING:layout_engine_name"));
        assertEquals("48.0",          results.get("STRING:layout_engine_version"));

        assertEquals("Browser",       results.get("STRING:agent_class"));
        assertEquals("Chrome",        results.get("STRING:agent_name"));
        assertEquals("48.0.2564.82",  results.get("STRING:agent_version"));
    }

    @Test
    public void validateNameConversion() {
        assertEquals("foo",         UserAgentDissector.fieldNameToDissectionName("Foo"));
        assertEquals("foo_bar",     UserAgentDissector.fieldNameToDissectionName("FooBar"));
        assertEquals("foo_bar_baz", UserAgentDissector.fieldNameToDissectionName("FooBarBaz"));
    }


}
