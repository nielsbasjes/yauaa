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
echo "      name: 'OperatingSystemNames'"
echo "      map:"
cat "OperatingSystemNames.csv" | grep . | fgrep -v '#' | while read osname ; \
do
    echo "       \"${osname}\" : \"${osname}\""
done

cat "OperatingSystemNames.csv" | grep . | fgrep -v '#' | while read osname ; \
do
echo "
  - matcher:
      require:
        - 'agent.(1)product.(1)comments.entry.text=\"${osname}\"'
      extract:
        - 'OperatingSystemClass  :   100:\"Desktop\"'
        - 'OperatingSystemName   :   100:\"${osname}\"'
        - 'OperatingSystemVersion:   100:\"??\"'

  - matcher:
      extract:
        - 'OperatingSystemClass  :   100:\"Desktop\"'
        - 'OperatingSystemName   :   100:\"${osname}\"'
        - 'OperatingSystemVersion:   100:agent.(1)product.(1)comments.entry.product.name=\"${osname}\"^.version'

  - matcher:
      extract:
        - 'OperatingSystemClass  :   100:\"Desktop\"'
        - 'OperatingSystemName   :   100:\"${osname}\"'
        - 'OperatingSystemVersion:   100:agent.product.name=\"${osname}\"^.version'
"
done

) > ../OperatingSystemNames.yaml
