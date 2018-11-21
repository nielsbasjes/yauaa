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

package nl.basjes.parse.useragent.utils;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestPrefixLookup {

    private static final Logger LOG = LoggerFactory.getLogger(TestPrefixLookup.class);

    @Test
    public void testLookup(){
        Map<String, String> prefixMap = new HashMap<>();
        prefixMap.put("1",      "Result 1");
        prefixMap.put("12",     "Result 12");
        prefixMap.put("123",    "Result 123");
        // The 1234 is missing !!!
        prefixMap.put("12345",  "Result 12345");
        prefixMap.put("123X",   "Result 123X");
        prefixMap.put("1234X",  "Result 1234X");

        PrefixLookup prefixLookup = new PrefixLookup(prefixMap);

        assertNull(prefixLookup.findLongestMatchingPrefix("MisMatch"));

        assertEquals("Wrong result for '1'",             "Result 1",        prefixLookup.findLongestMatchingPrefix("1"));
        assertEquals("Wrong result for '12'",            "Result 12",       prefixLookup.findLongestMatchingPrefix("12"));
        assertEquals("Wrong result for '123'",           "Result 123",      prefixLookup.findLongestMatchingPrefix("123"));
        assertEquals("Wrong result for '1234'",          "Result 123",      prefixLookup.findLongestMatchingPrefix("1234"));
        assertEquals("Wrong result for '12345'",         "Result 12345",    prefixLookup.findLongestMatchingPrefix("12345"));

        assertEquals("Wrong result for '1234'",          "Result 123",      prefixLookup.findLongestMatchingPrefix("1234"));
        assertEquals("Wrong result for '12 Something'",  "Result 12",       prefixLookup.findLongestMatchingPrefix("12 Something"));
        assertEquals("Wrong result for '1111'",          "Result 1",        prefixLookup.findLongestMatchingPrefix("1111"));

        assertEquals("Wrong result for '12€'",           "Result 12",       prefixLookup.findLongestMatchingPrefix("12€"));
        assertEquals("Wrong result for '12\\t'",           "Result 12",       prefixLookup.findLongestMatchingPrefix("12\t"));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testStoreNonASCII() {
        Map<String, String> prefixMap = new HashMap<>();
        prefixMap.put("12€", "Euro");
        PrefixLookup prefixLookup = new PrefixLookup(prefixMap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStoreNonASCIITab() {
        Map<String, String> prefixMap = new HashMap<>();
        prefixMap.put("12\t", "Euro");
        PrefixLookup prefixLookup = new PrefixLookup(prefixMap);
    }

    @Test
    public void testLookupSpeed(){
        Map<String, String> prefixMap = new HashMap<>();
        prefixMap.put("1",    "Result 1");
        for (int i = 10; i < 1000; i++) {
            prefixMap.put("" + i, "Something");
        }
        PrefixLookup prefixLookup = new PrefixLookup(prefixMap);

        long iterations = 1000000;

        long start = System.nanoTime();
        for (int i = 0; i<iterations; i++) {
            prefixLookup.findLongestMatchingPrefix("999");
        }
        long stop = System.nanoTime();
        LOG.info("Speed stats: {} runs took {}ms --> {}us each.", iterations, (stop - start)/1000000, ((stop - start)/iterations)/1000);
        assertEquals("Result 1", prefixLookup.findLongestMatchingPrefix("1"));
    }

}
