/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2017 Niels Basjes
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

import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester;
import org.junit.Assert;
import org.junit.Test;

public class TestMatcherExpressions {
//    @Test
//    public void runSingleMatcherFile() {
//        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:**/Linux.yaml");
//        Assert.assertTrue(uaa.runTests(true, false));
//    }

    @Test
    public void runMatcherTests() {
        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:Matcher-tests.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void runMatcherNestedFunctionsTests() {
        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:Matcher-nested-functions.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void runMatcherIsNullTests() {
        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:Matcher-IsNull-tests.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void runSubstringTests() {
        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:SubString-tests.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void runSubstringVersionTests() {
        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:SubStringVersion-tests.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void runLookupTests() {
        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:Lookup-tests.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void runVariableTests() {
        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:Variable-tests.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void runPositionalTests() {
        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:Positional-tests.yaml");
        Assert.assertTrue(uaa.runTests(false, true));
    }

    @Test
    public void runWalkingTests() {
        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:Walking-tests.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void runAllFieldsTests() {
        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:AllFields-tests.yaml");
        Assert.assertTrue(uaa.runTests(false, true));
    }

    @Test
    public void runDebugOutputTest() {
        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:DebugOutput-tests.yaml");
        Assert.assertTrue(uaa.runTests(true, true));
    }

    @Test
    public void runEdgecasesTest() {
        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:Edgecases-tests.yaml");
        Assert.assertTrue(uaa.runTests(false, true));
    }

    @Test
    public void runAllPossibleSteps() {
        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:AllPossibleSteps.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

}
