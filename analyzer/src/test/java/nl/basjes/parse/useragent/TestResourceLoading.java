/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2023 Niels Basjes
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

import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import nl.basjes.parse.useragent.config.AnalyzerConfig;
import nl.basjes.parse.useragent.config.ConfigLoader;
import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester;
import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester.UserAgentAnalyzerTesterBuilder;
import nl.basjes.parse.useragent.utils.springframework.core.io.Resource;
import nl.basjes.parse.useragent.utils.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static nl.basjes.parse.useragent.utils.YamlUtils.getExactlyOneNodeTuple;
import static nl.basjes.parse.useragent.utils.YamlUtils.getKeyAsString;
import static nl.basjes.parse.useragent.utils.YamlUtils.getValueAsSequenceNode;
import static nl.basjes.parse.useragent.utils.YamlUtils.requireNodeInstanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class TestResourceLoading {

    private static final Logger LOG = LogManager.getLogger(TestResourceLoading.class);

    @Test
    void checkEmptyAndNormalAndOptionalMissingFile() {
        UserAgentAnalyzerTester uaa = UserAgentAnalyzerTester
            .newBuilder()
            .dropDefaultResources()
            .addResources("classpath*:AllSteps.yaml")
            .addResources("classpath*:BadDefinitions/EmptyFile.yaml")
            .addOptionalResources("classpath*:BadDefinitions/ThisOneDoesNotExist---Really.yaml")
            .build();
        assertTrue(uaa.runTests(false, false));
    }

    @Test
    void checkEmptyAndNormalAndMandatoryMissingFile() {
        assertThrows(InvalidParserConfigurationException.class, () -> {
            UserAgentAnalyzerTester uaa = UserAgentAnalyzerTester
                .newBuilder()
                .dropDefaultResources()
                .addResources("classpath*:AllSteps.yaml")
                .addResources("classpath*:BadDefinitions/EmptyFile.yaml")
                .addResources("classpath*:BadDefinitions/ThisOneDoesNotExist---Really.yaml") // Should cause a failure
                .build();
            assertTrue(uaa.runTests(false, false));
            }
        );
    }

    @Test
    void checkIfLoadingAllFilesSeparatelyWorks() {
        UserAgentAnalyzerTesterBuilder uaaB = UserAgentAnalyzerTester
            .newBuilder()
            .hideMatcherLoadStats()
            .dropDefaultResources();

        PackagedRules.getRuleFileNames().forEach(uaaB::addResources);

        UserAgentAnalyzerTester uaa = uaaB
            .withCache(10000)
            .build();

        assertTrue(uaa.runTests(false, false));
    }

    @Test
    void checkIfLoadingAllFilesSeparatelyAsOptionalWorks() {
        UserAgentAnalyzerTesterBuilder uaaB = UserAgentAnalyzerTester
            .newBuilder()
            .hideMatcherLoadStats()
            .dropDefaultResources();

        PackagedRules.getRuleFileNames().forEach(uaaB::addOptionalResources);

        UserAgentAnalyzerTester uaa = uaaB
            .withCache(10000)
            .build();

        assertTrue(uaa.runTests(false, false));
    }

    @Test
    void checkAllFieldsCallsAfterLoadingAdditionalResourceDuringBuildUnsorted() {
        UserAgentAnalyzerTester uaa = UserAgentAnalyzerTester
            .newBuilder()
            .dropDefaultResources()
            .addResources("classpath*:AllSteps.yaml")
            .addResources("CompanyInternalUserAgents.yaml")
            .build();

        Set<String> fieldSet = uaa.getAllPossibleFieldNames();

        List<String> extraFields = new ArrayList<>();
        Collections.addAll(extraFields, "ApplicationName", "ApplicationVersion", "ApplicationInstance", "ApplicationGitCommit", "ServerName");

        assertTrue(fieldSet.containsAll(extraFields));
    }

    @Test
    void checkAllFieldsCallsAfterLoadingAdditionalResourceUnsorted() {
        UserAgentAnalyzerTester uaa = UserAgentAnalyzerTester
            .newBuilder()
            .dropDefaultResources()
            .addResources("classpath*:AllSteps.yaml")
            .build();

//        uaa.finalizeLoadingRules();
        Set<String> fieldSet1 = uaa.getAllPossibleFieldNames();

        uaa.loadResources("CompanyInternalUserAgents.yaml");

//        uaa.finalizeLoadingRules();
        Set<String> fieldSet2 = uaa.getAllPossibleFieldNames();

        List<String> extraFields = new ArrayList<>();
        Collections.addAll(extraFields, "ApplicationName", "ApplicationVersion", "ApplicationInstance", "ApplicationGitCommit", "ServerName");

        assertFalse(fieldSet1.containsAll(extraFields));
        assertTrue(fieldSet2.containsAll(extraFields));
    }

    @Test
    void checkAllFieldsCallsAfterLoadingAdditionalResourceSorted() {
        UserAgentAnalyzerTester uaa = UserAgentAnalyzerTester
            .newBuilder()
            .dropDefaultResources()
            .addResources("classpath*:AllSteps.yaml")
            .build();

//        uaa.getMatchMaker().finalizeLoadingRules();
        List<String> fieldList1 = uaa.getAllPossibleFieldNamesSorted();

        uaa.loadResources("CompanyInternalUserAgents.yaml");

//        uaa.getMatchMaker().finalizeLoadingRules();
        List<String> fieldList2 = uaa.getAllPossibleFieldNamesSorted();

        List<String> extraFields = new ArrayList<>();
        Collections.addAll(extraFields, "ApplicationName", "ApplicationVersion", "ApplicationInstance", "ApplicationGitCommit", "ServerName");

        assertFalse(fieldList1.containsAll(extraFields));
        assertTrue(fieldList2.containsAll(extraFields));
    }

    @Test
    void testAllResourceFilesHaveTheProperName() throws IOException {
        for (String ruleFileName: PackagedRules.getRuleFileNames()) {
            final LoaderOptions yamlLoaderOptions = new LoaderOptions();
            yamlLoaderOptions.setMaxAliasesForCollections(200); // We use this many in the hacker/sql injection config.
            Yaml yaml = new Yaml(yamlLoaderOptions);

            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resourceArray = resolver.getResources(ruleFileName);
            assertEquals(1, resourceArray.length);

            long nrOfTestCases = 0;
            long nrOfMatchers = 0;
            long nrOfLookups = 0;
            long nrOfLookupSets = 0;

            Resource resource = resourceArray[0];

            Node loadedYaml = yaml.compose(new UnicodeReader(resource.getInputStream()));
            MappingNode rootNode = (MappingNode) loadedYaml;

            NodeTuple configNodeTuple = null;
            for (NodeTuple tuple : rootNode.getValue()) {
                String name = getKeyAsString(tuple, ruleFileName);
                if ("config".equals(name)) {
                    configNodeTuple = tuple;
                    break;
                }
            }

            assertNotNull(configNodeTuple);

            SequenceNode configNode = getValueAsSequenceNode(configNodeTuple, ruleFileName);
            List<Node> configList = configNode.getValue();

            for (Node configEntry : configList) {
                requireNodeInstanceOf(MappingNode.class, configEntry, ruleFileName, "The entry MUST be a mapping");
                NodeTuple entry = getExactlyOneNodeTuple((MappingNode) configEntry, ruleFileName);
                String entryType = getKeyAsString(entry, ruleFileName);
                switch (entryType) {
                    case "lookup":
                        nrOfLookups++;
                        break;
                    case "set":
                        nrOfLookupSets++;
                        break;
                    case "matcher":
                        nrOfMatchers++;
                        break;
                    case "test":
                        nrOfTestCases++;
                        break;
                    default:
                        break;
                }
            }

            if (ConfigLoader.isTestRulesOnlyFile(ruleFileName)) {
                assertTrue(nrOfTestCases > 0,
                    "A tests file MUST have tests: " + ruleFileName);
                assertEquals(0, nrOfMatchers,
                    "A tests file may only have tests (this one has " + nrOfMatchers + " Matchers): " + ruleFileName);
                assertEquals(0, nrOfLookups,
                    "A tests file may only have tests (this one has " + nrOfLookups + " Lookups): " + ruleFileName);
                assertEquals(0, nrOfLookupSets,
                    "A tests file may only have tests (this one has " + nrOfLookupSets + " LookupSets): " + ruleFileName);
            } else {
                assertTrue(nrOfMatchers >= 0 || nrOfLookups >= 0 || nrOfLookupSets >= 0,
                    "A file must have something (we did not find any matchers and/or lookups):" + ruleFileName);

                if (nrOfTestCases  >= 0 &&
                    nrOfMatchers   == 0 &&
                    nrOfLookups    == 0 &&
                    nrOfLookupSets == 0) {
                    fail("A file with ONLY tests must be called '-tests':" + ruleFileName);
                }
            }
        }
    }

    @Test
    void checkConfigLoaderEdgeCases(){
        ConfigLoader loader = new ConfigLoader(false);

        String message = assertThrows(InvalidParserConfigurationException.class, () ->
            loader.addResource((String)null, false)).getMessage();
        assertTrue(message.contains("resource name was null"));

        message = assertThrows(InvalidParserConfigurationException.class, () ->
            loader.addResource("", false)).getMessage();
        assertTrue(message.contains("resource name was empty"));

        assertFalse(ConfigLoader.isTestRulesOnlyFile(null));
    }

    private static final String YAML_RULE = "config:\n- matcher:\n    extract:\n      - 'FirstProductName     : 1 :agent.(1)product.(1)name'\n";

    @Test
    void checkConfigLoaderToString(){
        AnalyzerConfig analyzerConfig = new ConfigLoader(false)
            .addYaml(YAML_RULE, "foo")
            .keepTests()
            .dropTests()
            .load();

        String asString = analyzerConfig.toString();
        assertTrue(asString.contains("matcherSourceFilename='foo'"));
        assertTrue(asString.contains("attribute='FirstProductName'"));
        assertTrue(asString.contains("expression='agent.(1)product.(1)name'"));
    }

    @Test
    void checkConfigLoaderKeepTestsMandatoryTestsOnlyResource(){
        AnalyzerConfig configWithTests = new ConfigLoader(true)
            .addResource("classpath*:UserAgents/Additional-Tests.yaml", false)
            .addResource("classpath*:UserAgents/Android.yaml", false)
            .keepTests()
            .load();

        // A mandatory (non-optional) file with only tests in combination with
        // dropTests may not lead to an error.
        AnalyzerConfig configWithoutTests = new ConfigLoader(true)
            .addResource("classpath*:UserAgents/Additional-Tests.yaml", false)
            .addResource("classpath*:UserAgents/Android.yaml", false)
            .dropTests()
            .load();

        LOG.info("Config WITH    tests: {} MatcherConfigs, {} Testcases",
            configWithTests.getMatcherConfigs().size(),
            configWithTests.getTestCases().size());

        LOG.info("Config WITHOUT tests: {} MatcherConfigs, {} Testcases",
            configWithoutTests.getMatcherConfigs().size(),
            configWithoutTests.getTestCases().size());

        assertEquals(
            configWithTests.getMatcherConfigs().size(),
            configWithoutTests.getMatcherConfigs().size(),
            "The same number of MatcherConfigs must be loaded");

        assertTrue(configWithTests.getTestCases().size() > 10,
            "With tests MUST have testcases");

        assertEquals(0, configWithoutTests.getTestCases().size(),
            "Without tests MUST have 0 testcases");
    }

    @Test
    void testLoadingSeparateYamlRule(){
        UserAgentAnalyzer userAgentAnalyzer = UserAgentAnalyzer.newBuilder()
            .withFields(Arrays.asList("DeviceClass", "DeviceBrand", "DeviceName", "AgentNameVersionMajor", "FirstProductName"))
            .addYamlRule(YAML_RULE)
            .build();

        UserAgent userAgent = userAgentAnalyzer.parse(
            "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/53.0.2785.124 Mobile Safari/537.36");

        List<String> fieldNames = userAgent.getAvailableFieldNamesSorted();

        // The EXPLICITLY requested fields
        assertEquals("Mozilla",         userAgent.getValue("FirstProductName"));
        assertEquals("Phone",           userAgent.getValue("DeviceClass"));
        assertEquals("Google",          userAgent.getValue("DeviceBrand"));
        assertEquals("Google Nexus 6",  userAgent.getValue("DeviceName"));
        assertEquals("Chrome 53",       userAgent.getValue("AgentNameVersionMajor"));

        // The IMPLICITLY requested fields (i.e. partials of the actually requested ones)
        assertEquals("Chrome",          userAgent.getValue("AgentName"));
        assertEquals("53.0.2785.124",   userAgent.getValue("AgentVersion"));
        assertEquals("53",              userAgent.getValue("AgentVersionMajor"));

        // The NOT requested fields are not there
        assertFalse(fieldNames.contains("OperatingSystemClass"));
        assertFalse(fieldNames.contains("OperatingSystemName"));
        assertFalse(fieldNames.contains("OperatingSystemNameVersion"));
        assertFalse(fieldNames.contains("OperatingSystemNameVersionMajor"));
        assertFalse(fieldNames.contains("OperatingSystemVersion"));
        assertFalse(fieldNames.contains("OperatingSystemVersionBuild"));
        assertFalse(fieldNames.contains("OperatingSystemVersionMajor"));
        assertFalse(fieldNames.contains("LayoutEngineClass"));
        assertFalse(fieldNames.contains("LayoutEngineName"));
        assertFalse(fieldNames.contains("LayoutEngineNameVersion"));
        assertFalse(fieldNames.contains("LayoutEngineNameVersionMajor"));
        assertFalse(fieldNames.contains("LayoutEngineVersion"));
        assertFalse(fieldNames.contains("LayoutEngineVersionMajor"));
        assertFalse(fieldNames.contains("AgentClass"));
        assertFalse(fieldNames.contains("AgentNameVersion"));
    }
}
