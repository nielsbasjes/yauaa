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

package nl.basjes.parse.useragent.nifi;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.UserAgentAnalyzer.UserAgentAnalyzerBuilder;
import org.apache.nifi.annotation.behavior.EventDriven;
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
import java.util.List;
import java.util.Set;

import static nl.basjes.parse.useragent.nifi.ParseUserAgent.USERAGENTSTRING_ATTRIBUTENAME;

@EventDriven
@SideEffectFree
@Tags({"logs", "useragent", "webanalytics"})
@CapabilityDescription("Extract attributes from the UserAgent string.")
@ReadsAttributes({@ReadsAttribute(attribute=USERAGENTSTRING_ATTRIBUTENAME, description="The useragent string that is to be analyzed.")})
public class ParseUserAgent extends AbstractProcessor {

    static final String USERAGENTSTRING_ATTRIBUTENAME = "UseragentString";
    static final String PROPERTY_PREFIX = "Extract.";
    static final String ATTRIBUTE_PREFIX = "Useragent.";

    public static final Relationship SUCCESS = new Relationship.Builder()
        .name("success")
        .description("Here we route all FlowFiles that have been analyzed.")
        .build();

    public static final Relationship MISSING = new Relationship.Builder()
        .name("missing")
        .description("Here we route the FlowFiles that did not have the " + USERAGENTSTRING_ATTRIBUTENAME + " attribute set.")
        .build();

    private Set<Relationship> relationships;

    private UserAgentAnalyzer uaa = null;

    private static final List<String> ALL_FIELD_NAMES = new ArrayList<>();
    private List<PropertyDescriptor> supportedPropertyDescriptors = new ArrayList<>();
    private List<String> extractFieldNames = new ArrayList<>();

    @Override
    protected void init(ProcessorInitializationContext context) {
        super.init(context);

        synchronized (ALL_FIELD_NAMES) {
            if (ALL_FIELD_NAMES.isEmpty()) {
                ALL_FIELD_NAMES.addAll(UserAgentAnalyzer
                    .newBuilder()
                    .hideMatcherLoadStats()
                    .delayInitialization()
                    .dropTests()
                    .build()
                    .getAllPossibleFieldNamesSorted());
            }
        }
        final Set<Relationship> relationshipsSet = new HashSet<>();
        relationshipsSet.add(SUCCESS);
        relationshipsSet.add(MISSING);
        this.relationships = Collections.unmodifiableSet(relationshipsSet);

        for (String fieldName: ALL_FIELD_NAMES) {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor.Builder()
                .name(PROPERTY_PREFIX + fieldName)
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

    @OnScheduled
    public void onSchedule(ProcessContext context) {
        if (uaa == null) {
            UserAgentAnalyzerBuilder<? extends UserAgentAnalyzer, ? extends UserAgentAnalyzerBuilder> builder =
                UserAgentAnalyzer
                .newBuilder()
                .hideMatcherLoadStats()
                .dropTests();

            extractFieldNames.clear();

            for (PropertyDescriptor propertyDescriptor: supportedPropertyDescriptors) {
                if (context.getProperty(propertyDescriptor).asBoolean()) {
                    String name = propertyDescriptor.getName();
                    if (name.startsWith(PROPERTY_PREFIX)) { // Should always pass
                        String fieldName = name.substring(PROPERTY_PREFIX.length());

                        builder.withField(fieldName);
                        extractFieldNames.add(fieldName);
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
        String userAgentString = flowFile.getAttribute(USERAGENTSTRING_ATTRIBUTENAME);
        if (userAgentString == null) {
            session.transfer(flowFile, MISSING);
        } else {
            UserAgent userAgent = uaa.parse(userAgentString);
            for (String fieldName : extractFieldNames) {
                String fieldValue = userAgent.getValue(fieldName);
                flowFile = session.putAttribute(flowFile, ATTRIBUTE_PREFIX + fieldName, fieldValue);
            }
            session.transfer(flowFile, SUCCESS);
        }
        session.commit();
    }

}
