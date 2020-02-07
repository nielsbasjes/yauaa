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

package nl.basjes.parse.useragent.beam;

import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.apache.beam.sdk.transforms.SerializableFunction;

import java.util.Collections;
import java.util.Map;

import static nl.basjes.parse.useragent.UserAgentAnalyzer.DEFAULT_PARSE_CACHE_SIZE;

public class ParseUserAgent implements SerializableFunction<String, Map<String, String>> {
    private UserAgentAnalyzer userAgentAnalyzer = null;

    private int cacheSize;

    public ParseUserAgent() {
        this.cacheSize = DEFAULT_PARSE_CACHE_SIZE;
    }

    public ParseUserAgent(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    @Override
    public Map<String, String> apply(String input) {
        if (userAgentAnalyzer == null) {
            userAgentAnalyzer = UserAgentAnalyzer
                .newBuilder()
                .immediateInitialization()
                .hideMatcherLoadStats()
                .withCache(cacheSize)
//                .withFields() // TODO: Implement this later
                .build();
        }

        return userAgentAnalyzer.parse(input).toMap();
    }
}
