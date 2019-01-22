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

INPUT=CPUTypes.csv
OUTPUT="${TARGETDIR}/CPUTypes.yaml"

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

echo "
- matcher:
    extract:
    - 'DeviceCpu                           :    115 :LookUp[CPUArchitectures;agent.product.name=\"Sys_CPU\"^.version]'

- matcher:
    extract:
    - 'DeviceCpu                           :    115 :LookUp[CPUArchitectures;agent.(1-3)product.(1)comments.entry[1-2]]'
- matcher:
    extract:
    - 'DeviceCpu                           :    114 :LookUp[CPUArchitectures;agent.(1-3)product.(1)comments.entry[2-3]]'
- matcher:
    extract:
    - 'DeviceCpu                           :    113 :LookUp[CPUArchitectures;agent.(1-3)product.(1)comments.entry[3-4]]'
- matcher:
    extract:
    - 'DeviceCpu                           :    112 :LookUp[CPUArchitectures;agent.(1-3)product.(1)comments.entry[4-5]]'
- matcher:
    extract:
    - 'DeviceCpu                           :    111 :LookUp[CPUArchitectures;agent.(1-3)product.(1)comments.entry[5-6]]'

- matcher:
    extract:
    - 'DeviceCpu                           :    105 :LookUp[CPUArchitectures;agent.(1-3)product.(1)comments.entry[1]]'
- matcher:
    extract:
    - 'DeviceCpu                           :    104 :LookUp[CPUArchitectures;agent.(1-3)product.(1)comments.entry[2]]'
- matcher:
    extract:
    - 'DeviceCpu                           :    103 :LookUp[CPUArchitectures;agent.(1-3)product.(1)comments.entry[3]]'
- matcher:
    extract:
    - 'DeviceCpu                           :    102 :LookUp[CPUArchitectures;agent.(1-3)product.(1)comments.entry[4]]'
- matcher:
    extract:
    - 'DeviceCpu                           :    101 :LookUp[CPUArchitectures;agent.(1-3)product.(1)comments.entry[5]]'
- matcher:
    extract:
    - 'DeviceCpu                           :    106 :LookUp[CPUArchitectures;agent.(1-3)product.(1)comments.entry.product.version]'
"

echo "- lookup:"
echo "    name: 'CPUArchitectures'"
echo "    map:"
fgrep -v '#' "${INPUT}" | grep . | while read line
do
    cpu=$(echo "${line}" | sed 's/ *| */|/g' | cut -d'|' -f1)
    value=$(echo "${line}" | sed 's/ *| */|/g' | cut -d'|' -f2)
    echo "      \"${cpu}\" : \"${value}\""
done

echo "
- matcher:
    extract:
    - 'DeviceCpuBits                       :    117 :agent.product.name=\"App_Bitness\"^.version'

- matcher:
    extract:
    - 'DeviceCpuBits                       :    116 :LookUp[CPUArchitecturesBits;agent.product.name=\"Sys_CPU\"^.version]'

- matcher:
    extract:
    - 'DeviceCpuBits                       :    115 :LookUp[CPUArchitecturesBits;agent.(1-3)product.(1)comments.entry[1-2]]'
- matcher:
    extract:
    - 'DeviceCpuBits                       :    114 :LookUp[CPUArchitecturesBits;agent.(1-3)product.(1)comments.entry[2-3]]'
- matcher:
    extract:
    - 'DeviceCpuBits                       :    113 :LookUp[CPUArchitecturesBits;agent.(1-3)product.(1)comments.entry[3-4]]'
- matcher:
    extract:
    - 'DeviceCpuBits                       :    112 :LookUp[CPUArchitecturesBits;agent.(1-3)product.(1)comments.entry[4-5]]'
- matcher:
    extract:
    - 'DeviceCpuBits                       :    111 :LookUp[CPUArchitecturesBits;agent.(1-3)product.(1)comments.entry[5-6]]'

- matcher:
    extract:
    - 'DeviceCpuBits                       :    105 :LookUp[CPUArchitecturesBits;agent.(1-3)product.(1)comments.entry[1]]'
- matcher:
    extract:
    - 'DeviceCpuBits                       :    104 :LookUp[CPUArchitecturesBits;agent.(1-3)product.(1)comments.entry[2]]'
- matcher:
    extract:
    - 'DeviceCpuBits                       :    103 :LookUp[CPUArchitecturesBits;agent.(1-3)product.(1)comments.entry[3]]'
- matcher:
    extract:
    - 'DeviceCpuBits                       :    102 :LookUp[CPUArchitecturesBits;agent.(1-3)product.(1)comments.entry[4]]'
- matcher:
    extract:
    - 'DeviceCpuBits                       :    101 :LookUp[CPUArchitecturesBits;agent.(1-3)product.(1)comments.entry[5]]'
- matcher:
    extract:
    - 'DeviceCpuBits                       :    106 :LookUp[CPUArchitecturesBits;agent.(1-3)product.(1)comments.entry.product.version]'
"

echo "- lookup:"
echo "    name: 'CPUArchitecturesBits'"
echo "    map:"
fgrep -v '#' "${INPUT}" | grep . | while read line
do
    cpu=$(echo "${line}" | sed 's/ *| */|/g' | cut -d'|' -f1)
    value=$(echo "${line}" | sed 's/ *| */|/g' | cut -d'|' -f3)
    echo "      \"${cpu}\" : \"${value}\""
done


) > ${OUTPUT}
