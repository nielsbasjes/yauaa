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

package nl.basjes.parse.useragent.analyze.treewalker;

import nl.basjes.parse.useragent.UserAgentTreeWalkerBaseVisitor;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser.MatcherContext;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser.MatcherPathLookupContext;
import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import nl.basjes.parse.useragent.analyze.treewalker.steps.Step;
import nl.basjes.parse.useragent.analyze.treewalker.steps.WalkList;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepIsNull;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * This class gets the symbol table (1 value) uses that to evaluate
 * the expression against the parsed user agent
 */
public class TreeExpressionEvaluator {
    private static final Logger LOG = LoggerFactory.getLogger(TreeExpressionEvaluator.class);

    private boolean verbose = false;
    private final MatcherContext requiredPattern;
    private final Map<String, Map<String, String>> lookups;

    private final WalkList walkList;

    public TreeExpressionEvaluator(MatcherContext requiredPattern, Map<String, Map<String, String>> lookups) {
        this.requiredPattern = requiredPattern;
        this.lookups = lookups;

        walkList = new WalkList(requiredPattern, lookups);
    }

    public void setVerbose(boolean newVerbose) {
        this.verbose = newVerbose;
        walkList.setVerbose(newVerbose);
    }

    /**
     * @return The fixed value in case of a fixed value. NULL if a dynamic value
     */
    public String getFixedValue() {
        return (new UserAgentTreeWalkerBaseVisitor<String>() {
            @Override
            public String visitMatcherPathLookup(MatcherPathLookupContext ctx) {
                String value = visit(ctx.matcherLookup());
                if (value == null) {
                    return null;
                }
                // No we know this is a fixed value. Yet we can have a problem in the lookup that was
                // configured. If we have this then this is a FATAL error (it will fail always everywhere).

                Map<String, String> lookup = lookups.get(ctx.lookup.getText());
                if (lookup == null) {
                    throw new InvalidParserConfigurationException("Missing lookup \"" + ctx.lookup.getText() + "\" ");
                }

                String resultingValue = lookup.get(value.toLowerCase());
                if (resultingValue == null) {
                    if (ctx.defaultValue != null) {
                        return ctx.defaultValue.getText();
                    }
                    throw new InvalidParserConfigurationException(
                        "Fixed value >>" + value + "<< is missing in lookup: \"" + ctx.lookup.getText() + "\" ");
                }
                return resultingValue;
            }

            @Override
            public String visitPathFixedValue(UserAgentTreeWalkerParser.PathFixedValueContext ctx) {
                return ctx.value.getText();
            }
        }).visit(requiredPattern);
    }

    // ------------------------------------------

    public String evaluate(ParseTree tree, String key, String value) {
        if (verbose) {
            LOG.info("Evaluate: {} => {}", key, value);
            LOG.info("Pattern : {}", requiredPattern.getText());
            LOG.info("WalkList: {}", walkList.toString());
        }
        String result = walkList.walk(tree, value);
        if (verbose) {
            LOG.info("Evaluate: Result = {}", result);
        }
        return result;
    }

    public boolean usesIsNull() {
        Step first = walkList.getFirstStep();
        return (first instanceof StepIsNull);
    }

    public WalkList getWalkListForUnitTesting() {
        return walkList;
    }
}
