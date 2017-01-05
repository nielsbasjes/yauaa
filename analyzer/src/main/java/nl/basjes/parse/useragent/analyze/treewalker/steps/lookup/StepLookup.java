/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2017 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.useragent.analyze.treewalker.steps.lookup;

import nl.basjes.parse.useragent.analyze.treewalker.steps.Step;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Map;

public class StepLookup extends Step {

    private final String lookupName;
    private final Map<String, String> lookup;

    public StepLookup(String lookupName, Map<String, String> lookup) {
        this.lookupName = lookupName;
        this.lookup = lookup;
    }

    @Override
    public String walk(ParseTree tree, String value) {
        String input = getActualValue(tree, value);
        String result = lookup.get(getActualValue(tree, value).toLowerCase());
        if (verbose) {
            LOG.info("{} Lookup: {}[{}] => {}", logprefix, lookupName, input, result);
        }
        return result;
    }

    @Override
    public String toString() {
        return "Lookup(" + lookupName + ")";
    }

}
