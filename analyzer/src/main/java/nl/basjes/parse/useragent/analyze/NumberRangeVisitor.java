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

import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerBaseVisitor;
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
import static nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.StepDownContext;

public final class NumberRangeVisitor extends UserAgentTreeWalkerBaseVisitor<NumberRangeList> {

    private static final Integer DEFAULT_MIN = 1;
    private static final Integer DEFAULT_MAX = 10;

    private static final Map<String, Integer> MAX_RANGE = new HashMap<>();

    static {
        // Hardcoded maximum values because of the parsing rules
        MAX_RANGE.put("agent",                1);
        MAX_RANGE.put("name",                 1);
        MAX_RANGE.put("key",                  1);

        // Did statistics on over 200K real useragents from 2015.
        // These are the maximum values from that test set (+ a little margin)
        MAX_RANGE.put("value",                2); // Max was 2
        MAX_RANGE.put("version",              5); // Max was 4
        MAX_RANGE.put("comments",             2); // Max was 2
        MAX_RANGE.put("entry",               20); // Max was much higher
        MAX_RANGE.put("product",             10); // Max was much higher

        MAX_RANGE.put("email",                2);
        MAX_RANGE.put("keyvalue",             3);
        MAX_RANGE.put("text",                 8);
        MAX_RANGE.put("url",                  3);
        MAX_RANGE.put("uuid",                 4);
    }

    private NumberRangeVisitor() {
    }

    private static Integer getMaxRange(NumberRangeContext ctx) {
        ParserRuleContext parent = ctx.getParent();
        String name = ((StepDownContext) parent).name.getText();
        Integer maxRange = MAX_RANGE.get(name);
        if (maxRange == null) {
            return DEFAULT_MAX;
        }
        return maxRange;
    }

    static final NumberRangeVisitor NUMBER_RANGE_VISITOR = new NumberRangeVisitor();

    public static NumberRangeList getList(NumberRangeContext ctx) {
        return NUMBER_RANGE_VISITOR.visit(ctx);
    }

    @Override
    public NumberRangeList visitNumberRangeStartToEnd(NumberRangeStartToEndContext ctx) {
        return new NumberRangeList(
                Integer.parseInt(ctx.rangeStart.getText()),
                Integer.parseInt(ctx.rangeEnd.getText()));
    }

    @Override
    public NumberRangeList visitNumberRangeOpenStartToEnd(NumberRangeOpenStartToEndContext ctx) {
        return new NumberRangeList(
            1,
            Integer.parseInt(ctx.rangeEnd.getText()));
    }

    @Override
    public NumberRangeList visitNumberRangeStartToOpenEnd(NumberRangeStartToOpenEndContext ctx) {
        return new NumberRangeList(
            Integer.parseInt(ctx.rangeStart.getText()),
            getMaxRange(ctx));
    }

    @Override
    public NumberRangeList visitNumberRangeSingleValue(NumberRangeSingleValueContext ctx) {
        int value = Integer.parseInt(ctx.count.getText());
        return new NumberRangeList(value, value);
    }

    @Override
    public NumberRangeList visitNumberRangeAll(NumberRangeAllContext ctx) {
        return new NumberRangeList(DEFAULT_MIN, getMaxRange(ctx));
    }

    @Override
    public NumberRangeList visitNumberRangeEmpty(NumberRangeEmptyContext ctx) {
        return new NumberRangeList(DEFAULT_MIN, getMaxRange(ctx));
    }
}
