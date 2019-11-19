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

package nl.basjes.parse.useragent.analyze.treewalker.steps.lookup;

import nl.basjes.parse.useragent.analyze.treewalker.steps.Step;
import nl.basjes.parse.useragent.analyze.treewalker.steps.WalkList.WalkResult;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Map;
import java.util.Set;

public class StepIsInLookupContains extends Step {

    private final String      lookupName;
    private final Set<String> lookupKeys;

    // Private constructor for serialization systems ONLY (like Kyro)
    private StepIsInLookupContains() {
        lookupName = null;
        lookupKeys = null;
    }

    public StepIsInLookupContains(String lookupName, Map<String, String> lookup) {
        this.lookupName = lookupName;
        this.lookupKeys = lookup.keySet();
    }

    @Override
    public WalkResult walk(ParseTree tree, String value) {
        String input = getActualValue(tree, value);

        String compareInput = input.toLowerCase();
        for (String key: lookupKeys) {
            if (compareInput.contains(key)) {
                return walkNextStep(tree, input);
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
