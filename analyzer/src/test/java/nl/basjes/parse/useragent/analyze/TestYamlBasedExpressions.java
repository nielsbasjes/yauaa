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

import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestYamlBasedExpressions {
    private UserAgentAnalyzerTester createTester(String filename){
        return UserAgentAnalyzerTester
            .newBuilder()
            .dropDefaultResources()
            .keepTests()
            .addResources("classpath*:" + filename)
            .build();
    }

    @Test
    void runMatcherTests() {
        UserAgentAnalyzerTester uaa = createTester("Matcher-tests.yaml");
        assertTrue(uaa.runTests(false, false));
    }

    @Test
    void runMatcherNestedFunctionsTests() {
        UserAgentAnalyzerTester uaa = createTester("Matcher-nested-functions.yaml");
        assertTrue(uaa.runTests(false, false));
    }

    @Test
    void runMatcherIsNullTests() {
        UserAgentAnalyzerTester uaa = createTester("Matcher-IsNull-tests.yaml");
        assertTrue(uaa.runTests(false, false));
    }

    @Test
    void runSubstringTests() {
        UserAgentAnalyzerTester uaa = createTester("SubString-tests.yaml");
        assertTrue(uaa.runTests(false, false));
    }

    @Test
    void runSubstringVersionTests() {
        UserAgentAnalyzerTester uaa = createTester("SubStringVersion-tests.yaml");
        assertTrue(uaa.runTests(false, false));
    }

    @Test
    void runSubsegmentTests() {
        UserAgentAnalyzerTester uaa = createTester("SubSegment-tests.yaml");
        assertTrue(uaa.runTests(false, false));
    }


    @Test
    void runLookupTests() {
        UserAgentAnalyzerTester uaa = createTester("Lookup-tests.yaml");
        assertTrue(uaa.runTests(false, false));
    }

    @Test
    void runLookupPrefixTests() {
        UserAgentAnalyzerTester uaa = createTester("LookupPrefix-tests.yaml");
        assertTrue(uaa.runTests(false, true));
    }

    @Test
    void runVariableTests() {
        UserAgentAnalyzerTester uaa = createTester("Variable-tests.yaml");
        assertTrue(uaa.runTests(false, false));
    }

    @Test
    void runPositionalTests() {
        UserAgentAnalyzerTester uaa = createTester("Positional-tests.yaml");
        assertTrue(uaa.runTests(false, true));
    }

    @Test
    void runWalkingTests() {
        UserAgentAnalyzerTester uaa = createTester("Walking-tests.yaml");
        uaa.setVerbose(true);
        assertTrue(uaa.runTests(false, false));
    }

    @Test
    void runAllFieldsTests() {
        UserAgentAnalyzerTester uaa = createTester("AllFields-tests.yaml");
        assertTrue(uaa.runTests(false, true));
    }

    @Test
    void runAllStepsTests() {
        UserAgentAnalyzerTester uaa = createTester("AllSteps.yaml");
        assertTrue(uaa.runTests(false, true));
    }

    @Test
    void runDebugOutputTest() {
        UserAgentAnalyzerTester uaa = createTester("DebugOutput-tests.yaml");
        assertTrue(uaa.runTests(true, true));
    }

    @Test
    void runEdgecasesTest() {
        UserAgentAnalyzerTester uaa = createTester("Edgecases-tests.yaml");
        assertTrue(uaa.runTests(false, true));
    }

    @Test
    void runAllPossibleSteps() {
        UserAgentAnalyzerTester uaa = createTester("AllPossibleSteps.yaml");
        uaa.setVerbose(true);
        assertTrue(uaa.runTests(false, false));
    }

    @Test
    void runOnlyOneTest() {
        UserAgentAnalyzerTester uaa = createTester("TestOnlyOneTest.yaml");
        uaa.setVerbose(true);
        assertTrue(uaa.runTests(false, false));
    }

    @Test
    void runIsNullLookupTest() {
        UserAgentAnalyzerTester uaa = createTester("IsNullLookup.yaml");
        uaa.setVerbose(true);
        assertTrue(uaa.runTests(false, true));
    }

    @Test
    void runDefaultIfNullTest() {
        UserAgentAnalyzerTester uaa = createTester("Default-tests.yaml");
        uaa.setVerbose(true);
        assertTrue(uaa.runTests(false, true));
    }

    @Test
    void runReplaceStringTest() {
        UserAgentAnalyzerTester uaa = createTester("ReplaceString-tests.yaml");
        uaa.setVerbose(true);
        assertTrue(uaa.runTests(false, true));
    }

    @Test
    void runFailIfFoundTest() {
        UserAgentAnalyzerTester uaa = createTester("RequireIsNull.yaml");
        uaa.setVerbose(true);
        assertTrue(uaa.runTests(false, true));
    }

}
