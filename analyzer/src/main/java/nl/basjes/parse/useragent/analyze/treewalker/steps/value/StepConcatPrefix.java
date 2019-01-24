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

import nl.basjes.parse.useragent.analyze.treewalker.steps.Step;
import nl.basjes.parse.useragent.analyze.treewalker.steps.WalkList.WalkResult;
import org.antlr.v4.runtime.tree.ParseTree;

public class StepConcatPrefix extends Step {

    private final String prefix;

    // Private constructor for serialization systems ONLY (like Kyro)
    private StepConcatPrefix() {
        prefix = null;
    }

    public StepConcatPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public WalkResult walk(ParseTree tree, String value) {
        String actualValue = getActualValue(tree, value);
        String filteredValue = prefix + actualValue;
        return walkNextStep(tree, filteredValue);
    }

    @Override
    public boolean canFail(){
        return false;
    }

    @Override
    public String toString() {
        return "ConcatPrefix(" + prefix + ")";
    }

}
