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

package nl.basjes.parse.useragent.utils;

import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.core.StringContains.containsString;

public class TestYamlParsing {

    @Rule
    public final ExpectedException expectedEx = ExpectedException.none();

    private void runTest(String inputFilename, String message) {
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage(containsString(message));

        UserAgentAnalyzer uaa = UserAgentAnalyzer
            .newBuilder()
            .dropDefaultResources()
            .keepTests()
            .addResources("classpath*:YamlParsingTests/" + inputFilename)
            .delayInitialization()
            .build();
    }

    @Test
    public void testEmpty() {
        runTest("Empty.yaml", "No matchers were loaded at all.");
    }

    @Test
    public void testTopNotConfig() {
        runTest("TopNotConfig.yaml", "The top level entry MUST be 'config'");
    }

    @Test
    public void testNotAMapFile() {
        runTest("NotAMapFile.yaml", "File must be a Map");
    }

    @Test
    public void testBadConfig1() {
        runTest("BadConfig1.yaml", "The value should be a sequence but it is a scalar");
    }

    @Test
    public void testBadConfig2() {
        runTest("BadConfig2.yaml", "The entry MUST be a mapping");
    }

    @Test
    public void testInputAbsent() {
        runTest("InputAbsent.yaml", "Test is missing input");
    }

    @Test
    public void testInputNotString() {
        runTest("InputNotString.yaml", "The value should be a string but it is a sequence");
    }

    @Test
    public void testNotAMap() {
        runTest("NotAMap.yaml", "The value should be a map but it is a scalar");
    }

    @Test
    public void testNotSingle() {
        runTest("NotSingle.yaml", "There must be exactly 1 value in the list");
    }

    @Test
    public void testNotStringList1() {
        runTest("NotStringList1.yaml", "The value should be a string but it is a mapping");
    }

    @Test
    public void testNotStringList2() {
        runTest("NotStringList2.yaml", "The provided node must be a sequence but it is a scalar");
    }

    @Test
    public void testKeyNotString() {
        runTest("KeyNotString.yaml", "The key should be a string but it is a sequence");
    }

    @Test
    public void testParseError() {
        runTest("ParseError.yaml", "Parse error in the file ParseError.yaml: ");
    }

}
