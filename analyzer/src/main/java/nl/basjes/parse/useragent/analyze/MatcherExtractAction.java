/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2016 Niels Basjes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.UserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatcherExtractAction extends MatcherAction {
    private static final Logger LOG = LoggerFactory.getLogger(MatcherExtractAction.class);

    private String attribute;
    private long confidence;
    private String foundValue = null;
    private String fixedValue = null;
    private String expression;

    public MatcherExtractAction(String attribute, long confidence, String config, Matcher matcher) {
        this.attribute = attribute;
        this.confidence = confidence;
        expression = config;
        init(config, matcher);
//        if (verbose) {
//            LOG.info("-- construct ({} , {} , {})", attribute, confidence, config);
//        }
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
        /**
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
            userAgent.set(attribute, fixedValue, confidence);
            return true;
        }
        if (foundValue != null) {
            if (verbose) {
                LOG.info("Set parsevalue ({})[{}]: {}", attribute, confidence, foundValue);
            }
            userAgent.set(attribute, foundValue, confidence);
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
            return "Extract (FIXED): (" + attribute + ", " + confidence + ") = " + fixedValue;
        } else {
            return "Extract (DYNAM): (" + attribute + ", " + confidence + "): " + expression;
        }
    }
}
