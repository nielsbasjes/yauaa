#!/bin/bash
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2023 Niels Basjes
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
TARGETDIR="${SCRIPTDIR}/../../../antlr4/nl/basjes/parse/useragent/parser"

INPUT="${SCRIPTDIR}/../../../resources/mozilla-public-suffix-list.txt"
OUTPUT="${SCRIPTDIR}/PublicSuffix.g4.fragment"
TARGETFILE="${TARGETDIR}/UserAgent.g4"

if [ ! -f "${INPUT}" ]; then
    echo "MISSING INPUT: ${INPUT}"
    exit 1
fi

#if [ "Generate.sh" -ot "${OUTPUT}" ]; then
#    if [ "${INPUT}" -ot "${OUTPUT}" ]; then
#        echo "Up to date: ${OUTPUT}";
#        exit;
#    fi
#fi

echo "Generating: ${OUTPUT}";

(
echo "// The TLDs extracted from https://publicsuffix.org/list/public_suffix_list.dat"
echo -n "fragment TLD"
SEP=':'
grep -F -v '//' "${INPUT}" | grep . | sed 's@.*\.\([a-z]\+\)$@\1@g' | grep -v "[^a-z]" | grep -v -P "[\x7f-\xff]" | sort -u | \
while read -r suffix
do
  echo -n " ${SEP} '${suffix}'"
  SEP='|'
done
echo ';'
) > "${OUTPUT}"

sed -i '/The TLDs extracted/d' "${TARGETFILE}"
sed -i '/^fragment TLD/d' "${TARGETFILE}"
sed -i "/^\/\/ GENERATED TLD FRAGMENT:$/ r ${OUTPUT}" "${TARGETFILE}"

git add "${TARGETFILE}"
