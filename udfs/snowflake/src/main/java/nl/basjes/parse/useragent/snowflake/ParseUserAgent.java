/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2023 Niels Basjes
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

package nl.basjes.parse.useragent.snowflake;

import nl.basjes.parse.useragent.AnalyzerUtilities;
import nl.basjes.parse.useragent.AnalyzerUtilities.ParsedArguments;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static nl.basjes.parse.useragent.UserAgent.USERAGENT_HEADER;

// CHECKSTYLE.OFF: HideUtilityClassConstructor because this is how Snowflake wants it.
public class ParseUserAgent {

    private static final UserAgentAnalyzer  ANALYZER;
    private static final List<String>       ALL_ALLOWED_FIELDS;
    private static final List<String>       ALL_ALLOWED_HEADERS;
    static {
        ANALYZER = UserAgentAnalyzer.newBuilder().dropTests().immediateInitialization().build();
        ALL_ALLOWED_HEADERS = new ArrayList<>();
        ALL_ALLOWED_HEADERS.add(USERAGENT_HEADER);
        ALL_ALLOWED_HEADERS.addAll(ANALYZER.supportedClientHintHeaders());
        ALL_ALLOWED_FIELDS = new ArrayList<>();
        ALL_ALLOWED_FIELDS.addAll(ANALYZER.getAllPossibleFieldNamesSorted());
    }

    public static Map<String, String> parse(String... parameters) {
        ParsedArguments parsedArguments = AnalyzerUtilities.parseArguments(parameters, ALL_ALLOWED_FIELDS, ALL_ALLOWED_HEADERS);
        if (parsedArguments.getWantedFields().isEmpty()) {
            return ANALYZER.parse(parsedArguments.getRequestHeaders()).toMap();
        }
        return ANALYZER.parse(parsedArguments.getRequestHeaders()).toMap(parsedArguments.getWantedFields());
    }
}
