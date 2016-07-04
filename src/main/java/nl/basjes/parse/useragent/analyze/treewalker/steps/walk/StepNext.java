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

package nl.basjes.parse.useragent.analyze.treewalker.steps.walk;

import nl.basjes.parse.useragent.analyze.treewalker.steps.Step;
import org.antlr.v4.runtime.tree.ParseTree;

public class StepNext extends Step {

    private ParseTree next(ParseTree tree) {
        if (tree == null) {
            return null;
        }

        ParseTree parent = up(tree);
        ParseTree child;
        boolean foundCurrent = false;
        for (int i = 0; i < parent.getChildCount(); i++) {
            child = parent.getChild(i);
            if (foundCurrent) {
                if (treeIsSeparator(child)) {
                    continue;
                }
                return child;
            }

            if (child == tree) {
                foundCurrent = true;
            }
        }
        return null; // There is no next
    }

    @Override
    public String walk(ParseTree tree, String value) {
        ParseTree nextTree = next(tree);
        if (nextTree == null) {
            return null;
        }

        return walkNextStep(nextTree, null);
    }

    @Override
    public String toString() {
        return "Next()";
    }

}
