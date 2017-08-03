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

public class MatcherExtractAction extends MatcherAction {
    private static final Logger LOG = LoggerFactory.getLogger(MatcherExtractAction.class);

    private final String attribute;
    private final long confidence;
    private String foundValue = null;
    private String fixedValue = null;
    private final String expression;

    public MatcherExtractAction(String attribute, long confidence, String config, Matcher matcher) {
        this.attribute = attribute;
        this.confidence = confidence;
        expression = config;
        init(config, matcher);
    }

    protected ParserRuleContext parseWalkerExpression(UserAgentTreeWalkerParser parser) {
        return parser.matcher();
    }

    public boolean isFixedValue() {
        return this.fixedValue != null;
    }

    protected void setFixedValue(String newFixedValue) {
        if (verbose) {
            LOG.info("-- set Fixed value({} , {} , {})", attribute, confidence, newFixedValue);
        }
        this.fixedValue = newFixedValue;
    }

    public String getAttribute() {
        return attribute;
    }

    public void inform(String key, String newlyFoundValue) {
        if (verbose) {
            LOG.info("INFO  : EXTRACT ({}): {}", attribute, key);
            LOG.info("NEED  : EXTRACT ({}): {}", attribute, getMatchExpression());
        }
        /*
         * We know the tree is parsed from left to right.
         * This is also the priority in the fields.
         * So we always use the first value we find.
         */
        if (this.foundValue == null) {
            this.foundValue = newlyFoundValue;
            if (verbose) {
                LOG.info("KEPT  : EXTRACT ({}): {}", attribute, key);
            }
        } else {
            if (verbose) {
                LOG.info("IGNORE: EXTRACT ({}): {}", attribute, key);
            }
        }
    }

    public boolean obtainResult(UserAgent userAgent) {
        processInformedMatches();
        if (fixedValue != null) {
            if (verbose) {
                LOG.info("Set fixedvalue ({})[{}]: {}", attribute, confidence, fixedValue);
            }
            userAgent.setForced(attribute, fixedValue, confidence);
            return true;
        }
        if (foundValue != null) {
            if (verbose) {
                LOG.info("Set parsevalue ({})[{}]: {}", attribute, confidence, foundValue);
            }
            userAgent.setForced(attribute, foundValue, confidence);
            return true;
        }
        if (verbose) {
            LOG.info("Nothing found for {}", attribute);
        }
        return false;
    }

    public void reset() {
        super.reset();
        this.foundValue = null;
    }

    @Override
    public String toString() {
        if (isFixedValue()) {
            return "FIXED  : (" + attribute + ", " + confidence + ") =   \"" + fixedValue + "\"";
        } else {
            return "DYNAMIC: (" + attribute + ", " + confidence + "):    " + expression;
        }
    }
}
