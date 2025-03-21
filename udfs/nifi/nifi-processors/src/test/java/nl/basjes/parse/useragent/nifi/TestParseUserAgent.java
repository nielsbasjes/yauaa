/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2025 Niels Basjes
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
import static org.junit.jupiter.api.Assertions.assertEquals;

// CHECKSTYLE.OFF: ParenPad
class TestParseUserAgent {

    private static final String TEST_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.82 Safari/537.36";

    private TestRunner runner;

    @BeforeEach
    void before(){
        // Generate a test runner to mock a processor in a flow
        runner = TestRunners.newTestRunner(new ParseUserAgent());
    }

    @Test
    void testParserFull() {
        // Content to be mock a json file
        String content = "CONTENT:>>" + TEST_USER_AGENT + "<<";

        // Add properties
        runner.setProperty(FIELD_PREFIX + "DeviceClass",                      "true");
        runner.setProperty(FIELD_PREFIX + "DeviceName",                       "true");
        runner.setProperty(FIELD_PREFIX + "OperatingSystemClass",             "true");
        runner.setProperty(FIELD_PREFIX + "OperatingSystemName",              "true");
        runner.setProperty(FIELD_PREFIX + "OperatingSystemVersion",           "true");
        runner.setProperty(FIELD_PREFIX + "LayoutEngineClass",                "true");
        runner.setProperty(FIELD_PREFIX + "LayoutEngineName",                 "true");
        runner.setProperty(FIELD_PREFIX + "LayoutEngineVersion",              "true");
        runner.setProperty(FIELD_PREFIX + "AgentClass",                       "true");
        runner.setProperty(FIELD_PREFIX + "AgentName",                        "true");
        runner.setProperty(FIELD_PREFIX + "AgentVersion",                     "true");
        runner.setProperty(FIELD_PREFIX + "AgentVersionMajor",                "true");
        runner.setProperty(FIELD_PREFIX + "AgentNameVersion",                 "true");
        runner.setProperty(FIELD_PREFIX + "AgentNameVersionMajor",            "true");
        runner.setProperty(FIELD_PREFIX + "AgentBuild",                       "true");
        runner.setProperty(FIELD_PREFIX + "AgentInformationEmail",            "true");
        runner.setProperty(FIELD_PREFIX + "AgentInformationUrl",              "true");
        runner.setProperty(FIELD_PREFIX + "AgentLanguage",                    "true");
        runner.setProperty(FIELD_PREFIX + "AgentSecurity",                    "true");
        runner.setProperty(FIELD_PREFIX + "AgentUuid",                        "true");
        runner.setProperty(FIELD_PREFIX + "DeviceBrand",                      "true");
        runner.setProperty(FIELD_PREFIX + "DeviceCpu",                        "true");
        runner.setProperty(FIELD_PREFIX + "DeviceFirmwareVersion",            "true");
        runner.setProperty(FIELD_PREFIX + "DeviceVersion",                    "true");
        runner.setProperty(FIELD_PREFIX + "FacebookCarrier",                  "true");
        runner.setProperty(FIELD_PREFIX + "FacebookDeviceClass",              "true");
        runner.setProperty(FIELD_PREFIX + "FacebookDeviceName",               "true");
        runner.setProperty(FIELD_PREFIX + "FacebookDeviceVersion",            "true");
        runner.setProperty(FIELD_PREFIX + "FacebookFBOP",                     "true");
        runner.setProperty(FIELD_PREFIX + "FacebookFBSS",                     "true");
        runner.setProperty(FIELD_PREFIX + "FacebookOperatingSystemName",      "true");
        runner.setProperty(FIELD_PREFIX + "FacebookOperatingSystemVersion",   "true");
        runner.setProperty(FIELD_PREFIX + "HackerAttackVector",               "true");
        runner.setProperty(FIELD_PREFIX + "HackerToolkit",                    "true");
        runner.setProperty(FIELD_PREFIX + "KoboAffiliate",                    "true");
        runner.setProperty(FIELD_PREFIX + "KoboPlatformId",                   "true");
        runner.setProperty(FIELD_PREFIX + "LayoutEngineBuild",                "true");
        runner.setProperty(FIELD_PREFIX + "OperatingSystemVersionBuild",      "true");

        // Add the content to the runner (just because we 'should' have some content).
        MockFlowFile flowfile = runner.enqueue(content);
        Map<String, String> attributes = new HashMap<>();
        attributes.put(ParseUserAgent.USERAGENTSTRING_ATTRIBUTENAME, TEST_USER_AGENT);
        flowfile.putAttributes(attributes);

        // Run the enqueued content, it also takes an int = number of contents queued
        runner.run(1);

        // All results were processed with out failure
        runner.assertQueueEmpty();

        // If you need to read or do additional tests on results you can access the content
        List<MockFlowFile> results = runner.getFlowFilesForRelationship(ParseUserAgent.SUCCESS);
        assertEquals(1, results.size(), "Must be 1 match");
        MockFlowFile result = results.get(0);
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "DeviceClass",                      "Desktop"             );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "DeviceName",                       "Linux Desktop"       );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "OperatingSystemClass",             "Desktop"             );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "OperatingSystemName",              "Linux"               );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "OperatingSystemVersion",           "??"                  );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "LayoutEngineClass",                "Browser"             );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "LayoutEngineName",                 "Blink"               );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "LayoutEngineVersion",              "48.0"                );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "AgentClass",                       "Browser"             );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "AgentName",                        "Chrome"              );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "AgentVersion",                     "48.0.2564.82"        );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "AgentVersionMajor",                "48"                  );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "AgentNameVersion",                 "Chrome 48.0.2564.82" );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "AgentNameVersionMajor",            "Chrome 48"           );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "AgentBuild",                       "Unknown"             );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "AgentInformationEmail",            "Unknown"             );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "AgentInformationUrl",              "Unknown"             );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "AgentLanguage",                    "Unknown"             );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "AgentSecurity",                    "Unknown"             );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "AgentUuid",                        "Unknown"             );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "DeviceBrand",                      "Unknown"             );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "DeviceCpu",                        "Intel x86_64"        );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "DeviceFirmwareVersion",            "??"                  );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "DeviceVersion",                    "??"                  );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "FacebookCarrier",                  "Unknown"             );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "FacebookDeviceClass",              "Unknown"             );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "FacebookDeviceName",               "Unknown"             );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "FacebookDeviceVersion",            "??"                  );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "FacebookFBOP",                     "Unknown"             );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "FacebookFBSS",                     "Unknown"             );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "FacebookOperatingSystemName",      "Unknown"             );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "FacebookOperatingSystemVersion",   "??"                  );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "HackerAttackVector",               "Unknown"             );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "HackerToolkit",                    "Unknown"             );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "KoboAffiliate",                    "Unknown"             );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "KoboPlatformId",                   "Unknown"             );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "LayoutEngineBuild",                "Unknown"             );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "OperatingSystemVersionBuild",      "??"                  );

        // Test attributes and content
        result.assertContentEquals(content);
    }

    @Test
    void testParserPartial() {
        // Content to be mock a json file
        String content = "CONTENT:>>" + TEST_USER_AGENT + "<<";

        // Add properties
        runner.setProperty(FIELD_PREFIX + "DeviceClass",                      "true");
        runner.setProperty(FIELD_PREFIX + "OperatingSystemName",              "true");
        runner.setProperty(FIELD_PREFIX + "OperatingSystemVersion",           "true");
        runner.setProperty(FIELD_PREFIX + "AgentClass",                       "true");
        runner.setProperty(FIELD_PREFIX + "AgentName",                        "true");
        runner.setProperty(FIELD_PREFIX + "AgentNameVersionMajor",            "true");

        // Add the content to the runner (just because we 'should' have some content).
        MockFlowFile flowfile = runner.enqueue(content);
        Map<String, String> attributes = new HashMap<>();
        attributes.put(ParseUserAgent.USERAGENTSTRING_ATTRIBUTENAME, TEST_USER_AGENT);
        flowfile.putAttributes(attributes);

        // Run the enqueued content, it also takes an int = number of contents queued
        runner.run(1);

        // All results were processed with out failure
        runner.assertQueueEmpty();

        // If you need to read or do additional tests on results you can access the content
        List<MockFlowFile> results = runner.getFlowFilesForRelationship(ParseUserAgent.SUCCESS);
        assertEquals(1, results.size(), "Must be 1 match");
        MockFlowFile result = results.get(0);
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "DeviceClass",            "Desktop"      );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "OperatingSystemName",    "Linux"        );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "OperatingSystemVersion", "??"           );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "AgentClass",             "Browser"      );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "AgentName",              "Chrome"       );
        result.assertAttributeEquals(ATTRIBUTE_PREFIX + "AgentNameVersionMajor",  "Chrome 48"    );

        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "DeviceName"                      );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "OperatingSystemClass"            );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "LayoutEngineClass"               );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "LayoutEngineName"                );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "LayoutEngineVersion"             );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "AgentVersion"                    );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "AgentVersionMajor"               );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "AgentNameVersion"                );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "AgentBuild"                      );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "AgentInformationEmail"           );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "AgentInformationUrl"             );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "AgentLanguage"                   );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "AgentSecurity"                   );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "AgentUuid"                       );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "DeviceBrand"                     );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "DeviceCpu"                       );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "DeviceFirmwareVersion"           );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "DeviceVersion"                   );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "FacebookCarrier"                 );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "FacebookDeviceClass"             );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "FacebookDeviceName"              );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "FacebookDeviceVersion"           );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "FacebookFBOP"                    );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "FacebookFBSS"                    );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "FacebookOperatingSystemName"     );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "FacebookOperatingSystemVersion"  );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "HackerAttackVector"              );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "HackerToolkit"                   );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "KoboAffiliate"                   );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "KoboPlatformId"                  );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "LayoutEngineBuild"               );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "OperatingSystemVersionBuild"     );

        // Test attributes and content
        result.assertContentEquals(content);
    }

    @Test
    void testParserMissingInput() {
        // Content to be mock a json file
        String content = "CONTENT:>>" + TEST_USER_AGENT + "<<";
        // Add properties
        runner.setProperty(FIELD_PREFIX + "DeviceClass",                      "true");
        runner.setProperty(FIELD_PREFIX + "OperatingSystemName",              "true");
        runner.setProperty(FIELD_PREFIX + "OperatingSystemVersion",           "true");
        runner.setProperty(FIELD_PREFIX + "AgentClass",                       "true");
        runner.setProperty(FIELD_PREFIX + "AgentName",                        "true");
        runner.setProperty(FIELD_PREFIX + "AgentNameVersionMajor",            "true");

        // Add the content to the runner (just because we 'should' have some content).
        runner.enqueue(content);

        // We deliberatly DO NOT add the required attribute

        // Run the enqueued content, it also takes an int = number of contents queued
        runner.run(1);

        // All results were processed with out failure
        runner.assertQueueEmpty();

        List<MockFlowFile> results = runner.getFlowFilesForRelationship(ParseUserAgent.SUCCESS);
        assertEquals(0, results.size(), "None at success");

        results = runner.getFlowFilesForRelationship(ParseUserAgent.MISSING);
        assertEquals(1, results.size(), "Must be 1 match");
        MockFlowFile result = results.get(0);
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "DeviceClass"                    );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "OperatingSystemName"            );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "OperatingSystemVersion"         );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "AgentClass"                     );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "AgentName"                      );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "AgentNameVersionMajor"          );

        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "DeviceName"                      );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "OperatingSystemClass"            );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "LayoutEngineClass"               );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "LayoutEngineName"                );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "LayoutEngineVersion"             );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "AgentVersion"                    );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "AgentVersionMajor"               );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "AgentNameVersion"                );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "AgentBuild"                      );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "AgentInformationEmail"           );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "AgentInformationUrl"             );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "AgentLanguage"                   );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "AgentSecurity"                   );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "AgentUuid"                       );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "DeviceBrand"                     );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "DeviceCpu"                       );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "DeviceFirmwareVersion"           );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "DeviceVersion"                   );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "FacebookCarrier"                 );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "FacebookDeviceClass"             );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "FacebookDeviceName"              );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "FacebookDeviceVersion"           );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "FacebookFBOP"                    );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "FacebookFBSS"                    );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "FacebookOperatingSystemName"     );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "FacebookOperatingSystemVersion"  );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "HackerAttackVector"              );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "HackerToolkit"                   );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "KoboAffiliate"                   );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "KoboPlatformId"                  );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "LayoutEngineBuild"               );
        result.assertAttributeNotExists(ATTRIBUTE_PREFIX + "OperatingSystemVersionBuild"     );

        // Test attributes and content
        result.assertContentEquals(content);
    }

}
