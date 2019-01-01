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

package nl.basjes.parse.useragent.analyze.treewalker.steps;

import nl.basjes.parse.useragent.analyze.treewalker.steps.WalkList.WalkResult;
import nl.basjes.parse.useragent.parser.UserAgentParser;
import nl.basjes.parse.useragent.parser.UserAgentParser.CommentSeparatorContext;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

import static nl.basjes.parse.useragent.utils.AntlrUtils.getSourceText;

public abstract class Step implements Serializable {
    protected static final Logger LOG = LoggerFactory.getLogger(Step.class);
    private int stepNr;
    protected String logprefix = "";
    private Step nextStep;

    protected boolean verbose = false;

    public void setVerbose(boolean newVerbose) {
        this.verbose = newVerbose;
    }

    public final void setNextStep(int newStepNr, Step newNextStep) {
        this.stepNr = newStepNr;
        this.nextStep = newNextStep;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < newStepNr + 1; i++) {
            sb.append("-->");
        }
        logprefix = sb.toString();
    }

    protected final WalkResult walkNextStep(ParseTree tree, String value) {
        if (nextStep == null) {
            String result = value;
            if (value == null) {
                result = getSourceText((ParserRuleContext)tree);
            }
            if (verbose) {
                LOG.info("{} Final (implicit) step: {}", logprefix, result);
            }
            return new WalkResult(tree, result);
        }

        if (verbose) {
            LOG.info("{} Tree: >>>{}<<<", logprefix, getSourceText((ParserRuleContext)tree));
            LOG.info("{} Enter step({}): {}", logprefix, stepNr, nextStep);
        }
        WalkResult result = nextStep.walk(tree, value);
        if (verbose) {
            LOG.info("{} Result: >>>{}<<<", logprefix, result);
            LOG.info("{} Leave step({}): {}", logprefix, result == null ? "-" : "+", nextStep);
        }
        return result;
    }

    protected final ParseTree up(ParseTree tree) {
        ParseTree parent = tree.getParent();

        // Needed because of the way the ANTLR rules have been defined.
        if (parent instanceof UserAgentParser.ProductNameContext ||
            parent instanceof UserAgentParser.ProductVersionContext ||
            parent instanceof UserAgentParser.ProductVersionWithCommasContext
            ) {
            return up(parent);
        }
        return parent;
    }

    public static boolean treeIsSeparator(ParseTree tree) {
        return tree instanceof CommentSeparatorContext
            || tree instanceof TerminalNode;
    }

    protected String getActualValue(ParseTree tree, String value) {
        if (value == null) {
            return getSourceText((ParserRuleContext)tree);
        }
        return value;
    }

    /**
     * This will walk into the tree and recurse through all the remaining steps.
     * This must iterate of all possibilities and return the first matching result.
     *
     * @param tree  The tree to walk into.
     * @param value The string representation of the previous step (needed for compare and lookup operations).
     *              The null value means to use the implicit 'full' value (i.e. getSourceText(tree) )
     * @return Either null or the actual value that was found.
     */
    public abstract WalkResult walk(ParseTree tree, String value);

    /**
     * Some steps cannot fail.
     * For a require rule if the last step cannot fail then this can be removed from the require list
     * to improve performance at run time.
     * @return If this specific step can or cannot fail.
     */
    public boolean canFail(){
        return true; // Default is to assume the step is always needed.
    }

    public Step getNextStep() {
        return nextStep;
    }
}


