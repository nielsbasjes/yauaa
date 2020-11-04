/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2020 Niels Basjes
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

import nl.basjes.parse.useragent.calculate.CalculateAgentEmail;
import nl.basjes.parse.useragent.calculate.CalculateAgentName;
import nl.basjes.parse.useragent.calculate.CalculateDeviceBrand;
import nl.basjes.parse.useragent.calculate.CalculateDeviceName;
import nl.basjes.parse.useragent.calculate.CalculateNetworkType;
import nl.basjes.parse.useragent.calculate.ConcatNONDuplicatedCalculator;
import nl.basjes.parse.useragent.calculate.FieldCalculator;
import nl.basjes.parse.useragent.calculate.MajorVersionCalculator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import static nl.basjes.parse.useragent.ConfigLoader.DEFAULT_RESOURCES;
import static nl.basjes.parse.useragent.UserAgent.AGENT_INFORMATION_EMAIL;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.AGENT_VERSION;
import static nl.basjes.parse.useragent.UserAgent.AGENT_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_BRAND;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_CLASS;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_NAME;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_NAME;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_VERSION;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.NETWORK_TYPE;
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

@SuppressWarnings("unchecked") // For all the casts of 'this' to 'B'
public abstract class AbstractUserAgentAnalyzerDirectBuilder<UAA extends AbstractUserAgentAnalyzerDirect, B extends AbstractUserAgentAnalyzerDirectBuilder<UAA, B>> {

    private final UAA uaa;
    private boolean didBuildStep = false;
    private int preheatIterations = 0;

    private final ConfigLoader          configLoader      = new ConfigLoader();
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
        this.configLoader.setShowMatcherStats(false);
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
        configLoader.setShowMatcherStats(true);
        return (B)this;
    }

    /**
     * Set the stats logging during the startup of the analyzer back to the default of "minimal".
     * @return the current Builder instance.
     */
    public B hideMatcherLoadStats() {
        failIfAlreadyBuilt();
        configLoader.setShowMatcherStats(false);
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
        configLoader.keepTests();
        return (B)this;
    }

    /**
     * Remove all testcases in memory after initialization.
     * @return the current Builder instance.
     */
    public B dropTests() {
        failIfAlreadyBuilt();
        configLoader.dropTests();
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

    private void addCalculator(FieldCalculator calculator) {
        fieldCalculators.add(calculator);
        if (uaa.wantedFieldNames != null) {
            Collections.addAll(uaa.wantedFieldNames, calculator.getDependencies());
        }
    }

    private void addCalculatedMajorVersionField(String result, String dependency) {
        if (uaa.isWantedField(result)) {
            fieldCalculators.add(new MajorVersionCalculator(result, dependency));
            if (uaa.wantedFieldNames != null) {
                Collections.addAll(uaa.wantedFieldNames, dependency);
            }
        }
    }

    private void addCalculatedConcatNONDuplicated(String result, String first, String second) {
        if (uaa.isWantedField(result)) {
            fieldCalculators.add(new ConcatNONDuplicatedCalculator(result, first, second));
            if (uaa.wantedFieldNames != null) {
                Collections.addAll(uaa.wantedFieldNames, first, second);
            }
        }
    }

    /**
     * Construct the analyzer and run the preheat (if requested).
     * @return the new analyzer instance.
     */
    public UAA build() {
        failIfAlreadyBuilt();

        // In case we only want specific fields we must all these special cases too
        if (uaa.wantedFieldNames != null) {
            // Special field that affects ALL fields.
            uaa.wantedFieldNames.add(SET_ALL_FIELDS);

            // This is always needed to determine the Hacker fallback
            uaa.wantedFieldNames.add(DEVICE_CLASS);
        }

        addCalculatedConcatNONDuplicated(AGENT_NAME_VERSION_MAJOR,              AGENT_NAME,             AGENT_VERSION_MAJOR);
        addCalculatedConcatNONDuplicated(AGENT_NAME_VERSION,                    AGENT_NAME,             AGENT_VERSION);
        addCalculatedMajorVersionField(AGENT_VERSION_MAJOR,                     AGENT_VERSION);

        addCalculatedConcatNONDuplicated(WEBVIEW_APP_NAME_VERSION_MAJOR,        WEBVIEW_APP_NAME,       WEBVIEW_APP_VERSION_MAJOR);
        addCalculatedMajorVersionField(WEBVIEW_APP_VERSION_MAJOR,               WEBVIEW_APP_VERSION);

        addCalculatedConcatNONDuplicated(LAYOUT_ENGINE_NAME_VERSION_MAJOR,      LAYOUT_ENGINE_NAME,     LAYOUT_ENGINE_VERSION_MAJOR);
        addCalculatedConcatNONDuplicated(LAYOUT_ENGINE_NAME_VERSION,            LAYOUT_ENGINE_NAME,     LAYOUT_ENGINE_VERSION);
        addCalculatedMajorVersionField(LAYOUT_ENGINE_VERSION_MAJOR,             LAYOUT_ENGINE_VERSION);

        addCalculatedMajorVersionField(OPERATING_SYSTEM_NAME_VERSION_MAJOR,     OPERATING_SYSTEM_NAME_VERSION);
        addCalculatedConcatNONDuplicated(OPERATING_SYSTEM_NAME_VERSION,         OPERATING_SYSTEM_NAME,  OPERATING_SYSTEM_VERSION);
        addCalculatedMajorVersionField(OPERATING_SYSTEM_VERSION_MAJOR,          OPERATING_SYSTEM_VERSION);

        if (uaa.isWantedField(AGENT_NAME)) {
            addCalculator(new CalculateAgentName());
        }

        if (uaa.isWantedField(NETWORK_TYPE)) {
            addCalculator(new CalculateNetworkType());
        }

        if (uaa.isWantedField(DEVICE_NAME)) {
            addCalculator(new CalculateDeviceName());
        }

        if (uaa.isWantedField(DEVICE_BRAND)) {
            addCalculator(new CalculateDeviceBrand());
        }

        if (uaa.isWantedField(AGENT_INFORMATION_EMAIL)) {
            addCalculator(new CalculateAgentEmail());
        }

        Collections.reverse(fieldCalculators);
        uaa.setFieldCalculators(fieldCalculators);

        boolean mustDropTestsLater = !configLoader.willKeepTests();
        if (preheatIterations != 0) {
            configLoader.keepTests();
        }

        optionalResources.forEach(resource -> configLoader.loadResources(resource, true, true));
        resources.forEach(resource -> configLoader.loadResources(resource, true, false));

        int yamlRuleCount = 1;
        for (String yamlRule : yamlRules) {
            configLoader.loadYaml(yamlRule, "Manually Inserted Rules " + yamlRuleCount++);
        }

        configLoader.finalizeLoadingRules(uaa);

        uaa.useConfig(configLoader);

        if (preheatIterations < 0) {
            uaa.preHeat();
        } else {
            if (preheatIterations > 0) {
                uaa.preHeat(preheatIterations);
            }
        }
        if (mustDropTestsLater) {
            uaa.dropTests();
        }
        didBuildStep = true;
        return uaa;
    }
}
