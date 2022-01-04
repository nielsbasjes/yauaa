/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2022 Niels Basjes
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

import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerBaseVisitor;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownAgentContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownNameContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownKeyContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownValueContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownVersionContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownCommentsContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownEntryContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownProductContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownEmailContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownKeyvalueContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownTextContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownUrlContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownUuidContext;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.HashMap;
import java.util.Map;

import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.NumberRangeAllContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.NumberRangeContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.NumberRangeEmptyContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.NumberRangeOpenStartToEndContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.NumberRangeSingleValueContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.NumberRangeStartToEndContext;
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.NumberRangeStartToOpenEndContext;

public final class NumberRangeVisitor extends UserAgentTreeWalkerBaseVisitor<NumberRangeList, MatcherTree> {

    private static final Integer DEFAULT_MIN = 1;
    private static final Integer DEFAULT_MAX = 10;

    @SuppressWarnings("rawtypes")
    private static final Map<Class<? extends ParserRuleContext>, Integer> MAX_RANGE = new HashMap<>();

    static {
        // Hardcoded maximum values because of the parsing rules
        MAX_RANGE.put(StepDownAgentContext.class,                1);
        MAX_RANGE.put(StepDownNameContext.class,                 1);
        MAX_RANGE.put(StepDownKeyContext.class,                  1);

        // Did statistics on over 200K real useragents from 2015.
        // These are the maximum values from that test set (+ a little margin)
        MAX_RANGE.put(StepDownValueContext.class,                2); // Max was 2
        MAX_RANGE.put(StepDownVersionContext.class,              4); // Max was 4
        MAX_RANGE.put(StepDownCommentsContext.class,             2); // Max was 2
        MAX_RANGE.put(StepDownEntryContext.class,               20); // Max was much higher
        MAX_RANGE.put(StepDownProductContext.class,             10); // Max was much higher

        MAX_RANGE.put(StepDownEmailContext.class,                2);
        MAX_RANGE.put(StepDownKeyvalueContext.class,             3);
        MAX_RANGE.put(StepDownTextContext.class,                 8);
        MAX_RANGE.put(StepDownUrlContext.class,                  2);
        MAX_RANGE.put(StepDownUuidContext.class,                 2);
    }

    private NumberRangeVisitor() {
    }

    private Integer getMaxRange(NumberRangeContext<MatcherTree> ctx) {
        ParserRuleContext<MatcherTree> parent = ctx.getParent();
        Integer maxRange = MAX_RANGE.get(parent.getClass());
        if (maxRange == null) {
            return DEFAULT_MAX;
        }
        return maxRange;
    }

    static final NumberRangeVisitor NUMBER_RANGE_VISITOR = new NumberRangeVisitor();

    public static NumberRangeList getList(NumberRangeContext<MatcherTree> ctx) {
        return NUMBER_RANGE_VISITOR.visit(ctx);
    }

    @Override
    public NumberRangeList visitNumberRangeStartToEnd(NumberRangeStartToEndContext<MatcherTree> ctx) {
        return new NumberRangeList(
                Integer.parseInt(ctx.rangeStart.getText()),
                Integer.parseInt(ctx.rangeEnd.getText()));
    }

    @Override
    public NumberRangeList visitNumberRangeOpenStartToEnd(NumberRangeOpenStartToEndContext<MatcherTree> ctx) {
        return new NumberRangeList(
            1,
            Integer.parseInt(ctx.rangeEnd.getText()));
    }

    @Override
    public NumberRangeList visitNumberRangeStartToOpenEnd(NumberRangeStartToOpenEndContext<MatcherTree> ctx) {
        return new NumberRangeList(
            Integer.parseInt(ctx.rangeStart.getText()),
            getMaxRange(ctx));
    }

    @Override
    public NumberRangeList visitNumberRangeSingleValue(NumberRangeSingleValueContext<MatcherTree> ctx) {
        int value = Integer.parseInt(ctx.count.getText());
        return new NumberRangeList(value, value);
    }

    @Override
    public NumberRangeList visitNumberRangeAll(NumberRangeAllContext<MatcherTree> ctx) {
        return new NumberRangeList(DEFAULT_MIN, getMaxRange(ctx));
    }

    @Override
    public NumberRangeList visitNumberRangeEmpty(NumberRangeEmptyContext<MatcherTree> ctx) {
        return new NumberRangeList(DEFAULT_MIN, getMaxRange(ctx));
    }
}
