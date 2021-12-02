/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2021 Niels Basjes
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

import nl.basjes.parse.useragent.AbstractUserAgentAnalyzerDirect;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.analyze.Analyzer;
import nl.basjes.parse.useragent.analyze.MatcherAction;
import nl.basjes.parse.useragent.analyze.MatcherTree;
import nl.basjes.parse.useragent.analyze.WordRangeVisitor;
import nl.basjes.parse.useragent.config.TestCase;
import nl.basjes.parse.useragent.parse.UserAgentTreeMatchMaker;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GetAllPathsAnalyzer implements Analyzer {
    private final List<String> values = new ArrayList<>(128);

    private final UserAgent result;

    public GetAllPathsAnalyzer(String useragent) {
        UserAgentTreeMatchMaker flattener = new UserAgentTreeMatchMaker(this);
        result = flattener.parse(useragent);
    }

    public List<String> getValues() {
        return values;
    }

    public UserAgent getResult() {
        return result;
    }

    public void inform(String path, String value, ParseTree<MatcherTree> ctx) {
        values.add(path);
        values.add(path + "=\"" + value + "\"");
        values.add(path + "{\"" + AbstractUserAgentAnalyzerDirect.firstCharactersForPrefixHash(value, AbstractUserAgentAnalyzerDirect.MAX_PREFIX_HASH_MATCH) + "\"");
    }

    public void informMeAbout(MatcherAction matcherAction, String keyPattern) {
        // Not needed to only get all paths
    }

    public void lookingForRange(String treeName, WordRangeVisitor.Range range) {
        // Not needed to only get all paths
    }

    public Set<WordRangeVisitor.Range> getRequiredInformRanges(String treeName) {
        // Not needed to only get all paths
        return Collections.emptySet();
    }

    @Override
    public void informMeAboutPrefix(MatcherAction matcherAction, String treeName, String prefix) {
        // Not needed to only get all paths
    }

    @Override
    public Set<Integer> getRequiredPrefixLengths(String treeName) {
        // Not needed to only get all paths
        return Collections.emptySet();
    }

    @Override
    public Map<String, Map<String, String>> getLookups() {
        // Not needed to only get all paths
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Set<String>> getLookupSets() {
        // Not needed to only get all paths
        return Collections.emptyMap();
    }

    @Override
    public List<TestCase> getTestCases() {
        return Collections.emptyList();
    }
}
