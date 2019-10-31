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

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.analyze.treewalker.steps.WalkList.WalkResult;
import nl.basjes.parse.useragent.parse.MatcherTree;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class MatcherVariableAction extends MatcherAction {
    private static final Logger LOG = LoggerFactory.getLogger(MatcherVariableAction.class);

    private final String variableName;
    private transient WalkResult foundValue = null;
    private final String expression;
    private Set<MatcherAction> interestedActions;

    // private constructor for serialization systems ONLY (like Kyro)
    private MatcherVariableAction() {
        variableName = null;
        expression = null;
    }

    public MatcherVariableAction(String variableName, String config, Matcher matcher) {
        this.variableName = variableName;
        expression = config;
        init(config, matcher);
    }

    protected ParserRuleContext<MatcherTree> parseWalkerExpression(UserAgentTreeWalkerParser<MatcherTree> parser) {
        return parser.matcherVariable();
    }

    protected void setFixedValue(String fixedValue) {
        throw new InvalidParserConfigurationException(
            "It is useless to put a fixed value \"" + fixedValue + "\" in the variable section.");
    }

    public String getVariableName() {
        return variableName;
    }

    public void inform(MatcherTree key, WalkResult newlyFoundValue) {
        if (verbose) {
            LOG.info("--------------------------------------------------");
            LOG.info("INFO  : VARIABLE ({}): {}", variableName, key);
            LOG.info("NEED  : VARIABLE ({}): {}", variableName, getMatchExpression());
        }
        /*
         * We know the tree is parsed from left to right.
         * This is also the priority in the fields.
         * So we always use the first value we find.
         */
        if (this.foundValue == null) {
            this.foundValue = newlyFoundValue;
            if (verbose) {
                LOG.info("KEPT  : VARIABLE ({}): {}", variableName, key);
            }

            if (interestedActions != null && !interestedActions.isEmpty()) {
                for (MatcherAction action : interestedActions) {
                    action.inform(null, newlyFoundValue.getTree(), newlyFoundValue.getValue());
                }
            }
        }
        if (verbose) {
            LOG.info("--------------------------------------------------");
        }
    }

    public boolean obtainResult() {
        processInformedMatches();
        return this.foundValue != null;
    }

    @Override
    public void reset() {
        super.reset();
        this.foundValue = null;
    }

    @Override
    public String toString() {
        return "VARIABLE: (" + variableName + "): " + expression;
    }

    public void setInterestedActions(Set<MatcherAction> newInterestedActions) {
        this.interestedActions = newInterestedActions;
    }
}
