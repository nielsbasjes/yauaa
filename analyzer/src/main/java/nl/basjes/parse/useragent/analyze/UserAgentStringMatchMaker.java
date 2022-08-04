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

package nl.basjes.parse.useragent.analyze;

import com.esotericsoftware.kryo.DefaultSerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import nl.basjes.parse.useragent.AnalyzerPreHeater;
import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;
import nl.basjes.parse.useragent.analyze.WordRangeVisitor.Range;
import nl.basjes.parse.useragent.calculate.CalculateAgentClass;
import nl.basjes.parse.useragent.calculate.CalculateAgentEmail;
import nl.basjes.parse.useragent.calculate.CalculateAgentName;
import nl.basjes.parse.useragent.calculate.CalculateDeviceBrand;
import nl.basjes.parse.useragent.calculate.CalculateDeviceName;
import nl.basjes.parse.useragent.calculate.CalculateNetworkType;
import nl.basjes.parse.useragent.calculate.ConcatNONDuplicatedCalculator;
import nl.basjes.parse.useragent.calculate.FieldCalculator;
import nl.basjes.parse.useragent.calculate.MacOSXMajorVersionCalculator;
import nl.basjes.parse.useragent.calculate.MajorVersionCalculator;
import nl.basjes.parse.useragent.config.AnalyzerConfig;
import nl.basjes.parse.useragent.config.AnalyzerConfigHolder;
import nl.basjes.parse.useragent.config.ConfigLoader;
import nl.basjes.parse.useragent.config.MatcherConfig;
import nl.basjes.parse.useragent.parse.UserAgentTreeFlattener;
import nl.basjes.parse.useragent.utils.KryoConfig;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static nl.basjes.parse.useragent.UserAgent.AGENT_CLASS;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.AGENT_VERSION;
import static nl.basjes.parse.useragent.UserAgent.AGENT_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_BRAND;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_CLASS;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_NAME;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_VERSION;
import static nl.basjes.parse.useragent.UserAgent.HACKER_ATTACK_VECTOR;
import static nl.basjes.parse.useragent.UserAgent.HACKER_TOOLKIT;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_CLASS;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_NAME;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_VERSION;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.MutableUserAgent.isSystemField;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_CLASS;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_NAME;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_VERSION;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.PRE_SORTED_FIELDS_LIST;
import static nl.basjes.parse.useragent.UserAgent.REMARKABLE_PATTERN;
import static nl.basjes.parse.useragent.UserAgent.SET_ALL_FIELDS;
import static nl.basjes.parse.useragent.UserAgent.SYNTAX_ERROR;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_NAME;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_VERSION;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_VERSION_MAJOR;
import static nl.basjes.parse.useragent.utils.YauaaVersion.logVersion;

@DefaultSerializer(UserAgentStringMatchMaker.KryoSerializer.class)
public class UserAgentStringMatchMaker implements MatchMaker, AnalyzerConfigHolder, AnalyzerPreHeater, Serializable {
    // We set this to 1000000 always.
    // Why?
    // At the time of writing this the actual HashMap size needed about 410K entries.
    // To keep the bins small the load factor of 0.75 already puts us at the capacity of 1048576
    private static final int INFORM_ACTIONS_HASHMAP_CAPACITY = 1000000;

    private static final Logger LOG = LogManager.getLogger(UserAgentStringMatchMaker.class);
    private final ArrayList<Matcher> allMatchers = new ArrayList<>(5000);
    private final ArrayList<Matcher> zeroInputMatchers = new ArrayList<>(100);

    public List<Matcher> getAllMatchers() {
        return allMatchers;
    }

    public MatcherList getTouchedMatchers() {
        return touchedMatchers;
    }

    private final Map<String, Set<MatcherAction>> informMatcherActions = new LinkedHashMap<>(INFORM_ACTIONS_HASHMAP_CAPACITY);

    private boolean showMatcherStats;

    // If we want ALL fields this is null. If we only want specific fields this is a list of names.
    protected Set<String> wantedFieldNames = null; // NOSONAR: Only accessed via Builder.

    protected UserAgentTreeFlattener flattener;

    public static final int DEFAULT_USER_AGENT_MAX_LENGTH = 2048;
    private int userAgentMaxLength = DEFAULT_USER_AGENT_MAX_LENGTH;

    private AnalyzerConfig config;

    @Nonnull
    @Override
    public AnalyzerConfig getConfig() {
        return config;
    }

    void addAnalyzerConfig(AnalyzerConfig analyzerConfig) {
        if (config == null) {
            config = analyzerConfig;
        } else {
            config.merge(analyzerConfig);
        }
    }

    @SuppressWarnings("unused") // Private constructor for serialization systems ONLY (like Kryo)
    private UserAgentStringMatchMaker() {
        initTransientFields();
    }

    /*
     * Initialize the transient default values
     */
    void initTransientFields() {
        touchedMatchers = new MatcherList(32);
    }

    private void readObject(java.io.ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        initTransientFields();
        stream.defaultReadObject();
        showDeserializationStats();
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

    public static class KryoSerializer extends FieldSerializer<UserAgentStringMatchMaker> {
        public KryoSerializer(Kryo kryo, Class<?> type) {
            super(kryo, type);
        }

        @Override
        public void write(Kryo kryo, Output output, UserAgentStringMatchMaker object) {
            // Get rid of needless data
            object.reset();
            super.write(kryo, output, object);
        }

        @Override
        public UserAgentStringMatchMaker read(Kryo kryo, Input input, Class<? extends UserAgentStringMatchMaker> type) {
            UserAgentStringMatchMaker uaa = super.read(kryo, input, type);
            uaa.initTransientFields();
            uaa.showDeserializationStats();
            return uaa;
        }
    }

    private void showDeserializationStats() {
        List<String> lines = new ArrayList<>();
        lines.add("This Analyzer instance was deserialized.");
        lines.add("");
        lines.add("Lookups      : " + getLookups().size());
        lines.add("LookupSets   : " + getLookupSets().size());
        lines.add("Matchers     : " + allMatchers.size());
        lines.add("Hashmap size : " + informMatcherActions.size());
        lines.add("Ranges map   : " + informMatcherActionRanges.size());
        lines.add("Testcases    : " + getTestCases().size());

        logVersion(lines);
    }

    private boolean delayInitialization;
    void delayInitialization() {
        delayInitialization = true;
    }

    void immediateInitialization() {
        delayInitialization = false;
    }

    public void setShowMatcherStats(boolean newShowMatcherStats) {
        this.showMatcherStats = newShowMatcherStats;
    }

    public boolean getShowMatcherStats() {
        return showMatcherStats;
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
        allMatchers.forEach(Matcher::destroy);
        allMatchers.clear();
        allMatchers.trimToSize();

        zeroInputMatchers.forEach(Matcher::destroy);
        zeroInputMatchers.clear();
        zeroInputMatchers.trimToSize();

        informMatcherActions.clear();

        config = null;

        if (wantedFieldNames != null) {
            wantedFieldNames.clear();
        }

        flattener.clear();

        invalidateCaches();
    }

    private void invalidateCaches(){
        allPossibleFieldNamesCache=null;
        allPossibleFieldNamesSortedCache=null;
    }

    // --------------------------------------------

    public void loadResources(String resourceString) {
        loadResources(resourceString, showMatcherStats, false);
    }

    public void loadResources(String resourceString, boolean showLoadMessages, boolean optionalResources) {
        if (matchersHaveBeenInitialized) {
            throw new IllegalStateException("Refusing to load additional resources after the datastructures have been initialized.");
        }

        AnalyzerConfig extraConfig = new ConfigLoader(showLoadMessages)
            .addResource(resourceString, optionalResources)
            .load();

        addAnalyzerConfig(extraConfig);
        invalidateCaches();
        finalizeLoadingRules(); // FIXME: This is a rebuild of what has already been done. #SLOW!
    }

    public void finalizeLoadingRules() {
        flattener = new UserAgentTreeFlattener(this);

        if (wantedFieldNames != null) {
            int wantedSize = wantedFieldNames.size();
            if (wantedFieldNames.contains(SET_ALL_FIELDS)) {
                wantedSize--;
            }
            LOG.info("Building all needed matchers for the requested {} fields.", wantedSize);
        } else {
            LOG.info("Building all matchers for all possible fields.");
        }

        Map<String, MatcherConfig> matcherConfigs = config.getMatcherConfigs();
        userAgentMaxLength = config.getUserAgentMaxLength();

        if (matcherConfigs.isEmpty()) {
            throw new InvalidParserConfigurationException("No matchers were loaded at all.");
        }

        allMatchers.clear();
        for (Map.Entry<String, MatcherConfig> matcherConfigEntry : matcherConfigs.entrySet()) {
            MatcherConfig matcherConfig = matcherConfigEntry.getValue();
            try {
                allMatchers.add(new Matcher(this, wantedFieldNames, matcherConfig));
            } catch (UselessMatcherException ume) {
//                skippedMatchers++;
            }
        }

        verifyWeAreNotAskingForImpossibleFields();
        if (!delayInitialization) {
            initializeMatchers();
        }
    }

    private void verifyWeAreNotAskingForImpossibleFields() {
        if (wantedFieldNames == null) {
            return; // Nothing to check
        }
        List<String> impossibleFields = new ArrayList<>();
        List<String> allPossibleFields = getAllPossibleFieldNamesSorted();

        for (String wantedFieldName: wantedFieldNames) {
            if (isSystemField(wantedFieldName)) {
                continue; // These are fine
            }
            if (!allPossibleFields.contains(wantedFieldName)) {
                impossibleFields.add(wantedFieldName);
            }
        }
        if (impossibleFields.isEmpty()) {
            return;
        }
        throw new InvalidParserConfigurationException("We cannot provide these fields:" + impossibleFields);
    }

    private boolean matchersHaveBeenInitialized = false;
    public synchronized void initializeMatchers() {
        if (matchersHaveBeenInitialized) {
            return;
        }
        LOG.info("Initializing Analyzer data structures");

        if (allMatchers.isEmpty()) {
            throw new InvalidParserConfigurationException("No matchers were loaded at all.");
        }

        long start = System.nanoTime();
        allMatchers.forEach(Matcher::initialize);
        long stop = System.nanoTime();

        matchersHaveBeenInitialized = true;
        LOG.info("Built in {} msec : Hashmap {}, Ranges map:{}",
            (stop - start) / 1000000,
            informMatcherActions.size(),
            informMatcherActionRanges.size());

        for (Matcher matcher: allMatchers) {
            if (matcher.getActionsThatRequireInput() == 0) {
                zeroInputMatchers.add(matcher);
            }
        }

        // Reset all Matchers
        for (Matcher matcher : allMatchers) {
            matcher.reset();
        }
    }

    private transient volatile Set<String> allPossibleFieldNamesCache = null; //NOSONAR: The getter avoids the java:S3077 issues
    public Set<String> getAllPossibleFieldNames() {
        if (allPossibleFieldNamesCache == null) {
            synchronized (this) {
                if (allPossibleFieldNamesCache == null) {
                    Set<String> names = new TreeSet<>(CORE_SYSTEM_GENERATED_FIELDS);
                    if (wantedFieldNames == null) {
                        for (Matcher matcher : allMatchers) {
                            names.addAll(matcher.getAllPossibleFieldNames());
                        }
                        for (FieldCalculator calculator : fieldCalculators) {
                            names.add(calculator.getCalculatedFieldName());
                        }
                    } else {
                        for (Matcher matcher : allMatchers) {
                            // Not all wanted fields are possible !
                            for (String possibleFieldName : matcher.getAllPossibleFieldNames()) {
                                if (wantedFieldNames.contains(possibleFieldName)) {
                                    names.add(possibleFieldName);
                                }
                            }
                        }
                        for (FieldCalculator calculator : fieldCalculators) {
                            String possibleFieldName = calculator.getCalculatedFieldName();
                            if (wantedFieldNames.contains(possibleFieldName)) {
                                names.add(possibleFieldName);
                            }
                        }
                        names.remove(SET_ALL_FIELDS);
                    }
                    allPossibleFieldNamesCache = names;
                }
            }
        }
        return allPossibleFieldNamesCache;
    }

    private transient volatile List<String> allPossibleFieldNamesSortedCache = null; //NOSONAR: The getter avoids the java:S3077 issues
    public List<String> getAllPossibleFieldNamesSorted() {
        if (allPossibleFieldNamesSortedCache == null) {
            synchronized (this) {
                if (allPossibleFieldNamesSortedCache == null) {
                    List<String> fieldNames = new ArrayList<>(getAllPossibleFieldNames());
                    Collections.sort(fieldNames);

                    List<String> names = new ArrayList<>();
                    for (String fieldName : PRE_SORTED_FIELDS_LIST) {
                        fieldNames.remove(fieldName);
                        names.add(fieldName);
                    }
                    names.addAll(fieldNames);

                    // Force the Syntax error to be the last in the list.
                    names.remove(SYNTAX_ERROR);
                    names.add(SYNTAX_ERROR);
                    allPossibleFieldNamesSortedCache = names;
                }
            }
        }
        return allPossibleFieldNamesSortedCache;
    }


    // These are the actual subrange we need for the paths.
    private final Map<String, Set<Range>> informMatcherActionRanges = new HashMap<>(10000);
    @Override
    public void lookingForRange(String treeName, Range range) {
        Set<Range> ranges = informMatcherActionRanges.computeIfAbsent(treeName, k -> new LinkedHashSet<>(4));
        ranges.add(range);
    }

    // We do not want to put ALL lengths in the hashmap for performance reasons
    public static final int MAX_PREFIX_HASH_MATCH = 3;

    // Calculate the max length we will put in the hashmap.
    public static int firstCharactersForPrefixHashLength(String input, int maxChars) {
        return Math.min(maxChars, Math.min(MAX_PREFIX_HASH_MATCH, input.length()));
    }

    public static String firstCharactersForPrefixHash(String input, int maxChars) {
        return input.substring(0, firstCharactersForPrefixHashLength(input, maxChars));
    }

    // These are the paths for which we have prefix requests.
    private final Map<String, Set<Integer>> informMatcherActionPrefixesLengths = new HashMap<>(1000);

    @Override
    public void informMeAboutPrefix(MatcherAction matcherAction, String treeName, String prefix) {
        this.informMeAbout(matcherAction, treeName + "{\"" + firstCharactersForPrefixHash(prefix, MAX_PREFIX_HASH_MATCH) + "\"");
        Set<Integer> lengths = informMatcherActionPrefixesLengths.computeIfAbsent(treeName, k -> new LinkedHashSet<>(4));
        lengths.add(firstCharactersForPrefixHashLength(prefix, MAX_PREFIX_HASH_MATCH));
    }

    @Override
    public Set<Integer> getRequiredPrefixLengths(String treeName) {
        return informMatcherActionPrefixesLengths.get(treeName);
    }

    public void informMeAbout(MatcherAction matcherAction, String keyPattern) {
        String hashKey = keyPattern.toLowerCase(Locale.ROOT);
        Set<MatcherAction> analyzerSet = informMatcherActions
            .computeIfAbsent(hashKey, k -> new LinkedHashSet<>());
        analyzerSet.add(matcherAction);
    }

    private boolean verbose = false;

    public synchronized void setVerbose(boolean newVerbose) {
        this.verbose = newVerbose;
        flattener.setVerbose(newVerbose);
    }

    @Override
    public int getUserAgentMaxLength() {
        return this.userAgentMaxLength;
    }

    private void setAsHacker(MutableUserAgent userAgent, int confidence) {
        userAgent.set(DEVICE_CLASS,                 "Hacker",  confidence);
        userAgent.set(DEVICE_BRAND,                 "Hacker",  confidence);
        userAgent.set(DEVICE_NAME,                  "Hacker",  confidence);
        userAgent.set(DEVICE_VERSION,               "Hacker",  confidence);
        userAgent.set(OPERATING_SYSTEM_CLASS,       "Hacker",  confidence);
        userAgent.set(OPERATING_SYSTEM_NAME,        "Hacker",  confidence);
        userAgent.set(OPERATING_SYSTEM_VERSION,     "Hacker",  confidence);
        userAgent.set(LAYOUT_ENGINE_CLASS,          "Hacker",  confidence);
        userAgent.set(LAYOUT_ENGINE_NAME,           "Hacker",  confidence);
        userAgent.set(LAYOUT_ENGINE_VERSION,        "Hacker",  confidence);
        userAgent.set(LAYOUT_ENGINE_VERSION_MAJOR,  "Hacker",  confidence);
        userAgent.set(AGENT_CLASS,                  "Hacker",  confidence);
        userAgent.set(AGENT_NAME,                   "Hacker",  confidence);
        userAgent.set(AGENT_VERSION,                "Hacker",  confidence);
        userAgent.set(AGENT_VERSION_MAJOR,          "Hacker",  confidence);
        userAgent.set(HACKER_TOOLKIT,               "Unknown", confidence);
        userAgent.set(HACKER_ATTACK_VECTOR,         "Unknown", confidence);
        userAgent.set(REMARKABLE_PATTERN,           "Hacker",  confidence);
    }

    private transient MatcherList touchedMatchers = new MatcherList(32);

    @Override
    public void receivedInput(Matcher matcher) {
        if (zeroInputMatchers.contains(matcher)) {
            return;
        }
        touchedMatchers.add(matcher);
    }

    /**
     * Resets the state of the Analyzer to the default state.
     */
    public synchronized void reset() {
        // Reset all Matchers
        for (Matcher matcher : touchedMatchers) {
            matcher.reset();
        }
        touchedMatchers.clear();

        for (Matcher matcher : zeroInputMatchers) {
            matcher.reset();
        }
    }

    /**
     * Parses and analyzes the useragent string provided in the MutableUserAgent instance.
     * NOTE: This method is internally synchronized because the way the analyzer works is not reentrant.
     * @param userAgent The MutableUserAgent instance that is to be parsed and that gets all results
     * @return An ImmutableUserAgent copy of the results that is suitable for further usage and caching.
     */
    @Nonnull
    public MutableUserAgent parse(MutableUserAgent userAgent) {
        initializeMatchers();
        String useragentString = userAgent.getUserAgentString();
        if (useragentString != null && useragentString.length() > userAgentMaxLength) {
            setAsHacker(userAgent, 100);
            userAgent.setForced(HACKER_ATTACK_VECTOR, "Buffer overflow", 100);
            return hardCodedPostProcessing(userAgent);
        }

        synchronized (this) {
            // Reset all Matchers
            reset();

            if (userAgent.isDebug()) {
                for (Matcher matcher : allMatchers) {
                    matcher.setVerboseTemporarily(true);
                }
            }

            try {
                userAgent = flattener.parse(userAgent);

                inform(SYNTAX_ERROR, userAgent.getValue(SYNTAX_ERROR), null);

                if (verbose) {
                    LOG.info("=========== Checking all Touched Matchers: {}", touchedMatchers.size());
                }
                // Fire all Analyzers with any input
                for (Matcher matcher : touchedMatchers) {
                    matcher.analyze(userAgent);
                }

                if (verbose) {
                    LOG.info("=========== Checking all Zero Input Matchers: {}", zeroInputMatchers.size());
                }
                // Fire all Analyzers that should not get input
                for (Matcher matcher : zeroInputMatchers) {
                    matcher.analyze(userAgent);
                }

                userAgent.processSetAll();
            } catch (RuntimeException rte) {
                // If this occurs then someone has found a previously undetected problem.
                // So this is a safety for something that 'can' but 'should not' occur.
                userAgent.reset();
                setAsHacker(userAgent, 10000);
                userAgent.setForced(HACKER_ATTACK_VECTOR, "Yauaa Exploit", 10000);
            }
        }
        return hardCodedPostProcessing(userAgent);
    }

    private static final List<String> CORE_SYSTEM_GENERATED_FIELDS = new ArrayList<>();

    static {
        CORE_SYSTEM_GENERATED_FIELDS.add(SYNTAX_ERROR);
    }

    public boolean isWantedField(String fieldName) {
        if (wantedFieldNames == null) {
            return true;
        }
        return wantedFieldNames.contains(fieldName);
    }

    public Set<String> getWantedFieldNames(){
        return wantedFieldNames;
    }

    private final List<FieldCalculator> fieldCalculators = new ArrayList<>();

    public void setFieldCalculators(List<FieldCalculator> newFieldCalculators) {
        fieldCalculators.addAll(newFieldCalculators);
    }

    private MutableUserAgent hardCodedPostProcessing(MutableUserAgent userAgent) {
        // If it is really really bad ... then it is a Hacker.
        if ("true".equals(userAgent.getValue(SYNTAX_ERROR))) {
            if (userAgent.get(DEVICE_CLASS).getConfidence() == -1) {
                setAsHacker(userAgent, 10);
            }
        }

        // Calculate all fields that are constructed from the found ones.
        for (FieldCalculator fieldCalculator: fieldCalculators) {
            if (verbose) {
                LOG.info("Running FieldCalculator: {}", fieldCalculator);
            }
            fieldCalculator.calculate(userAgent);
        }

        return userAgent;
    }

    public Set<Range> getRequiredInformRanges(String treeName) {
        return informMatcherActionRanges.computeIfAbsent(treeName, k -> Collections.emptySet());
    }

    public void inform(String key, String value, ParseTree ctx) {
        inform(key, key, value, ctx);
        inform(key + "=\"" + value + '"', key, value, ctx);

        Set<Integer> lengths = getRequiredPrefixLengths(key);
        if (lengths != null) {
            int valueLength = value.length();
            for (Integer prefixLength : lengths) {
                if (valueLength >= prefixLength) {
                    inform(key + "{\"" + firstCharactersForPrefixHash(value, prefixLength) + '"', key, value, ctx);
                }
            }
        }
    }

    private void inform(String match, String key, String value, ParseTree ctx) {
        Set<MatcherAction> relevantActions = informMatcherActions.get(match.toLowerCase(Locale.ROOT));
        if (verbose) {
            if (relevantActions == null) {
                LOG.info("--- Have (0): {}", match);
            } else {
                LOG.info("+++ Have ({}): {}", relevantActions.size(), match);

                int count = 1;
                for (MatcherAction action : relevantActions) {
                    LOG.info("+++ -------> ({}): {}", count, action);
                    count++;
                }
            }
        }

        if (relevantActions != null) {
            for (MatcherAction matcherAction : relevantActions) {
                matcherAction.inform(key, value, ctx);
            }
        }
    }

    // ===============================================================================================================

    // ===============================================================================================================

    public UserAgentStringMatchMaker(
        AnalyzerConfig        analyzerConfig,
        boolean               showMatcherStats,
        boolean               delayInitialization) {

        this.config = analyzerConfig;
        this.showMatcherStats = showMatcherStats;
        this.delayInitialization = delayInitialization;

        Set<String> newWantedFieldNames = this.config.getWantedFieldNames();
        if (newWantedFieldNames != null && !newWantedFieldNames.isEmpty()) {
            this.wantedFieldNames = new TreeSet<>(newWantedFieldNames);
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

        // We use this way because of "Windows NT" ,"10.0" --> "Windows 10.0" --> "Windows 10"
        registerFieldCalculator(new MajorVersionCalculator(OPERATING_SYSTEM_NAME_VERSION_MAJOR,     OPERATING_SYSTEM_NAME_VERSION));
        registerFieldCalculator(new MacOSXMajorVersionCalculator(OPERATING_SYSTEM_NAME_VERSION_MAJOR,     OPERATING_SYSTEM_NAME_VERSION));

        registerFieldCalculator(new ConcatNONDuplicatedCalculator(OPERATING_SYSTEM_NAME_VERSION,    OPERATING_SYSTEM_NAME,  OPERATING_SYSTEM_VERSION));
        registerFieldCalculator(new MajorVersionCalculator(OPERATING_SYSTEM_VERSION_MAJOR,          OPERATING_SYSTEM_VERSION));
        registerFieldCalculator(new MacOSXMajorVersionCalculator(OPERATING_SYSTEM_VERSION_MAJOR,     OPERATING_SYSTEM_VERSION));

        registerFieldCalculator(new CalculateAgentClass());
        registerFieldCalculator(new CalculateAgentName());
        registerFieldCalculator(new CalculateNetworkType());
        registerFieldCalculator(new CalculateDeviceName());
        registerFieldCalculator(new CalculateDeviceBrand());
        registerFieldCalculator(new CalculateAgentEmail());

        Collections.reverse(fieldCalculators);
        verifyCalculatorDependencyOrdering();

        if (this.wantedFieldNames != null && !this.wantedFieldNames.isEmpty()) {
            this.wantedFieldNames.addAll(dependenciesNeededByCalculators);
        }

        finalizeLoadingRules();
    }

    private final Set<String> allFieldsForWhichACalculatorExists    = new HashSet<>();
    private final Set<String> dependenciesNeededByCalculators       = new HashSet<>();

    private void registerFieldCalculator(FieldCalculator fieldCalculator) {
        String calculatedFieldName = fieldCalculator.getCalculatedFieldName();
        allFieldsForWhichACalculatorExists.add(calculatedFieldName);
        if (isWantedField(calculatedFieldName) ||
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

    // ===============================================================================================================

    /**
     * This function is used only for analyzing which patterns that could possibly be relevant
     * were actually relevant for the matcher actions.
     * @return The list of Matches that were possibly relevant.
     */
    public List<MatchesList.Match> getMatches() {
        List<MatchesList.Match> allMatches = new ArrayList<>(128);
        for (Matcher matcher: getAllMatchers()) {
            allMatches.addAll(matcher.getMatches());
        }
        return allMatches;
    }

    public synchronized List<MatchesList.Match> getUsedMatches(MutableUserAgent userAgent) {
        // Reset all Matchers
        for (Matcher matcher : getAllMatchers()) {
            matcher.reset();
            matcher.setVerboseTemporarily(false);
        }

        flattener.parse(userAgent);

        List<MatchesList.Match> allMatches = new ArrayList<>(128);
        for (Matcher matcher: getAllMatchers()) {
            allMatches.addAll(matcher.getUsedMatches());
        }
        return allMatches;
    }

    // ===============================================================================================================

    @Override
    public String toString() {
        return "UserAgentStringMatchMaker{" +
//            "\nallMatchers=" + allMatchers +
//            "\n, zeroInputMatchers=" + zeroInputMatchers +
//            "\n, informMatcherActions=" + informMatcherActions +
            "\n, showMatcherStats=" + showMatcherStats +
//            "\n, doingOnlyASingleTest=" + doingOnlyASingleTest +
            "\n, wantedFieldNames=" + wantedFieldNames +
//            "\n, testCases=" + testCases +
//            "\n, lookups=" + lookups +
//            "\n, lookupSets=" + lookupSets +
//            "\n, flattener=" + flattener +
            "\n, userAgentMaxLength=" + userAgentMaxLength +
            "\n, delayInitialization=" + delayInitialization +
            "\n, matchersHaveBeenInitialized=" + matchersHaveBeenInitialized +
//            "\n, informMatcherActionRanges=" + ToString.toString(informMatcherActionRanges) +
//            "\n, informMatcherActionPrefixesLengths=" + ToString.toString(informMatcherActionPrefixesLengths) +
            "\n, verbose=" + verbose +
//            "\n, fieldCalculators=" + fieldCalculators +
            "\n}";
    }
}
