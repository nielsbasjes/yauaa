#!/bin/bash
#
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2022 Niels Basjes
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

(cd quarkus && mvn package)

cat <<'EOF' > __tmp__TEST__DockerFile
FROM nielsbasjes/yauaa-quarkus:local
RUN mkdir -p UserAgents-Customized-Rules
ADD CompanyInternalUserAgents.yaml  UserAgents-Customized-Rules
EOF

docker build -t nielsbasjes/yauaa-quarkus:local-extrarules -f __tmp__TEST__DockerFile .

docker run --rm -p 8080:8080 nielsbasjes/yauaa-quarkus:local-extrarules

