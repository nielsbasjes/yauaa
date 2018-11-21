/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2018 Niels Basjes
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

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

public class PrefixLookup implements Serializable {

    public static class PrefixTrie implements Serializable {
        private PrefixTrie[] childNodes;
        private int          charIndex;

        private String theValue;

        public PrefixTrie() {
            this(0);
        }

        private PrefixTrie(int charIndex) {
            this.charIndex = charIndex;
        }

        private void add(char[] prefix, String value) {
            if (charIndex == prefix.length) {
                theValue = value;
                return;
            }

            int myChar = prefix[charIndex]; // This will give us the ASCII value of the char
            if (myChar < 32 || myChar > 126) {
                throw new IllegalArgumentException("Only readable ASCII is allowed as key !!!");
            }

            if (childNodes == null) {
                childNodes = new PrefixTrie[128];
            }

            if (childNodes[myChar] == null) {
                childNodes[myChar] = new PrefixTrie(charIndex+1);
            }

            childNodes[myChar].add(prefix, value);
        }

        public void add(String prefix, String value) {
            add(prefix.toCharArray(), value);
        }

        public String find(char[] input) {
            if (charIndex == input.length) {
                return theValue;
            }
            int myChar = input[charIndex]; // This will give us the ASCII value of the char
            if (myChar < 32 || myChar > 126) {
                return null; // Cannot store these, so never an answer.
            }

            if (childNodes == null || childNodes[myChar] == null) {
                return theValue;
            }
            String returnValue = childNodes[myChar].find(input);
            if (returnValue == null) {
                return theValue;
            }
            return returnValue;
        }

        public String find(String input) {
            return find(input.toCharArray());
        }
    }

    private PrefixTrie prefixPrefixTrie = new PrefixTrie();

    public PrefixLookup(Map<String, String> prefixList) {
        // Translate the map into a different structure and lowercase the key.
        prefixList.forEach((key, value) -> prefixPrefixTrie.add(key.toLowerCase(Locale.ENGLISH), value));
    }

    public String findLongestMatchingPrefix(String input) {
        return prefixPrefixTrie.find(input.toLowerCase(Locale.ENGLISH));
    }

}
