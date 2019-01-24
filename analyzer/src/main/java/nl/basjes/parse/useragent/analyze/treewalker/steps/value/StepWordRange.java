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

package nl.basjes.parse.useragent.analyze.treewalker.steps.value;

import nl.basjes.parse.useragent.analyze.WordRangeVisitor;
import nl.basjes.parse.useragent.analyze.treewalker.steps.Step;
import nl.basjes.parse.useragent.analyze.treewalker.steps.WalkList.WalkResult;
import nl.basjes.parse.useragent.parser.UserAgentParser.SingleVersionContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.SingleVersionWithCommasContext;
import nl.basjes.parse.useragent.utils.VersionSplitter;
import nl.basjes.parse.useragent.utils.WordSplitter;
import org.antlr.v4.runtime.tree.ParseTree;

public class StepWordRange extends Step {

    private final int firstWord;
    private final int lastWord;

    // Private constructor for serialization systems ONLY (like Kyro)
    private StepWordRange() {
        firstWord = -1;
        lastWord = -1;
    }

    public StepWordRange(WordRangeVisitor.Range range) {
        firstWord = range.getFirst();
        lastWord = range.getLast();
    }

    @Override
    public WalkResult walk(ParseTree tree, String value) {
        String actualValue = getActualValue(tree, value);
        String filteredValue;
        if (tree.getChildCount() == 1 && (
              tree.getChild(0) instanceof SingleVersionContext ||
              tree.getChild(0) instanceof SingleVersionWithCommasContext)) {
            filteredValue = VersionSplitter.getInstance().getSplitRange(actualValue, firstWord, lastWord);
        } else {
            filteredValue = WordSplitter.getInstance().getSplitRange(actualValue, firstWord, lastWord);
        }
        if (filteredValue == null) {
            return null;
        }
        return walkNextStep(tree, filteredValue);
    }

    @Override
    public boolean canFail() {
        // If you want the first word it cannot fail.
        // For all other numbers it can.
        return !(firstWord==1 && lastWord==1);
    }

    @Override
    public String toString() {
        return "WordRange([" + firstWord + ":" + lastWord + "])";
    }

}
