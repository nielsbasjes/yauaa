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
import java.util.TreeMap;
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

    private static final int INFORM_ACTIONS_HASHMAP_SIZE = 200000;
    private static final int PARSE_CACHE_SIZE = 10000;

    private static final Logger LOG = LoggerFactory.getLogger(UserAgentAnalyzer.class);
    private List<Matcher> allMatchers;
    private Map<String, Set<MatcherAction>> informMatcherActions;
    private final Map<String, List<Map<String, List<String>>>> matcherConfigs = new HashMap<>(64);

    private boolean doingOnlyASingleTest = false;


    protected final List<Map<String, Map<String, String>>> testCases          = new ArrayList<>(2048);
    private Map<String, Map<String, String>> lookups                    = new HashMap<>(128);

    private UserAgentTreeFlattener flattener;

    private Yaml yaml;

    private final LRUMap<String, UserAgent> parseCache = new LRUMap<>(PARSE_CACHE_SIZE);

    public UserAgentAnalyzer() {
        this("classpath*:UserAgents/**/*.yaml");
    }

    public UserAgentAnalyzer(String resourceString) {
        LOG.info("Loading from: \"{}\"", resourceString);
        informMatcherActions = new HashMap<>(INFORM_ACTIONS_HASHMAP_SIZE);
        allMatchers = new ArrayList<>();
        flattener = new UserAgentTreeFlattener(this);
        yaml = new Yaml();

        Map<String, Resource> resources = new TreeMap<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resourceArray = resolver.getResources(resourceString);
            for (Resource resource:resourceArray) {
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
                loadResource(resource.getInputStream(), filename);
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
        if (matcherConfigs != null) {
            long fullStart = System.nanoTime();
            for (Map.Entry<String, Resource> resourceEntry : resources.entrySet()) {
                Resource resource = resourceEntry.getValue();
                String configFilename= resource.getFilename();
                List<Map<String, List<String>>> matcherConfig = matcherConfigs.get(configFilename);
                if (matcherConfig== null) {
                    continue; // No matchers in this file (probably only lookups and/or tests)
                }

                long start = System.nanoTime();
                int startSize = informMatcherActions.size();
                for (Map<String, List<String>> map : matcherConfig) {
                    allMatchers.add(new Matcher(this, lookups, map));
                    totalNumberOfMatchers++;
                }
                long stop = System.nanoTime();
                int stopSize = informMatcherActions.size();

                Formatter msg = new Formatter(Locale.ENGLISH);
                msg.format("Building %4d matchers from %-"+maxFilenameLength+"s took %5d msec resulted in %8d extra hashmap entries",
                    matcherConfig.size(),
                    configFilename,
                    (stop-start)/1000000,
                    stopSize-startSize);
                LOG.info(msg.toString());
            }
            long fullStop = System.nanoTime();

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
//        LOG.info("All possible field names:");
//        int count = 1;
//        for (String fieldName : getAllPossibleFieldNames()) {
//            LOG.info("- {}: {}", count++, fieldName);
//        }
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
        Object loadedYaml;
        try {
            loadedYaml = yaml.load(yamlStream);
        } catch (Exception e) {
            LOG.error("Caught exception during parse of file {}", filename);
            throw e;
        }
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
                        Map<String, String> expected = testCase.get("expected");
                        if (options != null) {
                            if (options.contains("only")) {
                                doingOnlyASingleTest = true;
                                testCases.clear();
                            }
                        }
                        if (expected == null || expected.isEmpty()) {
                            doingOnlyASingleTest = true;
                            testCases.clear();
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

        userAgent = flattener.parse(userAgent);

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
        HARD_CODED_GENERATED_FIELDS.add("WebviewAppVersionMajor");
        HARD_CODED_GENERATED_FIELDS.add("WebviewAppNameVersionMajor");
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
        addMajorVersionField(userAgent, "WebviewAppVersion", "WebviewAppVersionMajor");

        concatFieldValuesNONDuplicated(userAgent, "AgentNameVersion",               AGENT_NAME,             AGENT_VERSION);
        concatFieldValuesNONDuplicated(userAgent, "AgentNameVersionMajor",          AGENT_NAME,             AGENT_VERSION_MAJOR);
        concatFieldValuesNONDuplicated(userAgent, "WebviewAppNameVersionMajor",     "WebviewAppName",       "WebviewAppVersionMajor");
        concatFieldValuesNONDuplicated(userAgent, "LayoutEngineNameVersion",        LAYOUT_ENGINE_NAME,     LAYOUT_ENGINE_VERSION);
        concatFieldValuesNONDuplicated(userAgent, "LayoutEngineNameVersionMajor",   LAYOUT_ENGINE_NAME,     LAYOUT_ENGINE_VERSION_MAJOR);
        concatFieldValuesNONDuplicated(userAgent, "OperatingSystemNameVersion",     OPERATING_SYSTEM_NAME,  OPERATING_SYSTEM_VERSION);

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
            firstConfidence = firstField.confidence;
        }
        if (secondField != null) {
            second = secondField.getValue();
            secondConfidence = secondField.confidence;
        }

        if (first == null && second == null) {
            return; // Nothing to do
        }

        if (second == null) {
            if (firstConfidence >= 0){
                userAgent.set(targetName, first, firstConfidence);
                return;
            }
            return; // Nothing to do
        } else {
            if (first == null) {
                if (secondConfidence >= 0) {
                    userAgent.set(targetName, second, secondConfidence);
                    return;
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
                userAgent.set(targetName, first + " " + second, Math.max(firstField.confidence, secondField.confidence));
            }
        }
    }

    private void addMajorVersionField(UserAgent userAgent, String versionName, String majorVersionName) {
        UserAgent.AgentField agentVersionMajor = userAgent.get(majorVersionName);
        if (agentVersionMajor == null || agentVersionMajor.confidence == -1) {
            UserAgent.AgentField agentVersion = userAgent.get(versionName);
            if (agentVersion != null) {
                userAgent.set(
                    majorVersionName,
                    VersionSplitter.getSingleVersion(agentVersion.getValue(), 1),
                    agentVersion.confidence);
            }
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
    @SuppressWarnings({"unused"})
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

}
