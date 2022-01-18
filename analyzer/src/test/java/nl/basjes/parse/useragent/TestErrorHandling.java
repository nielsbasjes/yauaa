/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2022 Niels Basjes
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

import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;
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

class TestErrorHandling {

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

        assertTrue(expectedMessage.matches(exception.getMessage()), "Bad message:" + exception.getMessage());
    }

    @Test
    void checkNoFile() {
        runTest(
            "classpath*:BadDefinitions/ThisOneDoesNotExist---Really.yaml",
            containsString("There were no resources found for the expression: "));
    }

    @Test
    void checkEmptyFile() {
        runTest(
            "classpath*:BadDefinitions/EmptyFile.yaml",
            containsString("No matchers were loaded at all."));
    }

    @Test
    void checkAlmostEmptyFile() {
        runTest(
            "classpath*:BadDefinitions/AlmostEmptyFile.yaml",
            containsString("The value should be a sequence but it is a scalar"));
    }

    @Test
    void checkBadStructure() {
        runTest(
            "classpath*:BadDefinitions/BadStructure.yaml",
            containsString("The top level entry MUST be 'config'."));
    }

    @Test
    void checkFileIsNotAMap() {
        runTest(
            "classpath*:BadDefinitions/FileIsNotAMap.yaml",
            containsString("Yaml config problem.(FileIsNotAMap.yaml:20): The value should be a sequence but it is a mapping"));
    }

    @Test
    void checkLookupSetMissing() {
        runTest(
            "classpath*:BadDefinitions/LookupSetMissing.yaml",
            containsString("Missing lookupSet"));
    }

    @Test
    void checkBadEntry() {
        runTest(
            "classpath*:BadDefinitions/BadEntry.yaml",
            containsString("Found unexpected config entry:"));
    }

    @Test
    void checkLookupMergeMissing() {
        runTest(
            "classpath*:BadDefinitions/LookupMergeMissing.yaml",
            containsString("Unable to merge lookup"));
    }

    @Test
    void checkLookupMergeSetMissing() {
        runTest(
            "classpath*:BadDefinitions/LookupMergeSetMissing.yaml",
            containsString("Unable to merge set"));
    }

    @Test
    void checkLookupMissing() {
        runTest(
            "classpath*:BadDefinitions/LookupMissing.yaml",
            containsString("Missing lookup"));
    }

    @Test
    void checkLookupPrefixMissing() {
        runTest(
            "classpath*:BadDefinitions/LookupPrefixMissing.yaml",
            containsString("Missing lookup"));
    }

    @Test
    void checkIsInLookupPrefixMissing() {
        runTest(
            "classpath*:BadDefinitions/IsInLookupPrefixMissing.yaml",
            containsString("Missing lookup"));
    }

    @Test
    void checkIsNotInLookupPrefixMissing() {
        runTest(
            "classpath*:BadDefinitions/IsNotInLookupPrefixMissing.yaml",
            containsString("Missing lookup"));
    }

    @Test
    void checkLookupDuplicateKey() {
        runTest(
            "classpath*:BadDefinitions/LookupDuplicateKey.yaml",
            containsString("appears multiple times"));
    }

    @Test void checkFixedStringFailIfFound() {
        runTest(
            "classpath*:BadDefinitions/FixedStringFailIfFound.yaml",
            containsString("It is useless to put a fixed value \"One\" in the failIfFound section."));
    }

    private static final String FIXED_LOOKUP_ERROR = "A lookup for a fixed input value is a needless complexity.";

    @Test void checkFixedStringLookup() {
        runTest(
            "classpath*:BadDefinitions/FixedStringLookup.yaml",
            containsString(FIXED_LOOKUP_ERROR));
    }

    @Test void checkFixedStringLookupContains() {
        runTest(
            "classpath*:BadDefinitions/FixedStringLookupContains.yaml",
            containsString(FIXED_LOOKUP_ERROR));
    }

    @Test void checkFixedStringLookupPrefix() {
        runTest(
            "classpath*:BadDefinitions/FixedStringLookupPrefix.yaml",
            containsString(FIXED_LOOKUP_ERROR));
    }

    @Test void checkFixedStringIsInLookup() {
        runTest(
            "classpath*:BadDefinitions/FixedStringIsInLookup.yaml",
            containsString(FIXED_LOOKUP_ERROR));
    }

    @Test void checkFixedStringIsInLookupContains() {
        runTest(
            "classpath*:BadDefinitions/FixedStringIsInLookupContains.yaml",
            containsString(FIXED_LOOKUP_ERROR));
    }

    @Test void checkFixedStringIsInLookupPrefix() {
        runTest(
            "classpath*:BadDefinitions/FixedStringIsInLookupPrefix.yaml",
            containsString(FIXED_LOOKUP_ERROR));
    }

    @Test
    void checkNoExtract() {
        runTest(
            "classpath*:BadDefinitions/NoExtract.yaml",
            containsString("Matcher does not extract anything"));
    }

    @Test
    void checkInvalidExtract() {
        runTest(
            "classpath*:BadDefinitions/InvalidExtract.yaml",
            containsString("Invalid extract config line: agent.text=\"foo\""));
    }

    @Test
    void checkNoTestInput() {
        runTest(
            "classpath*:BadDefinitions/NoTestInput.yaml",
            containsString("Test is missing input"));
    }

    @Test
    void checkSyntaxErrorRequire() {
        runTest(
            "classpath*:BadDefinitions/SyntaxErrorRequire.yaml",
            startsWith("Syntax error"));
    }

    @Test
    void checkSyntaxErrorExtract1() {
        runTest(
            "classpath*:BadDefinitions/SyntaxErrorExtract1.yaml",
            startsWith("Syntax error"));
    }

    @Test
    void checkSyntaxErrorExtract2() {
        runTest(
            "classpath*:BadDefinitions/SyntaxErrorExtract2.yaml",
            startsWith("Invalid extract config line"));
    }

    @Test
    void checkSyntaxErrorVariable1() {
        runTest(
            "classpath*:BadDefinitions/SyntaxErrorVariable1.yaml",
            startsWith("Syntax error"));
    }

    @Test
    void checkSyntaxErrorVariable2() {
        runTest(
            "classpath*:BadDefinitions/SyntaxErrorVariable2.yaml",
            startsWith("Invalid variable config line:"));
    }

    @Test
    void checkSyntaxErrorVariableBackReference() {
        runTest(
            "classpath*:BadDefinitions/Variable-BackReference.yaml",
            startsWith("Syntax error"));
    }

    @Test
    void checkSyntaxErrorVariableBadDefinition() {
        runTest(
            "classpath*:BadDefinitions/Variable-BadDefinition.yaml",
            startsWith("Invalid variable config line:"));
    }

    @Test
    void checkSyntaxErrorVariableFixedString() {
        runTest(
            "classpath*:BadDefinitions/Variable-FixedString.yaml",
            startsWith("Syntax error"));
    }

    @Test
    void checkForVariableExistance() {
        runTest(
            "classpath*:BadDefinitions/Variable-NoSuchVariable.yaml",
            startsWith("Syntax error"));
    }


    @Test
    void methodInputValidation(){
        UserAgentAnalyzer uaa = UserAgentAnalyzer.newBuilder()
            .withField("AgentClass")
            .build();

        UserAgent agent = uaa.parse((String)null);
        assertNotNull(agent);
        assertNull(agent.getUserAgentString());

        agent = uaa.parse((MutableUserAgent) null);
        assertNull(agent);

        assertNull(EvilManualUseragentStringHacks.fixIt(null));
    }

}
