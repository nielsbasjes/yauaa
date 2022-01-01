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

package nl.basjes.parse.useragent.snowflake;

import nl.basjes.parse.useragent.UserAgentAnalyzer;

import java.util.Map;

// CHECKSTYLE.OFF: HideUtilityClassConstructor because this is how Snowflake wants it.
public class ParseUserAgent {

    private static final UserAgentAnalyzer ANALYZER = UserAgentAnalyzer.newBuilder().dropTests().immediateInitialization().build();

    public static Map<String, String> parse(String useragent) {
        return ANALYZER.parse(useragent).toMap();
    }
}
