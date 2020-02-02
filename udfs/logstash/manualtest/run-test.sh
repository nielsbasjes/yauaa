#!/bin/bash
#
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2020 Niels Basjes
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

cd "${DIR}" || exit 1

VERSION=$(fgrep logstash.version "${DIR}/../../../pom.xml" | cut -d'>' -f2 | cut -d'<' -f1)

[ -z "$VERSION" ] && echo "Unable to get the logstash version to use" && exit 1

[ -f "logstash-${VERSION}.tar.gz" ] || ( echo "Logstash ${VERSION}: Downloading" ; wget "https://artifacts.elastic.co/downloads/logstash/logstash-${VERSION}.tar.gz" )

[ -d "logstash-${VERSION}" ] || ( echo "Logstash ${VERSION}: Unpacking" ; tar xzf "logstash-${VERSION}.tar.gz" )

FILTERGEM=$(find "${DIR}/../logstash-filter/target/" -maxdepth 1 | grep 'logstash-filter-yauaa-.*.gem')

echo "Testing ${FILTERGEM}"

"${DIR}/logstash-${VERSION}/bin/logstash-plugin" install "${FILTERGEM}"


cat - > run-test.conf << EOF
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
EOF


echo "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36" | \
  "${DIR}/logstash-${VERSION}/bin/logstash" -f run-test.conf



