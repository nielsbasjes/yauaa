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

import nl.basjes.parse.useragent.UserAgent.ImmutableUserAgent;
import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;
import nl.basjes.parse.useragent.analyze.MatchesList.Match;
import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestDeveloperTools {

    @Test
    void validateErrorSituationOutput() {
        UserAgentAnalyzerTester uaa = UserAgentAnalyzerTester
            .newBuilder()
            .hideMatcherLoadStats()
            .delayInitialization()
            .dropTests()
            .build();
        uaa.setShowMatcherStats(true);
        uaa.keepTests();
        uaa.loadResources("classpath*:**/CheckErrorOutput.yaml");
        assertFalse(uaa.runTests(false, true)); // This test must return an error state
    }

    @Test
    void validateNewTestcaseSituationOutput() {
        UserAgentAnalyzerTester uaa = UserAgentAnalyzerTester
            .newBuilder()
            .delayInitialization()
            .hideMatcherLoadStats()
            .dropTests()
            .build();
        uaa.setShowMatcherStats(true);
        uaa.keepTests();
        uaa.loadResources("classpath*:**/CheckNewTestcaseOutput.yaml");
        assertTrue(uaa.runTests(false, true));

        // Immediately test the output of the toString methods of all of these classes.
        assertTrue(uaa.toString().length() > 1000);
    }


    @Test
    void validateStringOutputsAndMatches() {
        UserAgentAnalyzerTester uaa = UserAgentAnalyzerTester.newBuilder().withField("DeviceName").build();

        MutableUserAgent useragent = new MutableUserAgent("Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36");

        ImmutableUserAgent parseResult = uaa.parse(useragent);
        assertTrue(parseResult.toString().contains("'Google Nexus 6'"));

        assertTrue(parseResult.toJson().contains("\"DeviceName\":\"Google Nexus 6\""));
        assertTrue(parseResult.toJson("DeviceName").contains("\"DeviceName\":\"Google Nexus 6\""));
        assertFalse(parseResult.toJson("DeviceClass").contains("\"DeviceName\":\"Google Nexus 6\""));

        assertTrue(parseResult.toXML().contains("<DeviceName>Google Nexus 6</DeviceName>"));
        assertTrue(parseResult.toXML("DeviceName").contains("<DeviceName>Google Nexus 6</DeviceName>"));
        assertFalse(parseResult.toXML("DeviceClass").contains("<DeviceName>Google Nexus 6</DeviceName>"));

        assertEquals("Google Nexus 6", parseResult.toMap().get("DeviceName"));
        assertEquals("Google Nexus 6", parseResult.toMap("DeviceName").get("DeviceName"));
        assertNull(parseResult.toMap("DeviceClass").get("DeviceName"));

        assertTrue(parseResult.toYamlTestCase(true).contains("'Google Nexus 6'"));

        boolean ok = false;
        for (Match match : uaa.getMatches()) {
            if ("agent.(1)product.(1)comments.(3)entry[3-3]".equals(match.getKey())) {
                assertEquals("Build", match.getValue());
                ok = true;
                break;
            }
        }
        assertTrue(ok, "Did not see the expected match.");

        ok = false;
        for (Match match : uaa.getUsedMatches(useragent)) {
            if ("agent.(1)product.(1)comments.(3)entry[3-3]".equals(match.getKey())) {
                assertEquals("Build", match.getValue());
                ok = true;
                break;
            }
        }
        assertTrue(ok, "Did not see the expected match.");
    }


}
