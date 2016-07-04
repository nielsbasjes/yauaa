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

public class VersionSplitter {
    public static boolean isVersionSeparator(char c) {
        switch (c) {
            case '.':
            case '_':
                return true;
        }
        return false;
    }

    /**
     * Find the start offset of next word
     *
     * @param chars The input in which we are seeking
     * @param offset The start offset from where to seek
     * @return The offset of the next word
     */
    public static int findNextVersionStart(char[] chars, int offset) {
        for (int charNr = offset ; charNr<chars.length; charNr++) {
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
        char[] characters = value.toCharArray();
        int start = VersionSplitter.findVersionStart(characters, word);
        if (start == -1) {
            return null;
        }
        int end = VersionSplitter.findVersionEnd(characters, start);
        return value.substring(start, end);
    }

    public static String getFirstVersions(String value, int word) {
        char[] characters = value.toCharArray();
        int start = VersionSplitter.findVersionStart(characters, word);
        if (start == -1) {
            return null;
        }
        int end = VersionSplitter.findVersionEnd(characters, start);
        return value.substring(0, end);
    }

}
