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

package nl.basjes.parse.useragent.analyze.treewalker.steps;

import nl.basjes.parse.useragent.parser.UserAgentParser;
import nl.basjes.parse.useragent.parser.UserAgentParser.CommentSeparatorContext;
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

    protected final String walkNextStep(ParseTree tree, String value) {
        if (nextStep == null) {
            String result = value;
            if (value == null) {
                result = GetResultValueVisitor.getResultValue(tree);
            }
            if (verbose) {
                LOG.info("{} Final (implicit) step: {}", logprefix, result);
            }
            return result;
        }

        if (verbose) {
            LOG.info("{} Tree: >>>{}<<<", logprefix, getSourceText(tree));
            LOG.info("{} Enter step({}): {}", logprefix, stepNr, nextStep);
        }
        String result = nextStep.walk(tree, value);
        if (verbose) {
            LOG.info("{} Result: >>>{}<<<", logprefix, result);
            LOG.info("{} Leave step({}): {}", logprefix, result == null ? "-" : "+", nextStep);
        }
        return result;
    }

    protected final ParseTree up(ParseTree tree) {
        if (tree == null) {
            return null;
        }

        // Needed because of the way the ANTLR rules have been defined.
        if (tree instanceof UserAgentParser.ProductNameWordsContext     ||
            tree instanceof UserAgentParser.ProductNameEmailContext     ||
            tree instanceof UserAgentParser.ProductNameUuidContext      ||
            tree instanceof UserAgentParser.ProductNameKeyValueContext  ||
            tree instanceof UserAgentParser.ProductNameVersionContext
            ) {
            return up(tree.getParent());
        }
        return tree.getParent();
    }

    public static boolean treeIsSeparator(ParseTree tree) {
        return tree instanceof CommentSeparatorContext
            || tree instanceof TerminalNode;
    }

    protected String getActualValue(ParseTree tree, String value) {
        if (value == null) {
            return getSourceText(tree);
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
    public abstract String walk(ParseTree tree, String value);

    public Step getNextStep() {
        return nextStep;
    }
}


