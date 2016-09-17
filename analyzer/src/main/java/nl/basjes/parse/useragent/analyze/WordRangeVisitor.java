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

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.UserAgentTreeWalkerBaseVisitor;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser;
import nl.basjes.parse.useragent.UserAgentTreeWalkerParser.WordRangeContext;

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

        int first;
        int last;

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
