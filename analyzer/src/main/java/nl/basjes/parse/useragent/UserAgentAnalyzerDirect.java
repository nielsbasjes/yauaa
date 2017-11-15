/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2017 Niels Basjes
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

public class UserAgentAnalyzerDirect extends Analyzer implements Serializable {

    private static final int INFORM_ACTIONS_HASHMAP_SIZE = 300000;

    private static final Logger LOG = LoggerFactory.getLogger(UserAgentAnalyzerDirect.class);
    protected List<Matcher> allMatchers = new ArrayList<>(5000);
    private Map<String, Set<MatcherAction>> informMatcherActions = new HashMap<>(INFORM_ACTIONS_HASHMAP_SIZE);
    private transient Map<String, List<MappingNode>> matcherConfigs = new HashMap<>();

    private boolean showMatcherStats = false;
    private boolean doingOnlyASingleTest = false;

    // If we want ALL fields this is null. If we only want specific fields this is a list of names.
    protected List<String> wantedFieldNames = null;

    protected final List<Map<String, Map<String, String>>> testCases = new ArrayList<>(2048);
    private Map<String, Map<String, String>> lookups = new HashMap<>(128);
    private Map<String, Set<String>> lookupSets = new HashMap<>(128);

    protected UserAgentTreeFlattener flattener;

    public static final int DEFAULT_USER_AGENT_MAX_LENGTH = 2048;
    private int userAgentMaxLength = DEFAULT_USER_AGENT_MAX_LENGTH;

    /**
     * Initialize the transient default values
     */
    private void setDefaultFieldValues() {
        matcherConfigs = new HashMap<>(64);
    }

    private void readObject(java.io.ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        setDefaultFieldValues();
        stream.defaultReadObject();

        List<String> lines = new ArrayList<>();
        lines.add("This Analyzer instance was deserialized.");
        lines.add("");
        lines.add("Lookups      : " + ((lookups == null) ? 0 : lookups.size()));
        lines.add("LookupSets   : " + ((lookupSets == null) ? 0 : lookupSets.size()));
        lines.add("Matchers     : " + allMatchers.size());
        lines.add("Hashmap size : " + informMatcherActions.size());
        lines.add("Testcases    : " + testCases.size());
//        lines.add("All possible field names:");
//        int count = 1;
//        for (String fieldName : getAllPossibleFieldNames()) {
//            lines.add("- " + count++ + ": " + fieldName);
//        }

        String[] x = {};
        logVersion(lines.toArray(x));
    }

    public UserAgentAnalyzerDirect() {
        this(true);
    }

    protected UserAgentAnalyzerDirect(boolean initialize) {
        setDefaultFieldValues();
        if (initialize) {
            initialize();
        }
    }

    public UserAgentAnalyzerDirect(String resourceString) {
        setDefaultFieldValues();
        setShowMatcherStats(true);
        loadResources(resourceString);
    }

    public UserAgentAnalyzerDirect setShowMatcherStats(boolean newShowMatcherStats) {
        this.showMatcherStats = newShowMatcherStats;
        return this;
    }

    protected void initialize() {
        logVersion();
        loadResources("classpath*:UserAgents/**/*.yaml");
        verifyWeAreNotAskingForImpossibleFields();
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


    public static void logVersion(String... extraLines) {
        String[] lines = {
            "For more information: https://github.com/nielsbasjes/yauaa",
            "Copyright (C) 2013-2017 Niels Basjes - License Apache 2.0"
        };
        String version = getVersion();
        int width = version.length();
        for (String line : lines) {
            width = Math.max(width, line.length());
        }
        for (String line : extraLines) {
            width = Math.max(width, line.length());
        }

        LOG.info("");
        LOG.info("/-{}-\\", padding('-', width));
        logLine(version, width);
        LOG.info("+-{}-+", padding('-', width));
        for (String line : lines) {
            logLine(line, width);
        }
        if (extraLines.length > 0) {
            LOG.info("+-{}-+", padding('-', width));
            for (String line : extraLines) {
                logLine(line, width);
            }
        }

        LOG.info("\\-{}-/", padding('-', width));
        LOG.info("");
    }

    private static String padding(char letter, int count) {
        StringBuilder sb = new StringBuilder(128);
        for (int i = 0; i < count; i++) {
            sb.append(letter);
        }
        return sb.toString();
    }

    private static void logLine(String line, int width) {
        LOG.info("| {}{} |", line, padding(' ', width - line.length()));
    }

    // --------------------------------------------


    public static String getVersion() {
        return "Yauaa " + Version.getProjectVersion() + " (" + Version.getGitCommitIdDescribeShort() + " @ " + Version.getBuildTimestamp() + ")";
    }

    public void loadResources(String resourceString) {
        LOG.info("Loading from: \"{}\"", resourceString);

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
        LOG.info("Loaded {} files", resources.size());

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

        LOG.info("Building all matchers");
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
                int startSize = informMatcherActions.size();
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
                int stopSize = informMatcherActions.size();
                int stopSkipped = skippedMatchers;

                if (showMatcherStats) {
                    Formatter msg = new Formatter(Locale.ENGLISH);
                    msg.format("Building %4d (dropped %4d) matchers from %-" + maxFilenameLength + "s " +
                            "took %5d msec resulted in %8d extra hashmap entries",
                        matcherConfig.size(),
                        stopSkipped - startSkipped,
                        configFilename,
                        (stop - start) / 1000000,
                        stopSize - startSize);
                    LOG.info(msg.toString());
                }
            }
            long fullStop = System.nanoTime();

            Formatter msg = new Formatter(Locale.ENGLISH);
            msg.format("Building %4d (dropped %4d) matchers from %4d files took %5d msec resulted in %8d hashmap entries",
                totalNumberOfMatchers,
                skippedMatchers,
                matcherConfigs.size(),
                (fullStop - fullStart) / 1000000,
                informMatcherActions.size());
            LOG.info(msg.toString());

        }
        LOG.info("Analyzer stats");
        LOG.info("Lookups         : {}", (lookups == null) ? 0 : lookups.size());
        LOG.info("LookupSets      : {}", (lookupSets == null) ? 0 : lookupSets.size());
        LOG.info("Matchers        : {} (total:{} ; dropped: {})", allMatchers.size(), totalNumberOfMatchers, skippedMatchers);
        LOG.info("Hashmap size    : {}", informMatcherActions.size());
        LOG.info("Ranges map size : {}", informMatcherActionRanges.size());
        LOG.info("Testcases       : {}", testCases.size());
//        LOG.info("All possible field names:");
//        int count = 1;
//        for (String fieldName : getAllPossibleFieldNames()) {
//            LOG.info("- {}: {}", count++, fieldName);
//        }
    }

    /**
     * Used by some unit tests to get rid of all the standard tests and focus on the experiment at hand.
     */
    public void eraseTestCases() {
        testCases.clear();
    }

    public Set<String> getAllPossibleFieldNames() {
        Set<String> results = new TreeSet<>();
        results.addAll(HARD_CODED_GENERATED_FIELDS);
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
        Node loadedYaml = yaml.compose(new UnicodeReader(yamlStream));

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
                    loadYamlTestcase(actualEntry, filename);
                    break;
                default:
                    throw new InvalidParserConfigurationException(
                        "Yaml config.(" + filename + ":" + actualEntry.getStartMark().getLine() + "): " +
                            "Found unexpected config entry: " + entryType + ", allowed are 'lookup, 'matcher' and 'test'");
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

    public synchronized UserAgent parse(UserAgent userAgent) {
        String useragentString = userAgent.getUserAgentString();
        if (useragentString != null && useragentString.length() > userAgentMaxLength) {
            userAgent.set(DEVICE_CLASS, "Hacker", 100);
            userAgent.set(DEVICE_BRAND, "Hacker", 100);
            userAgent.set(DEVICE_NAME, "Hacker", 100);
            userAgent.set(DEVICE_VERSION, "Hacker", 100);
            userAgent.set(OPERATING_SYSTEM_CLASS, "Hacker", 100);
            userAgent.set(OPERATING_SYSTEM_NAME, "Hacker", 100);
            userAgent.set(OPERATING_SYSTEM_VERSION, "Hacker", 100);
            userAgent.set(LAYOUT_ENGINE_CLASS, "Hacker", 100);
            userAgent.set(LAYOUT_ENGINE_NAME, "Hacker", 100);
            userAgent.set(LAYOUT_ENGINE_VERSION, "Hacker", 100);
            userAgent.set(LAYOUT_ENGINE_VERSION_MAJOR, "Hacker", 100);
            userAgent.set(AGENT_CLASS, "Hacker", 100);
            userAgent.set(AGENT_NAME, "Hacker", 100);
            userAgent.set(AGENT_VERSION, "Hacker", 100);
            userAgent.set(AGENT_VERSION_MAJOR, "Hacker", 100);
            userAgent.set("HackerToolkit", "Unknown", 100);
            userAgent.set("HackerAttackVector", "Buffer overflow", 100);
            return hardCodedPostProcessing(userAgent);
        }

        // Reset all Matchers
        for (Matcher matcher : allMatchers) {
            matcher.reset();
        }

        if (userAgent.isDebug()) {
            for (Matcher matcher : allMatchers) {
                matcher.setVerboseTemporarily(false);
            }
        }

        userAgent = flattener.parse(userAgent);

        // Fire all Analyzers
        for (Matcher matcher : allMatchers) {
            matcher.analyze(userAgent);
        }

        userAgent.processSetAll();
        return hardCodedPostProcessing(userAgent);
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
        return userAgent;
    }

    private void concatFieldValuesNONDuplicated(UserAgent userAgent, String targetName, String firstName, String secondName) {
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
        UserAgent.AgentField agentVersionMajor = userAgent.get(majorVersionName);
        if (agentVersionMajor == null || agentVersionMajor.getConfidence() == -1) {
            UserAgent.AgentField agentVersion = userAgent.get(versionName);
            if (agentVersion != null) {
                userAgent.set(
                    majorVersionName,
                    VersionSplitter.getInstance().getSingleSplit(agentVersion.getValue(), 1),
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
        if (!testCases.isEmpty()) {
            if (preheatIterations > 0) {
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
                    LOG.info("Preheating JVM completed. ({} proper results)", preheatIterations, goodResults);
                }
            }
        }
    }

    // ===============================================================================================================

    public static class GetAllPathsAnalyzer extends Analyzer {
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
        }

        public void informMeAbout(MatcherAction matcherAction, String keyPattern) {
        }
    }

    @SuppressWarnings({"unused"})
    public static List<String> getAllPaths(String agent) {
        return new GetAllPathsAnalyzer(agent).getValues();
    }

    public static GetAllPathsAnalyzer getAllPathsAnalyzer(String agent) {
        return new GetAllPathsAnalyzer(agent);
    }

    // ===============================================================================================================

    @SuppressWarnings("unchecked")
    public static UserAgentAnalyzerDirectBuilder<? extends UserAgentAnalyzerDirect, ? extends UserAgentAnalyzerDirectBuilder> newBuilder() {
        return new UserAgentAnalyzerDirectBuilder<>(new UserAgentAnalyzerDirect(false));
    }

    @SuppressWarnings("unchecked")
    public static class UserAgentAnalyzerDirectBuilder<UAA extends UserAgentAnalyzerDirect, B extends UserAgentAnalyzerDirectBuilder<UAA, B>> {
        private final UAA uaa;
        private int preheatIterations = 0;

        protected UserAgentAnalyzerDirectBuilder(UAA newUaa) {
            this.uaa = newUaa;
            this.uaa.setShowMatcherStats(false);
        }

        public B preheat(int iterations) {
            this.preheatIterations = iterations;
            return (B)this;
        }

        public B withField(String fieldName) {
            if (uaa.wantedFieldNames == null) {
                uaa.wantedFieldNames = new ArrayList<>(32);
            }
            uaa.wantedFieldNames.add(fieldName);
            return (B)this;
        }

        public B withFields(Collection<String> fieldNames) {
            if (fieldNames == null) {
                return (B)this;
            }
            for (String fieldName : fieldNames) {
                withField(fieldName);
            }
            return (B)this;
        }

        public B withAllFields() {
            uaa.wantedFieldNames = null;
            return (B)this;
        }

        public B showMatcherLoadStats() {
            uaa.setShowMatcherStats(true);
            return (B)this;
        }

        public B hideMatcherLoadStats() {
            uaa.setShowMatcherStats(false);
            return (B)this;
        }

        public B withUserAgentMaxLength(int newUserAgentMaxLength) {
            uaa.setUserAgentMaxLength(newUserAgentMaxLength);
            return (B)this;
        }

        private void addGeneratedFields(String result, String... dependencies) {
            if (uaa.wantedFieldNames.contains(result)) {
                Collections.addAll(uaa.wantedFieldNames, dependencies);
            }
        }

        public UAA build() {
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

                // Special field that affects ALL fields.
                uaa.wantedFieldNames.add(SET_ALL_FIELDS);
            }
            uaa.initialize();
            if (preheatIterations > 0) {
                uaa.preHeat(preheatIterations);
            }
            return uaa;
        }

    }
}
