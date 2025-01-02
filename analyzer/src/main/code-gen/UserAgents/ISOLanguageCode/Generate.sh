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

FILE_CODES2="${SCRIPTDIR}/__tmp_ISOLanguageCode-Lookup2.yaml"
FILE_CODES3="${SCRIPTDIR}/__tmp_ISOLanguageCode-Lookup3.yaml"
FILE_CODES4="${SCRIPTDIR}/__tmp_ISOLanguageCode-Lookup4.yaml"
FILE_CODES4L="${SCRIPTDIR}/__tmp_ISOLanguageCode-Lookup4Letters.yaml"

(
echo "# -----------------------------------------------------------------------------"
echo "- lookup:"
echo "    name: 'ISOLanguageCodes2'"
echo "    map:"
) > "${FILE_CODES2}"

(
echo "# -----------------------------------------------------------------------------"
echo "- lookup:"
echo "    name: 'ISOLanguageCodes4'"
echo "    map:"
) > "${FILE_CODES4}"

(
echo "# -----------------------------------------------------------------------------"
echo "- lookup:"
echo "    name: 'ISOLanguageCodes4Letters'"
echo "    map:"
) > "${FILE_CODES4L}"

grep -F -v '#' "${INPUT1}" | while read -r line
do
    CODE=$(echo "${line}" | cut -d' ' -f1)
    NAME=$(echo "${line}" | cut -d' ' -f2-)
    echo "\"${CODE}\"" | grep -F -f "${UNWANTED}" > /dev/null
    wanted=$?
    if [ ${wanted} -ne 0 ];
    then
      if [[ ${CODE} =~ ^[a-z]{2,3}$ ]];
      then
        (
          echo "      \"${CODE}\" : \"${CODE}|${NAME}\""
        ) >> "${FILE_CODES2}"
      fi

      if [[ ${CODE} = *"-"* ]];
      then
        (
          echo "      \"${CODE}\" : \"${CODE}|${NAME}\""
          echo "      \"${CODE//-/_}\" : \"${CODE}|${NAME}\""
          echo "      \"${CODE//-/_}_\" : \"${CODE}|${NAME}\""
          echo "      \"${CODE//-/}_\" : \"${CODE}|${NAME}\""
        ) >> "${FILE_CODES4}"

        (
          echo "      \"${CODE//-/}\" : \"${CODE}|${NAME}\""
        ) >> "${FILE_CODES4L}"
      fi
    fi
done

(
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
) > "${FILE_CODES3}"

(
cat <<"End-of-message"
# $schema: https://yauaa.basjes.nl/v1/YauaaConfig.json
# =============================================
# THIS FILE WAS GENERATED; DO NOT EDIT MANUALLY
# =============================================
#
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
#

config:
# Match the 2 and 2-2 letter variants:
- matcher:
    variable:
    - 'Language: LookUp[ISOLanguageCodes;agent.(1-20)text]'
    extract:
    - 'AgentLanguageCode                   :      100 :@Language[[1]]'
    - 'AgentLanguage                       :      100 :@Language[[2]]'

- matcher:
    variable:
    - 'Language: LookUp[ISOLanguageCodes;agent.(1)product.(1)comments.entry.(1)text]'
    extract:
    - 'AgentLanguageCode                   :   500008 :@Language[[1]]'
    - 'AgentLanguage                       :   500008 :@Language[[2]]'

- matcher:
    variable:
    - 'Language: LookUpPrefix[ISOLanguageCodes4;agent.(1)product.(1)comments.entry.(1)text]'
    extract:
    - 'AgentLanguageCode                   :   500007 :@Language[[1]]'
    - 'AgentLanguage                       :   500007 :@Language[[2]]'

- matcher:
    variable:
    - 'Language: LookUp[ISOLanguageCodes;agent.(1)product.(1)comments.entry.(2)text]'
    extract:
    - 'AgentLanguageCode                   :   500006 :@Language[[1]]'
    - 'AgentLanguage                       :   500006 :@Language[[2]]'


- matcher:
    variable:
    - 'Language: LookUpPrefix[ISOLanguageCodes4;agent.(1)product.(1)comments.entry.(2)text]'
    extract:
    - 'AgentLanguageCode                   :   500005 :@Language[[1]]'
    - 'AgentLanguage                       :   500005 :@Language[[2]]'


- matcher:
    variable:
    - 'Language: LookUp[ISOLanguageCodes;agent.(2-8)product.(1)comments.(1-5)entry.(1-2)text]'
    extract:
    - 'AgentLanguageCode                   :   500003 :@Language[[1]]'
    - 'AgentLanguage                       :   500003 :@Language[[2]]'

- matcher:
    variable:
    - 'Language: LookUp[ISOLanguageCodes;agent.(1-2)product.(2)comments.(1-5)entry.(1-2)text]'
    extract:
    - 'AgentLanguageCode                   :   500004 :@Language[[1]]'
    - 'AgentLanguage                       :   500004 :@Language[[2]]'

- matcher:
    variable:
    - 'Language: LookUp[ISOLanguageCodes4;agent.(2-8)product.(1)comments.(1-5)entry.(1)product.(1)name]'
    extract:
    - 'AgentLanguageCode                   :   500001 :@Language[[1]]'
    - 'AgentLanguage                       :   500001 :@Language[[2]]'

- matcher:
    variable:
    - 'Language: LookUp[ISOLanguageCodes4;agent.(1-2)product.(1)comments.(1-5)entry.(1)product.(1)name]'
    extract:
    - 'AgentLanguageCode                   :   500002 :@Language[[1]]'
    - 'AgentLanguage                       :   500002 :@Language[[2]]'

- matcher:
    variable:
    - 'Language: LookUp[ISOLanguageCodes4;agent.product.name="Language"^.version]'
    extract:
    - 'AgentLanguageCode                   :   500010 :@Language[[1]]'
    - 'AgentLanguage                       :   500010 :@Language[[2]]'

# -----------------------------------------------------------------------------
- lookup:
    name: 'ISOLanguageCodes'
    merge:
    - 'ISOLanguageCodes2'
    - 'ISOLanguageCodes3'
    - 'ISOLanguageCodes4'
    - 'ISOLanguageCodes4Letters'

End-of-message

cat "${FILE_CODES2}"
cat "${FILE_CODES3}"
cat "${FILE_CODES4}"
cat "${FILE_CODES4L}"

) | grep -F -v -f "${UNWANTED}" >"${OUTPUT}"
