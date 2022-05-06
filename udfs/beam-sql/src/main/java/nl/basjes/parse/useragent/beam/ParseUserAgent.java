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

import nl.basjes.parse.useragent.AnalyzerUtilities.ParsedArguments;
import nl.basjes.parse.useragent.UserAgent;
import org.apache.beam.vendor.calcite.v1_28_0.org.apache.calcite.linq4j.function.Parameter;

import java.util.List;
import java.util.Map;

public class ParseUserAgent extends BaseParseUserAgentUDF {
    // The eval does not support a var args list ( i.e. "String... args" ).
    // This is a Calcite limitation: https://issues.apache.org/jira/browse/CALCITE-2772
    // CHECKSTYLE.OFF: ParameterNumber
    @SuppressWarnings("unused") // Used via reflection
    public static Map<String, String> eval(// NOSONAR java:S107 Methods should not have too many parameters
        @Parameter(name = "Arg 00")                  String arg00,
        @Parameter(name = "Arg 01", optional = true) String arg01,
        @Parameter(name = "Arg 02", optional = true) String arg02,
        @Parameter(name = "Arg 03", optional = true) String arg03,
        @Parameter(name = "Arg 04", optional = true) String arg04,
        @Parameter(name = "Arg 05", optional = true) String arg05,
        @Parameter(name = "Arg 06", optional = true) String arg06,
        @Parameter(name = "Arg 07", optional = true) String arg07,
        @Parameter(name = "Arg 08", optional = true) String arg08,
        @Parameter(name = "Arg 09", optional = true) String arg09,
        @Parameter(name = "Arg 10", optional = true) String arg10,
        @Parameter(name = "Arg 11", optional = true) String arg11,
        @Parameter(name = "Arg 12", optional = true) String arg12,
        @Parameter(name = "Arg 13", optional = true) String arg13,
        @Parameter(name = "Arg 14", optional = true) String arg14,
        @Parameter(name = "Arg 15", optional = true) String arg15,
        @Parameter(name = "Arg 16", optional = true) String arg16,
        @Parameter(name = "Arg 17", optional = true) String arg17,
        @Parameter(name = "Arg 18", optional = true) String arg18,
        @Parameter(name = "Arg 19", optional = true) String arg19,
        @Parameter(name = "Arg 20", optional = true) String arg20,
        @Parameter(name = "Arg 21", optional = true) String arg21,
        @Parameter(name = "Arg 22", optional = true) String arg22,
        @Parameter(name = "Arg 23", optional = true) String arg23,
        @Parameter(name = "Arg 24", optional = true) String arg24,
        @Parameter(name = "Arg 25", optional = true) String arg25,
        @Parameter(name = "Arg 26", optional = true) String arg26,
        @Parameter(name = "Arg 27", optional = true) String arg27,
        @Parameter(name = "Arg 28", optional = true) String arg28,
        @Parameter(name = "Arg 29", optional = true) String arg29
    ) {
        // Workaround for  https://issues.apache.org/jira/browse/CALCITE-2772
        // Have a LOT of fields
        ParsedArguments parseArguments = parseArguments(
            arg00, arg01, arg02, arg03, arg04, arg05, arg06, arg07, arg08, arg09,
            arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17, arg18, arg19,
            arg20, arg21, arg22, arg23, arg24, arg25, arg26, arg27, arg28, arg29);

        UserAgent.ImmutableUserAgent agent = getInstance().parse(parseArguments.getRequestHeaders());
        List<String> wantedFields = parseArguments.getWantedFields();
        if (wantedFields.isEmpty()) {
            return agent.toMap(getAllFields());
        } else {
            return agent.toMap(wantedFields);
        }
    }
}
