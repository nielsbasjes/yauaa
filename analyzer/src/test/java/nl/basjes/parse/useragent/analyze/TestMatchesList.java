/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2020 Niels Basjes
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

import nl.basjes.parse.useragent.analyze.MatchesList.Match;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestMatchesList {

    @Test
    public void testNormalUse() {
        MatchesList list = new MatchesList(5);
        assertEquals(0, list.size());
        list.add("one", "two", null);
        assertEquals(1, list.size());
        list.add("three", "four", null);
        assertEquals(2, list.size());
        Iterator<Match> iterator = list.iterator();
        assertTrue(iterator.hasNext());
        Match match1 = iterator.next();
        assertTrue(iterator.hasNext());
        Match match2 = iterator.next();
        assertFalse(iterator.hasNext());
        assertEquals("one", match1.getKey());
        assertEquals("two", match1.getValue());
        assertNull(match1.getResult());
        assertEquals("three", match2.getKey());
        assertEquals("four", match2.getValue());
        assertNull(match2.getResult());
    }

    @Test
    public void testTooMany() {
        MatchesList list = new MatchesList(5);
        list.add("one", "two", null);
        list.add("three", "four", null);
        Iterator<Match> iterator = list.iterator();

        iterator.next(); // Ok
        iterator.next(); // Ok
        assertThrows(NoSuchElementException.class, iterator::next); // Should throw
    }

    @Test
    public void testSizeAfterClear() {
        MatchesList list = new MatchesList(5);
        list.add("one", "two", null);
        list.add("three", "four", null);

        assertEquals(2, list.size());
        assertEquals("MatchesList(2) [{ \"one\"=\"two\" }, { \"three\"=\"four\" }]", list.toString());

        list.clear();

        assertEquals(0, list.size());
        assertEquals("MatchesList(0) []", list.toString());
    }

    @Test
    public void testUnsupportedAdd() {
        assertThrows(UnsupportedOperationException.class, () ->
            new MatchesList(1).add(null));
    }

    @Test
    public void testUnsupportedAddAll() {
        assertThrows(UnsupportedOperationException.class, () ->
            new MatchesList(1).addAll(null));
    }

    @Test
    public void testUnsupportedRemove() {
        assertThrows(UnsupportedOperationException.class, () ->
            new MatchesList(1).remove(null));
    }

    @Test
    public void testUnsupportedRemoveAll() {
        assertThrows(UnsupportedOperationException.class, () ->
            new MatchesList(1).removeAll(null));
    }

    @Test
    public void testUnsupportedRetainAll() {
        assertThrows(UnsupportedOperationException.class, () ->
            new MatchesList(1).retainAll(null));
    }

    @Test
    public void testUnsupportedContains() {
        assertThrows(UnsupportedOperationException.class, () ->
            assertFalse(new MatchesList(1).contains(null)));
    }

    @Test
    public void testUnsupportedContainsAll() {
        assertThrows(UnsupportedOperationException.class, () ->
            new MatchesList(1).containsAll(null));
    }

    @Test
    public void testUnsupportedToArray() {
        assertThrows(UnsupportedOperationException.class, () ->
            new MatchesList(1).toArray(null));
    }

}
