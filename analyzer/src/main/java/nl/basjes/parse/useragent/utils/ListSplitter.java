/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2021 Niels Basjes
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

public final class ListSplitter extends Splitter {
    private ListSplitter() {
    }

    private static ListSplitter instance = null;

    public static ListSplitter getInstance() {
        if (instance == null) {
            instance = new ListSplitter();
        }
        return instance;
    }

    public boolean isSeparator(char c) {
        return c == '|';
    }
    public boolean isEndOfStringSeparator(char c) {
        return false;
    }
}
