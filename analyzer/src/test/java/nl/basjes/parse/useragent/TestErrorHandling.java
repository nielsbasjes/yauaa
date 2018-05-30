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

package nl.basjes.parse.useragent;

import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester;
import nl.basjes.parse.useragent.parse.EvilManualUseragentStringHacks;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringStartsWith.startsWith;
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
    public void checkLookupSetMissing() {
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage(containsString("Missing lookupSet"));

        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:BadDefinitions/LookupSetMissing.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void checkBadEntry() {
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage(containsString("Found unexpected config entry:"));

        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:BadDefinitions/BadEntry.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void checkLookupMissing() {
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage(containsString("Missing lookup"));

        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:BadDefinitions/LookupMissing.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void checkFixedStringLookupMissing() {
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage(containsString("Missing lookup"));

        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:BadDefinitions/FixedStringLookupMissing.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void checkFixedStringLookupMissingvalue() {
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage(containsString("Fixed value"));

        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:BadDefinitions/FixedStringLookupMissingValue.yaml");
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
        expectedEx.expectMessage(startsWith("Syntax error"));

        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:BadDefinitions/SyntaxErrorRequire.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void checkSyntaxErrorExpect() {
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage(startsWith("Syntax error"));

        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:BadDefinitions/SyntaxErrorExtract.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void checkSyntaxErrorVariableBackReference() {
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage(startsWith("Syntax error"));

        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:BadDefinitions/Variable-BackReference.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void checkSyntaxErrorVariableBadDefinition() {
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage(startsWith("Invalid variable config line:"));

        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:BadDefinitions/Variable-BadDefinition.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }

    @Test
    public void checkSyntaxErrorVariableFixedString() {
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage(startsWith("Syntax error"));

        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester("classpath*:BadDefinitions/Variable-FixedString.yaml");
        Assert.assertTrue(uaa.runTests(false, false));
    }


    @Test
    public void methodInputValidation(){
        UserAgentAnalyzer uaa = UserAgentAnalyzer.newBuilder()
            .withField("AgentClass")
            .build();

        UserAgent agent = uaa.parse((String)null);
        assertNotNull(agent);
        assertNull(agent.getUserAgentString());

        agent = uaa.parse((UserAgent) null);
        assertNull(agent);

        assertNull(EvilManualUseragentStringHacks.fixIt(null));
    }

}
