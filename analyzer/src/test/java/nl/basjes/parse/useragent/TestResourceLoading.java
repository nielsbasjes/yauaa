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

import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester;
import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester.UserAgentAnalyzerTesterBuilder;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestResourceLoading {

    private void runTest(String resourceString, Matcher<String> expectedMessage) {
        InvalidParserConfigurationException exception =
            assertThrows(InvalidParserConfigurationException.class, () -> {
                UserAgentAnalyzerTester uaa = UserAgentAnalyzerTester
                    .newBuilder()
                    .dropDefaultResources()
                    .keepTests()
                    .addResources(resourceString)
                    .build();
                assertTrue(uaa.runTests(false, false));
            });
        assertTrue(expectedMessage.matches(exception.getMessage()));
    }

    @Test
    public void checkEmptyAndNormalFile() {
        UserAgentAnalyzerTester uaa = UserAgentAnalyzerTester
            .newBuilder()
            .dropDefaultResources()
            .addResources("classpath*:BadDefinitions/ThisOneDoesNotExist---Really.yaml")
            .addResources("classpath*:BadDefinitions/EmptyFile.yaml")
            .addResources("classpath*:AllSteps.yaml")
            .build();
        assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void checkIfLoadingAllFilesSeparatelyWorks() {
        UserAgentAnalyzerTesterBuilder<?, ?> uaaB = UserAgentAnalyzerTester
            .newBuilder()
            .hideMatcherLoadStats()
            .dropDefaultResources();

        PackagedRules.getRuleFileNames().forEach(uaaB::addResources);

        UserAgentAnalyzerTester uaa = uaaB
            .withCache(10000)
            .build();

        assertTrue(uaa.runTests(false, false));
    }

}
