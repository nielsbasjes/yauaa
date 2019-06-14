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
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
public class TestResourceLoading {
    @Rule
    public final ExpectedException expectedEx = ExpectedException.none();

    private void runTest(String resourceString, Matcher<String> expectedMessage) {
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage(expectedMessage);

        UserAgentAnalyzerTester uaa = UserAgentAnalyzerTester.newBuilder().dropDefaultResources().keepTests().addResources(resourceString).build();
        Assert.assertTrue(uaa.runTests(false, false));
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
        Assert.assertTrue(uaa.runTests(false, false));
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

        Assert.assertTrue(uaa.runTests(false, false));
    }

}
