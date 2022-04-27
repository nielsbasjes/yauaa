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

package nl.basjes.parse.useragent.beam;

import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.apache.beam.sdk.extensions.sql.BeamSqlUdf;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static nl.basjes.parse.useragent.UserAgent.USERAGENT_FIELDNAME;
import static nl.basjes.parse.useragent.UserAgent.USERAGENT_HEADER;

abstract class BaseParseUserAgentUDF implements BeamSqlUdf {
    private static transient UserAgentAnalyzer userAgentAnalyzer = null;

    protected static UserAgentAnalyzer getInstance() {
        // NOTE: We currently do NOT make an instance with only the wanted fields.
        //       We only know the required parameters the moment the call is done.
        //       At that point it is too late to create an optimized instance.
        if (userAgentAnalyzer == null) {
            userAgentAnalyzer = UserAgentAnalyzer
                .newBuilder()
                .immediateInitialization()
                .dropTests()
                .build();
        }
        return userAgentAnalyzer;
    }

    private static List<String> allFields = null;

    protected static synchronized List<String> getAllFields() {
        if (allFields == null) {
            allFields = new ArrayList<>();
            allFields.add(USERAGENT_FIELDNAME);
            allFields.addAll(getInstance().getAllPossibleFieldNamesSorted());
        }
        return allFields;
    }

    private static List<String> allHeaders = null;

    protected static synchronized List<String> getAllHeaders() {
        if (allHeaders == null) {
            allHeaders = new ArrayList<>();
            allHeaders.add(USERAGENT_HEADER);
            allHeaders.addAll(getInstance().supportedClientHintHeaders());
        }
        return allHeaders;
    }

    // The eval does not support a var args list ( i.e. "String... args" ).
    // This is a Calcite limitation: https://issues.apache.org/jira/browse/CALCITE-2772
    // CHECKSTYLE.OFF: ParameterNumber
    protected static Pair<Map<String, String>, List<String>> parseArguments(
        String arg0,  String arg1,  String arg2,  String arg3,  String arg4,  String arg5,  String arg6,  String arg7,  String arg8,  String arg9,
        String arg10, String arg11, String arg12, String arg13, String arg14, String arg15, String arg16, String arg17, String arg18, String arg19,
        String arg20, String arg21, String arg22, String arg23, String arg24, String arg25, String arg26, String arg27, String arg28, String arg29
    ) {
        // Workaround for  https://issues.apache.org/jira/browse/CALCITE-2772
        // Have a LOT of fields
        List<String> rawInput = Arrays.asList(
            arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9,
            arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17, arg18, arg19,
            arg20, arg21, arg22, arg23, arg24, arg25, arg26, arg27, arg28, arg29);

        List<String> input = new ArrayList<>();
        for (String rawInputValue : rawInput) {
            if (rawInputValue == null) {
                break;
            }
            input.add(rawInputValue);
        }

        if (input.isEmpty()) {
            throw new IllegalArgumentException("Input may not be empty.");
        }

        Map<String, String> requestHeaders = new TreeMap<>();
        List<String> wantedFields = new ArrayList<>();

        int i = 0;
        while (i < input.size()) {
            String parameter = input.get(i);
            if (parameter == null || parameter.isEmpty()) {
                throw new IllegalArgumentException("Null/Empty argument provided to ParseUserAgent.");
            }
            if (getAllFields().stream().anyMatch(parameter::equalsIgnoreCase)) {
                wantedFields.add(parameter);
                i++;
                continue;
            }
            if (getAllHeaders().stream().anyMatch(parameter::equalsIgnoreCase)) {
                String value;
                if (i + 1 >= input.size()) {
                    throw new IllegalArgumentException("Invalid last element in argument list (was a header name which requires a value to follow)");
                } else {
                    value = input.get(i + 1);
                    i++;
                }
                requestHeaders.put(parameter, value);
                i++;
                continue;
            }
            if (i == 0) {
                requestHeaders.put(USERAGENT_HEADER, input.get(i));
                i++;
                continue;
            }
            throw new IllegalArgumentException("Bad argument list for ParseUserAgent.");
        }

        return Pair.of(requestHeaders, wantedFields);
    }


}
