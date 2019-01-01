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

public final class WordSplitter extends Splitter {
    private WordSplitter() {
    }

    private static WordSplitter instance = null;

    public static WordSplitter getInstance() {
        if (instance == null) {
            instance = new WordSplitter();
        }
        return instance;
    }

    public boolean isSeparator(char c) {
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
    public boolean isEndOfStringSeparator(char c) {
        return c == '(' || c == ')';
    }
}
