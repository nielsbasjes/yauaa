/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2020 Niels Basjes
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

package nl.basjes.parse.useragent.flink.table;

import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.ScalarFunction;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static nl.basjes.parse.useragent.UserAgentAnalyzer.DEFAULT_PARSE_CACHE_SIZE;

public class AnalyzeUseragentFunction extends ScalarFunction {
    private transient UserAgentAnalyzer userAgentAnalyzer;

    private final int cacheSize;
    private final List<String> extractedFields;

    /**
     * Create a UserAgentAnalyzer that extracts only the specified fields
     * @param desiredFields The list of desired field names.
     */
    public AnalyzeUseragentFunction(String... desiredFields) {
        this(DEFAULT_PARSE_CACHE_SIZE, Arrays.asList(desiredFields));
    }

    /**
     * Create a UserAgentAnalyzer that extracts only the specified fields
     * @param desiredFields The list of desired field names.
     */
    public AnalyzeUseragentFunction(List<String> desiredFields) {
        this(DEFAULT_PARSE_CACHE_SIZE, desiredFields);
    }

    /**
     * Create a UserAgentAnalyzer that extracts only the specified fields
     * @param cacheSize The desired size of the cache.
     * @param desiredFields The list of desired field names.
     */
    public AnalyzeUseragentFunction(int cacheSize, String... desiredFields) {
        this(cacheSize, Arrays.asList(desiredFields));
    }

    /**
     * Create a UserAgentAnalyzer that extracts only the specified fields
     * @param cacheSize The desired size of the cache.
     * @param desiredFields The list of desired field names.
     */
    public AnalyzeUseragentFunction(int cacheSize, List<String> desiredFields) {
        this.cacheSize = cacheSize;
        this.extractedFields = desiredFields;
    }

    @Override
    public void open(FunctionContext context) {
        userAgentAnalyzer = UserAgentAnalyzer
            .newBuilder()
            .withFields(extractedFields)
            .withCache(cacheSize)
            .immediateInitialization()
            .build();
    }

    public Map<String, String> eval(String userAgentString) {
        return userAgentAnalyzer.parse(userAgentString).toMap(extractedFields);
    }

    @Override
    public TypeInformation<?> getResultType(Class<?>[] bla) {
        return Types.MAP(Types.STRING, Types.STRING);
    }
}
