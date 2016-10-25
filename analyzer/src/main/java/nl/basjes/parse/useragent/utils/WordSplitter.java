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
