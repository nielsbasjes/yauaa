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

package nl.basjes.parse.useragent.analyze.treewalker.steps.lookup;

import nl.basjes.parse.useragent.analyze.treewalker.steps.Step;
import nl.basjes.parse.useragent.analyze.treewalker.steps.WalkList.WalkResult;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public class StepLookup extends Step {

    private final String lookupName;
    private final Map<String, String> lookup;
    private final String  defaultValue;
    private final boolean canFail;

    @SuppressWarnings("unused") // Private constructor for serialization systems ONLY (like Kryo)
    private StepLookup() {
        lookupName = "<< Should not be seen anywhere >>";
        lookup = Collections.emptyMap();
        defaultValue = "<< Should not be seen anywhere >>";
        canFail = false;
    }

    public StepLookup(String lookupName, Map<String, String> lookup, String defaultValue) {
        this.lookupName = lookupName;
        this.lookup = lookup;
        this.defaultValue = defaultValue;
        canFail = defaultValue == null;
    }

    @Override
    public boolean canFail() {
        return canFail;
    }

    @Override
    public boolean mustHaveInput() {
        return canFail;
    }

    @Override
    public WalkResult walk(@Nonnull ParseTree tree, @Nullable String value) {
        String actualValue = getActualValue(tree, value);

        String result = null;
        if (actualValue != null) {
            result = lookup.get(actualValue.toLowerCase(Locale.ROOT));
        }

        if (result == null) {
            if (defaultValue == null) {
                return null;
            } else {
                return walkNextStep(tree, defaultValue);
            }
        }
        return walkNextStep(tree, result);
    }

    @Override
    public String toString() {
        return "Lookup(@" + lookupName + " ; default=" + defaultValue + ")";
    }

}
