/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2018 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.useragent;

import com.google.common.net.InternetDomainName;
import nl.basjes.parse.useragent.analyze.Analyzer;
import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import nl.basjes.parse.useragent.analyze.Matcher;
import nl.basjes.parse.useragent.analyze.MatcherAction;
import nl.basjes.parse.useragent.analyze.UselessMatcherException;
import nl.basjes.parse.useragent.analyze.WordRangeVisitor.Range;
import nl.basjes.parse.useragent.parse.UserAgentTreeFlattener;
import nl.basjes.parse.useragent.utils.Normalize;
import nl.basjes.parse.useragent.utils.VersionSplitter;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static nl.basjes.parse.useragent.UserAgent.AGENT_CLASS;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME;
import static nl.basjes.parse.useragent.UserAgent.AGENT_VERSION;
import static nl.basjes.parse.useragent.UserAgent.AGENT_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_BRAND;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_CLASS;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_NAME;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_VERSION;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_CLASS;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_NAME;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_VERSION;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_CLASS;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_NAME;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_VERSION;
import static nl.basjes.parse.useragent.UserAgent.PRE_SORTED_FIELDS_LIST;
import static nl.basjes.parse.useragent.UserAgent.SET_ALL_FIELDS;
import static nl.basjes.parse.useragent.UserAgent.SYNTAX_ERROR;
import static nl.basjes.parse.useragent.utils.YamlUtils.fail;
import static nl.basjes.parse.useragent.utils.YamlUtils.getExactlyOneNodeTuple;
import static nl.basjes.parse.useragent.utils.YamlUtils.getKeyAsString;
import static nl.basjes.parse.useragent.utils.YamlUtils.getStringValues;
import static nl.basjes.parse.useragent.utils.YamlUtils.getValueAsMappingNode;
import static nl.basjes.parse.useragent.utils.YamlUtils.getValueAsSequenceNode;
import static nl.basjes.parse.useragent.utils.YamlUtils.getValueAsString;
import static nl.basjes.parse.useragent.utils.YauaaVersion.assertSameVersion;
import static nl.basjes.parse.useragent.utils.YauaaVersion.logVersion;

public class UserAgentAnalyzerDirect implements Analyzer, Serializable {

    // We set this to 1000000 always.
    // Why?
    // At the time of writing this the actual HashMap size needed about 410K entries.
    // To keep the bins small the load factor of 0.75 already puts us at the capacity of 1048576
    private static final int INFORM_ACTIONS_HASHMAP_CAPACITY = 1000000;

    private static final Logger LOG = LoggerFactory.getLogger(UserAgentAnalyzerDirect.class);
    protected final List<Matcher> allMatchers = new ArrayList<>(5000);
    private final Map<String, Set<MatcherAction>> informMatcherActions = new HashMap<>(INFORM_ACTIONS_HASHMAP_CAPACITY);
    private transient Map<String, List<MappingNode>> matcherConfigs = new HashMap<>();

    private boolean showMatcherStats = false;
    private boolean doingOnlyASingleTest = false;

    // If we want ALL fields this is null. If we only want specific fields this is a list of names.
    protected List<String> wantedFieldNames = null;

    protected final List<Map<String, Map<String, String>>> testCases = new ArrayList<>(2048);
    private Map<String, Map<String, String>> lookups = new HashMap<>(128);
    private final Map<String, Set<String>> lookupSets = new HashMap<>(128);

    protected UserAgentTreeFlattener flattener;

    public static final int DEFAULT_USER_AGENT_MAX_LENGTH = 2048;
    private int userAgentMaxLength = DEFAULT_USER_AGENT_MAX_LENGTH;
    private boolean loadTests = false;

    private static final String DEFAULT_RESOURCES = "classpath*:UserAgents/**/*.yaml";

    /*
     * Initialize the transient default values
     */
    private void initTransientFields() {
        matcherConfigs = new HashMap<>(64);
    }

    private void readObject(java.io.ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        initTransientFields();
        stream.defaultReadObject();

        List<String> lines = new ArrayList<>();
        lines.add("This Analyzer instance was deserialized.");
        lines.add("");
        lines.add("Lookups      : " + ((lookups == null) ? 0 : lookups.size()));
        lines.add("LookupSets   : " + lookupSets.size());
        lines.add("Matchers     : " + allMatchers.size());
        lines.add("Hashmap size : " + informMatcherActions.size());
        lines.add("Testcases    : " + testCases.size());

        String[] x = {};
        logVersion(lines.toArray(x));
    }

    protected UserAgentAnalyzerDirect() {
    }

    private boolean delayInitialization = true;
    public void delayInitialization() {
        delayInitialization = true;
    }

    public void immediateInitialization() {
        delayInitialization = false;
    }

    public UserAgentAnalyzerDirect setShowMatcherStats(boolean newShowMatcherStats) {
        this.showMatcherStats = newShowMatcherStats;
        return this;
    }

    public UserAgentAnalyzerDirect dropTests() {
        loadTests = false;
        testCases.clear();
        return this;
    }

    public UserAgentAnalyzerDirect keepTests() {
        loadTests = true;
        return this;
    }

    public boolean willKeepTests() {
        return loadTests;
    }

    public long getNumberOfTestCases() {
        return testCases.size();
    }

    protected void initialize() {
        initialize(Collections.singletonList(DEFAULT_RESOURCES));
    }

    protected void initialize(List<String> resources) {
        logVersion();
        resources.forEach(this::loadResources);
        verifyWeAreNotAskingForImpossibleFields();
        if (!delayInitialization) {
            initializeMatchers();
        }
    }

    protected void verifyWeAreNotAskingForImpossibleFields() {
        if (wantedFieldNames == null) {
            return; // Nothing to check
        }
        List<String> impossibleFields = new ArrayList<>();
        List<String> allPossibleFields = getAllPossibleFieldNamesSorted();

        for (String wantedFieldName: wantedFieldNames) {
            if (UserAgent.isSystemField(wantedFieldName)) {
                continue; // These are fine
            }
            if (!allPossibleFields.contains(wantedFieldName)) {
                impossibleFields.add(wantedFieldName);
            }
        }
        if (impossibleFields.isEmpty()) {
            return;
        }
        throw new InvalidParserConfigurationException("We cannot provide these fields:" + impossibleFields.toString());
    }

    // --------------------------------------------


    public static String getVersion() {
        return "Yauaa " + Version.getProjectVersion() + " (" + Version.getGitCommitIdDescribeShort() + " @ " + Version.getBuildTimestamp() + ")";
    }

    public void loadResources(String resourceString) {
        if (matchersHaveBeenInitialized) {
            throw new IllegalStateException("Refusing to load additional resources after the datastructures have been initialized.");
        }

        LOG.info("Loading from: \"{}\"", resourceString);
        long startFiles = System.nanoTime();

        flattener = new UserAgentTreeFlattener(this);
        Yaml yaml = new Yaml();

        Map<String, Resource> resources = new TreeMap<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resourceArray = resolver.getResources(resourceString);
            for (Resource resource : resourceArray) {
                resources.put(resource.getFilename(), resource);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        doingOnlyASingleTest = false;
        int maxFilenameLength = 0;

        if (resources.isEmpty()) {
            throw new InvalidParserConfigurationException("Unable to find ANY config files");
        }

        // We need to determine if we are trying to load the yaml files TWICE.
        // This can happen if the library is loaded twice (perhaps even two different versions).
        Set<String> resourceBasenames = resources
            .keySet().stream()
            .map(k -> k.replaceAll("^.*/", ""))
            .collect(Collectors.toSet());

        Set<String> alreadyLoadedResourceBasenames = matcherConfigs
            .keySet().stream()
            .map(k -> k.replaceAll("^.*/", ""))
            .collect(Collectors.toSet());

        alreadyLoadedResourceBasenames.retainAll(resourceBasenames);
        if (!alreadyLoadedResourceBasenames.isEmpty()) {
            LOG.error("Trying to load these {} resources for the second time: {}",
                alreadyLoadedResourceBasenames.size(), alreadyLoadedResourceBasenames.toString());
            throw new InvalidParserConfigurationException("Trying to load " + alreadyLoadedResourceBasenames.size() +
                " resources for the second time");
        }

        for (Map.Entry<String, Resource> resourceEntry : resources.entrySet()) {
            try {
                Resource resource = resourceEntry.getValue();
                String filename = resource.getFilename();
                maxFilenameLength = Math.max(maxFilenameLength, filename.length());
                loadResource(yaml, resource.getInputStream(), filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        long stopFiles = System.nanoTime();
        LOG.info("Loaded {} files in {} msec", resources.size(),  (stopFiles - startFiles) / 1000000);

        if (lookups != null && !lookups.isEmpty()) {
            // All compares are done in a case insensitive way. So we lowercase ALL keys of the lookups beforehand.
            Map<String, Map<String, String>> cleanedLookups = new HashMap<>(lookups.size());
            for (Map.Entry<String, Map<String, String>> lookupsEntry : lookups.entrySet()) {
                Map<String, String> cleanedLookup = new HashMap<>(lookupsEntry.getValue().size());
                for (Map.Entry<String, String> entry : lookupsEntry.getValue().entrySet()) {
                    cleanedLookup.put(entry.getKey().toLowerCase(), entry.getValue());
                }
                cleanedLookups.put(lookupsEntry.getKey(), cleanedLookup);
            }
            lookups = cleanedLookups;
        }

        if (wantedFieldNames != null) {
            int wantedSize = wantedFieldNames.size();
            if (wantedFieldNames.contains(SET_ALL_FIELDS)) {
                wantedSize--;
            }
            LOG.info("Building all needed matchers for the requested {} fields.", wantedSize);
        } else {
            LOG.info("Building all matchers for all possible fields.");
        }
        int totalNumberOfMatchers = 0;
        int skippedMatchers = 0;
        if (matcherConfigs != null) {
            long fullStart = System.nanoTime();
            for (Map.Entry<String, Resource> resourceEntry : resources.entrySet()) {
                Resource resource = resourceEntry.getValue();
                String configFilename = resource.getFilename();
                List<MappingNode> matcherConfig = matcherConfigs.get(configFilename);
                if (matcherConfig == null) {
                    continue; // No matchers in this file (probably only lookups and/or tests)
                }

                long start = System.nanoTime();
                int startSkipped = skippedMatchers;
                for (MappingNode map : matcherConfig) {
                    try {
                        allMatchers.add(new Matcher(this, lookups, lookupSets, wantedFieldNames, map, configFilename));
                        totalNumberOfMatchers++;
                    } catch (UselessMatcherException ume) {
                        skippedMatchers++;
                    }
                }
                long stop = System.nanoTime();
                int stopSkipped = skippedMatchers;

                if (showMatcherStats) {
                    Formatter msg = new Formatter(Locale.ENGLISH);
                    msg.format("Loading %4d (dropped %4d) matchers from %-" + maxFilenameLength + "s " + "took %5d msec",
                        matcherConfig.size() - (stopSkipped - startSkipped),
                        stopSkipped - startSkipped,
                        configFilename,
                        (stop - start) / 1000000);
                    LOG.info(msg.toString());
                }
            }
            long fullStop = System.nanoTime();

            Formatter msg = new Formatter(Locale.ENGLISH);
            msg.format("Loading %4d (dropped %4d) matchers, %d lookups, %d lookupsets, %d testcases from %4d files took %5d msec",
                totalNumberOfMatchers,
                skippedMatchers,
                (lookups == null) ? 0 : lookups.size(),
                lookupSets.size(),
                testCases.size(),
                matcherConfigs.size(),
                (fullStop - fullStart) / 1000000);
            LOG.info(msg.toString());

        }
    }

    private boolean matchersHaveBeenInitialized = false;
    public void initializeMatchers() {
        if (matchersHaveBeenInitialized) {
            return;
        }
        LOG.info("Initializing Analyzer data structures");
        long start = System.nanoTime();
        allMatchers.forEach(Matcher::initialize);
        long stop = System.nanoTime();
        matchersHaveBeenInitialized = true;
        LOG.info("Built in {} msec : Hashmap {}, Ranges map:{}",
            (stop - start) / 1000000,
            informMatcherActions.size(),
            informMatcherActionRanges.size());
    }

    public Set<String> getAllPossibleFieldNames() {
        Set<String> results = new TreeSet<>(HARD_CODED_GENERATED_FIELDS);
        for (Matcher matcher : allMatchers) {
            results.addAll(matcher.getAllPossibleFieldNames());
        }
        return results;
    }

    public List<String> getAllPossibleFieldNamesSorted() {
        List<String> fieldNames = new ArrayList<>(getAllPossibleFieldNames());
        Collections.sort(fieldNames);

        List<String> result = new ArrayList<>();
        for (String fieldName : PRE_SORTED_FIELDS_LIST) {
            fieldNames.remove(fieldName);
            result.add(fieldName);
        }
        result.addAll(fieldNames);

        return result;
    }

/*
Example of the structure of the yaml file:
----------------------------
config:
  - lookup:
    name: 'lookupname'
    map:
        "From1" : "To1"
        "From2" : "To2"
        "From3" : "To3"
  - matcher:
      options:
        - 'verbose'
        - 'init'
      require:
        - 'Require pattern'
        - 'Require pattern'
      extract:
        - 'Extract pattern'
        - 'Extract pattern'
  - test:
      input:
        user_agent_string: 'Useragent'
      expected:
        FieldName     : 'ExpectedValue'
        FieldName     : 'ExpectedValue'
        FieldName     : 'ExpectedValue'
----------------------------
*/

    private void loadResource(Yaml yaml, InputStream yamlStream, String filename) {
        Node loadedYaml;
        try {
            loadedYaml = yaml.compose(new UnicodeReader(yamlStream));
        } catch (Exception e) {
            throw new InvalidParserConfigurationException("Parse error in the file " + filename + ": " + e.getMessage(), e);
        }

        if (loadedYaml == null) {
            throw new InvalidParserConfigurationException("The file " + filename + " is empty");
        }

        // Get and check top level config
        if (!(loadedYaml instanceof MappingNode)) {
            fail(loadedYaml, filename, "File must be a Map");
        }

        MappingNode rootNode = (MappingNode) loadedYaml;

        NodeTuple configNodeTuple = null;
        for (NodeTuple tuple : rootNode.getValue()) {
            String name = getKeyAsString(tuple, filename);
            if ("config".equals(name)) {
                configNodeTuple = tuple;
                break;
            }
            if ("version".equals(name)) {
                // Check the version information from the Yaml files
                assertSameVersion(tuple, filename);
                return;
            }
        }

        if (configNodeTuple == null) {
            fail(loadedYaml, filename, "The top level entry MUST be 'config'.");
        }

        SequenceNode configNode = getValueAsSequenceNode(configNodeTuple, filename);
        List<Node> configList = configNode.getValue();

        for (Node configEntry : configList) {
            if (!(configEntry instanceof MappingNode)) {
                fail(loadedYaml, filename, "The entry MUST be a mapping");
            }

            NodeTuple entry = getExactlyOneNodeTuple((MappingNode) configEntry, filename);
            MappingNode actualEntry = getValueAsMappingNode(entry, filename);
            String entryType = getKeyAsString(entry, filename);
            switch (entryType) {
                case "lookup":
                    loadYamlLookup(actualEntry, filename);
                    break;
                case "set":
                    loadYamlLookupSets(actualEntry, filename);
                    break;
                case "matcher":
                    loadYamlMatcher(actualEntry, filename);
                    break;
                case "test":
                    if (loadTests) {
                        loadYamlTestcase(actualEntry, filename);
                    }
                    break;
                default:
                    throw new InvalidParserConfigurationException(
                        "Yaml config.(" + filename + ":" + actualEntry.getStartMark().getLine() + "): " +
                            "Found unexpected config entry: " + entryType + ", allowed are 'lookup', 'set', 'matcher' and 'test'");
            }
        }
    }

    private void loadYamlLookup(MappingNode entry, String filename) {
//        LOG.info("Loading lookup.({}:{})", filename, entry.getStartMark().getLine());
        String name = null;
        Map<String, String> map = null;

        for (NodeTuple tuple : entry.getValue()) {
            switch (getKeyAsString(tuple, filename)) {
                case "name":
                    name = getValueAsString(tuple, filename);
                    break;
                case "map":
                    if (map == null) {
                        map = new HashMap<>();
                    }
                    List<NodeTuple> mappings = getValueAsMappingNode(tuple, filename).getValue();
                    for (NodeTuple mapping : mappings) {
                        String key = getKeyAsString(mapping, filename);
                        String value = getValueAsString(mapping, filename);
                        map.put(key, value);
                    }
                    break;
                default:
                    break;
            }
        }

        if (name == null && map == null) {
            fail(entry, filename, "Invalid lookup specified");
        }

        lookups.put(name, map);
    }

    private void loadYamlLookupSets(MappingNode entry, String filename) {
//        LOG.info("Loading lookupSet.({}:{})", filename, entry.getStartMark().getLine());
        String name = null;
        Set<String> lookupSet = new HashSet<>();

        for (NodeTuple tuple : entry.getValue()) {
            switch (getKeyAsString(tuple, filename)) {
                case "name":
                    name = getValueAsString(tuple, filename);
                    break;
                case "values":
                    SequenceNode node = getValueAsSequenceNode(tuple, filename);
                    List<String> values = getStringValues(node, filename);
                    for (String value: values) {
                        lookupSet.add(value.toLowerCase(Locale.ENGLISH));
                    }
                    break;
                default:
                    break;
            }
        }

        lookupSets.put(name, lookupSet);
    }

    private void loadYamlMatcher(MappingNode entry, String filename) {
//        LOG.info("Loading matcher.({}:{})", filename, entry.getStartMark().getLine());
        List<MappingNode> matcherConfigList = matcherConfigs
            .computeIfAbsent(filename, k -> new ArrayList<>(32));
        matcherConfigList.add(entry);
    }

    private void loadYamlTestcase(MappingNode entry, String filename) {
        if (!doingOnlyASingleTest) {
//            LOG.info("Skipping testcase.({}:{})", filename, entry.getStartMark().getLine());
//        } else {
//            LOG.info("Loading testcase.({}:{})", filename, entry.getStartMark().getLine());

            Map<String, String> metaData = new HashMap<>();
            metaData.put("filename", filename);
            metaData.put("fileline", String.valueOf(entry.getStartMark().getLine()));

            Map<String, String> input = null;
            List<String> options = null;
            Map<String, String> expected = null;
            for (NodeTuple tuple : entry.getValue()) {
                String name = getKeyAsString(tuple, filename);
                switch (name) {
                    case "options":
                        options = getStringValues(tuple.getValueNode(), filename);
                        if (options != null) {
                            if (options.contains("only")) {
                                doingOnlyASingleTest = true;
                                testCases.clear();
                            }
                        }
                        break;
                    case "input":
                        for (NodeTuple inputTuple : getValueAsMappingNode(tuple, filename).getValue()) {
                            String inputName = getKeyAsString(inputTuple, filename);
                            switch (inputName) {
                                case "user_agent_string":
                                    String inputString = getValueAsString(inputTuple, filename);
                                    input = new HashMap<>();
                                    input.put(inputName, inputString);
                                    break;
                                default:
                                    break;
                            }
                        }
                        break;
                    case "expected":
                        List<NodeTuple> mappings = getValueAsMappingNode(tuple, filename).getValue();
                        if (mappings != null) {
                            if (expected == null) {
                                expected = new HashMap<>();
                            }
                            for (NodeTuple mapping : mappings) {
                                String key = getKeyAsString(mapping, filename);
                                String value = getValueAsString(mapping, filename);
                                expected.put(key, value);
                            }
                        }
                        break;
                    default:
//                        fail(tuple.getKeyNode(), filename, "Unexpected: " + name);
                        break; // Skip
                }
            }

            if (input == null) {
                fail(entry, filename, "Test is missing input");
            }

            if (expected == null || expected.isEmpty()) {
                doingOnlyASingleTest = true;
                testCases.clear();
            }

            Map<String, Map<String, String>> testCase = new HashMap<>();

            testCase.put("input", input);
            if (expected != null) {
                testCase.put("expected", expected);
            }
            if (options != null) {
                Map<String, String> optionsMap = new HashMap<>(options.size());
                for (String option: options) {
                    optionsMap.put(option, option);
                }
                testCase.put("options", optionsMap);
            }
            testCase.put("metaData", metaData);
            testCases.add(testCase);
        }

    }

    // These are the actual subrange we need for the paths.
    private final Map<String, Set<Range>> informMatcherActionRanges = new HashMap<>(10000);
    @Override
    public void lookingForRange(String treeName, Range range) {
        Set<Range> ranges = informMatcherActionRanges.computeIfAbsent(treeName, k -> new HashSet<>(4));
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
        Set<Integer> lengths = informMatcherActionPrefixesLengths.computeIfAbsent(treeName, k -> new HashSet<>(4));
        lengths.add(firstCharactersForPrefixHashLength(prefix, MAX_PREFIX_HASH_MATCH));
    }

    @Override
    public Set<Integer> getRequiredPrefixLengths(String treeName) {
        return informMatcherActionPrefixesLengths.get(treeName);
    }

    public void informMeAbout(MatcherAction matcherAction, String keyPattern) {
        String hashKey = keyPattern.toLowerCase();
        Set<MatcherAction> analyzerSet = informMatcherActions
            .computeIfAbsent(hashKey, k -> new HashSet<>());
        analyzerSet.add(matcherAction);
    }

    private boolean verbose = false;

    public void setVerbose(boolean newVerbose) {
        this.verbose = newVerbose;
        flattener.setVerbose(newVerbose);
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

    public UserAgent parse(String userAgentString) {
        UserAgent userAgent = new UserAgent(userAgentString);
        return parse(userAgent);
    }

    private UserAgent setAsHacker(UserAgent userAgent, int confidence) {
        userAgent.set(DEVICE_CLASS,                 "Hacker",           confidence);
        userAgent.set(DEVICE_BRAND,                 "Hacker",           confidence);
        userAgent.set(DEVICE_NAME,                  "Hacker",           confidence);
        userAgent.set(DEVICE_VERSION,               "Hacker",           confidence);
        userAgent.set(OPERATING_SYSTEM_CLASS,       "Hacker",           confidence);
        userAgent.set(OPERATING_SYSTEM_NAME,        "Hacker",           confidence);
        userAgent.set(OPERATING_SYSTEM_VERSION,     "Hacker",           confidence);
        userAgent.set(LAYOUT_ENGINE_CLASS,          "Hacker",           confidence);
        userAgent.set(LAYOUT_ENGINE_NAME,           "Hacker",           confidence);
        userAgent.set(LAYOUT_ENGINE_VERSION,        "Hacker",           confidence);
        userAgent.set(LAYOUT_ENGINE_VERSION_MAJOR,  "Hacker",           confidence);
        userAgent.set(AGENT_CLASS,                  "Hacker",           confidence);
        userAgent.set(AGENT_NAME,                   "Hacker",           confidence);
        userAgent.set(AGENT_VERSION,                "Hacker",           confidence);
        userAgent.set(AGENT_VERSION_MAJOR,          "Hacker",           confidence);
        userAgent.set("HackerToolkit",              "Unknown",          confidence);
        userAgent.set("HackerAttackVector",         "Buffer overflow",  confidence);
        return userAgent;
    }

    public synchronized UserAgent parse(UserAgent userAgent) {
        initializeMatchers();
        String useragentString = userAgent.getUserAgentString();
        if (useragentString != null && useragentString.length() > userAgentMaxLength) {
            userAgent = setAsHacker(userAgent, 100);
            userAgent.setForced("HackerAttackVector", "Buffer overflow", 100);
            return hardCodedPostProcessing(userAgent);
        }

        // Reset all Matchers
        for (Matcher matcher : allMatchers) {
            matcher.reset();
        }

        if (userAgent.isDebug()) {
            for (Matcher matcher : allMatchers) {
                matcher.setVerboseTemporarily(true);
            }
        }

        try {
            userAgent = flattener.parse(userAgent);

            // Fire all Analyzers
            for (Matcher matcher : allMatchers) {
                matcher.analyze(userAgent);
            }

            userAgent.processSetAll();
            return hardCodedPostProcessing(userAgent);
        } catch (NullPointerException npe) {
            userAgent.reset();
            userAgent = setAsHacker(userAgent, 10000);
            userAgent.setForced("HackerAttackVector", "Yauaa NPE Exploit", 10000);
            return hardCodedPostProcessing(userAgent);
        }
    }

    private static final List<String> HARD_CODED_GENERATED_FIELDS = new ArrayList<>();

    static {
        HARD_CODED_GENERATED_FIELDS.add(SYNTAX_ERROR);
        HARD_CODED_GENERATED_FIELDS.add(AGENT_VERSION_MAJOR);
        HARD_CODED_GENERATED_FIELDS.add(LAYOUT_ENGINE_VERSION_MAJOR);
        HARD_CODED_GENERATED_FIELDS.add("AgentNameVersion");
        HARD_CODED_GENERATED_FIELDS.add("AgentNameVersionMajor");
        HARD_CODED_GENERATED_FIELDS.add("LayoutEngineNameVersion");
        HARD_CODED_GENERATED_FIELDS.add("LayoutEngineNameVersionMajor");
        HARD_CODED_GENERATED_FIELDS.add("OperatingSystemNameVersion");
        HARD_CODED_GENERATED_FIELDS.add("WebviewAppVersionMajor");
        HARD_CODED_GENERATED_FIELDS.add("WebviewAppNameVersionMajor");
    }

    public boolean isWantedField(String fieldName) {
        if (wantedFieldNames == null) {
            return true;
        }
        return wantedFieldNames.contains(fieldName);
    }

    private UserAgent hardCodedPostProcessing(UserAgent userAgent) {
        // If it is really really bad ... then it is a Hacker.
        if ("true".equals(userAgent.getValue(SYNTAX_ERROR))) {
            if (wantedFieldNames == null || (
                wantedFieldNames.contains(DEVICE_CLASS) &&
                wantedFieldNames.contains(OPERATING_SYSTEM_CLASS) &&
                wantedFieldNames.contains(LAYOUT_ENGINE_CLASS))
                ) {
                if (userAgent.get(DEVICE_CLASS).getConfidence() == -1 &&
                    userAgent.get(OPERATING_SYSTEM_CLASS).getConfidence() == -1 &&
                    userAgent.get(LAYOUT_ENGINE_CLASS).getConfidence() == -1) {

                    userAgent.set(DEVICE_CLASS, "Hacker", 10);
                    userAgent.set(DEVICE_BRAND, "Hacker", 10);
                    userAgent.set(DEVICE_NAME, "Hacker", 10);
                    userAgent.set(DEVICE_VERSION, "Hacker", 10);
                    userAgent.set(OPERATING_SYSTEM_CLASS, "Hacker", 10);
                    userAgent.set(OPERATING_SYSTEM_NAME, "Hacker", 10);
                    userAgent.set(OPERATING_SYSTEM_VERSION, "Hacker", 10);
                    userAgent.set(LAYOUT_ENGINE_CLASS, "Hacker", 10);
                    userAgent.set(LAYOUT_ENGINE_NAME, "Hacker", 10);
                    userAgent.set(LAYOUT_ENGINE_VERSION, "Hacker", 10);
                    userAgent.set(LAYOUT_ENGINE_VERSION_MAJOR, "Hacker", 10);
                    userAgent.set(AGENT_CLASS, "Hacker", 10);
                    userAgent.set(AGENT_NAME, "Hacker", 10);
                    userAgent.set(AGENT_VERSION, "Hacker", 10);
                    userAgent.set(AGENT_VERSION_MAJOR, "Hacker", 10);
                    userAgent.set("HackerToolkit", "Unknown", 10);
                    userAgent.set("HackerAttackVector", "Unknown", 10);
                }
            }
        }

        // !!!!!!!!!! NOTE !!!!!!!!!!!!
        // IF YOU ADD ANY EXTRA FIELDS YOU MUST ADD THEM TO THE BUILDER TOO !!!!
        // TODO: Perhaps this should be more generic. Like a "Post processor"  (Generic: Create fields from fields)?
        addMajorVersionField(userAgent, AGENT_VERSION, AGENT_VERSION_MAJOR);
        addMajorVersionField(userAgent, LAYOUT_ENGINE_VERSION, LAYOUT_ENGINE_VERSION_MAJOR);
        addMajorVersionField(userAgent, "WebviewAppVersion", "WebviewAppVersionMajor");

        concatFieldValuesNONDuplicated(userAgent, "AgentNameVersion",               AGENT_NAME,             AGENT_VERSION);
        concatFieldValuesNONDuplicated(userAgent, "AgentNameVersionMajor",          AGENT_NAME,             AGENT_VERSION_MAJOR);
        concatFieldValuesNONDuplicated(userAgent, "WebviewAppNameVersionMajor",     "WebviewAppName",       "WebviewAppVersionMajor");
        concatFieldValuesNONDuplicated(userAgent, "LayoutEngineNameVersion",        LAYOUT_ENGINE_NAME,     LAYOUT_ENGINE_VERSION);
        concatFieldValuesNONDuplicated(userAgent, "LayoutEngineNameVersionMajor",   LAYOUT_ENGINE_NAME,     LAYOUT_ENGINE_VERSION_MAJOR);
        concatFieldValuesNONDuplicated(userAgent, "OperatingSystemNameVersion",     OPERATING_SYSTEM_NAME,  OPERATING_SYSTEM_VERSION);

        // The device brand field is a mess.
        UserAgent.AgentField deviceBrand = userAgent.get(DEVICE_BRAND);
        if (deviceBrand.getConfidence() >= 0) {
            userAgent.setForced(
                DEVICE_BRAND,
                Normalize.brand(deviceBrand.getValue()),
                deviceBrand.getConfidence());
        }

        // The email address is a mess
        UserAgent.AgentField email = userAgent.get("AgentInformationEmail");
        if (email != null && email.getConfidence() >= 0) {
            userAgent.setForced(
                "AgentInformationEmail",
                Normalize.email(email.getValue()),
                email.getConfidence());
        }

        // Make sure the DeviceName always starts with the DeviceBrand
        UserAgent.AgentField deviceName = userAgent.get(DEVICE_NAME);
        if (deviceName.getConfidence() >= 0) {
            deviceBrand = userAgent.get(DEVICE_BRAND);
            String deviceNameValue = deviceName.getValue();
            String deviceBrandValue = deviceBrand.getValue();
            if (deviceName.getConfidence() >= 0 &&
                deviceBrand.getConfidence() >= 0 &&
                !deviceBrandValue.equals("Unknown")) {
                // In some cases it does start with the brand but without a separator following the brand
                deviceNameValue = Normalize.cleanupDeviceBrandName(deviceBrandValue, deviceNameValue);
            } else {
                deviceNameValue = Normalize.brand(deviceNameValue);
            }

            userAgent.setForced(
                DEVICE_NAME,
                deviceNameValue,
                deviceName.getConfidence());
        }

        if (deviceBrand.getConfidence() < 0) {
            // If no brand is known then try to extract something that looks like a Brand from things like URL and Email addresses.
            String newDeviceBrand = determineDeviceBrand(userAgent);
            if (newDeviceBrand != null) {
                userAgent.setForced(
                    DEVICE_BRAND,
                    newDeviceBrand,
                    1);
            }
        }

        return userAgent;
    }

    private String extractCompanyFromHostName(String hostname) {
        try {
            InternetDomainName domainName = InternetDomainName.from(hostname);
            return Normalize.brand(domainName.topPrivateDomain().parts().get(0));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String determineDeviceBrand(UserAgent userAgent) {
        // If no brand is known but we do have a URL then we assume the hostname to be the brand.
        // We put this AFTER the creation of the DeviceName because we choose to not have
        // this brandname in the DeviceName.

        UserAgent.AgentField informationUrl = userAgent.get("AgentInformationUrl");
        if (informationUrl != null && informationUrl.getConfidence() >= 0) {
            String hostname = informationUrl.getValue();
            try {
                URL url = new URL(hostname);
                hostname = url.getHost();
            } catch (MalformedURLException e) {
                // Ignore any exception and continue.
            }
            hostname = extractCompanyFromHostName(hostname);
            if (hostname != null) {
                return hostname;
            }
        }

        UserAgent.AgentField informationEmail = userAgent.get("AgentInformationEmail");
        if (informationEmail != null && informationEmail.getConfidence() >= 0) {
            String hostname = informationEmail.getValue();
            int atOffset = hostname.indexOf('@');
            if (atOffset >= 0) {
                hostname = hostname.substring(atOffset+1);
            }
            hostname = extractCompanyFromHostName(hostname);
            if (hostname != null) {
                return hostname;
            }
        }

        return null;
    }

    private void concatFieldValuesNONDuplicated(UserAgent userAgent, String targetName, String firstName, String secondName) {
        if (!isWantedField(targetName)) {
            return;
        }
        UserAgent.AgentField firstField = userAgent.get(firstName);
        UserAgent.AgentField secondField = userAgent.get(secondName);

        String first = null;
        long firstConfidence = -1;
        String second = null;
        long secondConfidence = -1;

        if (firstField != null) {
            first = firstField.getValue();
            firstConfidence = firstField.getConfidence();
        }
        if (secondField != null) {
            second = secondField.getValue();
            secondConfidence = secondField.getConfidence();
        }

        if (first == null && second == null) {
            return; // Nothing to do
        }

        if (second == null) {
            if (firstConfidence >= 0) {
                userAgent.set(targetName, first, firstConfidence);
                return;
            }
            return; // Nothing to do
        } else {
            if (first == null) {
                if (secondConfidence >= 0) {
                    userAgent.set(targetName, second, secondConfidence);
                }
                return;
            }
        }

        if (first.equals(second)) {
            userAgent.set(targetName, first, firstConfidence);
        } else {
            if (second.startsWith(first)) {
                userAgent.set(targetName, second, secondConfidence);
            } else {
                userAgent.set(targetName, first + " " + second, Math.max(firstField.getConfidence(), secondField.getConfidence()));
            }
        }
    }

    private void addMajorVersionField(UserAgent userAgent, String versionName, String majorVersionName) {
        if (!isWantedField(majorVersionName)) {
            return;
        }
        UserAgent.AgentField agentVersionMajor = userAgent.get(majorVersionName);
        if (agentVersionMajor == null || agentVersionMajor.getConfidence() == -1) {
            UserAgent.AgentField agentVersion = userAgent.get(versionName);
            if (agentVersion != null) {
                String version = agentVersion.getValue();
                if (version != null) {
                    version = VersionSplitter.getInstance().getSingleSplit(agentVersion.getValue(), 1);
                }
                userAgent.set(
                    majorVersionName,
                    version,
                    agentVersion.getConfidence());
            }
        }
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
        Set<MatcherAction> relevantActions = informMatcherActions.get(match.toLowerCase(Locale.ENGLISH));
        if (verbose) {
            if (relevantActions == null) {
                LOG.info("--- Have (0): {}", match);
            } else {
                LOG.info("+++ Have ({}): {}", relevantActions.size(), match);

                int count = 1;
                for (MatcherAction action : relevantActions) {
                    LOG.info("+++ -------> ({}): {}", count, action.toString());
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

    public void preHeat() {
        preHeat(testCases.size(), true);
    }
    public void preHeat(int preheatIterations) {
        preHeat(preheatIterations, true);
    }
    public void preHeat(int preheatIterations, boolean log) {
        if (testCases.isEmpty() || preheatIterations == 0) {
            LOG.warn("NO PREHEAT WAS DONE. Simply because there are no test cases available.");
        } else {
            if (log) {
                LOG.info("Preheating JVM by running {} testcases.", preheatIterations);
            }
            int remainingIterations = preheatIterations;
            int goodResults = 0;
            while (remainingIterations > 0) {
                for (Map<String, Map<String, String>> test : testCases) {
                    Map<String, String> input = test.get("input");
                    if (input == null) {
                        continue;
                    }

                    String userAgentString = input.get("user_agent_string");
                    if (userAgentString == null) {
                        continue;
                    }
                    remainingIterations--;
                    // Calculate and use result to guarantee not optimized away.
                    if(!UserAgentAnalyzerDirect.this.parse(userAgentString).hasSyntaxError()) {
                        goodResults++;
                    }
                    if (remainingIterations <= 0) {
                        break;
                    }
                }
            }
            if (log) {
                LOG.info("Preheating JVM completed. ({} of {} were proper results)", goodResults, preheatIterations);
            }
        }
    }

    // ===============================================================================================================

    public static class GetAllPathsAnalyzer implements Analyzer {
        final List<String> values = new ArrayList<>(128);
        final UserAgentTreeFlattener flattener;

        private final UserAgent result;

        GetAllPathsAnalyzer(String useragent) {
            flattener = new UserAgentTreeFlattener(this);
            result = flattener.parse(useragent);
        }

        public List<String> getValues() {
            return values;
        }

        public UserAgent getResult() {
            return result;
        }

        public void inform(String path, String value, ParseTree ctx) {
            values.add(path);
            values.add(path + "=\"" + value + "\"");
            values.add(path + "{\"" + firstCharactersForPrefixHash(value, MAX_PREFIX_HASH_MATCH) + "\"");
        }

        public void informMeAbout(MatcherAction matcherAction, String keyPattern) {
        }
    }

    public static List<String> getAllPaths(String agent) {
        return new GetAllPathsAnalyzer(agent).getValues();
    }

    public static GetAllPathsAnalyzer getAllPathsAnalyzer(String agent) {
        return new GetAllPathsAnalyzer(agent);
    }

    // ===============================================================================================================

    @SuppressWarnings("unchecked")
    public static class UserAgentAnalyzerDirectBuilder<UAA extends UserAgentAnalyzerDirect, B extends UserAgentAnalyzerDirectBuilder<UAA, B>> {
        private final UAA uaa;
        private boolean didBuildStep = false;
        private int preheatIterations = 0;

        private List<String> resources = new ArrayList<>();

        protected void failIfAlreadyBuilt() {
            if (didBuildStep) {
                throw new IllegalStateException("A builder can provide only a single instance. It is not allowed to set values after doing build()");
            }
        }

        protected UserAgentAnalyzerDirectBuilder(UAA newUaa) {
            this.uaa = newUaa;
            this.uaa.setShowMatcherStats(false);
            resources.add(DEFAULT_RESOURCES);
        }

        /**
         * Drop the default set of rules. Useful in parsing ONLY company specific useragents.
         */
        public B dropDefaultResources() {
            failIfAlreadyBuilt();
            resources.remove(DEFAULT_RESOURCES);
            return (B)this;
        }

        /**
         * Add a set of additional rules. Useful in handling specific cases.
         */
        public B addResources(String resourceString) {
            failIfAlreadyBuilt();
            resources.add(resourceString);
            return (B)this;
        }

        public B preheat(int iterations) {
            failIfAlreadyBuilt();
            this.preheatIterations = iterations;
            return (B)this;
        }

        public B preheat() {
            failIfAlreadyBuilt();
            this.preheatIterations = -1;
            return (B)this;
        }

        public B withField(String fieldName) {
            failIfAlreadyBuilt();
            if (uaa.wantedFieldNames == null) {
                uaa.wantedFieldNames = new ArrayList<>(32);
            }
            uaa.wantedFieldNames.add(fieldName);
            return (B)this;
        }

        public B withFields(Collection<String> fieldNames) {
            for (String fieldName : fieldNames) {
                withField(fieldName);
            }
            return (B)this;
        }

        public B withFields(String... fieldNames) {
            for (String fieldName : fieldNames) {
                withField(fieldName);
            }
            return (B)this;
        }

        public B withAllFields() {
            failIfAlreadyBuilt();
            uaa.wantedFieldNames = null;
            return (B)this;
        }

        public B showMatcherLoadStats() {
            failIfAlreadyBuilt();
            uaa.setShowMatcherStats(true);
            return (B)this;
        }

        public B hideMatcherLoadStats() {
            failIfAlreadyBuilt();
            uaa.setShowMatcherStats(false);
            return (B)this;
        }

        public B withUserAgentMaxLength(int newUserAgentMaxLength) {
            failIfAlreadyBuilt();
            uaa.setUserAgentMaxLength(newUserAgentMaxLength);
            return (B)this;
        }

        public B keepTests() {
            failIfAlreadyBuilt();
            uaa.keepTests();
            return (B)this;
        }

        public B dropTests() {
            failIfAlreadyBuilt();
            uaa.dropTests();
            return (B)this;
        }

        public B delayInitialization() {
            failIfAlreadyBuilt();
            uaa.delayInitialization();
            return (B)this;
        }

        public B immediateInitialization() {
            failIfAlreadyBuilt();
            uaa.immediateInitialization();
            return (B)this;
        }

        private void addGeneratedFields(String result, String... dependencies) {
            if (uaa.wantedFieldNames.contains(result)) {
                Collections.addAll(uaa.wantedFieldNames, dependencies);
            }
        }

        public UAA build() {
            failIfAlreadyBuilt();
            if (uaa.wantedFieldNames != null) {
                addGeneratedFields("AgentNameVersion", AGENT_NAME, AGENT_VERSION);
                addGeneratedFields("AgentNameVersionMajor", AGENT_NAME, AGENT_VERSION_MAJOR);
                addGeneratedFields("WebviewAppNameVersionMajor", "WebviewAppName", "WebviewAppVersionMajor");
                addGeneratedFields("LayoutEngineNameVersion", LAYOUT_ENGINE_NAME, LAYOUT_ENGINE_VERSION);
                addGeneratedFields("LayoutEngineNameVersionMajor", LAYOUT_ENGINE_NAME, LAYOUT_ENGINE_VERSION_MAJOR);
                addGeneratedFields("OperatingSystemNameVersion", OPERATING_SYSTEM_NAME, OPERATING_SYSTEM_VERSION);
                addGeneratedFields(DEVICE_NAME, DEVICE_BRAND);
                addGeneratedFields(AGENT_VERSION_MAJOR, AGENT_VERSION);
                addGeneratedFields(LAYOUT_ENGINE_VERSION_MAJOR, LAYOUT_ENGINE_VERSION);
                addGeneratedFields("WebviewAppVersionMajor", "WebviewAppVersion");

                // If we do not have a Brand we try to extract it from URL/Email iff present.
                addGeneratedFields(DEVICE_BRAND, "AgentInformationUrl", "AgentInformationEmail");

                // Special field that affects ALL fields.
                uaa.wantedFieldNames.add(SET_ALL_FIELDS);
            }

            boolean mustDropTestsLater = !uaa.willKeepTests();
            if (preheatIterations != 0) {
                uaa.keepTests();
            }
            uaa.initialize(resources);
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
}
