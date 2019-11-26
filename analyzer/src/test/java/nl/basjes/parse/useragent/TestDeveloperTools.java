/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2019 Niels Basjes
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

import nl.basjes.parse.useragent.analyze.MatchesList.Match;
import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestDeveloperTools {

    @Test
    public void validateErrorSituationOutput() {
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
    public void validateNewTestcaseSituationOutput() {
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
    }


    @Test
    public void validateStringOutputsAndMatches() {
        UserAgentAnalyzerTester uaa = UserAgentAnalyzerTester.newBuilder().withField("DeviceName").build();
        UserAgent useragent = uaa.parse("Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36");
        assertTrue(useragent.toString().contains("'Google Nexus 6'"));
        assertTrue(useragent.toJson().contains("\"DeviceName\":\"Google Nexus 6\""));
        assertTrue(useragent.toXML().contains("<DeviceName>Google Nexus 6</DeviceName>"));
        assertTrue(useragent.toYamlTestCase(true).contains("'Google Nexus 6'"));

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
