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
SCRIPTDIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null && pwd)"

cd "${SCRIPTDIR}" || exit

DAY=$(date +%Y%m%d)

# https://www.google.com/search?q=site%3Asupport.apple.com+%22Identify+your%22+%22Model+Identifier%3A%22
# Do NOT fetch this more than once a day. There is no need at all.
if [ ! -f ${DAY}-AppleSupport-HTML-Mac-mini.tmp ];
then
    curl "https://support.apple.com/en-us/HT201894" > ${DAY}-AppleSupport-HTML-Mac-mini.tmp
    curl "https://support.apple.com/en-us/HT201300" > ${DAY}-AppleSupport-HTML-MacBook-Pro.tmp
    curl "https://support.apple.com/en-us/HT201608" > ${DAY}-AppleSupport-HTML-MacBook.tmp
    curl "https://support.apple.com/en-us/HT201634" > ${DAY}-AppleSupport-HTML-iMac.tmp
    curl "https://support.apple.com/en-us/HT213073" > ${DAY}-AppleSupport-HTML-Mac-Studio.tmp
    curl "https://support.apple.com/en-us/HT201862" > ${DAY}-AppleSupport-HTML-MacBook-Air.tmp
    curl "https://support.apple.com/en-us/HT202888" > ${DAY}-AppleSupport-HTML-Mac-Pro.tmp
fi

cat ${DAY}-AppleSupport-HTML-*.tmp | fgrep "Model Identifier:" | sed '
  s@</strong>@@g;
  s@<br/>@@g;
  s@<br>@@g;
  s@,&nbsp;@\n@g;
  s@&nbsp;@\n@g;
  s@;@\n@g;
  s@, @\n@g;
  s@Model Identifier:@@g;
  s@ @@g;
  s@,$@@g;
' | grep '[A-Za-z]' | sort -u > AppleSupport-Identifiers.tmp

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
   grep -F '|' AppleTypes.csv | grep -F -v '#' | cut -d'|' -f1 | sort -u
) > __currentIds.txt

echo "===================================="
echo "Apparently missing entries"
echo "vvvvv"
diff __currentIds.txt AppleSupport-Identifiers.tmp | grep '^>'
# fgrep -v -i -f  __currentIds.txt AppleSupport-Identifiers.tmp
echo "^^^^^"
echo "===================================="
#rm -f __currentIds.txt
