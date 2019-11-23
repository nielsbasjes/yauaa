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
import nl.basjes.parse.useragent.parse.EvilManualUseragentStringHacks;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestErrorHandling {

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
    public void checkNoFile() {
        runTest(
            "classpath*:BadDefinitions/ThisOneDoesNotExist---Really.yaml",
            containsString("No matchers were loaded at all."));
    }

    @Test
    public void checkEmptyFile() {
        runTest(
            "classpath*:BadDefinitions/EmptyFile.yaml",
            containsString("No matchers were loaded at all."));
    }

    @Test
    public void checkBadStructure() {
        runTest(
            "classpath*:BadDefinitions/BadStructure.yaml",
            containsString("The top level entry MUST be 'config'."));
    }

    @Test
    public void checkFileIsNotAMap() {
        runTest(
            "classpath*:BadDefinitions/FileIsNotAMap.yaml",
            containsString("Yaml config problem.(FileIsNotAMap.yaml:20): The value should be a sequence but it is a mapping"));
    }

    @Test
    public void checkLookupSetMissing() {
        runTest(
            "classpath*:BadDefinitions/LookupSetMissing.yaml",
            containsString("Missing lookupSet"));
    }

    @Test
    public void checkBadEntry() {
        runTest(
            "classpath*:BadDefinitions/BadEntry.yaml",
            containsString("Found unexpected config entry:"));
    }

    @Test
    public void checkLookupMissing() {
        runTest(
            "classpath*:BadDefinitions/LookupMissing.yaml",
            containsString("Missing lookup"));
    }

    @Test
    public void checkLookupPrefixMissing() {
        runTest(
            "classpath*:BadDefinitions/LookupPrefixMissing.yaml",
            containsString("Missing lookup"));
    }

    @Test
    public void checkIsInLookupPrefixMissing() {
        runTest(
            "classpath*:BadDefinitions/IsInLookupPrefixMissing.yaml",
            containsString("Missing lookup"));
    }

    @Test
    public void checkLookupDuplicateKey() {
        runTest(
            "classpath*:BadDefinitions/LookupDuplicateKey.yaml",
            containsString("appears multiple times"));
    }

    @Test
    public void checkFixedStringLookupMissing() {
        runTest(
            "classpath*:BadDefinitions/FixedStringLookupMissing.yaml",
            containsString("Missing lookup"));
    }

    @Test
    public void checkFixedStringLookupMissingvalue() {
        runTest(
            "classpath*:BadDefinitions/FixedStringLookupMissingValue.yaml",
            containsString("Fixed value"));
    }


    @Test
    public void checkNoExtract() {
        runTest(
            "classpath*:BadDefinitions/NoExtract.yaml",
            containsString("Matcher does not extract anything"));
    }

    @Test
    public void checkInvalidExtract() {
        runTest(
            "classpath*:BadDefinitions/InvalidExtract.yaml",
            containsString("Invalid extract config line: agent.text=\"foo\""));
    }

    @Test
    public void checkNoTestInput() {
        runTest(
            "classpath*:BadDefinitions/NoTestInput.yaml",
            containsString("Test is missing input"));
    }

    @Test
    public void checkSyntaxErrorRequire() {
        runTest(
            "classpath*:BadDefinitions/SyntaxErrorRequire.yaml",
            startsWith("Syntax error"));
    }

    @Test
    public void checkSyntaxErrorExtract1() {
        runTest(
            "classpath*:BadDefinitions/SyntaxErrorExtract1.yaml",
            startsWith("Syntax error"));
    }

    @Test
    public void checkSyntaxErrorExtract2() {
        runTest(
            "classpath*:BadDefinitions/SyntaxErrorExtract2.yaml",
            startsWith("Invalid extract config line"));
    }

    @Test
    public void checkSyntaxErrorVariable1() {
        runTest(
            "classpath*:BadDefinitions/SyntaxErrorVariable1.yaml",
            startsWith("Syntax error"));
    }

    @Test
    public void checkSyntaxErrorVariable2() {
        runTest(
            "classpath*:BadDefinitions/SyntaxErrorVariable2.yaml",
            startsWith("Invalid variable config line:"));
    }

    @Test
    public void checkSyntaxErrorVariableBackReference() {
        runTest(
            "classpath*:BadDefinitions/Variable-BackReference.yaml",
            startsWith("Syntax error"));
    }

    @Test
    public void checkSyntaxErrorVariableBadDefinition() {
        runTest(
            "classpath*:BadDefinitions/Variable-BadDefinition.yaml",
            startsWith("Invalid variable config line:"));
    }

    @Test
    public void checkSyntaxErrorVariableFixedString() {
        runTest(
            "classpath*:BadDefinitions/Variable-FixedString.yaml",
            startsWith("Syntax error"));
    }

    @Test
    public void checkForVariableExistance() {
        runTest(
            "classpath*:BadDefinitions/Variable-NoSuchVariable.yaml",
            startsWith("Syntax error"));
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
