# Manual testing

This script allows a real test of running the yauaa filter in logstash.

Because this is so slow this is only run manually.


# Config

    input {
      stdin {}
    }

    filter {
      yauaa {
        source => "message"
        fields => {
          "DeviceClass"                      => "userAgentDeviceClass"
          "DeviceName"                       => "userAgentDeviceName"
          "DeviceBrand"                      => "userAgentDeviceBrand"
          "OperatingSystemClass"             => "userAgentOperatingSystemClass"
          "OperatingSystemNameVersion"       => "userAgentOperatingSystemNameVersion"
          "LayoutEngineClass"                => "userAgentLayoutEngineClass"
          "LayoutEngineNameVersion"          => "userAgentLayoutEngineNameVersion"
          "AgentClass"                       => "userAgentAgentClass"
          "AgentName"                        => "userAgentAgentName"
          "AgentNameVersion"                 => "userAgentAgentNameVersion"
          "AgentNameVersionMajor"            => "userAgentAgentNameVersionMajor"
        }
      }
    }

    output {
      stdout {}
    }

# Input

    Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36

# Expected output

Note: Because the ordering of the fields changes all the time I have sorted them here for consistency.

    {
                                 "@timestamp" => 2020-01-28T12:41:02.995Z,
                                   "@version" => "1",
                                    "message" => "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36",
                                       "host" => "nielslaptop"
                       "userAgentDeviceClass" => "Phone",
                       "userAgentDeviceBrand" => "Google",
                        "userAgentDeviceName" => "Google Nexus 6",
              "userAgentOperatingSystemClass" => "Mobile",
        "userAgentOperatingSystemNameVersion" => "Android 7.0",
                 "userAgentLayoutEngineClass" => "Browser",
           "userAgentLayoutEngineNameVersion" => "Blink 53.0",
                        "userAgentAgentClass" => "Browser",
                         "userAgentAgentName" => "Chrome",
                  "userAgentAgentNameVersion" => "Chrome 53.0.2785.124",
             "userAgentAgentNameVersionMajor" => "Chrome 53",
    }

<!--
    Yet Another UserAgent Analyzer
    Copyright (C) 2013-2020 Niels Basjes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
-->
