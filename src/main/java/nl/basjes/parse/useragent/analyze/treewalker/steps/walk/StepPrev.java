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

public class StepPrev extends Step {

    private ParseTree prev(ParseTree tree) {
        if (tree == null) {
            return null;
        }
        ParseTree parent = up(tree);

        ParseTree prevChild = null;
        ParseTree child = null;
        int i;
        for (i = 0; i < parent.getChildCount(); i++) {
            if (!treeIsSeparator(child)) {
                prevChild = child;
            }
            child = parent.getChild(i);
            if (child == tree) {
                return prevChild;
            }
        }
        return null; // There is no previous
    }

    @Override
    public String walk(ParseTree tree, String value) {
        ParseTree nextTree = prev(tree);
        if (nextTree == null) {
            return null;
        }

        return walkNextStep(nextTree, null);
    }

    @Override
    public String toString() {
        return "Prev()";
    }

}
