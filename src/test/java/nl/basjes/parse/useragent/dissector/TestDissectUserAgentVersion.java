/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2016 Niels Basjes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package nl.basjes.parse.useragent.dissector;

import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Field;
import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestDissectUserAgentVersion {

    UAParser uaParser;

    @Before
    public void setUp() throws Exception {
        uaParser = new UAParser();
    }

    public class TestRecord {

        private final Map<String, String> results = new HashMap<>();

        @SuppressWarnings({"unused"}) // Used via reflection
        @Field({
            "STRING:major",
            "STRING:minor",
            "STRING:full",
        })
        public void setValue(final String name, final String value) {
            results.put(name, value);
        }

        public void clear() {
            results.clear();
        }
    }

    class UAParser extends Parser<TestRecord> {
        public UAParser() {
            super(TestRecord.class);
            Dissector userAgentVersionDissector = new UserAgentVersionDissector();
            addDissector(userAgentVersionDissector);
            setRootType(userAgentVersionDissector.getInputType());
        }
    }

    @Test
    public void testVersionDissector() throws Exception {
        TestRecord record = new TestRecord();

        record.clear();
        checkVersion("48.0.2564.82",    "48.0.2564.82", "48", "48.0");
        checkVersion("48.0.2564",       "48.0.2564",    "48", "48.0");
        checkVersion("48.0",            "48.0",         "48", "48.0");
        checkVersion("48",              "48",           "48", null);
        checkVersion("48_0_2564_82",    "48.0.2564.82", "48", "48.0");
        checkVersion("48_0_2564",       "48.0.2564",    "48", "48.0");
        checkVersion("48_0",            "48.0",         "48", "48.0");
        checkVersion("48",              "48",           "48", null);
    }

    private void checkVersion(String input, String full, String major, String minor) throws InvalidDissectorException, MissingDissectorsException, DissectionFailure {
        TestRecord record = new TestRecord();
        uaParser.parse(record, input);
        assertEquals("The 'full' of "  + input + " failed", full,  record.results.get("STRING:full"));
        assertEquals("The 'major' of " + input + " failed", major, record.results.get("STRING:major"));
        assertEquals("The 'minor' of " + input + " failed", minor, record.results.get("STRING:minor"));
    }

}
