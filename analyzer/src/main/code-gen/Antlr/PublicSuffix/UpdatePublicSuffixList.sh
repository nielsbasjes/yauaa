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

TMPDIR=${SCRIPTDIR}/tmp

if [ ! -d "${TMPDIR}" ];
then
  mkdir -p "${TMPDIR}"
  if [ ! -d "${TMPDIR}" ];
  then
    echo "Unable to create temp directory"
    exit 1
  fi
fi

DAILYFILE="${TMPDIR}/$(date +%Y-%m-%d)-public-suffix-list.txt"
TARGETFILE="${SCRIPTDIR}/../../../resources/mozilla-public-suffix-list.txt"

[ -f "${TARGETFILE}" ] && [ -f "${DAILYFILE}" ] && echo "Already have the public-suffix-list.txt for today" && exit 0

echo -n "Downloading public_suffix_list.dat ... "
curl -s https://publicsuffix.org/list/public_suffix_list.dat -o "${DAILYFILE}"
cp "${DAILYFILE}" "${TARGETFILE}"
git add "${TARGETFILE}"
git commit -m"chore: Updated the Mozilla Public Suffix list" "${TARGETFILE}"
echo "done"
exit 0
