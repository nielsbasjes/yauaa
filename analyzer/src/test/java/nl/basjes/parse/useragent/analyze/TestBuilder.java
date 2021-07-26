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

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.AbstractUserAgentAnalyzerDirect;
import nl.basjes.parse.useragent.PreHeatCases;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.UserAgentAnalyzer.UserAgentAnalyzerBuilder;
import nl.basjes.parse.useragent.UserAgentAnalyzerDirect;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static nl.basjes.parse.useragent.config.ConfigLoader.DEFAULT_RESOURCES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// CHECKSTYLE.OFF: ParenPad
class TestBuilder {

    private void runTestCase(AbstractUserAgentAnalyzerDirect userAgentAnalyzer) {
        UserAgent parsedAgent = userAgentAnalyzer.parse("Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36");

        LogManager.getLogger(TestBuilder.class).info("{}", parsedAgent.toYamlTestCase(true));

        // The requested fields
        assertEquals("Phone",                    parsedAgent.getValue("DeviceClass"              )); // Phone
        assertEquals("Chrome 53",                parsedAgent.getValue("AgentNameVersionMajor"    )); // Chrome 53

        // The fields that are internally needed to build the requested fields

        // Needed for AgentNameVersionMajor
        assertEquals("Chrome",                   parsedAgent.getValue("AgentName"                )); // Chrome
        assertEquals("53",                       parsedAgent.getValue("AgentVersionMajor"        )); // 53

        // Needed for AgentVersionMajor
        assertEquals("53.0.2785.124",            parsedAgent.getValue("AgentVersion"             )); // 53.0.2785.124

        // Needed for AgentName (The brand is used as fallback in edge cases that occurs with spiders)
        assertEquals("Google",                   parsedAgent.getValue("DeviceBrand"              )); // Google

        // The rest must be the default value (i.e. no rules fired)
        assertTrue(parsedAgent.get("DeviceName"                   ).isDefaultValue()); // Nexus 6
        assertTrue(parsedAgent.get("OperatingSystemClass"         ).isDefaultValue()); // Mobile
        assertTrue(parsedAgent.get("OperatingSystemName"          ).isDefaultValue()); // Android
        assertTrue(parsedAgent.get("OperatingSystemVersion"       ).isDefaultValue()); // 7.0
        assertTrue(parsedAgent.get("OperatingSystemNameVersion"   ).isDefaultValue()); // Android 7.0
        assertTrue(parsedAgent.get("OperatingSystemVersionBuild"  ).isDefaultValue()); // NBD90Z
        assertTrue(parsedAgent.get("LayoutEngineClass"            ).isDefaultValue()); // Browser
        assertTrue(parsedAgent.get("LayoutEngineName"             ).isDefaultValue()); // Blink
        assertTrue(parsedAgent.get("LayoutEngineVersion"          ).isDefaultValue()); // 53.0
        assertTrue(parsedAgent.get("LayoutEngineVersionMajor"     ).isDefaultValue()); // 53
        assertTrue(parsedAgent.get("LayoutEngineNameVersion"      ).isDefaultValue()); // Blink 53.0
        assertTrue(parsedAgent.get("LayoutEngineNameVersionMajor" ).isDefaultValue()); // Blink 53
        assertTrue(parsedAgent.get("AgentClass"                   ).isDefaultValue()); // Browser
        assertTrue(parsedAgent.get("AgentNameVersion"             ).isDefaultValue()); // Chrome 53.0.2785.124
    }

    @Test
    void testLimitedFieldsDirect() {
        UserAgentAnalyzerDirect userAgentAnalyzer =
            UserAgentAnalyzerDirect
                .newBuilder()
                .preheat(100)
                .preheat()
                .hideMatcherLoadStats()
                .showMatcherLoadStats()
                .withAllFields()
                .withField("DeviceClass")
                .withField("AgentNameVersionMajor")
                .withUserAgentMaxLength(1234)
                .build();

        assertEquals(1234, userAgentAnalyzer.getUserAgentMaxLength());

        runTestCase(userAgentAnalyzer);
    }

    @Test
    void testLimitedFields() {
        UserAgentAnalyzer userAgentAnalyzer =
            UserAgentAnalyzer
                .newBuilder()
                .preheat(100)
                .preheat()
                .withCache(42)
                .withoutCache()
                .hideMatcherLoadStats()
                .showMatcherLoadStats()
                .withAllFields()
                .withField("DeviceClass")
                .withField("AgentNameVersionMajor")
                .withUserAgentMaxLength(1234)
                .build();

        assertEquals(1234, userAgentAnalyzer.getUserAgentMaxLength());

        runTestCase(userAgentAnalyzer);
    }

    @Test
    void testLoadAdditionalRules() {
        UserAgentAnalyzer userAgentAnalyzer =
            UserAgentAnalyzer
                .newBuilder()
                .withField("DeviceClass")
                .withoutCache()
                .hideMatcherLoadStats()
                .addResources("ExtraLoadedRule1.yaml")
                .withField("ExtraValue2")
                .withField("ExtraValue1")
                .addResources("ExtraLoadedRule2.yaml")
                .build();

        UserAgent parsedAgent = userAgentAnalyzer.parse("Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36");

        // The requested fields
        assertEquals("Phone",  parsedAgent.getValue("DeviceClass" ));
        assertEquals("One",    parsedAgent.getValue("ExtraValue1" ));
        assertEquals("Two",    parsedAgent.getValue("ExtraValue2" ));
    }

    @Test
    void testLoadOnlyCustomRules() {
        UserAgentAnalyzer userAgentAnalyzer =
            UserAgentAnalyzer
                .newBuilder()
                .withoutCache()
                .hideMatcherLoadStats()
                .addResources("ExtraLoadedRule1.yaml")
                .withField("ExtraValue2")
                .withField("ExtraValue1")
                .addResources("ExtraLoadedRule2.yaml")
                .build();

        UserAgent parsedAgent = userAgentAnalyzer.parse("Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36");

        // The requested fields
        assertEquals("One",    parsedAgent.getValue("ExtraValue1" ));
        assertEquals("Two",    parsedAgent.getValue("ExtraValue2" ));
    }

    @Test
    void testLoadOnlyCompanyCustomFormatRules() {
        UserAgentAnalyzer userAgentAnalyzer =
            UserAgentAnalyzer
                .newBuilder()
                .withoutCache()
                .hideMatcherLoadStats()
                .dropDefaultResources()
                .addResources("CompanyInternalUserAgents.yaml")
                .withFields("ApplicationName", "ApplicationVersion")
                .withFields(Arrays.asList("ApplicationInstance", "ApplicationGitCommit"))
                .withField("ServerName")
                .build();

        UserAgent parsedAgent = userAgentAnalyzer.parse(
            "TestApplication/1.2.3 (node123.datacenter.example.nl; 1234; d71922715c2bfe29343644b14a4731bf5690e66e)");

        // The requested fields
        assertEquals("TestApplication",                          parsedAgent.getValue("ApplicationName"));
        assertEquals("1.2.3",                                    parsedAgent.getValue("ApplicationVersion"));
        assertEquals("1234",                                     parsedAgent.getValue("ApplicationInstance"));
        assertEquals("d71922715c2bfe29343644b14a4731bf5690e66e", parsedAgent.getValue("ApplicationGitCommit"));
        assertEquals("node123.datacenter.example.nl",            parsedAgent.getValue("ServerName"));
    }


    @Test
    void testAskingForImpossibleField() {
        InvalidParserConfigurationException exception = assertThrows(InvalidParserConfigurationException.class, () ->
            UserAgentAnalyzer
                .newBuilder()
                .withoutCache()
                .hideMatcherLoadStats()
                .delayInitialization()
                .withField("FirstNonexistentField")
                .withField("DeviceClass")
                .withField("SecondNonexistentField")
                .build());

        assertEquals("We cannot provide these fields:[FirstNonexistentField, SecondNonexistentField]", exception.getMessage());
    }

    @Test
    void testDualBuilderUsageNoSecondInstance() {
        UserAgentAnalyzerBuilder builder =
            UserAgentAnalyzer.newBuilder().delayInitialization();

        assertNotNull(builder.build(), "We should get a first instance from a single builder.");
        // And calling build() again should fail with an exception

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void testDualBuilderUsageUseSetterAfterBuild() {
        UserAgentAnalyzerBuilder builder =
            UserAgentAnalyzer.newBuilder().delayInitialization();

        assertNotNull(builder.build(), "We should get a first instance from a single builder.");

        // And calling a setter after the build() should fail with an exception
        assertThrows(IllegalStateException.class, () ->
            builder.withCache(1234)
        );
    }


    @Test
    void testLoadMoreResources() {
        UserAgentAnalyzerBuilder builder =
            UserAgentAnalyzer.newBuilder().delayInitialization().withField("DeviceClass");

        UserAgentAnalyzer uaa = builder.build();
        assertNotNull(uaa, "We should get a first instance from a single builder.");

        // Try to load something that does not yield any files.
        // Optional --> error message and continue
        uaa.loadResources("Bad resource string that is optional should only give a warning", true, true);

        // NOT Optional --> fail with exception
        assertThrows(InvalidParserConfigurationException.class, () ->
            uaa.loadResources("Bad resource string that is NOT optional should fail hard", true, false)
        );

        uaa.initializeMatchers();
        assertThrows(IllegalStateException.class, () ->
            uaa.loadResources(DEFAULT_RESOURCES)
        );
    }

    @Test
    void testPostPreheatDroptests() {
        UserAgentAnalyzer userAgentAnalyzer =
            UserAgentAnalyzer
                .newBuilder()
                .immediateInitialization()
                // Without .preheat(100)
                .dropTests()
                .hideMatcherLoadStats()
                .withField("AgentName")
                .build();
        assertEquals(0, userAgentAnalyzer.getNumberOfTestCases());

        userAgentAnalyzer =
            UserAgentAnalyzer
                .newBuilder()
                .immediateInitialization()
                .preheat(100) // With .preheat(100)
                .dropTests()
                .hideMatcherLoadStats()
                .withField("AgentName")
                .build();
        assertEquals(0, userAgentAnalyzer.getNumberOfTestCases());
    }

    @Test
    void testPreheatNoTests() {
        UserAgentAnalyzer userAgentAnalyzer =
            UserAgentAnalyzer
                .newBuilder()
                .keepTests()
                .hideMatcherLoadStats()
                .withField("AgentName")
                .build();

        assertTrue(userAgentAnalyzer.getNumberOfTestCases() > 100);
        assertEquals(0, userAgentAnalyzer.preHeat(0));
        assertEquals(0, userAgentAnalyzer.preHeat(-1));
        assertEquals(0, userAgentAnalyzer.preHeat(1000000000L));

        userAgentAnalyzer.dropTests();

        // The full testCases from the yaml are NO LONGER used for the preHeat !
        // Preheat has a separate list with all the test cases (smaller since we do not have the answers there).
        assertEquals(0, userAgentAnalyzer.getNumberOfTestCases());
        assertEquals(PreHeatCases.USERAGENTS.size(), userAgentAnalyzer.preHeat());
    }

}
