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

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.analyze.treewalker.steps.WalkList.WalkResult;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MatcherFailIfFoundAction extends MatcherAction {
    private static final Logger LOG = LogManager.getLogger(MatcherFailIfFoundAction.class);

    @SuppressWarnings("unused") // Private constructor for serialization systems ONLY (like Kryo)
    private MatcherFailIfFoundAction() {
    }

    public MatcherFailIfFoundAction(String config, Matcher matcher) {
        init(config, matcher);
    }

    protected ParserRuleContext parseWalkerExpression(UserAgentTreeWalkerParser parser) {
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
                "It is useless to put a fixed value \"" + fixedValue + "\" in the failIfFound section.");
    }

    private boolean foundRequiredValue = false;

    @Override
    public void inform(String key, String value, ParseTree result) {
        super.inform(key, value, result);
        // If there are NO additional steps then we can immediately conclude this is matcher must fail.
        if (evaluator.isEmpty()) {
            matcher.failImmediately();
        }
    }

    @Override
    public void inform(String key, WalkResult foundValue) {
        foundRequiredValue = true;
        if (verbose) {
            LOG.info("Info FailIfFound: {}", key);
            LOG.info("NEED FailIfFound: {}", getMatchExpression());
            LOG.info("KEPT FailIfFound: {}", key);
        }
    }

    @Override
    public boolean obtainResult() {
        processInformedMatches();
        return !foundRequiredValue;
    }

    @Override
    public void reset() {
        super.reset();
        foundRequiredValue = false;
    }

    @Override
    public String toString() {
        return "FailIfFound.("+matcher.getMatcherSourceLocation()+"): " + getMatchExpression();
    }
}



