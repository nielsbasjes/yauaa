/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2021 Niels Basjes
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
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

public class StepIsInLookupContains extends Step {

    private final String          lookupName;
    private final HashSet<String> lookupKeys;

    @SuppressWarnings("unused") // Private constructor for serialization systems ONLY (like Kryo)
    private StepIsInLookupContains() {
        lookupName = null;
        lookupKeys = null;
    }

    public StepIsInLookupContains(String lookupName, Map<String, String> lookup) {
        this.lookupName = lookupName;
        this.lookupKeys = new HashSet<>(lookup.keySet());
    }

    @Override
    public WalkResult walk(@Nonnull ParseTree tree, @Nullable String value) {
        String actualValue = getActualValue(tree, value);
        String compareInput = actualValue.toLowerCase(Locale.ROOT);
        for (String key : lookupKeys) {
            if (compareInput.contains(key)) {
                return walkNextStep(tree, actualValue);
            }
        }
        // Not found:
        return null;
    }

    @Override
    public String toString() {
        return "IsInLookupContains(@" + lookupName + ")";
    }

}
