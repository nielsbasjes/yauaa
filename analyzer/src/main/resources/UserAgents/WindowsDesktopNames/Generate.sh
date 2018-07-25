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

INPUT=WindowsDesktop.csv
OUTPUT=../WindowsDesktopLookups.yaml

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
echo "    name: 'WindowsDesktopOSName'"
echo "    map:"
fgrep -v '#' "${INPUT}" | grep  . | while read line
do
    tag=$(        echo "${line}" | sed 's@ *| *@|@g' | cut -d'|' -f1)
    osname=$(     echo "${line}" | sed 's@ *| *@|@g' | cut -d'|' -f2)
    echo "      \"${tag}\" : \"${osname}\""
done

echo "- lookup:"
echo "    name: 'WindowsDesktopOSVersion'"
echo "    map:"
fgrep -v '#' "${INPUT}" | grep  . | while read line
do
    tag=$(        echo "${line}" | sed 's@ *| *@|@g' | cut -d'|' -f1)
    osversion=$(  echo "${line}" | sed 's@ *| *@|@g' | cut -d'|' -f3)
    echo "      \"${tag}\" : \"${osversion}\""
done

echo "- lookup:"
echo "    name: 'WindowsDesktopOSCpuBits'"
echo "    map:"
fgrep -v '#' "${INPUT}" | grep  . | while read line
do
    tag=$(        echo "${line}" | sed 's@ *| *@|@g' | cut -d'|' -f1)
    cpubits=$(    echo "${line}" | sed 's@ *| *@|@g' | cut -d'|' -f4)
    if [ ! -z "${cpubits}" ];
    then
        echo "      \"${tag}\" : \"${cpubits}\""
    fi
done

) > ${OUTPUT}
