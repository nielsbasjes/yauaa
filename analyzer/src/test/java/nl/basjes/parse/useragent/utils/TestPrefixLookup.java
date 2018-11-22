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
    public void testLookupCaseSensitive(){
        Map<String, String> prefixMap = new HashMap<>();
        prefixMap.put("A",      "Result A");
        prefixMap.put("AB",     "Result AB");
        prefixMap.put("ABC",    "Result ABC");
        // The ABCD is missing !!!
        prefixMap.put("ABCDE",  "Result ABCDE");
        prefixMap.put("ABCX",   "Result ABCX");
        prefixMap.put("ABCDX",  "Result ABCDX");

        PrefixLookup prefixLookup = new PrefixLookup(prefixMap, true);

        assertNull(prefixLookup.findLongestMatchingPrefix("MisMatch"));

        assertEquals("Wrong result for 'A'",             "Result A",        prefixLookup.findLongestMatchingPrefix("A"));
        assertEquals("Wrong result for 'AB'",            "Result AB",       prefixLookup.findLongestMatchingPrefix("AB"));
        assertEquals("Wrong result for 'ABC'",           "Result ABC",      prefixLookup.findLongestMatchingPrefix("ABC"));
        assertEquals("Wrong result for 'ABCD'",          "Result ABC",      prefixLookup.findLongestMatchingPrefix("ABCD"));
        assertEquals("Wrong result for 'ABCDE'",         "Result ABCDE",    prefixLookup.findLongestMatchingPrefix("ABCDE"));

        assertEquals("Wrong result for 'ABCD'",          "Result ABC",      prefixLookup.findLongestMatchingPrefix("ABCD"));
        assertEquals("Wrong result for 'AB Something'",  "Result AB",       prefixLookup.findLongestMatchingPrefix("AB Something"));
        assertEquals("Wrong result for 'AAAA'",          "Result A",        prefixLookup.findLongestMatchingPrefix("AAAA"));

        assertEquals("Wrong result for 'AB€'",           "Result AB",       prefixLookup.findLongestMatchingPrefix("AB€"));
        assertEquals("Wrong result for 'AB\\t'",         "Result AB",       prefixLookup.findLongestMatchingPrefix("AB\t"));
    }


    @Test
    public void testLookupCaseInsensitive(){
        Map<String, String> prefixMap = new HashMap<>();
        prefixMap.put("A",      "Result A");
        prefixMap.put("AB",     "Result AB");
        prefixMap.put("ABC",    "Result ABC");
        // The ABCD is missing !!!
        prefixMap.put("ABCDE",  "Result ABCDE");
        prefixMap.put("ABCX",   "Result ABCX");
        prefixMap.put("ABCDX",  "Result ABCDX");

        PrefixLookup prefixLookup = new PrefixLookup(prefixMap, false);

        assertNull(prefixLookup.findLongestMatchingPrefix("MisMatch"));

        assertEquals("Wrong result for 'A'",             "Result A",        prefixLookup.findLongestMatchingPrefix("A"));
        assertEquals("Wrong result for 'AB'",            "Result AB",       prefixLookup.findLongestMatchingPrefix("AB"));
        assertEquals("Wrong result for 'ABC'",           "Result ABC",      prefixLookup.findLongestMatchingPrefix("ABC"));
        assertEquals("Wrong result for 'ABCD'",          "Result ABC",      prefixLookup.findLongestMatchingPrefix("ABCD"));
        assertEquals("Wrong result for 'ABCDE'",         "Result ABCDE",    prefixLookup.findLongestMatchingPrefix("ABCDE"));

        assertEquals("Wrong result for 'ABCD'",          "Result ABC",      prefixLookup.findLongestMatchingPrefix("ABCD"));
        assertEquals("Wrong result for 'AB Something'",  "Result AB",       prefixLookup.findLongestMatchingPrefix("AB Something"));
        assertEquals("Wrong result for 'AAAA'",          "Result A",        prefixLookup.findLongestMatchingPrefix("AAAA"));

        assertEquals("Wrong result for 'AB€'",           "Result AB",       prefixLookup.findLongestMatchingPrefix("AB€"));
        assertEquals("Wrong result for 'AB\\t'",         "Result AB",       prefixLookup.findLongestMatchingPrefix("AB\t"));

        assertEquals("Wrong result for 'a'",             "Result A",        prefixLookup.findLongestMatchingPrefix("a"));
        assertEquals("Wrong result for 'ab'",            "Result AB",       prefixLookup.findLongestMatchingPrefix("ab"));
        assertEquals("Wrong result for 'abc'",           "Result ABC",      prefixLookup.findLongestMatchingPrefix("abc"));
        assertEquals("Wrong result for 'abcd'",          "Result ABC",      prefixLookup.findLongestMatchingPrefix("abcd"));
        assertEquals("Wrong result for 'abcde'",         "Result ABCDE",    prefixLookup.findLongestMatchingPrefix("abcde"));

        assertEquals("Wrong result for 'abcd'",          "Result ABC",      prefixLookup.findLongestMatchingPrefix("abcd"));
        assertEquals("Wrong result for 'ab something'",  "Result AB",       prefixLookup.findLongestMatchingPrefix("ab something"));
        assertEquals("Wrong result for 'aaaa'",          "Result A",        prefixLookup.findLongestMatchingPrefix("aaaa"));

        assertEquals("Wrong result for 'ab€'",           "Result AB",       prefixLookup.findLongestMatchingPrefix("ab€"));
        assertEquals("Wrong result for 'ab\\t'",         "Result AB",       prefixLookup.findLongestMatchingPrefix("ab\t"));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testStoreNonASCII() {
        Map<String, String> prefixMap = new HashMap<>();
        prefixMap.put("12€", "Euro");
        PrefixLookup prefixLookup = new PrefixLookup(prefixMap, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStoreNonASCIITab() {
        Map<String, String> prefixMap = new HashMap<>();
        prefixMap.put("12\t", "Euro");
        PrefixLookup prefixLookup = new PrefixLookup(prefixMap, false);
    }

    @Test
    public void testLookupSpeed(){
        Map<String, String> prefixMap = new HashMap<>();
        prefixMap.put("1",    "Result 1");
        for (int i = 10; i < 1000; i++) {
            prefixMap.put("" + i, "Something");
        }
        PrefixLookup prefixLookup = new PrefixLookup(prefixMap, false);

        long iterations = 100_000_000;

        long start = System.nanoTime();
        for (int i = 0; i<iterations; i++) {
            prefixLookup.findLongestMatchingPrefix("999");
        }
        long stop = System.nanoTime();
        LOG.info("Speed stats: {} runs took {}ms --> {}ns each (={}us) .",
            iterations, (stop - start)/1000000, ((stop - start)/iterations), ((stop - start)/iterations)/1000);
        assertEquals("Result 1", prefixLookup.findLongestMatchingPrefix("1"));
    }

}
