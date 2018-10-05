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

import nl.basjes.parse.useragent.analyze.WordRangeVisitor.Range;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class TestNumberRangeVisitor {
    @Test
    public void rangeSingleValue() {
        List<Integer> values = new NumberRangeList(5, 5);
        assertEquals(1, values.size());
        assertFalse(values.contains(1));
        assertFalse(values.contains(2));
        assertFalse(values.contains(3));
        assertFalse(values.contains(4));

        assertTrue(values.contains(5));

        assertFalse(values.contains(6));
        assertFalse(values.contains(7));
        assertFalse(values.contains(8));
        assertFalse(values.contains(9));
    }

    @Test
    public void rangeMultipleValues() {
        List<Integer> values = new NumberRangeList(3, 5);
        assertEquals(3, values.size());
        assertFalse(values.contains(1));
        assertFalse(values.contains(2));

        assertTrue(values.contains(3));
        assertTrue(values.contains(4));
        assertTrue(values.contains(5));

        assertFalse(values.contains(6));
        assertFalse(values.contains(7));
        assertFalse(values.contains(8));
        assertFalse(values.contains(9));
    }

    @Test
    public void testRangeCompare() {
        Range range1a = new Range(1, 2);
        Range range1b = new Range(1, 2);
        Range range2 = new Range(2, 1);
        String notARange = "Range";

        assertEquals(range1a, range1b);
        assertNotEquals(range1a, range2);
        assertNotEquals(range1a, notARange);
    }

}
