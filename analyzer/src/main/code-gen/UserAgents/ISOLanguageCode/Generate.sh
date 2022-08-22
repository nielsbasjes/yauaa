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

INPUT1="${SCRIPTDIR}/ISOLanguageCodes.csv"
INPUT2="${SCRIPTDIR}/iso-639-3.tab"
UNWANTED="${SCRIPTDIR}/unwanted-language-codes.txt"
OUTPUT="${TARGETDIR}/ISOLanguageCode.yaml"

[ "$1" = "--force" ] && rm "${OUTPUT}"

if [ "Generate.sh" -ot "${OUTPUT}" ]; then
    if [ "${INPUT1}" -ot "${OUTPUT}" ] && [ "${INPUT2}" -ot "${OUTPUT}" ] && [ "${UNWANTED}" -ot "${OUTPUT}" ] ; then
        echo "Up to date: ${OUTPUT}";
        exit;
    fi
fi

echo "Generating: ${OUTPUT}";

(
cat <<End-of-message
# =============================================
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
# Match the 2 and 2-2 letter variants:
- matcher:
    variable:
    - 'Language: LookUp[ISOLanguageCodesAll;agent.(1-20)text]'
    extract:
    - 'AgentLanguageCode                   :      100 :@Language[[1]]'
    - 'AgentLanguage                       :      100 :@Language[[2]]'

- matcher:
    variable:
    - 'Language: LookUp[ISOLanguageCodesAll;agent.(1)product.(1)comments.entry.(1)text]'
    extract:
    - 'AgentLanguageCode                   :   500006 :@Language[[1]]'
    - 'AgentLanguage                       :   500006 :@Language[[2]]'

- matcher:
    variable:
    - 'Language: LookUp[ISOLanguageCodesAll;agent.(1)product.(1)comments.entry.(2)text]'
    extract:
    - 'AgentLanguageCode                   :   500005 :@Language[[1]]'
    - 'AgentLanguage                       :   500005 :@Language[[2]]'

- matcher:
    variable:
    - 'Language: LookUp[ISOLanguageCodesAll;agent.(2-8)product.(1)comments.(1-5)entry.(1-2)text]'
    extract:
    - 'AgentLanguageCode                   :   500000 :@Language[[1]]'
    - 'AgentLanguage                       :   500000 :@Language[[2]]'

- matcher:
    variable:
    - 'Language: LookUp[ISOLanguageCodesAll;agent.(1-2)product.(2)comments.(1-5)entry.(1-2)text]'
    extract:
    - 'AgentLanguageCode                   :   500004 :@Language[[1]]'
    - 'AgentLanguage                       :   500004 :@Language[[2]]'

- matcher:
    variable:
    - 'Language: LookUp[ISOLanguageFullCodes;agent.(2-8)product.(1)comments.(1-5)entry.(1)product.(1)name]'
    extract:
    - 'AgentLanguageCode                   :   500004 :@Language[[1]]'
    - 'AgentLanguage                       :   500004 :@Language[[2]]'

- matcher:
    variable:
    - 'Language: LookUp[ISOLanguageFullCodes;agent.(1-2)product.(1)comments.(1-5)entry.(1)product.(1)name]'
    extract:
    - 'AgentLanguageCode                   :   500004 :@Language[[1]]'
    - 'AgentLanguage                       :   500004 :@Language[[2]]'

- matcher:
    variable:
    - 'Language: LookUp[ISOLanguageCodesAll;agent.product.name="Language"^.version]'
    extract:
    - 'AgentLanguageCode                   :   500008 :@Language[[1]]'
    - 'AgentLanguage                       :   500008 :@Language[[2]]'

# -----------------------------------------------------------------------------
- lookup:
    name: 'ISOLanguageCodesAll'
    merge:
    - 'ISOLanguageCodes'
    - 'ISOLanguageCodes3'

# -----------------------------------------------------------------------------

End-of-message

echo "- lookup:"
echo "    name: 'ISOLanguageFullCodes'"
echo "    map:"
grep -F -v '#' "${INPUT1}" | grep -F -- '-' | while read -r line
do
    CODE=$(echo "${line}" | cut -d' ' -f1)
    NAME=$(echo "${line}" | cut -d' ' -f2-)
    echo "      \"${CODE}\" : \"${CODE}|${NAME}\""
done

echo "# -----------------------------------------------------------------------------"

echo "- lookup:"
echo "    name: 'ISOLanguageCodes'"
echo "    map:"
grep -F -v '#' "${INPUT1}" | grep . | while read -r line
do
    CODE=$(echo "${line}" | cut -d' ' -f1)
    NAME=$(echo "${line}" | cut -d' ' -f2-)
    echo "      \"${CODE}\" : \"${CODE}|${NAME}\""
    if [[ ${CODE} = *"-"* ]]; then
        echo "      \"${CODE//-/_}\" : \"${CODE}|${NAME}\""
        echo "      \"${CODE//-/_}_\" : \"${CODE}|${NAME}\""
        echo "      \"${CODE//-/}\" : \"${CODE}|${NAME}\""
        echo "      \"${CODE//-/}_\" : \"${CODE}|${NAME}\""
    fi
done

echo "# -----------------------------------------------------------------------------"
echo "- lookup:"
echo "    name: 'ISOLanguageCodes3'"
echo "    map:"
cat "${INPUT2}" | while read -r line
do
    CODE=$(echo "${line}" | cut -d'	' -f1)
    NAME=$(echo "${line}" | cut -d'	' -f7)
    echo "      \"${CODE}\" : \"${CODE}|${NAME}\""
done
echo "# -----------------------------------------------------------------------------"

) | grep -F -v -f "${UNWANTED}" >"${OUTPUT}"

