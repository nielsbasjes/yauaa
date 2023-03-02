/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2023 Niels Basjes
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
import nl.basjes.parse.useragent.utils.ListSplitter;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StepSegmentRange extends Step {

    private final int firstSegment;
    private final int lastSegment;

    @SuppressWarnings("unused") // Private constructor for serialization systems ONLY (like Kryo)
    private StepSegmentRange() {
        firstSegment = -1;
        lastSegment = -1;
    }

    public StepSegmentRange(WordRangeVisitor.Range range) {
        firstSegment = range.getFirst();
        lastSegment = range.getLast();
    }

    @Override
    public WalkResult walk(@Nonnull ParseTree tree, @Nullable String value) {
        String actualValue = getActualValue(tree, value);
        String filteredValue = ListSplitter.getInstance().getSplitRange(actualValue, firstSegment, lastSegment);
        if (filteredValue == null) {
            return null;
        }
        return walkNextStep(tree, filteredValue.trim());
    }

    @Override
    public boolean canFail() {
        // If you want the first word it cannot fail.
        // For all other numbers it can.
        return !(firstSegment ==1 && lastSegment ==1);
    }

    @Override
    public String toString() {
        return "SegmentRange([" + firstSegment + ":" + lastSegment + "])";
    }

}
