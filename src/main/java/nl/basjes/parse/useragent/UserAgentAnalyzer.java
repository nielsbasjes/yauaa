/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2016 Niels Basjes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package nl.basjes.parse.useragent;

import nl.basjes.parse.useragent.analyze.Analyzer;
import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import nl.basjes.parse.useragent.analyze.Matcher;
import nl.basjes.parse.useragent.analyze.MatcherAction;
import nl.basjes.parse.useragent.parse.UserAgentTreeFlattener;
import nl.basjes.parse.useragent.utils.VersionSplitter;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.collections4.map.LRUMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static nl.basjes.parse.useragent.UserAgent.AGENT_CLASS;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME;
import static nl.basjes.parse.useragent.UserAgent.AGENT_VERSION;
import static nl.basjes.parse.useragent.UserAgent.AGENT_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_BRAND;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_CLASS;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_NAME;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_CLASS;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_NAME;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_VERSION;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_CLASS;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_NAME;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_VERSION;
import static nl.basjes.parse.useragent.UserAgent.PRE_SORTED_FIELDS_LIST;
import static nl.basjes.parse.useragent.UserAgent.SYNTAX_ERROR;

public class UserAgentAnalyzer extends Analyzer {

    private static final int INFORM_ACTIONS_HASHMAP_SIZE = 1000000;
    private static final int PARSE_CACHE_SIZE = 10000;

    private static final Logger LOG = LoggerFactory.getLogger(UserAgentAnalyzer.class);
    private List<Matcher> allMatchers;
    private Map<String, Set<MatcherAction>> informMatcherActions;
    private Map<String, List<Map<String, List<String>>>> matcherConfigs = new HashMap<>(64);

    private boolean doingOnlyASingleTest = false;


    private List<Map<String, Map<String, String>>> testCases            = new ArrayList<>(2048);
    private Map<String, Map<String, String>> lookups                    = new HashMap<>(128);

    private UserAgentTreeFlattener flattener;

    private Yaml yaml;

    private LRUMap<String, UserAgent> parseCache = new LRUMap<>(PARSE_CACHE_SIZE);

    public UserAgentAnalyzer() {
        this("classpath*:UserAgents/**/*.yaml");
    }

    public UserAgentAnalyzer(String resourceString) {
        LOG.info("Loading from: \"{}\"", resourceString);
        informMatcherActions = new HashMap<>(INFORM_ACTIONS_HASHMAP_SIZE);
        allMatchers = new ArrayList<>();
        flattener = new UserAgentTreeFlattener(this);
        yaml = new Yaml();

        Resource[] resources;
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            resources = resolver.getResources(resourceString);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        doingOnlyASingleTest = false;
        int maxFilenameLength = 0;
        for (Resource resource : resources) {
            try {
                maxFilenameLength = Math.max(maxFilenameLength, resource.getFilename().length());
                loadResource(resource.getInputStream(), resource.getFilename());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        LOG.info("Loaded {} files", resources.length);

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
        if (matcherConfigs != null) {
            long fullStart = System.nanoTime();
            for (Map.Entry<String, List<Map<String, List<String>>>> matcherConfigSet: matcherConfigs.entrySet()){
                long start = System.nanoTime();
                int startSize = informMatcherActions.size();
//                LOG.info("Start @ {}", start);
                for (Map<String, List<String>> map : matcherConfigSet.getValue()) {
                    allMatchers.add(new Matcher(this, lookups, map));
                    totalNumberOfMatchers++;
                }
                long stop = System.nanoTime();
                int stopSize = informMatcherActions.size();
//                LOG.info("Stop  @ {}", stop);
                Formatter msg = new Formatter(Locale.ENGLISH);
                msg.format("Building %4d matchers from %-"+maxFilenameLength+"s took %5d msec resulted in %8d extra hashmap entries",
                    matcherConfigSet.getValue().size(),
                    matcherConfigSet.getKey(),
                    (stop-start)/1000000,
                    stopSize-startSize);
                LOG.info(msg.toString());
            }
            long fullStop = System.nanoTime();
//            LOG.info("Total time building all matchers: {} msec", (fullStop-fullStart)/1000000);

            Formatter msg = new Formatter(Locale.ENGLISH);
            msg.format("Building %4d matchers from %4d files took %5d msec resulted in %8d hashmap entries",
                totalNumberOfMatchers,
                matcherConfigs.size(),
                (fullStop-fullStart)/1000000,
                informMatcherActions.size());
            LOG.info(msg.toString());

        }
        LOG.info("Analyzer stats");
        LOG.info("Lookups      : {}", (lookups == null) ? 0 : lookups.size());
        LOG.info("Matchers     : {}", allMatchers.size());
        LOG.info("Hashmap size : {}", informMatcherActions.size());
        LOG.info("Testcases    : {}", testCases .size());
        LOG.info("All possible field names:");
        int count = 1;
        for (String fieldName : getAllPossibleFieldNamesSorted()) {
            LOG.info("- {}: {}", count++, fieldName);
        }
    }


    public Set<String> getAllPossibleFieldNames() {
        Set<String> results = new TreeSet<>();
        results.addAll(HARD_CODED_GENERATED_FIELDS);
        for (Matcher matcher: allMatchers) {
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
        for (String fieldName : fieldNames) {
            result.add(fieldName);
        }
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

    private void loadResource(InputStream yamlStream, String filename) {
        Object loadedYaml = yaml.load(yamlStream);
        if (loadedYaml == null) {
            return;
        }

        if (!(loadedYaml instanceof Map)) {
            throw new InvalidParserConfigurationException(
                "Yaml config  ("+filename+"): File must be a Map");
        }

        @SuppressWarnings({"unchecked"}) // Ignoring the possibly wrong generic here
        Object rawConfig = ((Map<String, Object>) loadedYaml).get("config");
        if (rawConfig == null) {
            throw new InvalidParserConfigurationException(
                "Yaml config ("+filename+"): Missing 'config' top level entry");
        }
        if (!(rawConfig instanceof List)) {
            throw new InvalidParserConfigurationException(
                "Yaml config ("+filename+"): Top level 'config' must be a Map");
        }

        @SuppressWarnings({"unchecked"}) // Ignoring the possibly wrong generic here
        List<Object> configList = (List<Object>)rawConfig;
        int entryCount = 0;
        for (Object configEntry: configList) {
            entryCount++;
            if (!(configEntry instanceof Map)) {
                throw new InvalidParserConfigurationException(
                    "Yaml config ("+filename+" ["+entryCount+"]): Entry must be a Map");
            }
            @SuppressWarnings({"unchecked"}) // Ignoring the possibly wrong generic here
            Map<String, Object> entry = (Map<String, Object>) configEntry;
            if (entry.size() != 1) {
                StringBuilder sb = new StringBuilder();
                for (String key: entry.keySet()) {
                    sb.append('"').append(key).append("\" ");
                }
                throw new InvalidParserConfigurationException(
                    "Yaml config ("+filename+" ["+entryCount+"]): Entry has more than one child: "+sb.toString());
            }

            Map.Entry<String, Object> onlyEntry = entry.entrySet().iterator().next();
            String key   = onlyEntry.getKey();
            Object value = onlyEntry.getValue();
            switch (key) {

                case "lookup":
                    if (!(value instanceof Map)) {
                        throw new InvalidParserConfigurationException(
                            "Yaml config ("+filename+" ["+entryCount+"]): Entry 'lookup' must be a Map");
                    }

                    @SuppressWarnings({"unchecked"}) // Ignoring the possibly wrong generic here
                    Map<String, Object> newLookup = (Map<String, Object>)value;
                    Object rawName = newLookup.get("name");
                    if (rawName == null) {
                        throw new InvalidParserConfigurationException(
                            "Yaml config ("+filename+" ["+entryCount+"]): Lookup does not have 'name'");
                    }
                    if (!(rawName instanceof String)) {
                        throw new InvalidParserConfigurationException(
                            "Yaml config ("+filename+" ["+entryCount+"]): Lookup 'name' must be a String");
                    }

                    Object rawMap = newLookup.get("map");
                    if (rawMap == null) {
                        throw new InvalidParserConfigurationException(
                            "Yaml config ("+filename+" ["+entryCount+"]): Lookup does not have 'map'");
                    }
                    if (!(rawMap instanceof Map)) {
                        throw new InvalidParserConfigurationException(
                            "Yaml config ("+filename+" ["+entryCount+"]): Lookup 'map' must be a Map");
                    }

                    @SuppressWarnings({"unchecked"}) // Ignoring the possibly wrong generic here
                    Map<String, String> map = (Map<String, String>)rawMap;
                    lookups.put((String)rawName, map);
                    break;

                case "matcher":
                    if (!(value instanceof Map)) {
                        throw new InvalidParserConfigurationException(
                            "Yaml config ("+filename+"): Entry 'matcher' must be a Map");
                    }
                    @SuppressWarnings({"unchecked"}) // Ignoring the possibly wrong generic here
                    Map<String, List<String>> matcherConfig = (Map<String, List<String>>) value;

                    List<Map<String, List<String>>> matcherConfigList = matcherConfigs.get(filename);
                    if (matcherConfigList == null) {
                        matcherConfigList = new ArrayList<>(32);
                        matcherConfigs.put(filename, matcherConfigList);
                    }
                    matcherConfigList.add(matcherConfig);
                    break;

                case "test":
                    if (!doingOnlyASingleTest) {
                        if (!(value instanceof Map)) {
                            throw new InvalidParserConfigurationException(
                                "Yaml config (" + filename + "): Entry 'testcase' must be a Map");
                        }
                        @SuppressWarnings({"unchecked"}) // Ignoring the possibly wrong generic here
                                Map<String, Map<String, String>> testCase = (Map<String, Map<String, String>>) value;
                        Map<String, String> metaData = testCase.get("metaData");
                        if (metaData == null) {
                            metaData = new HashMap<>();
                            testCase.put("metaData", metaData);
                        }
                        metaData.put("filename", filename);
                        metaData.put("fileentry", String.valueOf(entryCount));

                        @SuppressWarnings("unchecked")
                        List<String> options = (List<String>) testCase.get("options");
                        if (options != null) {
                            if (options.contains("only")) {
                                doingOnlyASingleTest = true;
                                testCases.clear();
                            }
                        }

                        testCases.add(testCase);
                    }
                    break;

                default:
                    throw new InvalidParserConfigurationException(
                        "Yaml config ("+filename+"): Found unexpected config entry: " + key + ", allowed are 'lookup, 'matcher' and 'test'");
            }
        }

    }

    public void informMeAbout(MatcherAction matcherAction, String keyPattern) {
        String hashKey = keyPattern.toLowerCase();
        Set<MatcherAction> analyzerSet = informMatcherActions.get(hashKey);
        if (analyzerSet == null) {
            analyzerSet = new HashSet<>();
            informMatcherActions.put(hashKey, analyzerSet);
        }
        analyzerSet.add(matcherAction);
    }

    private boolean verbose = false;

    public void setVerbose(boolean newVerbose) {
        this.verbose = newVerbose;
        flattener.setVerbose(newVerbose);
    }

    public UserAgent parse(String userAgentString) {
        UserAgent userAgent = new UserAgent(userAgentString);
//        userAgent.setDebug(verbose);
        return cachedParse(userAgent);
    }

    public UserAgent parse(UserAgent userAgent) {
        userAgent.reset();
        return cachedParse(userAgent);
    }

    private boolean cachingEnabled = true;

    public void disableCaching() {
        cachingEnabled = false;
    }

    private UserAgent cachedParse(UserAgent userAgent) {

        if (!cachingEnabled) {
            return nonCachedParse(userAgent);
        }

        String userAgentString = userAgent.getUserAgentString();
        UserAgent cachedValue = parseCache.get(userAgentString);
        if (cachedValue != null) {
            userAgent.clone(cachedValue);
        } else {
            cachedValue = new UserAgent(nonCachedParse(userAgent));
            parseCache.put(userAgentString, cachedValue);
        }
        // We have our answer.
        return userAgent;
    }

    private UserAgent nonCachedParse(UserAgent userAgent) {

        boolean setVerboseTemporarily = userAgent.isDebug();

        // Reset all Matchers
        for (Matcher matcher : allMatchers) {
            matcher.reset(setVerboseTemporarily);
        }

        flattener.parse(userAgent);

        // Fire all Analyzers
        for (Matcher matcher : allMatchers) {
            matcher.analyze(userAgent);
        }

        return hardCodedPostProcessing(userAgent);
    }


    private static final List<String> HARD_CODED_GENERATED_FIELDS = new ArrayList<>();
    static {
        HARD_CODED_GENERATED_FIELDS.add(SYNTAX_ERROR); // FIXME: Find better place for this one
        HARD_CODED_GENERATED_FIELDS.add(AGENT_VERSION_MAJOR);
        HARD_CODED_GENERATED_FIELDS.add(LAYOUT_ENGINE_VERSION_MAJOR);
        HARD_CODED_GENERATED_FIELDS.add("AgentNameVersion");
        HARD_CODED_GENERATED_FIELDS.add("AgentNameVersionMajor");
        HARD_CODED_GENERATED_FIELDS.add("LayoutEngineNameVersion");
        HARD_CODED_GENERATED_FIELDS.add("LayoutEngineNameVersionMajor");
        HARD_CODED_GENERATED_FIELDS.add("OperatingSystemNameVersion");
    }

    private UserAgent hardCodedPostProcessing(UserAgent userAgent){
        // If it is really really bad ... then it is a Hacker.
        if ("true".equals(userAgent.getValue(SYNTAX_ERROR))) {
//            userAgent.get(AGENT_CLASS).confidence == -1 &&
            if (userAgent.get(DEVICE_CLASS).confidence == -1 &&
                userAgent.get(OPERATING_SYSTEM_CLASS).confidence == -1 &&
                userAgent.get(LAYOUT_ENGINE_CLASS).confidence == -1)  {

                userAgent.set(DEVICE_CLASS, "Hacker", 10);
                userAgent.set(DEVICE_BRAND, "Hacker", 10);
                userAgent.set(DEVICE_NAME, "Hacker", 10);
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
        // TODO: Perhaps this should be more generic. Like a "Post processor"  (Generic: Create fields from fields)?
        addMajorVersionField(userAgent, AGENT_VERSION, AGENT_VERSION_MAJOR);
        addMajorVersionField(userAgent, LAYOUT_ENGINE_VERSION, LAYOUT_ENGINE_VERSION_MAJOR);

        concatFieldValuesNONDuplicated(userAgent, "AgentNameVersion",               AGENT_NAME,             AGENT_VERSION);
        concatFieldValuesNONDuplicated(userAgent, "AgentNameVersionMajor",          AGENT_NAME,             AGENT_VERSION_MAJOR);
        concatFieldValuesNONDuplicated(userAgent, "LayoutEngineNameVersion",        LAYOUT_ENGINE_NAME,     LAYOUT_ENGINE_VERSION);
        concatFieldValuesNONDuplicated(userAgent, "LayoutEngineNameVersionMajor",   LAYOUT_ENGINE_NAME,     LAYOUT_ENGINE_VERSION_MAJOR);
        concatFieldValuesNONDuplicated(userAgent, "OperatingSystemNameVersion",     OPERATING_SYSTEM_NAME,  OPERATING_SYSTEM_VERSION);

        return userAgent;
    }


    private void concatFieldValuesNONDuplicated(UserAgent userAgent, String targetName, String firstName, String secondName) {
        UserAgent.AgentField firstField = userAgent.get(firstName);
        UserAgent.AgentField secondField = userAgent.get(secondName);
        if (firstField.confidence < 0 || secondField.confidence < 0) {
            if (firstField.confidence >= 0) {
                userAgent.set(targetName, firstField.getValue(), firstField.confidence);
                return; // No usefull input
            }
            if (secondField.confidence >= 0) {
                userAgent.set(targetName, secondField.getValue(), secondField.confidence);
                return; // No usefull input
            }
            return; // No usefull input
        }

        String first = userAgent.getValue(firstName);
        String second = userAgent.getValue(secondName);
        if (firstField.getValue().equals(second)) {
            userAgent.set(targetName, first, firstField.confidence);
        } else {
            userAgent.set(targetName, first + " " + second, Math.max(firstField.confidence, secondField.confidence));
        }

    }

    private void addMajorVersionField(UserAgent userAgent, String versionName, String majorVersionName) {
        UserAgent.AgentField agentVersionMajor = userAgent.get(majorVersionName);
        if (agentVersionMajor == null || agentVersionMajor.confidence == -1) {
            UserAgent.AgentField agentVersion = userAgent.get(versionName);
            userAgent.set(
                majorVersionName,
                VersionSplitter.getSingleVersion(agentVersion.getValue(), 1),
                agentVersion.confidence);
        }
    }



    public void inform(String key, String value, ParseTree ctx) {
        inform(key, key, value, ctx);
        inform(key + "=\"" + value + '"', key, value, ctx);
    }

    private void inform(String match, String key, String value, ParseTree ctx) {
        Set<MatcherAction> relevantActions = informMatcherActions.get(match.toLowerCase());
        if (verbose) {
            if (relevantActions == null) {
                LOG.info("--- Have (0): {}", match);
            } else {
                LOG.info("+++ Have ({}): {}", relevantActions.size(), match);

                int count = 1;
                for (MatcherAction action: relevantActions) {
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

    // ===============================================================================================================

    /**
     * This function is used only for analyzing which patterns that could possibly be relevant
     * were actually relevant for the matcher actions
     */
    public List<MatcherAction.Match> getMatches() {
        List<MatcherAction.Match> allMatches = new ArrayList<>(128);
        for (Matcher matcher: allMatchers) {
            allMatches.addAll(matcher.getMatches());
        }
        return allMatches;
    }

    public List<MatcherAction.Match> getUsedMatches(UserAgent userAgent) {
        // Reset all Matchers
        for (Matcher matcher : allMatchers) {
            matcher.reset(false);
        }

        flattener.parse(userAgent);

        List<MatcherAction.Match> allMatches = new ArrayList<>(128);
        for (Matcher matcher: allMatchers) {
            allMatches.addAll(matcher.getUsedMatches());
        }
        return allMatches;
    }

    // ===============================================================================================================

    static class GetAllPathsAnalyzer extends Analyzer {
        List<String> values = new ArrayList<>(128);
        UserAgentTreeFlattener flattener;

        private UserAgent result;

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

    public static List<String> getAllPaths(String agent) {
        return new GetAllPathsAnalyzer(agent).getValues();
    }

    public static GetAllPathsAnalyzer getAllPathsAnalyzer(String agent) {
        return new GetAllPathsAnalyzer(agent);
    }

    // ===============================================================================================================

    class TestResult {
        String field;
        String expected;
        String actual;
        boolean pass;
        boolean warn;
        long confidence;
    }

    /**
     * Run all the test_cases available.
     *
     * @return true if all tests were successful.
     */
    public boolean runTests() {
        return runTests(false, true);
    }

    public boolean runTests(boolean showAll, boolean failOnUnexpected) {
        boolean allPass = true;
        if (testCases == null) {
            return allPass;
        }
        UserAgent agent = new UserAgent();

        List<TestResult> results = new ArrayList<>(32);

        String filenameHeader = "Name of the testfile";
        int filenameHeaderLength = filenameHeader.length();
        int maxFilenameLength = filenameHeaderLength;
        for (Map<String, Map<String, String>> test : testCases) {
            Map<String, String> metaData = test.get("metaData");
            String filename = metaData.get("filename");
            maxFilenameLength = Math.max(maxFilenameLength, filename.length());
        }

        maxFilenameLength++;
        LOG.info("+===========================================================================================");

        StringBuilder sb = new StringBuilder(1024);

        sb.append("| ").append(filenameHeader);
        for (int i = filenameHeaderLength; i < maxFilenameLength; i++) {
            sb.append(' ');
        }
        sb.append("|vv --> S=Syntax Error, A=Ambiguity during parse");

        LOG.info(sb.toString());
        LOG.info("+-------------------------------------------------------------------------------------------");


        for (Map<String, Map<String, String>> test : testCases) {

            Map<String, String> input = test.get("input");
            Map<String, String> expected = test.get("expected");

            @SuppressWarnings("unchecked")
            List<String> options = (List<String>) test.get("options");
            Map<String, String> metaData = test.get("metaData");
            String filename = metaData.get("filename");

            boolean init = false;

            if (options == null) {
                setVerbose(false);
                agent.setDebug(false);
            } else {
                boolean newVerbose = options.contains("verbose");
                setVerbose(newVerbose);
                agent.setDebug(newVerbose);
                init = options.contains("init");
            }
            if (expected == null || expected.size() == 0) {
                init = true;
            }

            String testName = input.get("name");
            String userAgentString = input.get("user_agent_string");

            if (testName == null) {
                testName = userAgentString;
            }

            sb.setLength(0);
            sb.append("| ").append(filename);
            for (int i = filename.length(); i < maxFilenameLength; i++) {
                sb.append(' ');
            }

            agent.setUserAgentString(userAgentString);
            agent = parse(agent);

            sb.append('|');
            if (agent.hasSyntaxError()) {
                sb.append('S');
            } else {
                sb.append(' ');
            }
            if (agent.hasAmbiguity()) {
                sb.append('A');
            } else {
                sb.append(' ');
            }

            sb.append("| ").append(testName);
            LOG.info(sb.toString());
            sb.setLength(0);

            boolean pass = true;
            results.clear();

            if (init) {
                sb.append(agent.toYamlTestCase());
                LOG.info(sb.toString());
//                return allPass;
            } else {
                if (expected == null) {
                    LOG.info("| - No expectations ... ");
                    continue;
                }
            }

            int maxNameLength = 6; // "Field".length()+1;
            int maxActualLength = 7; // "Actual".length()+1;
            int maxExpectedLength = 9; // "Expected".length()+1;

            if (expected != null) {
                List<String> fieldNames = agent.getAvailableFieldNamesSorted();
                for (String fieldName : fieldNames) {

                    TestResult result = new TestResult();
                    result.field = fieldName;
                    boolean expectedSomething;

                    // Expected value
                    String expectedValue = expected.get(fieldName);
                    if (expectedValue == null) {
                        expectedSomething = false;
                        result.expected = "<<absent>>";
                    } else {
                        expectedSomething = true;
                        result.expected = expectedValue;
                    }

                    // Actual value
                    UserAgent.AgentField agentField = agent.get(result.field);
                    if (agentField != null) {
                        result.actual = agentField.value;
                        result.confidence = agentField.confidence;
                    }
                    if (result.actual == null) {
                        result.actual = "<<<null>>>";
                    }

                    result.pass = result.actual.equals(result.expected);
                    if (!result.pass) {
                        result.warn=true;
                        if (expectedSomething) {
                            result.warn=false;
                            pass = false;
                            allPass = false;
                        } else {
                            if (failOnUnexpected) {
                                result.warn=false;
                                pass = false;
                                allPass = false;
                            }
                        }
                    }

                    results.add(result);

                    maxNameLength = Math.max(maxNameLength, result.field.length());
                    maxActualLength = Math.max(maxActualLength, result.actual.length());
                    maxExpectedLength = Math.max(maxExpectedLength, result.expected.length());
                }
            }

            if (!init && pass && !showAll) {
                continue;
            }

            if (!pass) {
                LOG.error("| TEST FAILED !");
            }

            if (agent.hasAmbiguity()) {
                LOG.info("| Parsing problem: Ambiguity");
            }
            if (agent.hasSyntaxError()) {
                LOG.info("| Parsing problem: Syntax Error");
            }

            if (init || !pass) {
                sb.setLength(0);
                sb.append("\n");
                sb.append("\n");
                sb.append("  - matcher:\n");
                sb.append("#      options:\n");
                sb.append("#        - 'verbose'\n");
                sb.append("      require:\n");
                for (String path : getAllPathsAnalyzer(userAgentString).getValues()) {
                    if (path.contains("=\"")) {
                        sb.append("#        - '").append(path).append("'\n");
                    }
                }
                sb.append("      extract:\n");
                sb.append("#        - 'DeviceClass           :   1:' # Hacker / Cloud / Server / Desktop / Tablet / Phone / Watch \n");
                sb.append("#        - 'DeviceBrand           :   1:' # (Google/AWS/Asure) / Samsung / Apple / ... \n");
                sb.append("#        - 'DeviceName            :   1:' # (Google/AWS/Asure) / Samsung Galaxy SII / ... \n");
                sb.append("#        - 'OperatingSystemClass  :   1:' # Cloud, Desktop, Mobile, Embedded \n");
                sb.append("#        - 'OperatingSystemName   :   1:' # ( Linux / Android / Windows ...) \n");
                sb.append("#        - 'OperatingSystemVersion:   1:' # 1.2 / 43 / ...\n");
                sb.append("#        - 'LayoutEngineClass     :   1:' # None / Hacker / Robot / Browser / ... \n");
                sb.append("#        - 'LayoutEngineName      :   1:' # ( GoogleBot / Bing / Yahoo / ...) / (Trident /Gecko / Webkit / ...)\n");
                sb.append("#        - 'LayoutEngineVersion   :   1:' # 7.0 / 1.2 / ... \n");
                sb.append("#        - 'AgentClass            :   1:' # Hacker / Robot / Browser / ... \n");
                sb.append("#        - 'AgentName             :   1:' # ( GoogleBot / Bing / Yahoo / ...) / ( Firefox / Chrome / ... ) \n");
                sb.append("#        - 'AgentVersion          :   1:' # 4.0 / 43.1.2.3 / ...\n");
                sb.append("\n");
                sb.append("\n");
                LOG.info(sb.toString());
            }

            sb.setLength(0);
            sb.append("+--------+-");
            for (int i = 0; i < maxNameLength; i++) {
                sb.append('-');
            }
            sb.append("-+-");
            for (int i = 0; i < maxActualLength; i++) {
                sb.append('-');
            }
            sb.append("-+------------+-");
            for (int i = 0; i < maxExpectedLength; i++) {
                sb.append('-');
            }
            sb.append("-+");

            String separator = sb.toString();
            LOG.info(separator);

            sb.setLength(0);
            sb.append("| Result | Field ");
            for (int i = 6; i < maxNameLength; i++) {
                sb.append(' ');
            }
            sb.append(" | Actual ");
            for (int i = 7; i < maxActualLength; i++) {
                sb.append(' ');
            }
            sb.append(" | Confidence | Expected ");
            for (int i = 9; i < maxExpectedLength; i++) {
                sb.append(' ');
            }
            sb.append(" |");

            LOG.info(sb.toString());

            LOG.info(separator);

            for (TestResult result : results) {
                sb.setLength(0);
                if (result.pass) {
                    sb.append("|        | ");
                } else {
                    if (result.warn) {
                        sb.append("| ~warn~ | ");
                    } else {
                        sb.append("| -FAIL- | ");
                    }
                }
                sb.append(result.field);
                for (int i = result.field.length(); i < maxNameLength; i++) {
                    sb.append(' ');
                }
                sb.append(" | ");
                sb.append(result.actual);

                for (int i = result.actual.length(); i < maxActualLength; i++) {
                    sb.append(' ');
                }
                sb.append(" | ");
                sb.append(String.format("%10d", result.confidence));
                sb.append(" | ");

                if (result.pass) {
                    for (int i = 0; i < maxExpectedLength; i++) {
                        sb.append(' ');
                    }
                    sb.append(" |");
                    LOG.info(sb.toString());
                } else {
                    sb.append(result.expected);
                    for (int i = result.expected.length(); i < maxExpectedLength; i++) {
                        sb.append(' ');
                    }
                    sb.append(" |");
                    if (result.warn) {
                        LOG.warn(sb.toString());
                    } else {
                        LOG.error(sb.toString());
                    }
                }
            }

            LOG.info(separator);
            LOG.info("");

            LOG.info("\n\ntests:\n"+agent.toYamlTestCase());
            if (!pass && !showAll) {
//                LOG.info("+===========================================================================================");
                return false;
            }
            if (init) {
                return allPass;
            }
        }

        LOG.info("+===========================================================================================");
        return allPass;
    }
}
