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

package nl.basjes.parse.useragent.utils;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;

public final class AntlrUtils {
    private AntlrUtils() {
    }

    public static String getSourceText(ParserRuleContext ctx){
        if (ctx.start == null) {
            return null; // Invalid
        }
        if (ctx.stop == null) {
            return ctx.getText();
        }
        int startIndex = ctx.start.getStartIndex();
        int stopIndex = ctx.stop.getStopIndex();
        if (stopIndex < startIndex) {
            return ctx.getText();
        }
        CharStream inputStream = ctx.start.getInputStream();
        return inputStream.getText(new Interval(startIndex, stopIndex));
    }

    public static String getSourceText(RuleNode ruleNode){
        if (ruleNode instanceof ParserRuleContext) {
            return getSourceText((ParserRuleContext)ruleNode);
        }
        return ruleNode.getText();
    }

    public static String getSourceText(ParseTree ruleNode){
        if (ruleNode instanceof ParserRuleContext) {
            return getSourceText((ParserRuleContext)ruleNode);
        }
        return ruleNode.getText();
    }

}
