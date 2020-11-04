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

import nl.basjes.parse.useragent.analyze.Analyzer;
import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import nl.basjes.parse.useragent.analyze.Matcher;
import nl.basjes.parse.useragent.analyze.UselessMatcherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nl.basjes.parse.useragent.utils.YamlUtils.getExactlyOneNodeTuple;
import static nl.basjes.parse.useragent.utils.YamlUtils.getKeyAsString;
import static nl.basjes.parse.useragent.utils.YamlUtils.getStringValues;
import static nl.basjes.parse.useragent.utils.YamlUtils.getValueAsMappingNode;
import static nl.basjes.parse.useragent.utils.YamlUtils.getValueAsSequenceNode;
import static nl.basjes.parse.useragent.utils.YamlUtils.getValueAsString;
import static nl.basjes.parse.useragent.utils.YamlUtils.require;
import static nl.basjes.parse.useragent.utils.YamlUtils.requireNodeInstanceOf;
import static nl.basjes.parse.useragent.utils.YauaaVersion.assertSameVersion;

public class ConfigLoader implements Serializable {
    static final String DEFAULT_RESOURCES = "classpath*:UserAgents/**/*.yaml";

    private static final Logger LOG = LoggerFactory.getLogger(ConfigLoader.class);
    private boolean doingOnlyASingleTest;

    private boolean loadTests = true;

    // If we want ALL fields this is null. If we only want specific fields this is a list of names.
    protected Set<String> wantedFieldNames = null; // NOSONAR: Only accessed via Builder.

    private final ArrayList<Matcher> matchers = new ArrayList<>(5000);
    private final ArrayList<Map<String, Map<String, String>>> testCases = new ArrayList<>(2048);
    private final Map<String, Map<String, String>> lookups = new LinkedHashMap<>(128);
    private final Map<String, Set<String>> lookupSets = new LinkedHashMap<>(128);
//    private final Map<String, Set<String>> lookupSetMerge = new LinkedHashMap<>(128);
    private boolean showMatcherStats = true;

    public ArrayList<Matcher> getMatchers() {
        return matchers;
    }

    public ArrayList<Map<String, Map<String, String>>> getTestCases() {
        return testCases;
    }

    public Map<String, Map<String, String>> getLookups() {
        return lookups;
    }

    public Map<String, Set<String>> getLookupSets() {
        return lookupSets;
    }
//
//    public Map<String, Set<String>> getLookupSetMerge() {
//        return lookupSetMerge;
//    }

    public ConfigLoader() {
        doingOnlyASingleTest = false;
    }

    public void setShowMatcherStats(boolean newShowMatcherStats) {
        this.showMatcherStats = newShowMatcherStats;
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

    public void loadResources(String resourceString) {
        loadResources(resourceString, true, false);
    }

    private transient Map<String, List<MappingNode>> matcherConfigs = new HashMap<>();

    private Yaml createYaml() {
        final LoaderOptions yamlLoaderOptions = new LoaderOptions();
        yamlLoaderOptions.setMaxAliasesForCollections(100); // We use this many in the hacker/sql injection config.
        return new Yaml(yamlLoaderOptions);
    }

    public void loadResources(String resourceString, boolean showLoadMessages, boolean optionalResources) {
        long startFiles = System.nanoTime();

        Yaml yaml = createYaml();

        final boolean loadingDefaultResources = DEFAULT_RESOURCES.equals(resourceString);

        Map<String, Resource> resources = new TreeMap<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resourceArray = resolver.getResources(resourceString);
            if (!loadingDefaultResources && showLoadMessages) {
                LOG.info("Loading {} rule files using expression: {}", resourceArray.length, resourceString);
            }
            for (Resource resource : resourceArray) {
                if (!loadingDefaultResources && showLoadMessages) {
                    LOG.info("- Preparing {} ({} bytes)", resource.getFilename(), resource.contentLength());
                }
                resources.put(resource.getFilename(), resource);
            }
        } catch (IOException e) {
            if (optionalResources) {
                LOG.error("The specified (optional) resource string is invalid: {}", resourceString);
                return;
            } else {
                throw new InvalidParserConfigurationException("Error reading resources: " + e.getMessage(), e);
            }
        }

        int maxFilenameLength = 0;

        // When using a framework like Quarkus loading resources can fail in mysterious ways.
        // Just trying to open a stream for one of the resources is enough to see if we can continue.
        if (!resources.isEmpty()) {
            Resource resource = resources.entrySet().iterator().next().getValue();
            try (InputStream ignored = resource.getInputStream()) {
                // Just seeing if opening this stream triggers an error.
                // Having a useless statement that references the 'ignored' to avoid checkstyle and compilation warnings.
                LOG.debug("Opening the resource worked. {}", ignored);
            } catch (IOException e) {
                LOG.error("Cannot load the resources (usually classloading problem).");
                LOG.error("- Resource   : {}", resource);
                LOG.error("- Filename   : {}", resource.getFilename());
                LOG.error("- Description: {}", resource.getDescription());
                if (loadingDefaultResources) {
                    LOG.warn("Falling back to the built in list of resources");
                    resources.clear();
                } else {
                    LOG.error("FATAL: Unable to load the specified resources for {}", resourceString);
                    throw new InvalidParserConfigurationException("Error reading resources (" + resourceString + "): " + e.getMessage(), e);
                }
            }
        }

        if (resources.isEmpty()) {
            if (optionalResources) {
                LOG.warn("NO optional resources were loaded from expression: {}", resourceString);
            } else {
                LOG.error("NO config files were found matching this expression: {}", resourceString);

                if (loadingDefaultResources) {
                    LOG.warn("Unable to load the default resources, usually caused by classloader problems.");
                    LOG.warn("Retrying with built in list.");
                    PackagedRules.getRuleFileNames().forEach(s -> loadResources(s, false, false));
                } else {
                    LOG.warn("If you are using wildcards in your expression then try explicitly naming all yamls files explicitly.");
                    throw new InvalidParserConfigurationException("There were no resources found for the expression: " + resourceString);
                }
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
                    loadYaml(yaml, resource.getInputStream(), filename);
                }
            } catch (IOException e) {
                throw new InvalidParserConfigurationException("Error reading resources: " + e.getMessage(), e);
            }
        }

        long stopFiles = System.nanoTime();
        try(Formatter msg = new Formatter(Locale.ENGLISH)) {
            msg.format("- Loaded %2d files in %4d msec using expression: %s",
                resources.size(),
                (stopFiles - startFiles) / 1000000,
                resourceString);
            LOG.info(msg.toString());
        }

        if (resources.isEmpty()) {
            throw new InvalidParserConfigurationException("No matchers were loaded at all.");
        }

    }

    protected synchronized void finalizeLoadingRules(Analyzer analyzer) {

        if (matcherConfigs.isEmpty()) {
            throw new InvalidParserConfigurationException("No matchers were loaded at all.");
        }

        if (!lookups.isEmpty()) {
            // All compares are done in a case insensitive way. So we lowercase ALL keys of the lookups beforehand.
            Map<String, Map<String, String>> cleanedLookups = new LinkedHashMap<>(lookups.size());
            for (Map.Entry<String, Map<String, String>> lookupsEntry : lookups.entrySet()) {
                Map<String, String> cleanedLookup = new LinkedHashMap<>(lookupsEntry.getValue().size());
                for (Map.Entry<String, String> entry : lookupsEntry.getValue().entrySet()) {
                    cleanedLookup.put(entry.getKey().toLowerCase(), entry.getValue());
                }
                cleanedLookups.put(lookupsEntry.getKey(), cleanedLookup);
            }
            lookups.clear();
            lookups.putAll(cleanedLookups);
        }

//        if (!lookupSetMerge.isEmpty()) {
//            lookupSetMerge.forEach((set, allExtraToLoad) -> {
//                Set<String> theSet = lookupSets.get(set);
//                if (theSet != null) {
//                    allExtraToLoad.forEach(extraToLoad -> {
//                        Map<String, String> extralookup = lookups.get(extraToLoad);
//                        if (extralookup != null) {
//                            theSet.addAll(extralookup.keySet());
//                        }
//                        Set<String> extralookupSet = lookupSets.get(extraToLoad);
//                        if (extralookupSet != null) {
//                            theSet.addAll(extralookupSet);
//                        }
//                    });
//                }
//            });
//        }

        matchers.clear();
        for (Map.Entry<String, List<MappingNode>> matcherConfigEntry : matcherConfigs.entrySet()) {
            int skippedMatchers = 0;
            String configFilename = matcherConfigEntry.getKey();
            List<MappingNode> matcherConfig = matcherConfigEntry.getValue();
            if (matcherConfig == null) {
                continue; // No matchers in this file (probably only lookups and/or tests)
            }

            long start = System.nanoTime();
            int startSkipped = skippedMatchers;
            for (MappingNode map : matcherConfig) {
                try {
                    matchers.add(new Matcher(analyzer, wantedFieldNames, map, configFilename));
                } catch (UselessMatcherException ume) {
                    skippedMatchers++;
                }
            }
            long stop = System.nanoTime();
            int stopSkipped = skippedMatchers;

            if (showMatcherStats) {
                try (Formatter msg = new Formatter(Locale.ENGLISH)) {
                    String format = "Loading %4d (dropped %4d) matchers from " +
                        "%-20s took %5d msec";
                    msg.format(format,
                        matcherConfig.size() - (stopSkipped - startSkipped),
                        stopSkipped - startSkipped,
                        configFilename,
                        (stop - start) / 1000000);
                    LOG.info(msg.toString());
                }
            }
        }

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

    void loadYaml(String yamlString, String filename) {
        loadYaml(createYaml(), new ByteArrayInputStream(yamlString.getBytes(UTF_8)), filename);
    }

    private synchronized void loadYaml(Yaml yaml, InputStream yamlStream, String filename) {
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
        Set<String> lookupSet = new LinkedHashSet<>();

        Set<String> merge = new LinkedHashSet<>();

        for (NodeTuple tuple : entry.getValue()) {
            switch (getKeyAsString(tuple, filename)) {
                case "name":
                    name = getValueAsString(tuple, filename);
                    break;
                case "merge":
                    merge.addAll(getStringValues(getValueAsSequenceNode(tuple, filename), filename));
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

        if (!merge.isEmpty()) {
            lookupSets.put(name, merge);
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
            Map<String, String> expected = new LinkedHashMap<>();
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

            Map<String, Map<String, String>> testCase = new LinkedHashMap<>();

            testCase.put("input", input);
            if (!expected.isEmpty()) {
                testCase.put("expected", expected);
            }
            if (options != null) {
                Map<String, String> optionsMap = new LinkedHashMap<>(options.size());
                for (String option: options) {
                    optionsMap.put(option, option);
                }
                testCase.put("options", optionsMap);
            }
            testCase.put("metaData", metaData);
            testCases.add(testCase);
        }

    }

}
