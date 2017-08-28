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

import nl.basjes.parse.useragent.analyze.WordRangeVisitor.Range;
import nl.basjes.parse.useragent.analyze.treewalker.TreeExpressionEvaluator;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerBaseVisitor;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerLexer;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherBaseContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherNormalizeBrandContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherRequireContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherWordRangeContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepContainsValueContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepWordRangeContext;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Collection;

import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.BasePathContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherCleanVersionContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathIsNullContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathLookupContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.PathContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.PathFixedValueContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.PathWalkContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepEndsWithValueContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepEqualsValueContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepNotEqualsValueContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepStartsWithValueContext;

public abstract class MatcherAction implements Serializable {

    private String matchExpression;
    private static final NumberRangeVisitor NUMBER_RANGE_VISITOR = new NumberRangeVisitor();
    private TreeExpressionEvaluator evaluator;

    TreeExpressionEvaluator getEvaluatorForUnitTesting() {
        return evaluator;
    }

    private static final Logger LOG = LoggerFactory.getLogger(MatcherAction.class);


    private Matcher matcher;
    private MatchesList matches;
    private boolean mustHaveMatches = false;

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

    class InitErrorListener implements ANTLRErrorListener {
        @Override
        public void syntaxError(
                Recognizer<?, ?> recognizer,
                Object offendingSymbol,
                int line,
                int charPositionInLine,
                String msg,
                RecognitionException e) {
            LOG.error("Syntax error");
            LOG.error("Source : {}", matchExpression);
            LOG.error("Message: {}", msg);
            throw new InvalidParserConfigurationException("Syntax error \"" + msg + "\" caused by \"" + matchExpression + "\".");
        }

        @Override
        public void reportAmbiguity(
                Parser recognizer,
                DFA dfa,
                int startIndex,
                int stopIndex,
                boolean exact,
                BitSet ambigAlts,
                ATNConfigSet configs) {
            // Ignore this type of problem
        }

        @Override
        public void reportAttemptingFullContext(
                Parser recognizer,
                DFA dfa,
                int startIndex,
                int stopIndex,
                BitSet conflictingAlts,
                ATNConfigSet configs) {
            // Ignore this type of problem
        }

        @Override
        public void reportContextSensitivity(
                Parser recognizer,
                DFA dfa,
                int startIndex,
                int stopIndex,
                int prediction,
                ATNConfigSet configs) {
            // Ignore this type of problem
        }
    }

    void init(String newMatchExpression, Matcher newMatcher) {
        this.matcher = newMatcher;
        this.matchExpression = newMatchExpression;
        setVerbose(newMatcher.getVerbose());

        InitErrorListener errorListener = new InitErrorListener();

        CodePointCharStream input = CharStreams.fromString(this.matchExpression);
        UserAgentTreeWalkerLexer lexer = new UserAgentTreeWalkerLexer(input);

        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        UserAgentTreeWalkerParser parser = new UserAgentTreeWalkerParser(tokens);

        parser.addErrorListener(errorListener);

//        parser.setTrace(true);
        ParserRuleContext requiredPattern = parseWalkerExpression(parser);

        if (requiredPattern == null) {
            throw new InvalidParserConfigurationException("NO pattern ?!?!?");
        }

        // We couldn't ditch the double quotes around the fixed values in the parsing pase.
        // So we ditch them here. We simply walk the tree and modify some of the tokens.
        new UnQuoteValues().visit(requiredPattern);

        // Now we create an evaluator instance
        evaluator = new TreeExpressionEvaluator(requiredPattern, matcher.lookups, verbose);

        // Is a fixed value (i.e. no events will ever be fired)?
        String fixedValue = evaluator.getFixedValue();
        if (fixedValue != null) {
            setFixedValue(fixedValue);
            mustHaveMatches = false;
            matches = new MatchesList(0);
            return; // Not interested in any patterns
        }

        mustHaveMatches = !evaluator.usesIsNull();

        int informs = calculateInformPath("agent", requiredPattern);

        int listSize = 1;
        if (informs > 5) {
            listSize = 3;
        }
        this.matches = new MatchesList(listSize);
    }

    protected abstract ParserRuleContext parseWalkerExpression(UserAgentTreeWalkerParser parser);

    private static class UnQuoteValues extends UserAgentTreeWalkerBaseVisitor<Void> {
        private void unQuoteToken(Token token) {
            if (token instanceof CommonToken) {
                CommonToken commonToken = (CommonToken) token;
                commonToken.setStartIndex(commonToken.getStartIndex() + 1);
                commonToken.setStopIndex(commonToken.getStopIndex() - 1);
            }
        }

        @Override
        public Void visitMatcherPathLookup(MatcherPathLookupContext ctx) {
            unQuoteToken(ctx.defaultValue);
            return super.visitMatcherPathLookup(ctx);
        }

        @Override
        public Void visitPathFixedValue(PathFixedValueContext ctx) {
            unQuoteToken(ctx.value);
            return super.visitPathFixedValue(ctx);
        }

        @Override
        public Void visitStepEqualsValue(StepEqualsValueContext ctx) {
            unQuoteToken(ctx.value);
            return super.visitStepEqualsValue(ctx);
        }

        @Override
        public Void visitStepNotEqualsValue(StepNotEqualsValueContext ctx) {
            unQuoteToken(ctx.value);
            return super.visitStepNotEqualsValue(ctx);
        }

        @Override
        public Void visitStepStartsWithValue(StepStartsWithValueContext ctx) {
            unQuoteToken(ctx.value);
            return super.visitStepStartsWithValue(ctx);
        }

        @Override
        public Void visitStepEndsWithValue(StepEndsWithValueContext ctx) {
            unQuoteToken(ctx.value);
            return super.visitStepEndsWithValue(ctx);
        }

        @Override
        public Void visitStepContainsValue(StepContainsValueContext ctx) {
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
     * @param result The node in the parser tree where the match occurred
     */
    public void inform(String key, String value, ParseTree result) {
        // Only if this needs input we tell the matcher on the first one.
        if (mustHaveMatches && matches.isEmpty()) {
            matcher.gotMyFirstStartingPoint();
        }
        matches.add(key, value, result);
    }

    protected abstract void inform(String key, String foundValue);

    /**
     * @return If it is impossible that this can be valid it returns true, else false.
     */
    boolean cannotBeValid() {
        return mustHaveMatches && matches.isEmpty();
    }

    /**
     * Called after all nodes have been notified.
     * @return true if the obtainResult result was valid. False will fail the entire matcher this belongs to.
     */
    public abstract boolean obtainResult();

    boolean isValidIsNull() {
        if (matches.isEmpty()) {
            if (evaluator.usesIsNull()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Optimization: Only if there is a possibility that all actions for this matcher CAN be valid do we
     * actually perform the analysis and do the (expensive) tree walking and matching.
     */
    void processInformedMatches() {
        for (MatchesList.Match match : matches) {
            String matchedValue = evaluator.evaluate(match.result, match.key, match.value);
            if (matchedValue != null) {
                inform(match.key, matchedValue);
                break; // We always stick to the first match
            }
        }
    }

    // ============================================================================================================

    // -----
    private int calculateInformPath(@SuppressWarnings("SameParameterValue") String treeName, ParserRuleContext tree) {
        if (tree instanceof MatcherRequireContext) {
            return calculateInformPath(treeName, ((MatcherRequireContext) tree));
        }
        if (tree instanceof MatcherContext){
            return calculateInformPath(treeName, ((MatcherContext) tree));
        }
        return 0;
    }

    private int calculateInformPath(String treeName, MatcherRequireContext tree) {
        if (tree instanceof MatcherBaseContext) {
            return calculateInformPath(treeName, ((MatcherBaseContext) tree).matcher());
        }
        if (tree instanceof MatcherPathIsNullContext){
            return calculateInformPath(treeName, ((MatcherPathIsNullContext) tree).matcher());
        }
        return 0;
    }

    private int calculateInformPath(String treeName, MatcherContext tree) {
        if (tree instanceof MatcherPathContext) {
            return calculateInformPath(treeName, ((MatcherPathContext) tree).basePath());
        }
        if (tree instanceof MatcherCleanVersionContext){
            return calculateInformPath(treeName, ((MatcherCleanVersionContext) tree).matcher());
        }
        if (tree instanceof MatcherNormalizeBrandContext){
            return calculateInformPath(treeName, ((MatcherNormalizeBrandContext) tree).matcher());
        }
        if (tree instanceof MatcherPathLookupContext){
            return calculateInformPath(treeName, ((MatcherPathLookupContext) tree).matcher());
        }
        if (tree instanceof MatcherWordRangeContext){
            return calculateInformPath(treeName, ((MatcherWordRangeContext) tree).matcher());
        }
        return 0;
    }

    // -----

    private int calculateInformPath(String treeName, BasePathContext tree) {
        // Useless to register a fixed value
//             case "PathFixedValueContext"         : calculateInformPath(treeName, (PathFixedValueContext)         tree); break;
        if (tree instanceof PathWalkContext) {
            return calculateInformPath(treeName, ((PathWalkContext) tree).nextStep);
        }
        return 0;
    }

    private int calculateInformPath(String treeName, PathContext tree) {
        if (tree != null) {
            if (tree instanceof StepDownContext){
                return calculateInformPath(treeName, (StepDownContext) tree);
            }
            if (tree instanceof StepEqualsValueContext){
                return calculateInformPath(treeName, (StepEqualsValueContext) tree);
            }
            if (tree instanceof StepWordRangeContext) {
                return calculateInformPath(treeName, (StepWordRangeContext) tree);
            }
        }
        matcher.informMeAbout(this, treeName);
        return 1;
    }
    // -----

    private int calculateInformPath(String treeName, StepDownContext tree) {
        if (treeName.length() == 0) {
            return calculateInformPath(treeName + '.' + tree.name.getText(), tree.nextStep);
        }

        int informs = 0;
        for (Integer number : NUMBER_RANGE_VISITOR.visit(tree.numberRange())) {
            informs += calculateInformPath(treeName + '.' + "(" + number + ")" + tree.name.getText(), tree.nextStep);
        }
        return informs;
    }

    private int calculateInformPath(String treeName, StepEqualsValueContext tree) {
        matcher.informMeAbout(this, treeName + "=\"" + tree.value.getText() + "\"");
        return 1;
    }

    private int calculateInformPath(String treeName, StepWordRangeContext tree) {
        Range range = WordRangeVisitor.getRange(tree.wordRange());
        matcher.lookingForRange(treeName, range);
        return calculateInformPath(treeName + "[" + range.first + "-" + range.last + "]", tree.nextStep);
    }

    // ============================================================================================================

    public void reset() {
        matches.clear();
        if (verboseTemporary) {
            verbose = verbosePermanent;
        }
    }

    public Collection<MatchesList.Match> getMatches() {
        return matches;
    }
}
