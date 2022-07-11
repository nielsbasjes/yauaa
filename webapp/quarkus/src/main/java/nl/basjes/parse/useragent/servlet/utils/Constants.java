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

package nl.basjes.parse.useragent.servlet.utils;

import nl.basjes.parse.useragent.Version;

public final class Constants {

    private Constants() {
    }

    public static final String GIT_REPO_URL = "https://github.com/nielsbasjes/yauaa/tree/" + Version.GIT_COMMIT_ID;

    public static final String TEXT_XYAML_VALUE       = "text/x-yaml";

    public static final String EXAMPLE_USERAGENT      =
        "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36";

    public static final String EXAMPLE_USERAGENT_2    =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.63 Safari/537.36";

    public static final String EXAMPLE_TWO_USERAGENTS = EXAMPLE_USERAGENT + '\n' + EXAMPLE_USERAGENT_2;


    public static final String EXAMPLE_JSON = """
        [ {
          "Useragent": "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36",
          "DeviceClass": "Phone",
          "DeviceName": "Google Nexus 6",
          "DeviceBrand": "Google",
          "OperatingSystemClass": "Mobile",
          "OperatingSystemName": "Android",
          "OperatingSystemVersion": "7.0",
          "OperatingSystemVersionMajor": "7",
          "OperatingSystemNameVersion": "Android 7.0",
          "OperatingSystemNameVersionMajor": "Android 7",
          "OperatingSystemVersionBuild": "NBD90Z",
          "LayoutEngineClass": "Browser",
          "LayoutEngineName": "Blink",
          "LayoutEngineVersion": "53.0",
          "LayoutEngineVersionMajor": "53",
          "LayoutEngineNameVersion": "Blink 53.0",
          "LayoutEngineNameVersionMajor": "Blink 53",
          "AgentClass": "Browser",
          "AgentName": "Chrome",
          "AgentVersion": "53.0.2785.124",
          "AgentVersionMajor": "53",
          "AgentNameVersion": "Chrome 53.0.2785.124",
          "AgentNameVersionMajor": "Chrome 53"
        } ]""";

    public static final String EXAMPLE_XML = """
        <?xml version="1.0" encoding="UTF-8"?>
          <Yauaa>
            <Useragent>Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36</Useragent>
            <DeviceClass>Phone</DeviceClass>
            <DeviceName>Google Nexus 6</DeviceName>
            <DeviceBrand>Google</DeviceBrand>
            <OperatingSystemClass>Mobile</OperatingSystemClass>
            <OperatingSystemName>Android</OperatingSystemName>
            <OperatingSystemVersion>7.0</OperatingSystemVersion>
            <OperatingSystemVersionMajor>7</OperatingSystemVersionMajor>
            <OperatingSystemNameVersion>Android 7.0</OperatingSystemNameVersion>
            <OperatingSystemNameVersionMajor>Android 7</OperatingSystemNameVersionMajor>
            <OperatingSystemVersionBuild>NBD90Z</OperatingSystemVersionBuild>
            <LayoutEngineClass>Browser</LayoutEngineClass>
            <LayoutEngineName>Blink</LayoutEngineName>
            <LayoutEngineVersion>53.0</LayoutEngineVersion>
            <LayoutEngineVersionMajor>53</LayoutEngineVersionMajor>
            <LayoutEngineNameVersion>Blink 53.0</LayoutEngineNameVersion>
            <LayoutEngineNameVersionMajor>Blink 53</LayoutEngineNameVersionMajor>
            <AgentClass>Browser</AgentClass>
            <AgentName>Chrome</AgentName>
            <AgentVersion>53.0.2785.124</AgentVersion>
            <AgentVersionMajor>53</AgentVersionMajor>
            <AgentNameVersion>Chrome 53.0.2785.124</AgentNameVersion>
            <AgentNameVersionMajor>Chrome 53</AgentNameVersionMajor>
          </Yauaa>""";

    public static final String EXAMPLE_YAML =
        """
            - test:
                input:
                  user_agent_string: 'Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36'
                expected:
                  DeviceClass                           : 'Phone'
                  DeviceName                            : 'Google Nexus 6'
                  DeviceBrand                           : 'Google'
                  OperatingSystemClass                  : 'Mobile'
                  OperatingSystemName                   : 'Android'
                  OperatingSystemVersion                : '7.0'
                  OperatingSystemVersionMajor           : '7'
                  OperatingSystemNameVersion            : 'Android 7.0'
                  OperatingSystemNameVersionMajor       : 'Android 7'
                  OperatingSystemVersionBuild           : 'NBD90Z'
                  LayoutEngineClass                     : 'Browser'
                  LayoutEngineName                      : 'Blink'
                  LayoutEngineVersion                   : '53.0'
                  LayoutEngineVersionMajor              : '53'
                  LayoutEngineNameVersion               : 'Blink 53.0'
                  LayoutEngineNameVersionMajor          : 'Blink 53'
                  AgentClass                            : 'Browser'
                  AgentName                             : 'Chrome'
                  AgentVersion                          : '53.0.2785.124'
                  AgentVersionMajor                     : '53'
                  AgentNameVersion                      : 'Chrome 53.0.2785.124'
                  AgentNameVersionMajor                 : 'Chrome 53'
            """;

}
