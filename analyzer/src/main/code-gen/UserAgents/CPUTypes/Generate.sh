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

INPUT="${SCRIPTDIR}/CPUTypes.csv"
OUTPUT="${TARGETDIR}/CPUTypes.yaml"

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

echo "
- matcher:
    extract:
      - 'DeviceCpuBits :      117 :agent.product.name=\"App_Bitness\"^.version'

- matcher:
    variable:
      - 'CPUInfo:LookUp[CPUArchitectures;agent.product.name=\"Sys_CPU\"^.version]'
    extract:
      - 'DeviceCpu     :      116 :@CPUInfo[[1]]'
      - 'DeviceCpuBits :      116 :@CPUInfo[[2]]'
#      - 'DeviceClass   :        0 :@CPUInfo[[3]]'

- matcher:
    variable:
      - 'CPUInfo:LookUp[CPUArchitectures;agent.(1-3)product.(1)comments.entry[1-2]]'
    extract:
      - 'DeviceCpu     :      115 :@CPUInfo[[1]]'
      - 'DeviceCpuBits :      115 :@CPUInfo[[2]]'
#      - 'DeviceClass   :        0 :@CPUInfo[[3]]'

- matcher:
    variable:
      - 'CPUInfo:LookUp[CPUArchitectures;agent.(1-3)product.(1)comments.entry[2-3]]'
    extract:
      - 'DeviceCpu     :      114 :@CPUInfo[[1]]'
      - 'DeviceCpuBits :      114 :@CPUInfo[[2]]'
#      - 'DeviceClass   :        0 :@CPUInfo[[3]]'

- matcher:
    variable:
      - 'CPUInfo:LookUp[CPUArchitectures;agent.(1-3)product.(1)comments.entry[3-4]]'
    extract:
      - 'DeviceCpu     :      113 :@CPUInfo[[1]]'
      - 'DeviceCpuBits :      113 :@CPUInfo[[2]]'
#      - 'DeviceClass   :        0 :@CPUInfo[[3]]'

- matcher:
    variable:
      - 'CPUInfo:LookUp[CPUArchitectures;agent.(1-3)product.(1)comments.entry[4-5]]'
    extract:
      - 'DeviceCpu     :      112 :@CPUInfo[[1]]'
      - 'DeviceCpuBits :      112 :@CPUInfo[[2]]'
#      - 'DeviceClass   :        0 :@CPUInfo[[3]]'

- matcher:
    variable:
      - 'CPUInfo:LookUp[CPUArchitectures;agent.(1-3)product.(1)comments.entry[5-6]]'
    extract:
      - 'DeviceCpu     :      111 :@CPUInfo[[1]]'
      - 'DeviceCpuBits :      111 :@CPUInfo[[2]]'
#      - 'DeviceClass   :        0 :@CPUInfo[[3]]'

- matcher:
    variable:
      - 'CPUInfo:LookUp[CPUArchitectures;agent.(1-3)product.(1)comments.entry.(1-2)product.(1-2)version]'
    extract:
      - 'DeviceCpu     :      106 :@CPUInfo[[1]]'
      - 'DeviceCpuBits :      106 :@CPUInfo[[2]]'
#      - 'DeviceClass   :        0 :@CPUInfo[[3]]'

- matcher:
    variable:
      - 'CPUInfo:LookUp[CPUArchitectures;agent.(1-3)product.(1)comments.entry[1]]'
    extract:
      - 'DeviceCpu     :      105 :@CPUInfo[[1]]'
      - 'DeviceCpuBits :      105 :@CPUInfo[[2]]'
#      - 'DeviceClass   :        0 :@CPUInfo[[3]]'

- matcher:
    variable:
      - 'CPUInfo:LookUp[CPUArchitectures;agent.(1-3)product.(1)comments.entry[2]]'
    extract:
      - 'DeviceCpu     :      104 :@CPUInfo[[1]]'
      - 'DeviceCpuBits :      104 :@CPUInfo[[2]]'
#      - 'DeviceClass   :        0 :@CPUInfo[[3]]'

- matcher:
    variable:
      - 'CPUInfo:LookUp[CPUArchitectures;agent.(1-3)product.(1)comments.entry[3]]'
    extract:
      - 'DeviceCpu     :      103 :@CPUInfo[[1]]'
      - 'DeviceCpuBits :      103 :@CPUInfo[[2]]'
#      - 'DeviceClass   :        0 :@CPUInfo[[3]]'

- matcher:
    variable:
      - 'CPUInfo:LookUp[CPUArchitectures;agent.(1-3)product.(1)comments.entry[4]]'
    extract:
      - 'DeviceCpu     :      102 :@CPUInfo[[1]]'
      - 'DeviceCpuBits :      102 :@CPUInfo[[2]]'
#      - 'DeviceClass   :        0 :@CPUInfo[[3]]'

- matcher:
    variable:
      - 'CPUInfo:LookUp[CPUArchitectures;agent.(1-3)product.(1)comments.entry[5]]'
    extract:
      - 'DeviceCpu     :      101 :@CPUInfo[[1]]'
      - 'DeviceCpuBits :      101 :@CPUInfo[[2]]'
#      - 'DeviceClass   :        0 :@CPUInfo[[3]]'


- matcher:
    variable:
      - 'CPUInfo:LookUp[CPUArchitectures;agent.product.(1-2)version]'
    extract:
      - 'DeviceCpu     :      100 :@CPUInfo[[1]]'
      - 'DeviceCpuBits :      100 :@CPUInfo[[2]]'
#      - 'DeviceClass   :        0 :@CPUInfo[[3]]'
"

echo "- lookup:"
echo "    name: 'CPUArchitectures'"
echo "    map:"
grep -F -v '#' "${INPUT}" | grep . | while read -r line
do
    cpuTag=$(echo "${line}" | sed 's/ *| */|/g' | cut -d'|' -f1)
    name=$(echo "${line}" | sed 's/ *| */|/g' | cut -d'|' -f2)
    bits=$(echo "${line}" | sed 's/ *| */|/g' | cut -d'|' -f3)
    deviceClass=$(echo "${line}" | sed 's/ *| */|/g' | cut -d'|' -f4)
    echo "      \"${cpuTag}\" : \"${name}|${bits}|${deviceClass}\""
done


) > "${OUTPUT}"
