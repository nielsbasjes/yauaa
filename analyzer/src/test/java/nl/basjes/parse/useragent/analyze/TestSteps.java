/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2022 Niels Basjes
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
import nl.basjes.parse.useragent.analyze.treewalker.steps.WalkList.WalkResult;
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
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepExtractBrandFromUrl;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepNormalizeBrand;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepReplaceString;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepSegmentRange;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepWordRange;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepDown;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepNext;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepNextN;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepPrev;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepPrevN;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepUp;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static nl.basjes.parse.useragent.parse.AgentPathFragment.BASE64;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestSteps {

    private static final Logger LOG = LogManager.getLogger(TestSteps.class);

    private static Map<String, String> lookup;
    private static Set<String>         set;

    private final ParseTree<MatcherTree> dummyTree = new ParserRuleContext(){
        @Override
        public String getText() {
            return "DuMmY";
        }
    };

    private final ParseTree nullTree = new ParserRuleContext(){
        @Override
        public String getText() {
            return null;
        }
    };


    private final Step returnNullStep = new Step() {
        @Override
        public WalkResult walk(@Nonnull ParseTree<MatcherTree> tree, String value) {
            return null;
        }
    };

    @BeforeAll
    public static void init() {
        lookup = new HashMap<>();
        lookup.put("foo", "FooFoo");
        lookup.put("bar", "BarBar");
        set = new HashSet<>(Arrays.asList("foo", "bar"));
    }

    @Test
    void testStepContains() {
        Step step = new StepContains("Foo");
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertNull(step.walk(dummyTree, null));
        assertNull(step.walk(dummyTree, "Something"));
        assertEquals("SomeFooBar", step.walk(dummyTree, "SomeFooBar").getValue());
    }

    @Test
    void testStepDefaultIfNull() {
        Step step = new StepDefaultIfNull("Foo");
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertEquals("Bar", step.walk(dummyTree, "Bar").getValue());

        step.setNextStep(1, returnNullStep);
        assertEquals("Foo", step.walk(dummyTree, "Bar").getValue());
    }

    @Test
    void testStepDefaultIfNullNoDefault() {
        assertThrows(InvalidParserConfigurationException.class, () ->
            new StepDefaultIfNull(null));
    }

    @Test
    void testStepEndsWith() {
        Step step = new StepEndsWith("Foo");
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertNull(step.walk(dummyTree, null));
        assertNull(step.walk(dummyTree, "Something"));
        assertEquals("SomethingFoo", step.walk(dummyTree, "SomethingFoo").getValue());
    }

    @Test
    void testStepEquals() {
        Step step = new StepEquals("Foo");
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertNull(step.walk(dummyTree, null));
        assertNull(step.walk(dummyTree, "Something"));
        assertEquals("Foo", step.walk(dummyTree, "Foo").getValue());
    }

    @Test
    void testStepIsInSet() {
        Step step = new StepIsInSet("MySet", set);
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertNull(step.walk(dummyTree, null));
        assertNull(step.walk(dummyTree, "Something"));
        assertEquals("Foo", step.walk(dummyTree, "Foo").getValue());
    }

    @Test
    void testStepIsNotInSet() {
        Step step = new StepIsNotInSet("MySet", set);
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertEquals("DuMmY", step.walk(dummyTree, null).getValue());
        assertEquals("Something", step.walk(dummyTree, "Something").getValue());
        assertNull(step.walk(dummyTree, "Foo"));
    }

    @Test
    void testStepIsNull() {
        Step step = new StepIsNull();
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertNull(step.walk(dummyTree, "Something"));
        assertNull(step.walk(dummyTree, null));
        assertNull(step.walk(nullTree, "Something"));
        assertNotNull(step.walk(nullTree, null));
    }

    @Test
    void testStepNotEquals() {
        Step step = new StepNotEquals("Foo");
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertEquals("DuMmY", step.walk(dummyTree, null).getValue());
        assertNull(step.walk(dummyTree, "Foo"));
        assertEquals("Bar", step.walk(dummyTree, "Bar").getValue());
    }


    @Test
    void testStepStartsWith() {
        Step step = new StepStartsWith("Foo");
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertNull(step.walk(dummyTree, null));
        assertNull(step.walk(dummyTree, "Something"));
        assertEquals("FooFoo", step.walk(dummyTree, "FooFoo").getValue());
    }


    @Test
    void testStepIsInLookupContains() {
        Step step = new StepIsInLookupContains("Foo", lookup);
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertNull(step.walk(dummyTree, null));
        assertNull(step.walk(dummyTree, "Something"));
        assertEquals("FooFoo", step.walk(dummyTree, "FooFoo").getValue());
    }

    @Test
    void testStepIsInLookupPrefix() {
        Step step = new StepIsInLookupPrefix("Foo", lookup);
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertNull(step.walk(dummyTree, null));
        assertNull(step.walk(dummyTree, "Something"));
        assertEquals("FooFoo", step.walk(dummyTree, "FooFoo").getValue());
    }

    @Test
    void testStepIsNotInLookupPrefix() {
        Step step = new StepIsNotInLookupPrefix("Foo", lookup);
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertEquals("DuMmY", step.walk(dummyTree, null).getValue());
        assertEquals("Something", step.walk(dummyTree, "Something").getValue());
        assertNull(step.walk(dummyTree, "FooFoo"));
    }

    @Test
    void testStepLookupContains() {
        Step step = new StepLookupContains("Foo", lookup, "Default");
        assertEquals("Default", step.walk(dummyTree, null).getValue());
        assertEquals("Default", step.walk(dummyTree, "Something").getValue());
        assertEquals("FooFoo", step.walk(dummyTree, "Foo").getValue());
    }

    @Test
    void testStepLookupContainsNoDefault() {
        Step step = new StepLookupContains("Foo", lookup, null);
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertNull(step.walk(dummyTree, null));
        assertNull(step.walk(dummyTree, "Something"));
        assertEquals("FooFoo", step.walk(dummyTree, "Foo").getValue());
    }

    @Test
    void testStepLookup() {
        Step step = new StepLookup("Foo", lookup, "Default");
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertEquals("Default", step.walk(dummyTree, null).getValue());
        assertEquals("Default", step.walk(dummyTree, "Something").getValue());
        assertEquals("FooFoo", step.walk(dummyTree, "Foo").getValue());
    }

    @Test
    void testStepLookupNoDefault() {
        Step step = new StepLookup("Foo", lookup, null);
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertNull(step.walk(dummyTree, null));
        assertNull(step.walk(dummyTree, "Something"));
        assertEquals("FooFoo", step.walk(dummyTree, "Foo").getValue());
    }

    @Test
    void testStepLookupPrefix() {
        Step step = new StepLookupPrefix("Foo", lookup, "Default");
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertEquals("Default", step.walk(dummyTree, null).getValue());
        assertEquals("Default", step.walk(dummyTree, "Something").getValue());
        assertEquals("FooFoo", step.walk(dummyTree, "Foo").getValue());
    }

    @Test
    void testStepLookupPrefixNoDefault() {
        Step step = new StepLookupPrefix("Foo", lookup, null);
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertNull(step.walk(dummyTree, null));
        assertNull(step.walk(dummyTree, "Something"));
        assertEquals("FooFoo", step.walk(dummyTree, "Foo").getValue());
    }

    @Test
    void testStepBackToFull() {
        Step step = new StepBackToFull();
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertEquals("DuMmY", step.walk(dummyTree, null).getValue());
        assertEquals("DuMmY", step.walk(dummyTree, "Something").getValue());
    }

    @Test
    void testStepCleanVersion() {
        Step step = new StepCleanVersion();
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertEquals("DuMmY", step.walk(dummyTree, null).getValue());
        assertEquals("Something", step.walk(dummyTree, "Something").getValue());
        assertEquals("1.2.3", step.walk(dummyTree, "1.2.3").getValue());
        assertEquals("1.2.3", step.walk(dummyTree, "1_2_3").getValue());
    }


    @Test
    void testStepReplaceString() {
        Step step = new StepReplaceString("foo", "bar");
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertEquals("DuMmY", step.walk(dummyTree, null).getValue());
        assertEquals("Something", step.walk(dummyTree, "Something").getValue());
        assertEquals("barbar1bar2", step.walk(dummyTree, "foofoo1bar2").getValue());
        assertEquals("1bar2bar3bar4", step.walk(dummyTree, "1foo2bar3foo4").getValue());
    }

    @Test
    void testStepConcat() {
        Step step = new StepConcat(">>", "<<");
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertEquals(">>DuMmY<<", step.walk(dummyTree, null).getValue());
        assertEquals(">>Something<<", step.walk(dummyTree, "Something").getValue());
    }

    @Test
    void testStepConcatPostfix() {
        Step step = new StepConcatPostfix("<<");
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertEquals("DuMmY<<", step.walk(dummyTree, null).getValue());
        assertEquals("Something<<", step.walk(dummyTree, "Something").getValue());
    }

    @Test
    void testStepConcatPrefix() {
        Step step = new StepConcatPrefix(">>");
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertEquals(">>DuMmY", step.walk(dummyTree, null).getValue());
        assertEquals(">>Something", step.walk(dummyTree, "Something").getValue());
    }

    @Test
    void testStepNormalizeBrand() {
        Step step = new StepNormalizeBrand();
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertEquals("Dummy", step.walk(dummyTree, null).getValue());
        assertEquals("Something", step.walk(dummyTree, "something").getValue());
        assertEquals("NielsBasjes", step.walk(dummyTree, "NielsBasjes").getValue());
    }

    @Test
    void testExtractBrandFromUrl() {
        Step step = new StepExtractBrandFromUrl();
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);

        ParseTree tree = new ParserRuleContext(){
            @Override
            public String getText() {
                return "https://yauaa.basjes.nl/";
            }
        };

        assertEquals("Basjes", step.walk(tree, null).getValue());
    }

    @Test
    void testStepSegmentRange() {
        Step step = new StepSegmentRange(new Range(2, 3));
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertNull(step.walk(dummyTree, null));
        assertEquals("Two|Tree", step.walk(dummyTree, "One|Two|Tree|Four|Five").getValue());
    }

    @Test
    void testStepWordRange() {
        Step step = new StepWordRange(new Range(2, 3));
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertNull(step.walk(dummyTree, null));
        assertEquals("Two Tree", step.walk(dummyTree, "One Two Tree Four Five").getValue());
    }

    @Test
    void testStepDown() {
        Step step = new StepDown(new NumberRangeList(2, 3), BASE64);
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertNull(step.walk(dummyTree, null));
    }

    @Test
    void testStepNext() {
        Step step = new StepNext();
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertNull(step.walk(dummyTree, null));
    }

    @Test
    void testStepNextN() {
        Step step = new StepNextN(5);
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertNull(step.walk(dummyTree, null));
    }

    @Test
    void testStepPrev() {
        Step step = new StepPrev();
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertNull(step.walk(dummyTree, null));
    }

    @Test
    void testStepPrevN() {
        Step step = new StepPrevN(5);
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertNull(step.walk(dummyTree, null));
    }

    @Test
    void testStepUp() {
        Step step = new StepUp();
        LOG.info("Step {} --> {}", step.getClass().getSimpleName(), step);
        assertNull(step.walk(dummyTree, null));
    }

}
