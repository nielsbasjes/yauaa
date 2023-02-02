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

package nl.basjes.parse.useragent.analyze.treewalker.steps.compare;

import nl.basjes.parse.useragent.analyze.treewalker.steps.Step;
import nl.basjes.parse.useragent.analyze.treewalker.steps.WalkList.WalkResult;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static nl.basjes.parse.useragent.utils.springframework.util.StringUtils.endsWithIgnoreCase;

public class StepEndsWith extends Step {

    private final String desiredValue;

    @SuppressWarnings("unused") // Private constructor for serialization systems ONLY (like Kryo)
    private StepEndsWith() {
        desiredValue = null;
    }

    public StepEndsWith(String desiredValue) {
        this.desiredValue = desiredValue;
    }

    @Override
    public WalkResult walk(@Nonnull ParseTree tree, @Nullable String value) {
        String actualValue = getActualValue(tree, value);

        if (endsWithIgnoreCase(actualValue, desiredValue)) {
            return walkNextStep(tree, actualValue);
        }
        return null;
    }

    @Override
    public String toString() {
        return "EndsWith(" + desiredValue + ")";
    }

}
