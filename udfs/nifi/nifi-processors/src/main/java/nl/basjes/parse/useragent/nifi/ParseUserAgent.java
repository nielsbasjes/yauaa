/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2024 Niels Basjes
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

package nl.basjes.parse.useragent.nifi;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.UserAgentAnalyzer.UserAgentAnalyzerBuilder;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.SideEffectFree;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static nl.basjes.parse.useragent.UserAgent.USERAGENT_HEADER;
import static nl.basjes.parse.useragent.nifi.ParseUserAgent.USERAGENTSTRING_ATTRIBUTENAME;

@SideEffectFree
@Tags({"logs", "useragent", "webanalytics"})
@CapabilityDescription("Extract attributes from the UserAgent string.")
@ReadsAttributes({@ReadsAttribute(attribute=USERAGENTSTRING_ATTRIBUTENAME, description="The useragent string that is to be analyzed.")})
public class ParseUserAgent extends AbstractProcessor {

    static final String USERAGENTSTRING_ATTRIBUTENAME = "UseragentString";
    static final String HEADER_PREFIX                 = "RequestHeader.";
    static final String FIELD_PREFIX                  = "Extract.";
    static final String ATTRIBUTE_PREFIX              = "Useragent.";

    public static final Relationship SUCCESS = new Relationship.Builder()
        .name("success")
        .description("Here we route all FlowFiles that have been analyzed.")
        .build();

    public static final Relationship MISSING = new Relationship.Builder()
        .name("missing")
        .description("Here we route the FlowFiles that did not have a value for the " + USERAGENT_HEADER + " request header.")
        .build();

    private Set<Relationship> relationships;

    private UserAgentAnalyzer uaa = null;

    private static final List<String>             ALL_CLIENT_HINT_HEADERS_NAMES = new ArrayList<>();
    private static final List<String>             ALL_FIELD_NAMES               = new ArrayList<>();
    private final        List<PropertyDescriptor> supportedPropertyDescriptors  = new ArrayList<>();
    private final        Map<String, String>      requestHeadersProperties      = new LinkedHashMap<>(); // Map<RequestHeaderProperty, RequestHeaderName>
    private final        Map<String, String>      requestHeadersMapping         = new TreeMap<>(); // Map<RequestHeaderName, AttributeName>
    private final        List<String>             extractFieldNames             = new ArrayList<>();

    @Override
    protected void init(ProcessorInitializationContext context) {
        super.init(context);

        synchronized (ALL_FIELD_NAMES) {
            if (ALL_FIELD_NAMES.isEmpty()) {
                UserAgentAnalyzer analyzer = UserAgentAnalyzer
                    .newBuilder()
                    .hideMatcherLoadStats()
                    .delayInitialization()
                    .dropTests()
                    .build();

                ALL_CLIENT_HINT_HEADERS_NAMES.addAll(analyzer.supportedClientHintHeaders());
                ALL_FIELD_NAMES.addAll(analyzer.getAllPossibleFieldNamesSorted());
            }
        }

        final Set<Relationship> relationshipsSet = new HashSet<>();
        relationshipsSet.add(SUCCESS);
        relationshipsSet.add(MISSING);
        this.relationships = Collections.unmodifiableSet(relationshipsSet);

        requestHeadersProperties.put(HEADER_PREFIX + USERAGENT_HEADER.replaceAll("[^a-zA-Z0-9]", ""), USERAGENT_HEADER);
        for (String headerName: ALL_CLIENT_HINT_HEADERS_NAMES) {
            requestHeadersProperties.put(HEADER_PREFIX + headerName.replaceAll("[^a-zA-Z0-9]", ""), headerName);
        }

        for (Map.Entry<String, String> entry: requestHeadersProperties.entrySet()) {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor.Builder()
                .name(entry.getKey())
                .description("Which attribute holds the value from the " + entry.getValue() + " request header?")
                .required(USERAGENT_HEADER.equals(entry.getValue()))
                .defaultValue(USERAGENT_HEADER.equals(entry.getValue()) ? USERAGENTSTRING_ATTRIBUTENAME : null)
                .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
                .build();
            supportedPropertyDescriptors.add(propertyDescriptor);
        }

        for (String fieldName: ALL_FIELD_NAMES) {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor.Builder()
                .name(FIELD_PREFIX + fieldName)
                .description("If enabled will extract the " + fieldName + " field")
                .required(true)
                .allowableValues("true", "false")
                .defaultValue("false")
                .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
                .build();
            supportedPropertyDescriptors.add(propertyDescriptor);
        }

    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return supportedPropertyDescriptors;
    }

    @SuppressWarnings("unused") // Called via the annotation
    @OnScheduled
    public void onSchedule(ProcessContext context) {
        if (uaa == null) {
            UserAgentAnalyzerBuilder builder =
                UserAgentAnalyzer
                .newBuilder()
                .hideMatcherLoadStats()
                .dropTests();

            extractFieldNames.clear();

            for (PropertyDescriptor propertyDescriptor: supportedPropertyDescriptors) {
                String name = propertyDescriptor.getName();
                if (name.startsWith(FIELD_PREFIX)) { // Do we need this field?
                    if (Boolean.TRUE.equals(context.getProperty(propertyDescriptor).asBoolean())) {
                        String fieldName = name.substring(FIELD_PREFIX.length());
                        builder.withField(fieldName);
                        extractFieldNames.add(fieldName);
                    }
                }
                if (name.startsWith(HEADER_PREFIX)) { // Do we need this field?
                    String value = context.getProperty(propertyDescriptor).getValue();
                    if (value != null && !value.trim().isEmpty()) {
                        requestHeadersMapping.put(requestHeadersProperties.get(name), value);
                    }
                }
            }

            uaa = builder.build();
        }
    }

    @Override
    public void onPropertyModified(PropertyDescriptor descriptor, String oldValue, String newValue) {
        uaa = null;
    }

    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException { // NOSONAR: Explicitly name the exception
        FlowFile flowFile = session.get();

        Map<String, String> requestHeaders = new TreeMap<>();

        for (Map.Entry<String, String> entry : requestHeadersMapping.entrySet()) {
            String attributeName = entry.getValue();
            String value = flowFile.getAttribute(attributeName);
            requestHeaders.put(entry.getKey(), value);
        }

        if (requestHeaders.get(USERAGENT_HEADER) == null) {
            session.transfer(flowFile, MISSING);
        } else {
            UserAgent userAgent = uaa.parse(requestHeaders);

            for (String fieldName : extractFieldNames) {
                String fieldValue = userAgent.getValue(fieldName);
                flowFile = session.putAttribute(flowFile, ATTRIBUTE_PREFIX + fieldName, fieldValue);
            }
            session.transfer(flowFile, SUCCESS);
        }
        session.commitAsync();
    }

}
