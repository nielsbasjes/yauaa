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

import nl.basjes.parse.useragent.analyze.WordRangeVisitor.Range;
import nl.basjes.parse.useragent.analyze.treewalker.steps.Step;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepContains;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepDefaultIfNull;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepEndsWith;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepEquals;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepIsInSet;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepIsNotInSet;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepIsNull;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepNotEquals;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepStartsWith;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepIsInLookupContains;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepIsInLookupPrefix;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepIsNotInLookupPrefix;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepLookup;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepLookupContains;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepLookupPrefix;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepBackToFull;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepCleanVersion;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepConcat;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepConcatPostfix;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepConcatPrefix;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepNormalizeBrand;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepSegmentRange;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepWordRange;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepDown;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepNext;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepNextN;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepPrev;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepPrevN;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepUp;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestSteps {

    private static Map<String, String> lookup;
    private static Set<String>         set;

    @BeforeAll
    public static void init() {
        lookup = new HashMap<>();
        lookup.put("foo", "FooFoo");
        lookup.put("bar", "BarBar");
        set = new HashSet<>(Arrays.asList("foo", "bar"));
    }

    @Test
    public void testStepContains() {
        Step step = new StepContains("Foo");
        assertNull(step.walk(null, null));
        assertNull(step.walk(null, "Something"));
        assertEquals("SomeFooBar", step.walk(null, "SomeFooBar").getValue());
    }

    @Test
    public void testStepDefaultIfNull() {
        Step step = new StepDefaultIfNull("Foo");
        assertEquals("Bar", step.walk(null, "Bar").getValue());
        assertEquals("Foo", step.walk(null, null).getValue());
    }

    @Test
    public void testStepDefaultIfNullNoDefault() {
        Step step = new StepDefaultIfNull(null);
        assertEquals("Bar", step.walk(null, "Bar").getValue());
        assertNull(step.walk(null, null).getValue());
    }

    @Test
    public void testStepEndsWith() {
        Step step = new StepEndsWith("Foo");
        assertNull(step.walk(null, null));
        assertNull(step.walk(null, "Something"));
        assertEquals("SomethingFoo", step.walk(null, "SomethingFoo").getValue());
    }

    @Test
    public void testStepEquals() {
        Step step = new StepEquals("Foo");
        assertNull(step.walk(null, null));
        assertNull(step.walk(null, "Something"));
        assertEquals("Foo", step.walk(null, "Foo").getValue());
    }

    @Test
    public void testStepIsInSet() {
        Step step = new StepIsInSet("MySet", set);
        assertNull(step.walk(null, null));
        assertNull(step.walk(null, "Something"));
        assertEquals("Foo", step.walk(null, "Foo").getValue());
    }

    @Test
    public void testStepIsNotInSet() {
        Step step = new StepIsNotInSet("MySet", set);
        assertNull(step.walk(null, null));
        assertEquals("Something", step.walk(null, "Something").getValue());
        assertNull(step.walk(null, "Foo"));
    }

    @Test
    public void testStepIsNull() {
        Step step = new StepIsNull();
        assertNull(step.walk(null, "Something"));
        assertNotNull(step.walk(null, null));
    }


    @Test
    public void testStepNotEquals() {
        Step step = new StepNotEquals("Foo");
        assertNull(step.walk(null, null));
        assertNull(step.walk(null, "Foo"));
        assertEquals("Bar", step.walk(null, "Bar").getValue());
    }


    @Test
    public void testStepStartsWith() {
        Step step = new StepStartsWith("Foo");
        assertNull(step.walk(null, null));
        assertNull(step.walk(null, "Something"));
        assertEquals("FooFoo", step.walk(null, "FooFoo").getValue());
    }


    @Test
    public void testStepIsInLookupContains() {
        Step step = new StepIsInLookupContains("Foo", lookup);
        assertNull(step.walk(null, null));
        assertNull(step.walk(null, "Something"));
        assertEquals("FooFoo", step.walk(null, "FooFoo").getValue());
    }

    @Test
    public void testStepIsInLookupPrefix() {
        Step step = new StepIsInLookupPrefix("Foo", lookup);
        assertNull(step.walk(null, null));
        assertNull(step.walk(null, "Something"));
        assertEquals("FooFoo", step.walk(null, "FooFoo").getValue());
    }

    @Test
    public void testStepIsNotInLookupPrefix() {
        Step step = new StepIsNotInLookupPrefix("Foo", lookup);
        assertNull(step.walk(null, null).getValue());
        assertEquals("Something", step.walk(null, "Something").getValue());
        assertNull(step.walk(null, "FooFoo"));
    }

    @Test
    public void testStepLookupContains() {
        Step step = new StepLookupContains("Foo", lookup, "Default");
        assertEquals("Default", step.walk(null, null).getValue());
        assertEquals("Default", step.walk(null, "Something").getValue());
        assertEquals("FooFoo", step.walk(null, "Foo").getValue());
    }

    @Test
    public void testStepLookupContainsNoDefault() {
        Step step = new StepLookupContains("Foo", lookup, null);
        assertNull(step.walk(null, null));
        assertNull(step.walk(null, "Something"));
        assertEquals("FooFoo", step.walk(null, "Foo").getValue());
    }

    @Test
    public void testStepLookup() {
        Step step = new StepLookup("Foo", lookup, "Default");
        assertEquals("Default", step.walk(null, null).getValue());
        assertEquals("Default", step.walk(null, "Something").getValue());
        assertEquals("FooFoo", step.walk(null, "Foo").getValue());
    }

    @Test
    public void testStepLookupNoDefault() {
        Step step = new StepLookup("Foo", lookup, null);
        assertNull(step.walk(null, null));
        assertNull(step.walk(null, "Something"));
        assertEquals("FooFoo", step.walk(null, "Foo").getValue());
    }

    @Test
    public void testStepLookupPrefix() {
        Step step = new StepLookupPrefix("Foo", lookup, "Default");
        assertEquals("Default", step.walk(null, null).getValue());
        assertEquals("Default", step.walk(null, "Something").getValue());
        assertEquals("FooFoo", step.walk(null, "Foo").getValue());
    }

    @Test
    public void testStepLookupPrefixNoDefault() {
        Step step = new StepLookupPrefix("Foo", lookup, null);
        assertNull(step.walk(null, null));
        assertNull(step.walk(null, "Something"));
        assertEquals("FooFoo", step.walk(null, "Foo").getValue());
    }

    @Test
    public void testStepBackToFull() {
        Step step = new StepBackToFull();
        assertNull(step.walk(null, null).getValue());
        assertNull(step.walk(null, "Something").getValue());
    }

    @Test
    public void testStepCleanVersion() {
        Step step = new StepCleanVersion();
        assertNull(step.walk(null, null).getValue());
        assertEquals("Something", step.walk(null, "Something").getValue());
        assertEquals("1.2.3", step.walk(null, "1.2.3").getValue());
        assertEquals("1.2.3", step.walk(null, "1_2_3").getValue());
    }

    @Test
    public void testStepConcat() {
        Step step = new StepConcat(">>", "<<");
        assertNull(step.walk(null, null));
        assertEquals(">>Something<<", step.walk(null, "Something").getValue());
    }

    @Test
    public void testStepConcatPostfix() {
        Step step = new StepConcatPostfix("<<");
        assertNull(step.walk(null, null));
        assertEquals("Something<<", step.walk(null, "Something").getValue());
    }

    @Test
    public void testStepConcatPrefix() {
        Step step = new StepConcatPrefix(">>");
        assertNull(step.walk(null, null));
        assertEquals(">>Something", step.walk(null, "Something").getValue());
    }

    @Test
    public void testStepNormalizeBrand() {
        Step step = new StepNormalizeBrand();
        assertNull(step.walk(null, null));
        assertEquals("Something", step.walk(null, "something").getValue());
        assertEquals("NielsBasjes", step.walk(null, "NielsBasjes").getValue());
    }

    @Test
    public void testStepSegmentRange() {
        Step step = new StepSegmentRange(new Range(2, 3));
        assertNull(step.walk(null, null));
        assertEquals("Two|Tree", step.walk(null, "One|Two|Tree|Four|Five").getValue());
    }

    @Test
    public void testStepWordRange() {
        Step step = new StepWordRange(new Range(2, 3));
        assertNull(step.walk(null, null));
        assertEquals("Two Tree", step.walk(null, "One Two Tree Four Five").getValue());
    }

    @Test
    public void testStepDown() {
        Step step = new StepDown(new NumberRangeList(2, 3), "something");
        assertNull(step.walk(null, null));
    }

    @Test
    public void testStepNext() {
        Step step = new StepNext();
        assertNull(step.walk(null, null));
    }

    @Test
    public void testStepNextN() {
        Step step = new StepNextN(5);
        assertNull(step.walk(null, null));
    }

    @Test
    public void testStepPrev() {
        Step step = new StepPrev();
        assertNull(step.walk(null, null));
    }

    @Test
    public void testStepPrevN() {
        Step step = new StepPrevN(5);
        assertNull(step.walk(null, null));
    }

    @Test
    public void testStepUp() {
        Step step = new StepUp();
        assertNull(step.walk(null, null));
    }

}
