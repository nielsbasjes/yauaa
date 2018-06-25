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

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.UserAgentAnalyzer.UserAgentAnalyzerBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

// CHECKSTYLE.OFF: ParenPad
public class TestBuilder {

    @Test
    public void testLimitedFields() {
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

        UserAgent parsedAgent = userAgentAnalyzer.parse("Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36");

        // The requested fields
        assertEquals("Phone",                    parsedAgent.getValue("DeviceClass"              )); // Phone
        assertEquals("Chrome 53",                parsedAgent.getValue("AgentNameVersionMajor"    )); // Chrome 53

        // The fields that are internally needed to build the requested fields
        assertEquals("Chrome",                   parsedAgent.getValue("AgentName"                )); // Chrome
        assertEquals("53.0.2785.124",            parsedAgent.getValue("AgentVersion"             )); // 53.0.2785.124
        assertEquals("53",                       parsedAgent.getValue("AgentVersionMajor"        )); // 53
        assertEquals("Chrome 53.0.2785.124",     parsedAgent.getValue("AgentNameVersion"         )); // Chrome 53.0.2785.124

        // The rest must be at confidence -1 (i.e. no rules fired)
        assertEquals(-1, parsedAgent.get("DeviceName"                   ).getConfidence()); // Nexus 6
        assertEquals(-1, parsedAgent.get("DeviceBrand"                  ).getConfidence()); // Google
        assertEquals(-1, parsedAgent.get("OperatingSystemClass"         ).getConfidence()); // Mobile
        assertEquals(-1, parsedAgent.get("OperatingSystemName"          ).getConfidence()); // Android
        assertEquals(-1, parsedAgent.get("OperatingSystemVersion"       ).getConfidence()); // 7.0
        assertEquals(-1, parsedAgent.get("OperatingSystemNameVersion"   ).getConfidence()); // Android 7.0
//        Assert.assertEquals(-1, parsedAgent.get("OperatingSystemVersionBuild"  ).getConfidence()); // NBD90Z
        assertEquals(-1, parsedAgent.get("LayoutEngineClass"            ).getConfidence()); // Browser
        assertEquals(-1, parsedAgent.get("LayoutEngineName"             ).getConfidence()); // Blink
        assertEquals(-1, parsedAgent.get("LayoutEngineVersion"          ).getConfidence()); // 53.0
        assertEquals(-1, parsedAgent.get("LayoutEngineVersionMajor"     ).getConfidence()); // 53
        assertEquals(-1, parsedAgent.get("LayoutEngineNameVersion"      ).getConfidence()); // Blink 53.0
        assertEquals(-1, parsedAgent.get("LayoutEngineNameVersionMajor" ).getConfidence()); // Blink 53
        assertEquals(-1, parsedAgent.get("AgentClass"                   ).getConfidence()); // Browser
    }

    @Rule
    public final ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testAskingForImpossibleField() {
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage("We cannot provide these fields:[FirstNonexistentField, SecondNonexistentField]");

        UserAgentAnalyzer
            .newBuilder()
            .withoutCache()
            .hideMatcherLoadStats()
            .delayInitialization()
            .withField("FirstNonexistentField")
            .withField("DeviceClass")
            .withField("SecondNonexistentField")
            .build();
    }

    @Test(expected = IllegalStateException.class)
    public void testDualBuilderUsageNoSecondInstance() {
        UserAgentAnalyzerBuilder<?, ?> builder =
            UserAgentAnalyzer.newBuilder().delayInitialization();

        assertNotNull("We should get a first instance from a single builder.", builder.build());
        // And calling build() again should fail with an exception
        builder.build();
    }

    @Test(expected = IllegalStateException.class)
    public void testDualBuilderUsageUseSetterAfterBuild() {
        UserAgentAnalyzerBuilder<?, ?> builder =
            UserAgentAnalyzer.newBuilder().delayInitialization();

        assertNotNull("We should get a first instance from a single builder.", builder.build());

        // And calling a setter after the build() should fail with an exception
        builder.withCache(1234);
    }


    @Test(expected = IllegalStateException.class)
    public void testLoadMoreResources() {
        UserAgentAnalyzerBuilder<?, ?> builder =
            UserAgentAnalyzer.newBuilder().delayInitialization().withField("DeviceClass");

        UserAgentAnalyzer uaa = builder.build();
        assertNotNull("We should get a first instance from a single builder.", uaa);

        uaa.initializeMatchers();
        uaa.loadResources("Something extra");
    }

    @Test
    public void testPostPreheatDroptests() {
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
}
