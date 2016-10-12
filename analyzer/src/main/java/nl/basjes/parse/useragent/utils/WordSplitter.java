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

public final class WordSplitter {
    private WordSplitter() {
    }

    public static boolean isWordSeparator(char c) {
        switch (c) {
            case ' ':
            case '.':
            case ':':
            case ';':
            case '=':
            case '/':
            case '\\':
            case '+':
            case '-':
            case '_':
            case '<':
            case '>':
            case '~':
            case '(': // EndOfString marker
            case ')': // EndOfString marker
                return true;
            default:
                return false;
        }
    }
    public static boolean isEndOfStringSeparator(char c) {
        switch (c) {
            case '(':
            case ')':
                return true;
            default:
                return false;
        }
    }

    /**
     * Find the start offset of next word
     *
     * @param chars The input in which we are seeking
     * @param offset The start offset from where to seek
     * @return The offset of the next word
     */
    public static int findNextWordStart(char[] chars, int offset) {
        for (int charNr = offset; charNr<chars.length; charNr++) {
            char theChar = chars[charNr];
            if (isEndOfStringSeparator(theChar)) {
                return -1;
            }
            if (!isWordSeparator(theChar)) {
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
     * @return The offset of the last character of the last word.
     */
    public static int findEndOfString(char[] chars, int offset) {
        for (int charNr = offset; charNr<chars.length; charNr++) {
            char theChar = chars[charNr];
            if (isEndOfStringSeparator(theChar)) {
                return charNr;
            }
        }
        return chars.length;
    }

    /**
     * Find the start offset of word
     *
     * @param chars The input in which we are seeking
     * @param word  The word number for which we are looking for the start
     * @return The offset or -1 if it does not exist
     */
    public static int findWordStart(char[] chars, int word) {
        if (word <= 0) {
            return -1;
        }
        // We expect the chars to start with a word.
        int charNr = 0;
        boolean inWord = false;
        int currentWord = 0;
        for (char theChar : chars) {
            if (isEndOfStringSeparator(theChar)) {
                return -1;
            }

            if (isWordSeparator(theChar)) {
                if (inWord) {
                    inWord = false;
                }
            } else {
                if (!inWord) {
                    inWord = true;
                    currentWord++;
                    if (currentWord == word) {
                        return charNr;
                    }
                }
            }
            charNr++;
        }
        return -1;
    }

    public static int findWordEnd(char[] chars, int start) {
        int i = start;
        while (i < chars.length) {
            if (isWordSeparator(chars[i])) {
                return i;
            }
            i++;
        }
        return chars.length; // == The end of the string
    }

    public static String getSingleWord(String value, int word) {
        char[] characters = value.toCharArray();
        int start = WordSplitter.findWordStart(characters, word);
        if (start == -1) {
            return null;
        }
        int end = WordSplitter.findWordEnd(characters, start);
        return value.substring(start, end);
    }

    public static String getFirstWords(String value, int word) {
        char[] characters = value.toCharArray();
        int start = WordSplitter.findWordStart(characters, word);
        if (start == -1) {
            return null;
        }
        int end = findWordEnd(characters, start);
        return value.substring(0, end);
    }

    public static String getWordRange(String value, int firstWord, int lastWord) {
        if (value == null || (lastWord >0 && lastWord < firstWord)) {
            return null;
        }
        char[] characters = value.toCharArray();
        int firstCharOfFirstWord = findWordStart(characters, firstWord);
        if (firstCharOfFirstWord == -1) {
            return null;
        }

        if (lastWord == -1) {
            return value.substring(firstCharOfFirstWord, findEndOfString(characters, firstCharOfFirstWord));
        }
        int firstCharOfLastWord = firstCharOfFirstWord;
        if (lastWord != firstWord) {
            firstCharOfLastWord = findWordStart(characters, lastWord);
            if (firstCharOfLastWord == -1) {
                return null;
            }
        }

        int lastCharOfLastWord = findWordEnd(characters, firstCharOfLastWord);
        if (lastCharOfLastWord == -1) {
            return null;
        }

        return value.substring(firstCharOfFirstWord, lastCharOfLastWord);
    }

}
