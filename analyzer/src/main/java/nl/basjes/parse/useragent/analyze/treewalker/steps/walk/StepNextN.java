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

public class StepNextN extends Step {

    private final int steps;

    // Private constructor for serialization systems ONLY (like Kyro)
    private StepNextN() {
        steps = -1;
    }

    public StepNextN(int steps) {
        this.steps = steps;
    }

    private ParseTree next(ParseTree tree) {
        ParseTree parent = up(tree);
        ParseTree child;
        boolean foundCurrent = false;
        int stepsToDo = steps;
        for (int i = 0; i < parent.getChildCount(); i++) {
            child = parent.getChild(i);
            if (foundCurrent) {
                if (treeIsSeparator(child)) {
                    continue;
                }
                stepsToDo--;
                if (stepsToDo == 0) {
                    return child;
                }
            }

            if (child == tree) {
                foundCurrent = true;
            }
        }
        return null; // There is no next
    }

    @Override
    public WalkResult walk(ParseTree tree, String value) {
        ParseTree nextTree = next(tree);
        if (nextTree == null) {
            return null;
        }

        return walkNextStep(nextTree, null);
    }

    @Override
    public String toString() {
        return "Next(" + steps + ")";
    }

}
