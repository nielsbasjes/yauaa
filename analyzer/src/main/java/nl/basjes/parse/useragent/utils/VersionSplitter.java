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

public final class VersionSplitter extends Splitter {
    private VersionSplitter() {
    }

    private static VersionSplitter instance;
    public static VersionSplitter getInstance() {
        if (instance == null) {
            instance = new VersionSplitter();
        }
        return instance;
    }

    public boolean isSeparator(char c) {
        switch (c) {
            case '.':
            case '_':
            case '-':
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean isEndOfStringSeparator(char c) {
        return false;
    }

    private boolean looksLikeEmailOrWebaddress(String value) {
        // Simple quick and dirty way to avoid splitting email and web addresses
        return (value.startsWith("www.") || value.startsWith("http") || (value.contains("@") && value.contains(".")));
    }

    @Override
    public String getSingleSplit(String value, int split) {
        if (looksLikeEmailOrWebaddress(value)) {
            return (split == 1) ? value : null;
        }

        char[] characters = value.toCharArray();
        int start = findSplitStart(characters, split);
        if (start == -1) {
            return null;
        }
        int end = findSplitEnd(characters, start);
        return value.substring(start, end);
    }

    @Override
    public String getFirstSplits(String value, int split) {
        if (looksLikeEmailOrWebaddress(value)) {
            return (split == 1) ? value : null;
        }

        char[] characters = value.toCharArray();
        int start = findSplitStart(characters, split);
        if (start == -1) {
            return null;
        }
        int end = findSplitEnd(characters, start);
        return value.substring(0, end);
    }

}
