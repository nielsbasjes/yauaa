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
import nl.basjes.parse.useragent.config.AnalyzerConfig;
import nl.basjes.parse.useragent.config.AnalyzerConfigHolder;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

public interface MatchMaker extends AnalyzerConfigHolder {
    void inform(String path, String value, ParseTree ctx);

    void informMeAbout(MatcherAction matcherAction, String keyPattern);

    void lookingForRange(String treeName, Range range);

    Set<Range> getRequiredInformRanges(String treeName);

    void informMeAboutPrefix(MatcherAction matcherAction, String treeName, String prefix);

    Set<Integer> getRequiredPrefixLengths(String treeName);

    default void receivedInput(Matcher matcher) {
        // Nothing to do
    }

    class Dummy implements MatchMaker {
        @Override
        public void inform(String path, String value, ParseTree ctx) {
            // Nothing, this class should never be actually called.
        }

        @Override
        public void informMeAbout(MatcherAction matcherAction, String keyPattern) {
            // Nothing, this class should never be actually called.
        }

        @Override
        public void lookingForRange(String treeName, Range range) {
            // Nothing, this class should never be actually called.
        }

        @Override
        public Set<Range> getRequiredInformRanges(String treeName) {
            // Nothing, this class should never be actually called.
            return Collections.emptySet();
        }

        @Override
        public void informMeAboutPrefix(MatcherAction matcherAction, String treeName, String prefix) {
            // Nothing, this class should never be actually called.
        }

        @Override
        public Set<Integer> getRequiredPrefixLengths(String treeName) {
            // Nothing, this class should never be actually called.
            return Collections.emptySet();
        }

        @Nonnull
        @Override
        public AnalyzerConfig getConfig() {
            // Nothing, this class should never be actually called.
            return AnalyzerConfig.newBuilder().build();
        }
    }

}
