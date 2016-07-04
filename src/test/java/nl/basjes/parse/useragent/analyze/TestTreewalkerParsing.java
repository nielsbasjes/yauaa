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

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.analyze.treewalker.TreeExpressionEvaluator;
import nl.basjes.parse.useragent.analyze.treewalker.steps.Step;
import nl.basjes.parse.useragent.analyze.treewalker.steps.WalkList;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepCleanVersion;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepIsNull;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepStartsWith;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepDefaultValue;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepLookup;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepSingleWord;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepDown;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepUp;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class TestTreewalkerParsing {


    @Test
    public void validateWalkPathParsing() {
        String path = "IsNull[LookUp[TridentVersions;agent.(1)product.([2-4])comments.(*)product.name#1=\"Trident\"^.(*)version%2{\"7.\";\"DefaultValue\"]]";


        Map<String, Map<String, String>> lookups = new HashMap<>();
        lookups.put("TridentVersions", new HashMap<String, String>());

        TestMatcher matcher =  new TestMatcher(null,lookups);
        MatcherRequireAction action = new MatcherRequireAction(path, matcher);

        // Validate the expected walk list entries (i.e. the dynamic part of the path)

        TreeExpressionEvaluator evaluator = action.getEvaluatorForUnitTesting();
        WalkList walkList = evaluator.getWalkListForUnitTesting();

//                String path = "TridentVersions[agent.(1)product.([2-4])comments.(*)product.name#1=\"Trident\"^.(*)version%2{\"7.\";\"DefaultValue\"]";

        Step step = walkList.getFirstStep();
        assertTrue(step instanceof StepIsNull);
        assertEquals("IsNull()" , step.toString());
        step = step.getNextStep();
        assertTrue(step instanceof StepDefaultValue);
        assertEquals("DefaultValue(DefaultValue)" , step.toString());
        step = step.getNextStep();
        assertTrue(step instanceof StepUp);
        assertEquals("Up()"                       , step.toString());
        step = step.getNextStep();
        assertTrue(step instanceof StepDown);
        assertEquals("Down([1:5]version)"         , step.toString());
        step = step.getNextStep();
        assertTrue(step instanceof StepSingleWord);
        assertEquals("FirstWords(2)"              , step.toString());
        step = step.getNextStep();
        assertTrue(step instanceof StepStartsWith);
        assertEquals("StartsWith(7.)"             , step.toString());
        step = step.getNextStep();
        assertTrue(step instanceof StepLookup);
        assertEquals("Lookup(TridentVersions)"    , step.toString());
        step = step.getNextStep();
        assertNull(step);

        // Validate the expected hash entries (i.e. the first part of the path)

        String[] expectedHashEntries = {
                "agent.(1)product.(2)comments.(1)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(2)comments.(2)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(2)comments.(3)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(2)comments.(4)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(2)comments.(5)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(2)comments.(6)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(2)comments.(7)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(2)comments.(8)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(2)comments.(9)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(2)comments.(10)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(3)comments.(1)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(3)comments.(2)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(3)comments.(3)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(3)comments.(4)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(3)comments.(5)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(3)comments.(6)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(3)comments.(7)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(3)comments.(8)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(3)comments.(9)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(3)comments.(10)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(4)comments.(1)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(4)comments.(2)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(4)comments.(3)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(4)comments.(4)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(4)comments.(5)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(4)comments.(6)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(4)comments.(7)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(4)comments.(8)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(4)comments.(9)product.(1)name#1=\"Trident\"",
                "agent.(1)product.(4)comments.(10)product.(1)name#1=\"Trident\"",
        };

        for (String expect: expectedHashEntries){
            assertTrue("Missing:" + expect, matcher.reveicedValues.contains(expect));
        }

    }

    @Test
    public void validateWalkPathParsingCleanVersion() {
        String path = "CleanVersion[LookUp[TridentVersions;agent.(1)product.([2-4])comments.(*)product.name#1=\"Trident\"^.(*)version%2{\"7.\";\"DefaultValue\"]]";


        Map<String, Map<String, String>> lookups = new HashMap<>();
        lookups.put("TridentVersions", new HashMap<String, String>());

        TestMatcher matcher =  new TestMatcher(null,lookups);
        MatcherRequireAction action = new MatcherRequireAction(path, matcher);

        // Validate the expected walk list entries (i.e. the dynamic part of the path)

        TreeExpressionEvaluator evaluator = action.getEvaluatorForUnitTesting();
        WalkList walkList = evaluator.getWalkListForUnitTesting();

        Step step = walkList.getFirstStep();
        assertTrue(step instanceof StepDefaultValue);
        assertEquals("DefaultValue(DefaultValue)" , step.toString());
        step = step.getNextStep();
        assertTrue(step instanceof StepUp);
        assertEquals("Up()"                       , step.toString());
        step = step.getNextStep();
        assertTrue(step instanceof StepDown);
        assertEquals("Down([1:5]version)"         , step.toString());
        step = step.getNextStep();
        assertTrue(step instanceof StepSingleWord);
        assertEquals("FirstWords(2)"              , step.toString());
        step = step.getNextStep();
        assertTrue(step instanceof StepStartsWith);
        assertEquals("StartsWith(7.)"             , step.toString());
        step = step.getNextStep();
        assertTrue(step instanceof StepLookup);
        assertEquals("Lookup(TridentVersions)"    , step.toString());
        step = step.getNextStep();
        assertTrue(step instanceof StepCleanVersion);
        assertEquals("CleanVersion()" , step.toString());
        step = step.getNextStep();
        assertNull(step);

        // Validate the expected hash entries (i.e. the first part of the path)

        String[] expectedHashEntries = {
            "agent.(1)product.(2)comments.(1)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(2)comments.(2)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(2)comments.(3)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(2)comments.(4)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(2)comments.(5)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(2)comments.(6)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(2)comments.(7)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(2)comments.(8)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(2)comments.(9)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(2)comments.(10)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(3)comments.(1)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(3)comments.(2)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(3)comments.(3)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(3)comments.(4)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(3)comments.(5)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(3)comments.(6)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(3)comments.(7)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(3)comments.(8)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(3)comments.(9)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(3)comments.(10)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(4)comments.(1)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(4)comments.(2)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(4)comments.(3)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(4)comments.(4)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(4)comments.(5)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(4)comments.(6)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(4)comments.(7)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(4)comments.(8)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(4)comments.(9)product.(1)name#1=\"Trident\"",
            "agent.(1)product.(4)comments.(10)product.(1)name#1=\"Trident\"",
        };

        for (String expect: expectedHashEntries){
            assertTrue("Missing:" + expect, matcher.reveicedValues.contains(expect));
        }

    }


    private static class TestMatcher extends Matcher {
        private static final Logger LOG = LoggerFactory.getLogger(TestMatcher.class);

        List<String> reveicedValues = new ArrayList<>(128);

        TestMatcher(Analyzer analyzer, Map<String, Map<String, String>> lookups) {
            super(analyzer, lookups);
        }

        @Override
        public void informMeAbout(MatcherAction matcherAction, String keyPattern) {
            reveicedValues.add(keyPattern);
        }

        @Override
        public void analyze(UserAgent userAgent) {
            return; // Don't
        }

        @Override
        public boolean getVerbose() {
            return super.getVerbose();
        }

        @Override
        public void reset(boolean setVerboseTemporarily) {
            super.reset(setVerboseTemporarily);
        }
    }
 }
