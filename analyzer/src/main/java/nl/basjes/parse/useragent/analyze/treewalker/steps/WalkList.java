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

package nl.basjes.parse.useragent.analyze.treewalker.steps;

import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import nl.basjes.parse.useragent.analyze.WordRangeVisitor;
import nl.basjes.parse.useragent.analyze.WordRangeVisitor.Range;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepContains;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepDefaultIfNull;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepEndsWith;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepEquals;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepIsInSet;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepIsNotInSet;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepIsNull;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepNotContains;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepNotEquals;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepStartsWith;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepIsInLookupContains;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepIsInLookupPrefix;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepIsNotInLookupContains;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepIsNotInLookupPrefix;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepLookup;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepLookupContains;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepLookupPrefix;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepBackToFull;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepCleanVersion;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepConcat;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepConcatPostfix;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepConcatPrefix;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepExtractBrandFromUrl;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepNormalizeBrand;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepReplaceString;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepSegmentRange;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepWordRange;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepDown;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepNext;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepNextN;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepPrev;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepPrevN;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepUp;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerBaseVisitor;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherCleanVersionContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherConcatContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherConcatPostfixContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherConcatPrefixContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherDefaultIfNullContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherExtractBrandFromUrlContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherNormalizeBrandContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathIsInLookupContainsContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathIsInLookupContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathIsInLookupPrefixContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathIsNotInLookupContainsContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathIsNotInLookupPrefixContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathIsNullContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathLookupContainsContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathLookupContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherPathLookupPrefixContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherReplaceStringContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherSegmentRangeContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.PathContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.PathVariableContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.PathWalkContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepBackToFullContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepContainsValueContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepEndsWithValueContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepEqualsValueContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepIsInSetContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepIsNotInSetContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepNext2Context;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepNext3Context;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepNext4Context;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepNextContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepNotContainsValueContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepNotEqualsValueContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepPrev2Context;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepPrev3Context;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepPrev4Context;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepPrevContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepStartsWithValueContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepUpContext;
import nl.basjes.parse.useragent.utils.AntlrUtils;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static nl.basjes.parse.useragent.analyze.UserAgentStringMatchMaker.MAX_PREFIX_HASH_MATCH;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.MatcherWordRangeContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepWordRangeContext;

public class WalkList implements Serializable {
    private static final Logger LOG = LogManager.getLogger(WalkList.class);

    private final Map<String, Map<String, String>> lookups;
    private final Map<String, Set<String>>         lookupSets;
    private final List<Step> steps = new ArrayList<>();

    private final boolean verbose;

    public static class WalkResult {
        private final ParseTree tree;
        private final String value;

        public WalkResult(ParseTree tree, String value) {
            this.tree = tree;
            this.value = value;
        }

        public ParseTree getTree() {
            return tree;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "WalkResult{" +
                "tree=" + (tree == null ? ">>>NULL<<<" : tree.getText()) +
                ", value=" + (value == null ? ">>>NULL<<<" : '\'' + value + '\'') +
                '}';
        }
    }

    @SuppressWarnings("unused") // Private constructor for serialization systems ONLY (like Kryo)
    private WalkList() {
        lookups = Collections.emptyMap();
        lookupSets = Collections.emptyMap();
        verbose = false;
    }

    public WalkList(ParserRuleContext requiredPattern,
                    Map<String, Map<String, String>> lookups,
                    Map<String, Set<String>> lookupSets,
                    boolean verbose) {
        this.lookups = lookups;
        this.lookupSets = lookupSets;
        this.verbose = verbose;
        // Generate the walkList from the requiredPattern
        new WalkListBuilder().visit(requiredPattern);
        linkSteps();

        int i = 1;
        if (verbose) {
            LOG.info("------------------------------------");
            LOG.info("Required: {}", requiredPattern.getText());
            for (Step step : steps) {
                step.setVerbose(true);
                LOG.info("{}: {}", i++, step);
            }
        }
    }

    public void destroy() {
        steps.forEach(Step::destroy);
        steps.clear();
        lookups.clear();
        lookupSets.clear();
    }

    public boolean isEmpty() {
        return steps.isEmpty();
    }

    private void linkSteps() {
        Step nextStep = null;
        for (int i = steps.size() - 1; i >= 0; i--) {
            Step current = steps.get(i);
            current.setNextStep(i, nextStep);
            nextStep = current;
        }
    }

    public long pruneTrailingStepsThatCannotFail() {
        int lastStepThatCannotFail = Integer.MAX_VALUE;
        for (int i = steps.size() - 1; i >= 0; i--) {
            Step current = steps.get(i);
            if (current.canFail()) {
                break; // We're done. We have the last step that CAN fail.
            }
            lastStepThatCannotFail = i;
        }

        if (lastStepThatCannotFail == Integer.MAX_VALUE) {
            return 0;
        }
        if (lastStepThatCannotFail == 0) {
            long prunedSteps = steps.size();
            steps.clear();
            return prunedSteps;
        }

        int lastRelevantStepIndex = lastStepThatCannotFail - 1;
        Step lastRelevantStep = steps.get(lastRelevantStepIndex);
        lastRelevantStep.setNextStep(lastRelevantStepIndex, null);

        steps.subList(lastRelevantStepIndex + 1, steps.size()).clear();
        return ((long)steps.size()) - (lastRelevantStepIndex + 1);
    }

    public WalkResult walk(@Nonnull ParseTree tree, @Nullable String value) {
        if (steps.isEmpty()) {
            return new WalkResult(tree, value);
        }
        Step firstStep = steps.get(0);
        if (verbose) {
            Step.LOG.info("Tree: >>>{}<<<", AntlrUtils.getSourceText((ParserRuleContext)tree));
            Step.LOG.info("Enter step: {}", firstStep);
        }
        WalkResult result = firstStep.walk(tree, value);
        if (verbose) {
            Step.LOG.info("Leave step ({}): {}", result == null ? "-" : "+", firstStep);
        }
        return result;
    }

    public Step getFirstStep() {
        return steps.isEmpty() ? null : steps.get(0);
    }

    private Boolean mustHaveMatches = null;

    public boolean mustHaveMatches() {
        if (mustHaveMatches != null) {
            return mustHaveMatches;
        }

        mustHaveMatches = true;

        Step step = getFirstStep();
        while (step != null) {
            if (!step.mustHaveInput()) {
                mustHaveMatches = false;
                break;
            }
            step = step.getNextStep();
        }
        return mustHaveMatches;
    }

    @Override
    public String toString() {
        if (steps.isEmpty()) {
            return "Empty";
        }
        StringBuilder sb = new StringBuilder(128);
        for (Step step : steps) {
            sb.append(" --> ").append(step);
        }
        return sb.toString();
    }

    private class WalkListBuilder extends UserAgentTreeWalkerBaseVisitor<Void> {

        // Because we are jumping in 'mid way' we need to skip creating steps until that point.
        boolean foundHashEntryPoint = false;

        private void fromHereItCannotBeInHashMapAnymore() {
            foundHashEntryPoint = true;
        }

        private boolean stillGoingToHashMap() {
            return !foundHashEntryPoint;
        }

        private void add(Step step) {
            if (foundHashEntryPoint) {
                steps.add(step);
            }
        }

        private void visitNext(PathContext nextStep) {
            if (nextStep != null) {
                visit(nextStep);
            }
        }

        @Override
        public Void visitMatcherPath(MatcherPathContext ctx) {
            visit(ctx.basePath());
            return null; // Void
        }

        private String extractText(Token token) {
            if (token == null) {
                return null;
            }
            return token.getText();
        }

        @Override
        public Void visitMatcherPathLookup(MatcherPathLookupContext ctx) {
            visit(ctx.matcher());

            fromHereItCannotBeInHashMapAnymore();

            String lookupName = ctx.lookup.getText();
            Map<String, String> lookup = getLookup(lookupName);

            add(new StepLookup(lookupName, lookup, extractText(ctx.defaultValue)));
            return null; // Void
        }

        @Override
        public Void visitMatcherPathIsInLookup(MatcherPathIsInLookupContext ctx) {
            visit(ctx.matcher());

            fromHereItCannotBeInHashMapAnymore();

            String lookupName = ctx.lookup.getText();
            Map<String, String> lookup = getLookup(lookupName);

            // No need to write new code for essentially the same in a different syntax
            add(new StepIsInSet(lookupName, new LinkedHashSet<>(lookup.keySet())));
            return null; // Void
        }

        @Override
        public Void visitMatcherPathLookupContains(MatcherPathLookupContainsContext ctx) {
            visit(ctx.matcher());

            fromHereItCannotBeInHashMapAnymore();

            String lookupName = ctx.lookup.getText();
            Map<String, String> lookup = getLookup(lookupName);

            add(new StepLookupContains(lookupName, lookup, extractText(ctx.defaultValue)));
            return null; // Void
        }

        @Override
        public Void visitMatcherPathLookupPrefix(MatcherPathLookupPrefixContext ctx) {
            visit(ctx.matcher());

            fromHereItCannotBeInHashMapAnymore();

            String lookupName = ctx.lookup.getText();
            Map<String, String> lookup = getLookup(lookupName);

            add(new StepLookupPrefix(lookupName, lookup, extractText(ctx.defaultValue)));
            return null; // Void
        }


        @Override
        public Void visitMatcherPathIsInLookupContains(MatcherPathIsInLookupContainsContext ctx) {
            visit(ctx.matcher());

            fromHereItCannotBeInHashMapAnymore();

            String lookupName = ctx.lookup.getText();
            Map<String, String> lookup = getLookup(lookupName);

            add(new StepIsInLookupContains(lookupName, lookup));
            return null; // Void
        }


        @Override
        public Void visitMatcherPathIsNotInLookupContains(MatcherPathIsNotInLookupContainsContext ctx) {
            visit(ctx.matcher());

            fromHereItCannotBeInHashMapAnymore();

            String lookupName = ctx.lookup.getText();
            Map<String, String> lookup = getLookup(lookupName);

            add(new StepIsNotInLookupContains(lookupName, lookup));
            return null; // Void
        }


        @Override
        public Void visitMatcherPathIsInLookupPrefix(MatcherPathIsInLookupPrefixContext ctx) {
            visit(ctx.matcher());

            fromHereItCannotBeInHashMapAnymore();

            String lookupName = ctx.lookup.getText();
            Map<String, String> lookup = getLookup(lookupName);

            add(new StepIsInLookupPrefix(lookupName, lookup));
            return null; // Void
        }

        @Override
        public Void visitMatcherPathIsNotInLookupPrefix(MatcherPathIsNotInLookupPrefixContext ctx) {
            visit(ctx.matcher());

            fromHereItCannotBeInHashMapAnymore();

            String              lookupName = ctx.lookup.getText();
            Set<String>         lookupSet  = lookupSets.get(lookupName);
            if (lookupSet != null) {
                add(new StepIsNotInLookupPrefix(lookupName, lookupSet));
            } else {
                Map<String, String> lookup     = lookups.get(lookupName);
                if (lookup != null) {
                    add(new StepIsNotInLookupPrefix(lookupName, lookup));
                } else {
                    throw new InvalidParserConfigurationException("Missing lookup/set \"" + lookupName + "\" ");
                }
            }

            return null; // Void
        }

        @Override
        public Void visitMatcherDefaultIfNull(MatcherDefaultIfNullContext ctx) {
            // Always add this one, it's special
            steps.add(new StepDefaultIfNull(ctx.defaultValue.getText()));
            visit(ctx.matcher());
            return null; // Void
        }

        private Map<String, String> getLookup(String lookupName) {
            Map<String, String> lookup = lookups.get(lookupName);
            if (lookup == null) {
                throw new InvalidParserConfigurationException("Missing lookup \"" + lookupName + "\" ");
            }
            return lookup;
        }

        @Override
        public Void visitMatcherCleanVersion(MatcherCleanVersionContext ctx) {
            visit(ctx.matcher());
            fromHereItCannotBeInHashMapAnymore();
            add(new StepCleanVersion());
            return null; // Void
        }

        @Override
        public Void visitMatcherReplaceString(MatcherReplaceStringContext ctx) {
            visit(ctx.matcher());
            fromHereItCannotBeInHashMapAnymore();
            add(new StepReplaceString(ctx.search.getText(), ctx.replace.getText()));
            return null; // Void
        }

        @Override
        public Void visitMatcherNormalizeBrand(MatcherNormalizeBrandContext ctx) {
            visit(ctx.matcher());
            fromHereItCannotBeInHashMapAnymore();
            add(new StepNormalizeBrand());
            return null; // Void
        }

        @Override
        public Void visitMatcherExtractBrandFromUrl(MatcherExtractBrandFromUrlContext ctx) {
            visit(ctx.matcher());
            fromHereItCannotBeInHashMapAnymore();
            add(new StepExtractBrandFromUrl());
            return null; // Void
        }

        @Override
        public Void visitMatcherConcat(MatcherConcatContext ctx) {
            visit(ctx.matcher());
            fromHereItCannotBeInHashMapAnymore();
            add(new StepConcat(ctx.prefix.getText(), ctx.postfix.getText()));
            return null; // Void
        }

        @Override
        public Void visitMatcherConcatPrefix(MatcherConcatPrefixContext ctx) {
            visit(ctx.matcher());
            fromHereItCannotBeInHashMapAnymore();
            add(new StepConcatPrefix(ctx.prefix.getText()));
            return null; // Void
        }

        @Override
        public Void visitMatcherConcatPostfix(MatcherConcatPostfixContext ctx) {
            visit(ctx.matcher());
            fromHereItCannotBeInHashMapAnymore();
            add(new StepConcatPostfix(ctx.postfix.getText()));
            return null; // Void
        }

        @Override
        public Void visitMatcherWordRange(MatcherWordRangeContext ctx) {
            visit(ctx.matcher());
            fromHereItCannotBeInHashMapAnymore();
            add(new StepWordRange(WordRangeVisitor.getRange(ctx.wordRange())));
            return null; // Void
        }

        @Override
        public Void visitMatcherSegmentRange(MatcherSegmentRangeContext ctx) {
            visit(ctx.matcher());
            fromHereItCannotBeInHashMapAnymore();
            add(new StepSegmentRange(WordRangeVisitor.getRange(ctx.wordRange())));
            return null; // Void
        }

        @Override
        public Void visitMatcherPathIsNull(MatcherPathIsNullContext ctx) {
            // Always add this one, it's special
            steps.add(new StepIsNull());
            visit(ctx.matcher());
            return null; // Void
        }

        @Override
        public Void visitPathVariable(PathVariableContext ctx) {
            fromHereItCannotBeInHashMapAnymore();
            visitNext(ctx.nextStep);
            return null; // Void
        }

        @Override
        public Void visitPathWalk(PathWalkContext ctx) {
            visitNext(ctx.nextStep);
            return null; // Void
        }

        @Override
        public Void visitStepDown(StepDownContext ctx) {
            add(new StepDown(ctx.numberRange(), ctx.name.getText()));
            visitNext(ctx.nextStep);
            return null; // Void
        }

        @Override
        public Void visitStepUp(StepUpContext ctx) {
            fromHereItCannotBeInHashMapAnymore();
            add(new StepUp());
            visitNext(ctx.nextStep);
            return null; // Void
        }

        //----
        @Override
        public Void visitStepNext(StepNextContext ctx) {
            fromHereItCannotBeInHashMapAnymore();
            add(new StepNext());
            visitNext(ctx.nextStep);
            return null; // Void
        }

        private void doStepNextN(PathContext nextStep, int nextSteps) {
            fromHereItCannotBeInHashMapAnymore();
            add(new StepNextN(nextSteps));
            visitNext(nextStep);
        }

        @Override
        public Void visitStepNext2(StepNext2Context ctx) {
            doStepNextN(ctx.nextStep, 2);
            return null; // Void
        }

        @Override
        public Void visitStepNext3(StepNext3Context ctx) {
            doStepNextN(ctx.nextStep, 3);
            return null; // Void
        }

        @Override
        public Void visitStepNext4(StepNext4Context ctx) {
            doStepNextN(ctx.nextStep, 4);
            return null; // Void
        }

        //----
        @Override
        public Void visitStepPrev(StepPrevContext ctx) {
            fromHereItCannotBeInHashMapAnymore();
            add(new StepPrev());
            visitNext(ctx.nextStep);
            return null; // Void
        }

        private void doStepPrevN(PathContext nextStep, int prevSteps) {
            fromHereItCannotBeInHashMapAnymore();
            add(new StepPrevN(prevSteps));
            visitNext(nextStep);
        }

        @Override
        public Void visitStepPrev2(StepPrev2Context ctx) {
            doStepPrevN(ctx.nextStep, 2);
            return null; // Void
        }

        @Override
        public Void visitStepPrev3(StepPrev3Context ctx) {
            doStepPrevN(ctx.nextStep, 3);
            return null; // Void
        }

        @Override
        public Void visitStepPrev4(StepPrev4Context ctx) {
            doStepPrevN(ctx.nextStep, 4);
            return null; // Void
        }

        //----
        @Override
        public Void visitStepEqualsValue(StepEqualsValueContext ctx) {
            add(new StepEquals(ctx.value.getText()));
            fromHereItCannotBeInHashMapAnymore();
            visitNext(ctx.nextStep);
            return null; // Void
        }

        @Override
        public Void visitStepNotEqualsValue(StepNotEqualsValueContext ctx) {
            fromHereItCannotBeInHashMapAnymore();
            add(new StepNotEquals(ctx.value.getText()));
            visitNext(ctx.nextStep);
            return null; // Void
        }

        @Override
        public Void visitStepIsInSet(StepIsInSetContext ctx) {
            fromHereItCannotBeInHashMapAnymore();
            String      lookupSetName = ctx.set.getText();
            add(new StepIsInSet(lookupSetName, getLookupSet(lookupSetName)));
            visitNext(ctx.nextStep);
            return null; // Void
        }

        @Override
        public Void visitStepIsNotInSet(StepIsNotInSetContext ctx) {
            fromHereItCannotBeInHashMapAnymore();
            String      lookupSetName = ctx.set.getText();
            add(new StepIsNotInSet(lookupSetName, getLookupSet(lookupSetName)));
            visitNext(ctx.nextStep);
            return null; // Void
        }

        private Set<String> getLookupSet(String lookupSetName) {
            Set<String> lookupSet = lookupSets.get(lookupSetName);
            if (lookupSet == null) {
                Map<String, String> lookup = lookups.get(lookupSetName);
                if (lookup != null) {
                    lookupSet = new LinkedHashSet<>(lookup.keySet());
                }
            }
            if (lookupSet == null) {
                throw new InvalidParserConfigurationException("Missing lookupSet \"" + lookupSetName + "\" ");
            }
            return lookupSet;
        }

        @Override
        public Void visitStepStartsWithValue(StepStartsWithValueContext ctx) {
            boolean skipIfShortEnough = stillGoingToHashMap();
            fromHereItCannotBeInHashMapAnymore();
            String value = ctx.value.getText();

            boolean addTheStep = true;
            if (skipIfShortEnough) {
                // If the compare value is short enough that the ENTIRE value was in the hashmap then
                // the actual compare is not longer required
                if (value.length() <= MAX_PREFIX_HASH_MATCH) {
                    addTheStep = false;
                }
            }

            if (addTheStep) {
                add(new StepStartsWith(value));
            }
            visitNext(ctx.nextStep);
            return null; // Void
        }

        @Override
        public Void visitStepEndsWithValue(StepEndsWithValueContext ctx) {
            fromHereItCannotBeInHashMapAnymore();
            add(new StepEndsWith(ctx.value.getText()));
            visitNext(ctx.nextStep);
            return null; // Void
        }

        @Override
        public Void visitStepContainsValue(StepContainsValueContext ctx) {
            fromHereItCannotBeInHashMapAnymore();
            add(new StepContains(ctx.value.getText()));
            visitNext(ctx.nextStep);
            return null; // Void
        }

        @Override
        public Void visitStepNotContainsValue(StepNotContainsValueContext ctx) {
            fromHereItCannotBeInHashMapAnymore();
            add(new StepNotContains(ctx.value.getText()));
            visitNext(ctx.nextStep);
            return null; // Void
        }

        @Override
        public Void visitStepWordRange(StepWordRangeContext ctx) {
            Range range = WordRangeVisitor.getRange(ctx.wordRange());
            add(new StepWordRange(range));
            visitNext(ctx.nextStep);
            return null; // Void
        }

        @Override
        public Void visitStepBackToFull(StepBackToFullContext ctx) {
            add(new StepBackToFull());
            visitNext(ctx.nextStep);
            return null; // Void
        }

    }
}
