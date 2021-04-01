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

package nl.basjes.parse.useragent.analyze.treewalker;

import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import nl.basjes.parse.useragent.analyze.Matcher;
import nl.basjes.parse.useragent.analyze.treewalker.steps.WalkList;
import nl.basjes.parse.useragent.analyze.treewalker.steps.WalkList.WalkResult;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerBaseVisitor;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathIsInLookupContainsContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathIsInLookupContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathIsInLookupPrefixContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathIsNotInLookupPrefixContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathLookupContainsContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathLookupContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathLookupPrefixContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.PathFixedValueContext;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;

/**
 * This class gets the symbol table (1 value) uses that to evaluate
 * the expression against the parsed user agent
 */
public class TreeExpressionEvaluator implements Serializable {
    private static final Logger LOG = LogManager.getLogger(TreeExpressionEvaluator.class);
    private final boolean verbose;

    private final String requiredPatternText;
    private final WalkList walkList;
    private final String fixedValue;

    @SuppressWarnings("unused") // Private constructor for serialization systems ONLY (like Kryo)
    private TreeExpressionEvaluator() {
        requiredPatternText = null;
        verbose = false;
        fixedValue = null;
        walkList = null;
    }

    public TreeExpressionEvaluator(ParserRuleContext requiredPattern,
                                   Matcher matcher,
                                   boolean verbose) {
        this.requiredPatternText = requiredPattern.getText();
        this.verbose = verbose;
        this.fixedValue = calculateFixedValue(requiredPattern);
        walkList = new WalkList(requiredPattern, matcher.getLookups(), matcher.getLookupSets(), verbose);
    }

    public boolean isEmpty() {
        return walkList.isEmpty();
    }

    public void destroy() {
        walkList.destroy();
    }

    /**
     * @return The fixed value in case of a fixed value. NULL if a dynamic value
     */
    public String getFixedValue() {
        return fixedValue;
    }

    private String calculateFixedValue(ParserRuleContext requiredPattern) {
        return new UserAgentTreeWalkerBaseVisitor<String>() {

            @Override
            protected boolean shouldVisitNextChild(RuleNode node, String currentResult) {
                return currentResult == null;
            }

            @Override
            protected String aggregateResult(String aggregate, String nextResult) {
                return nextResult == null ? aggregate : nextResult;
            }

            // =================
            // Having a lookup that provides a fixed value yields an error as it complicates things needlessly
            @Override public String visitMatcherPathLookup(MatcherPathLookupContext ctx) {
                return visitLookupsFailOnFixedString(ctx.matcher());
            }

            @Override public String visitMatcherPathLookupContains(MatcherPathLookupContainsContext ctx) {
                return visitLookupsFailOnFixedString(ctx.matcher());
            }

            @Override public String visitMatcherPathLookupPrefix(MatcherPathLookupPrefixContext ctx) {
                return visitLookupsFailOnFixedString(ctx.matcher());
            }

            @Override public String visitMatcherPathIsInLookup(MatcherPathIsInLookupContext ctx) {
                return visitLookupsFailOnFixedString(ctx.matcher());
            }

            @Override public String visitMatcherPathIsInLookupContains(MatcherPathIsInLookupContainsContext ctx) {
                return visitLookupsFailOnFixedString(ctx.matcher());
            }

            @Override public String visitMatcherPathIsInLookupPrefix(MatcherPathIsInLookupPrefixContext ctx) {
                return visitLookupsFailOnFixedString(ctx.matcher());
            }

            @Override public String visitMatcherPathIsNotInLookupPrefix(MatcherPathIsNotInLookupPrefixContext ctx) {
                return visitLookupsFailOnFixedString(ctx.matcher());
            }


            private String visitLookupsFailOnFixedString(ParseTree matcherTree) {
                String value = visit(matcherTree);
                if (value == null) {
                    return null;
                }
                // Now we know this is a fixed value lookup ... JUST DON'T as it is needless.
                throw new InvalidParserConfigurationException("A lookup for a fixed input value is a needless complexity.");
            }
            // =================

            @Override
            public String visitPathFixedValue(PathFixedValueContext ctx) {
                return ctx.value.getText();
            }
        }.visit(requiredPattern);
    }

    // ------------------------------------------

    public WalkResult evaluate(ParseTree tree, String key, String value) {
        if (verbose) {
            LOG.info("Evaluate: {} => {}", key, value);
            LOG.info("Pattern : {}", requiredPatternText);
            LOG.info("WalkList: {}", walkList);
        }
        WalkResult result = walkList.walk(tree, value);
        if (verbose) {
            LOG.info("Evaluate: Result = {}", result == null ? "null" : result.getValue());
        }
        return result;
    }

    public boolean mustHaveMatches() {
        return walkList.mustHaveMatches();
    }

    public WalkList getWalkListForUnitTesting() {
        return walkList;
    }

    public long pruneTrailingStepsThatCannotFail() {
        return walkList.pruneTrailingStepsThatCannotFail();
    }
}
