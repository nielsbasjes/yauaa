/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2016 Niels Basjes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package nl.basjes.parse.useragent.analyze.treewalker.steps.lookup;

import nl.basjes.parse.useragent.analyze.treewalker.steps.Step;
import org.antlr.v4.runtime.tree.ParseTree;

public class StepDefaultValue extends Step {

    private String defaultValue;

    public StepDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String walk(ParseTree tree, String value) {
        String result = walkNextStep(tree, null);
        if (result == null) {
            result = defaultValue;
        }
        return result;
    }

    @Override
    public String toString() {
        return "DefaultValue(" + defaultValue + ")";
    }

}
