/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2017 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatcherRequireAction extends MatcherAction {
    private static final Logger LOG = LoggerFactory.getLogger(MatcherRequireAction.class);

    public MatcherRequireAction(String config, Matcher matcher) {
        init(config, matcher);
    }

    protected ParserRuleContext parseWalkerExpression(UserAgentTreeWalkerParser parser) {
        return parser.matcherRequire();
    }

    protected void setFixedValue(String fixedValue) {
        throw new InvalidParserConfigurationException(
                "It is useless to put a fixed value \"" + fixedValue + "\"in the require section.");
    }

    private boolean foundRequiredValue = false;

    @Override
    public void inform(String key, String foundValue) {
        foundRequiredValue = true;
        if (verbose) {
            LOG.info("Info REQUIRE: {}", key);
            LOG.info("NEED REQUIRE: {}", getMatchExpression());
            LOG.info("KEPT REQUIRE: {}", key);
        }
    }

    @Override
    public boolean obtainResult(UserAgent userAgent) {
        if (isValidIsNull()) {
            foundRequiredValue = true;
        }
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
        return "Require: " + getMatchExpression();
    }

}



