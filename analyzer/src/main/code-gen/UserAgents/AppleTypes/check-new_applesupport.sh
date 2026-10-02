#!/bin/bash
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2026 Niels Basjes
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
SCRIPTDIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null && pwd)"

cd "${SCRIPTDIR}" || exit

DAY=$(date +%Y%m%d)

# https://www.google.com/search?q=site%3Asupport.apple.com+%22Identify+your%22+%22Model+Identifier%3A%22
# Do NOT fetch this more than once a day. There is no need at all.
if [ ! -f ${DAY}-AppleSupport-HTML-Mac-mini.tmp ];
then
    curl "https://support.apple.com/en-us/102852" > ${DAY}-AppleSupport-HTML-Mac-mini.tmp
    curl "https://support.apple.com/en-us/108052" > ${DAY}-AppleSupport-HTML-MacBook-Pro.tmp

# Only really OLD models, no more updates and page has different html structure.
#    curl "https://support.apple.com/en-us/103257" > ${DAY}-AppleSupport-HTML-MacBook.tmp
    curl "https://support.apple.com/en-us/108054" > ${DAY}-AppleSupport-HTML-iMac.tmp
    curl "https://support.apple.com/en-us/102231" > ${DAY}-AppleSupport-HTML-Mac-Studio.tmp
    curl "https://support.apple.com/en-us/102869" > ${DAY}-AppleSupport-HTML-MacBook-Air.tmp
    curl "https://support.apple.com/en-us/102887" > ${DAY}-AppleSupport-HTML-Mac-Pro.tmp
fi

cat "${DAY}"-AppleSupport-HTML-*.tmp | \
  grep -E '<h2 class="gb-header">|Model Identifier:' | \
  grep -F -v "Learn more" |\
  grep -F -v "Find the name" |\
  grep -F -v "Identify your" |\
  sed 's@<b>@@g;s@</b>@@g' |\
  sed -E 'N; s/.*gb-header">([^<]+)<\/h2>.*\n.*Model Identifier: *([^<]+)<\/p>.*/\2 | \1/' | \
  grep '[A-Za-z]' | sort -u > AppleSupport-Identifiers.tmp

(
#   # Do not want the "iPhone Simulator" entries as they are the same as the normal cpu tags.
#   echo "i386"
#   echo "x86_64"
#   echo "arm64"
##    We do not want these because they do not have a browser.
#   echo "AirPods[0-9]"
#   echo "iProd[0-9]"
#   echo "AirPort[0-9]"
#   echo "AirTag[0-9]"
#   echo "AppleDisplay[0-9]"
#   echo "AudioAccessory[0-9]"
   grep -F '|' AppleTypes.csv | grep -F -v '#' | cut -d'|' -f1 | sed 's@^ *@@;s@ *$@@g' | sort -u
) > __currentIds.txt

echo "===================================="
echo "Apparently missing entries"
echo "vvvvv"
#diff __currentIds.txt AppleSupport-Identifiers.tmp | grep '^>'
grep -F -v -i -f  __currentIds.txt AppleSupport-Identifiers.tmp
echo "^^^^^"
echo "===================================="
#rm -f __currentIds.txt
