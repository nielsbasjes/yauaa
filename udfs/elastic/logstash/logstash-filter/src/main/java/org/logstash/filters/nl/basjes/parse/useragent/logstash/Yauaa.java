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

package org.logstash.filters.nl.basjes.parse.useragent.logstash;

import co.elastic.logstash.api.Configuration;
import co.elastic.logstash.api.Context;
import co.elastic.logstash.api.Event;
import co.elastic.logstash.api.Filter;
import co.elastic.logstash.api.FilterMatchListener;
import co.elastic.logstash.api.LogstashPlugin;
import co.elastic.logstash.api.PluginConfigSpec;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.UserAgentAnalyzer.UserAgentAnalyzerBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static nl.basjes.parse.useragent.UserAgent.MutableUserAgent.isSystemField;
import static nl.basjes.parse.useragent.UserAgent.USERAGENT_HEADER;

@LogstashPlugin(name = "yauaa")
public class Yauaa implements Filter {

    private static final Logger LOG = LogManager.getLogger(Yauaa.class);

    private final String            id;
    private final UserAgentAnalyzer userAgentAnalyzer;

    private final List<String> requestedFieldNames = new ArrayList<>();

    public static final PluginConfigSpec<String> SOURCE_CONFIG =
        PluginConfigSpec.stringSetting("source");

    public static final PluginConfigSpec<Map<String, Object>> FIELDS_CONFIG =
        PluginConfigSpec.hashSetting("fields");

    private final Map<String, String> fieldToHeaderMappings;
    private       Map<String, String> outputFields;

    private static final List<String> ALL_ALLOWED_HEADERS = getAllAllowedHeaders();

    private static List<String> getAllAllowedHeaders() {
        ArrayList<String> headers = new ArrayList<>();
        headers.add(USERAGENT_HEADER);
        headers.addAll(UserAgentAnalyzer.newBuilder().build().supportedClientHintHeaders());
        return headers;
    }


    @SuppressWarnings("unused") // The constructor MUST have this parameter list
    public Yauaa(String id, Configuration config, Context context) {
        this.id = id;
        // constructors should validate configuration options

        final Map<String, Object> requestedFields = config.get(FIELDS_CONFIG);

        if (requestedFields != null) {
            outputFields = new HashMap<>();
            requestedFields.forEach((key, value) -> outputFields.put(key, value.toString()));
        }

        fieldToHeaderMappings = new TreeMap<>();
        Object source = config.getRawValue(SOURCE_CONFIG);
        if (source instanceof String) {
            if (!source.toString().trim().isEmpty()) {
                fieldToHeaderMappings.put(source.toString(), USERAGENT_HEADER);
            }
        }
        if (source instanceof Map) {
            Map<?, ?> sourceMap = ((Map<?, ?>) source);
            for (Map.Entry<?, ?> entry : sourceMap.entrySet()) {
                String field = entry.getKey().toString();
                String header = entry.getValue().toString();
                if (isSupportedHeader(ALL_ALLOWED_HEADERS, header)) {
                    fieldToHeaderMappings.put(field, header);
                }
            }
        }

        checkConfiguration();

        UserAgentAnalyzerBuilder userAgentAnalyzerBuilder =
            UserAgentAnalyzer
                .newBuilder()
                .immediateInitialization()
                .dropTests()
                .hideMatcherLoadStats();

        outputFields.forEach((yauaaFieldName, outputFieldName) -> {
            requestedFieldNames.add(yauaaFieldName);
            userAgentAnalyzerBuilder.withField(yauaaFieldName);
        });

        userAgentAnalyzer = userAgentAnalyzerBuilder.build();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Collection<Event> filter(Collection<Event> events, FilterMatchListener filterMatchListener) {
        for (Event event : events) {
            Map<String, String> headers = new TreeMap<>();
            for (Map.Entry<String, String> entry : fieldToHeaderMappings.entrySet()) {
                Object rawField = event.getField(entry.getKey());
                if (rawField instanceof String) {
                    headers.put(entry.getValue(), rawField.toString());
                }
            }
            UserAgent agent = userAgentAnalyzer.parse(headers);

            for (String fieldName : requestedFieldNames) {
                event.setField(outputFields.get(fieldName), agent.getValue(fieldName));
            }
        }
        return events;
    }

    @Override
    public Collection<PluginConfigSpec<?>> configSchema() {
        return Arrays.asList(SOURCE_CONFIG, FIELDS_CONFIG);
    }

    private void checkConfiguration() {
        List<String> configProblems = new ArrayList<>();

        UserAgentAnalyzer uaa = UserAgentAnalyzer
            .newBuilder()
            .delayInitialization()
            .dropTests()
            .hideMatcherLoadStats()
            .showMinimalVersion()
            .build();

        List<String> allFieldNames = uaa.getAllPossibleFieldNamesSorted();

        if (fieldToHeaderMappings == null || fieldToHeaderMappings.isEmpty()) {
            configProblems.add("The \"source\" map has not been specified.\n");
        }

        if (outputFields == null) {
            configProblems.add("The list of needed \"fields\" has not been specified.\n");
        } else {
            if (outputFields.isEmpty()) {
                configProblems.add("The list of needed \"fields\" is empty.\n");
            }
            for (String outputField: outputFields.keySet()) {
                if (!allFieldNames.contains(outputField)) {
                    configProblems.add("The requested field \"" + outputField + "\" does not exist.\n");
                }
            }
        }

        if (configProblems.isEmpty()) {
            return; // All is fine
        }

        StringBuilder errorMessage = new StringBuilder();

        int maxNameLength = 0;
        for (String field: allFieldNames) {
            maxNameLength = Math.max(maxNameLength, field.length());
        }
        for (String header: ALL_ALLOWED_HEADERS) {
            maxNameLength = Math.max(maxNameLength, header.length());
        }

        errorMessage.append("\nThe Yauaa filter config is invalid.\n");
        errorMessage.append("The problems we found:\n");

        configProblems.forEach(problem -> errorMessage.append("- ").append(problem).append('\n'));

        errorMessage.append("\n");
        errorMessage.append("Example of a generic valid config:\n");
        errorMessage.append("\n");
        errorMessage.append("filter {\n");
        errorMessage.append("   yauaa {\n");
        errorMessage.append("       source => {\n");
        for (String header : ALL_ALLOWED_HEADERS) {
            String syntheticFieldname = header.replace("-", "_").toLowerCase(Locale.ROOT).replace("sec_ch_", "");
            errorMessage.append("           \"").append(syntheticFieldname).append("\"");
            for (int i = syntheticFieldname.length(); i < maxNameLength; i++) {
                errorMessage.append(' ');
            }
            errorMessage.append("  => \"").append(header).append("\"\n");
        }

        errorMessage.append("       }\n");
        errorMessage.append("       fields => {\n");

        for (String field: allFieldNames) {
            if (!isSystemField(field)) {
                errorMessage.append("           \"").append(field).append("\"");
                for (int i = field.length(); i < maxNameLength; i++) {
                    errorMessage.append(' ');
                }
                errorMessage.append("  => \"userAgent").append(field).append("\"\n");
            }
        }
        errorMessage.append("       }\n");
        errorMessage.append("   }\n");
        errorMessage.append("}\n");
        errorMessage.append("\n");

        LOG.error("{}", errorMessage);
        throw new IllegalArgumentException(errorMessage.toString());
    }

    private boolean isSupportedHeader(List<String> supportedHeaders, String headerName) {
        for (String allowedHeader : supportedHeaders) {
            if (allowedHeader.equalsIgnoreCase(headerName)) {
                return true;
            }
        }
        return false;
    }

}
