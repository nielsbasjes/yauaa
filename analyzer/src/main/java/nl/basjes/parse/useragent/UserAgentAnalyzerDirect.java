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

package nl.basjes.parse.useragent;

import com.esotericsoftware.kryo.DefaultSerializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import nl.basjes.parse.useragent.analyze.Analyzer;
import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import nl.basjes.parse.useragent.analyze.Matcher;
import nl.basjes.parse.useragent.analyze.MatcherAction;
import nl.basjes.parse.useragent.analyze.MatcherList;
import nl.basjes.parse.useragent.analyze.UselessMatcherException;
import nl.basjes.parse.useragent.analyze.WordRangeVisitor.Range;
import nl.basjes.parse.useragent.calculate.CalculateAgentEmail;
import nl.basjes.parse.useragent.calculate.CalculateDeviceBrand;
import nl.basjes.parse.useragent.calculate.CalculateDeviceName;
import nl.basjes.parse.useragent.calculate.ConcatNONDuplicatedCalculator;
import nl.basjes.parse.useragent.calculate.FieldCalculator;
import nl.basjes.parse.useragent.calculate.MajorVersionCalculator;
import nl.basjes.parse.useragent.parse.AgentPathFragment;
import nl.basjes.parse.useragent.parse.PathMatcherTree;
import nl.basjes.parse.useragent.parse.MatcherTree;
import nl.basjes.parse.useragent.parse.UserAgentTreeFlattener;
import org.antlr.v4.runtime.tree.ParseTree;
import nl.basjes.parse.useragent.utils.Normalize;
import nl.basjes.parse.useragent.utils.VersionSplitter;
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
import static nl.basjes.parse.useragent.UserAgent.AGENT_INFORMATION_EMAIL;
import static nl.basjes.parse.useragent.UserAgent.AGENT_INFORMATION_URL;
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
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_CLASS;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_NAME;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_VERSION;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.PRE_SORTED_FIELDS_LIST;
import static nl.basjes.parse.useragent.UserAgent.SET_ALL_FIELDS;
import static nl.basjes.parse.useragent.UserAgent.SYNTAX_ERROR;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_NAME;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_VERSION;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_VERSION_MAJOR;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.AGENT;
import static nl.basjes.parse.useragent.utils.YamlUtils.getExactlyOneNodeTuple;
import static nl.basjes.parse.useragent.utils.YamlUtils.getKeyAsString;
import static nl.basjes.parse.useragent.utils.YamlUtils.getStringValues;
import static nl.basjes.parse.useragent.utils.YamlUtils.getValueAsMappingNode;
import static nl.basjes.parse.useragent.utils.YamlUtils.getValueAsSequenceNode;
import static nl.basjes.parse.useragent.utils.YamlUtils.getValueAsString;
import static nl.basjes.parse.useragent.utils.YamlUtils.require;
import static nl.basjes.parse.useragent.utils.YamlUtils.requireNodeInstanceOf;
import static nl.basjes.parse.useragent.utils.YauaaVersion.assertSameVersion;
import static nl.basjes.parse.useragent.utils.YauaaVersion.logVersion;

@DefaultSerializer(UserAgentAnalyzerDirect.KryoSerializer.class)
public class UserAgentAnalyzerDirect implements Analyzer, Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(UserAgentAnalyzerDirect.class);
    private final List<Matcher> allMatchers = new ArrayList<>(5000);
    private final MatcherList zeroInputMatchers = new MatcherList(100);

    protected List<Matcher> getAllMatchers() {
        return allMatchers;
    }

    private final     MatcherTree                    informMatcherActions = new MatcherTree(AGENT, 1);
    private transient Map<String, List<MappingNode>> matcherConfigs       = new HashMap<>();

    private boolean showMatcherStats = false;
    private boolean doingOnlyASingleTest = false;

    // If we want ALL fields this is null. If we only want specific fields this is a list of names.
    protected Set<String> wantedFieldNames = null; // NOSONAR: Only accessed via Builder.

    private final List<Map<String, Map<String, String>>> testCases = new ArrayList<>(2048);

    public List<Map<String, Map<String, String>>> getTestCases() {
        return testCases;
    }

    private Map<String, Map<String, String>> lookups = new HashMap<>(128);
    private final Map<String, Set<String>> lookupSets = new HashMap<>(128);

    @Override
    public Map<String, Map<String, String>> getLookups() {
        return lookups;
    }

    @Override
    public Map<String, Set<String>> getLookupSets() {
        return lookupSets;
    }

    @Override
    public MatcherTree getMatcherTreeRoot() {
        return informMatcherActions;
    }

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
        touchedMatchers = new MatcherList(16);
    }

    private void readObject(java.io.ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        initTransientFields();
        stream.defaultReadObject();
        showDeserializationStats();
    }

    public static class KryoSerializer extends FieldSerializer<UserAgentAnalyzerDirect> {
        public KryoSerializer(Kryo kryo, Class<?> type) {
            super(kryo, type);
        }

        @Override
        public UserAgentAnalyzerDirect read(Kryo kryo, Input input, Class<UserAgentAnalyzerDirect> type) {
            UserAgentAnalyzerDirect uaa = super.read(kryo, input, type);
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
        long fullStart = System.nanoTime();

        if (wantedFieldNames != null) {
            int wantedSize = wantedFieldNames.size();
            if (wantedFieldNames.contains(SET_ALL_FIELDS)) {
                wantedSize--;
            }
            LOG.info("Building all needed matchers for the requested {} fields.", wantedSize);
        } else {
            LOG.info("Building all matchers for all possible fields.");
        }

        resources.forEach(this::loadResources);

        if (matcherConfigs.isEmpty()) {
            throw new InvalidParserConfigurationException("No matchers were loaded at all.");
        }

        long fullStop = System.nanoTime();

        try(Formatter msg = new Formatter(Locale.ENGLISH)) {
            msg.format("Loading %4d matchers, %d lookups, %d lookupsets, %d testcases from %4d files took %5d msec",
                allMatchers.size(),
                (lookups == null) ? 0 : lookups.size(),
                lookupSets.size(),
                testCases.size(),
                matcherConfigs.size(),
                (fullStop - fullStart) / 1000000);
            LOG.info(msg.toString());
        }


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

    public void loadResources(String resourceString) {
        if (matchersHaveBeenInitialized) {
            throw new IllegalStateException("Refusing to load additional resources after the datastructures have been initialized.");
        }

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
            throw new InvalidParserConfigurationException("Error reading resources: " + e.getMessage(), e);
        }
        doingOnlyASingleTest = false;
        int maxFilenameLength = 0;

        if (resources.isEmpty()) {
            LOG.warn("NO config files were found matching this expression: {}", resourceString);

            if (DEFAULT_RESOURCES.equals(resourceString)) {
                LOG.warn("Unable to load the default resources, usually caused by classloader problems.");
                LOG.warn("Retrying with built in list.");
                PackagedRules.getRuleFileNames().forEach(this::loadResources);
            } else {
                LOG.error("If you are using wildcards in your expression then try explicitly naming all yamls files explicitly.");
            }

            return;
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
                alreadyLoadedResourceBasenames.size(), alreadyLoadedResourceBasenames);
            throw new InvalidParserConfigurationException("Trying to load " + alreadyLoadedResourceBasenames.size() +
                " resources for the second time");
        }

        for (Map.Entry<String, Resource> resourceEntry : resources.entrySet()) {
            try {
                Resource resource = resourceEntry.getValue();
                String filename = resource.getFilename();
                if (filename != null) {
                    maxFilenameLength = Math.max(maxFilenameLength, filename.length());
                    loadResource(yaml, resource.getInputStream(), filename);
                }
            } catch (IOException e) {
                throw new InvalidParserConfigurationException("Error reading resources: " + e.getMessage(), e);
            }
        }

        long stopFiles = System.nanoTime();
        try(Formatter msg = new Formatter(Locale.ENGLISH)) {
            msg.format("Loading %2d files in %4d msec from %s",
                resources.size(),
                (stopFiles - startFiles) / 1000000,
                resourceString);
            LOG.info(msg.toString());
        }

        if (resources.isEmpty()) {
            throw new InvalidParserConfigurationException("No matchers were loaded at all.");
        }

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

        int skippedMatchers = 0;
        if (matcherConfigs != null) {
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
                        allMatchers.add(new Matcher(this, wantedFieldNames, map, configFilename));
                        totalNumberOfMatchers++;
                    } catch (UselessMatcherException ume) {
                        skippedMatchers++;
                    }
                }
                long stop = System.nanoTime();
                int stopSkipped = skippedMatchers;

                if (showMatcherStats) {
                    try(Formatter msg = new Formatter(Locale.ENGLISH)) {
                        msg.format("Loading %4d (dropped %4d) matchers from " +
                                "%-" + maxFilenameLength + "s " + // NOSONAR: I'm creating the format using concatenation
                                "took %5d msec",
                            matcherConfig.size() - (stopSkipped - startSkipped),
                            stopSkipped - startSkipped,
                            configFilename,
                            (stop - start) / 1000000);
                        LOG.info(msg.toString());
                    }
                }
            }
        }
    }

    private boolean matchersHaveBeenInitialized = false;
    public void initializeMatchers() {
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
        LOG.info("Built in {} msec : TreeSize {} nodes, Ranges map:{}",
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

        touchedMatchers = new MatcherList(16);

        // FIX ME: Devlopment debugging ONLY
//        List<String> childStrings = informMatcherActions.getChildrenStrings();
//        LOG.warn(">Final tree: {}", childStrings.size());
//        childStrings.forEach(s -> LOG.warn("--> {}", s));
//        LOG.warn("<Final tree: {}", childStrings.size());
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
            LOG.warn("The file {} is empty", filename);
            return;
        }

        // Get and check top level config
        requireNodeInstanceOf(MappingNode.class, loadedYaml, filename, "File must be a Map");

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

        require(configNodeTuple != null, loadedYaml, filename, "The top level entry MUST be 'config'.");

        SequenceNode configNode = getValueAsSequenceNode(configNodeTuple, filename);
        List<Node> configList = configNode.getValue();

        for (Node configEntry : configList) {
            requireNodeInstanceOf(MappingNode.class, configEntry, filename, "The entry MUST be a mapping");
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

                        if (map.containsKey(key)) {
                            throw new InvalidParserConfigurationException(
                                "In the lookup \"" + name + "\" the key \"" + key + "\" appears multiple times.");
                        }

                        map.put(key, value);
                    }
                    break;
                default:
                    break;
            }
        }

        require(name != null && map != null, entry, filename, "Invalid lookup specified");

        lookups.put(name, map);
    }

    private void loadYamlLookupSets(MappingNode entry, String filename) {
        String name = null;
        Set<String> lookupSet = new HashSet<>();

        for (NodeTuple tuple : entry.getValue()) {
            switch (getKeyAsString(tuple, filename)) {
                case "name":
                    name = getValueAsString(tuple, filename);
                    break;
                case "values":
                    SequenceNode node = getValueAsSequenceNode(tuple, filename);
                    for (String value: getStringValues(node, filename)) {
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
        List<MappingNode> matcherConfigList = matcherConfigs
            .computeIfAbsent(filename, k -> new ArrayList<>(32));
        matcherConfigList.add(entry);
    }

    private void loadYamlTestcase(MappingNode entry, String filename) {
        if (!doingOnlyASingleTest) {
            Map<String, String> metaData = new HashMap<>();
            metaData.put("filename", filename);
            metaData.put("fileline", String.valueOf(entry.getStartMark().getLine()));

            Map<String, String> input = null;
            List<String> options = null;
            Map<String, String> expected = new HashMap<>();
            for (NodeTuple tuple : entry.getValue()) {
                String name = getKeyAsString(tuple, filename);
                switch (name) {
                    case "options":
                        options = getStringValues(tuple.getValueNode(), filename);
                        if (options.contains("only")) {
                            doingOnlyASingleTest = true;
                            testCases.clear();
                        }
                        break;
                    case "input":
                        for (NodeTuple inputTuple : getValueAsMappingNode(tuple, filename).getValue()) {
                            String inputName = getKeyAsString(inputTuple, filename);
                            if ("user_agent_string".equals(inputName)) {
                                String inputString = getValueAsString(inputTuple, filename);
                                input = new HashMap<>();
                                input.put(inputName, inputString);
                            }
                        }
                        break;
                    case "expected":
                        List<NodeTuple> mappings = getValueAsMappingNode(tuple, filename).getValue();
                        for (NodeTuple mapping : mappings) {
                            String key = getKeyAsString(mapping, filename);
                            String value = getValueAsString(mapping, filename);
                            expected.put(key, value);
                        }
                        break;
                    default:
                        // Ignore
                        break;
                }
            }

            require(input != null, entry, filename, "Test is missing input");

            if (expected.isEmpty()) {
                doingOnlyASingleTest = true;
                testCases.clear();
            }

            Map<String, Map<String, String>> testCase = new HashMap<>();

            testCase.put("input", input);
            if (!expected.isEmpty()) {
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
    public void informMeAboutPrefix(MatcherAction matcherAction, MatcherTree matcherTree, String prefix) {
        String treeName = matcherTree.toString(); // FIXME:
//        this.informMeAbout(matcherAction, treeName + "{\"" + firstCharactersForPrefixHash(prefix, MAX_PREFIX_HASH_MATCH) + "\"");
        Set<Integer> lengths = informMatcherActionPrefixesLengths.computeIfAbsent(treeName, k -> new HashSet<>(4));
        lengths.add(firstCharactersForPrefixHashLength(prefix, MAX_PREFIX_HASH_MATCH));
    }

    @Override
    public Set<Integer> getRequiredPrefixLengths(String treeName) {
        return informMatcherActionPrefixesLengths.get(treeName);
    }

    public void informMeAbout(MatcherAction matcherAction, MatcherTree matcherTree) {
        LOG.info("[informMeAbout] tree: {}   -->  action {}", matcherTree, matcherAction);

//        Set<MatcherAction> analyzerSet = informMatcherActions
//            .computeIfAbsent(hashKey, k -> new HashSet<>());
//        analyzerSet.add(matcherAction);
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
        UserAgent userAgent = new UserAgent(userAgentString, wantedFieldNames);
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
        userAgent.set(HACKER_TOOLKIT,              "Unknown",          confidence);
        userAgent.set(HACKER_ATTACK_VECTOR,         "Buffer overflow",  confidence);
        return userAgent;
    }


    private transient MatcherList touchedMatchers = null;

    @Override
    public void receivedInput(Matcher matcher) {
        touchedMatchers.add(matcher);
    }

    public synchronized UserAgent parse(UserAgent userAgent) {
        initializeMatchers();
        String useragentString = userAgent.getUserAgentString();
        if (useragentString != null && useragentString.length() > userAgentMaxLength) {
            setAsHacker(userAgent, 100);
            userAgent.setForced(HACKER_ATTACK_VECTOR, "Buffer overflow", 100);
            return hardCodedPostProcessing(userAgent);
        }

        // Reset all Matchers
        for (Matcher matcher : touchedMatchers) {
            matcher.reset();
        }
        touchedMatchers.clear();

        for (Matcher matcher : zeroInputMatchers) {
            matcher.reset();
        }

        if (userAgent.isDebug()) {
            for (Matcher matcher : allMatchers) {
                matcher.setVerboseTemporarily(true);
            }
        }

// FIXME: Reenable safetynet       try {
        userAgent = flattener.parse(userAgent);

        // Fire all Analyzers with any input
        for (Matcher matcher : touchedMatchers) {
            matcher.analyze(userAgent);
        }

        // Fire all Analyzers that should not get input
        for (Matcher matcher : zeroInputMatchers) {
            matcher.analyze(userAgent);
        }

        userAgent.processSetAll();
        return hardCodedPostProcessing(userAgent);
//        } catch (RuntimeException rte) {
////             If this occurs then someone has found a previously undetected problem.
////             So this is a safety for something that 'can' but 'should not' occur.
//            userAgent.reset();
//            userAgent = setAsHacker(userAgent, 10000);
//            userAgent.setForced("HackerAttackVector", "Yauaa Exploit", 10000);
//            return hardCodedPostProcessing(userAgent);
//        }
    }

    private static final List<String> HARD_CODED_GENERATED_FIELDS = new ArrayList<>();

    static {
        HARD_CODED_GENERATED_FIELDS.add(SYNTAX_ERROR);
        HARD_CODED_GENERATED_FIELDS.add(AGENT_VERSION_MAJOR);
        HARD_CODED_GENERATED_FIELDS.add(LAYOUT_ENGINE_VERSION_MAJOR);
        HARD_CODED_GENERATED_FIELDS.add(AGENT_NAME_VERSION);
        HARD_CODED_GENERATED_FIELDS.add(AGENT_NAME_VERSION_MAJOR);
        HARD_CODED_GENERATED_FIELDS.add(LAYOUT_ENGINE_NAME_VERSION);
        HARD_CODED_GENERATED_FIELDS.add(LAYOUT_ENGINE_NAME_VERSION_MAJOR);
        HARD_CODED_GENERATED_FIELDS.add(OPERATING_SYSTEM_NAME_VERSION);
        HARD_CODED_GENERATED_FIELDS.add(OPERATING_SYSTEM_NAME_VERSION_MAJOR);
        HARD_CODED_GENERATED_FIELDS.add(WEBVIEW_APP_VERSION_MAJOR);
        HARD_CODED_GENERATED_FIELDS.add(WEBVIEW_APP_NAME_VERSION_MAJOR);
    }

    public boolean isWantedField(String fieldName) {
        if (wantedFieldNames == null) {
            return true;
        }
        return wantedFieldNames.contains(fieldName);
    }

    List<FieldCalculator> fieldCalculators = new ArrayList<>();

    private UserAgent hardCodedPostProcessing(UserAgent userAgent) {
        // If it is really really bad ... then it is a Hacker.
        if ("true".equals(userAgent.getValue(SYNTAX_ERROR))) {
            if (userAgent.get(DEVICE_CLASS).getConfidence() == -1) {
                userAgent.set(DEVICE_CLASS,                "Hacker", 10);
                userAgent.set(DEVICE_BRAND,                "Hacker", 10);
                userAgent.set(DEVICE_NAME,                 "Hacker", 10);
                userAgent.set(DEVICE_VERSION,              "Hacker", 10);
                userAgent.set(OPERATING_SYSTEM_CLASS,      "Hacker", 10);
                userAgent.set(OPERATING_SYSTEM_NAME,       "Hacker", 10);
                userAgent.set(OPERATING_SYSTEM_VERSION,    "Hacker", 10);
                userAgent.set(LAYOUT_ENGINE_CLASS,         "Hacker", 10);
                userAgent.set(LAYOUT_ENGINE_NAME,          "Hacker", 10);
                userAgent.set(LAYOUT_ENGINE_VERSION,       "Hacker", 10);
                userAgent.set(LAYOUT_ENGINE_VERSION_MAJOR, "Hacker", 10);
                userAgent.set(AGENT_CLASS,                 "Hacker", 10);
                userAgent.set(AGENT_NAME,                  "Hacker", 10);
                userAgent.set(AGENT_VERSION,               "Hacker", 10);
                userAgent.set(AGENT_VERSION_MAJOR,         "Hacker", 10);
                userAgent.set(HACKER_TOOLKIT,              "Unknown", 10);
                userAgent.set(HACKER_ATTACK_VECTOR,        "Unknown", 10);
            }
        }

        // Calculate all fields that are constructed from the found ones.
        for (FieldCalculator fieldCalculator: fieldCalculators) {
            fieldCalculator.calculate(userAgent);
        }

        return userAgent;
    }

    public Set<Range> getRequiredInformRanges(String treeName) {
        return informMatcherActionRanges.computeIfAbsent(treeName, k -> Collections.emptySet());
    }

//    @Override
//    public void inform(MatcherTree matcherTree, String value, ParseTree ctx) {
//        String key = matcherTree.toString(); // FIXME
//        inform(key, key, value, ctx);
//        inform(key + "=\"" + value + '"', key, value, ctx);
//
//        Set<Integer> lengths = getRequiredPrefixLengths(key);
//        if (lengths != null) {
//            int valueLength = value.length();
//            for (Integer prefixLength : lengths) {
//                if (valueLength >= prefixLength) {
//                    inform(key + "{\"" + firstCharactersForPrefixHash(value, prefixLength) + '"', key, value, ctx);
//                }
//            }
//        }
//    }

//    private void inform(String match, String key, String value, ParseTree ctx) {
//        Set<MatcherAction> relevantActions = null; // FIXME informMatcherActions.get(match.toLowerCase(Locale.ENGLISH));
//        if (verbose) {
//            if (relevantActions == null) {
//                LOG.info("--- Have (0): {}", match);
//            } else {
//                LOG.info("+++ Have ({}): {}", relevantActions.size(), match);
//
//                int count = 1;
//                for (MatcherAction action : relevantActions) {
//                    LOG.info("+++ -------> ({}): {}", count, action);
//                    count++;
//                }
//            }
//        }
//
////        if (relevantActions != null) {
////            for (MatcherAction matcherAction : relevantActions) {
////                matcherAction.inform(key, value, ctx);
////            }
////        }
//    }


    /**
     * Runs all testcases once to heat up the JVM.
     * @return Number of actually done testcases.
     */
    public long preHeat() {
        return preHeat(testCases.size(), true);
    }
    /**
     * Runs the number of specified testcases to heat up the JVM.
     * @param preheatIterations Number of desired tests to run.
     * @return Number of actually done testcases.
     */
    public long preHeat(long preheatIterations) {
        return preHeat(preheatIterations, true);
    }

    private static final long MAX_PRE_HEAT_ITERATIONS = 1_000_000L;

    /**
     * Runs the number of specified testcases to heat up the JVM.
     * @param preheatIterations Number of desired tests to run.
     * @param log Enable logging?
     * @return Number of actually done testcases.
     */
    public long preHeat(long preheatIterations, boolean log) {
        if (testCases.isEmpty()) {
            LOG.warn("NO PREHEAT WAS DONE. Simply because there are no test cases available.");
            return 0;
        }
        if (preheatIterations <= 0) {
            LOG.warn("NO PREHEAT WAS DONE. Simply because you asked for {} to run.", preheatIterations);
            return 0;
        }
        if (preheatIterations > MAX_PRE_HEAT_ITERATIONS) {
            LOG.warn("NO PREHEAT WAS DONE. Simply because you asked for too many ({} > {}) to run.", preheatIterations, MAX_PRE_HEAT_ITERATIONS);
            return 0;
        }
        if (log) {
            LOG.info("Preheating JVM by running {} testcases.", preheatIterations);
        }
        long remainingIterations = preheatIterations;
        long goodResults = 0;
        while (remainingIterations > 0) {
            for (Map<String, Map<String, String>> test : testCases) {
                Map<String, String> input = test.get("input");
                String userAgentString = input.get("user_agent_string");
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
        return preheatIterations;
    }

    // ===============================================================================================================

    public static class GetAllPathsAnalyzer implements Analyzer {
        private final List<String> values = new ArrayList<>(128);

        private final UserAgent result;

        private final MatcherTree matcherTreeRoot = new MatcherTree(AGENT, 1);

        GetAllPathsAnalyzer(String useragent) {
            UserAgentTreeFlattener flattener = new UserAgentTreeFlattener(this);
            result = flattener.parse(useragent);
        }

        public List<String> getValues() {
            return values;
        }

        public UserAgent getResult() {
            return result;
        }

//        public void inform(MatcherTree matcherTree, String value, ParseTree ctx) {
//            String path = matcherTree.toString();
//            values.add(path);
//            values.add(path + "=\"" + value + "\"");
//            values.add(path + "{\"" + firstCharactersForPrefixHash(value, MAX_PREFIX_HASH_MATCH) + "\"");
//        }

        public void informMeAbout(MatcherAction matcherAction, MatcherTree matcherTree) {
            // Not needed to only get all paths
        }

        public void lookingForRange(String treeName, Range range) {
            // Not needed to only get all paths
        }

        public Set<Range> getRequiredInformRanges(String treeName) {
            // Not needed to only get all paths
            return Collections.emptySet();
        }

        @Override
        public void informMeAboutPrefix(MatcherAction matcherAction, MatcherTree matcherTree, String prefix) {
            // Not needed to only get all paths
        }

        @Override
        public Set<Integer> getRequiredPrefixLengths(String treeName) {
            // Not needed to only get all paths
            return Collections.emptySet();
        }

        @Override
        public Map<String, Map<String, String>> getLookups() {
            // Not needed to only get all paths
            return Collections.emptyMap();
        }

        @Override
        public Map<String, Set<String>> getLookupSets() {
            // Not needed to only get all paths
            return Collections.emptyMap();
        }

        @Override
        public MatcherTree getMatcherTreeRoot() {
            return matcherTreeRoot;
        }
    }

    public static List<String> getAllPaths(String agent) {
        return new GetAllPathsAnalyzer(agent).getValues();
    }

    public static GetAllPathsAnalyzer getAllPathsAnalyzer(String agent) {
        return new GetAllPathsAnalyzer(agent);
    }

    // ===============================================================================================================

    public static UserAgentAnalyzerDirectBuilder<? extends UserAgentAnalyzer, ? extends UserAgentAnalyzerDirectBuilder<?, ?>> newBuilder() {
        return new UserAgentAnalyzerDirectBuilder<>(new UserAgentAnalyzer());
    }

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

        private void addSpecialDependencies(String result, String... dependencies) {
            if (uaa.isWantedField(result)) {
                if (uaa.wantedFieldNames != null) {
                    Collections.addAll(uaa.wantedFieldNames, dependencies);
                }
            }
        }

        private void addCalculatedMajorVersionField(String result, String dependency) {
            if (uaa.isWantedField(result)) {
                uaa.fieldCalculators.add(new MajorVersionCalculator(result, dependency));
                if (uaa.wantedFieldNames != null) {
                    Collections.addAll(uaa.wantedFieldNames, dependency);
                }
            }
        }

        private void addCalculatedConcatNONDuplicated(String result, String first, String second) {
            if (uaa.isWantedField(result)) {
                uaa.fieldCalculators.add(new ConcatNONDuplicatedCalculator(result, first, second));
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

            if (uaa.isWantedField(DEVICE_NAME)) {
                uaa.fieldCalculators.add(new CalculateDeviceName());
                addSpecialDependencies(DEVICE_NAME, DEVICE_BRAND);
            }

            if (uaa.isWantedField(DEVICE_BRAND)) {
                uaa.fieldCalculators.add(new CalculateDeviceBrand());
                // If we do not have a Brand we try to extract it from URL/Email iff present.
                addSpecialDependencies(DEVICE_BRAND, AGENT_INFORMATION_URL, AGENT_INFORMATION_EMAIL);
            }

            if (uaa.isWantedField(AGENT_INFORMATION_EMAIL)) {
                uaa.fieldCalculators.add(new CalculateAgentEmail());
            }

            Collections.reverse(uaa.fieldCalculators);

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
