#!/bin/bash
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
SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
TARGETDIR=$(cd "${SCRIPTDIR}/../../../resources/UserAgents" || exit 1; pwd)

INPUT="${SCRIPTDIR}/ProductNames.csv"
OUTPUT="${TARGETDIR}/ValidProductVersions-rules.yaml"

[ "$1" = "--force" ] && rm "${OUTPUT}"

if [ "Generate.sh" -ot "${OUTPUT}" ]; then
    if [ "${INPUT}" -ot "${OUTPUT}" ]; then
        echo "Up to date: ${OUTPUT}";
        exit;
    fi
fi

echo "Generating: ${OUTPUT}";

(
echo "# ============================================="
echo "# THIS FILE WAS GENERATED; DO NOT EDIT MANUALLY"
echo "# ============================================="
echo "#"
echo "# Yet Another UserAgent Analyzer"
echo "# Copyright (C) 2013-2022 Niels Basjes"
echo "#"
echo "# Licensed under the Apache License, Version 2.0 (the \"License\");"
echo "# you may not use this file except in compliance with the License."
echo "# You may obtain a copy of the License at"
echo "#"
echo "# https://www.apache.org/licenses/LICENSE-2.0"
echo "#"
echo "# Unless required by applicable law or agreed to in writing, software"
echo "# distributed under the License is distributed on an \"AS IS\" BASIS,"
echo "# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied."
echo "# See the License for the specific language governing permissions and"
echo "# limitations under the License."
echo "#"

echo "config:"

cat <<End-of-message
- set:
    name: 'NormalMozillaVersions'
    values:
      - '1.10'
      - '1.22'
      - '1.3'
      - '1.4'
      - '1.5'
      - '1.6'
      - '1.7'
      - '2.0'
      - '3.0'
      - '4.0'
      - '4.1'
      - '4.2'
      - '4.3'
      - '4.4'
      - '4.5'
      - '4.6'
      - '4.7'
      - '4.8'
      - '5.0'

- matcher:
    require:
      - 'agent.(1)product.name="Mozilla"'
      - 'IsNotInLookUpPrefix[NormalMozillaVersions;agent.(1)product.version]'
    extract:
      - 'RemarkablePattern                 :    10 :"No such version: Mozilla"'

- matcher:
    variable:
    - 'Product                             :agent.(1)product.(1)comments.entry.product.name="Windows NT"^'
    require:
    - '@Product[-4]!?WindowsDesktopOSName'
    extract:
    - 'RemarkablePattern                   :       20 :"No such version: Windows NT"'

End-of-message

grep -F -v '#' "${INPUT}" | grep . | while read -r name
do
    cat <<End-of-message
- matcher:
    variable:
    - 'AgentVersion: agent.product.name="${name}"^.(1)version'
    require:
    - 'IsNull[IsValidVersion[@AgentVersion]]'
    extract:
    - 'DeviceClass                         :    10000 :"Robot"'
    - 'RemarkablePattern                   :      101: "Not a version"'

- matcher:
    variable:
    - 'AgentVersion: agent.product.name="${name}"^.(2)version'
    require:
    - 'IsNull[@AgentVersion.uuid]'
    - 'IsNull[@AgentVersion.url]'
    - 'IsNull[IsValidVersion[@AgentVersion]]'
    extract:
    - 'RemarkablePattern                   :      100: "Not a version/Anti fingerprinting"'
End-of-message
done

) > "${OUTPUT}"
