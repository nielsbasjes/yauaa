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

import nl.basjes.parse.useragent.analyze.MatchesList.Match;
import nl.basjes.parse.useragent.parse.MatcherTree;
import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static nl.basjes.parse.useragent.parse.AgentPathFragment.AGENT;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.PRODUCT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestMatchesList {

    MatcherTree agent   = new MatcherTree(AGENT, 0);
    MatcherTree product = agent.getOrCreateChild(PRODUCT, 1);

    @Test
    public void testNormalUse() {
        MatchesList list = new MatchesList(5);
        assertEquals(0, list.size());
        list.add(null, agent, "two");
        assertEquals(1, list.size());
        list.add(null, product, "four");
        assertEquals(2, list.size());
        Iterator<Match> iterator = list.iterator();
        assertTrue(iterator.hasNext());
        Match match1 = iterator.next();
        assertTrue(iterator.hasNext());
        Match match2 = iterator.next();
        assertFalse(iterator.hasNext());
        assertEquals("agent", match1.getKey().toString());
        assertEquals("two", match1.getValue());
        assertNull(match1.getResult());
        assertEquals("agent.(1)product", match2.getKey().toString());
        assertEquals("four", match2.getValue());
        assertNull(match2.getResult());
    }

    @Test(expected = NoSuchElementException.class)
    public void testTooMany() {
        MatchesList list = new MatchesList(5);
        list.add(null, agent, "two");
        list.add(null, product, "four");
        Iterator<Match> iterator = list.iterator();

        Match match1 = iterator.next(); // Ok
        Match match2 = iterator.next(); // Ok
        Match match3 = iterator.next(); // Should throw
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
