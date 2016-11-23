/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2016 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.useragent.platfora;

import com.platfora.udf.UserDefinedFunction;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

import java.util.List;

public abstract class AbstractAnalyzeUserAgent implements UserDefinedFunction {

    private static UserAgentAnalyzer userAgentAnalyzer = null;
    private static List<String> allPossibleFieldNamesSorted = null;

    private UserAgentAnalyzer getAnalyzer() {
        if (userAgentAnalyzer == null) {
            userAgentAnalyzer = new UserAgentAnalyzer();
        }
        return userAgentAnalyzer;
    }

    protected List<String> getAllPossibleFieldNamesSorted() {
        if (allPossibleFieldNamesSorted == null) {
            allPossibleFieldNamesSorted = getAnalyzer().getAllPossibleFieldNamesSorted();
        }
        return allPossibleFieldNamesSorted;
    }

    protected String getAnalyzerVersion() {
        return UserAgentAnalyzer.getVersion();
    }

    public String getReturnType() {
        return "STRING";
    }

    protected UserAgent parseUserAgent(String useragent) {
        return getAnalyzer().parse(useragent);
    }

}
