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

package nl.basjes.parse.useragent.analyze.treewalker.steps.lookup;

import nl.basjes.parse.useragent.analyze.treewalker.steps.Step;
import nl.basjes.parse.useragent.analyze.treewalker.steps.WalkList.WalkResult;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public class StepLookupContains extends Step {

    private final String lookupName;
    private final Map<String, String> lookup;
    private final String defaultValue;

    @SuppressWarnings("unused") // Private constructor for serialization systems ONLY (like Kryo)
    private StepLookupContains() {
        lookupName = "<< Should not be seen anywhere >>";
        lookup = Collections.emptyMap();
        defaultValue = "<< Should not be seen anywhere >>";
    }

    public StepLookupContains(String lookupName, Map<String, String> lookup, String defaultValue) {
        this.lookupName = lookupName;
        this.lookup = lookup;
        this.defaultValue = defaultValue;
    }

    @Override
    public WalkResult walk(@Nonnull ParseTree tree, @Nullable String value) {
        String actualValue = getActualValue(tree, value);

        actualValue = actualValue.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, String> entry: lookup.entrySet()) {
            if (actualValue.contains(entry.getKey())) {
                return walkNextStep(tree, entry.getValue());
            }
        }

        // Not found:
        if (defaultValue == null) {
            return null;
        }
        return walkNextStep(tree, defaultValue);
    }

    @Override
    public String toString() {
        return "LookupContains(@" + lookupName + " ; default="+defaultValue+")";
    }

}
