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

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.utils.ListSplitter;
import nl.basjes.parse.useragent.utils.Splitter;
import nl.basjes.parse.useragent.utils.VersionSplitter;
import nl.basjes.parse.useragent.utils.WordSplitter;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TestWordSplitter {

    @Test
    void versionSplitterEmpty() {
        String value = "";
        Splitter splitter = VersionSplitter.getInstance();
        assertEquals(null,       splitter.getSingleSplit(value, -5));
        assertEquals(null,       splitter.getSingleSplit(value, -1));
        assertEquals(null,       splitter.getSingleSplit(value, 0));
        assertEquals(null,       splitter.getSingleSplit(value, 1));
        assertEquals(null,       splitter.getSingleSplit(value, 2));

        assertEquals(null,       splitter.getFirstSplits(value, -5));
        assertEquals(null,       splitter.getFirstSplits(value, -1));
        assertEquals(null,       splitter.getFirstSplits(value, 0));
        assertEquals(null,       splitter.getFirstSplits(value, 1));
        assertEquals(null,       splitter.getFirstSplits(value, 2));
    }

    @Test
    void versionSplitterOne() {
        String value = "123";
        Splitter splitter = VersionSplitter.getInstance();
        assertEquals(null,       splitter.getSingleSplit(value, -5));
        assertEquals(null,       splitter.getSingleSplit(value, -1));
        assertEquals(null,       splitter.getSingleSplit(value, 0));
        assertEquals("123",       splitter.getSingleSplit(value, 1));
        assertEquals(null,       splitter.getSingleSplit(value, 2));

        assertEquals(null,       splitter.getFirstSplits(value, -5));
        assertEquals(null,       splitter.getFirstSplits(value, -1));
        assertEquals(null,       splitter.getFirstSplits(value, 0));
        assertEquals("123",       splitter.getFirstSplits(value, 1));
        assertEquals(null,       splitter.getFirstSplits(value, 2));
    }

    @Test
    void versionSplitterDOT() {
        String value = "1.2.3";
        Splitter splitter = VersionSplitter.getInstance();

        assertEquals(null,       splitter.getSingleSplit(value, -1));
        assertEquals(null,       splitter.getSingleSplit(value, 0));
        assertEquals("1",        splitter.getSingleSplit(value, 1));
        assertEquals("2",        splitter.getSingleSplit(value, 2));
        assertEquals("3",        splitter.getSingleSplit(value, 3));
        assertEquals(null,       splitter.getSingleSplit(value, 4));

        assertEquals(null,       splitter.getFirstSplits(value, -1));
        assertEquals(null,       splitter.getFirstSplits(value, 0));
        assertEquals("1",        splitter.getFirstSplits(value, 1));
        assertEquals("1.2",      splitter.getFirstSplits(value, 2));
        assertEquals("1.2.3",    splitter.getFirstSplits(value, 3));
        assertEquals(null,       splitter.getFirstSplits(value, 4));
    }

    @Test
    void versionSplitterUS() {
        String value = "1_2_3";
        Splitter splitter = VersionSplitter.getInstance();

        assertEquals(null,       splitter.getSingleSplit(value, -1));
        assertEquals(null,       splitter.getSingleSplit(value, 0));
        assertEquals("1",        splitter.getSingleSplit(value, 1));
        assertEquals("2",        splitter.getSingleSplit(value, 2));
        assertEquals("3",        splitter.getSingleSplit(value, 3));
        assertEquals(null,       splitter.getSingleSplit(value, 4));

        assertEquals(null,       splitter.getFirstSplits(value, -1));
        assertEquals(null,       splitter.getFirstSplits(value, 0));
        assertEquals("1",        splitter.getFirstSplits(value, 1));
        assertEquals("1_2",      splitter.getFirstSplits(value, 2));
        assertEquals("1_2_3",    splitter.getFirstSplits(value, 3));
        assertEquals(null,       splitter.getFirstSplits(value, 4));
    }

    @Test
    void versionSplitterMIX1() {
        String value = "1_2.3";
        Splitter splitter = VersionSplitter.getInstance();

        assertEquals(null,       splitter.getSingleSplit(value, -1));
        assertEquals(null,       splitter.getSingleSplit(value, 0));
        assertEquals("1",        splitter.getSingleSplit(value, 1));
        assertEquals("2",        splitter.getSingleSplit(value, 2));
        assertEquals("3",        splitter.getSingleSplit(value, 3));
        assertEquals(null,       splitter.getSingleSplit(value, 4));

        assertEquals(null,       splitter.getFirstSplits(value, -1));
        assertEquals(null,       splitter.getFirstSplits(value, 0));
        assertEquals("1",        splitter.getFirstSplits(value, 1));
        assertEquals("1_2",      splitter.getFirstSplits(value, 2));
        assertEquals("1_2.3",    splitter.getFirstSplits(value, 3));
        assertEquals(null,       splitter.getFirstSplits(value, 4));
    }

    @Test
    void versionSplitterMIX2() {
        String value = "1.2_3";
        Splitter splitter = VersionSplitter.getInstance();

        assertEquals(null,       splitter.getSingleSplit(value, -1));
        assertEquals(null,       splitter.getSingleSplit(value, 0));
        assertEquals("1",        splitter.getSingleSplit(value, 1));
        assertEquals("2",        splitter.getSingleSplit(value, 2));
        assertEquals("3",        splitter.getSingleSplit(value, 3));
        assertEquals(null,       splitter.getSingleSplit(value, 4));

        assertEquals(null,       splitter.getFirstSplits(value, -1));
        assertEquals(null,       splitter.getFirstSplits(value, 0));
        assertEquals("1",        splitter.getFirstSplits(value, 1));
        assertEquals("1.2",      splitter.getFirstSplits(value, 2));
        assertEquals("1.2_3",    splitter.getFirstSplits(value, 3));
        assertEquals(null,       splitter.getFirstSplits(value, 4));
    }

    @Test
    void versionSplitterWWW1() {
        String value = "www.bar.com";
        Splitter splitter = VersionSplitter.getInstance();

        assertEquals("www.bar.com",     splitter.getSingleSplit(value, 1));
        assertEquals(null,              splitter.getSingleSplit(value, 2));

        assertEquals("www.bar.com",     splitter.getFirstSplits(value, 1));
        assertEquals(null,              splitter.getFirstSplits(value, 2));
    }

    @Test
    void versionSplitterWWW2() {
        String value = "http://bar.com";
        Splitter splitter = VersionSplitter.getInstance();

        assertEquals("http://bar.com",  splitter.getSingleSplit(value, 1));
        assertEquals(null,              splitter.getSingleSplit(value, 2));

        assertEquals("http://bar.com",  splitter.getFirstSplits(value, 1));
        assertEquals(null,              splitter.getFirstSplits(value, 2));
    }

    @Test
    void versionSplitterEMail() {
        String value = "foo@bar.com";
        Splitter splitter = VersionSplitter.getInstance();

        assertEquals("foo@bar.com",     splitter.getSingleSplit(value, 1));
        assertEquals(null,              splitter.getSingleSplit(value, 2));

        assertEquals("foo@bar.com",     splitter.getFirstSplits(value, 1));
        assertEquals(null,              splitter.getFirstSplits(value, 2));
    }

    @Test
    void versionSplitterRanges() {
        String value = "1.2.3.4.5";
        Splitter splitter = VersionSplitter.getInstance();

        // Bad values
        assertEquals(null,            splitter.getSplitRange(value, 4, 2));
        assertEquals(null,            splitter.getSplitRange(null, 2, 3));

        // Single version
        assertEquals(null,            splitter.getSplitRange(value, 0, 0));
        assertEquals("1",             splitter.getSplitRange(value, 1, 1));
        assertEquals("2",             splitter.getSplitRange(value, 2, 2));
        assertEquals("3",             splitter.getSplitRange(value, 3, 3));
        assertEquals("4",             splitter.getSplitRange(value, 4, 4));
        assertEquals("5",             splitter.getSplitRange(value, 5, 5));
        assertEquals(null,            splitter.getSplitRange(value, 6, 6));

        // First versions
        assertEquals(null,            splitter.getSplitRange(value, 1, 0));
        assertEquals("1",             splitter.getSplitRange(value, 1, 1));
        assertEquals("1.2",           splitter.getSplitRange(value, 1, 2));
        assertEquals("1.2.3",         splitter.getSplitRange(value, 1, 3));
        assertEquals("1.2.3.4",       splitter.getSplitRange(value, 1, 4));
        assertEquals("1.2.3.4.5",     splitter.getSplitRange(value, 1, 5));
        assertEquals(null,            splitter.getSplitRange(value, 1, 6));

        // Last versions
        assertEquals(null,            splitter.getSplitRange(value, 0, -1));
        assertEquals("1.2.3.4.5",     splitter.getSplitRange(value, 1, -1));
        assertEquals("2.3.4.5",       splitter.getSplitRange(value, 2, -1));
        assertEquals("3.4.5",         splitter.getSplitRange(value, 3, -1));
        assertEquals("4.5",           splitter.getSplitRange(value, 4, -1));
        assertEquals("5",             splitter.getSplitRange(value, 5, -1));
        assertEquals(null,            splitter.getSplitRange(value, 6, -1));

        // 2 version slice
        assertEquals(null,            splitter.getSplitRange(value, 0, 1));
        assertEquals("1.2",           splitter.getSplitRange(value, 1, 2));
        assertEquals("2.3",           splitter.getSplitRange(value, 2, 3));
        assertEquals("3.4",           splitter.getSplitRange(value, 3, 4));
        assertEquals("4.5",           splitter.getSplitRange(value, 4, 5));
        assertEquals(null,            splitter.getSplitRange(value, 5, 6));
    }

    @Test
    void wordSplitterEmpty() {
        String value = "";
        Splitter splitter = WordSplitter.getInstance();
        assertEquals(null,       splitter.getSingleSplit(value, -5));
        assertEquals(null,       splitter.getSingleSplit(value, -1));
        assertEquals(null,       splitter.getSingleSplit(value, 0));
        assertEquals(null,       splitter.getSingleSplit(value, 1));
        assertEquals(null,       splitter.getSingleSplit(value, 2));

        assertEquals(null,       splitter.getFirstSplits(value, -5));
        assertEquals(null,       splitter.getFirstSplits(value, -1));
        assertEquals(null,       splitter.getFirstSplits(value, 0));
        assertEquals(null,       splitter.getFirstSplits(value, 1));
        assertEquals(null,       splitter.getFirstSplits(value, 2));
    }

    @Test
    void wordSplitterOne() {
        String value = "word";
        Splitter splitter = WordSplitter.getInstance();
        assertEquals(null,       splitter.getSingleSplit(value, -5));
        assertEquals(null,       splitter.getSingleSplit(value, -1));
        assertEquals(null,       splitter.getSingleSplit(value, 0));
        assertEquals("word",       splitter.getSingleSplit(value, 1));
        assertEquals(null,       splitter.getSingleSplit(value, 2));

        assertEquals(null,       splitter.getFirstSplits(value, -5));
        assertEquals(null,       splitter.getFirstSplits(value, -1));
        assertEquals(null,       splitter.getFirstSplits(value, 0));
        assertEquals("word",       splitter.getFirstSplits(value, 1));
        assertEquals(null,       splitter.getFirstSplits(value, 2));
    }

    @Test
    void wordSplitter() {
        String value = "one two/3 four-4 five(some more)";
        Splitter splitter = WordSplitter.getInstance();

        assertEquals(null,                     splitter.getSingleSplit(value, 0));
        assertEquals("one",                    splitter.getSingleSplit(value, 1));
        assertEquals("two",                    splitter.getSingleSplit(value, 2));
        assertEquals("3",                      splitter.getSingleSplit(value, 3));
        assertEquals("four",                   splitter.getSingleSplit(value, 4));
        assertEquals("4",                      splitter.getSingleSplit(value, 5));
        assertEquals("five",                   splitter.getSingleSplit(value, 6));
        assertEquals(null,                     splitter.getSingleSplit(value, 7));

        assertEquals(null,                     splitter.getFirstSplits(value, 0));
        assertEquals("one",                    splitter.getFirstSplits(value, 1));
        assertEquals("one two",                splitter.getFirstSplits(value, 2));
        assertEquals("one two/3",              splitter.getFirstSplits(value, 3));
        assertEquals("one two/3 four",         splitter.getFirstSplits(value, 4));
        assertEquals("one two/3 four-4",       splitter.getFirstSplits(value, 5));
        assertEquals("one two/3 four-4 five",  splitter.getFirstSplits(value, 6));
        assertEquals(null,                     splitter.getFirstSplits(value, 7));
    }

    @Test
    void wordSplitterRange() {
        String value = "one two/3 four-4 five(some more)";
        // The '(' is one of the string terminators for this string splitter

        Splitter splitter = WordSplitter.getInstance();

        // Single word
        assertEquals(null,                     splitter.getSplitRange(value, 0, 0));
        assertEquals("one",                    splitter.getSplitRange(value, 1, 1));
        assertEquals("two",                    splitter.getSplitRange(value, 2, 2));
        assertEquals("3",                      splitter.getSplitRange(value, 3, 3));
        assertEquals("four",                   splitter.getSplitRange(value, 4, 4));
        assertEquals("4",                      splitter.getSplitRange(value, 5, 5));
        assertEquals("five",                   splitter.getSplitRange(value, 6, 6));
        assertEquals(null,                     splitter.getSplitRange(value, 7, 7));

        // First words
        assertEquals("one",                    splitter.getSplitRange(value, 1, 1));
        assertEquals("one two",                splitter.getSplitRange(value, 1, 2));
        assertEquals("one two/3",              splitter.getSplitRange(value, 1, 3));
        assertEquals("one two/3 four",         splitter.getSplitRange(value, 1, 4));
        assertEquals("one two/3 four-4",       splitter.getSplitRange(value, 1, 5));
        assertEquals("one two/3 four-4 five",  splitter.getSplitRange(value, 1, 6));
        assertEquals(null,                     splitter.getSplitRange(value, 1, 7));

        // Last words
        assertEquals("one two/3 four-4 five",  splitter.getSplitRange(value, 1, -1));
        assertEquals("two/3 four-4 five",      splitter.getSplitRange(value, 2, -1));
        assertEquals("3 four-4 five",          splitter.getSplitRange(value, 3, -1));
        assertEquals("four-4 five",            splitter.getSplitRange(value, 4, -1));
        assertEquals("4 five",                 splitter.getSplitRange(value, 5, -1));
        assertEquals("five",                   splitter.getSplitRange(value, 6, -1));
        assertEquals(null,                     splitter.getSplitRange(value, 7, -1));

        // 2 word slices
        assertEquals(null,                     splitter.getSplitRange(value, 0, 1));
        assertEquals("one two",                splitter.getSplitRange(value, 1, 2));
        assertEquals("two/3",                  splitter.getSplitRange(value, 2, 3));
        assertEquals("3 four",                 splitter.getSplitRange(value, 3, 4));
        assertEquals("four-4",                 splitter.getSplitRange(value, 4, 5));
        assertEquals("4 five",                 splitter.getSplitRange(value, 5, 6));
        assertEquals(null,                     splitter.getSplitRange(value, 6, 7));

        // 3 word slices
        assertEquals(null,                     splitter.getSplitRange(value, 0, 2));
        assertEquals("one two/3",              splitter.getSplitRange(value, 1, 3));
        assertEquals("two/3 four",             splitter.getSplitRange(value, 2, 4));
        assertEquals("3 four-4",               splitter.getSplitRange(value, 3, 5));
        assertEquals("four-4 five",            splitter.getSplitRange(value, 4, 6));
        assertEquals(null,                     splitter.getSplitRange(value, 5, 7));

        // Edge cases
        assertEquals(null,                     splitter.getSplitRange(value, 0,  0));
        assertEquals(null,                     splitter.getSplitRange(value, 0, -1));
        assertEquals(null,                     splitter.getSplitRange(value, -1, -1));
    }


    @Test
    void testSplitList(){
        String value = "one two/3 four-4 five(some more)";
        Splitter splitter = WordSplitter.getInstance();

        List<Pair<Integer, Integer>> splitList = splitter.createSplitList(value);

        // Illegal values
        assertEquals(null,                     splitter.getSplitRange(value, splitList, -5,  0));
        assertEquals(null,                     splitter.getSplitRange(value, splitList,  0, -5));
        assertEquals(null,                     splitter.getSplitRange(value, splitList, -5, -5));

        // Single word
        assertEquals(null,                     splitter.getSplitRange(value, splitList, 0, 0));
        assertEquals("one",                    splitter.getSplitRange(value, splitList, 1, 1));
        assertEquals("two",                    splitter.getSplitRange(value, splitList, 2, 2));
        assertEquals("3",                      splitter.getSplitRange(value, splitList, 3, 3));
        assertEquals("four",                   splitter.getSplitRange(value, splitList, 4, 4));
        assertEquals("4",                      splitter.getSplitRange(value, splitList, 5, 5));
        assertEquals("five",                   splitter.getSplitRange(value, splitList, 6, 6));
        assertEquals(null,                     splitter.getSplitRange(value, splitList, 7, 7));

        // First words
        assertEquals("one",                    splitter.getSplitRange(value, splitList, 1, 1));
        assertEquals("one two",                splitter.getSplitRange(value, splitList, 1, 2));
        assertEquals("one two/3",              splitter.getSplitRange(value, splitList, 1, 3));
        assertEquals("one two/3 four",         splitter.getSplitRange(value, splitList, 1, 4));
        assertEquals("one two/3 four-4",       splitter.getSplitRange(value, splitList, 1, 5));
        assertEquals("one two/3 four-4 five",  splitter.getSplitRange(value, splitList, 1, 6));
        assertEquals(null,                     splitter.getSplitRange(value, splitList, 1, 7));

        // Last words
        assertEquals("one two/3 four-4 five",  splitter.getSplitRange(value, splitList, 1, -1));
        assertEquals("two/3 four-4 five",      splitter.getSplitRange(value, splitList, 2, -1));
        assertEquals("3 four-4 five",          splitter.getSplitRange(value, splitList, 3, -1));
        assertEquals("four-4 five",            splitter.getSplitRange(value, splitList, 4, -1));
        assertEquals("4 five",                 splitter.getSplitRange(value, splitList, 5, -1));
        assertEquals("five",                   splitter.getSplitRange(value, splitList, 6, -1));
        assertEquals(null,                     splitter.getSplitRange(value, splitList, 7, -1));

        // 2 word slices
        assertEquals(null,                     splitter.getSplitRange(value, splitList, 0, 1));
        assertEquals("one two",                splitter.getSplitRange(value, splitList, 1, 2));
        assertEquals("two/3",                  splitter.getSplitRange(value, splitList, 2, 3));
        assertEquals("3 four",                 splitter.getSplitRange(value, splitList, 3, 4));
        assertEquals("four-4",                 splitter.getSplitRange(value, splitList, 4, 5));
        assertEquals("4 five",                 splitter.getSplitRange(value, splitList, 5, 6));
        assertEquals(null,                     splitter.getSplitRange(value, splitList, 6, 7));

        // 3 word slices
        assertEquals(null,                     splitter.getSplitRange(value, splitList, 0, 2));
        assertEquals("one two/3",              splitter.getSplitRange(value, splitList, 1, 3));
        assertEquals("two/3 four",             splitter.getSplitRange(value, splitList, 2, 4));
        assertEquals("3 four-4",               splitter.getSplitRange(value, splitList, 3, 5));
        assertEquals("four-4 five",            splitter.getSplitRange(value, splitList, 4, 6));
        assertEquals(null,                     splitter.getSplitRange(value, splitList, 5, 7));

        // Edge cases
        assertEquals(null,                     splitter.getSplitRange(value, splitList, 0,  0));
        assertEquals(null,                     splitter.getSplitRange(value, splitList, 0, -1));
        assertEquals(null,                     splitter.getSplitRange(value, splitList, -1, -1));
    }

    @Test
    void badCalls() {
        String value = "one two/3 four-4 five(some more)";
        Splitter splitter = WordSplitter.getInstance();

        assertNull(splitter.getSplitRange(null,  1, 2));
        assertNull(splitter.getSplitRange(value, -1, 2));
        assertNull(splitter.getSplitRange(value, 1, -2));
        assertNull(splitter.getSplitRange(value, 3, 2));
    }


    @Test
    void segmentSplitterRange() {
        String value = "one two | three | four five";

        Splitter splitter = ListSplitter.getInstance();

        // Single word
        assertEquals(null,                              splitter.getSplitRange(value, 0, 0));
        assertEquals("one two ",                        splitter.getSplitRange(value, 1, 1));
        assertEquals(" three ",                         splitter.getSplitRange(value, 2, 2));
        assertEquals(" four five",                      splitter.getSplitRange(value, 3, 3));
        assertEquals(null,                              splitter.getSplitRange(value, 4, 4));

        // First words
        assertEquals("one two ",                        splitter.getSplitRange(value, 1, 1));
        assertEquals("one two | three ",                splitter.getSplitRange(value, 1, 2));
        assertEquals("one two | three | four five",     splitter.getSplitRange(value, 1, 3));
        assertEquals(null,                              splitter.getSplitRange(value, 1, 4));

        // Last words
        assertEquals("one two | three | four five",     splitter.getSplitRange(value, 1, -1));
        assertEquals(" three | four five",              splitter.getSplitRange(value, 2, -1));
        assertEquals(" four five",                      splitter.getSplitRange(value, 3, -1));
        assertEquals(null,                              splitter.getSplitRange(value, 4, -1));

        // 2 word slices
        assertEquals(null,                              splitter.getSplitRange(value, 0, 1));
        assertEquals("one two | three ",                splitter.getSplitRange(value, 1, 2));
        assertEquals(" three | four five",              splitter.getSplitRange(value, 2, 3));
        assertEquals(null,                              splitter.getSplitRange(value, 3, 4));

        // 3 word slices
        assertEquals(null,                              splitter.getSplitRange(value, 0, 2));
        assertEquals("one two | three | four five",     splitter.getSplitRange(value, 1, 3));
        assertEquals(null,                              splitter.getSplitRange(value, 2, 4));

        // Edge cases
        assertEquals(null,                              splitter.getSplitRange(value, 0,  0));
        assertEquals(null,                              splitter.getSplitRange(value, 0, -1));
        assertEquals(null,                              splitter.getSplitRange(value, -1, -1));
    }

}
