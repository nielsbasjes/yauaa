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

package nl.basjes.parse.useragent.servlet.graphql.utils;

import nl.basjes.parse.useragent.servlet.ParseService;

import java.util.List;

public final class FieldsAndSchema {
    private FieldsAndSchema() {
        // Utility class
    }

    /**
     * Convert a Yauaa fieldname into a GraphQL schema fieldname
     */
    public static String getSchemaFieldName(String fieldName) {
        StringBuilder sb = new StringBuilder(fieldName);
        sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        return sb.toString();
    }

    /**
     * Convert a GraphQL schema fieldname into a Yauaa fieldname
     */
    public static String getFieldName(String schemaFieldName) {
        StringBuilder sb = new StringBuilder(schemaFieldName);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    // Get the list of all fields we want to expose from the analyzer.
    public static List<String> getAllFieldsForGraphQL(ParseService parseService) {
        return parseService
            .getUserAgentAnalyzer()
            .getAllPossibleFieldNamesSorted()
            // Avoiding this error:
            // "__SyntaxError__" in "AnalysisResult" must not begin with "__", which is reserved by GraphQL introspection.
            // The field "__SyntaxError__" is handled separately.
            .stream()
            .filter(name -> !name.startsWith("__"))
            .toList();
    }
}
