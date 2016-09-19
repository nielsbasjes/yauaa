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

package nl.basjes.parse.useragent.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestNormalize {

    @Test
    public void checkBrandOne() {
        assertEquals("N", Normalize.brand("n"));
        assertEquals("N", Normalize.brand("N"));
    }

    @Test
    public void checkBrandTwo() {
        assertEquals("NB", Normalize.brand("nb"));
        assertEquals("NB", Normalize.brand("nB"));
        assertEquals("NB", Normalize.brand("Nb"));
        assertEquals("NB", Normalize.brand("NB"));
    }

    @Test
    public void checkBrandThree() {
        assertEquals("NBA", Normalize.brand("nba"));
        assertEquals("NBA", Normalize.brand("nBa"));
        assertEquals("NBA", Normalize.brand("Nba"));
        assertEquals("NBA", Normalize.brand("NBA"));
    }

    @Test
    public void checkBrandNormalizationWord() {
        assertEquals("Niels", Normalize.brand("niels"));
        assertEquals("Niels", Normalize.brand("Niels"));
        assertEquals("Niels", Normalize.brand("NiElS"));
        assertEquals("Niels", Normalize.brand("nIELS"));
        assertEquals("Niels", Normalize.brand("NIELS"));
    }

    @Test
    public void checkBrandNormalizationExamples() {
        assertEquals("NielsBasjes", Normalize.brand("NielsBasjes"));
        assertEquals("NielsBasjes", Normalize.brand("NIelsBasJES"));
        assertEquals("BlackBerry", Normalize.brand("BlackBerry"));
        assertEquals("Blackberry", Normalize.brand("BLACKBERRY"));
    }

}
