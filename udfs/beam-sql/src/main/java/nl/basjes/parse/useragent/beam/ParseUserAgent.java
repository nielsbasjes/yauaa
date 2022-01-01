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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ParseUserAgent extends BaseParseUserAgentUDF {
    // The eval does not support a var args list ( i.e. "String... args" ).
    // This is a Calcite limitation: https://issues.apache.org/jira/browse/CALCITE-2772
    // CHECKSTYLE.OFF: ParameterNumber
    @SuppressWarnings("unused") // Used via reflection
    public static Map<String, String> eval(// NOSONAR java:S107 Methods should not have too many parameters
        @Parameter(name = "userAgent") String input,
        @Parameter(name = "Field  1", optional = true) String field1,
        @Parameter(name = "Field  2", optional = true) String field2,
        @Parameter(name = "Field  3", optional = true) String field3,
        @Parameter(name = "Field  4", optional = true) String field4,
        @Parameter(name = "Field  5", optional = true) String field5,
        @Parameter(name = "Field  6", optional = true) String field6,
        @Parameter(name = "Field  7", optional = true) String field7,
        @Parameter(name = "Field  8", optional = true) String field8,
        @Parameter(name = "Field  9", optional = true) String field9,
        @Parameter(name = "Field 10", optional = true) String field10
    ) {
        UserAgent.ImmutableUserAgent agent = getInstance().parse(input);

        if (field1 != null) {
            String[] fieldArray = {field1, field2, field3, field4, field5, field6, field7, field8, field9, field10};

            List<String> fieldList = Arrays
                .stream(fieldArray)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            return agent.toMap(fieldList);
        }
        return agent.toMap();
    }
}
