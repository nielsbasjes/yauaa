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

import nl.basjes.parse.useragent.analyze.WordRangeVisitor.Range;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public abstract class Splitter {

    public abstract boolean isSeparator(char c);

    public abstract boolean isEndOfStringSeparator(char c);

    /**
     * Find the start offset of next split
     *
     * @param chars The input in which we are seeking
     * @param offset The start offset from where to seek
     * @return The offset of the next split
     */
    public int findNextSplitStart(char[] chars, int offset) {
        for (int charNr = offset; charNr<chars.length; charNr++) {
            char theChar = chars[charNr];
            if (isEndOfStringSeparator(theChar)) {
                return -1;
            }
            if (!isSeparator(theChar)) {
                return charNr;
            }
        }
        return -1;
    }

    /**
     * Find the end of the string
     *
     * @param chars The input in which we are seeking
     * @param offset The start offset from where to seek
     * @return The offset of the last character of the last split.
     */
    public int findEndOfString(char[] chars, int offset) {
        for (int charNr = offset; charNr<chars.length; charNr++) {
            char theChar = chars[charNr];
            if (isEndOfStringSeparator(theChar)) {
                return charNr;
            }
        }
        return chars.length;
    }

    /**
     * Find the start offset of split
     *
     * @param chars The input in which we are seeking
     * @param split  The split number for which we are looking for the start
     * @return The offset or -1 if it does not exist
     */
    public int findSplitStart(char[] chars, int split) {
        if (split <= 0) {
            return -1;
        }
        // We expect the chars to start with a split.
        int charNr = 0;
        boolean inSplit = false;
        int currentSplit = 0;
        for (char theChar : chars) {
            if (isEndOfStringSeparator(theChar)) {
                return -1;
            }

            if (isSeparator(theChar)) {
                if (inSplit) {
                    inSplit = false;
                }
            } else {
                if (!inSplit) {
                    inSplit = true;
                    currentSplit++;
                    if (currentSplit == split) {
                        return charNr;
                    }
                }
            }
            charNr++;
        }
        return -1;
    }

    public int findSplitEnd(char[] chars, int startOffset) {
        int i = startOffset;
        while (i < chars.length) {
            if (isSeparator(chars[i])) {
                return i;
            }
            i++;
        }
        return chars.length; // == The end of the string
    }

    public String getSingleSplit(String value, int split) {
        char[] characters = value.toCharArray();
        int start = findSplitStart(characters, split);
        if (start == -1) {
            return null;
        }
        int end = findSplitEnd(characters, start);
        return value.substring(start, end);
    }

    public String getFirstSplits(String value, int split) {
        char[] characters = value.toCharArray();
        int start = findSplitStart(characters, split);
        if (start == -1) {
            return null;
        }
        int end = findSplitEnd(characters, start);
        return value.substring(0, end);
    }

    public String getSplitRange(String value, int firstSplit, int lastSplit) {
        if (value == null || (lastSplit > 0 && lastSplit < firstSplit)) {
            return null;
        }
        char[] characters = value.toCharArray();
        int firstCharOfFirstSplit = findSplitStart(characters, firstSplit);
        if (firstCharOfFirstSplit == -1) {
            return null;
        }

        if (lastSplit == -1) {
            return value.substring(firstCharOfFirstSplit, findEndOfString(characters, firstCharOfFirstSplit));
        }
        int firstCharOfLastSplit = firstCharOfFirstSplit;
        if (lastSplit != firstSplit) {
            firstCharOfLastSplit = findSplitStart(characters, lastSplit);
            if (firstCharOfLastSplit == -1) {
                return null;
            }
        }

        int lastCharOfLastSplit = findSplitEnd(characters, firstCharOfLastSplit);

        return value.substring(firstCharOfFirstSplit, lastCharOfLastSplit);
    }

    public String getSplitRange(String value, Range range) {
        return getSplitRange(value, range.getFirst(), range.getLast());
    }

    public String getSplitRange(String value, List<Pair<Integer, Integer>> splitList, Range range) {
        return getSplitRange(value, splitList, range.getFirst(), range.getLast());
    }

    public String getSplitRange(String value, List<Pair<Integer, Integer>> splitList, int first, int last) {
        int lastIndex = last - 1;
        int firstIndex = first - 1;
        int splits = splitList.size();

        if (last == -1) {
            lastIndex = splits - 1;
        }
        if (firstIndex < 0 || lastIndex < 0) {
            return null;
        }
        if (firstIndex >= splits || lastIndex >= splits) {
            return null;
        }

        return value.substring(splitList.get(firstIndex).getLeft(), splitList.get(lastIndex).getRight());
    }

    public List<Pair<Integer, Integer>> createSplitList(String characters) {
        return createSplitList(characters.toCharArray());
    }

    public List<Pair<Integer, Integer>> createSplitList(char[] characters){
        List<Pair<Integer, Integer>> result = new ArrayList<>(8);

        int offset = findSplitStart(characters, 1);
        if (offset == -1) {
            return result; // Nothing at all. So we are already done
        }
        while(offset != -1) {

            int start = offset;
            int end= findSplitEnd(characters, start);

            result.add(new ImmutablePair<>(start, end));
            offset = findNextSplitStart(characters, end);
        }
        return result;
    }

}
