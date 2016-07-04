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

package nl.basjes.parse.useragent;

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


}
