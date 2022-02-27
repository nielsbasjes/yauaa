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

package nl.basjes.parse.useragent.trino;

import io.airlift.slice.Slice;
import io.trino.spi.block.Block;
import io.trino.spi.block.BlockBuilder;
import io.trino.spi.function.Description;
import io.trino.spi.function.ScalarFunction;
import io.trino.spi.function.SqlType;
import io.trino.spi.type.MapType;
import io.trino.spi.type.StandardTypes;
import io.trino.spi.type.TypeOperators;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

import java.util.Map;

import static io.trino.spi.type.VarcharType.VARCHAR;

public final class ParseUserAgentFunction {

    private ParseUserAgentFunction() {
    }

    private static UserAgentAnalyzer userAgentAnalyzerInstance = null;

    private static UserAgentAnalyzer getInstance() {
        // NOTE: We currently cannot make an instance with only the wanted fields.
        //       We only know the required parameters the moment the call is done.
        //       At that point it is too late to create an optimized instance.
        if (userAgentAnalyzerInstance == null) {
            userAgentAnalyzerInstance = UserAgentAnalyzer
                .newBuilder()
                .immediateInitialization()
                .dropTests()
                .build();
        }
        return userAgentAnalyzerInstance;
    }

    @ScalarFunction("parse_user_agent")
    @Description("Parses the UserAgent into all possible pieces using the Yauaa library. https://yauaa.basjes.nl ")
    @SqlType("map(varchar, varchar)")
    public static Block parseUserAgent(@SqlType(StandardTypes.VARCHAR) Slice userAgentSlice) throws IllegalArgumentException {
        String userAgentStringToParse = null;
        if (userAgentSlice != null) {
            userAgentStringToParse = userAgentSlice.toStringUtf8();
        }

        UserAgentAnalyzer userAgentAnalyzer = getInstance();

        UserAgent userAgent = userAgentAnalyzer.parse(userAgentStringToParse);
        Map<String, String> resultMap = userAgent.toMap(userAgent.getAvailableFieldNamesSorted());

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

