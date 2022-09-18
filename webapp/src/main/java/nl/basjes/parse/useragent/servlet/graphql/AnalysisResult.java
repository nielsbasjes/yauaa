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

package nl.basjes.parse.useragent.servlet.graphql;

import lombok.Getter;
import lombok.Setter;
import nl.basjes.parse.useragent.UserAgent;

import java.util.Map;
import java.util.TreeMap;

//@Description("The analysis results of a User-Agent and other headers.")
public class AnalysisResult {

    private final UserAgent userAgent;

    public AnalysisResult(UserAgent userAgent) {
        this.userAgent = userAgent;
    }

    public UserAgent getUserAgent() {
        return userAgent;
    }

    public String getValue(String fieldName) {
        return userAgent.getValue(fieldName);
    }

    public Boolean getSyntaxError() {
        return Boolean.parseBoolean(userAgent.getValue("__SyntaxError__"));
    }

    private Map<String, FieldResult> fieldsMap = null;
    public synchronized Map<String, FieldResult> getFields(){
        if (fieldsMap == null) {
            fieldsMap = new TreeMap<>();

            for (String fieldName : userAgent.getAvailableFieldNamesSorted()) {
                fieldsMap.put(fieldName, new FieldResult(fieldName, userAgent.getValue(fieldName)));
            }
        }
        return fieldsMap;
    }

//    @Description("The provided input can be requested back (useful in batch analysis scenarios)")
    @Getter @Setter private RequestHeaders requestHeaders;

}
