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

package nl.basjes.parse.useragent.analyze.treewalker.steps.compare;

import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import nl.basjes.parse.useragent.analyze.treewalker.steps.Step;
import nl.basjes.parse.useragent.analyze.treewalker.steps.WalkList.WalkResult;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StepDefaultIfNull extends Step {

    private final String  defaultValue;

    @SuppressWarnings("unused") // Private constructor for serialization systems ONLY (like Kryo)
    private StepDefaultIfNull() {
        defaultValue = "<< Should not be seen anywhere >>";
    }

    public StepDefaultIfNull(String defaultValue) {
        this.defaultValue = defaultValue;
        if (defaultValue == null) {
            throw new InvalidParserConfigurationException("Setting a null default on DefaultIfNull is useless.");
        }
    }

    @Override
    public boolean canFail() {
        return false;
    }

    @Override
    public boolean mustHaveInput() {
        return false;
    }

    @Override
    public WalkResult walk(@Nonnull ParseTree tree, @Nullable String value) {
        WalkResult actualValue = walkNextStep(tree, value);

        if (actualValue == null ||
            actualValue.getValue() == null) {
            return new WalkResult(tree, defaultValue);
        }
        return actualValue;
    }

    @Override
    public String toString() {
        return "DefaultIfNull(default=" + defaultValue + ")";
    }

}
