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

import nl.basjes.parse.useragent.utils.VersionSplitter;
import nl.basjes.parse.useragent.utils.WordSplitter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestWordSplitter {

    @Test
    public void versionSplitterDOT() {
        String value = "1.2.3";

        assertEquals(null      , VersionSplitter.getSingleVersion(value,0));
        assertEquals("1"       , VersionSplitter.getSingleVersion(value,1));
        assertEquals("2"       , VersionSplitter.getSingleVersion(value,2));
        assertEquals("3"       , VersionSplitter.getSingleVersion(value,3));
        assertEquals(null      , VersionSplitter.getSingleVersion(value,4));

        assertEquals(null      , VersionSplitter.getFirstVersions(value,0));
        assertEquals("1"       , VersionSplitter.getFirstVersions(value,1));
        assertEquals("1.2"     , VersionSplitter.getFirstVersions(value,2));
        assertEquals("1.2.3"   , VersionSplitter.getFirstVersions(value,3));
        assertEquals(null      , VersionSplitter.getFirstVersions(value,4));
    }

    @Test
    public void versionSplitterUS() {
        String value = "1_2_3";

        assertEquals(null      , VersionSplitter.getSingleVersion(value,0));
        assertEquals("1"       , VersionSplitter.getSingleVersion(value,1));
        assertEquals("2"       , VersionSplitter.getSingleVersion(value,2));
        assertEquals("3"       , VersionSplitter.getSingleVersion(value,3));
        assertEquals(null      , VersionSplitter.getSingleVersion(value,4));

        assertEquals(null      , VersionSplitter.getFirstVersions(value,0));
        assertEquals("1"       , VersionSplitter.getFirstVersions(value,1));
        assertEquals("1_2"     , VersionSplitter.getFirstVersions(value,2));
        assertEquals("1_2_3"   , VersionSplitter.getFirstVersions(value,3));
        assertEquals(null      , VersionSplitter.getFirstVersions(value,4));
    }

    @Test
    public void versionSplitterMIX1() {
        String value = "1_2.3";

        assertEquals(null      , VersionSplitter.getSingleVersion(value,0));
        assertEquals("1"       , VersionSplitter.getSingleVersion(value,1));
        assertEquals("2"       , VersionSplitter.getSingleVersion(value,2));
        assertEquals("3"       , VersionSplitter.getSingleVersion(value,3));
        assertEquals(null      , VersionSplitter.getSingleVersion(value,4));

        assertEquals(null      , VersionSplitter.getFirstVersions(value,0));
        assertEquals("1"       , VersionSplitter.getFirstVersions(value,1));
        assertEquals("1_2"     , VersionSplitter.getFirstVersions(value,2));
        assertEquals("1_2.3"   , VersionSplitter.getFirstVersions(value,3));
        assertEquals(null      , VersionSplitter.getFirstVersions(value,4));
    }


    @Test
    public void versionSplitterMIX2() {
        String value = "1.2_3";

        assertEquals(null      , VersionSplitter.getSingleVersion(value,0));
        assertEquals("1"       , VersionSplitter.getSingleVersion(value,1));
        assertEquals("2"       , VersionSplitter.getSingleVersion(value,2));
        assertEquals("3"       , VersionSplitter.getSingleVersion(value,3));
        assertEquals(null      , VersionSplitter.getSingleVersion(value,4));

        assertEquals(null      , VersionSplitter.getFirstVersions(value,0));
        assertEquals("1"       , VersionSplitter.getFirstVersions(value,1));
        assertEquals("1.2"     , VersionSplitter.getFirstVersions(value,2));
        assertEquals("1.2_3"   , VersionSplitter.getFirstVersions(value,3));
        assertEquals(null      , VersionSplitter.getFirstVersions(value,4));
    }

    @Test
    public void versionSplitterRanges() {
        String value = "1.2.3.4.5";

        // Single version
        assertEquals(null          , VersionSplitter.getVersionRange(value, 0, 0));
        assertEquals("1"           , VersionSplitter.getVersionRange(value, 1, 1));
        assertEquals("2"           , VersionSplitter.getVersionRange(value, 2, 2));
        assertEquals("3"           , VersionSplitter.getVersionRange(value, 3, 3));
        assertEquals("4"           , VersionSplitter.getVersionRange(value, 4, 4));
        assertEquals("5"           , VersionSplitter.getVersionRange(value, 5, 5));
        assertEquals(null          , VersionSplitter.getVersionRange(value, 6, 6));

        // First versions
        assertEquals(null          , VersionSplitter.getVersionRange(value, 1, 0));
        assertEquals("1"           , VersionSplitter.getVersionRange(value, 1, 1));
        assertEquals("1.2"         , VersionSplitter.getVersionRange(value, 1, 2));
        assertEquals("1.2.3"       , VersionSplitter.getVersionRange(value, 1, 3));
        assertEquals("1.2.3.4"     , VersionSplitter.getVersionRange(value, 1, 4));
        assertEquals("1.2.3.4.5"   , VersionSplitter.getVersionRange(value, 1, 5));
        assertEquals(null          , VersionSplitter.getVersionRange(value, 1, 6));

        // Last versions
        assertEquals(null           , VersionSplitter.getVersionRange(value, 0, -1));
        assertEquals("1.2.3.4.5"    , VersionSplitter.getVersionRange(value, 1, -1));
        assertEquals("2.3.4.5"      , VersionSplitter.getVersionRange(value, 2, -1));
        assertEquals("3.4.5"        , VersionSplitter.getVersionRange(value, 3, -1));
        assertEquals("4.5"          , VersionSplitter.getVersionRange(value, 4, -1));
        assertEquals("5"            , VersionSplitter.getVersionRange(value, 5, -1));
        assertEquals(null           , VersionSplitter.getVersionRange(value, 6, -1));

        // 2 version slice
        assertEquals(null           , VersionSplitter.getVersionRange(value, 0, 1));
        assertEquals("1.2"          , VersionSplitter.getVersionRange(value, 1, 2));
        assertEquals("2.3"          , VersionSplitter.getVersionRange(value, 2, 3));
        assertEquals("3.4"          , VersionSplitter.getVersionRange(value, 3, 4));
        assertEquals("4.5"          , VersionSplitter.getVersionRange(value, 4, 5));
        assertEquals(null           , VersionSplitter.getVersionRange(value, 5, 6));
    }


    @Test
    public void wordSplitter() {
        String value = "one two/3 four-4 five(some more)";

        assertEquals(null                    , WordSplitter.getSingleWord(value,0));
        assertEquals("one"                   , WordSplitter.getSingleWord(value,1));
        assertEquals("two"                   , WordSplitter.getSingleWord(value,2));
        assertEquals("3"                     , WordSplitter.getSingleWord(value,3));
        assertEquals("four"                  , WordSplitter.getSingleWord(value,4));
        assertEquals("4"                     , WordSplitter.getSingleWord(value,5));
        assertEquals("five"                  , WordSplitter.getSingleWord(value,6));
        assertEquals(null                    , WordSplitter.getSingleWord(value,7));

        assertEquals(null                    , WordSplitter.getFirstWords(value,0));
        assertEquals("one"                   , WordSplitter.getFirstWords(value,1));
        assertEquals("one two"               , WordSplitter.getFirstWords(value,2));
        assertEquals("one two/3"             , WordSplitter.getFirstWords(value,3));
        assertEquals("one two/3 four"        , WordSplitter.getFirstWords(value,4));
        assertEquals("one two/3 four-4"      , WordSplitter.getFirstWords(value,5));
        assertEquals("one two/3 four-4 five" , WordSplitter.getFirstWords(value,6));
        assertEquals(null                    , WordSplitter.getFirstWords(value,7));
    }

    @Test
    public void wordSplitterRange() {
        String value = "one two/3 four-4 five(some more)";
        // The '(' is one of the string terminators for this string splitter

        // Single word
        assertEquals(null                    , WordSplitter.getWordRange(value, 0, 0));
        assertEquals("one"                   , WordSplitter.getWordRange(value, 1, 1));
        assertEquals("two"                   , WordSplitter.getWordRange(value, 2, 2));
        assertEquals("3"                     , WordSplitter.getWordRange(value, 3, 3));
        assertEquals("four"                  , WordSplitter.getWordRange(value, 4, 4));
        assertEquals("4"                     , WordSplitter.getWordRange(value, 5, 5));
        assertEquals("five"                  , WordSplitter.getWordRange(value, 6, 6));
        assertEquals(null                    , WordSplitter.getWordRange(value, 7, 7));

        // First words
        assertEquals("one"                   , WordSplitter.getWordRange(value, 1, 1));
        assertEquals("one two"               , WordSplitter.getWordRange(value, 1, 2));
        assertEquals("one two/3"             , WordSplitter.getWordRange(value, 1, 3));
        assertEquals("one two/3 four"        , WordSplitter.getWordRange(value, 1, 4));
        assertEquals("one two/3 four-4"      , WordSplitter.getWordRange(value, 1, 5));
        assertEquals("one two/3 four-4 five" , WordSplitter.getWordRange(value, 1, 6));
        assertEquals(null                    , WordSplitter.getWordRange(value, 1, 7));

        // Last words
        assertEquals("one two/3 four-4 five" , WordSplitter.getWordRange(value, 1, -1));
        assertEquals("two/3 four-4 five"     , WordSplitter.getWordRange(value, 2, -1));
        assertEquals("3 four-4 five"         , WordSplitter.getWordRange(value, 3, -1));
        assertEquals("four-4 five"           , WordSplitter.getWordRange(value, 4, -1));
        assertEquals("4 five"                , WordSplitter.getWordRange(value, 5, -1));
        assertEquals("five"                  , WordSplitter.getWordRange(value, 6, -1));
        assertEquals(null                    , WordSplitter.getWordRange(value, 7, -1));

        // 2 word slices
        assertEquals(null                    , WordSplitter.getWordRange(value, 0, 1));
        assertEquals("one two"               , WordSplitter.getWordRange(value, 1, 2));
        assertEquals("two/3"                 , WordSplitter.getWordRange(value, 2, 3));
        assertEquals("3 four"                , WordSplitter.getWordRange(value, 3, 4));
        assertEquals("four-4"                , WordSplitter.getWordRange(value, 4, 5));
        assertEquals("4 five"                , WordSplitter.getWordRange(value, 5, 6));
        assertEquals(null                    , WordSplitter.getWordRange(value, 6, 7));

        // 3 word slices
        assertEquals(null                    , WordSplitter.getWordRange(value, 0, 2));
        assertEquals("one two/3"             , WordSplitter.getWordRange(value, 1, 3));
        assertEquals("two/3 four"            , WordSplitter.getWordRange(value, 2, 4));
        assertEquals("3 four-4"              , WordSplitter.getWordRange(value, 3, 5));
        assertEquals("four-4 five"           , WordSplitter.getWordRange(value, 4, 6));
        assertEquals(null                    , WordSplitter.getWordRange(value, 5, 7));

        // Edge cases
        assertEquals(null                    , WordSplitter.getWordRange(value, 0,  0));
        assertEquals(null                    , WordSplitter.getWordRange(value, 0, -1));
        assertEquals(null                    , WordSplitter.getWordRange(value,-1, -1));
    }


}
