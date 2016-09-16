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

package nl.basjes.parse.useragent.analyze.treewalker.steps;

import nl.basjes.parse.useragent.UserAgentTreeWalkerBaseVisitor;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser.MatcherCleanVersionContext;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser.MatcherContext;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser.MatcherPathContext;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser.MatcherPathIsNullContext;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser.MatcherPathLookupContext;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser.PathContext;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser.PathFixedValueContext;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser.PathWalkContext;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser.StepBackToFullContext;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser.StepContainsValueContext;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser.StepDownContext;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser.StepEndsWithValueContext;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser.StepEqualsValueContext;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser.StepFirstWordsContext;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser.StepNextContext;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser.StepNotEqualsValueContext;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser.StepPrevContext;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser.StepSingleWordContext;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser.StepStartsWithValueContext;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser.StepUpContext;
import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import nl.basjes.parse.useragent.analyze.WordRangeVisitor;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepCleanVersion;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepContains;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepEndsWith;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepEquals;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepIsNull;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepNotEquals;
import nl.basjes.parse.useragent.analyze.treewalker.steps.compare.StepStartsWith;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepDefaultValue;
import nl.basjes.parse.useragent.analyze.treewalker.steps.lookup.StepLookup;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepBackToFull;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepFirstWords;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepFixedString;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepSingleWord;
import nl.basjes.parse.useragent.analyze.treewalker.steps.value.StepWordRange;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepDown;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepNext;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepPrev;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.StepUp;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WalkList {
    private static final Logger LOG = LoggerFactory.getLogger(WalkList.class);

    private final Map<String, Map<String, String>> lookups;
    private final List<Step> steps = new ArrayList<>();

    private boolean verbose;

    public void setVerbose(boolean newVerbose) {
        this.verbose = newVerbose;
        for (Step step : steps) {
            step.setVerbose(newVerbose);
        }
    }

    public WalkList(MatcherContext requiredPattern, Map<String, Map<String, String>> lookups) {
        this.lookups = lookups;
        // Generate the walkList from the requiredPattern
        new WalkListBuilder().visit(requiredPattern);
//        steps.add(new FinalStep());
        linkSteps();

        int i = 1;
        if (verbose) {
            LOG.info("------------------------------------");
            LOG.info("Required: " + requiredPattern.getText());
            for (Step step : steps) {
                step.setVerbose(verbose);
                LOG.info("{}: {}", i++, step);
            }
        }
    }

    private void linkSteps() {
        if (steps.size() > 0) {
            int i;
            for (i = 0; i < steps.size() - 1; i++) {
                steps.get(i).setNextStep(i, steps.get(i + 1));
            }
            steps.get(i).setNextStep(i, null); // This sets the logging prefix for the last step
        }
    }

    public String walk(ParseTree tree, String value) {
        if (steps.size() == 0) {
            return value;
//            return GetResultValueVisitor.getResultValue(tree);
        }
        Step firstStep = steps.get(0);
        if (verbose) {
            Step.LOG.info("Tree: >>>{}<<<", tree.getText());
            Step.LOG.info("Enter step: {}", firstStep);
        }
        String result = firstStep.walk(tree, value);
        if (verbose) {
            Step.LOG.info("Leave step ({}): {}", result == null ? "-" : "+", firstStep);
        }
        return result;
    }

    public Step getFirstStep() {
        if (steps == null || steps.size() == 0) {
            return null;
        }
        return steps.get(0);
    }

    @Override
    public String toString() {
        if (steps.size() == 0) {
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

        @Override
        public Void visitMatcherPathLookup(MatcherPathLookupContext ctx) {
            if (ctx.defaultValue != null) {
                // Always add this one
                steps.add(new StepDefaultValue(ctx.defaultValue.getText()));
            }
            visit(ctx.matcherLookup());
            String lookupName = ctx.lookup.getText();
            Map<String, String> lookup = lookups.get(lookupName);
            if (lookup == null) {
                throw new InvalidParserConfigurationException("Missing lookup \"" + ctx.lookup.getText() + "\" ");
            }
            // Always add this one
            steps.add(new StepLookup(lookupName, lookup));
            return null; // Void
        }

        @Override
        public Void visitMatcherCleanVersion(MatcherCleanVersionContext ctx) {
            // Always add this one
            visit(ctx.matcherLookup());
            steps.add(new StepCleanVersion());
            return null; // Void
        }

        @Override
        public Void visitMatcherPathIsNull(MatcherPathIsNullContext ctx) {
            // Always add this one
            steps.add(new StepIsNull());
            visit(ctx.matcherLookup());
            return null; // Void
        }

        @Override
        public Void visitPathFixedValue(PathFixedValueContext ctx) {
            add(new StepFixedString(ctx.value.getText()));
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
            foundHashEntryPoint = true;
            add(new StepUp());
            visitNext(ctx.nextStep);
            return null; // Void
        }

        @Override
        public Void visitStepNext(StepNextContext ctx) {
            foundHashEntryPoint = true;
            add(new StepNext());
            visitNext(ctx.nextStep);
            return null; // Void
        }

        @Override
        public Void visitStepPrev(StepPrevContext ctx) {
            foundHashEntryPoint = true;
            add(new StepPrev());
            visitNext(ctx.nextStep);
            return null; // Void
        }

        @Override
        public Void visitStepEqualsValue(StepEqualsValueContext ctx) {
            add(new StepEquals(ctx.value.getText()));
            foundHashEntryPoint = true;
            visitNext(ctx.nextStep);
            return null; // Void
        }

        @Override
        public Void visitStepNotEqualsValue(StepNotEqualsValueContext ctx) {
            foundHashEntryPoint = true;
            add(new StepNotEquals(ctx.value.getText()));
            visitNext(ctx.nextStep);
            return null; // Void
        }

        @Override
        public Void visitStepStartsWithValue(StepStartsWithValueContext ctx) {
            foundHashEntryPoint = true;
            add(new StepStartsWith(ctx.value.getText()));
            visitNext(ctx.nextStep);
            return null; // Void
        }

        @Override
        public Void visitStepEndsWithValue(StepEndsWithValueContext ctx) {
            foundHashEntryPoint = true;
            add(new StepEndsWith(ctx.value.getText()));
            visitNext(ctx.nextStep);
            return null; // Void
        }

        @Override
        public Void visitStepContainsValue(StepContainsValueContext ctx) {
            foundHashEntryPoint = true;
            add(new StepContains(ctx.value.getText()));
            visitNext(ctx.nextStep);
            return null; // Void
        }

        @Override
        public Void visitStepFirstWords(StepFirstWordsContext ctx) {
            add(new StepFirstWords(ctx.NUMBER().getSymbol()));
            visitNext(ctx.nextStep);
            return null; // Void
        }

        @Override
        public Void visitStepSingleWord(StepSingleWordContext ctx) {
            add(new StepSingleWord(ctx.NUMBER().getSymbol()));
            visitNext(ctx.nextStep);
            return null; // Void
        }

        @Override
        public Void visitStepWordRange(UserAgentTreeWalkerParser.StepWordRangeContext ctx) {
            WordRangeVisitor.Range range = WordRangeVisitor.getRange(ctx.wordRange());
            if (range.isRangeInHashMap()) {
                foundHashEntryPoint = true;
            }
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
