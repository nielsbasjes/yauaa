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

public final class VersionSplitter {
    private VersionSplitter() {
    }

    public static boolean isVersionSeparator(char c) {
        switch (c) {
            case '.':
            case '_':
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
    public static int findNextVersionStart(char[] chars, int offset) {
        for (int charNr = offset; charNr<chars.length; charNr++) {
            char theChar = chars[charNr];
            if (!isVersionSeparator(theChar)) {
                return charNr;
            }
        }
        return -1;
    }

    /**
     * Find the start offset of version
     *
     * @param chars The input in which we are seeking
     * @param version  The version number for which we are looking for the start
     * @return The offset or -1 if it does not exist
     */
    public static int findVersionStart(char[] chars, int version) {
        if (version <= 0) {
            return -1;
        }
        // We expect the chars to start with a version.

        int charNr = 0;
        boolean inVersion = false;
        int currentVersion = 0;
        for (char theChar : chars) {
            if (isVersionSeparator(theChar)) {
                if (inVersion) {
                    inVersion = false;
                }
            } else {
                if (!inVersion) {
                    inVersion = true;
                    currentVersion++;
                    if (currentVersion == version) {
                        return charNr;
                    }
                }
            }
            charNr++;
        }
        return -1;
    }

    public static int findVersionEnd(char[] chars, int start) {
        int i = start;
        while (i < chars.length) {
            if (isVersionSeparator(chars[i])) {
                return i;
            }
            i++;
        }
        return chars.length; // == The end of the string
    }

    public static String getSingleVersion(String value, int word) {
        if (value == null) {
            return null;
        }
        char[] characters = value.toCharArray();
        int start = VersionSplitter.findVersionStart(characters, word);
        if (start == -1) {
            return null;
        }
        int end = VersionSplitter.findVersionEnd(characters, start);
        return value.substring(start, end);
    }

    public static String getFirstVersions(String value, int word) {
        if (value == null) {
            return null;
        }

        // FIXME: Simple quick and dirty way to avoid splitting email and web addresses
        if (value.startsWith("www.") || value.startsWith("http") || value.contains("@")) {
            return value;
        }
        char[] characters = value.toCharArray();
        int start = VersionSplitter.findVersionStart(characters, word);
        if (start == -1) {
            return null;
        }
        int end = VersionSplitter.findVersionEnd(characters, start);
        return value.substring(0, end);
    }

    public static String getVersionRange(String value, int firstVersion, int lastVersion) {
        if (value == null || (lastVersion >0 && lastVersion < firstVersion)) {
            return null;
        }
        char[] characters = value.toCharArray();
        int firstCharOfFirstVersion = findVersionStart(characters, firstVersion);
        if (firstCharOfFirstVersion == -1) {
            return null;
        }

        if (lastVersion == -1) {
            return value.substring(firstCharOfFirstVersion, characters.length);
        }
        int firstCharOfLastWord = firstCharOfFirstVersion;
        if (lastVersion != firstVersion) {
            firstCharOfLastWord = findVersionStart(characters, lastVersion);
            if (firstCharOfLastWord == -1) {
                return null;
            }
        }

        int lastCharOfLastVersion = findVersionEnd(characters, firstCharOfLastWord);
        if (lastCharOfLastVersion == -1) {
            return null;
        }

        return value.substring(firstCharOfFirstVersion, lastCharOfLastVersion);
    }

}
