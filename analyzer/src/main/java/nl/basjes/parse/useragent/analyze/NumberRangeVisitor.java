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

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.parse.MatcherTree;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerBaseVisitor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.NotImplementedException;

import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.NumberRangeAllContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.NumberRangeContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.NumberRangeEmptyContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.NumberRangeOpenStartToEndContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.NumberRangeSingleValueContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.NumberRangeStartToEndContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.NumberRangeStartToOpenEndContext;

public final class NumberRangeVisitor<P> extends UserAgentTreeWalkerBaseVisitor<NumberRangeList, P> {

    private static final Integer DEFAULT_MIN = 1;
    private static final Integer DEFAULT_MAX = 10;

//    private NumberRangeVisitor() {
//    }

    private static Integer getMaxRange(NumberRangeContext<?> ctx) {
        ParserRuleContext<?> parent = ctx.getParent();

        switch (parent.getClass().getSimpleName()) {
            // Hardcoded maximum values because of the parsing rules
            case "StepDownAgentContext":    return 1;
            case "StepDownNameContext":     return 1;
            case "StepDownKeyContext":      return 1;

            // Did statistics on over 200K real useragents from 2015.
            // These are the maximum values from that test set (+ a little margin)
            case "StepDownValueContext":    return  2;  // Max was 2
            case "StepDownVersionContext":  return  5;  // Max was 4
            case "StepDownCommentsContext": return  2;  // Max was 2
            case "StepDownEntryContext":    return 20;  // Max was much higher
            case "StepDownProductContext":  return 10;  // Max was much higher

            case "StepDownEmailContext":    return  2;
            case "StepDownKeyvalueContext": return  3;
            case "StepDownTextContext":     return  8;
            case "StepDownUrlContext":      return  3;
            case "StepDownUuidContext":     return  4;
            default:                        return DEFAULT_MAX;
        }
    }

    @Override
    public NumberRangeList visit(ParseTree<P> tree, P parameter) {
        throw new NotImplementedException("Wrong visit usage");
    }

    @Override
    public NumberRangeList visitNumberRangeStartToEnd(NumberRangeStartToEndContext<P> ctx) {
        return new NumberRangeList(
                Integer.parseInt(ctx.rangeStart.getText()),
                Integer.parseInt(ctx.rangeEnd.getText()));
    }

    @Override
    public NumberRangeList visitNumberRangeOpenStartToEnd(NumberRangeOpenStartToEndContext<P> ctx) {
        return new NumberRangeList(
            1,
            Integer.parseInt(ctx.rangeEnd.getText()));
    }

    @Override
    public NumberRangeList visitNumberRangeStartToOpenEnd(NumberRangeStartToOpenEndContext<P> ctx) {
        return new NumberRangeList(
            Integer.parseInt(ctx.rangeStart.getText()),
            getMaxRange(ctx));
    }

    @Override
    public NumberRangeList visitNumberRangeSingleValue(NumberRangeSingleValueContext<P> ctx) {
        int value = Integer.parseInt(ctx.count.getText());
        return new NumberRangeList(value, value);
    }

    @Override
    public NumberRangeList visitNumberRangeAll(NumberRangeAllContext<P> ctx) {
        return new NumberRangeList(DEFAULT_MIN, getMaxRange(ctx));
    }

    @Override
    public NumberRangeList visitNumberRangeEmpty(NumberRangeEmptyContext<P> ctx) {
        return new NumberRangeList(DEFAULT_MIN, getMaxRange(ctx));
    }
}
