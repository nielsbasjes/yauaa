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

INPUT="${SCRIPTDIR}/AmazonDevices.csv"
OUTPUT="${TARGETDIR}/AmazonDevices.yaml"

[ "$1" = "--force" ] && rm "${OUTPUT}"

if [ "Generate.sh" -ot "${OUTPUT}" ]; then
    if [ "${INPUT}" -ot "${OUTPUT}" ]; then
        echo "Up to date: ${OUTPUT}";
        exit;
    fi
fi

echo "Generating: ${OUTPUT}";

(
echo '# =============================================
# THIS FILE WAS GENERATED; DO NOT EDIT MANUALLY
# =============================================
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
config:

# https://developer.amazon.com/docs/fire-tv/fire-os-overview.html
# Fire OS 7: Based on Android 9 (Pie, API level 28).
# Fire OS 6: Based on Android 7.1 (Nougat, API level 25).
# Fire OS 5: Based on Android 5.1 (Lollipop, API level 22)

- lookup:
    name: "AndroidToFireOSVersion"
    map:
      "5"   : "5"
      "7"   : "6"
      "9"   : "7"

- lookup:
    name: "AmazonDeviceTags"
    map:'

grep -F -v '#' "${INPUT}" | grep '[a-z]' | while read -r line
do
    tag=$(        echo "${line}" | sed 's@ *| *@|@g' | cut -d'|' -f1 )
    deviceName=$( echo "${line}" | sed 's@ *| *@|@g' | cut -d'|' -f2 )
    deviceBrand=$(echo "${line}" | sed 's@ *| *@|@g' | cut -d'|' -f3 )
    deviceClass=$(echo "${line}" | sed 's@ *| *@|@g' | cut -d'|' -f4 )
    echo "      '${tag}' : '${deviceClass}|${deviceBrand}|${deviceName}'"
done

echo "
- matcher:
    variable:
    - 'Device:LookUp[AmazonDeviceTags;agent.(1)product.(1)comments.(1-5)entry]'
    extract:
    - 'DeviceClass                         :     1001 :@Device[[1]]'
    - 'DeviceName                          :     1002 :@Device[[3]]'
    - 'DeviceBrand                         :     1002 :@Device[[2]]'
    - 'OperatingSystemName                 :     1002 :\"FireOS\"' # All known Amazon devices run FireOS (a tweaked Android)
    - 'OperatingSystemVersion              :      1001 :LookUp[AndroidToFireOSVersion;agent.(1)product.(1)comments.entry.(1)product.name[1]=\"Android\"^.version[1]]'
"


grep -F -v '#' "${INPUT}" | grep '[a-z]' | while read -r line
do
    tag=$(        echo "${line}" | sed 's@ *| *@|@g' | cut -d'|' -f1 )
    deviceName=$( echo "${line}" | sed 's@ *| *@|@g' | cut -d'|' -f2 )
    deviceBrand=$(echo "${line}" | sed 's@ *| *@|@g' | cut -d'|' -f3 )
    deviceClass=$(echo "${line}" | sed 's@ *| *@|@g' | cut -d'|' -f4 )

    tagWords=$(echo "${tag}" | sed 's@-@ @g' | wc -w)

echo "
- matcher:
    require:
    - 'agent.product.name=\"${tag}\"'
    extract:
    - 'DeviceClass                         :     1001 :\"${deviceClass}\"'
    - 'DeviceName                          :     1002 :\"${deviceName}\"'
    - 'DeviceBrand                         :     1002 :\"${deviceBrand}\"'
    - 'OperatingSystemName                 :     1002 :\"FireOS\"' # All known Amazon devices run FireOS (a tweaked Android)

- matcher:
    require:
    - 'agent.(1)product.(1)comments.entry.(1)product.name[1]=\"Android\"'
    - 'agent.(1)product.(1)comments.entry.(1)product.name[1]=\"${tag}\"'
    extract:
    - 'DeviceClass                         :     1001 :\"${deviceClass}\"'
    - 'DeviceName                          :     1002 :\"${deviceName}\"'
    - 'DeviceBrand                         :     1002 :\"${deviceBrand}\"'
    - 'OperatingSystemName                 :     1002 :\"FireOS\"' # All known Amazon devices run FireOS (a tweaked Android)

- matcher:
    require:
    - 'IsNull[agent.product.name=\"Chrome\"]'
    extract:
    - 'DeviceClass                         :     1001 :\"${deviceClass}\"'
    - 'DeviceName                          :     1002 :\"${deviceName}\"'
    - 'DeviceBrand                         :     1002 :\"${deviceBrand}\"'
    - 'OperatingSystemVersionBuild         :       12 :agent.(1)product.(1)comments.entry[1-$((tagWords+1))]=\"${tag} Build\"@[$((tagWords+2))]'
    - 'OperatingSystemName                 :     1002 :\"FireOS\"' # All known Amazon devices run FireOS (a tweaked Android)

- matcher:
    require:
    - 'IsNull[agent.product.name=\"Chrome\"]'
    extract:
    - 'DeviceClass                         :     1001 :\"${deviceClass}\"'
    - 'DeviceName                          :     1002 :\"${deviceName}\"'
    - 'DeviceBrand                         :     1002 :\"${deviceBrand}\"'
    - 'OperatingSystemVersionBuild         :       11 :agent.(1)product.(1)comments.entry[1-$((tagWords))]=\"${tag}\"@[$((tagWords+1))]'
    - 'OperatingSystemName                 :     1002 :\"FireOS\"' # All known Amazon devices run FireOS (a tweaked Android)

"

done

) > "${OUTPUT}"
