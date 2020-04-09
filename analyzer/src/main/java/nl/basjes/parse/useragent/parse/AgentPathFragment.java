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

package nl.basjes.parse.useragent.parse;

public enum AgentPathFragment {
    AGENT("agent"),
    PRODUCT("product"),
    NAME("name"),
    VERSION("version"),
    COMMENTS("comments"),
    ENTRY("entry"),
    KEYVALUE("keyvalue"),
    KEY("key"),
    TEXT("text"),
    URL("url"),
    UUID("uuid"),
    EMAIL("email"),
    BASE64("base64"),
    VALUE("value"),

    // Extract substring
    WORDRANGE("WordRange"), // Special

    // Compare entries
    EQUALS("Equals"),
    STARTSWITH("StartsWith");

    private final String name;

    AgentPathFragment(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        // (otherName == null) check is not needed because name.equals(null) returns false
        return name.equals(otherName);
    }

    @Override
    public String toString() {
        return this.name;
    }

}
