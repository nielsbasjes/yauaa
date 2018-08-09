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

package nl.basjes.parse.useragent.analyze;

import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestMatchesList {

    @Test
    public void testNormalUse() {
        MatchesList list = new MatchesList(5);
        assertEquals(0, list.size());
        list.add("one", "two", null);
        assertEquals(1, list.size());
        list.add("three", "four", null);
        assertEquals(2, list.size());
        Iterator<MatchesList.Match> iterator = list.iterator();
        assertTrue(iterator.hasNext());
        MatchesList.Match match1 = iterator.next();
        assertTrue(iterator.hasNext());
        MatchesList.Match match2 = iterator.next();
        assertFalse(iterator.hasNext());
        assertEquals("one", match1.getKey());
        assertEquals("two", match1.getValue());
        assertNull(match1.getResult());
        assertEquals("three", match2.getKey());
        assertEquals("four", match2.getValue());
        assertNull(match2.getResult());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedAdd() {
        new MatchesList(1).add(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedAddAll() {
        new MatchesList(1).addAll(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedRemove() {
        new MatchesList(1).remove(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedRemoveAll() {
        new MatchesList(1).removeAll(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedRetainAll() {
        new MatchesList(1).retainAll(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedContains() {
        assertFalse(new MatchesList(1).contains(null));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedContainsAll() {
        new MatchesList(1).containsAll(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedToArray() {
        new MatchesList(1).toArray(null);
    }

}
