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

package nl.basjes.parse.useragent.servlet.utils;

import nl.basjes.parse.useragent.Version;

public final class Constants {

    private Constants() {
    }

    public static final String GIT_REPO_URL = "https://github.com/nielsbasjes/yauaa/tree/" + Version.GIT_COMMIT_ID;

    public static final String            TEXT_XYAML_VALUE                = "text/x-yaml";

    public static final String            EXAMPLE_USERAGENT               =
        "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36";

    public static final String EXAMPLE_JSON = "[ {\n" +
        "  \"Useragent\": \"Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36\",\n" +
        "  \"DeviceClass\": \"Phone\",\n" +
        "  \"DeviceName\": \"Google Nexus 6\",\n" +
        "  \"DeviceBrand\": \"Google\",\n" +
        "  \"OperatingSystemClass\": \"Mobile\",\n" +
        "  \"OperatingSystemName\": \"Android\",\n" +
        "  \"OperatingSystemVersion\": \"7.0\",\n" +
        "  \"OperatingSystemVersionMajor\": \"7\",\n" +
        "  \"OperatingSystemNameVersion\": \"Android 7.0\",\n" +
        "  \"OperatingSystemNameVersionMajor\": \"Android 7\",\n" +
        "  \"OperatingSystemVersionBuild\": \"NBD90Z\",\n" +
        "  \"LayoutEngineClass\": \"Browser\",\n" +
        "  \"LayoutEngineName\": \"Blink\",\n" +
        "  \"LayoutEngineVersion\": \"53.0\",\n" +
        "  \"LayoutEngineVersionMajor\": \"53\",\n" +
        "  \"LayoutEngineNameVersion\": \"Blink 53.0\",\n" +
        "  \"LayoutEngineNameVersionMajor\": \"Blink 53\",\n" +
        "  \"AgentClass\": \"Browser\",\n" +
        "  \"AgentName\": \"Chrome\",\n" +
        "  \"AgentVersion\": \"53.0.2785.124\",\n" +
        "  \"AgentVersionMajor\": \"53\",\n" +
        "  \"AgentNameVersion\": \"Chrome 53.0.2785.124\",\n" +
        "  \"AgentNameVersionMajor\": \"Chrome 53\"\n" +
        "} ]";

    public static final String EXAMPLE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "  <Yauaa>\n" +
        "    <Useragent>Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36</Useragent>\n" +
        "    <DeviceClass>Phone</DeviceClass>\n" +
        "    <DeviceName>Google Nexus 6</DeviceName>\n" +
        "    <DeviceBrand>Google</DeviceBrand>\n" +
        "    <OperatingSystemClass>Mobile</OperatingSystemClass>\n" +
        "    <OperatingSystemName>Android</OperatingSystemName>\n" +
        "    <OperatingSystemVersion>7.0</OperatingSystemVersion>\n" +
        "    <OperatingSystemVersionMajor>7</OperatingSystemVersionMajor>\n" +
        "    <OperatingSystemNameVersion>Android 7.0</OperatingSystemNameVersion>\n" +
        "    <OperatingSystemNameVersionMajor>Android 7</OperatingSystemNameVersionMajor>\n" +
        "    <OperatingSystemVersionBuild>NBD90Z</OperatingSystemVersionBuild>\n" +
        "    <LayoutEngineClass>Browser</LayoutEngineClass>\n" +
        "    <LayoutEngineName>Blink</LayoutEngineName>\n" +
        "    <LayoutEngineVersion>53.0</LayoutEngineVersion>\n" +
        "    <LayoutEngineVersionMajor>53</LayoutEngineVersionMajor>\n" +
        "    <LayoutEngineNameVersion>Blink 53.0</LayoutEngineNameVersion>\n" +
        "    <LayoutEngineNameVersionMajor>Blink 53</LayoutEngineNameVersionMajor>\n" +
        "    <AgentClass>Browser</AgentClass>\n" +
        "    <AgentName>Chrome</AgentName>\n" +
        "    <AgentVersion>53.0.2785.124</AgentVersion>\n" +
        "    <AgentVersionMajor>53</AgentVersionMajor>\n" +
        "    <AgentNameVersion>Chrome 53.0.2785.124</AgentNameVersion>\n" +
        "    <AgentNameVersionMajor>Chrome 53</AgentNameVersionMajor>\n" +
        "  </Yauaa>";

    public static final String EXAMPLE_YAML =
        "- test:\n" +
        "    input:\n" +
        "      user_agent_string: 'Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36'\n" +
        "    expected:\n" +
        "      DeviceClass                           : 'Phone'\n" +
        "      DeviceName                            : 'Google Nexus 6'\n" +
        "      DeviceBrand                           : 'Google'\n" +
        "      OperatingSystemClass                  : 'Mobile'\n" +
        "      OperatingSystemName                   : 'Android'\n" +
        "      OperatingSystemVersion                : '7.0'\n" +
        "      OperatingSystemVersionMajor           : '7'\n" +
        "      OperatingSystemNameVersion            : 'Android 7.0'\n" +
        "      OperatingSystemNameVersionMajor       : 'Android 7'\n" +
        "      OperatingSystemVersionBuild           : 'NBD90Z'\n" +
        "      LayoutEngineClass                     : 'Browser'\n" +
        "      LayoutEngineName                      : 'Blink'\n" +
        "      LayoutEngineVersion                   : '53.0'\n" +
        "      LayoutEngineVersionMajor              : '53'\n" +
        "      LayoutEngineNameVersion               : 'Blink 53.0'\n" +
        "      LayoutEngineNameVersionMajor          : 'Blink 53'\n" +
        "      AgentClass                            : 'Browser'\n" +
        "      AgentName                             : 'Chrome'\n" +
        "      AgentVersion                          : '53.0.2785.124'\n" +
        "      AgentVersionMajor                     : '53'\n" +
        "      AgentNameVersion                      : 'Chrome 53.0.2785.124'\n" +
        "      AgentNameVersionMajor                 : 'Chrome 53'\n";

}
