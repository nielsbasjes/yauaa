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

package nl.basjes.parse.useragent.trino;

import io.airlift.slice.Slice;
import io.trino.spi.block.Block;
import io.trino.spi.block.BlockBuilder;
import io.trino.spi.function.Description;
import io.trino.spi.function.ScalarFunction;
import io.trino.spi.function.SqlType;
import io.trino.spi.type.MapType;
import io.trino.spi.type.TypeOperators;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.Version;

import java.util.Map;
import java.util.TreeMap;

import static io.trino.spi.type.VarcharType.VARCHAR;
import static nl.basjes.parse.useragent.UserAgent.USERAGENT_HEADER;

public final class ParseUserAgentFunctionClientHints {

    private ParseUserAgentFunctionClientHints() {
    }

    // NOTE: We currently cannot make an instance with only the wanted fields.
    //       We only know the required parameters the moment the call is done.
    //       At that point it is too late to create an optimized instance.
    private static ThreadLocal<UserAgentAnalyzer> threadLocalUserAgentAnalyzer =
        ThreadLocal.withInitial(() ->
            UserAgentAnalyzer
                .newBuilder()
                .hideMatcherLoadStats()
                .withCache(10000)
                .immediateInitialization()
                .build());


    private static Slice getSlice(Block elements, int i) {
        return elements.getSlice(i, 0, elements.getSliceLength(i));
    }

    @ScalarFunction("parse_user_agent")
    @Description("Tries to parse and analyze the provided useragent string and extract as many attributes " +
        "as possible. Uses Yauaa (Yet Another UserAgent Analyzer) version " + Version.PROJECT_VERSION + ". " +
        "See https://yauaa.basjes.nl/udf/trino/ for documentation.")
    @SqlType("map(varchar, varchar)")
    public static Block parseUserAgent(@SqlType("array(varchar)") Block input) throws IllegalArgumentException {
        UserAgentAnalyzer userAgentAnalyzer = threadLocalUserAgentAnalyzer.get();

        Map<String, String> requestHeaders = new TreeMap<>();
        int i = 0;

        int inputLength = input.getPositionCount();
        while (i < inputLength) {
            Slice parameterSlice = getSlice(input, i);
            if (parameterSlice == null) {
                throw new IllegalArgumentException("Null argument provided to ParseUserAgent.");
            }
            String parameter = parameterSlice.toStringUtf8();
            if (parameter.isEmpty()) {
                throw new IllegalArgumentException("Empty argument provided to ParseUserAgent.");
            }
            if (userAgentAnalyzer.supportedClientHintHeaders().stream().anyMatch(parameter::equalsIgnoreCase) ||
                USERAGENT_HEADER.equalsIgnoreCase(parameter)) {
                String value;
                if (i + 1 >= inputLength) {
                    throw new IllegalArgumentException("Invalid last element in argument list (was a header name which requires a value to follow)");
                } else {
                    value = getSlice(input, i+1).toStringUtf8();
                    i++;
                }
                requestHeaders.put(parameter, value);
                i++;
                continue;
            }
            if (i == 0) {
                requestHeaders.put(USERAGENT_HEADER, getSlice(input, i).toStringUtf8());
                i++;
                continue;
            }
            throw new IllegalArgumentException("Bad argument list for ParseUserAgent: \"" + parameter + "\"");
        }

        UserAgent userAgent = userAgentAnalyzer.parse(requestHeaders);

        Map<String, String> resultMap = userAgent.toMap(userAgentAnalyzer.getAllPossibleFieldNamesSorted());

        MapType mapType = new MapType(VARCHAR, VARCHAR, new TypeOperators());

        BlockBuilder blockBuilder = mapType.createBlockBuilder(null, resultMap.size());
        BlockBuilder singleMapBlockBuilder = blockBuilder.beginBlockEntry();
        for (Map.Entry<String, String> entry : resultMap.entrySet()) {
            VARCHAR.writeString(singleMapBlockBuilder, entry.getKey());
            VARCHAR.writeString(singleMapBlockBuilder, entry.getValue());
        }
        blockBuilder.closeEntry();

        return mapType.getObject(blockBuilder, blockBuilder.getPositionCount() - 1);
    }
}

