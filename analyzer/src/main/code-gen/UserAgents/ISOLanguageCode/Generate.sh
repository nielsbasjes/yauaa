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

INPUT1=ISOLanguageCodes.csv
INPUT2=iso-639-3.tab
INPUT3=unwanted-language-codes.txt
OUTPUT="${TARGETDIR}/ISOLanguageCode.yaml"

if [ "Generate.sh" -ot "${OUTPUT}" ]; then
    if [ "${INPUT1}" -ot "${OUTPUT}" ] && [ "${INPUT2}" -ot "${OUTPUT}" ] && [ "${INPUT3}" -ot "${OUTPUT}" ] ; then
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

echo ""
echo "config:"

echo "# Match the 2 and 2-2 letter variants:"
echo "- matcher:"
echo "    extract:"
echo "    - 'AgentLanguageCode                   :   5000 :LookUp[ISOLanguageCodes;agent.(1)product.(1)comments.entry.(1-2)text]'"
echo "    - 'AgentLanguage                       :   5000 :LookUp[ISOLanguageCodesName;agent.(1)product.(1)comments.entry.(1-2)text]'"
echo ""

echo "- matcher:"
echo "    extract:"
echo "    - 'AgentLanguageCode                   :   5000 :LookUp[ISOLanguageCodes;agent.(2-4)product.(1)comments.(1-5)entry.(1-2)text]'"
echo "    - 'AgentLanguage                       :   5000 :LookUp[ISOLanguageCodesName;agent.(2-4)product.(1)comments.(1-5)entry.(1-2)text]'"
echo ""

echo "- matcher:"
echo "    extract:"
echo "    - 'AgentLanguageCode                   :   5000 :LookUp[ISOLanguageCodes;agent.(1-2)product.(2)comments.(1-5)entry.(1-2)text]'"
echo "    - 'AgentLanguage                       :   5000 :LookUp[ISOLanguageCodesName;agent.(1-2)product.(2)comments.(1-5)entry.(1-2)text]'"
echo ""

echo "- matcher:"
echo "    extract:"
echo "    - 'AgentLanguageCode                   :   5000 :LookUp[ISOLanguageCodes;agent.product.name=\"Language\"^.version]'"
echo "    - 'AgentLanguage                       :   5000 :LookUp[ISOLanguageCodesName;agent.product.name=\"Language\"^.version]'"
echo ""

echo "# Match the 3 variants:"
echo "- matcher:"
echo "    extract:"
echo "    - 'AgentLanguageCode                   :   4999 :LookUp[ISOLanguageCodes3;agent.(1)product.(1)comments.entry.(1-2)text]'"
echo "    - 'AgentLanguage                       :   4999 :LookUp[ISOLanguageCodes3Name;agent.(1)product.(1)comments.entry.(1-2)text]'"
echo ""

echo "- matcher:"
echo "    extract:"
echo "    - 'AgentLanguageCode                   :   4999 :LookUp[ISOLanguageCodes3;agent.(2-4)product.(1)comments.(1-5)entry.(1-2)text]'"
echo "    - 'AgentLanguage                       :   4999 :LookUp[ISOLanguageCodes3Name;agent.(2-4)product.(1)comments.(1-5)entry.(1-2)text]'"
echo ""

echo "- matcher:"
echo "    extract:"
echo "    - 'AgentLanguageCode                   :   4999 :LookUp[ISOLanguageCodes3;agent.(1-2)product.(2)comments.(1-5)entry.(1-2)text]'"
echo "    - 'AgentLanguage                       :   4999 :LookUp[ISOLanguageCodes3Name;agent.(1-2)product.(2)comments.(1-5)entry.(1-2)text]'"
echo ""

echo "- matcher:"
echo "    extract:"
echo "    - 'AgentLanguageCode                   :   5000 :LookUp[ISOLanguageCodes3;agent.product.name=\"Language\"^.version]'"
echo "    - 'AgentLanguage                       :   5000 :LookUp[ISOLanguageCodes3Name;agent.product.name=\"Language\"^.version]'"
echo ""

echo "# -----------------------------------------------------------------------------"
echo "- lookup:"
echo "    name: 'ISOLanguageCodes'"
echo "    map:"
fgrep -v '#' "${INPUT1}" | grep . | while read line
do
    CODE=$(echo "${line}" | cut -d' ' -f1)
    echo "      \"${CODE}\" : \"${CODE}\""
    if [[ ${CODE} = *"-"* ]]; then
        echo "      \"$(echo ${CODE} | sed 's/-/_/g')\" : \"${CODE}\""
    fi
done

echo "# -----------------------------------------------------------------------------"
echo "- lookup:"
echo "    name: 'ISOLanguageCodesName'"
echo "    map:"
fgrep -v '#' "${INPUT1}" | grep . | while read line
do
    CODE=$(echo "${line}" | cut -d' ' -f1)
    NAME=$(echo "${line}" | cut -d' ' -f2-)
    echo "      \"${CODE}\" : \"${NAME}\""
    if [[ ${CODE} = *"-"* ]]; then
        echo "      \"$(echo ${CODE} | sed 's/-/_/g')\" : \"${NAME}\""
    fi
done

echo "# -----------------------------------------------------------------------------"
echo "- lookup:"
echo "    name: 'ISOLanguageCodes3'"
echo "    map:"
grep -v -f unwanted-language-codes.txt "${INPUT2}" | while read line
do
    CODE=$(echo "${line}" | cut -d'	' -f1)
    echo "      \"${CODE}\" : \"${CODE}\""
done

echo "# -----------------------------------------------------------------------------"
echo "- lookup:"
echo "    name: 'ISOLanguageCodes3Name'"
echo "    map:"
grep -v -f unwanted-language-codes.txt "${INPUT2}" | while read line
do
    CODE=$(echo "${line}" | cut -d'	' -f1)
    NAME=$(echo "${line}" | cut -d'	' -f7)
    echo "      \"${CODE}\" : \"${NAME}\""
done
echo "# -----------------------------------------------------------------------------"

) > ${OUTPUT}
