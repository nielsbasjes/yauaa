/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2021 Niels Basjes
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

package nl.basjes.parse.useragent.config;

import nl.basjes.parse.useragent.PackagedRules;
import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import nl.basjes.parse.useragent.config.MatcherConfig.ConfigLine;
import nl.basjes.parse.useragent.utils.YamlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
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
import static nl.basjes.parse.useragent.config.MatcherConfig.ConfigLine.Type.EXTRACT;
import static nl.basjes.parse.useragent.config.MatcherConfig.ConfigLine.Type.FAIL_IF_FOUND;
import static nl.basjes.parse.useragent.config.MatcherConfig.ConfigLine.Type.REQUIRE;
import static nl.basjes.parse.useragent.config.MatcherConfig.ConfigLine.Type.VARIABLE;
import static nl.basjes.parse.useragent.utils.YamlUtils.getExactlyOneNodeTuple;
import static nl.basjes.parse.useragent.utils.YamlUtils.getKeyAsString;
import static nl.basjes.parse.useragent.utils.YamlUtils.getStringValues;
import static nl.basjes.parse.useragent.utils.YamlUtils.getValueAsMappingNode;
import static nl.basjes.parse.useragent.utils.YamlUtils.getValueAsSequenceNode;
import static nl.basjes.parse.useragent.utils.YamlUtils.getValueAsString;
import static nl.basjes.parse.useragent.utils.YamlUtils.require;
import static nl.basjes.parse.useragent.utils.YamlUtils.requireNodeInstanceOf;
import static nl.basjes.parse.useragent.utils.YauaaVersion.assertSameVersion;

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

public class ConfigLoader {

    // ------------------------------------------
    private static final Logger LOG = LogManager.getLogger(ConfigLoader.class);

    public static final String DEFAULT_RESOURCES = "classpath*:UserAgents/**/*.yaml";
    private boolean doingOnlyASingleTest = false;

    private final List<String>          mandatoryResources;
    private final List<String>          optionalResources;
    private final AnalyzerConfig.AnalyzerConfigBuilder analyzerConfig;

    // Map from filename to yaml config string.
    private final Map<String, String>          yamlRules;

    private boolean keepTests = true;

    final boolean showLoading;

    // ------------------------------------------

    public ConfigLoader(boolean showLoading) {
        this.showLoading = showLoading;
        mandatoryResources = new ArrayList<>();
        optionalResources  = new ArrayList<>();
        yamlRules          = new LinkedHashMap<>();
        analyzerConfig = AnalyzerConfig.newBuilder();
    }

    public ConfigLoader addResource(List<String> resourceNames, boolean optionalResource) {
        resourceNames.forEach(resource -> this.addResource(resource, optionalResource));
        return this;
    }

    public ConfigLoader addResource(String resourceName, boolean optionalResource){
        if (resourceName== null) {
            throw new InvalidParserConfigurationException("The provided resource name was null.");
        }
        if (resourceName.trim().isEmpty()) {
            throw new InvalidParserConfigurationException("The provided resource name was empty.");
        }
        if (optionalResource) {
            optionalResources.add(resourceName);
        } else {
            mandatoryResources.add(resourceName);
        }
        return this;
    }

    public ConfigLoader addYaml(String configString, String filename) {
        yamlRules.put(filename, configString);
        return this;
    }

    public ConfigLoader keepTests(){
        this.keepTests = true;
        return this;
    }

    public ConfigLoader dropTests(){
        this.keepTests = false;
        return this;
    }

    // ------------------------------------------

    public AnalyzerConfig load(){
        mandatoryResources.forEach(resourceString -> loadResources(resourceString, showLoading, false));
        optionalResources.forEach(resourceString -> loadResources(resourceString, showLoading, true));

        Yaml yaml = createYaml();
        yamlRules.forEach(
            (filename, yamlString) -> loadYaml(yaml, new ByteArrayInputStream(yamlString.getBytes(UTF_8)), filename)
        );

        return analyzerConfig.build();
    }

    // ------------------------------------------

    public void loadResources(String resourceString, boolean showLoadMessages, boolean areOptional) {
        long startFiles = System.nanoTime();
        final boolean loadingDefaultResources = DEFAULT_RESOURCES.equals(resourceString);
        Map<String, Resource> resources = findAllResources(resourceString, showLoadMessages, areOptional, loadingDefaultResources);

        doingOnlyASingleTest = false;
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
            if (areOptional) {
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

        Set<String> alreadyLoadedResourceBasenames = yamlRules
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

                    String yamlString = new BufferedReader(
                        new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"));

                    yamlRules.put(filename, yamlString);
                }
            } catch (IOException e) {
                throw new InvalidParserConfigurationException("Error reading resources: " + e.getMessage(), e);
            }
        }

        long stopFiles = System.nanoTime();
        try(Formatter msg = new Formatter(Locale.ENGLISH)) {
            msg.format("- Loaded %2d files in %4d ms using expression: %s",
                resources.size(),
                (stopFiles - startFiles) / 1000000,
                resourceString);
            LOG.info("{}", msg);
        }

        if (resources.isEmpty()) {
            throw new InvalidParserConfigurationException("No matchers were loaded at all.");
        }

    }

    private Map<String, Resource> findAllResources(String resourceString, boolean showLoadMessages, boolean areOptional, boolean loadingDefaultResources) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Map<String, Resource> resources = new TreeMap<>();
        try {
            Resource[] resourceArray = resolver.getResources(resourceString);
            if (!loadingDefaultResources && showLoadMessages) {
                LOG.info("Loading {} rule files using expression: {}", resourceArray.length, resourceString);
            }
            for (Resource resource : resourceArray) {
                if (!keepTests && isTestRulesOnlyFile(resource.getFilename())) {
                    if (showLoadMessages) {
                        LOG.info("- Skipping tests only file {} ({} bytes)", resource.getFilename(), resource.contentLength());
                    }
                    continue;
                }
                if (!loadingDefaultResources && showLoadMessages) {
                    LOG.info("- Preparing {} ({} bytes)", resource.getFilename(), resource.contentLength());
                }
                resources.put(resource.getFilename(), resource);
            }
        } catch (IOException e) {
            if (areOptional) {
                LOG.error("The specified (optional) resource string is invalid: {}", resourceString);
                return Collections.emptyMap();
            } else {
                throw new InvalidParserConfigurationException("Error reading resources: " + e.getMessage(), e);
            }
        }
        return resources;
    }

    public static boolean isTestRulesOnlyFile(String filename) {
        if (filename == null) {
            return false;
        }
        return (filename.contains("-tests") || filename.contains("-Tests"));
    }

    private Yaml createYaml() {
        final LoaderOptions yamlLoaderOptions = new LoaderOptions();
        yamlLoaderOptions.setMaxAliasesForCollections(200); // We use this many in the hacker/sql injection config.
        return new Yaml(yamlLoaderOptions);
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
                    if (keepTests) {
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
        Map<String, String> map = new HashMap<>();

        Set<String> merge = new LinkedHashSet<>();

        for (NodeTuple tuple : entry.getValue()) {
            switch (getKeyAsString(tuple, filename)) {
                case "name":
                    name = getValueAsString(tuple, filename);
                    break;
                case "merge":
                    merge.addAll(getStringValues(getValueAsSequenceNode(tuple, filename), filename));
                    break;
                case "map":
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

        require(name != null && (!map.isEmpty() || !merge.isEmpty()), entry, filename, "Invalid lookup specified");

        if (!merge.isEmpty()) {
            analyzerConfig.putLookupMerges(name, merge);
        }

        analyzerConfig.putLookup(name, map);
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
                        lookupSet.add(value.toLowerCase(Locale.ROOT));
                    }
                    break;
                default:
                    break;
            }
        }

        require(name != null && (!lookupSet.isEmpty() || !merge.isEmpty()), entry, filename, "Invalid lookup specified");

        if (!merge.isEmpty()) {
            analyzerConfig.putLookupSetsMerges(name, merge);
        }

        analyzerConfig.putLookupSet(name, lookupSet);
    }

    private void loadYamlMatcher(MappingNode entry, String filename) {
        int matcherSourceLineNumber = entry.getStartMark().getLine();
        String matcherSourceLocation = filename + ':' + matcherSourceLineNumber;

        // List of 'attribute', 'confidence', 'expression'
        List<ConfigLine> configLines = new ArrayList<>(16);
        List<String> options = null;

        for (NodeTuple nodeTuple: entry.getValue()) {
            String name = getKeyAsString(nodeTuple, matcherSourceLocation);
            switch (name) {
                case "options":
                    options = YamlUtils.getStringValues(nodeTuple.getValueNode(), matcherSourceLocation);
                    break;
                case "variable":
                    for (String variableConfig : YamlUtils.getStringValues(nodeTuple.getValueNode(), matcherSourceLocation)) {
                        String[] configParts = variableConfig.split(":", 2);

                        if (configParts.length != 2) {
                            throw new InvalidParserConfigurationException("Invalid variable config line: " + variableConfig);
                        }
                        String variableName = configParts[0].trim();
                        String config = configParts[1].trim();

                        configLines.add(new ConfigLine(VARIABLE, variableName, null, config));
                    }
                    break;
                case "require":
                    for (String requireConfig : YamlUtils.getStringValues(nodeTuple.getValueNode(), matcherSourceLocation)) {
                        requireConfig = requireConfig.trim();
                        if (requireConfig.startsWith("IsNull[")) {
                            // FIXME: Nasty String manipulation code: Cleanup
                            String failIfFoundConfig = requireConfig.replaceAll("^IsNull\\[", "").replaceAll("]$", "");
                            configLines.add(new ConfigLine(FAIL_IF_FOUND, null, null, failIfFoundConfig));
                        } else {
                            configLines.add(new ConfigLine(REQUIRE, null, null, requireConfig));
                        }
                    }
                    break;
                case "extract":
                    for (String extractConfig : YamlUtils.getStringValues(nodeTuple.getValueNode(), matcherSourceLocation)) {
                        String[] configParts = extractConfig.split(":", 3);

                        if (configParts.length != 3) {
                            throw new InvalidParserConfigurationException("Invalid extract config line: " + extractConfig);
                        }
                        String attribute = configParts[0].trim();
                        Long confidence = Long.parseLong(configParts[1].trim());
                        String config = configParts[2].trim();
                        configLines.add(new ConfigLine(EXTRACT, attribute, confidence, config));
                    }
                    break;
                default:
                    // Ignore
            }
        }

        analyzerConfig.addMatcherConfigs(matcherSourceLocation, new MatcherConfig(filename, matcherSourceLineNumber, options, configLines));
    }

    private void loadYamlTestcase(MappingNode entry, String filename) {
        if (!doingOnlyASingleTest) {
            String input = null;
            String testName = null;
            List<String> options = null;
            Map<String, String> expected = new LinkedHashMap<>();
            for (NodeTuple tuple : entry.getValue()) {
                String name = getKeyAsString(tuple, filename);
                switch (name) {
                    case "options":
                        options = getStringValues(tuple.getValueNode(), filename);
                        if (options.contains("only")) {
                            doingOnlyASingleTest = true;
                            analyzerConfig.clearAllTestCases();
                        }
                        break;
                    case "input":
                        for (NodeTuple inputTuple : getValueAsMappingNode(tuple, filename).getValue()) {
                            String inputName = getKeyAsString(inputTuple, filename);
                            if ("user_agent_string".equals(inputName)) {
                                input = getValueAsString(inputTuple, filename);
                            }
                            if ("name".equals(inputName)) {
                                testName = getValueAsString(inputTuple, filename);
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
                analyzerConfig.clearAllTestCases();
            }

            TestCase testCase = new TestCase(input, testName);

            if (!expected.isEmpty()) {
                expected.forEach(testCase::expect);
            }
            if (options != null) {
                for (String option: options) {
                    testCase.addOption(option);
                }
            }
            testCase.addMetadata("filename", filename);
            testCase.addMetadata("fileline", String.valueOf(entry.getStartMark().getLine()));

            analyzerConfig.addTestCase(testCase);
        }

    }

}
