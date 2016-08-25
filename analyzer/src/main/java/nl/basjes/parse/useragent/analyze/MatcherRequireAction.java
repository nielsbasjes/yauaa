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

public class MatcherRequireAction extends MatcherAction {
    private static final Logger LOG = LoggerFactory.getLogger(MatcherRequireAction.class);
    private final String expression;

    public MatcherRequireAction(String config, Matcher matcher) {
        expression = config;
        init(config, matcher);
//        if (verbose) {
//            LOG.info("{} ({})", this.getClass().getSimpleName(), config);
//        }
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
        return "Require: " + expression;
    }

}



