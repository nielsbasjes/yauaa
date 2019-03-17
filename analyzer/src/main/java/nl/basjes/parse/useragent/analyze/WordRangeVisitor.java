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
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.WordRangeContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.WordRangeFirstWordsContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.WordRangeLastWordsContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.WordRangeSingleWordContext;
import nl.basjes.parse.useragent.parser.UserAgentTreeWalkerParser.WordRangeStartToEndContext;

import java.io.Serializable;
import java.util.Objects;

public final class WordRangeVisitor extends UserAgentTreeWalkerBaseVisitor<WordRangeVisitor.Range> {

    public static class Range implements Serializable {

        // private constructor for serialization systems ONLY (like Kyro)
        private Range() {
            first = -1;
            last = -1;
        }

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

        private final int first;
        private final int last;

        private String rangeString = null;

        @Override
        public String toString() {
            if (rangeString == null) {
                if (last == -1) {
                    rangeString = "[" + first + "-]";
                } else {
                    rangeString = "[" + first + "-" + last + "]";
                }
            }
            return rangeString;
        }

        @Override
        public boolean equals(Object o) {
            if ((!(o instanceof Range))) {
                return false;
            }
            Range range = (Range) o;
            return first == range.first &&
                last == range.last;
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, last);
        }
    }

    private static final WordRangeVisitor WORD_RANGE_VISITOR = new WordRangeVisitor();

    // private constructor for serialization systems ONLY (like Kyro)
    private WordRangeVisitor() {
    }

    public static Range getRange(WordRangeContext ctx) {
        return WORD_RANGE_VISITOR.visit(ctx);
    }

    @Override
    public Range visitWordRangeStartToEnd(WordRangeStartToEndContext ctx) {
        return new Range(
            Integer.parseInt(ctx.firstWord.getText()),
            Integer.parseInt(ctx.lastWord.getText()));
    }

    @Override
    public Range visitWordRangeFirstWords(WordRangeFirstWordsContext ctx) {
        return new Range(
            1,
            Integer.parseInt(ctx.lastWord.getText()));
    }

    @Override
    public Range visitWordRangeLastWords(WordRangeLastWordsContext ctx) {
        return new Range(
            Integer.parseInt(ctx.firstWord.getText()),
            -1);
    }

    @Override
    public Range visitWordRangeSingleWord(WordRangeSingleWordContext ctx) {
        int wordNumber = Integer.parseInt(ctx.singleWord.getText());
        return new Range(
            wordNumber,
            wordNumber);
    }
}
