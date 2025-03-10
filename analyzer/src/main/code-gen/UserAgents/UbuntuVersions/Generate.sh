#!/bin/bash
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2025 Niels Basjes
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

INPUT="${SCRIPTDIR}/UbuntuVersions.csv"
OUTPUT="${TARGETDIR}/UbuntuVersionLookups.yaml"

[ "$1" = "--force" ] && rm "${OUTPUT}"

if [ "Generate.sh" -ot "${OUTPUT}" ]; then
    if [ "${INPUT}" -ot "${OUTPUT}" ]; then
        echo "Up to date: ${OUTPUT}";
        exit;
    fi
fi

echo "Generating: ${OUTPUT}";

(
echo '# $schema: https://yauaa.basjes.nl/v1/YauaaConfig.json'
echo "# ============================================="
echo "# THIS FILE WAS GENERATED; DO NOT EDIT MANUALLY"
echo "# ============================================="
echo "#"
echo "# Yet Another UserAgent Analyzer"
echo "# Copyright (C) 2013-2025 Niels Basjes"
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

echo "- lookup:"
echo "    name: 'UbuntuOSName'"
echo "    map:"
grep -F -v '#' "${INPUT}" | grep  . | while read -r line
do
    firstname=$(   echo "${line}" | sed 's@ *| *@|@g' | cut -d'|' -f1 | cut -d' ' -f1)
    name=$(        echo "${line}" | sed 's@ *| *@|@g' | cut -d'|' -f1)
    version=$(     echo "${line}" | sed 's@ *| *@|@g' | cut -d'|' -f2)
    echo "      \"${firstname}\"        : \"Ubuntu\""
    echo "      \"${firstname}-security\" : \"Ubuntu\""
    echo "      \"${name}\"             : \"Ubuntu\""
    echo "      \"ubuntu-${firstname}\" : \"Ubuntu\""
    echo "      \"ubuntu-${version}\"   : \"Ubuntu\""
    echo "      \"ubuntu${version}\"    : \"Ubuntu\""
done

echo ""
echo "- lookup:"
echo "    name: 'UbuntuOSVersion'"
echo "    map:"
grep -F -v '#' "${INPUT}" | grep  . | while read -r line
do
    firstname=$(   echo "${line}" | sed 's@ *| *@|@g' | cut -d'|' -f1 | cut -d' ' -f1)
    name=$(        echo "${line}" | sed 's@ *| *@|@g' | cut -d'|' -f1)
    version=$(     echo "${line}" | sed 's@ *| *@|@g' | cut -d'|' -f2)
    echo "      \"${firstname}\"        : \"${version}\""
    echo "      \"${firstname}-security\"  : \"${version} Security\""
    echo "      \"${name}\"             : \"${version}\""
    echo "      \"ubuntu-${firstname}\" : \"${version}\""
    echo "      \"ubuntu-${version}\"   : \"${version}\""
    echo "      \"ubuntu${version}\"    : \"${version}\""
done

) >"${OUTPUT}"
