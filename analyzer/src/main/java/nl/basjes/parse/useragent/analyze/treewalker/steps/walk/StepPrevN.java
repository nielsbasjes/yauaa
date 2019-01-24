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

package nl.basjes.parse.useragent.analyze.treewalker.steps.walk;

import nl.basjes.parse.useragent.analyze.treewalker.steps.Step;
import nl.basjes.parse.useragent.analyze.treewalker.steps.WalkList.WalkResult;
import org.antlr.v4.runtime.tree.ParseTree;

public class StepPrevN extends Step {

    private static final int SIZE = 20;
    private transient ParseTree[] children = null;

    private final int steps;

    // Private constructor for serialization systems ONLY (like Kyro)
    private StepPrevN() {
        steps = -1;
    }

    public StepPrevN(int steps) {
        this.steps = steps;
    }

    private ParseTree prev(ParseTree tree) {
        ParseTree parent = up(tree);

        if (children== null) {
            children = new ParseTree[SIZE];
        }

        int lastChildIndex = -1;
        ParseTree child = null;
        int i;
        for (i = 0; i < parent.getChildCount(); i++) {
            if (!treeIsSeparator(child)) {
                lastChildIndex++;
                children[lastChildIndex] = child;
            }
            child = parent.getChild(i);
            if (child == tree) {
                if (lastChildIndex < steps) {
                    break; // There is no previous
                }
                return children[lastChildIndex - steps + 1];
            }
        }
        return null; // There is no previous
    }

    @Override
    public WalkResult walk(ParseTree tree, String value) {
        ParseTree prevTree = prev(tree);
        if (prevTree == null) {
            return null;
        }

        return walkNextStep(prevTree, null);
    }

    @Override
    public String toString() {
        return "Prev(" + steps + ")";
    }

}
