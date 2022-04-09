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

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.Analyzer;
import nl.basjes.parse.useragent.PreHeatCases;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.UserAgentAnalyzer.UserAgentAnalyzerBuilder;
import nl.basjes.parse.useragent.UserAgentAnalyzerDirect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static nl.basjes.parse.useragent.UserAgent.AGENT_CLASS;
import static nl.basjes.parse.useragent.UserAgent.AGENT_INFORMATION_EMAIL;
import static nl.basjes.parse.useragent.UserAgent.AGENT_INFORMATION_URL;
import static nl.basjes.parse.useragent.UserAgent.AGENT_LANGUAGE;
import static nl.basjes.parse.useragent.UserAgent.AGENT_LANGUAGE_CODE;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.AGENT_SECURITY;
import static nl.basjes.parse.useragent.UserAgent.AGENT_UUID;
import static nl.basjes.parse.useragent.UserAgent.AGENT_VERSION;
import static nl.basjes.parse.useragent.UserAgent.AGENT_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_BRAND;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_CLASS;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_CPU;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_CPU_BITS;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_FIRMWARE_VERSION;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_NAME;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_VERSION;
import static nl.basjes.parse.useragent.UserAgent.HACKER_ATTACK_VECTOR;
import static nl.basjes.parse.useragent.UserAgent.HACKER_TOOLKIT;
import static nl.basjes.parse.useragent.UserAgent.LAYOUT_ENGINE_BUILD;
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
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_VERSION_BUILD;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.SET_ALL_FIELDS;
import static nl.basjes.parse.useragent.UserAgent.SYNTAX_ERROR;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_NAME;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_VERSION;
import static nl.basjes.parse.useragent.UserAgent.WEBVIEW_APP_VERSION_MAJOR;
import static nl.basjes.parse.useragent.config.ConfigLoader.DEFAULT_RESOURCES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// CHECKSTYLE.OFF: ParenPad
class TestBuilder {

    private void runTestCase(Analyzer userAgentAnalyzer) {

        String userAgentString = "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36";
        UserAgent parsedAgent = userAgentAnalyzer.parse(userAgentString);

        LogManager.getLogger(TestBuilder.class).info("{}", parsedAgent.toYamlTestCase(true));

        // The original input
        assertEquals(userAgentString,            parsedAgent.getUserAgentString());

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
        shouldBeDefaultValue(parsedAgent, "DeviceName"                   ); // Nexus 6
        shouldBeDefaultValue(parsedAgent, "OperatingSystemClass"         ); // Mobile
        shouldBeDefaultValue(parsedAgent, "OperatingSystemName"          ); // Android
        shouldBeDefaultValue(parsedAgent, "OperatingSystemVersion"       ); // 7.0
        shouldBeDefaultValue(parsedAgent, "OperatingSystemNameVersion"   ); // Android 7.0
        shouldBeDefaultValue(parsedAgent, "OperatingSystemVersionBuild"  ); // NBD90Z
        shouldBeDefaultValue(parsedAgent, "LayoutEngineClass"            ); // Browser
        shouldBeDefaultValue(parsedAgent, "LayoutEngineName"             ); // Blink
        shouldBeDefaultValue(parsedAgent, "LayoutEngineVersion"          ); // 53.0
        shouldBeDefaultValue(parsedAgent, "LayoutEngineVersionMajor"     ); // 53
        shouldBeDefaultValue(parsedAgent, "LayoutEngineNameVersion"      ); // Blink 53.0
        shouldBeDefaultValue(parsedAgent, "LayoutEngineNameVersionMajor" ); // Blink 53
        shouldBeDefaultValue(parsedAgent, "AgentClass"                   ); // Browser
        shouldBeDefaultValue(parsedAgent, "AgentNameVersion"             ); // Chrome 53.0.2785.124
    }

    private void shouldBeDefaultValue(UserAgent parsedAgent, String fieldName) {
        assertTrue(parsedAgent.get(fieldName).isDefaultValue(),
            "Unexpected value for \"" + fieldName + "\"");
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

    private static final Logger LOG = LogManager.getLogger(TestBuilder.class);

    private UserAgentAnalyzer createWithWantedFieldNames(String... fieldNames) {
        UserAgentAnalyzerBuilder builder =
            UserAgentAnalyzer
                .newBuilder()
                .dropTests()
                .hideMatcherLoadStats();
        for (String fieldName: fieldNames) {
            builder.withField(fieldName);
        }
        UserAgentAnalyzer userAgentAnalyzer = builder.build();

        Set<String> wantedFieldNames = userAgentAnalyzer.getWantedFieldNames();
        LOG.info("Input : fields({}): {}", fieldNames.length, fieldNames);
        LOG.info("Output: fields({}): {}", wantedFieldNames == null ? "null" : wantedFieldNames.size(), wantedFieldNames);
        return userAgentAnalyzer;
    }

    private Set<String> fields(String... names) {
        return new TreeSet<>(Arrays.asList(names));
    }

    private static final String TEST_UA = "Mozilla/5.0 (SM-123) Niels Basjes/42 (https://yauaa.basjes.nl)";

    @Test
    void testWantedFieldNamesAll() {
        UserAgentAnalyzer uaa = createWithWantedFieldNames();
        assertNull(uaa.getWantedFieldNames());
        Set<String> expectedPossibleFields = fields(
            SYNTAX_ERROR,
            DEVICE_CLASS,
            DEVICE_BRAND,
            DEVICE_NAME,
            DEVICE_CPU,
            DEVICE_CPU_BITS,
            DEVICE_FIRMWARE_VERSION,
            DEVICE_VERSION,
            OPERATING_SYSTEM_CLASS,
            OPERATING_SYSTEM_NAME,
            OPERATING_SYSTEM_VERSION,
            OPERATING_SYSTEM_VERSION_MAJOR,
            OPERATING_SYSTEM_NAME_VERSION,
            OPERATING_SYSTEM_NAME_VERSION_MAJOR,
            OPERATING_SYSTEM_VERSION_BUILD,
            LAYOUT_ENGINE_CLASS,
            LAYOUT_ENGINE_NAME,
            LAYOUT_ENGINE_VERSION,
            LAYOUT_ENGINE_VERSION_MAJOR,
            LAYOUT_ENGINE_NAME_VERSION,
            LAYOUT_ENGINE_NAME_VERSION_MAJOR,
            AGENT_CLASS,
            AGENT_NAME,
            AGENT_VERSION,
            AGENT_VERSION_MAJOR,
            AGENT_NAME_VERSION,
            AGENT_NAME_VERSION_MAJOR,
            AGENT_INFORMATION_EMAIL,
            AGENT_INFORMATION_URL,
            AGENT_LANGUAGE,
            AGENT_LANGUAGE_CODE,
            AGENT_NAME,
            AGENT_NAME_VERSION,
            AGENT_NAME_VERSION_MAJOR,
            AGENT_SECURITY,
            AGENT_UUID,
            AGENT_VERSION,
            AGENT_VERSION_MAJOR,
            HACKER_ATTACK_VECTOR,
            HACKER_TOOLKIT,
            LAYOUT_ENGINE_BUILD,
            LAYOUT_ENGINE_CLASS,
            LAYOUT_ENGINE_NAME,
            LAYOUT_ENGINE_NAME_VERSION,
            LAYOUT_ENGINE_NAME_VERSION_MAJOR,
            LAYOUT_ENGINE_VERSION,
            LAYOUT_ENGINE_VERSION_MAJOR,
            WEBVIEW_APP_NAME,
            WEBVIEW_APP_NAME_VERSION,
            WEBVIEW_APP_NAME_VERSION_MAJOR,
            WEBVIEW_APP_VERSION,
            WEBVIEW_APP_VERSION_MAJOR
        );
        // We only specify a subset of all fields to test for because otherwise each new field will fail this test.
        List<String> allPossibleFieldNames = uaa.getAllPossibleFieldNamesSorted();
        for (String expectedField : expectedPossibleFields) {
            assertTrue(allPossibleFieldNames.contains(expectedField), "Missing field: " + expectedField);
        }

        UserAgent userAgent = uaa.parse(TEST_UA);
        List<String> available = userAgent.getAvailableFieldNamesSorted();

        Set<String> expectedAvailableFields = fields(
            SYNTAX_ERROR,
            DEVICE_CLASS,
            DEVICE_BRAND,
            DEVICE_NAME,
            OPERATING_SYSTEM_CLASS,
            OPERATING_SYSTEM_NAME,
            OPERATING_SYSTEM_VERSION,
            OPERATING_SYSTEM_VERSION_MAJOR,
            OPERATING_SYSTEM_NAME_VERSION,
            OPERATING_SYSTEM_NAME_VERSION_MAJOR,
            LAYOUT_ENGINE_CLASS,
            LAYOUT_ENGINE_NAME,
            LAYOUT_ENGINE_VERSION,
            LAYOUT_ENGINE_VERSION_MAJOR,
            LAYOUT_ENGINE_NAME_VERSION,
            LAYOUT_ENGINE_NAME_VERSION_MAJOR,
            AGENT_CLASS,
            AGENT_NAME,
            AGENT_VERSION,
            AGENT_VERSION_MAJOR,
            AGENT_NAME_VERSION,
            AGENT_NAME_VERSION_MAJOR,
            AGENT_INFORMATION_URL,
            AGENT_NAME,
            AGENT_NAME_VERSION,
            AGENT_NAME_VERSION_MAJOR,
            AGENT_VERSION,
            AGENT_VERSION_MAJOR
        );

        // We only specify a subset of all fields to test for because otherwise each new field will fail this test.
        for (String expectedField : expectedAvailableFields) {
            assertTrue(available.contains(expectedField), "Missing field: " + expectedField);
        }
    }

    @Test
    void testWantedFieldNamesOne() {
        UserAgentAnalyzer uaa = createWithWantedFieldNames(
            OPERATING_SYSTEM_NAME_VERSION
        );

        Set<String> expectedWantedFields = fields(
            DEVICE_CLASS,
            OPERATING_SYSTEM_NAME,
            OPERATING_SYSTEM_VERSION,
            OPERATING_SYSTEM_NAME_VERSION,
            SET_ALL_FIELDS);
        assertEquals(expectedWantedFields, uaa.getWantedFieldNames());

        Set<String> expectedPossibleFields = fields(
            SYNTAX_ERROR,
            DEVICE_CLASS,
            OPERATING_SYSTEM_NAME,
            OPERATING_SYSTEM_VERSION,
            OPERATING_SYSTEM_NAME_VERSION);
        assertEquals(expectedPossibleFields, uaa.getAllPossibleFieldNames());

        UserAgent userAgent = uaa.parse(TEST_UA);
        assertEquals(expectedPossibleFields, new TreeSet<>(userAgent.getAvailableFieldNamesSorted()));
    }

    @Test
    void testWantedFieldNamesTwo() {
        UserAgentAnalyzer uaa = createWithWantedFieldNames(
            OPERATING_SYSTEM_NAME,
            AGENT_VERSION
        );

        Set<String> expectedWantedFields = fields(
            DEVICE_CLASS,
            OPERATING_SYSTEM_NAME,
            AGENT_VERSION,
            SET_ALL_FIELDS);
        assertEquals(expectedWantedFields, uaa.getWantedFieldNames());

        Set<String> expectedPossibleFields = fields(
            SYNTAX_ERROR,
            DEVICE_CLASS,
            OPERATING_SYSTEM_NAME,
            AGENT_VERSION);
        assertEquals(expectedPossibleFields, uaa.getAllPossibleFieldNames());

        UserAgent userAgent = uaa.parse(TEST_UA);
        assertEquals(expectedPossibleFields, new TreeSet<>(userAgent.getAvailableFieldNamesSorted()));
    }

    @Test
    void testWantedFieldNamesOneAndCalc() {
        UserAgentAnalyzer uaa = createWithWantedFieldNames(
            AGENT_NAME_VERSION_MAJOR
        );

        Set<String> expectedWantedFields = fields(
            DEVICE_CLASS,
            DEVICE_BRAND,
            AGENT_INFORMATION_EMAIL,
            AGENT_INFORMATION_URL,
            AGENT_NAME,
            AGENT_VERSION,
            AGENT_VERSION_MAJOR,
            AGENT_NAME_VERSION_MAJOR,
            SET_ALL_FIELDS);
        assertEquals(expectedWantedFields, uaa.getWantedFieldNames());

        Set<String> expectedPossibleFields = fields(
            SYNTAX_ERROR,
            DEVICE_CLASS,
            DEVICE_BRAND,
            AGENT_INFORMATION_EMAIL,
            AGENT_INFORMATION_URL,
            AGENT_NAME,
            AGENT_VERSION,
            AGENT_VERSION_MAJOR,
            AGENT_NAME_VERSION_MAJOR);
        assertEquals(expectedPossibleFields, uaa.getAllPossibleFieldNames());

        UserAgent userAgent = uaa.parse(TEST_UA);
        assertEquals(expectedPossibleFields, new TreeSet<>(userAgent.getAvailableFieldNamesSorted()));
    }

}
