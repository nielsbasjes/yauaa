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

package nl.basjes.parse.useragent.flink.table;

import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.ScalarFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static nl.basjes.parse.useragent.UserAgent.USERAGENT_HEADER;
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
        this.extractedFields = new ArrayList<>(desiredFields);
    }

    @Override
    public void open(FunctionContext context) {
        userAgentAnalyzer = UserAgentAnalyzer
            .newBuilder()
            .withFields(extractedFields)
            .withCache(cacheSize)
            .immediateInitialization()
            .build();

        if (extractedFields.isEmpty()) {
            extractedFields.addAll(userAgentAnalyzer.getAllPossibleFieldNamesSorted());
        }
    }

    public Map<String, String> eval(String userAgentString) {
        return userAgentAnalyzer.parse(userAgentString).toMap(extractedFields);
    }

    public Map<String, String> eval(String... input) {
        if (input == null || input.length == 0) {
            throw new IllegalArgumentException("Input may not be null or empty.");
        }

        Map<String, String> requestHeaders = new TreeMap<>();

        if (input.length == 1) {
            // One value --> it is the user agent
            requestHeaders.put(USERAGENT_HEADER, input[0]);
        } else {
            // More than one we expect a key1, value1, key2, value2, etc list.
            if (input.length % 2 != 0) {
                throw new IllegalArgumentException("Input must be either 1 value (the User-Agent) or a key1, value1, key2, value2, etc list.");
            }
            for (int i = 0; i < input.length; i+=2) {
                requestHeaders.put(input[i], input[i+1]);
            }
        }

        return userAgentAnalyzer.parse(requestHeaders).toMap(extractedFields);
    }
}
