/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2019 Niels Basjes
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

package org.logstash.filters.nl.basjes.parse.useragent.logstash;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;
import co.elastic.logstash.api.v0.Filter;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.UserAgentAnalyzer.UserAgentAnalyzerBuilder;
import org.logstash.Event;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@LogstashPlugin(name = "yauaa")
public class Yauaa implements Filter {

//    private static final Logger LOG = LoggerFactory.getLogger(Yauaa.class);

    private UserAgentAnalyzer userAgentAnalyzer;

    private List<String> requestedFieldNames = new ArrayList<>();


    public static final PluginConfigSpec<String> SOURCE_CONFIG =
        Configuration.requiredStringSetting("source");

    public static final PluginConfigSpec<Map<String, String>> FIELDS_CONFIG =
        Configuration.hashSetting("fields");

    private String sourceField;
    private Map<String, String> outputFields;

    public Yauaa(Configuration config, Context context) {
        // constructors should validate configuration options
        sourceField = config.get(SOURCE_CONFIG);
        outputFields = config.get(FIELDS_CONFIG);

        UserAgentAnalyzerBuilder<?, ?> userAgentAnalyzerBuilder =
            UserAgentAnalyzer
                .newBuilder()
                .delayInitialization()
                .dropTests()
                .hideMatcherLoadStats();

        outputFields.forEach((yauaaFieldName, outputFieldName) -> {
            requestedFieldNames.add(yauaaFieldName);
            userAgentAnalyzerBuilder.withField(yauaaFieldName);
        });

        userAgentAnalyzer = userAgentAnalyzerBuilder.build();
        userAgentAnalyzer.initializeMatchers();
    }

    @Override
    public Collection<Event> filter(Collection<Event> events) {
        for (Event event : events) {
            Object rawField = event.getField(sourceField);
            if (rawField instanceof String) {
                String userAgentString = (String)rawField;

                UserAgent agent = userAgentAnalyzer.parse(userAgentString);

                for (String fieldName : requestedFieldNames) {
                    event.setField(outputFields.get(fieldName), agent.getValue(fieldName));
                }
            }
        }
        return events;
    }

    @Override
    public Collection<PluginConfigSpec<?>> configSchema() {
        return Arrays.asList(SOURCE_CONFIG, FIELDS_CONFIG);
    }
}
