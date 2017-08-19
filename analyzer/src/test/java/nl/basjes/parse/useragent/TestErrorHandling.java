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

package nl.basjes.parse.useragent;

import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester;
import nl.basjes.parse.useragent.parse.EvilManualUseragentStringHacks;
import org.hamcrest.core.StringStartsWith;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestErrorHandling {
    @Rule
    public final ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void checkNoFile() {
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage("Unable to find ANY config files");

        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:BadDefinitions/ThisOneDoesNotExist---Really.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void checkEmptyFile() {
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage("The file EmptyFile.yaml is empty");

        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:BadDefinitions/EmptyFile.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void checkBadStructure() {
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage("The top level entry MUST be 'config'.");

        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:BadDefinitions/BadStructure.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void checkFileIsNotAMap() {
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage("Yaml config problem.(FileIsNotAMap.yaml:20): The value should be a sequence but it is a mapping");

        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:BadDefinitions/FileIsNotAMap.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void checkNoExtract() {
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage("Matcher does not extract anything");

        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:BadDefinitions/NoExtract.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void checkInvalidExtract() {
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage("Invalid extract config line: agent.text=\"foo\"");

        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:BadDefinitions/InvalidExtract.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void checkNoTestInput() {
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage("Test is missing input");

        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:BadDefinitions/NoTestInput.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void checkSyntaxErrorRequire() {
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage(new StringStartsWith("Syntax error"));

        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:BadDefinitions/SyntaxErrorRequire.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void checkSyntaxErrorExpect() {
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage(new StringStartsWith("Syntax error"));

        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:BadDefinitions/SyntaxErrorExtract.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }


    @Test
    public void methodInputValidation(){
        UserAgentAnalyzer uaa = UserAgentAnalyzer.newBuilder()
            .build();

        UserAgent agent = uaa.parse((String)null);
        assertNotNull(agent);
        assertNull(agent.getUserAgentString());

        agent = uaa.parse((UserAgent) null);
        assertNull(agent);

        assertNull(EvilManualUseragentStringHacks.fixIt(null));
    }

}
