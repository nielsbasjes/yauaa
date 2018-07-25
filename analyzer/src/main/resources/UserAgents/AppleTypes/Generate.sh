#!/bin/bash
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2018 Niels Basjes
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

INPUT=AppleTypes.csv
OUTPUT=../AppleTypes.yaml

if [ "Generate.sh" -ot "${OUTPUT}" ]; then
    if [ "${INPUT}" -ot "${OUTPUT}" ]; then
        echo "${OUTPUT} is up to date";
        exit;
    fi
fi

echo "Generating ${OUTPUT}";

(
echo "# ============================================="
echo "# THIS FILE WAS GENERATED; DO NOT EDIT MANUALLY"
echo "# ============================================="
echo "#"
echo "# Yet Another UserAgent Analyzer"
echo "# Copyright (C) 2013-2018 Niels Basjes"
echo "#"
echo "# Licensed under the Apache License, Version 2.0 (the \"License\");"
echo "# you may not use this file except in compliance with the License."
echo "# You may obtain a copy of the License at"
echo "#"
echo "# http://www.apache.org/licenses/LICENSE-2.0"
echo "#"
echo "# Unless required by applicable law or agreed to in writing, software"
echo "# distributed under the License is distributed on an \"AS IS\" BASIS,"
echo "# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied."
echo "# See the License for the specific language governing permissions and"
echo "# limitations under the License."
echo "#"

echo "config:"

echo "- lookup:"
echo "    name: 'AppleDeviceClass'"
echo "    map:"
echo "      \"iPhone\"     : \"Phone\""
echo "      \"iPad\"       : \"Tablet\""
echo "      \"iPod\"       : \"Phone\""
echo "      \"iPod touch\" : \"Phone\""

fgrep -v '#' "${INPUT}" | grep '[a-z]' | while read line
do
    key=$(echo "${line}" | cut -d'|' -f1)
    keyC=$(echo "${line}" | cut -d'|' -f1 | sed 's/,/C/g')
    keyS=$(echo "${line}" | cut -d'|' -f1 | sed 's/,1//g')
    deviceClass=$(echo "${line}" | cut -d'|' -f2)
    deviceName=$(echo "${line}" | cut -d'|' -f3)
    deviceVersion=$(echo "${line}" | cut -d'|' -f4-)
    echo "      \"${key}\" : \"${deviceClass}\""
    echo "      \"${keyC}\" : \"${deviceClass}\""
    if [[ ${keyS} != *","* ]]; then
        echo "      \"${keyS}\" : \"${deviceClass}\""
    fi
done

echo ""
echo "- lookup:"
echo "    name: 'AppleDeviceName'"
echo "    map:"
echo "      \"iPhone\"     : \"Apple iPhone\""
echo "      \"iPad\"       : \"Apple iPad\""
echo "      \"iPod\"       : \"Apple iPod\""
echo "      \"iPod touch\" : \"Apple iPod touch\""
fgrep -v '#' "${INPUT}" | grep '[a-z]' | while read line
do
    key=$(echo "${line}" | cut -d'|' -f1)
    keyC=$(echo "${line}" | cut -d'|' -f1 | sed 's/,/C/g')
    keyS=$(echo "${line}" | cut -d'|' -f1 | sed 's/,1//g')
    deviceClass=$(echo "${line}" | cut -d'|' -f2)
    deviceName=$(echo "${line}" | cut -d'|' -f3)
    deviceVersion=$(echo "${line}" | cut -d'|' -f4-)
    echo "      \"${key}\" : \"${deviceName}\""
    echo "      \"${keyC}\" : \"${deviceName}\""
    if [[ ${keyS} != *","* ]]; then
        echo "      \"${keyS}\" : \"${deviceName}\""
    fi
done

echo ""
echo "- lookup:"
echo "    name: 'AppleDeviceVersion'"
echo "    map:"
echo "      \"iPhone\"     : \"iPhone\""
echo "      \"iPad\"       : \"iPad\""
echo "      \"iPod\"       : \"iPod\""
echo "      \"iPod touch\" : \"iPod touch\""
fgrep -v '#' "${INPUT}" | grep '[a-z]' | while read line
do
    key=$(echo "${line}" | cut -d'|' -f1)
    keyC=$(echo "${line}" | cut -d'|' -f1 | sed 's/,/C/g')
    keyS=$(echo "${line}" | cut -d'|' -f1 | sed 's/,1//g')
    deviceClass=$(echo "${line}" | cut -d'|' -f2)
    deviceName=$(echo "${line}" | cut -d'|' -f3)
    deviceVersion=$(echo "${line}" | cut -d'|' -f4-)
    echo "      \"${key}\" : \"${deviceVersion}\""
    echo "      \"${keyC}\" : \"${deviceVersion}\""
    if [[ ${keyS} != *","* ]]; then
        echo "      \"${keyS}\" : \"${deviceVersion}\""
    fi
done

fgrep -v '#' "${INPUT}" | grep '[a-z]' | while read line
do
    key=$(echo "${line}" | cut -d'|' -f1)
    deviceClass=$(echo "${line}" | cut -d'|' -f2)
    deviceName=$(echo "${line}" | cut -d'|' -f3)
    deviceVersion=$(echo "${line}" | cut -d'|' -f4-)
echo "
- matcher:
    require:
    - 'agent.product.comments.entry.(1)text=\"${key}\"'
    extract:
    - 'DeviceBrand   : 110:\"Apple\"'
    - 'DeviceClass   : 110:\"${deviceClass}\"'
    - 'DeviceName    : 110:\"${deviceName}\"'
    - 'DeviceVersion : 110:\"${deviceVersion}\"'

- matcher:
    require:
    - 'agent.product.(1)name=\"${key}\"'
    extract:
    - 'DeviceBrand   : 111:\"Apple\"'
    - 'DeviceClass   : 111:\"${deviceClass}\"'
    - 'DeviceName    : 111:\"${deviceName}\"'
    - 'DeviceVersion : 111:\"${deviceVersion}\"'

- matcher:
    require:
    - 'agent.text=\"${key}\"'
    extract:
    - 'DeviceBrand   : 111:\"Apple\"'
    - 'DeviceClass   : 111:\"${deviceClass}\"'
    - 'DeviceName    : 111:\"${deviceName}\"'
    - 'DeviceVersion : 111:\"${deviceVersion}\"'
"
done
) > ${OUTPUT}
