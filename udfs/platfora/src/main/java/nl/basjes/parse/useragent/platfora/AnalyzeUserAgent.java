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

import nl.basjes.parse.useragent.UserAgent;

import java.util.List;

public class AnalyzeUserAgent extends AbstractAnalyzeUserAgent {

    public String getFunctionName() {
        return "ANALYZE_USERAGENT";
    }

    public String getReturnType() {
        return "STRING";
    }

    public String[] getArgumentTypes() {
        return new String[]{"STRING", "STRING"};
    }

    public String getDescription() {
        List<String> fieldNames = getAllPossibleFieldNamesSorted();
        StringBuilder sb = new StringBuilder(1024);
        for (String fieldName: fieldNames) {
            sb.append('"').append(fieldName).append("\" ");
        }
        return "The ANALYZE_USERAGENT function returns the value of a single field from " +
            "the analysis of the specified useragent string. " +
            "Available field names are: " + sb;
    }

    public String getReturnValueDescription() {
        return getDescription();
    }

    public String getExampleUsage() {
        return "ANALYZE_USERAGENT( useragent , \"AgentName\" )";
    }

    public String compute(List arguments) {
        try {
            String useragent = (String) arguments.get(0);
            String attribute = (String) arguments.get(1);

            if (!getAllPossibleFieldNamesSorted().contains(attribute)){
                return "[[ERROR: The fieldname \""+attribute+"\" does not exist]]";
            }

            UserAgent parsedUserAgent = parseUserAgent(useragent);
            if (parsedUserAgent == null) {
                return null;
            }
            return parsedUserAgent.getValue(attribute);
        } catch (Exception e) {
            return "[[ERROR: Exception: "+ e.getMessage() + "]]";
        }
    }
}
