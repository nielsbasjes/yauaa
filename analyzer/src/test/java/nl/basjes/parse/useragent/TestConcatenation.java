/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2023 Niels Basjes
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

import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;
import nl.basjes.parse.useragent.calculate.ConcatNONDuplicatedCalculator;
import nl.basjes.parse.useragent.calculate.FieldCalculator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestConcatenation {

    private MutableUserAgent createUserAgent() {
        MutableUserAgent userAgent = new MutableUserAgent();
        userAgent.setForced("MinusOne", "MinusOne", -1);
        userAgent.setForced("Zero", "Zero", 0);
        userAgent.setForced("One", "One", 1);
        userAgent.setForced("Two", "Two", 2);
        userAgent.setForced("One Two", "One Two", 12);
        return userAgent;
    }

    @Test
    void testFieldConcatenation() {
        FieldCalculator fc;
        MutableUserAgent       userAgent = createUserAgent();

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
    void testFieldConcatenationSamePrefix() {
        FieldCalculator fc;
        MutableUserAgent       userAgent = createUserAgent();

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
    void testFieldConcatenationNonExistent() {
        FieldCalculator fc;
        MutableUserAgent       userAgent = createUserAgent();

        fc = new ConcatNONDuplicatedCalculator("Combined2", "One", "NonExistent");
        fc.calculate(userAgent);
        assertEquals("One Unknown", userAgent.getValue("Combined2"));

        fc = new ConcatNONDuplicatedCalculator("Combined3", "NonExistent", "Two");
        fc.calculate(userAgent);
        assertEquals("Unknown Two", userAgent.getValue("Combined3"));

        fc = new ConcatNONDuplicatedCalculator("Combined4", "NonExistent1", "NonExistent2");
        fc.calculate(userAgent);
        assertEquals("Unknown", userAgent.getValue("Combined4"));
    }

    @Test
    void testFieldConcatenationNoConfidence() {
        FieldCalculator fc;
        MutableUserAgent       userAgent = createUserAgent();

        fc = new ConcatNONDuplicatedCalculator("Combined2", "One", "MinusOne");
        fc.calculate(userAgent);
        assertEquals("One MinusOne", userAgent.getValue("Combined2"));

        fc = new ConcatNONDuplicatedCalculator("Combined3", "MinusOne", "Two");
        fc.calculate(userAgent);
        assertEquals("MinusOne Two", userAgent.getValue("Combined3"));
    }

    @Test
    void testFieldConcatenationAllDefaults() {
        FieldCalculator fc;
        MutableUserAgent       userAgent = new MutableUserAgent();
        userAgent.set("FooBarName", "Dummy", 1);
        userAgent.set("FooBarVersion", "Dummy", 1);
        userAgent.reset();

        fc = new ConcatNONDuplicatedCalculator("FooBarNameVersion", "FooBarName", "FooBarVersion");
        fc.calculate(userAgent);
        assertEquals("Unknown", userAgent.getValue("FooBarName"));
        assertEquals("??", userAgent.getValue("FooBarVersion"));
        assertEquals("Unknown ??", userAgent.getValue("FooBarNameVersion"));

        assertTrue(userAgent.get("FooBarName").isDefaultValue());
        assertTrue(userAgent.get("FooBarVersion").isDefaultValue());
        assertTrue(userAgent.get("FooBarNameVersion").isDefaultValue());
    }

}
