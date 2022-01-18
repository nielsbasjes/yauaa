/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2022 Niels Basjes
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

package nl.basjes.parse.useragent.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MatcherConfig implements Serializable {

    private final List<String> options;

    private final String matcherSourceFilename;
    private final int matcherSourceLineNumber;

    private final List<ConfigLine> configLines = new ArrayList<>();

    // For Kryo ONLY
    @SuppressWarnings("unused")
    private MatcherConfig(){
        options = Collections.emptyList();
        matcherSourceFilename = "<<Should never appear after deserialization>>";
        matcherSourceLineNumber = -1;
    }

    public MatcherConfig(String matcherSourceFilename, int matcherSourceLineNumber, List<String> options, List<ConfigLine> configLines) {
        this.matcherSourceFilename = matcherSourceFilename;
        this.matcherSourceLineNumber = matcherSourceLineNumber;
//        this.matcherSourceLocation = matcherSourceLocation;
        this.options = options;
        this.configLines.addAll(configLines);
    }

    public List<String> getOptions() {
        return options;
    }

    public String getMatcherSourceFilename() {
        return matcherSourceFilename;
    }

    public int getMatcherSourceLineNumber() {
        return matcherSourceLineNumber;
    }

    public List<ConfigLine> getConfigLines() {
        return configLines;
    }

    public static class ConfigLine implements Serializable {
        public enum Type {
            VARIABLE,
            REQUIRE,
            FAIL_IF_FOUND,
            EXTRACT
        }
        private final Type type;
        private final String attribute;
        private final Long confidence;
        private final String expression;

        // For Kryo ONLY
        @SuppressWarnings("unused")
        private ConfigLine() {
            this.type = Type.FAIL_IF_FOUND;
            this.attribute = "<<Should never appear after deserialization>>";
            this.confidence = -42L;
            this.expression = "<<Should never appear after deserialization>>";
        }

        public ConfigLine(Type type, String attribute, Long confidence, String expression) {
            this.type = type;
            this.attribute = attribute;
            this.confidence = confidence;
            this.expression = expression;
        }

        public Type getType() {
            return type;
        }

        public String getAttribute() {
            return attribute;
        }

        public Long getConfidence() {
            return confidence;
        }

        public String getExpression() {
            return expression;
        }

        @Override
        public String toString() {
            return "ConfigLine{\n" +
                "  type=" + type + ",\n" +
                "  attribute='" + attribute + "',\n" +
                "  confidence=" + confidence + ",\n" +
                "  expression='" + expression + "',\n" +
                '}';
        }
    }

    @Override
    public String toString() {
        return "MatcherConfig{\n" +
            "   options=" + options + ",\n" +
            "   matcherSourceFilename='" + matcherSourceFilename + "',\n" +
            "   matcherSourceLineNumber=" + matcherSourceLineNumber + ",\n" +
            "   configLines=" + configLines + "\n" +
            "}";
    }
}
