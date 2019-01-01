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

package nl.basjes.parse.useragent.utils;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;

public final class AntlrUtils {
    private AntlrUtils() {}

    public static String getSourceText(ParserRuleContext ctx){
        if (ctx.start == null || ctx.stop == null) {
            return ctx.getText();
        }
        int startIndex = ctx.start.getStartIndex();
        int stopIndex = ctx.stop.getStopIndex();
        if (stopIndex < startIndex) {
            return ""; // Just return the empty string.
        }
        CharStream inputStream = ctx.start.getInputStream();
        return inputStream.getText(new Interval(startIndex, stopIndex));
    }

}
