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

package nl.basjes.parse.useragent;

import com.esotericsoftware.kryo.DefaultSerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import nl.basjes.parse.useragent.UserAgent.ImmutableUserAgent;
import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;
import nl.basjes.parse.useragent.analyze.Matcher;
import nl.basjes.parse.useragent.analyze.MatcherList;
import nl.basjes.parse.useragent.analyze.MatchesList;
import nl.basjes.parse.useragent.analyze.UserAgentStringMatchMaker;
import nl.basjes.parse.useragent.clienthints.ClientHints;
import nl.basjes.parse.useragent.clienthints.ClientHintsAnalyzer;
import nl.basjes.parse.useragent.config.AnalyzerConfig;
import nl.basjes.parse.useragent.config.AnalyzerConfigHolder;
import nl.basjes.parse.useragent.config.ConfigLoader;
import nl.basjes.parse.useragent.utils.CheckLoggingDependencies;
import nl.basjes.parse.useragent.utils.KryoConfig;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static nl.basjes.parse.useragent.UserAgent.DEVICE_CLASS;
import static nl.basjes.parse.useragent.UserAgent.SET_ALL_FIELDS;
import static nl.basjes.parse.useragent.UserAgent.USERAGENT_HEADER;
import static nl.basjes.parse.useragent.config.ConfigLoader.DEFAULT_RESOURCES;
import static nl.basjes.parse.useragent.utils.YauaaVersion.logVersion;

@DefaultSerializer(AbstractUserAgentAnalyzerDirect.KryoSerializer.class)
public abstract class AbstractUserAgentAnalyzerDirect implements Analyzer, AnalyzerConfigHolder, AnalyzerPreHeater, Serializable {

    protected ClientHintsAnalyzer clientHintsAnalyzer = new ClientHintsAnalyzer();
    private UserAgentStringMatchMaker matchMaker;

    private AnalyzerConfig        analyzerConfig;

    @Nonnull
    @Override
    public AnalyzerConfig getConfig() {
        return analyzerConfig;
    }

    protected UserAgentStringMatchMaker getMatchMaker() {
        return matchMaker;
    }

    /**
     * This is used to configure the provided Kryo instance if Kryo serialization is desired.
     * The expected type here is Object because otherwise the Kryo library becomes
     * a mandatory dependency on any project that uses Yauaa.
     * @param kryo The instance of com.esotericsoftware.kryo.Kryo that needs to be configured.
     */
    public static void configureKryo(Object kryo) {
        KryoConfig.configureKryo((Kryo) kryo);
    }

    public static class KryoSerializer extends FieldSerializer<AbstractUserAgentAnalyzerDirect> {
        public KryoSerializer(Kryo kryo, Class<?> type) {
            super(kryo, type);
        }

        @Override
        public void write(Kryo kryo, Output output, AbstractUserAgentAnalyzerDirect object) {
            // Get rid of needless data
            object.reset();
            super.write(kryo, output, object);
        }

        @Override
        public AbstractUserAgentAnalyzerDirect read(Kryo kryo, Input input, Class<? extends AbstractUserAgentAnalyzerDirect> type) {
            return super.read(kryo, input, type);
        }
    }

    protected AbstractUserAgentAnalyzerDirect() {
    }


    // --------------------------------------------

    /**
     * In some cases it was found that simply dereferencing the instance and letting the GC clean it all up was "too hard".
     * To assist in these kinds of problem cases this method will wipe the internal data structures as much as possible.
     * After calling this method this instance becomes unusable and cannot be 'repaired'.
     * Normal applications will never need this. Simply dereferencing the analyzer will clean everything,
     * no memory leaks (that we know of).
     */
    public synchronized void destroy() {
        matchMaker.destroy();
    }

    // --------------------------------------------

    public void loadResources(String resourceString) {
        matchMaker.loadResources(resourceString);
    }

    public void loadResources(String resourceString, boolean showLoadMessages, boolean optionalResources) {
        matchMaker.loadResources(resourceString, showLoadMessages, optionalResources);
    }

    public void initializeMatchers() {
        matchMaker.initializeMatchers();
    }

    public Set<String> getAllPossibleFieldNames() {
        return matchMaker.getAllPossibleFieldNames();
    }

    public List<String> getAllPossibleFieldNamesSorted() {
        return matchMaker.getAllPossibleFieldNamesSorted();
    }

    public synchronized void setVerbose(boolean newVerbose) {
        matchMaker.setVerbose(newVerbose);
    }

    /**
     * Resets the state of the Analyzer to the default state.
     */
    public synchronized void reset() {
        matchMaker.reset();
    }

    /**
     * Parses and analyzes the provided useragent string
     * @param userAgentString The User-Agent String that is to be parsed and analyzed
     * @return An ImmutableUserAgent record that holds all the results.
     */
    @Nonnull
    @Override
    public ImmutableUserAgent parse(String userAgentString) {
        return parse(Collections.singletonMap(USERAGENT_HEADER, userAgentString));
    }

    /**
     * Parses and analyzes the provided useragent string
     * @param requestHeaders A map of all useful request Headers: the "User-Agent" and all Client Hints: "Sec-Ch-Ua"*
     * @return An ImmutableUserAgent record that holds all the results.
     */
    @Nonnull
    @Override
    public ImmutableUserAgent parse(Map<String, String> requestHeaders) {
        MutableUserAgent userAgent = new MutableUserAgent(getWantedFieldNames());
        userAgent.addHeader(requestHeaders);
        return parse(userAgent);
    }

    /**
     * Parses and analyzes the useragent string provided in the MutableUserAgent instance.
     * NOTE: This method is internally synchronized because the way the analyzer works is not reentrant.
     * @param inputUserAgent The MutableUserAgent instance that is to be parsed and that gets all results
     * @return An ImmutableUserAgent copy of the results that is suitable for further usage and caching.
     */
    @Nonnull
    public ImmutableUserAgent parse(MutableUserAgent inputUserAgent) {
        if (inputUserAgent == null) {
            return new ImmutableUserAgent(matchMaker.parse(new MutableUserAgent((String) null)));
        }

        // So we parse the User-Agent header normally
        MutableUserAgent userAgent = matchMaker.parse(inputUserAgent);

        Map<String, String> requestHeaders = inputUserAgent.getHeaders();
        if (requestHeaders.size() > 1) {
            // Then we check to see what ClientHints are available
            ClientHints clientHints = clientHintsAnalyzer.parse(requestHeaders);

            // Lastly we modify the parsed userAgent with the found clientHints
            userAgent = clientHintsAnalyzer.merge(userAgent, clientHints);
        }
        return new ImmutableUserAgent(userAgent);
    }

    public List<String> supportedClientHintHeaders() {
        return clientHintsAnalyzer.supportedClientHintHeaders();
    }

    public boolean isSupportedClientHintHeader(String header) {
        return clientHintsAnalyzer.isSupportedClientHintHeader(header);
    }

    public boolean isWantedField(String fieldName) {
        return matchMaker.isWantedField(fieldName);
    }

    public Set<String> getWantedFieldNames(){
        return matchMaker.getWantedFieldNames();
    }

    protected void configure(
        AnalyzerConfig        pAnalyzerConfig,
        boolean               pShowMatcherStats,
        boolean               pDelayInitialization
    ) {
        analyzerConfig = pAnalyzerConfig;
        matchMaker = new UserAgentStringMatchMaker(
            pAnalyzerConfig,
            pShowMatcherStats,
            pDelayInitialization);
    }

    // ===============================================================================================================

    /**
     * This function is used only for analyzing which patterns that could possibly be relevant
     * were actually relevant for the matcher actions.
     * @return The list of Matches that were possibly relevant.
     */
    public List<MatchesList.Match> getMatches() {
        return matchMaker.getMatches();
    }

    public synchronized List<MatchesList.Match> getUsedMatches(MutableUserAgent userAgent) {
        return matchMaker.getUsedMatches(userAgent);
    }

    public List<Matcher> getAllMatchers() {
        return matchMaker.getAllMatchers();
    }

    public MatcherList getTouchedMatchers() {
        return matchMaker.getTouchedMatchers();
    }


    // ===============================================================================================================

    @SuppressWarnings("unchecked") // For all the casts of 'this' to 'B'
    public abstract static class AbstractUserAgentAnalyzerDirectBuilder<UAA extends AbstractUserAgentAnalyzerDirect, B extends AbstractUserAgentAnalyzerDirectBuilder<UAA, B>> {
        private final UAA uaa;
        private boolean didBuildStep = false;
        private int preheatIterations = 0;

        private final Set<String>           wantedFieldNames    = new TreeSet<>();  // isEmpty() means "we want everything"
        private final List<String>          resources           = new ArrayList<>();
        private final List<String>          optionalResources   = new ArrayList<>();
        private final List<String>          yamlRules           = new ArrayList<>();
        private       boolean               showMatcherStats    = false;
        private       int                   userAgentMaxLength  = -1; // -1 --> use built in default
        private       boolean               delayInitialization = true;
        private       boolean               keepTests           = true;

        protected void failIfAlreadyBuilt() {
            if (didBuildStep) {
                throw new IllegalStateException(
                    "A builder can provide only a single instance. " +
                    "It is not allowed to set values after doing build()");
            }
        }

        protected AbstractUserAgentAnalyzerDirectBuilder(UAA newUaa) {
            this.uaa = newUaa;
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
            wantedFieldNames.add(fieldName);
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
            wantedFieldNames.clear();
            return (B)this;
        }

        /**
         * Log additional information during the startup of the analyzer.
         * @return the current Builder instance.
         */
        public B showMatcherLoadStats() {
            failIfAlreadyBuilt();
            showMatcherStats = true;
            return (B)this;
        }

        /**
         * Set the stats logging during the startup of the analyzer back to the default of "minimal".
         * @return the current Builder instance.
         */
        public B hideMatcherLoadStats() {
            failIfAlreadyBuilt();
            showMatcherStats = false;
            return (B)this;
        }

        /**
         * Set maximum length of a useragent for it to be classified as Hacker without any analysis.
         * @param newUserAgentMaxLength The new maximum length of a useragent for it to be classified as Hacker without any analysis.
         * @return the current Builder instance.
         */
        public B withUserAgentMaxLength(int newUserAgentMaxLength) {
            failIfAlreadyBuilt();
            userAgentMaxLength = newUserAgentMaxLength;
            return (B)this;
        }

        /**
         * Retain all testcases in memory after initialization.
         * @return the current Builder instance.
         */
        public B keepTests() {
            failIfAlreadyBuilt();
            keepTests = true;
            return (B)this;
        }

        /**
         * Remove all testcases in memory after initialization.
         * @return the current Builder instance.
         */
        public B dropTests() {
            failIfAlreadyBuilt();
            keepTests = false;
            return (B)this;
        }

        /**
         * Load all patterns and rules but do not yet build the lookup hashMaps yet.
         * For the engine to run these lookup hashMaps are needed so they will be constructed once "just in time".
         * @return the current Builder instance.
         */
        public B delayInitialization() {
            failIfAlreadyBuilt();
            delayInitialization = true;
            return (B)this;
        }

        /**
         * Load all patterns and rules and immediately build the lookup hashMaps.
         * @return the current Builder instance.
         */
        public B immediateInitialization() {
            failIfAlreadyBuilt();
            delayInitialization = false;
            return (B)this;
        }

        boolean showFullVersion = true;
        public B showMinimalVersion() {
            showFullVersion = false;
            return (B)this;
        }

        public B showFullVersion() {
            showFullVersion = true;
            return (B)this;
        }

        /**
         * Construct the analyzer and run the preheat sequence (if requested).
         * @return the new analyzer instance.
         */
        public UAA build() {
            failIfAlreadyBuilt();

            // Before we initialize we check if the logging has been set up correctly.
            // Not all useragents trigger the same logging libraries because some
            // logging libraries are only used in specific analysis code.
            // This is a "fail fast" to ensure any problems happen even before startup.
            CheckLoggingDependencies.verifyLoggingDependencies();

            logVersion(showFullVersion);

            // In case we only want specific fields we must add these special cases too
            if (!wantedFieldNames.isEmpty()) {
                // Special field that affects ALL fields.
                wantedFieldNames.add(SET_ALL_FIELDS);

                // This is always needed to determine the Hacker fallback
                wantedFieldNames.add(DEVICE_CLASS);
            }

            ConfigLoader configLoader = new ConfigLoader(showMatcherStats)
                .keepTests(keepTests)
                .addResource(resources, false)
                .addResource(optionalResources, true);

            int yamlRuleCount = 1;
            for (String yamlRule : yamlRules) {
                configLoader.addYaml(yamlRule, "Manually Inserted Rules " + yamlRuleCount++);
            }

            AnalyzerConfig analyzerConfig =
                configLoader
                    .load()
                    .setUserAgentMaxLength(userAgentMaxLength)
                    .wantedFieldNames(wantedFieldNames);

            uaa.configure(analyzerConfig, showMatcherStats, delayInitialization);

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

    @Override
    public String toString() {
        return "AbstractUserAgentAnalyzerDirect{" +
            "clientHintsAnalyzer=" + clientHintsAnalyzer +
            ", matchMaker=" + matchMaker +
            ", analyzerConfig=" + analyzerConfig +
            '}';
    }
}
