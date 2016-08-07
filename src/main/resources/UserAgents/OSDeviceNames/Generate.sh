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

echo "# Yet Another UserAgent Analyzer"
echo "# Copyright (C) 2013-2016 Niels Basjes"
echo "#"
echo "# This program is free software: you can redistribute it and/or modify"
echo "# it under the terms of the GNU General Public License as published by"
echo "# the Free Software Foundation, either version 3 of the License, or"
echo "# (at your option) any later version."
echo "#"
echo "# This program is distributed in the hope that it will be useful,"
echo "# but WITHOUT ANY WARRANTY; without even the implied warranty of"
echo "# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the"
echo "# GNU General Public License for more details."
echo "#"
echo "# You should have received a copy of the GNU General Public License"
echo "# along with this program.  If not, see <http://www.gnu.org/licenses/>."

echo "config:"

cat "OperatingSystemDeviceNames.csv" | grep . | fgrep -v '#' | while read line ; \
do
    osname=$(   echo ${line} | cut -d'|' -f1)
    devclass=$( echo ${line} | cut -d'|' -f2)
    devname=$(  echo ${line} | cut -d'|' -f3)
    devbrand=$(  echo ${line} | cut -d'|' -f4)
echo "
- matcher:
    require:
    - 'agent.(1)product.(1)comments.entry.text=\"${osname}\"'
    extract:
    - 'DeviceClass           :  111:\"${devclass}\"'
    - 'DeviceName            :  111:\"${devname}\"'
    - 'DeviceBrand           :  111:\"${devbrand}\"'
    - 'OperatingSystemClass  :  150:\"Desktop\"'
    - 'OperatingSystemName   :  150:\"${osname}\"'
    - 'OperatingSystemVersion:  149:\"??\"'

- matcher:
    extract:
    - 'DeviceClass           :  111:\"${devclass}\"'
    - 'DeviceName            :  111:\"${devname}\"'
    - 'DeviceBrand           :  111:\"${devbrand}\"'
    - 'OperatingSystemClass  :  150:\"Desktop\"'
    - 'OperatingSystemName   :  150:\"${osname}\"'
    - 'OperatingSystemVersion:  150:agent.(1)product.(1)comments.entry.product.name=\"${osname}\"^.version'

- matcher:
    extract:
    - 'DeviceClass           :  111:\"${devclass}\"'
    - 'DeviceName            :  111:\"${devname}\"'
    - 'DeviceBrand           :  111:\"${devbrand}\"'
    - 'OperatingSystemClass  :  150:\"Desktop\"'
    - 'OperatingSystemName   :  150:agent.product.(1)comments.entry.text=\"${osname}\"'
    - 'OperatingSystemVersion:  149:\"??\"'

- matcher:
    extract:
    - 'DeviceClass           :  111:\"${devclass}\"'
    - 'DeviceName            :  111:\"${devname}\"'
    - 'DeviceBrand           :  111:\"${devbrand}\"'
    - 'OperatingSystemClass  :  150:\"Desktop\"'
    - 'OperatingSystemName   :  150:\"${osname}\"'
    - 'OperatingSystemVersion:  150:agent.product.name=\"${osname}\"^.version'

- matcher:
    require:
    - 'agent.product.name#1=\"${osname}\"'
    extract:
    - 'DeviceClass           :  111:\"${devclass}\"'
    - 'DeviceName            :  111:\"${devname}\"'
    - 'DeviceBrand           :  111:\"${devbrand}\"'
    - 'OperatingSystemClass  :  150:\"Desktop\"'
    - 'OperatingSystemName   :  150:\"${osname}\"'
    - 'OperatingSystemVersion:  149:\"??\"'

- matcher:
    require:
    - 'agent.product.name#2=\"${osname}\"'
    extract:
    - 'DeviceClass           :  111:\"${devclass}\"'
    - 'DeviceName            :  111:\"${devname}\"'
    - 'DeviceBrand           :  111:\"${devbrand}\"'
    - 'OperatingSystemClass  :  150:\"Desktop\"'
    - 'OperatingSystemName   :  150:\"${osname}\"'
    - 'OperatingSystemVersion:  149:\"??\"'

"
done

) > ../OperatingSystemDeviceNames.yaml
