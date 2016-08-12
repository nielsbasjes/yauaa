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

package nl.basjes.parse.useragent.analyze.treewalker.steps;

import nl.basjes.parse.useragent.UserAgentBaseVisitor;
import nl.basjes.parse.useragent.UserAgentParser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;

import static nl.basjes.parse.useragent.utils.AntlrUtils.getSourceText;

public class GetResultValueVisitor extends UserAgentBaseVisitor<String> {

    private static final GetResultValueVisitor INSTANCE = new GetResultValueVisitor();

    public static String getResultValue(ParseTree tree) {
        return INSTANCE.visit(tree);
    }

    @Override
    public String visitUuId(UserAgentParser.UuIdContext ctx) {
        return ctx.uuid.getText();
    }

    // This way we can force all other implementations to be really simple.
    @Override
    public String visitChildren(RuleNode node) {
        return getSourceText(node);
    }
}
