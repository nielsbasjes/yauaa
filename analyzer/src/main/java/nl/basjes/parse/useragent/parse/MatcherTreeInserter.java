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

import nl.basjes.collections.PrefixMap;
import nl.basjes.collections.prefixmap.ASCIIPrefixMap;
import nl.basjes.parse.useragent.analyze.Matcher;
import nl.basjes.parse.useragent.analyze.MatcherAction;
import nl.basjes.parse.useragent.analyze.WordRangeVisitor.Range;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.stepdown.UserAgentGetChildrenVisitor;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static java.util.Locale.ENGLISH;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.AGENT;

public class MatcherTreeInserter {

    private static final Logger LOG = LoggerFactory.getLogger(MatcherTreeInserter.class);

    /**
     * Add the provided Matcher into the provided tree for fast finding.
     */
    public static void insert(MatcherTree tree, Matcher matcher) {
        matcher.

    }


}
