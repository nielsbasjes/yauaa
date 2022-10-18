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

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.servlet.ParseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static nl.basjes.parse.useragent.UserAgent.USERAGENT_HEADER;

@Controller
public class Analyze {

    private final ParseService parseService;

    @Autowired
    public Analyze(ParseService parseService) {
        this.parseService = parseService;
    }

    @QueryMapping
    public AnalysisResult analyze(@Argument RequestHeaders requestHeaders, @Argument String userAgentString) {
        UserAgentAnalyzer analyzer = parseService.getUserAgentAnalyzer();
        Map<String, String> inputHeaders = new TreeMap<>();
        if (requestHeaders != null) {
            inputHeaders.putAll(requestHeaders.toMap());
        }
        if (userAgentString != null) {
            inputHeaders.put(USERAGENT_HEADER, userAgentString);
        }
        UserAgent userAgent = analyzer.parse(inputHeaders);
        AnalysisResult result = new AnalysisResult(userAgent);
        result.setRequestHeaders(requestHeaders);
        return result;
    }

    @SchemaMapping
    public List<FieldResult> fields(AnalysisResult analysisResult, @Argument("fieldNames") List<String> fieldNames) {
        Map<String, FieldResult> resultFields = analysisResult.getFields();

        if (fieldNames == null || fieldNames.isEmpty()) {
            return new ArrayList<>(resultFields.values());
        }

        ArrayList<FieldResult> result = new ArrayList<>();
        for (String fieldName : fieldNames) {
            FieldResult fieldResult = resultFields.get(fieldName);
            if (fieldResult != null) {
                result.add(fieldResult);
            }
        }
        return result;
    }

    @SchemaMapping
    public FieldResult field(AnalysisResult analysisResult, @Argument("fieldName") String fieldName) {
        return analysisResult.getFields().get(fieldName);
    }

}
