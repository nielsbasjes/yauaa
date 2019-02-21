/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2019 Niels Basjes
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

import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestMatcherList {

    @Test
    public void testNormalUse() {
        MatcherList list = new MatcherList(1);
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
        list.add(new Matcher(null));
        assertEquals(1, list.size());
        assertFalse(list.isEmpty());
        list.add(new Matcher(null));
        assertEquals(2, list.size());
        assertFalse(list.isEmpty());
        Iterator<Matcher> iterator = list.iterator();
        assertTrue(iterator.hasNext());
        Matcher match1 = iterator.next();
        assertNotNull(match1);
        assertTrue(iterator.hasNext());
        Matcher match2 = iterator.next();
        assertNotNull(match2);
        assertFalse(iterator.hasNext());

        list.clear();

        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
        list.add(new Matcher(null));
        assertEquals(1, list.size());
        assertFalse(list.isEmpty());
        list.add(new Matcher(null));
        assertEquals(2, list.size());
        assertFalse(list.isEmpty());
        iterator = list.iterator();
        assertTrue(iterator.hasNext());
        match1 = iterator.next();
        assertNotNull(match1);
        assertTrue(iterator.hasNext());
        match2 = iterator.next();
        assertNotNull(match2);
        assertFalse(iterator.hasNext());


//        assertEquals("one", match1.getKey());
//        assertEquals("two", match1.getValue());
//        assertNull(match1.getResult());
//        assertEquals("three", match2.getKey());
//        assertEquals("four", match2.getValue());
//        assertNull(match2.getResult());
    }

    @Test(expected = NoSuchElementException.class)
    public void testTooMany() {
        MatcherList list = new MatcherList(5);
        list.add(new Matcher(null));
        list.add(new Matcher(null));
        Iterator<Matcher> iterator = list.iterator();

        Matcher match1 = iterator.next(); // Ok
        Matcher match2 = iterator.next(); // Ok
        Matcher match3 = iterator.next(); // Should throw
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedAddAll() {
        new MatcherList(1).addAll(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedRemove() {
        new MatcherList(1).remove(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedRemoveAll() {
        new MatcherList(1).removeAll(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedRetainAll() {
        new MatcherList(1).retainAll(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedContains() {
        assertFalse(new MatcherList(1).contains(null));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedContainsAll() {
        new MatcherList(1).containsAll(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedToArray() {
        new MatcherList(1).toArray(null);
    }

}
