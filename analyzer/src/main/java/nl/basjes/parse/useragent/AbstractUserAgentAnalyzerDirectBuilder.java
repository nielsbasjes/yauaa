/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2021 Niels Basjes
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

package nl.basjes.parse.useragent;

import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import nl.basjes.parse.useragent.calculate.CalculateAgentClass;
import nl.basjes.parse.useragent.calculate.CalculateAgentEmail;
import nl.basjes.parse.useragent.calculate.CalculateAgentName;
import nl.basjes.parse.useragent.calculate.CalculateDeviceBrand;
import nl.basjes.parse.useragent.calculate.CalculateDeviceName;
import nl.basjes.parse.useragent.calculate.CalculateNetworkType;
import nl.basjes.parse.useragent.calculate.ConcatNONDuplicatedCalculator;
import nl.basjes.parse.useragent.calculate.FieldCalculator;
import nl.basjes.parse.useragent.calculate.MajorVersionCalculator;
import nl.basjes.parse.useragent.config.ConfigLoader;
import nl.basjes.parse.useragent.utils.CheckLoggingDependencies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.AGENT_VERSION;
import static nl.basjes.parse.useragent.UserAgent.AGENT_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_CLASS;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_NAME;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_VERSION;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_NAME;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_VERSION;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.SET_ALL_FIELDS;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_NAME;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_VERSION;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_VERSION_MAJOR;
import static nl.basjes.parse.useragent.config.ConfigLoader.DEFAULT_RESOURCES;

@SuppressWarnings("unchecked") // For all the casts of 'this' to 'B'
public abstract class AbstractUserAgentAnalyzerDirectBuilder<UAA extends AbstractUserAgentAnalyzerDirect, B extends AbstractUserAgentAnalyzerDirectBuilder<UAA, B>> {
    private final UAA uaa;
    private boolean didBuildStep = false;
    private int preheatIterations = 0;

    private final List<String>          resources         = new ArrayList<>();
    private final List<String>          optionalResources = new ArrayList<>();
    private final List<String>          yamlRules         = new ArrayList<>();
    private final List<FieldCalculator> fieldCalculators  = new ArrayList<>();

    protected void failIfAlreadyBuilt() {
        if (didBuildStep) {
            throw new IllegalStateException("A builder can provide only a single instance. It is not allowed to set values after doing build()");
        }
    }

    protected AbstractUserAgentAnalyzerDirectBuilder(UAA newUaa) {
        this.uaa = newUaa;
        this.uaa.setShowMatcherStats(false);
        resources.add(DEFAULT_RESOURCES);
    }

    /**
     * Drop the default set of rules. Useful in parsing ONLY company specific useragents.
     * @return the current Builder instance.
     */
    public B dropDefaultResources() {
        failIfAlreadyBuilt();
        resources.remove(DEFAULT_RESOURCES);
        return (B)this;
    }

    /**
     * Add a set of additional rules. Useful in handling specific cases.
     * @param resourceString The resource list that needs to be added.
     * @return the current Builder instance.
     */
    public B addResources(String resourceString) {
        failIfAlreadyBuilt();
        resources.add(resourceString);
        return (B)this;
    }

    /**
     * Add a set of additional rules. Useful in handling specific cases.
     * The startup will continue even if these do not exist.
     * @param resourceString The resource list that should to be added.
     * @return the current Builder instance.
     */
    public B addOptionalResources(String resourceString) {
        failIfAlreadyBuilt();
        optionalResources.add(resourceString);
        return (B)this;
    }

    /**
     * Add a set of additional rules. Useful in handling specific cases.
     * The startup will continue even if these do not exist.
     * @param yamlRule The Yaml expression that should to be added.
     * @return the current Builder instance.
     */
    public B addYamlRule(String yamlRule) {
        failIfAlreadyBuilt();
        yamlRules.add(yamlRule);
        return (B)this;
    }

    /**
     * Use the available testcases to preheat the jvm on this analyzer.
     * @param iterations How many testcases must be run
     * @return the current Builder instance.
     */
    public B preheat(int iterations) {
        failIfAlreadyBuilt();
        this.preheatIterations = iterations;
        return (B)this;
    }

    /**
     * Use the available testcases to preheat the jvm on this analyzer.
     * All available testcases will be run exactly once.
     * @return the current Builder instance.
     */
    public B preheat() {
        failIfAlreadyBuilt();
        this.preheatIterations = -1;
        return (B)this;
    }

    /**
     * Specify an additional field that we want to retrieve.
     * @param fieldName The name of the additional field
     * @return the current Builder instance.
     */
    public B withField(String fieldName) {
        failIfAlreadyBuilt();
        if (uaa.wantedFieldNames == null) {
            uaa.wantedFieldNames = new TreeSet<>(); // This avoids duplicates and maintains ordering.
        }
        uaa.wantedFieldNames.add(fieldName);
        return (B)this;
    }

    /**
     * Specify a set of additional fields that we want to retrieve.
     * @param fieldNames The collection of names of the additional fields
     * @return the current Builder instance.
     */
    public B withFields(Collection<String> fieldNames) {
        for (String fieldName : fieldNames) {
            withField(fieldName);
        }
        return (B)this;
    }

    /**
     * Specify a set of additional fields that we want to retrieve.
     * @param fieldNames The array of names of the additional fields
     * @return the current Builder instance.
     */
    public B withFields(String... fieldNames) {
        for (String fieldName : fieldNames) {
            withField(fieldName);
        }
        return (B)this;
    }

    /**
     * Specify that we simply want to retrieve all possible fields.
     * @return the current Builder instance.
     */
    public B withAllFields() {
        failIfAlreadyBuilt();
        uaa.wantedFieldNames = null;
        return (B)this;
    }

    /**
     * Log additional information during the startup of the analyzer.
     * @return the current Builder instance.
     */
    public B showMatcherLoadStats() {
        failIfAlreadyBuilt();
        uaa.setShowMatcherStats(true);
        return (B)this;
    }

    /**
     * Set the stats logging during the startup of the analyzer back to the default of "minimal".
     * @return the current Builder instance.
     */
    public B hideMatcherLoadStats() {
        failIfAlreadyBuilt();
        uaa.setShowMatcherStats(false);
        return (B)this;
    }

    /**
     * Set maximum length of a useragent for it to be classified as Hacker without any analysis.
     * @param newUserAgentMaxLength The new maximum length of a useragent for it to be classified as Hacker without any analysis.
     * @return the current Builder instance.
     */
    public B withUserAgentMaxLength(int newUserAgentMaxLength) {
        failIfAlreadyBuilt();
        uaa.setUserAgentMaxLength(newUserAgentMaxLength);
        return (B)this;
    }

    /**
     * Retain all testcases in memory after initialization.
     * @return the current Builder instance.
     */
    public B keepTests() {
        failIfAlreadyBuilt();
        uaa.keepTests();
        return (B)this;
    }

    /**
     * Remove all testcases in memory after initialization.
     * @return the current Builder instance.
     */
    public B dropTests() {
        failIfAlreadyBuilt();
        uaa.dropTests();
        return (B)this;
    }

    /**
     * Load all patterns and rules but do not yet build the lookup hashMaps yet.
     * For the engine to run these lookup hashMaps are needed so they will be constructed once "just in time".
     * @return the current Builder instance.
     */
    public B delayInitialization() {
        failIfAlreadyBuilt();
        uaa.delayInitialization();
        return (B)this;
    }

    /**
     * Load all patterns and rules and immediately build the lookup hashMaps.
     * @return the current Builder instance.
     */
    public B immediateInitialization() {
        failIfAlreadyBuilt();
        uaa.immediateInitialization();
        return (B)this;
    }

    protected final Set<String> allFieldsForWhichACalculatorExists = new HashSet<>();
    protected final Set<String> dependenciesNeededByCalculators = new HashSet<>();

    private void registerFieldCalculator(FieldCalculator fieldCalculator) {
        String calculatedFieldName = fieldCalculator.getCalculatedFieldName();
        allFieldsForWhichACalculatorExists.add(calculatedFieldName);
        if (uaa.isWantedField(calculatedFieldName) ||
            dependenciesNeededByCalculators.contains(calculatedFieldName)) {
            fieldCalculators.add(fieldCalculator);
            dependenciesNeededByCalculators.addAll(fieldCalculator.getDependencies());
        }
    }

    protected void verifyCalculatorDependencyOrdering() {
        // Verify calculator dependencies ordering
        Set<String> seenCalculatedFields = new HashSet<>();
        for (FieldCalculator fieldCalculator: fieldCalculators) {
            for (String dependency: fieldCalculator.getDependencies()){
                if (allFieldsForWhichACalculatorExists.contains(dependency)) {
                    if (!seenCalculatedFields.contains(dependency)) {
                        throw new InvalidParserConfigurationException(
                            "Calculator ordering is wrong:" +
                                "For " + fieldCalculator.getCalculatedFieldName() +
                                " we need " + dependency + " which is a " +
                                "calculated field but it has not yet been calculated.");
                    }
                }
            }
            seenCalculatedFields.add(fieldCalculator.getCalculatedFieldName());
        }
    }

    /**
     * Construct the analyzer and run the preheat (if requested).
     * @return the new analyzer instance.
     */
    public UAA build() {
        failIfAlreadyBuilt();
        uaa.initTransientFields();

        // Before we initialize we check if the logging has been setup correctly.
        // Not all useragents trigger the same logging libraries because some
        // of the logging libraries are only used in specific analysis code.
        // This is a "fail fast" to ensure any problems happen even before startup.
        CheckLoggingDependencies.verifyLoggingDependencies();

        // In case we only want specific fields we must all these special cases too
        if (uaa.wantedFieldNames != null) {
            // Special field that affects ALL fields.
            uaa.wantedFieldNames.add(SET_ALL_FIELDS);

            // This is always needed to determine the Hacker fallback
            uaa.wantedFieldNames.add(DEVICE_CLASS);
        }

        registerFieldCalculator(new ConcatNONDuplicatedCalculator(AGENT_NAME_VERSION_MAJOR,         AGENT_NAME,             AGENT_VERSION_MAJOR));
        registerFieldCalculator(new ConcatNONDuplicatedCalculator(AGENT_NAME_VERSION,               AGENT_NAME,             AGENT_VERSION));
        registerFieldCalculator(new MajorVersionCalculator(AGENT_VERSION_MAJOR,                     AGENT_VERSION));

        registerFieldCalculator(new ConcatNONDuplicatedCalculator(WEBVIEW_APP_NAME_VERSION_MAJOR,   WEBVIEW_APP_NAME,       WEBVIEW_APP_VERSION_MAJOR));
        registerFieldCalculator(new ConcatNONDuplicatedCalculator(WEBVIEW_APP_NAME_VERSION,         WEBVIEW_APP_NAME,       WEBVIEW_APP_VERSION));
        registerFieldCalculator(new MajorVersionCalculator(WEBVIEW_APP_VERSION_MAJOR,               WEBVIEW_APP_VERSION));

        registerFieldCalculator(new ConcatNONDuplicatedCalculator(LAYOUT_ENGINE_NAME_VERSION_MAJOR, LAYOUT_ENGINE_NAME,     LAYOUT_ENGINE_VERSION_MAJOR));
        registerFieldCalculator(new ConcatNONDuplicatedCalculator(LAYOUT_ENGINE_NAME_VERSION,       LAYOUT_ENGINE_NAME,     LAYOUT_ENGINE_VERSION));
        registerFieldCalculator(new MajorVersionCalculator(LAYOUT_ENGINE_VERSION_MAJOR,             LAYOUT_ENGINE_VERSION));

        registerFieldCalculator(new MajorVersionCalculator(OPERATING_SYSTEM_NAME_VERSION_MAJOR,     OPERATING_SYSTEM_NAME_VERSION));
        registerFieldCalculator(new ConcatNONDuplicatedCalculator(OPERATING_SYSTEM_NAME_VERSION,    OPERATING_SYSTEM_NAME,  OPERATING_SYSTEM_VERSION));
        registerFieldCalculator(new MajorVersionCalculator(OPERATING_SYSTEM_VERSION_MAJOR,          OPERATING_SYSTEM_VERSION));

        registerFieldCalculator(new CalculateAgentClass());
        registerFieldCalculator(new CalculateAgentName());
        registerFieldCalculator(new CalculateNetworkType());
        registerFieldCalculator(new CalculateDeviceName());
        registerFieldCalculator(new CalculateDeviceBrand());
        registerFieldCalculator(new CalculateAgentEmail());

        Collections.reverse(fieldCalculators);
        uaa.setFieldCalculators(fieldCalculators);

        if (uaa.wantedFieldNames != null) {
            uaa.wantedFieldNames.addAll(dependenciesNeededByCalculators);
        }

        boolean showLoading = uaa.getShowMatcherStats();

        ConfigLoader configLoader = new ConfigLoader(showLoading)
            .addResource(resources, false)
            .addResource(optionalResources, true);

        if (uaa.willKeepTests()) {
            configLoader.keepTests();
        } else {
            configLoader.dropTests();
        }

        int yamlRuleCount = 1;
        for (String yamlRule : yamlRules) {
            configLoader.addYaml(yamlRule, "Manually Inserted Rules " + yamlRuleCount++);
        }

        uaa.addAnalyzerConfig(configLoader.load());

        uaa.finalizeLoadingRules();
        if (preheatIterations < 0) {
            uaa.preHeat();
        } else {
            if (preheatIterations > 0) {
                uaa.preHeat(preheatIterations);
            }
        }
        didBuildStep = true;
        return uaa;
    }
}
