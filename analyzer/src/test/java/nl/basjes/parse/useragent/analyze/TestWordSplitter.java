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
