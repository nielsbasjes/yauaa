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

import nl.basjes.parse.useragent.analyze.treewalker.steps.WalkList.WalkResult;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MatcherRequireAction extends MatcherAction {
    private static final Logger LOG = LogManager.getLogger(MatcherRequireAction.class);

    @SuppressWarnings("unused") // Private constructor for serialization systems ONLY (like Kryo)
    protected MatcherRequireAction() {
    }

    public MatcherRequireAction(String config, Matcher matcher) {
        init(config, matcher);
    }

    protected ParserRuleContext<MatcherTree> parseWalkerExpression(UserAgentTreeWalkerParser<MatcherTree> parser) {
        return parser.matcherRequire();
    }

    @Override
    public long initialize() {
        long newEntries = super.initialize();
        newEntries -= evaluator.pruneTrailingStepsThatCannotFail();
        return newEntries;
    }

    protected void setFixedValue(String fixedValue) {
        throw new InvalidParserConfigurationException(
                "It is useless to put a fixed value \"" + fixedValue + "\" in the require section.");
    }

    private boolean foundRequiredValue = false;

    @Override
    public void inform(String key, WalkResult foundValue) {
        foundRequiredValue = true;
        if (verbose) {
            LOG.info("Info REQUIRE: {}", key);
            LOG.info("NEED REQUIRE: {}", getMatchExpression());
            LOG.info("KEPT REQUIRE: {}", key);
        }
    }

    @Override
    public boolean obtainResult() {
        processInformedMatches();
        return foundRequiredValue;
    }

    @Override
    public void reset() {
        super.reset();
        foundRequiredValue = false;
    }

    @Override
    public String toString() {
        return "Require.("+matcher.getMatcherSourceLocation()+"): " + getMatchExpression();
    }
}



