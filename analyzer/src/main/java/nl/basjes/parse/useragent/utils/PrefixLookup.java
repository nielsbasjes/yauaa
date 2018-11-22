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
import java.util.Map;

public class PrefixLookup implements Serializable {

    public static class PrefixTrie implements Serializable {
        private PrefixTrie[] childNodes;
        private int          charIndex;
        private boolean      caseSensitive;

        private String theValue;

        public PrefixTrie(boolean caseSensitive) {
            this(caseSensitive, 0);
        }

        private PrefixTrie(boolean caseSensitive, int charIndex) {
            this.caseSensitive = caseSensitive;
            this.charIndex = charIndex;
        }

        private void add(String prefix, String value) {
            if (charIndex == prefix.length()) {
                theValue = value;
                return;
            }

            char myChar = prefix.charAt(charIndex); // This will give us the ASCII value of the char
            if (myChar < 32 || myChar > 126) {
                throw new IllegalArgumentException("Only readable ASCII is allowed as key !!!");
            }

            if (childNodes == null) {
                childNodes = new PrefixLookup.PrefixTrie[128];
            }

            if (caseSensitive) {
                // If case sensitive we 'just' build the tree
                if (childNodes[myChar] == null) {
                    childNodes[myChar] = new PrefixTrie(true, charIndex + 1);
                }
                childNodes[myChar].add(prefix, value);
            } else {
                // If case INsensitive we build the tree
                // and we link the same child to both the
                // lower and uppercase entries in the child array.
                char lower = Character.toLowerCase(myChar);
                char upper = Character.toUpperCase(myChar);

                if (childNodes[lower] == null) {
                    childNodes[lower] = new PrefixTrie(false, charIndex + 1);
                }
                childNodes[lower].add(prefix, value);

                if (childNodes[upper] == null) {
                    childNodes[upper] = childNodes[lower];
                }
            }
        }

        public String find(String input) {
            if (charIndex == input.length()) {
                return theValue;
            }

            char myChar = input.charAt(charIndex); // This will give us the ASCII value of the char
            if (myChar < 32 || myChar > 126) {
                return theValue; // Cannot store these, so this is where it ends.
            }

            if (childNodes == null) {
                return theValue;
            }

            PrefixTrie child = childNodes[myChar];
            if (child == null) {
                return theValue;
            }

            String returnValue = child.find(input);
            return (returnValue == null) ? theValue : returnValue;
        }

    }

    private PrefixTrie prefixPrefixTrie;

    public PrefixLookup(Map<String, String> prefixList, boolean caseSensitive) {
        // Translate the map into a different structure.
        prefixPrefixTrie = new PrefixTrie(caseSensitive);
        prefixList.forEach((key, value) -> prefixPrefixTrie.add(key, value));
    }

    public String findLongestMatchingPrefix(String input) {
        return prefixPrefixTrie.find(input);
    }

}
