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

package nl.basjes.parse.useragent;

import nl.basjes.parse.useragent.calculate.ConcatNONDuplicatedCalculator;
import nl.basjes.parse.useragent.calculate.FieldCalculator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestConcatenation {

    private UserAgent createUserAgent() {
        UserAgent userAgent = new UserAgent();
        userAgent.setForced("MinusOne", "MinusOne", -1);
        userAgent.setForced("Zero", "Zero", 0);
        userAgent.setForced("One", "One", 1);
        userAgent.setForced("Two", "Two", 2);
        userAgent.setForced("One Two", "One Two", 12);
        return userAgent;
    }

    @Test
    public void testFieldConcatenation() {
        FieldCalculator fc;
        UserAgent       userAgent = createUserAgent();

        fc = new ConcatNONDuplicatedCalculator("Combined1", "One", "Two");
        fc.calculate(userAgent);
        assertEquals("One Two", userAgent.getValue("Combined1"));

        fc = new ConcatNONDuplicatedCalculator("Combined2", "One", "One");
        fc.calculate(userAgent);
        assertEquals("One", userAgent.getValue("Combined2"));

        fc = new ConcatNONDuplicatedCalculator("Combined3", "MinusOne", "One");
        fc.calculate(userAgent);
        assertEquals("MinusOne One", userAgent.getValue("Combined3"));

        fc = new ConcatNONDuplicatedCalculator("Combined4", "One", "MinusOne");
        fc.calculate(userAgent);
        assertEquals("One MinusOne", userAgent.getValue("Combined4"));
    }

    @Test
    public void testFieldConcatenationNulls() {
        FieldCalculator fc;
        UserAgent       userAgent = createUserAgent();

        fc = new ConcatNONDuplicatedCalculator("Combined3", "MinusOne", null);
        fc.calculate(userAgent);
        assertEquals("Unknown", userAgent.getValue("Combined3"));

        fc = new ConcatNONDuplicatedCalculator("Combined4", null, "MinusOne");
        fc.calculate(userAgent);
        assertEquals("Unknown", userAgent.getValue("Combined4"));

        fc = new ConcatNONDuplicatedCalculator("Combined3", null, "One");
        fc.calculate(userAgent);
        assertEquals("One", userAgent.getValue("Combined3"));

        fc = new ConcatNONDuplicatedCalculator("Combined4", "One", null);
        fc.calculate(userAgent);
        assertEquals("One", userAgent.getValue("Combined4"));
    }

    @Test
    public void testFieldConcatenationSamePrefix() {
        FieldCalculator fc;
        UserAgent       userAgent = createUserAgent();

        fc = new ConcatNONDuplicatedCalculator("Combined1", "One", "Two");
        fc.calculate(userAgent);
        assertEquals("One Two", userAgent.getValue("Combined1"));

        fc = new ConcatNONDuplicatedCalculator("Combined2", "One", "One");
        fc.calculate(userAgent);
        assertEquals("One", userAgent.getValue("Combined2"));

        fc = new ConcatNONDuplicatedCalculator("Combined3", "One", "One Two");
        fc.calculate(userAgent);
        assertEquals("One Two", userAgent.getValue("Combined3"));
    }


    @Test
    public void testFieldConcatenationNonExistent() {
        FieldCalculator fc;
        UserAgent       userAgent = createUserAgent();

        fc = new ConcatNONDuplicatedCalculator("Combined2", "One", "NonExistent");
        fc.calculate(userAgent);
        assertEquals("One", userAgent.getValue("Combined2"));

        fc = new ConcatNONDuplicatedCalculator("Combined3", "NonExistent", "Two");
        fc.calculate(userAgent);
        assertEquals("Two", userAgent.getValue("Combined3"));

        fc = new ConcatNONDuplicatedCalculator("Combined4", "NonExistent1", "NonExistent2");
        fc.calculate(userAgent);
        assertEquals("Unknown", userAgent.getValue("Combined4"));
    }

    @Test
    public void testFieldConcatenationNull() {
        FieldCalculator fc;
        UserAgent       userAgent = createUserAgent();

        fc = new ConcatNONDuplicatedCalculator("Combined2", "One", null);
        fc.calculate(userAgent);
        assertEquals("One", userAgent.getValue("Combined2"));

        fc = new ConcatNONDuplicatedCalculator("Combined3", null, "Two");
        fc.calculate(userAgent);
        assertEquals("Two", userAgent.getValue("Combined3"));

        fc = new ConcatNONDuplicatedCalculator("Combined4", null, null);
        fc.calculate(userAgent);
        assertEquals("Unknown", userAgent.getValue("Combined4"));
    }

    @Test
    public void testFieldConcatenationNoConfidence() {
        FieldCalculator fc;
        UserAgent       userAgent = createUserAgent();

        fc = new ConcatNONDuplicatedCalculator("Combined2", "One", "MinusOne");
        fc.calculate(userAgent);
        assertEquals("One MinusOne", userAgent.getValue("Combined2"));

        fc = new ConcatNONDuplicatedCalculator("Combined3", "MinusOne", "Two");
        fc.calculate(userAgent);
        assertEquals("MinusOne Two", userAgent.getValue("Combined3"));
    }

}
