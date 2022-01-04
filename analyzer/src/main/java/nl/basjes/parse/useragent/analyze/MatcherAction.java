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

import nl.basjes.parse.useragent.analyze.WordRangeVisitor.Range;
import nl.basjes.parse.useragent.analyze.treewalker.TreeExpressionEvaluator;
import nl.basjes.parse.useragent.analyze.treewalker.steps.WalkList.WalkResult;
import nl.basjes.parse.useragent.parse.AgentPathFragment;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerBaseVisitor;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerLexer;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.IsSyntaxErrorContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherBaseContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherConcatContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherConcatPostfixContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherConcatPrefixContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherDefaultIfNullContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherExtractBrandFromUrlContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherExtractContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherNormalizeBrandContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathIsInLookupContainsContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathIsInLookupContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathIsInLookupPrefixContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathIsNotInLookupContainsContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathIsNotInLookupPrefixContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathLookupContainsContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathLookupPrefixContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherReplaceStringContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherSegmentRangeContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherVariableContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherWordRangeContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.NumberRangeContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.PathContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.PathVariableContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepContainsValueContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepNotContainsValueContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownKeyvalueContext;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static nl.basjes.parse.useragent.UserAgent.SYNTAX_ERROR;
import static nl.basjes.parse.useragent.analyze.MatcherTree.createNewAgentRoot;
import static nl.basjes.parse.useragent.analyze.NumberRangeVisitor.NUMBER_RANGE_VISITOR;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherCleanVersionContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathIsNullContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathLookupContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.PathFixedValueContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.PathWalkContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownAgentContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownBase64Context;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownCommentsContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownEmailContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownEntryContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownKeyContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownNameContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownProductContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownTextContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownUrlContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownUuidContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownValueContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownVersionContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepEndsWithValueContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepEqualsValueContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepNotEqualsValueContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepStartsWithValueContext;
import static nl.basjes.parse.useragent.utils.AntlrUtils.NULL_PARSE_TREE;

public abstract class MatcherAction implements Serializable {

    private String matchExpression;
    TreeExpressionEvaluator evaluator;

    TreeExpressionEvaluator getEvaluatorForUnitTesting() {
        return evaluator;
    }

    private static final Logger LOG = LogManager.getLogger(MatcherAction.class);

    protected Matcher matcher;
    private MatchesList<MatcherTree> matches;
    protected boolean mustHaveMatches = false;

    boolean mustHaveMatches() {
        return mustHaveMatches;
    }

    public void destroy() {
        evaluator.destroy();
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

    public long initialize() {
        InitErrorListener errorListener = new InitErrorListener();

        CodePointCharStream input = CharStreams.fromString(this.matchExpression);
        UserAgentTreeWalkerLexer lexer = new UserAgentTreeWalkerLexer(input);

        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
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
            matches = new MatchesList<>(0);
            return 0; // Not interested in any patterns
        }

        // If this is a failIfFound we do not need any matches from the hashmap.
        if (this instanceof MatcherFailIfFoundAction) {
            mustHaveMatches = false;
        } else {
            mustHaveMatches = evaluator.mustHaveMatches();
        }

        MatcherTree matcherTree = createNewAgentRoot();

        int informs = calculateInformPath(this, matcherTree, requiredPattern);

        // If this is based on a variable we do not need any matches from the hashmap.
        if (mustHaveMatches && informs == 0) {
            mustHaveMatches = false;
        }

        int listSize = 0;
        if (informs > 0) {
            listSize = 1;
        }
        this.matches = new MatchesList<>(listSize);
        return informs;
    }

    protected abstract ParserRuleContext<MatcherTree> parseWalkerExpression(UserAgentTreeWalkerParser<MatcherTree> parser);

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
        public Void visitMatcherPathLookupContains(MatcherPathLookupContainsContext<MatcherTree> ctx) {
            unQuoteToken(ctx.defaultValue);
            return super.visitMatcherPathLookupContains(ctx);
        }

        @Override
        public Void visitMatcherPathLookupPrefix(MatcherPathLookupPrefixContext<MatcherTree> ctx) {
            unQuoteToken(ctx.defaultValue);
            return super.visitMatcherPathLookupPrefix(ctx);
        }

        @Override
        public Void visitMatcherDefaultIfNull(MatcherDefaultIfNullContext<MatcherTree> ctx) {
            unQuoteToken(ctx.defaultValue);
            return super.visitMatcherDefaultIfNull(ctx);
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

        @Override
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

        @Override
        public Void visitStepNotContainsValue(StepNotContainsValueContext<MatcherTree> ctx) {
            unQuoteToken(ctx.value);
            return super.visitStepNotContainsValue(ctx);
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
    public void inform(String key, String value, ParseTree<MatcherTree> result) {
        matcher.receivedInput();

        // Only if this needs input we tell the matcher on the first one.
        if (mustHaveMatches && matches.isEmpty()) {
            matcher.gotMyFirstStartingPoint();
        }
        matches.add(key, value, result);
    }

    protected abstract void inform(String key, WalkResult foundValue);

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
     *
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
        for (MatchesList.Match<MatcherTree> match : matches) {
            WalkResult matchedValue = evaluator.evaluate(match.getResult(), match.getKey(), match.getValue());
            if (matchedValue != null) {
                inform(match.getKey(), matchedValue);
                return; // We always stick to the first match
            }
        }

        if (isValidWithoutMatches()) {
            WalkResult matchedValue = evaluator.evaluate(NULL_PARSE_TREE, null, null);
            if (matchedValue != null) {
                inform(null, matchedValue);
            }
        }
    }

    // ============================================================================================================

    @FunctionalInterface
    private interface CalculateInformPathFunction {
        /**
         * Applies this function to the given arguments.
         *
         * @param action The applicable action.
         * @param matcherTree The name of the current tree.
         * @param tree The actual location in the parseTree
         * @return the number of informs done
         */
        int calculateInformPath(MatcherAction action, MatcherTree matcherTree, ParserRuleContext<MatcherTree> tree);
    }

    private static final Map<Class<?>, CalculateInformPathFunction> CALCULATE_INFORM_PATH = new HashMap<>();

    static {
        // -------------
        CALCULATE_INFORM_PATH.put(MatcherBaseContext.class,             (action, matcherTree, tree) ->
            calculateInformPath(action, matcherTree, ((MatcherBaseContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherPathIsNullContext.class,       (action, matcherTree, tree) ->
            calculateInformPath(action, matcherTree, ((MatcherPathIsNullContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(IsSyntaxErrorContext.class,           (action, matcherTree, tree) -> {
            action.matcher.informMeAbout(action, SYNTAX_ERROR + "=" + ((IsSyntaxErrorContext<MatcherTree>) tree).value.getText());
            return 1;
        });

        // -------------
        CALCULATE_INFORM_PATH.put(MatcherExtractContext.class,          (action, matcherTree, tree) ->
            calculateInformPath(action, matcherTree, ((MatcherExtractContext<MatcherTree>) tree).expression));

        // -------------
        CALCULATE_INFORM_PATH.put(MatcherVariableContext.class,         (action, matcherTree, tree) ->
            calculateInformPath(action, matcherTree, ((MatcherVariableContext<MatcherTree>) tree).expression));

        // -------------
        CALCULATE_INFORM_PATH.put(MatcherPathContext.class,             (action, matcherTree, tree) ->
            calculateInformPath(action, matcherTree, ((MatcherPathContext<MatcherTree>) tree).basePath()));
        CALCULATE_INFORM_PATH.put(MatcherConcatContext.class,           (action, matcherTree, tree) ->
            calculateInformPath(action, matcherTree, ((MatcherConcatContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherConcatPrefixContext.class,     (action, matcherTree, tree) ->
            calculateInformPath(action, matcherTree, ((MatcherConcatPrefixContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherConcatPostfixContext.class,    (action, matcherTree, tree) ->
            calculateInformPath(action, matcherTree, ((MatcherConcatPostfixContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherNormalizeBrandContext.class,   (action, matcherTree, tree) ->
            calculateInformPath(action, matcherTree, ((MatcherNormalizeBrandContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherExtractBrandFromUrlContext.class,   (action, matcherTree, tree) ->
            calculateInformPath(action, matcherTree, ((MatcherExtractBrandFromUrlContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherCleanVersionContext.class,     (action, matcherTree, tree) ->
            calculateInformPath(action, matcherTree, ((MatcherCleanVersionContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherReplaceStringContext.class,     (action, matcherTree, tree) ->
            calculateInformPath(action, matcherTree, ((MatcherReplaceStringContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherPathLookupContext.class,       (action, matcherTree, tree) ->
            calculateInformPath(action, matcherTree, ((MatcherPathLookupContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherPathIsInLookupContext.class,  (action, matcherTree, tree) ->
            calculateInformPath(action, matcherTree, ((MatcherPathIsInLookupContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherPathLookupContainsContext.class,      (action, matcherTree, tree) ->
            calculateInformPath(action, matcherTree, ((MatcherPathLookupContainsContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherPathIsNotInLookupContainsContext.class,  (action, treeName, tree) ->
            calculateInformPath(action, treeName, ((MatcherPathIsNotInLookupContainsContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherPathIsInLookupContainsContext.class,  (action, matcherTree, tree) ->
            calculateInformPath(action, matcherTree, ((MatcherPathIsInLookupContainsContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherPathLookupPrefixContext.class,        (action, matcherTree, tree) ->
            calculateInformPath(action, matcherTree, ((MatcherPathLookupPrefixContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherPathIsInLookupPrefixContext.class,    (action, matcherTree, tree) ->
            calculateInformPath(action, matcherTree, ((MatcherPathIsInLookupPrefixContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherPathIsNotInLookupPrefixContext.class,    (action, matcherTree, tree) ->
            calculateInformPath(action, matcherTree, ((MatcherPathIsNotInLookupPrefixContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH.put(MatcherDefaultIfNullContext.class,           (action, matcherTree, tree) ->
            calculateInformPath(action, matcherTree, ((MatcherDefaultIfNullContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH .put(MatcherWordRangeContext.class,        (action, matcherTree, tree) ->
            calculateInformPath(action, matcherTree, ((MatcherWordRangeContext<MatcherTree>) tree).matcher()));
        CALCULATE_INFORM_PATH .put(MatcherSegmentRangeContext.class,        (action, matcherTree, tree) ->
            calculateInformPath(action, matcherTree, ((MatcherSegmentRangeContext<MatcherTree>) tree).matcher()));

        // -------------
        CALCULATE_INFORM_PATH.put(PathVariableContext.class,            (action, matcherTree, tree) -> {
            action.matcher.informMeAboutVariable(action, ((PathVariableContext<MatcherTree>) tree).variable.getText());
            return 0;
        });

        CALCULATE_INFORM_PATH.put(PathWalkContext.class,                (action, matcherTree, tree) ->
            calculateInformPath(action, matcherTree, ((PathWalkContext<MatcherTree>) tree).nextStep));

        // -------------
        CALCULATE_INFORM_PATH.put(StepDownAgentContext.class, (action, matcherTree, tree) -> {
            StepDownAgentContext<MatcherTree> thisTree = ((StepDownAgentContext<MatcherTree>) tree);
            return calculateInformDownPath(action, matcherTree, thisTree.numberRange(), AgentPathFragment.AGENT, thisTree.nextStep);
        });

        CALCULATE_INFORM_PATH.put(StepDownProductContext.class, (action, matcherTree, tree) -> {
            StepDownProductContext<MatcherTree> thisTree = ((StepDownProductContext<MatcherTree>) tree);
            return calculateInformDownPath(action, matcherTree, thisTree.numberRange(), AgentPathFragment.PRODUCT, thisTree.nextStep);
        });

        CALCULATE_INFORM_PATH.put(StepDownNameContext.class, (action, matcherTree, tree) -> {
            StepDownNameContext<MatcherTree> thisTree = ((StepDownNameContext<MatcherTree>) tree);
            return calculateInformDownPath(action, matcherTree, thisTree.numberRange(), AgentPathFragment.NAME, thisTree.nextStep);
        });

        CALCULATE_INFORM_PATH.put(StepDownVersionContext.class, (action, matcherTree, tree) -> {
            StepDownVersionContext<MatcherTree> thisTree = ((StepDownVersionContext<MatcherTree>) tree);
            return calculateInformDownPath(action, matcherTree, thisTree.numberRange(), AgentPathFragment.VERSION, thisTree.nextStep);
        });

        CALCULATE_INFORM_PATH.put(StepDownCommentsContext.class, (action, matcherTree, tree) -> {
            StepDownCommentsContext<MatcherTree> thisTree = ((StepDownCommentsContext<MatcherTree>) tree);
            return calculateInformDownPath(action, matcherTree, thisTree.numberRange(), AgentPathFragment.COMMENTS, thisTree.nextStep);
        });

        CALCULATE_INFORM_PATH.put(StepDownEntryContext.class, (action, matcherTree, tree) -> {
            StepDownEntryContext<MatcherTree> thisTree = ((StepDownEntryContext<MatcherTree>) tree);
            return calculateInformDownPath(action, matcherTree, thisTree.numberRange(), AgentPathFragment.ENTRY, thisTree.nextStep);
        });

        CALCULATE_INFORM_PATH.put(StepDownTextContext.class, (action, matcherTree, tree) -> {
            StepDownTextContext<MatcherTree> thisTree = ((StepDownTextContext<MatcherTree>) tree);
            return calculateInformDownPath(action, matcherTree, thisTree.numberRange(), AgentPathFragment.TEXT, thisTree.nextStep);
        });

        CALCULATE_INFORM_PATH.put(StepDownUrlContext.class, (action, matcherTree, tree) -> {
            StepDownUrlContext<MatcherTree> thisTree = ((StepDownUrlContext<MatcherTree>) tree);
            return calculateInformDownPath(action, matcherTree, thisTree.numberRange(), AgentPathFragment.URL, thisTree.nextStep);
        });

        CALCULATE_INFORM_PATH.put(StepDownEmailContext.class, (action, matcherTree, tree) -> {
            StepDownEmailContext<MatcherTree> thisTree = ((StepDownEmailContext<MatcherTree>) tree);
            return calculateInformDownPath(action, matcherTree, thisTree.numberRange(), AgentPathFragment.EMAIL, thisTree.nextStep);
        });

        CALCULATE_INFORM_PATH.put(StepDownBase64Context.class, (action, matcherTree, tree) -> {
            StepDownBase64Context<MatcherTree> thisTree = ((StepDownBase64Context<MatcherTree>) tree);
            return calculateInformDownPath(action, matcherTree, thisTree.numberRange(), AgentPathFragment.BASE64, thisTree.nextStep);
        });

        CALCULATE_INFORM_PATH.put(StepDownUuidContext.class, (action, matcherTree, tree) -> {
            StepDownUuidContext<MatcherTree> thisTree = ((StepDownUuidContext<MatcherTree>) tree);
            return calculateInformDownPath(action, matcherTree, thisTree.numberRange(), AgentPathFragment.UUID, thisTree.nextStep);
        });

        CALCULATE_INFORM_PATH.put(StepDownKeyvalueContext.class, (action, matcherTree, tree) -> {
            StepDownKeyvalueContext<MatcherTree> thisTree = ((StepDownKeyvalueContext<MatcherTree>) tree);
            return calculateInformDownPath(action, matcherTree, thisTree.numberRange(), AgentPathFragment.KEYVALUE, thisTree.nextStep);
        });

        CALCULATE_INFORM_PATH.put(StepDownKeyContext.class, (action, matcherTree, tree) -> {
            StepDownKeyContext<MatcherTree> thisTree = ((StepDownKeyContext<MatcherTree>) tree);
            return calculateInformDownPath(action, matcherTree, thisTree.numberRange(), AgentPathFragment.KEY, thisTree.nextStep);
        });

        CALCULATE_INFORM_PATH.put(StepDownValueContext.class, (action, matcherTree, tree) -> {
            StepDownValueContext<MatcherTree> thisTree = ((StepDownValueContext<MatcherTree>) tree);
            return calculateInformDownPath(action, matcherTree, thisTree.numberRange(), AgentPathFragment.VALUE, thisTree.nextStep);
        });

        CALCULATE_INFORM_PATH.put(StepEqualsValueContext.class, (action, matcherTree, tree) -> {
            StepEqualsValueContext<MatcherTree> thisTree = ((StepEqualsValueContext<MatcherTree>) tree);
            action.matcher.informMeAbout(action, matcherTree + "=\"" + thisTree.value.getText() + "\"");
            return 1;
        });

        CALCULATE_INFORM_PATH.put(StepStartsWithValueContext.class, (action, matcherTree, tree) -> {
            StepStartsWithValueContext<MatcherTree> thisTree = ((StepStartsWithValueContext<MatcherTree>) tree);
            action.matcher.informMeAboutPrefix(action, matcherTree.toString(), thisTree.value.getText());
            return 1;
        });

        CALCULATE_INFORM_PATH.put(StepWordRangeContext.class, (action, matcherTree, tree) -> {
            StepWordRangeContext<MatcherTree> thisTree = ((StepWordRangeContext<MatcherTree>) tree);
            Range range = WordRangeVisitor.getRange(thisTree.wordRange());
            action.matcher.lookingForRange(matcherTree.toString(), range);
            matcherTree.addWordRangeMatcherAction(range, action);
            return calculateInformPath(action, matcherTree, thisTree.nextStep);
//            return calculateInformPath(action, matcherTree + range, thisTree.nextStep);
        });
    }

    private static int calculateInformDownPath(MatcherAction action, MatcherTree matcherTree, NumberRangeContext<MatcherTree> numberRangeContext, AgentPathFragment agent, PathContext<MatcherTree> nextStep) {
        int informs = 0;
        for (int number : NUMBER_RANGE_VISITOR.visit(numberRangeContext)) {
//          FIXME: THIS IS BROKEN
            informs += 1; //calculateInformPath(action, matcherTree + '.' + "(" + number + ")" + agent, nextStep);
        }
        return informs;
    }

    private static int calculateInformPath(MatcherAction action, MatcherTree matcherTree, ParserRuleContext<MatcherTree> tree) {
        if (tree == null) {
            action.matcher.informMeAbout(action, matcherTree.toString());
            return 1;
        }

        CalculateInformPathFunction function = CALCULATE_INFORM_PATH.get(tree.getClass());
        if (function != null) {
            return function.calculateInformPath(action, matcherTree, tree);
        }

        action.matcher.informMeAbout(action, matcherTree.toString());
        return 1;
    }

    // ============================================================================================================

    public void reset() {
        matches.clear();
        if (verboseTemporary) {
            verbose = verbosePermanent;
        }
    }

    public MatchesList<MatcherTree> getMatches() {
        return matches;
    }

    @Override
    public String toString() {
        return "MatcherAction{" +
            "matchExpression='" + matchExpression + '\'' +
            ", evaluator=" + evaluator +
//            ", matcher=" + matcher +
            ", matches=" + matches +
            ", mustHaveMatches=" + mustHaveMatches +
            ", verbose=" + verbose +
            ", verbosePermanent=" + verbosePermanent +
            ", verboseTemporary=" + verboseTemporary +
            '}';
    }
}
