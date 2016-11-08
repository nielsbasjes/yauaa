/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2016 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerBaseVisitor;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.WordRangeContext;

public class WordRangeVisitor extends UserAgentTreeWalkerBaseVisitor<WordRangeVisitor.Range> {
    public static final int MAX_RANGE_IN_HASHMAP = 3;

    public static class Range {

        public Range(int first, int last) {
            this.first = first;
            this.last = last;
        }

        public int getFirst() {
            return first;
        }

        public int getLast() {
            return last;
        }

        final int first;
        final int last;

        public boolean isRangeInHashMap(){
            if (last > MAX_RANGE_IN_HASHMAP) {
                return false;
            }
            // First == 1 , Last = N
            // First == N , Last = N
            return ((first == 1 && last != -1) || first == last);
        }

    }

    private static final WordRangeVisitor WORD_RANGE_VISITOR = new WordRangeVisitor();

    public static Range getRange(WordRangeContext ctx) {
        return WORD_RANGE_VISITOR.visit(ctx);
    }

    @Override
    public Range visitWordRangeStartToEnd(UserAgentTreeWalkerParser.WordRangeStartToEndContext ctx) {
        return new Range(
            Integer.parseInt(ctx.firstWord.getText()),
            Integer.parseInt(ctx.lastWord.getText()));
    }

    @Override
    public Range visitWordRangeFirstWords(UserAgentTreeWalkerParser.WordRangeFirstWordsContext ctx) {
        return new Range(
            1,
            Integer.parseInt(ctx.lastWord.getText()));
    }

    @Override
    public Range visitWordRangeLastWords(UserAgentTreeWalkerParser.WordRangeLastWordsContext ctx) {
        return new Range(
            Integer.parseInt(ctx.firstWord.getText()),
            -1);
    }

    @Override
    public Range visitWordRangeSingleWord(UserAgentTreeWalkerParser.WordRangeSingleWordContext ctx) {
        Integer wordNumber = Integer.parseInt(ctx.singleWord.getText());
        return new Range(
            wordNumber,
            wordNumber);
    }
}
