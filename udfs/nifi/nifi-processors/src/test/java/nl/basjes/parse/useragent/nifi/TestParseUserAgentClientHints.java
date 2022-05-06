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

package nl.basjes.parse.useragent.nifi;

import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.basjes.parse.useragent.nifi.ParseUserAgent.ATTRIBUTE_PREFIX;
import static nl.basjes.parse.useragent.nifi.ParseUserAgent.FIELD_PREFIX;
import static nl.basjes.parse.useragent.nifi.ParseUserAgent.HEADER_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;

// CHECKSTYLE.OFF: ParenPad
class TestParseUserAgentClientHints {

    private TestRunner runner;

    @BeforeEach
    void before(){
        // Generate a test runner to mock a processor in a flow
        runner = TestRunners.newTestRunner(new ParseUserAgent());
    }

    @Test
    void testParserFullWithClientHints() {

        // Defined which Request Header can be found in which attribute
        //                                 Request Headers PropertyNames        Attribute names
        runner.setProperty(HEADER_PREFIX + "UserAgent",                         "XXuseragentXX");
        runner.setProperty(HEADER_PREFIX + "SecCHUA",                           "XXsecchuaXX");
        runner.setProperty(HEADER_PREFIX + "SecCHUAArch",                       "XXsecchuaarchXX");
        runner.setProperty(HEADER_PREFIX + "SecCHUABitness",                    "XXsecchuabitnessXX");
        runner.setProperty(HEADER_PREFIX + "SecCHUAFullVersion",                "XXsecchuafullversionXX");
        runner.setProperty(HEADER_PREFIX + "SecCHUAFullVersionList",            "XXsecchuafullversionlistXX");
        runner.setProperty(HEADER_PREFIX + "SecCHUAMobile",                     "XXsecchuamobileXX");
        runner.setProperty(HEADER_PREFIX + "SecCHUAModel",                      "XXsecchuamodelXX");
        runner.setProperty(HEADER_PREFIX + "SecCHUAPlatform",                   "XXsecchuaplatformXX");
        runner.setProperty(HEADER_PREFIX + "SecCHUAPlatformVersion",            "XXsecchuaplatformversionXX");
        runner.setProperty(HEADER_PREFIX + "SecCHUAWoW64",                      "XXsecchuawow64XX");

        // Which fields should be extracted (undefined here defaults to "false")
        runner.setProperty(FIELD_PREFIX + "DeviceClass",                        "true");
        runner.setProperty(FIELD_PREFIX + "DeviceName",                         "true");
        runner.setProperty(FIELD_PREFIX + "DeviceBrand",                        "true");
        runner.setProperty(FIELD_PREFIX + "DeviceCpu",                          "true");
        runner.setProperty(FIELD_PREFIX + "DeviceCpuBits",                      "true");
        runner.setProperty(FIELD_PREFIX + "OperatingSystemClass",               "true");
        runner.setProperty(FIELD_PREFIX + "OperatingSystemName",                "true");
        runner.setProperty(FIELD_PREFIX + "OperatingSystemVersion",             "true");
        runner.setProperty(FIELD_PREFIX + "OperatingSystemVersionMajor",        "true");
        runner.setProperty(FIELD_PREFIX + "OperatingSystemNameVersion",         "true");
        runner.setProperty(FIELD_PREFIX + "OperatingSystemNameVersionMajor",    "true");
        runner.setProperty(FIELD_PREFIX + "LayoutEngineClass",                  "true");
        runner.setProperty(FIELD_PREFIX + "LayoutEngineName",                   "true");
        runner.setProperty(FIELD_PREFIX + "LayoutEngineVersion",                "true");
        runner.setProperty(FIELD_PREFIX + "LayoutEngineVersionMajor",           "true");
        runner.setProperty(FIELD_PREFIX + "LayoutEngineNameVersion",            "true");
        runner.setProperty(FIELD_PREFIX + "LayoutEngineNameVersionMajor",       "true");
        runner.setProperty(FIELD_PREFIX + "AgentClass",                         "true");
        runner.setProperty(FIELD_PREFIX + "AgentName",                          "true");
        runner.setProperty(FIELD_PREFIX + "AgentVersion",                       "true");
        runner.setProperty(FIELD_PREFIX + "AgentVersionMajor",                  "true");
        runner.setProperty(FIELD_PREFIX + "AgentNameVersion",                   "true");
        runner.setProperty(FIELD_PREFIX + "AgentNameVersionMajor",              "true");

        String content = "UNUSED DUMMY CONTENT:>>Testing with Client Hints<<";

        // Add the content to the runner (just because we 'should' have some content).
        MockFlowFile flowfile = runner.enqueue(content);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("XXuseragentXX",                 "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36" ); // User-Agent
        attributes.put("XXsecchuaXX",                   "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"100\", \"Google Chrome\";v=\"100\""                             ); // Sec-Ch-Ua
        attributes.put("XXsecchuaarchXX",               "\"x86\""                                                                                                    ); // Sec-Ch-Ua-Arch
        attributes.put("XXsecchuabitnessXX",            "\"64\""                                                                                                     ); // Sec-Ch-Ua-Bitness
        attributes.put("XXsecchuafullversionXX",        "\"100.0.4896.127\""                                                                                         ); // Sec-Ch-Ua-Full-Version
        attributes.put("XXsecchuafullversionlistXX",    "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"100.0.4896.127\", \"Google Chrome\";v=\"100.0.4896.127\"" ); // Sec-Ch-Ua-Full-Version-List
        attributes.put("XXsecchuamobileXX",             "?0"                                                                                                         ); // Sec-Ch-Ua-Mobile
        attributes.put("XXsecchuamodelXX",              "\"\""                                                                                                       ); // Sec-Ch-Ua-Model
        attributes.put("XXsecchuaplatformXX",           "\"Linux\""                                                                                                  ); // Sec-Ch-Ua-Platform
        attributes.put("XXsecchuaplatformversionXX",    "\"5.13.0\""                                                                                                 ); // Sec-Ch-Ua-Platform-Version
        attributes.put("XXsecchuawow64XX",              "?0"                                                                                                         ); // Sec-Ch-Ua-Wow64

        flowfile.putAttributes(attributes);

        // Run the enqueued content, it also takes an int = number of contents queued
        runner.run(1);

        // All results were processed with out failure
        runner.assertQueueEmpty();

        // If you need to read or do additional tests on results you can access the content
        List<MockFlowFile> results = runner.getFlowFilesForRelationship(ParseUserAgent.SUCCESS);
        assertEquals(1, results.size(), "Must be 1 match");
        MockFlowFile result = results.get(0);

        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "DeviceClass",                      "Desktop");
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "DeviceName",                       "Linux Desktop");
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "DeviceBrand",                      "Unknown");
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "DeviceCpu",                        "Intel x86_64");
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "DeviceCpuBits",                    "64");
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "OperatingSystemClass",             "Desktop");
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "OperatingSystemName",              "Linux");
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "OperatingSystemVersion",           "5.13.0");
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "OperatingSystemVersionMajor",      "5");
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "OperatingSystemNameVersion",       "Linux 5.13.0");
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "OperatingSystemNameVersionMajor",  "Linux 5");
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "LayoutEngineClass",                "Browser");
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "LayoutEngineName",                 "Blink");
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "LayoutEngineVersion",              "100.0");
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "LayoutEngineVersionMajor",         "100");
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "LayoutEngineNameVersion",          "Blink 100.0");
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "LayoutEngineNameVersionMajor",     "Blink 100");
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "AgentClass",                       "Browser");
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "AgentName",                        "Chrome");
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "AgentVersion",                     "100.0.4896.127");
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "AgentVersionMajor",                "100");
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "AgentNameVersion",                 "Chrome 100.0.4896.127");
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "AgentNameVersionMajor",            "Chrome 100");


        // Test attributes and content
        result.assertContentEquals(content);
    }

}
