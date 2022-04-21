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

package org.elasticsearch.plugin.ingest.yauaa;

import nl.basjes.parse.useragent.AbstractUserAgentAnalyzer.CacheInstantiator;
import nl.basjes.parse.useragent.AbstractUserAgentAnalyzer.ClientHintsCacheInstantiator;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.UserAgentAnalyzer.UserAgentAnalyzerBuilder;
import org.apache.commons.collections4.map.LRUMap;
import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static nl.basjes.parse.useragent.UserAgent.USERAGENT_FIELDNAME;
import static org.elasticsearch.ingest.ConfigurationUtils.readIntProperty;
import static org.elasticsearch.ingest.ConfigurationUtils.readOptionalList;
import static org.elasticsearch.ingest.ConfigurationUtils.readOptionalMap;
import static org.elasticsearch.ingest.ConfigurationUtils.readOptionalStringProperty;
import static org.elasticsearch.ingest.ConfigurationUtils.readStringProperty;

public class YauaaProcessor extends AbstractProcessor {

    public static final String TYPE = "yauaa";

    private final Map<String, String> fieldToHeaderMapping;
    private final String targetField;

    private final UserAgentAnalyzer uaa;

    YauaaProcessor(String tag,
                   String description,
                   Map<String, String> fieldToHeaderMapping,
                   String targetField,
                   UserAgentAnalyzer uaa) {
        super(tag, description);
        this.fieldToHeaderMapping = fieldToHeaderMapping;
        this.targetField = targetField;
        this.uaa = uaa;
    }

    @Override
    public IngestDocument execute(IngestDocument ingestDocument) {
        Map<String, String> headers = new TreeMap<>();

        for (Map.Entry<String, String> entry : fieldToHeaderMapping.entrySet()) {
            String content = ingestDocument.getFieldValue(entry.getKey(), String.class);
            if (content != null) {
                headers.put(entry.getValue(), content);
            }
        }

        UserAgent userAgent = uaa.parse(headers);

        Map<String, String> resultMap = userAgent.toMap();
        resultMap.remove(USERAGENT_FIELDNAME);
        ingestDocument.setFieldValue(targetField, resultMap);
        return ingestDocument;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static final class Factory implements Processor.Factory {

        @Override
        public Processor create(Map<String, Processor.Factory> processorFactories, String tag, String description, Map<String, Object> config) {
            String                  field                       = readOptionalStringProperty(TYPE, tag, config, "field"); // Deprecated
            Map<String, String>     fieldToHeaderMappingConfig  = readOptionalMap(TYPE, tag, config, "field_to_header_mapping");
            String                  targetField                 = readStringProperty(TYPE, tag, config, "target_field", "user_agent");
            List<String>            fieldNames                  = readOptionalList(TYPE, tag, config, "fieldNames");
            Integer                 cacheSize                   = readIntProperty(TYPE, tag, config, "cacheSize", -1);
            Integer                 preheat                     = readIntProperty(TYPE, tag, config, "preheat", -1);
            String                  extraRules                  = readOptionalStringProperty(TYPE, tag, config, "extraRules");

            UserAgentAnalyzerBuilder builder = UserAgentAnalyzer
                .newBuilder()
                .dropTests()
                .immediateInitialization()

                // With the default Caffeine based cache an exception occurs at startup:
                // Caused by: java.security.AccessControlException: access denied ("java.lang.reflect.ReflectPermission" "suppressAccessChecks")
                //         at java.base/java.security.AccessControlContext.checkPermission(AccessControlContext.java:485)
                //         at java.base/java.security.AccessController.checkPermission(AccessController.java:1068)
                //         at java.base/java.lang.SecurityManager.checkPermission(SecurityManager.java:416)
                //         at java.base/java.lang.invoke.MethodHandles.privateLookupIn(MethodHandles.java:233)
                //         at com.github.benmanes.caffeine.cache.BaseMpscLinkedArrayQueue.<clinit>(MpscGrowableArrayQueue.java:662)
                // ...
                .withCacheInstantiator(
                    (CacheInstantiator) size ->
                        Collections.synchronizedMap(new LRUMap<>(size)))
                .withClientHintCacheInstantiator(
                    (ClientHintsCacheInstantiator<?>) size ->
                        Collections.synchronizedMap(new LRUMap<>(size)));

            if (cacheSize >= 0) {
                builder.withCache(cacheSize);
            }

            if (preheat >= 0) {
                builder.preheat(preheat);
            }

            if (extraRules != null) {
                builder.addYamlRule(extraRules);
            }

            if (fieldNames != null && !fieldNames.isEmpty()) {
                builder.withFields(fieldNames);
            }

            UserAgentAnalyzer userAgentAnalyzer = builder.build();

            List<String> supportedHeaders = new ArrayList<>(userAgentAnalyzer.supportedClientHintHeaders());
            supportedHeaders.add("User-Agent");
            Map<String, String> fieldToHeaderMapping = new TreeMap<>();
            if (fieldToHeaderMappingConfig != null) {
                for (Map.Entry<String, String> entry : fieldToHeaderMappingConfig.entrySet()) {
                    boolean supportedHeaderName = false;
                    String wantedHeader = entry.getValue();
                    for (String allowedHeader : supportedHeaders) {
                        if (allowedHeader.equalsIgnoreCase(wantedHeader)) {
                            supportedHeaderName = true;
                            break;
                        }
                    }
                    if (!supportedHeaderName) {
                        throw new IllegalArgumentException("The provided header name \"" + wantedHeader + "\"is not allowed." +
                            "Allowed header names are: " + supportedHeaders);
                    }
                    fieldToHeaderMapping.put(entry.getKey(), entry.getValue());
                }
            }
            if (field != null) {
                fieldToHeaderMapping.put(field, "User-Agent");
            }

            return new YauaaProcessor(tag, description, fieldToHeaderMapping, targetField, userAgentAnalyzer);
        }
    }
}
