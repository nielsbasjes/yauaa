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
import nl.basjes.parse.useragent.UserAgentTreeWalkerBaseVisitor;
import nl.basjes.parse.useragent.UserAgentTreeWalkerLexer;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser.StepContainsValueContext;
import nl.basjes.parse.useragent.analyze.treewalker.TreeExpressionEvaluator;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import static nl.basjes.parse.useragent.UserAgentTreeWalkerParser.BasePathContext;
import static nl.basjes.parse.useragent.UserAgentTreeWalkerParser.MatcherCleanVersionContext;
import static nl.basjes.parse.useragent.UserAgentTreeWalkerParser.MatcherContext;
import static nl.basjes.parse.useragent.UserAgentTreeWalkerParser.MatcherLookupContext;
import static nl.basjes.parse.useragent.UserAgentTreeWalkerParser.MatcherNextLookupContext;
import static nl.basjes.parse.useragent.UserAgentTreeWalkerParser.MatcherPathContext;
import static nl.basjes.parse.useragent.UserAgentTreeWalkerParser.MatcherPathIsNullContext;
import static nl.basjes.parse.useragent.UserAgentTreeWalkerParser.MatcherPathLookupContext;
import static nl.basjes.parse.useragent.UserAgentTreeWalkerParser.PathContext;
import static nl.basjes.parse.useragent.UserAgentTreeWalkerParser.PathFixedValueContext;
import static nl.basjes.parse.useragent.UserAgentTreeWalkerParser.PathNoWalkContext;
import static nl.basjes.parse.useragent.UserAgentTreeWalkerParser.PathWalkContext;
import static nl.basjes.parse.useragent.UserAgentTreeWalkerParser.StepDownContext;
import static nl.basjes.parse.useragent.UserAgentTreeWalkerParser.StepEndsWithValueContext;
import static nl.basjes.parse.useragent.UserAgentTreeWalkerParser.StepEqualsValueContext;
import static nl.basjes.parse.useragent.UserAgentTreeWalkerParser.StepFirstWordsContext;
import static nl.basjes.parse.useragent.UserAgentTreeWalkerParser.StepNotEqualsValueContext;
import static nl.basjes.parse.useragent.UserAgentTreeWalkerParser.StepSingleWordContext;
import static nl.basjes.parse.useragent.UserAgentTreeWalkerParser.StepStartsWithValueContext;

public abstract class MatcherAction {

    private String matchExpression;
    private MatcherContext requiredPattern;
    private NumberRangeVisitor numberRangeVisitor = new NumberRangeVisitor();
    private TreeExpressionEvaluator evaluator;

    TreeExpressionEvaluator getEvaluatorForUnitTesting() {
        return evaluator;
    }

    private static final Logger LOG = LoggerFactory.getLogger(MatcherAction.class);

    public class Match {
        String key;
        String value;
        ParseTree result;

        public Match(String key, String value, ParseTree result) {
            this.key = key;
            this.value = value;
            this.result = result;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public ParseTree getResult() {
            return result;
        }
    }

    private Matcher matcher;
    protected List<Match> matches;
    private boolean isFixedString;

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
        boolean hasAmbiguity;

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
            System.exit(-1); // VERY BRUTAL EXIT, TOO UNSAFE TO CONTINUE
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
            hasAmbiguity = true;
        }

        @Override
        public void reportAttemptingFullContext(
                Parser recognizer,
                DFA dfa,
                int startIndex,
                int stopIndex,
                BitSet conflictingAlts,
                ATNConfigSet configs) {
        }

        @Override
        public void reportContextSensitivity(
                Parser recognizer,
                DFA dfa,
                int startIndex,
                int stopIndex,
                int prediction,
                ATNConfigSet configs) {

        }
    }

    void init(String newMatchExpression, Matcher newMatcher) {
        this.matcher = newMatcher;
        this.matches = new ArrayList<>(16);
        this.isFixedString = false;
        this.matchExpression = newMatchExpression;
        setVerbose(newMatcher.getVerbose());

        InitErrorListener errorListener = new InitErrorListener();
        ANTLRInputStream input = new ANTLRInputStream(this.matchExpression);
        UserAgentTreeWalkerLexer lexer = new UserAgentTreeWalkerLexer(input);

        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        UserAgentTreeWalkerParser parser = new UserAgentTreeWalkerParser(tokens);

        parser.addErrorListener(errorListener);

//        parser.setTrace(true);
        requiredPattern = parser.matcher(); // parse

        // We couldn't ditch the double quotes around the fixed values in the parsing pase.
        // So we ditch them here. We simply walk the tree and modify some of the tokens.
        new UnQuoteValues().visit(requiredPattern);

        // Now we create an evaluator instance
        evaluator = new TreeExpressionEvaluator(requiredPattern, newMatcher.lookups);
        evaluator.setVerbose(verbose);

        // Is a fixed value (i.e. no events will ever be fired)?
        String fixedValue = evaluator.getFixedValue();
        if (fixedValue != null) {
            setFixedValue(fixedValue);
            isFixedString = true;
            return; // Not interested in any patterns
        }

        registerAllPossiblyInterestingPatterns();
    }

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
        matches.add(new Match(key, value, result));
        matcher.gotAStartingPoint();
    }

    protected abstract void inform(String key, String foundValue);

    /**
     * @return If it is impossible that this can be valid it returns false, else true.
     */
    public boolean canPossiblyBeValid() {
        return evaluator.usesIsNull() || isFixedString || !matches.isEmpty();
    }

    /**
     * Called after all nodes have been notified.
     *
     * @param userAgent The UserAgent instance in which the result must be placed.
     * @return true if the obtainResult result was valid. False will fail the entire matcher this belongs to.
     */
    public abstract boolean obtainResult(UserAgent userAgent);

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
        for (Match match : matches) {
            String matchedValue = evaluator.evaluate(match.result, match.key, match.value);
            if (matchedValue != null) {
                inform(match.key, matchedValue);
                break; // We always stick to the first match
            }
        }
    }


    // ============================================================================================================

    private void registerAllPossiblyInterestingPatterns() {
        if (requiredPattern == null) {
            throw new InvalidParserConfigurationException("NO pattern ?!?!?");
        }
        calculateInformPath("agent", requiredPattern);
    }

    // -----

    private void calculateInformPath(String treeName, MatcherContext tree) {
        if (tree instanceof MatcherNextLookupContext) {
            calculateInformPath(treeName, ((MatcherNextLookupContext) tree).matcherLookup());
            return;
        }
        if (tree instanceof MatcherCleanVersionContext){
            calculateInformPath(treeName, ((MatcherCleanVersionContext) tree).matcherLookup());
            return;
        }
        if (tree instanceof MatcherPathIsNullContext){
            calculateInformPath(treeName, ((MatcherPathIsNullContext) tree).matcherLookup());
        }
    }


    private void calculateInformPath(String treeName, MatcherLookupContext tree) {
        if (tree instanceof MatcherPathContext) {
            calculateInformPath(treeName, ((MatcherPathContext) tree).basePath());
            return;
        }
        if (tree instanceof MatcherPathLookupContext){
            calculateInformPath(treeName, ((MatcherPathLookupContext) tree).matcherLookup());
        }
    }

    // -----

    private void calculateInformPath(String treeName, BasePathContext tree) {
        // Useless to register a fixed value
//             case "PathFixedValueContext"         : calculateInformPath(treeName, (PathFixedValueContext)         tree); break;
        if (tree instanceof PathNoWalkContext) {
            matcher.informMeAbout(this, treeName);
            return;
        }
        if (tree instanceof PathWalkContext) {
            calculateInformPath(treeName, ((PathWalkContext) tree).nextStep);
        }
    }

    private void calculateInformPath(String treeName, PathContext tree) {
        if (tree != null) {
            if (tree instanceof StepDownContext){
                calculateInformPath(treeName, (StepDownContext) tree);
                return;
            }
            if (tree instanceof StepEqualsValueContext){
                calculateInformPath(treeName, (StepEqualsValueContext) tree);
                return;
            }
            if (tree instanceof StepFirstWordsContext){
                calculateInformPath(treeName, (StepFirstWordsContext) tree);
                return;
            }
            if (tree instanceof StepSingleWordContext){
                calculateInformPath(treeName, (StepSingleWordContext) tree);
                return;
            }
        }
        matcher.informMeAbout(this, treeName);
    }
    // -----

    private void calculateInformPath(String treeName, StepDownContext tree) {
        if (treeName.length() == 0) {
            calculateInformPath(treeName + '.' + tree.name.getText(), tree.nextStep);
        } else {
            for (Integer number : numberRangeVisitor.visit(tree.numberRange())) {
                calculateInformPath(treeName + '.' + "(" + number + ")" + tree.name.getText(), tree.nextStep);
            }
        }
    }

    private void calculateInformPath(String treeName, StepEqualsValueContext tree) {
        matcher.informMeAbout(this, treeName + "=\"" + tree.value.getText() + "\"");
    }

    private void calculateInformPath(String treeName, StepFirstWordsContext tree) {
        calculateInformPath(treeName + tree.FIRSTWORDS() + tree.NUMBER(), tree.nextStep);
    }

    private void calculateInformPath(String treeName, StepSingleWordContext tree) {
        calculateInformPath(treeName + tree.SINGLEWORD() + tree.NUMBER(), tree.nextStep);
    }

    // ============================================================================================================

    public void reset() {
        if (!matches.isEmpty()) {
            matches.clear();
        }
        if (verboseTemporary) {
            verbose = verbosePermanent;
        }
    }

    public List<Match> getMatches() {
        return matches;
    }
}
