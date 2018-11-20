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

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.analyze.treewalker.TreeExpressionEvaluator;
import nl.basjes.parse.useragent.analyze.treewalker.steps.Step;
import nl.basjes.parse.useragent.analyze.treewalker.steps.WalkList;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static nl.basjes.parse.useragent.UserAgentAnalyzerDirect.MAX_PREFIX_HASH_MATCH;
import static nl.basjes.parse.useragent.UserAgentAnalyzerDirect.firstCharactersForPrefixHash;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestTreewalkerExtract {

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
    public void validateWalkPathSimpleNameOpenStartRange() {
        String path = "agent.(-3)product.(1)name";

        String[] expectedHashEntries = {
            "agent.(1)product.(1)name",
            "agent.(2)product.(1)name",
            "agent.(3)product.(1)name",
        };

        String[] expectedWalkList = {
        };

        checkPath(path, expectedHashEntries, expectedWalkList);
    }

    @Test
    public void validateWalkPathSimpleNameOpenEndRange() {
        String path = "agent.(5-)product.(1)name";

        String[] expectedHashEntries = {
            "agent.(5)product.(1)name",
            "agent.(6)product.(1)name",
            "agent.(7)product.(1)name",
            "agent.(8)product.(1)name",
            "agent.(9)product.(1)name",
            "agent.(10)product.(1)name",
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
            "agent.(1)product.(1)name[1-2]=\"Foo\"",
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
            "agent.(1)product.(1)name[3-5]=\"Foo\"",
        };

        String[] expectedWalkList = {
            "Up()",
            "Down([1:3]version)"
        };

        checkPath(path, expectedHashEntries, expectedWalkList);
    }

    @Test
    public void validateWalkAroundTheWorld() {
        String path = "agent.(2-4)product.(1)comments.(5-6)entry.(1)text[2]=\"seven\"^^^<.name=\"foo faa\"^.comments.entry.text[-2]=\"three\"@[1-2]";

        String[] expectedHashEntries = {
            "agent.(2)product.(1)comments.(5)entry.(1)text[2-2]=\"seven\"",
            "agent.(2)product.(1)comments.(6)entry.(1)text[2-2]=\"seven\"",
            "agent.(3)product.(1)comments.(5)entry.(1)text[2-2]=\"seven\"",
            "agent.(3)product.(1)comments.(6)entry.(1)text[2-2]=\"seven\"",
            "agent.(4)product.(1)comments.(5)entry.(1)text[2-2]=\"seven\"",
            "agent.(4)product.(1)comments.(6)entry.(1)text[2-2]=\"seven\"",
        };

        String[] expectedWalkList = {
            "Up()",
            "Up()",
            "Up()",
            "Prev(1)",
            "Down([1:1]name)",
            "Equals(foo faa)",
            "Up()",
            "Down([1:2]comments)",
            "Down([1:20]entry)",
            "Down([1:8]text)",
            "WordRange([1:2])",
            "Equals(three)",
            "BackToFull()",
            "WordRange([1:2])",
        };

        checkPath(path, expectedHashEntries, expectedWalkList);
    }

    @Test
    public void validateWalkPathParsingCleanVersion() {

        String path = "CleanVersion[LookUp[TridentVersions;agent.(1)product.(2-4)comments.(*)product.name[1-1]=\"Trident\"" +
            "^.(*)version[-2]{\"7.\";\"DefaultValue\"]]";

        String[] expectedHashEntries = {
            "agent.(1)product.(2)comments.(1)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(2)comments.(2)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(2)comments.(3)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(2)comments.(4)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(2)comments.(5)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(2)comments.(6)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(2)comments.(7)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(2)comments.(8)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(2)comments.(9)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(2)comments.(10)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(3)comments.(1)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(3)comments.(2)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(3)comments.(3)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(3)comments.(4)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(3)comments.(5)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(3)comments.(6)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(3)comments.(7)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(3)comments.(8)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(3)comments.(9)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(3)comments.(10)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(4)comments.(1)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(4)comments.(2)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(4)comments.(3)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(4)comments.(4)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(4)comments.(5)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(4)comments.(6)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(4)comments.(7)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(4)comments.(8)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(4)comments.(9)product.(1)name[1-1]=\"Trident\"",
            "agent.(1)product.(4)comments.(10)product.(1)name[1-1]=\"Trident\"",
        };

        String[] expectedWalkList = {
            "Up()",
            "Down([1:5]version)",
            "WordRange([1:2])",
            "StartsWith(7.)",
            "Lookup(@TridentVersions ; default=DefaultValue)",
            "CleanVersion()"
        };

        checkPath(path, expectedHashEntries, expectedWalkList);
    }

    @Test(expected = InvalidParserConfigurationException.class)
    public void validateParseError() {
        String path = "agent.product.(1)name[3]\"Safari\"";

        TestMatcher matcher = new TestMatcher(new HashMap<>(), new HashMap<>());
        MatcherExtractAction action = new MatcherExtractAction("Dummy", 42, path, matcher);
        action.initialize();
    }

    private void checkPath(String path, String[] expectedHashEntries, String[] expectedWalkList) {
        Map<String, Map<String, String>> lookups = new HashMap<>();
        lookups.put("TridentVersions", new HashMap<>());

        TestMatcher matcher = new TestMatcher(lookups, new HashMap<>());
        MatcherExtractAction action = new MatcherExtractAction("Dummy", 42, path, matcher);
        action.initialize();

        StringBuilder sb = new StringBuilder("\n---------------------------\nActual list (")
            .append(matcher.reveicedValues.size())
            .append(" entries):\n");

        for (String actual : matcher.reveicedValues) {
            sb.append(actual).append('\n');
        }
        sb.append("---------------------------\n");

        // Validate the expected hash entries (i.e. the first part of the path)
        for (String expect : expectedHashEntries) {
            assertTrue("\nMissing:\n" + expect + sb.toString(), matcher.reveicedValues.contains(expect));
        }
        assertEquals("Found wrong number of entries", expectedHashEntries.length, matcher.reveicedValues.size());

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

        TestMatcher(Map<String, Map<String, String>> lookups, Map<String, Set<String>> lookupSets) {
            super(null, lookups, lookupSets);
        }

        @Override
        public void informMeAbout(MatcherAction matcherAction, String keyPattern) {
            reveicedValues.add(keyPattern);
        }

        @Override
        public void informMeAboutPrefix(MatcherAction matcherAction, String treeName, String prefix) {
            informMeAbout(matcherAction, treeName + "{\"" + firstCharactersForPrefixHash(prefix, MAX_PREFIX_HASH_MATCH) + "\"");
        }

        @Override
        public void analyze(UserAgent userAgent) {
            // Do nothing
        }

        @Override
        public void lookingForRange(String treeName, WordRangeVisitor.Range range) {
            // Do nothing
        }
    }
}
