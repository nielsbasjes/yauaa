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
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestTreewalkerParsing {

    @Test
    public void validateWalkPathParsing() {
        String path = "IsNull[LookUp[TridentVersions;agent.(1)product.(2-4)comments.(*)product.name#1=\"Trident\"[2-3]~\"Foo\"^.(*)version%2{\"7.\";\"DefaultValue\"]]";

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

        String[] expectedWalkList = {
            "IsNull()",
            "DefaultValue(DefaultValue)",
            "WordRange(2-3)",
            "Contains(foo)",
            "Up()",
            "Down([1:5]version)",
            "FirstWords(2)",
            "StartsWith(7.)",
            "Lookup(TridentVersions)",
        };

        checkPath(path, expectedHashEntries, expectedWalkList);
    }

    @Test
    public void validateWalkPathParsingRange() {
        String path = "IsNull[LookUp[TridentVersions;agent.(1)product.(2-4)comments.(*)product.name[1]=\"Trident\"[2-3]~\"Foo\"^.(*)version[2]{\"7.\";\"DefaultValue\"]]";

        String[] expectedHashEntries = {
            "agent.(1)product.(2)comments.(1)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(2)comments.(2)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(2)comments.(3)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(2)comments.(4)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(2)comments.(5)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(2)comments.(6)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(2)comments.(7)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(2)comments.(8)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(2)comments.(9)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(2)comments.(10)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(3)comments.(1)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(3)comments.(2)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(3)comments.(3)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(3)comments.(4)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(3)comments.(5)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(3)comments.(6)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(3)comments.(7)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(3)comments.(8)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(3)comments.(9)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(3)comments.(10)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(4)comments.(1)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(4)comments.(2)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(4)comments.(3)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(4)comments.(4)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(4)comments.(5)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(4)comments.(6)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(4)comments.(7)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(4)comments.(8)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(4)comments.(9)product.(1)name[1]=\"Trident\"",
            "agent.(1)product.(4)comments.(10)product.(1)name[1]=\"Trident\"",
        };

        String[] expectedWalkList = {
            "IsNull()",
            "DefaultValue(DefaultValue)",
            "WordRange(2-3)",
            "Contains(foo)",
            "Up()",
            "Down([1:5]version)",
            "WordRange(2-2)",
            "StartsWith(7.)",
            "Lookup(TridentVersions)",
        };

        checkPath(path, expectedHashEntries, expectedWalkList);
    }

    @Test
    public void validateWalkPathSimpleName() {
        String path = "agent.(1)product.(1)name";

        String[] expectedHashEntries = {
            "agent.(1)product.(1)name",
        };

        String[] expectedWalkList = {
        };

        checkPath(path, expectedHashEntries, expectedWalkList);
    }

    @Test
    public void validateWalkPathSimpleNameEquals() {
        String path = "agent.(1)product.(1)name=\"Foo\"^.(1-3)version";

        String[] expectedHashEntries = {
            "agent.(1)product.(1)name=\"Foo\"",
        };

        String[] expectedWalkList = {
            "Up()",
            "Down([1:3]version)"
        };

        checkPath(path, expectedHashEntries, expectedWalkList);
    }

    @Test
    public void validateWalkPathNameSubstring() {
        String path = "agent.(1)product.(1)name[1-2]=\"Foo\"^.(1-3)version";

        String[] expectedHashEntries = {
            "agent.(1)product.(1)name[-2]=\"Foo\"",
        };

        String[] expectedWalkList = {
            "Up()",
            "Down([1:3]version)"
        };

        checkPath(path, expectedHashEntries, expectedWalkList);
    }

    @Test
    public void validateWalkPathNameSubstring2() {
        String path = "agent.(1)product.(1)name[3-5]=\"Foo\"^.(1-3)version";

        String[] expectedHashEntries = {
            "agent.(1)product.(1)name",
        };

        String[] expectedWalkList = {
            "WordRange(3-5)",
            "Equals(foo)",
            "Up()",
            "Down([1:3]version)"
        };

        checkPath(path, expectedHashEntries, expectedWalkList);
    }

    @Test
    public void validateWalkAroundTheWorld() {
        String path = "agent.(2-4)product.(1)comments.(5-6)entry.(1)text%2=\"seven\"^^^<.name=\"foo faa\"^.comments.entry.text%2=\"three\"@#1";

        String[] expectedHashEntries = {
            "agent.(2)product.(1)comments.(5)entry.(1)text%2=\"seven\"" ,
            "agent.(2)product.(1)comments.(6)entry.(1)text%2=\"seven\"" ,
            "agent.(3)product.(1)comments.(5)entry.(1)text%2=\"seven\"" ,
            "agent.(3)product.(1)comments.(6)entry.(1)text%2=\"seven\"" ,
            "agent.(4)product.(1)comments.(5)entry.(1)text%2=\"seven\"" ,
            "agent.(4)product.(1)comments.(6)entry.(1)text%2=\"seven\"" ,
        };

        String[] expectedWalkList = {
            "Up()",
            "Up()",
            "Up()",
            "Prev()",
            "Down([1:1]name)",
            "Equals(foo faa)",
            "Up()",
            "Down([1:2]comments)",
            "Down([1:20]entry)",
            "Down([1:8]text)",
            "FirstWords(2)",
            "Equals(three)",
            "BackToFull()",
            "FirstWords(1)",
        };

        checkPath(path, expectedHashEntries, expectedWalkList);
    }

    @Test
    public void validateWalkPathParsingCleanVersion() {

        String path = "CleanVersion[LookUp[TridentVersions;agent.(1)product.(2-4)comments.(*)product.name#1=\"Trident\"^.(*)version%2{\"7.\";\"DefaultValue\"]]";

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

        String[] expectedWalkList = {
            "DefaultValue(DefaultValue)",
            "Up()",
            "Down([1:5]version)",
            "FirstWords(2)",
            "StartsWith(7.)",
            "Lookup(TridentVersions)",
            "CleanVersion()"
        };

        checkPath(path, expectedHashEntries, expectedWalkList);
    }

    private void checkPath(String path, String[] expectedHashEntries, String[] expectedWalkList) {
        Map<String, Map<String, String>> lookups = new HashMap<>();
        lookups.put("TridentVersions", new HashMap<String, String>());

        TestMatcher matcher = new TestMatcher(null, lookups);
        MatcherRequireAction action = new MatcherRequireAction(path, matcher);

        StringBuilder sb = new StringBuilder("\n---------------------------\nActual list (").append(matcher.reveicedValues.size()).append(" entries):\n");
        for (String actual : matcher.reveicedValues) {
            sb.append(actual).append('\n');
        }
        sb.append("---------------------------\n");

        // Validate the expected hash entries (i.e. the first part of the path)
        for (String expect : expectedHashEntries) {
            assertTrue("\nMissing:\n" + expect + sb.toString(), matcher.reveicedValues.contains(expect));
        }
        assertTrue("Found wrong number of entries", expectedHashEntries.length == matcher.reveicedValues.size());

        // Validate the expected walk list entries (i.e. the dynamic part of the path)
        TreeExpressionEvaluator evaluator = action.getEvaluatorForUnitTesting();
        WalkList walkList = evaluator.getWalkListForUnitTesting();

        Step step = walkList.getFirstStep();
        for (String walkStep : expectedWalkList) {
            assertNotNull("Missing step:" + walkStep, step);
            assertEquals("Wrong step (" + step.toString() + " should be " + walkStep + ")", walkStep, step.toString());
            step = step.getNextStep();
        }
        assertNull(step);
    }

    private static class TestMatcher extends Matcher {
        final List<String> reveicedValues = new ArrayList<>(128);

        TestMatcher(Analyzer analyzer, Map<String, Map<String, String>> lookups) {
            super(analyzer, lookups);
        }

        @Override
        public void informMeAbout(MatcherAction matcherAction, String keyPattern) {
            reveicedValues.add(keyPattern);
        }

        @Override
        public void analyze(UserAgent userAgent) {
            // Do nothing
        }
    }
}
