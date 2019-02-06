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
TARGETDIR=$(cd "${SCRIPTDIR}/.."; pwd)

INPUT=AllPossibleSteps.csv
INPUTTESTS=AllPossibleStepsTests.yaml
OUTPUT="${TARGETDIR}/AllPossibleSteps.yaml"

if [ "Generate.sh" -ot "${OUTPUT}" ]; then
    if [ "${INPUTTESTS}" -ot "${OUTPUT}" ]; then
        if [ "${INPUT}" -ot "${OUTPUT}" ]; then
            echo "Up to date: ${OUTPUT}";
            exit;
        fi
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
echo ""
echo "# ============================="
echo "#  Extract all fields possible"
echo "# ============================="
echo ""

fgrep -v '#' "${INPUT}" | grep '[a-z]' | while read line
do
    match="${line//agent/agent!=\"\"}"
    echo "- matcher:"
    echo "    extract:"
    echo "      - '${line}  :1:${match}'"
    echo ""
done

echo ""
echo "# ============================="
echo "#  Check for impossible matches"
echo "# ============================="
echo ""
echo "- matcher:"
echo "    extract:"
echo "      - 'MustBeOk  :1:\"Not OK\"'"

fgrep -v '#' "${INPUT}" | grep '[a-z]' | while read line
do
    match="${line//agent/agent!=\"\"}"
    echo "- matcher:"
    echo "    extract:"
    echo "      - 'MUST_BE_NULL_${line}.version.version  :1:${match}.version.version'"
    echo ""
done


echo "- matcher:"
echo "    require:"
fgrep -v '#' "${INPUT}" | grep '[a-z]' | while read line
do
    match="${line//agent/agent!=\"\"}"
    echo "      - 'IsNull[${match}.version.version]'"
done
echo "    extract:"
echo "    - 'MustBeOk :2:\"OK\"'"
fgrep -v '#' "${INPUT}" | grep '[a-z]' | while read line
do
    match="${line//agent/agent!=\"\"}"
    echo "    - 'MUST_BE_NULL_${line}.version.version  :2:\"<<<null>>>\"'"
done

cat "${INPUTTESTS}"

) > ${OUTPUT}
