#!/bin/bash
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2019 Niels Basjes
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
TARGETDIR=$(cd "${SCRIPTDIR}/../../../resources/UserAgents"; pwd)

INPUT=AmazonDevices.csv
OUTPUT="${TARGETDIR}/AmazonDevices.yaml"

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
echo "# Copyright (C) 2013-2019 Niels Basjes"
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

fgrep -v '#' "${INPUT}" | grep '[a-z]' | while read line
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
    - 'DeviceClass                         :   1001 :\"${deviceClass}\"'
    - 'DeviceName                          :   1002 :\"${deviceName}\"'
    - 'DeviceBrand                         :   1002 :\"${deviceBrand}\"'
    - 'OperatingSystemName                 :   1002 :\"FireOS\"' # All known Amazon devices run FireOS (a tweaked Android)

- matcher:
    require:
    - 'agent.product.comments.entry.product.name[1]=\"Android\"'
    - 'agent.product.comments.entry.product.name[1]=\"${tag}\"'
    extract:
    - 'DeviceClass                         :   1001 :\"${deviceClass}\"'
    - 'DeviceName                          :   1002 :\"${deviceName}\"'
    - 'DeviceBrand                         :   1002 :\"${deviceBrand}\"'
    - 'OperatingSystemName                 :   1002 :\"FireOS\"' # All known Amazon devices run FireOS (a tweaked Android)

- matcher:
    require:
    - 'IsNull[agent.product.name=\"Chrome\"]'
    extract:
    - 'DeviceClass                         :   1001 :\"${deviceClass}\"'
    - 'DeviceName                          :   1002 :\"${deviceName}\"'
    - 'DeviceBrand                         :   1002 :\"${deviceBrand}\"'
    - 'OperatingSystemVersionBuild         :     12 :agent.(1)product.(1)comments.entry[1-$((tagWords+1))]=\"${tag} Build\"@[$((tagWords+2))]'
    - 'OperatingSystemName                 :   1002 :\"FireOS\"' # All known Amazon devices run FireOS (a tweaked Android)

- matcher:
    require:
    - 'IsNull[agent.product.name=\"Chrome\"]'
    extract:
    - 'DeviceClass                         :   1001 :\"${deviceClass}\"'
    - 'DeviceName                          :   1002 :\"${deviceName}\"'
    - 'DeviceBrand                         :   1002 :\"${deviceBrand}\"'
    - 'OperatingSystemVersionBuild         :     11 :agent.(1)product.(1)comments.entry[1-$((tagWords))]=\"${tag}\"@[$((tagWords+1))]'
    - 'OperatingSystemName                 :   1002 :\"FireOS\"' # All known Amazon devices run FireOS (a tweaked Android)

"

done

) > ${OUTPUT}
