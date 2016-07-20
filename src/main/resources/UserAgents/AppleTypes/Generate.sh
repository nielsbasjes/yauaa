#!/bin/bash
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2016 Niels Basjes
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

(
echo "# ============================================="
echo "# THIS FILE WAS GENERATED; DO NOT EDIT MANUALLY"
echo "# ============================================="
echo "config:"

echo "  - lookup:"
echo "      name: 'AppleDeviceClass'"
echo "      map:"
cat "AppleTypes.csv" | fgrep -v '#' | grep '[a-z]' | while read line ; \
do
    key=$(echo ${line} | cut -d'|' -f1)
    deviceClass=$(echo ${line} | cut -d'|' -f2)
    deviceName=$(echo ${line} | cut -d'|' -f3)
    deviceVersion=$(echo ${line} | cut -d'|' -f4-)
    echo "       \"${key}\" : \"${deviceClass}\""
done

echo ""
echo "  - lookup:"
echo "      name: 'AppleDeviceName'"
echo "      map:"
cat "AppleTypes.csv" | fgrep -v '#' | grep '[a-z]' | while read line ; \
do
    key=$(echo ${line} | cut -d'|' -f1)
    deviceClass=$(echo ${line} | cut -d'|' -f2)
    deviceName=$(echo ${line} | cut -d'|' -f3)
    deviceVersion=$(echo ${line} | cut -d'|' -f4-)
    echo "       \"${key}\" : \"${deviceName}\""
done

cat "AppleTypes.csv" | fgrep -v '#' | grep '[a-z]' | while read line ; \
do
    key=$(echo ${line} | cut -d'|' -f1)
    deviceClass=$(echo ${line} | cut -d'|' -f2)
    deviceName=$(echo ${line} | cut -d'|' -f3)
    deviceVersion=$(echo ${line} | cut -d'|' -f4-)
echo "
  - matcher:
      require:
        - 'agent.product.comments.entry.(1)text=\"${key}\"'
      extract:
        - 'DeviceBrand   : 100:\"Apple\"'
        - 'DeviceClass   : 100:\"${deviceClass}\"'
        - 'DeviceName    : 100:\"${deviceName}\"'
        - 'DeviceVersion : 100:\"${deviceVersion}\"'

  - matcher:
      require:
        - 'agent.product.(1)name=\"${key}\"'
      extract:
        - 'DeviceBrand   : 101:\"Apple\"'
        - 'DeviceClass   : 101:\"${deviceClass}\"'
        - 'DeviceName    : 101:\"${deviceName}\"'
        - 'DeviceVersion : 101:\"${deviceVersion}\"'

  - matcher:
      require:
        - 'agent.text=\"${key}\"'
      extract:
        - 'DeviceBrand   : 101:\"Apple\"'
        - 'DeviceClass   : 101:\"${deviceClass}\"'
        - 'DeviceName    : 101:\"${deviceName}\"'
        - 'DeviceVersion : 101:\"${deviceVersion}\"'
"
done
) > ../AppleTypes.yaml
