/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2018 Niels Basjes
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
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StepLookupPrefix extends Step {

    private final String              lookupName;
    private final List<Pair<String, String>> sortedPrefixList;
    private final String              defaultValue;

    public StepLookupPrefix(String lookupName, Map<String, String> prefixList, String defaultValue) {
        this.lookupName = lookupName;
        this.defaultValue = defaultValue;

        sortedPrefixList = new ArrayList<>(prefixList.size());

        // Translate the map into a different structure and lowercase the key.
        prefixList.forEach((key, value) -> sortedPrefixList.add(Pair.of(key.toLowerCase(Locale.ENGLISH), value)));

        // Sort the list to work best with the chosen algorithm.
        // Sort values by length (longest first), then alphabetically.
        sortedPrefixList.sort((o1, o2) -> {
            String k1 = o1.getKey();
            String k2 = o2.getKey();
            int l1 =  k1.length();
            int l2 =  k2.length();
            if (l1==l2) {
                return k1.compareTo(k2);
            }
            return (l1<l2) ? 1 : -1;
        });
    }

    @Override
    public WalkResult walk(ParseTree tree, String value) {
        String input = getActualValue(tree, value);

        String result = lookupPrefix(input);

        if (result == null) {
            if (defaultValue == null) {
                return null;
            } else {
                return walkNextStep(tree, defaultValue);
            }
        }
        return walkNextStep(tree, result);
    }

    public String lookupPrefix(String input) {
        String lowerInput = input.toLowerCase(Locale.ENGLISH);

        // TODO: Experiment if a different datastructur is significantly faster (This implementation is SILLY simple)
        for (Pair<String, String> prefix: sortedPrefixList) {
            if (lowerInput.startsWith(prefix.getKey())) {
                return prefix.getRight();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "LookupPrefix(@" + lookupName + " ; default="+defaultValue+")";
    }

}
