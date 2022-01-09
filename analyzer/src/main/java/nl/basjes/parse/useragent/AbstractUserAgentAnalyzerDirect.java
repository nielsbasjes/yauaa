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
import nl.basjes.parse.useragent.analyze.Analyzer;
import nl.basjes.parse.useragent.analyze.AnalyzerMatcher;
import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import nl.basjes.parse.useragent.analyze.Matcher;
import nl.basjes.parse.useragent.analyze.MatcherAction;
import nl.basjes.parse.useragent.analyze.MatcherList;
import nl.basjes.parse.useragent.analyze.MatcherTree;
import nl.basjes.parse.useragent.analyze.UselessMatcherException;
import nl.basjes.parse.useragent.analyze.WordRangeVisitor.Range;
import nl.basjes.parse.useragent.calculate.FieldCalculator;
import nl.basjes.parse.useragent.config.AnalyzerConfig;
import nl.basjes.parse.useragent.config.ConfigLoader;
import nl.basjes.parse.useragent.config.MatcherConfig;
import nl.basjes.parse.useragent.config.TestCase;
import nl.basjes.parse.useragent.parse.UserAgentTreeMatchMaker;
import nl.basjes.parse.useragent.utils.GetAllPathsAnalyzer;
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
import static nl.basjes.parse.useragent.UserAgent.PRE_SORTED_FIELDS_LIST;
import static nl.basjes.parse.useragent.UserAgent.SET_ALL_FIELDS;
import static nl.basjes.parse.useragent.UserAgent.SYNTAX_ERROR;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_VERSION_MAJOR;
import static nl.basjes.parse.useragent.utils.YauaaVersion.logVersion;

@DefaultSerializer(AbstractUserAgentAnalyzerDirect.KryoSerializer.class)
public abstract class AbstractUserAgentAnalyzerDirect implements Analyzer, AnalyzerPreHeater, Serializable {

    private static final Logger LOG = LogManager.getLogger(AbstractUserAgentAnalyzerDirect.class);
    private final ArrayList<Matcher> allMatchers = new ArrayList<>(5000);
    private final ArrayList<Matcher> zeroInputMatchers = new ArrayList<>(100);

    public AnalyzerMatcher analyzerMatcher;

    protected List<Matcher> getAllMatchers() {
        return allMatchers;
    }

    protected MatcherList getTouchedMatchers() {
        return touchedMatchers;
    }

    private boolean showMatcherStats = false;

    // If we want ALL fields this is null. If we only want specific fields this is a list of names.
    protected Set<String> wantedFieldNames = null; // NOSONAR: Only accessed via Builder.

    private List<TestCase> testCases = new ArrayList<>();

    public List<TestCase> getTestCases() {
        return testCases;
    }

    private Map<String, Map<String, String>> lookups;
    private Map<String, Set<String>> lookupSets;

    @Override
    public Map<String, Map<String, String>> getLookups() {
        return lookups;
    }

    @Override
    public Map<String, Set<String>> getLookupSets() {
        return lookupSets;
    }

    protected UserAgentTreeMatchMaker matchMaker;

    public static final int DEFAULT_USER_AGENT_MAX_LENGTH = 2048;
    private int userAgentMaxLength = DEFAULT_USER_AGENT_MAX_LENGTH;
    private boolean loadTests = false;

    private AnalyzerConfig config = null;

    void addAnalyzerConfig(AnalyzerConfig analyzerConfig) {
        if (config == null) {
            config = analyzerConfig;
        } else {
            config.merge(analyzerConfig);
        }
        testCases = config.getTestCases();
        lookups = config.getLookups();
        lookupSets = config.getLookupSets();
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
            AbstractUserAgentAnalyzerDirect uaa = super.read(kryo, input, type);
            uaa.initTransientFields();
            uaa.showDeserializationStats();
            return uaa;
        }
    }

    private void showDeserializationStats() {
        List<String> lines = new ArrayList<>();
        lines.add("This Analyzer instance was deserialized.");
        lines.add("");
        lines.add("Lookups      : " + ((lookups == null) ? 0 : lookups.size()));
        lines.add("LookupSets   : " + lookupSets.size());
        lines.add("Matchers     : " + allMatchers.size());
//        lines.add("Hashmap size : " + informMatcherActions.size());
        lines.add("Ranges map   : " + informMatcherActionRanges.size());
        lines.add("Testcases    : " + testCases.size());

        logVersion(lines);
    }

    protected AbstractUserAgentAnalyzerDirect() {
    }

    private boolean delayInitialization = true;
    void delayInitialization() {
        delayInitialization = true;
    }

    void immediateInitialization() {
        delayInitialization = false;
    }

    void setShowMatcherStats(boolean newShowMatcherStats) {
        this.showMatcherStats = newShowMatcherStats;
    }

    public boolean getShowMatcherStats() {
        return showMatcherStats;
    }

    public void dropTests() {
        loadTests = false;
        testCases.clear();
    }

    public void keepTests() {
        loadTests = true;
    }

    public boolean willKeepTests() {
        return loadTests;
    }

    public long getNumberOfTestCases() {
        return testCases.size();
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

//        informMatcherActions.clear();

        config = null;

        if (wantedFieldNames != null) {
            wantedFieldNames.clear();
        }

        testCases = Collections.emptyList();
        lookups = Collections.emptyMap();
        lookupSets = Collections.emptyMap();
        matchMaker.clear();

        analyzerMatcher = null;

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
        finalizeLoadingRules();
    }

    protected void finalizeLoadingRules() {
        logVersion();
        matchMaker = new UserAgentTreeMatchMaker(this);

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

        touchedMatchers = new MatcherList(32);
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
        matchMaker.setVerbose(newVerbose);
    }

    public void setUserAgentMaxLength(int newUserAgentMaxLength) {
        if (newUserAgentMaxLength <= 0) {
            userAgentMaxLength = DEFAULT_USER_AGENT_MAX_LENGTH;
        } else {
            userAgentMaxLength = newUserAgentMaxLength;
        }
    }

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
    }

    private transient MatcherList touchedMatchers = null;

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
     * Parses and analyzes the provided useragent string
     * @param userAgentString The User-Agent String that is to be parsed and analyzed
     * @return An ImmutableUserAgent record that holds all of the results.
     */
    @Override
    public ImmutableUserAgent parse(String userAgentString) {
        MutableUserAgent userAgent = new MutableUserAgent(userAgentString, getAllPossibleFieldNames());
        return parse(userAgent);
    }

    /**
     * Parses and analyzes the useragent string provided in the MutableUserAgent instance.
     * NOTE: This method is internally synchronized because the way the analyzer works is not reentrant.
     * @param userAgent The MutableUserAgent instance that is to be parsed and that gets all results
     * @return An ImmutableUserAgent copy of the results that is suitable for further usage and caching.
     */
    @Nonnull
    public ImmutableUserAgent parse(MutableUserAgent userAgent) {
        initializeMatchers();
        String useragentString = userAgent.getUserAgentString();
        if (useragentString != null && useragentString.length() > userAgentMaxLength) {
            setAsHacker(userAgent, 100);
            userAgent.setForced(HACKER_ATTACK_VECTOR, "Buffer overflow", 100);
            return new ImmutableUserAgent(hardCodedPostProcessing(userAgent));
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
                userAgent = matchMaker.parse(userAgent);

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
        return new ImmutableUserAgent(hardCodedPostProcessing(userAgent));
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

    protected void setFieldCalculators(List<FieldCalculator> newFieldCalculators) {
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

    public void inform(String key, String value, ParseTree<MatcherTree> ctx) {
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

    private void inform(String match, String key, String value, ParseTree<MatcherTree> ctx) {
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

    public static List<String> getAllPaths(String agent) {
        return new GetAllPathsAnalyzer(agent).getValues();
    }

    public static GetAllPathsAnalyzer getAllPathsAnalyzer(String agent) {
        return new GetAllPathsAnalyzer(agent);
    }

    // ===============================================================================================================

    @Override
    public String toString() {
        return "UserAgentAnalyzerDirect{" +
            "\nallMatchers=" + allMatchers +
            "\n, zeroInputMatchers=" + zeroInputMatchers +
            "\n, informMatcherActions=" + informMatcherActions +
            "\n, showMatcherStats=" + showMatcherStats +
//            "\n, doingOnlyASingleTest=" + doingOnlyASingleTest +
            "\n, wantedFieldNames=" + wantedFieldNames +
            "\n, testCases=" + testCases +
            "\n, lookups=" + lookups +
            "\n, lookupSets=" + lookupSets +
            "\n, flattener=" + matchMaker +
            "\n, userAgentMaxLength=" + userAgentMaxLength +
            "\n, loadTests=" + loadTests +
            "\n, delayInitialization=" + delayInitialization +
            "\n, matchersHaveBeenInitialized=" + matchersHaveBeenInitialized +
//            "\n, informMatcherActionRanges=" + ToString.toString(informMatcherActionRanges) +
//            "\n, informMatcherActionPrefixesLengths=" + ToString.toString(informMatcherActionPrefixesLengths) +
            "\n, verbose=" + verbose +
            "\n, fieldCalculators=" + fieldCalculators +
            "\n}";
    }
}
