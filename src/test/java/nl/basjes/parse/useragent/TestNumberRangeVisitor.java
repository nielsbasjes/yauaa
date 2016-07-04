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

import nl.basjes.parse.useragent.analyze.NumberRangeList;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class TestNumberRangeVisitor {
    @Test
    public void rangeSingleValue() {
        List<Integer> values = new NumberRangeList(5,5);
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
        List<Integer> values = new NumberRangeList(3,5);
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

}
