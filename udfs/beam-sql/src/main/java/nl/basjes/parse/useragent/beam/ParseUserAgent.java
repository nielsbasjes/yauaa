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

import nl.basjes.parse.useragent.UserAgent;
import org.apache.beam.vendor.calcite.v1_28_0.org.apache.calcite.linq4j.function.Parameter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

public class ParseUserAgent extends BaseParseUserAgentUDF {
    // The eval does not support a var args list ( i.e. "String... args" ).
    // This is a Calcite limitation: https://issues.apache.org/jira/browse/CALCITE-2772
    // CHECKSTYLE.OFF: ParameterNumber
    @SuppressWarnings("unused") // Used via reflection
    public static Map<String, String> eval(// NOSONAR java:S107 Methods should not have too many parameters
        @Parameter(name = "Arg  0")                  String arg0,
        @Parameter(name = "Arg  1", optional = true) String arg1,
        @Parameter(name = "Arg  2", optional = true) String arg2,
        @Parameter(name = "Arg  3", optional = true) String arg3,
        @Parameter(name = "Arg  4", optional = true) String arg4,
        @Parameter(name = "Arg  5", optional = true) String arg5,
        @Parameter(name = "Arg  6", optional = true) String arg6,
        @Parameter(name = "Arg  7", optional = true) String arg7,
        @Parameter(name = "Arg  8", optional = true) String arg8,
        @Parameter(name = "Arg  9", optional = true) String arg9,
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
        Pair<Map<String, String>, List<String>> config = parseArguments(
            arg0,  arg1,  arg2,  arg3,  arg4,  arg5,  arg6,  arg7,  arg8,  arg9,
            arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17, arg18, arg19,
            arg20, arg21, arg22, arg23, arg24, arg25, arg26, arg27, arg28, arg29);

        Map<String, String> requestHeaders  = config.getLeft();
        List<String> wantedFields           = config.getRight();

        UserAgent.ImmutableUserAgent agent = getInstance().parse(requestHeaders);
        if (wantedFields.isEmpty()) {
            return agent.toMap();
        } else {
            return agent.toMap(wantedFields);
        }
    }
}
