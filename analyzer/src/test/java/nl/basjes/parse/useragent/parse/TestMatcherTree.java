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

package nl.basjes.parse.useragent.parse;

import nl.basjes.parse.useragent.UserAgentAnalyzerDirect;
import nl.basjes.parse.useragent.analyze.Matcher;
import nl.basjes.parse.useragent.analyze.MatcherAction;
import nl.basjes.parse.useragent.analyze.MatcherRequireAction;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static nl.basjes.parse.useragent.parse.AgentPathFragment.AGENT;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.COMMENTS;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.ENTRY;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.PRODUCT;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.VERSION;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestMatcherTree {

    private static final Logger LOG = LoggerFactory.getLogger(TestMatcherTree.class);

    @Test
    public void developFakeTest() {

        UserAgentAnalyzerDirect userAgentAnalyzerDirect = UserAgentAnalyzerDirect.newBuilder().build();
        Matcher                 matcher                 = new Matcher(userAgentAnalyzerDirect);
        MatcherAction           action                  = new MatcherRequireAction("agent.product.name", matcher);

        MatcherTree root = new MatcherTree(AGENT, 1);

        MatcherTree child1 = root
            .getOrCreateChild(PRODUCT, 1)
            .getOrCreateChild(COMMENTS, 2)
            .getOrCreateChild(ENTRY, 3)
            .getOrCreateChild(PRODUCT, 4)
            .getOrCreateChild(VERSION, 5);

        MatcherTree child2 = root
            .getOrCreateChild(PRODUCT, 1)
            .getOrCreateChild(COMMENTS, 2)
            .getOrCreateChild(ENTRY, 2);

        child2
            .getOrCreateChild(PRODUCT, 1)
            .getOrCreateChild(VERSION, 1);

        child2
            .getOrCreateChild(PRODUCT, 3)
            .getOrCreateChild(VERSION, 2);

//        root
//            .getOrCreateChild(PRODUCT, 2)
//            .getOrCreateChild(NAME, 1)
//            .getOrCreateChild(STARTSWITH, 0)
//            .makeItStartsWith("ItStartsWithThis");

//        root
//            .getOrCreateChild(PRODUCT, 2)
//            .getOrCreateChild(NAME, 1)
//            .getOrCreateChild(EQUALS, 0)
//            .makeItEquals("IsEqualTo");

//        LOG.info("1: {}", child1.toString());
//        LOG.info("2: {}", child2.toString());

        root.getChildrenStrings().forEach(s -> {
            LOG.info("==> {}", s);
        });

        root.verifyTree();
    }


//    @Test
//    public void testBasicTreeConstruction() {
//
//        UserAgentAnalyzerDirect userAgentAnalyzerDirect = UserAgentAnalyzerDirect.newBuilder().build();
//        Matcher         matcher = new Matcher(userAgentAnalyzerDirect);
//        MatcherAction   action  = new MatcherRequireAction("agent.product.name", matcher);
//
//        List<String> expected = new ArrayList<>();
//
//        MatcherTree root = new MatcherTree(AGENT, 1);
//
//        root
//            .getOrCreateChild(PRODUCT, 1)
//            .getOrCreateChild(COMMENTS, 2)
//            .getOrCreateChild(ENTRY, 10);
//        expected.add("agent.(1)product.(2)comments.(10)entry");
//
//        root
//            .getOrCreateChild(PRODUCT, 1)
//            .getOrCreateChild(COMMENTS, 2)
//            .getOrCreateChild(ENTRY, 5);
//        expected.add("agent.(1)product.(2)comments.(5)entry");
//
//        root
//            .getOrCreateChild(PRODUCT, 1)
//            .getOrCreateChild(COMMENTS, 2)
//            .getOrCreateChild(ENTRY, 1);
//        expected.add("agent.(1)product.(2)comments.(1)entry");
//
//        root
//            .getOrCreateChild(PRODUCT, 2)
//            .getOrCreateChild(NAME, 1)
//            .getOrCreateChild(STARTSWITH, 0)
//            .makeItStartsWith("ItStartsWithThis");
//        expected.add("agent.(2)product.(1)name{\"ItStartsWithThis\"");
//
//
//        root
//            .getOrCreateChild(PRODUCT, 2)
//            .getOrCreateChild(NAME, 1)
//            .getOrCreateChild(EQUALS, 0)
//            .makeItEquals("IsEqualTo");
//        expected.add("agent.(2)product.(1)name=\"IsEqualTo\"");
//
//        root
//            .getOrCreateChild(PRODUCT, 2)
//            .getOrCreateChild(NAME, 1)
//            .getOrCreateChild(EQUALS, 0)
//            .addMatcherAction(new MatcherExtractAction("foo", 42, "\"Something\"", new Matcher(null)));
//        root
//            .getOrCreateChild(PRODUCT, 2)
//            .addMatcherAction(new MatcherExtractAction("foo", 42, "\"Something\"", new Matcher(null)));
//        root
//            .getOrCreateChild(PRODUCT, 2)
//            .getOrCreateChild(NAME, 1)
//            .addMatcherAction(new MatcherExtractAction("foo", 42, "\"Something\"", new Matcher(null)));
//        root
//            .getOrCreateChild(PRODUCT, 2)
//            .getOrCreateChild(NAME, 1)
//            .getOrCreateChild(EQUALS, 0)
//            .addMatcherAction(new MatcherExtractAction("foo", 42, "\"Something\"", new Matcher(null)));
//
//        root
//            .getOrCreateChild(PRODUCT, 2)
//            .getOrCreateChild(NAME, 1)
//            .getOrCreateChild(EQUALS, 0)
//            .makeItEquals("IsEqualTo");
//        expected.add("agent.(2)product.(1)name=\"IsEqualTo\"");
//
//        assertTrue(root.verifyTree());
//
//        List<String> result = root.getChildrenStrings();
//
//        result.forEach(s -> {
//            LOG.info("==> {}", s);
//        });
//
//        expected.forEach(e -> assertTrue("Missing " + e, result.contains(e)));
//    }


    @Test
    public void testMultiSubstringConstruction() {

        UserAgentAnalyzerDirect userAgentAnalyzerDirect = UserAgentAnalyzerDirect.newBuilder().build();
        Matcher                 matcher                 = new Matcher(userAgentAnalyzerDirect);
        MatcherAction           action                  = new MatcherRequireAction("agent.product.name", matcher);

        List<String> expected = new ArrayList<>();

        MatcherTree root = new MatcherTree(AGENT, 1);

        root
            .getOrCreateChild(PRODUCT, 1)
            .getOrCreateChild(COMMENTS, 2)
            .getOrCreateChild(ENTRY, 10);
        expected.add("agent.(1)product.(2)comments.(10)entry");


//
//        root
//            .getOrCreateChild(PRODUCT, 2)
//            .getOrCreateChild(NAME, 1)
//            .getOrCreateChildEquals(EQUALS, "One")
//        expected.add("agent.(2)product.(1)name=\"One\"");
//
//        root
//            .getOrCreateChild(PRODUCT, 2)
//            .getOrCreateChild(NAME, 1)
//            .getOrCreateChildEquals(EQUALS, "Two");
//        expected.add("agent.(2)product.(1)name=\"Two\"");



//        root
//            .getOrCreateChild(PRODUCT, 2)
//            .getOrCreateChild(NAME, 1)
//            .getOrCreateChild(STARTSWITH, 0)
//            .makeItStartsWith("One");
//        expected.add("agent.(2)product.(1)name{\"One\"");
//
//        root
//            .getOrCreateChild(PRODUCT, 2)
//            .getOrCreateChild(NAME, 1)
//            .getOrCreateChild(STARTSWITH, 0)
//            .makeItStartsWith("Two");
//        expected.add("agent.(2)product.(1)name{\"Two\"");



        assertTrue(root.verifyTree());

        List<String> result = root.getChildrenStrings();

        result.forEach(s -> {
            LOG.info("==> {}", s);
        });

        expected.forEach(e -> assertTrue(result.contains(e), "Missing " + e));
    }

}
