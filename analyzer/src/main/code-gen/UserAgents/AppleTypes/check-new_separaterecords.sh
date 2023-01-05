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

# Clone if not done so already
[ -d _separaterecords ] || git clone https://github.com/SeparateRecords/apple_device_identifiers _separaterecords

# Ensure latest
( cd _separaterecords && git pull )

(
   # Do not want the "iPhone Simulator" entries as they are the same as the normal cpu tags.
   echo "i386"
   echo "x86_64"
   echo "arm64"
   # We do not want these because they do not have a browser.
   echo "AirPods[0-9]"
   echo "iProd[0-9]"
   echo "AirPort[0-9]"
   echo "AirTag[0-9]"
   echo "AppleDisplay[0-9]"
   echo "AudioAccessory[0-9]"
   grep -F '|' AppleTypes.csv | grep -F -v '#' | cut -d'|' -f1 | sort -u
) > __currentIds.txt

echo "===================================="
echo "Apparently missing entries"
echo "vvvvv"
cat _separaterecords/devices/*.json | grep -F ':' | grep -v -f  __currentIds.txt | sed 's@^ \+"\([^"]\+\)" *: *"\([^"]\+\)".*$@\2 : \1@'
echo "^^^^^"
echo "===================================="
rm -f __currentIds.txt
