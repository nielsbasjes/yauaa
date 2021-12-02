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

package nl.basjes.parse.useragent.debug;

import nl.basjes.parse.useragent.analyze.Analyzer;
import nl.basjes.parse.useragent.analyze.MatcherAction;
import nl.basjes.parse.useragent.analyze.MatcherTree;
import nl.basjes.parse.useragent.analyze.WordRangeVisitor;
import nl.basjes.parse.useragent.config.TestCase;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FlattenPrinter implements Analyzer {

    final transient PrintStream outputStream;

    public FlattenPrinter(PrintStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void inform(String path, String value, ParseTree<MatcherTree> ctx) {
        outputStream.println(path);
    }

    @Override
    public void informMeAbout(MatcherAction matcherAction, String keyPattern) {
        // Not needed
    }

    public void lookingForRange(String treeName, WordRangeVisitor.Range range) {
        // Never called
    }

    public Set<WordRangeVisitor.Range> getRequiredInformRanges(String treeName){
        // Never called
        return Collections.emptySet();
    }

    public void informMeAboutPrefix(MatcherAction matcherAction, String treeName, String prefix) {
        // Never called
    }

    public Set<Integer> getRequiredPrefixLengths(String treeName){
        // Never called
        return Collections.emptySet();
    }

    @Override
    public Map<String, Map<String, String>> getLookups() {
        // Never called
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Set<String>> getLookupSets() {
        // Never called
        return Collections.emptyMap();
    }

    @Override
    public List<TestCase> getTestCases() {
        // Never called
        return Collections.emptyList();
    }

}
