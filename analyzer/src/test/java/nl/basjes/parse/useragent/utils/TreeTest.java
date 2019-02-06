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

package nl.basjes.parse.useragent.utils;

import nl.basjes.parse.useragent.analyze.MatcherAction;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class TreeTest {

    private static final Logger LOG = LoggerFactory.getLogger(TreeTest.class);

    // Goal:
    // - Limit the backtracking over the parsed tree to ONLY the parts where matchers exist.
    // - Reduce the string concatenation impact by using 'fixed' enums.

    // MatcherTree = tree with tokens, positions, list of 'toInform' Matchers



    public enum NodeTypes {

    }

    public static class MatcherTree {
        int maxFilledNode = 0; // To avoid trying all nodes
        MatcherTree[] nodes; // Elements may be null
        Set<MatcherAction> toInform = new HashSet<>(); // Empty and we can add fields

        // Where to put the TYPE of node? product/version/text/...

    }


    @Test
    public void tryIt(){

    }


}
