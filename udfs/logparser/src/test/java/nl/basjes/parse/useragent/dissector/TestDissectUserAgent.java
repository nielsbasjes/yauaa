/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2018 Niels Basjes
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

import nl.basjes.parse.core.test.DissectorTester;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
        assertEquals("foo",         UserAgentDissector.fieldNameToDissectionName("Foo"));
        assertEquals("foo_bar",     UserAgentDissector.fieldNameToDissectionName("FooBar"));
        assertEquals("foo_bar_baz", UserAgentDissector.fieldNameToDissectionName("FooBarBaz"));
    }

}
