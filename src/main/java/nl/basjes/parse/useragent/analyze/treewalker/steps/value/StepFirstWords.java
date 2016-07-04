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

package nl.basjes.parse.useragent.analyze.treewalker.steps.value;

import nl.basjes.parse.useragent.analyze.treewalker.steps.Step;
import nl.basjes.parse.useragent.utils.VersionSplitter;
import nl.basjes.parse.useragent.utils.WordSplitter;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

public class StepFirstWords extends Step {

    private int numberOfWords;

    public StepFirstWords(Token numberOfWords) {
        this.numberOfWords = tokenToInteger(numberOfWords);
    }

    @Override
    public String walk(ParseTree tree, String value) {
        String actualValue = getActualValue(tree, value);
        String filteredValue;
        if (tree.getChildCount() == 1 &&
            tree.getChild(0).getClass().getSimpleName().equals("SimpleVersionContext")) {
                filteredValue = VersionSplitter.getFirstVersions(actualValue, numberOfWords);
        } else {
            filteredValue = WordSplitter.getFirstWords(actualValue, numberOfWords);
        }
        if (filteredValue == null) {
            return null;
        }
        return walkNextStep(tree, filteredValue);
    }

    @Override
    public String toString() {
        return "FirstWords(" + numberOfWords + ")";
    }

}
