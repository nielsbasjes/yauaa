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

import nl.basjes.parse.useragent.analyze.WordRangeVisitor.Range;
import nl.basjes.parse.useragent.analyze.treewalker.TreeExpressionEvaluator;
import nl.basjes.parse.useragent.analyze.treewalker.steps.WalkList.WalkResult;
import nl.basjes.parse.useragent.parse.MatcherTree;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerBaseVisitor;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerLexer;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherBaseContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherConcatContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherConcatPostfixContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherConcatPrefixContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherExtractContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherNormalizeBrandContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathIsInLookupPrefixContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathIsNotInLookupPrefixContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathLookupContainsContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathLookupPrefixContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherReplaceStringContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherSegmentRangeContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherVariableContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherWordRangeContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.PathVariableContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepContainsValueContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownAgentContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownBase64Context;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownCommentsContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownEmailContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownEntryContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownKeyContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownKeyvalueContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownNameContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownProductContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownTextContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownUrlContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownUuidContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownValueContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownVersionContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepWordRangeContext;
import nl.basjes.parse.useragent.utils.DefaultANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static nl.basjes.parse.useragent.parse.AgentPathFragment.AGENT;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.BASE64;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.COMMENTS;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.EMAIL;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.ENTRY;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.KEY;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.KEYVALUE;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.NAME;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.PRODUCT;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.TEXT;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.URL;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.UUID;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.VALUE;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.VERSION;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherCleanVersionContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathIsNullContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathLookupContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.PathFixedValueContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.PathWalkContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepEndsWithValueContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepEqualsValueContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepNotEqualsValueContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepStartsWithValueContext;

public abstract class MatcherAction implements Serializable {

    private   String                  matchExpression;
    protected TreeExpressionEvaluator evaluator;

    TreeExpressionEvaluator getEvaluatorForUnitTesting() {
        return evaluator;
    }

    private static final Logger LOG = LoggerFactory.getLogger(MatcherAction.class);

    protected Matcher     matcher;
    private MatchesList matches = new MatchesList(0);
    private boolean     mustHaveMatches = false;

    boolean mustHaveMatches() {
        return mustHaveMatches;
    }

    boolean verbose = false;
    private boolean verbosePermanent = false;
    private boolean verboseTemporary = false;

    private void setVerbose(boolean newVerbose) {
        setVerbose(newVerbose, false);
    }

    public void setVerbose(boolean newVerbose, boolean temporary) {
        this.verbose = newVerbose;
        if (!temporary) {
            this.verbosePermanent = newVerbose;
        }
        this.verboseTemporary = temporary;
    }

    public String getMatchExpression() {
        return matchExpression;
    }

    class InitErrorListener implements DefaultANTLRErrorListener {
        @Override
        public void syntaxError(
            Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int line,
            int charPositionInLine,
            String msg,
            RecognitionException e) {
            LOG.error("Syntax error");
            LOG.error("Source {}: {}", matcher.getMatcherSourceLocation(), matchExpression);
            LOG.error("Message: {}", msg);
            throw new InvalidParserConfigurationException("Syntax error \"" + msg + "\" caused by \"" + matchExpression + "\".");
        }

        // Ignore all other types of problems
    }

    void init(String newMatchExpression, Matcher newMatcher) {
        this.matcher = newMatcher;
        this.matchExpression = newMatchExpression;
        setVerbose(newMatcher.getVerbose());
    }

    public long initialize(MatcherTree treeRoot) {
        InitErrorListener errorListener = new InitErrorListener();

        CodePointCharStream      input = CharStreams.fromString(this.matchExpression);
        UserAgentTreeWalkerLexer lexer = new UserAgentTreeWalkerLexer(input);

        lexer.addErrorListener(errorListener);

        CommonTokenStream         tokens = new CommonTokenStream(lexer);
        UserAgentTreeWalkerParser<MatcherTree> parser = new UserAgentTreeWalkerParser<>(tokens);

        parser.addErrorListener(errorListener);

        ParserRuleContext<MatcherTree> requiredPattern = parseWalkerExpression(parser);

        // We couldn't ditch the double quotes around the fixed values in the parsing phase.
        // So we ditch them here. We simply walk the tree and modify some of the tokens.
        new UnQuoteValues().visit(requiredPattern);

        // Now we create an evaluator instance
        evaluator = new TreeExpressionEvaluator(requiredPattern, matcher, verbose);

        // Is a fixed value (i.e. no events will ever be fired)?
        String fixedValue = evaluator.getFixedValue();
        if (fixedValue != null) {
            setFixedValue(fixedValue);
            mustHaveMatches = false;
            matches = new MatchesList(0);
            return 0; // Not interested in any patterns
        }

        mustHaveMatches = evaluator.mustHaveMatches();

        int informs = calculateInformPath(this, treeRoot, requiredPattern);

        // If this is based on a variable we do not need any matches from the hashmap.
        if (mustHaveMatches && informs == 0) {
            mustHaveMatches = false;
        }

        int listSize = 0;
        if (informs > 0) {
            listSize = 1;
        }
        this.matches = new MatchesList(listSize);
        return informs;
    }

    protected abstract <T> ParserRuleContext<T> parseWalkerExpression(UserAgentTreeWalkerParser<T> parser);

    private static class UnQuoteValues extends UserAgentTreeWalkerBaseVisitor<Void, MatcherTree> {
        private void unQuoteToken(Token token) {
            if (token instanceof CommonToken) {
                CommonToken commonToken = (CommonToken) token;
                commonToken.setStartIndex(commonToken.getStartIndex() + 1);
                commonToken.setStopIndex(commonToken.getStopIndex() - 1);
            }
        }

        @Override
        public Void visitMatcherPathLookup(MatcherPathLookupContext<MatcherTree> ctx) {
            unQuoteToken(ctx.defaultValue);
            return super.visitMatcherPathLookup(ctx);
        }

        @Override
        public Void visitMatcherPathLookupPrefix(MatcherPathLookupPrefixContext<MatcherTree> ctx) {
            unQuoteToken(ctx.defaultValue);
            return super.visitMatcherPathLookupPrefix(ctx);
        }

        @Override
        public Void visitPathFixedValue(PathFixedValueContext<MatcherTree> ctx) {
            unQuoteToken(ctx.value);
            return super.visitPathFixedValue(ctx);
        }

        @Override
        public Void visitMatcherConcat(MatcherConcatContext<MatcherTree> ctx) {
            unQuoteToken(ctx.prefix);
            unQuoteToken(ctx.postfix);
            return super.visitMatcherConcat(ctx);
        }

        @Override
        public Void visitMatcherConcatPrefix(MatcherConcatPrefixContext<MatcherTree> ctx) {
            unQuoteToken(ctx.prefix);
            return super.visitMatcherConcatPrefix(ctx);
        }

        @Override
        public Void visitMatcherConcatPostfix(MatcherConcatPostfixContext<MatcherTree> ctx) {
            unQuoteToken(ctx.postfix);
            return super.visitMatcherConcatPostfix(ctx);
        }

        @Override
        public Void visitMatcherReplaceString(MatcherReplaceStringContext<MatcherTree> ctx) {
            unQuoteToken(ctx.search);
            unQuoteToken(ctx.replace);
            return super.visitMatcherReplaceString(ctx);
        }

        public Void visitStepEqualsValue(StepEqualsValueContext<MatcherTree> ctx) {
            unQuoteToken(ctx.value);
            return super.visitStepEqualsValue(ctx);
        }

        @Override
        public Void visitStepNotEqualsValue(StepNotEqualsValueContext<MatcherTree> ctx) {
            unQuoteToken(ctx.value);
            return super.visitStepNotEqualsValue(ctx);
        }

        @Override
        public Void visitStepStartsWithValue(StepStartsWithValueContext<MatcherTree> ctx) {
            unQuoteToken(ctx.value);
            return super.visitStepStartsWithValue(ctx);
        }

        @Override
        public Void visitStepEndsWithValue(StepEndsWithValueContext<MatcherTree> ctx) {
            unQuoteToken(ctx.value);
            return super.visitStepEndsWithValue(ctx);
        }

        @Override
        public Void visitStepContainsValue(StepContainsValueContext<MatcherTree> ctx) {
            unQuoteToken(ctx.value);
            return super.visitStepContainsValue(ctx);
        }
    }

    protected abstract void setFixedValue(String newFixedValue);

    /**
     * For each key that this action wants to be notified for this method is called.
     * Note that on a single parse event the same name CAN be called multiple times!!
     *
     * @param key    The key of the node
     * @param value  The value that was found
     * @param result The node in the parser tree where the match occurred
     */
    public void inform(MatcherTree key, ParseTree<MatcherTree> result, String value) {
        matcher.receivedInput();

        // Only if this needs input we tell the matcher on the first one.
        if (mustHaveMatches && matches.isEmpty()) {
            matcher.gotMyFirstStartingPoint();
        }

        LOG.info("{} was informed (part of matcher {}).", this.getClass().getSimpleName(), matcher.getMatcherSourceLocation());
        LOG.info("  Desired    : >>>{}<<<", matchExpression);
        LOG.info("  Found key  : >>>{}<<<", key);
        LOG.info("  Found value: >>>{}<<<", value);
//        LOG.info("   tree source >>>{}<<<", result);

        matches.add(result, key, value);
    }

    protected abstract void inform(MatcherTree key, WalkResult foundValue);

    /**
     * @return If it is impossible that this can be valid it returns true, else false.
     */
    boolean cannotBeValid() {
        if (mustHaveMatches) {
            return matches.isEmpty();
        }
        return false;
    }

    /**
     * Called after all nodes have been notified.
     * @return true if the obtainResult result was valid. False will fail the entire matcher this belongs to.
     */
    public abstract boolean obtainResult();

    boolean isValidWithoutMatches() {
        return matches.isEmpty() && !evaluator.mustHaveMatches();
    }

    /**
     * Optimization: Only if there is a possibility that all actions for this matcher CAN be valid do we
     * actually perform the analysis and do the (expensive) tree walking and matching.
     */
    void processInformedMatches() {
        for (MatchesList.Match match : matches) {
            WalkResult matchedValue = evaluator.evaluate(match.getResult(), match.getKey(), match.getValue());
            if (matchedValue != null) {
                inform(match.getKey(), matchedValue);
                break; // We always stick to the first match
            }
        }
    }

    // ============================================================================================================

    @FunctionalInterface
    public interface CalculateInformPathFunction {
        /**
         * Applies this function to the given arguments.
         * @param action The applicable action.
        //         * @param treeName The name of the current tree.
         * @param tree The actual location in the parseTree
         * @return the number of informs done
         */
        int calculateInformPath(MatcherAction action, MatcherTree matcherTree, ParserRuleContext<MatcherTree> tree);
    }

    private static final Map<Class<?>, CalculateInformPathFunction> CALCULATE_INFORM_PATH = new HashMap<>();

    private static final NumberRangeVisitor<MatcherTree> NUMBER_RANGE_VISITOR = new NumberRangeVisitor<>();
    private static final WordRangeVisitor<MatcherTree>  WORD_RANGE_VISITOR = new WordRangeVisitor<>();

    static {
        // -------------
        CALCULATE_INFORM_PATH.put(MatcherBaseContext.class,             (action, pathMatcherTree, tree) ->
            calculateInformPath(action, pathMatcherTree, ((MatcherBaseContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherPathIsNullContext.class,       (action, pathMatcherTree, tree) ->
            calculateInformPath(action, pathMatcherTree, ((MatcherPathIsNullContext<MatcherTree>) tree).matcher()));

        // -------------
        CALCULATE_INFORM_PATH.put(MatcherExtractContext.class,          (action, pathMatcherTree, tree) ->
            calculateInformPath(action, pathMatcherTree, ((MatcherExtractContext<MatcherTree>) tree).expression));

        // -------------
        CALCULATE_INFORM_PATH.put(MatcherVariableContext.class,         (action, pathMatcherTree, tree) ->
            calculateInformPath(action, pathMatcherTree, ((MatcherVariableContext<MatcherTree>) tree).expression));

        // -------------
        CALCULATE_INFORM_PATH.put(MatcherPathContext.class,             (action, treeName, tree) ->
            calculateInformPath(action, treeName, ((MatcherPathContext<MatcherTree>) tree).basePath()));
        CALCULATE_INFORM_PATH.put(MatcherConcatContext.class,           (action, treeName, tree) ->
            calculateInformPath(action, treeName, ((MatcherConcatContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherConcatPrefixContext.class,     (action, treeName, tree) ->
            calculateInformPath(action, treeName, ((MatcherConcatPrefixContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherConcatPostfixContext.class,    (action, treeName, tree) ->
            calculateInformPath(action, treeName, ((MatcherConcatPostfixContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherNormalizeBrandContext.class,   (action, treeName, tree) ->
            calculateInformPath(action, treeName, ((MatcherNormalizeBrandContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherCleanVersionContext.class,     (action, treeName, tree) ->
            calculateInformPath(action, treeName, ((MatcherCleanVersionContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherReplaceStringContext.class,     (action, treeName, tree) ->
            calculateInformPath(action, treeName, ((MatcherReplaceStringContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherPathLookupContext.class,       (action, treeName, tree) ->
            calculateInformPath(action, treeName, ((MatcherPathLookupContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherPathLookupContainsContext.class,      (action, treeName, tree) ->
            calculateInformPath(action, treeName, ((MatcherPathLookupContainsContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherPathIsInLookupContainsContext.class,  (action, treeName, tree) ->
            calculateInformPath(action, treeName, ((MatcherPathIsInLookupContainsContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherPathLookupPrefixContext.class,        (action, treeName, tree) ->
            calculateInformPath(action, treeName, ((MatcherPathLookupPrefixContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherPathIsInLookupPrefixContext.class,    (action, treeName, tree) ->
            calculateInformPath(action, treeName, ((MatcherPathIsInLookupPrefixContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherPathIsNotInLookupPrefixContext.class,    (action, treeName, tree) ->
            calculateInformPath(action, treeName, ((MatcherPathIsNotInLookupPrefixContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherDefaultIfNullContext.class,           (action, treeName, tree) ->
            calculateInformPath(action, treeName, ((MatcherDefaultIfNullContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH .put(MatcherWordRangeContext.class,        (action, treeName, tree) ->
            calculateInformPath(action, treeName, ((MatcherWordRangeContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH .put(MatcherSegmentRangeContext.class,        (action, treeName, tree) ->
            calculateInformPath(action, treeName, ((MatcherSegmentRangeContext<MatcherTree>) tree).matcher()));

        // -------------
        CALCULATE_INFORM_PATH.put(PathVariableContext.class, (action, pathMatcherTree, tree) -> {
//            LOG.info("Need variable {}", ((PathVariableContext<MatcherTree>) tree).variable.getText());
            action.matcher.informMeAboutVariable(action, ((PathVariableContext<MatcherTree>) tree).variable.getText());
            return 0;
        });

        CALCULATE_INFORM_PATH.put(PathWalkContext.class, (action, pathMatcherTree, tree) ->
            calculateInformPath(action, pathMatcherTree, ((PathWalkContext<MatcherTree>) tree).nextStep));

        // -------------
        CALCULATE_INFORM_PATH.put(StepDownAgentContext.class, (action, pathMatcherTree, tree) -> {
            StepDownAgentContext<MatcherTree> thisTree = ((StepDownAgentContext<MatcherTree>) tree);
            int total = 0;
            for (int i : NUMBER_RANGE_VISITOR.visit(thisTree.numberRange())) {
                MatcherTree nextMatcherTree = pathMatcherTree.getOrCreateChild(AGENT, i);
                total+= calculateInformPath(action, nextMatcherTree, thisTree.nextStep);
            }
            return total;
        });

        CALCULATE_INFORM_PATH.put(StepDownProductContext.class, (action, pathMatcherTree, tree) -> {
            StepDownProductContext<MatcherTree> thisTree = ((StepDownProductContext<MatcherTree>) tree);
            int total = 0;
            for (int i : NUMBER_RANGE_VISITOR.visit(thisTree.numberRange())) {
                MatcherTree nextMatcherTree = pathMatcherTree.getOrCreateChild(PRODUCT, i);
                total+= calculateInformPath(action, nextMatcherTree, thisTree.nextStep);
            }
            return total;
        });

        CALCULATE_INFORM_PATH.put(StepDownNameContext.class, (action, pathMatcherTree, tree) -> {
            StepDownNameContext<MatcherTree> thisTree = ((StepDownNameContext<MatcherTree>) tree);
            int total = 0;
            for (int i : NUMBER_RANGE_VISITOR.visit(thisTree.numberRange())) {
                MatcherTree nextMatcherTree = pathMatcherTree.getOrCreateChild(NAME, i);
                total+= calculateInformPath(action, nextMatcherTree, thisTree.nextStep);
            }
            return total;
        });

        CALCULATE_INFORM_PATH.put(StepDownVersionContext.class, (action, pathMatcherTree, tree) -> {
            StepDownVersionContext<MatcherTree> thisTree = ((StepDownVersionContext<MatcherTree>) tree);
            int total = 0;
            for (int i : NUMBER_RANGE_VISITOR.visit(thisTree.numberRange())) {
                MatcherTree nextMatcherTree = pathMatcherTree.getOrCreateChild(VERSION, i);
                total+= calculateInformPath(action, nextMatcherTree, thisTree.nextStep);
            }
            return total;
        });

        CALCULATE_INFORM_PATH.put(StepDownCommentsContext.class, (action, pathMatcherTree, tree) -> {
            StepDownCommentsContext<MatcherTree> thisTree = ((StepDownCommentsContext<MatcherTree>) tree);
            int total = 0;
            for (int i : NUMBER_RANGE_VISITOR.visit(thisTree.numberRange())) {
                MatcherTree nextMatcherTree = pathMatcherTree.getOrCreateChild(COMMENTS, i);
                total+= calculateInformPath(action, nextMatcherTree, thisTree.nextStep);
            }
            return total;
        });


        CALCULATE_INFORM_PATH.put(StepDownEntryContext.class, (action, pathMatcherTree, tree) -> {
            StepDownEntryContext<MatcherTree> thisTree = ((StepDownEntryContext<MatcherTree>) tree);
            int total = 0;
            for (int i : NUMBER_RANGE_VISITOR.visit(thisTree.numberRange())) {
                MatcherTree nextMatcherTree = pathMatcherTree.getOrCreateChild(ENTRY, i);
                total+= calculateInformPath(action, nextMatcherTree, thisTree.nextStep);
            }
            return total;
        });

        CALCULATE_INFORM_PATH.put(StepDownTextContext.class, (action, pathMatcherTree, tree) -> {
            StepDownTextContext<MatcherTree> thisTree = ((StepDownTextContext<MatcherTree>) tree);
            int total = 0;
            for (int i : NUMBER_RANGE_VISITOR.visit(thisTree.numberRange())) {
                MatcherTree nextMatcherTree = pathMatcherTree.getOrCreateChild(TEXT, i);
                total+= calculateInformPath(action, nextMatcherTree, thisTree.nextStep);
            }
            return total;
        });


        CALCULATE_INFORM_PATH.put(StepDownUrlContext.class, (action, pathMatcherTree, tree) -> {
            StepDownUrlContext<MatcherTree> thisTree = ((StepDownUrlContext<MatcherTree>) tree);
            int total = 0;
            for (int i : NUMBER_RANGE_VISITOR.visit(thisTree.numberRange())) {
                MatcherTree nextMatcherTree = pathMatcherTree.getOrCreateChild(URL, i);
                total+= calculateInformPath(action, nextMatcherTree, thisTree.nextStep);
            }
            return total;
        });


        CALCULATE_INFORM_PATH.put(StepDownEmailContext.class, (action, pathMatcherTree, tree) -> {
            StepDownEmailContext<MatcherTree> thisTree = ((StepDownEmailContext<MatcherTree>) tree);
            int total = 0;
            for (int i : NUMBER_RANGE_VISITOR.visit(thisTree.numberRange())) {
                MatcherTree nextMatcherTree = pathMatcherTree.getOrCreateChild(EMAIL, i);
                total+= calculateInformPath(action, nextMatcherTree, thisTree.nextStep);
            }
            return total;
        });

        CALCULATE_INFORM_PATH.put(StepDownBase64Context.class, (action, pathMatcherTree, tree) -> {
            StepDownBase64Context<MatcherTree> thisTree = ((StepDownBase64Context<MatcherTree>) tree);
            int total = 0;
            for (int i : NUMBER_RANGE_VISITOR.visit(thisTree.numberRange())) {
                MatcherTree nextMatcherTree = pathMatcherTree.getOrCreateChild(BASE64, i);
                total+= calculateInformPath(action, nextMatcherTree, thisTree.nextStep);
            }
            return total;
        });

        CALCULATE_INFORM_PATH.put(StepDownUuidContext.class, (action, pathMatcherTree, tree) -> {
            StepDownUuidContext<MatcherTree> thisTree = ((StepDownUuidContext<MatcherTree>) tree);
            int total = 0;
            for (int i : NUMBER_RANGE_VISITOR.visit(thisTree.numberRange())) {
                MatcherTree nextMatcherTree = pathMatcherTree.getOrCreateChild(UUID, i);
                total+= calculateInformPath(action, nextMatcherTree, thisTree.nextStep);
            }
            return total;
        });

        CALCULATE_INFORM_PATH.put(StepDownKeyvalueContext.class, (action, pathMatcherTree, tree) -> {
            StepDownKeyvalueContext<MatcherTree> thisTree = ((StepDownKeyvalueContext<MatcherTree>) tree);
            int total = 0;
            for (int i : NUMBER_RANGE_VISITOR.visit(thisTree.numberRange())) {
                MatcherTree nextMatcherTree = pathMatcherTree.getOrCreateChild(KEYVALUE, i);
                total+= calculateInformPath(action, nextMatcherTree, thisTree.nextStep);
            }
            return total;
        });


        CALCULATE_INFORM_PATH.put(StepDownKeyContext.class, (action, pathMatcherTree, tree) -> {
            StepDownKeyContext<MatcherTree> thisTree = ((StepDownKeyContext<MatcherTree>) tree);
            int total = 0;
            for (int i : NUMBER_RANGE_VISITOR.visit(thisTree.numberRange())) {
                MatcherTree nextMatcherTree = pathMatcherTree.getOrCreateChild(KEY, i);
                total+= calculateInformPath(action, nextMatcherTree, thisTree.nextStep);
            }
            return total;
        });


        CALCULATE_INFORM_PATH.put(StepDownValueContext.class, (action, pathMatcherTree, tree) -> {
            StepDownValueContext<MatcherTree> thisTree = ((StepDownValueContext<MatcherTree>) tree);
            int total = 0;
            for (int i : NUMBER_RANGE_VISITOR.visit(thisTree.numberRange())) {
                MatcherTree nextMatcherTree = pathMatcherTree.getOrCreateChild(VALUE, i);
                total+= calculateInformPath(action, nextMatcherTree, thisTree.nextStep);
            }
            return total;
        });

        CALCULATE_INFORM_PATH.put(StepEqualsValueContext.class,         (action, pathMatcherTree, tree) -> {
            StepEqualsValueContext<MatcherTree> thisTree        = ((StepEqualsValueContext<MatcherTree>)tree);
            pathMatcherTree.addEqualsMatcherAction(thisTree.value.getText(), action);
            return 1;
        });

        CALCULATE_INFORM_PATH.put(StepStartsWithValueContext.class,     (action, pathMatcherTree, tree) -> {
            StepStartsWithValueContext<MatcherTree> thisTree        = ((StepStartsWithValueContext<MatcherTree>)tree);
            pathMatcherTree.addStartsWithMatcherAction(thisTree.value.getText(), action);
            return 1;
        });

        CALCULATE_INFORM_PATH.put(StepWordRangeContext.class,           (action, pathMatcherTree, tree) -> {
            StepWordRangeContext<MatcherTree> thisTree        = ((StepWordRangeContext<MatcherTree>)tree);
            Range            range           = WORD_RANGE_VISITOR.visit(thisTree.wordRange());
            pathMatcherTree.addWordRangeMatcherAction(range, action);
//            MatcherTree                       nextMatcherTree = pathMatcherTree.getOrCreateChild(WORDRANGE, 0);
//            nextMatcherTree.makeItWordRange(range.getFirst(), range.getLast());
            return 1; // FIXME: Unsure if this is correct
            //            return calculateInformPath(action, nextMatcherTree, thisTree.nextStep);
        });
    }

    private static int calculateInformPath(MatcherAction action, MatcherTree matcherTree, ParserRuleContext<MatcherTree> tree) {
        if (tree == null) {
            matcherTree.addMatcherAction(action);
//            action.matcher.informMeAbout(action, matcherTree);
            return 1;
        }

        CalculateInformPathFunction function = CALCULATE_INFORM_PATH.get(tree.getClass());
        if (function != null) {
            return function.calculateInformPath(action, matcherTree, tree);
        }

        matcherTree.addMatcherAction(action);
//        action.matcher.informMeAbout(action, matcherTree);
        return 1;
    }

    // ============================================================================================================

    public void reset() {
        matches.clear();
        if (verboseTemporary) {
            verbose = verbosePermanent;
        }
    }

    public MatchesList getMatches() {
        return matches;
    }
}
