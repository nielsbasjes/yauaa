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

#echo "
#  - matcher:
#      extract:
#        - 'DeviceCpu : 105:LookUp[CPUArchitectures;agent.([1-3])product.(1)comments.entry%1]'
#  - matcher:
#      extract:
#        - 'DeviceCpu : 104:LookUp[CPUArchitectures;agent.([1-3])product.(1)comments.entry%2]'
#  - matcher:
#      extract:
#        - 'DeviceCpu : 103:LookUp[CPUArchitectures;agent.([1-3])product.(1)comments.entry%3]'
#  - matcher:
#      extract:
#        - 'DeviceCpu : 102:LookUp[CPUArchitectures;agent.([1-3])product.(1)comments.entry%4]'
#  - matcher:
#      extract:
#        - 'DeviceCpu : 101:LookUp[CPUArchitectures;agent.([1-3])product.(1)comments.entry%5]'
#  - matcher:
#      extract:
#        - 'DeviceCpu : 101:LookUp[CPUArchitectures;agent.([1-3])product.(1)comments.entry.product.version]'
#"

echo "  - lookup:"
echo "      name: 'CPUArchitectures'"
echo "      map:"
cat "CPUTypes.csv" | while read line ; \
do
    cpu=$(echo ${line} | cut -d' ' -f1)
    value=$(echo ${line} | cut -d' ' -f2-)
    echo "       \"${cpu}\" : \"${value}\""
done

cat "CPUTypes.csv" | while read line ; \
do
    cpu=$(echo ${line} | cut -d' ' -f1)
    value=$(echo ${line} | cut -d' ' -f2-)
echo "
  - matcher:
      require:
        - 'agent.([1-3])product.(1)comments.entry%1=\"${cpu}\"'
      extract:
        - 'DeviceCpu : 105:\"${value}\"'
  - matcher:
      require:
        - 'agent.([1-3])product.(1)comments.entry%2=\"${cpu}\"'
      extract:
        - 'DeviceCpu : 104:\"${value}\"'
  - matcher:
      require:
        - 'agent.([1-3])product.(1)comments.entry%3=\"${cpu}\"'
      extract:
        - 'DeviceCpu : 103:\"${value}\"'
  - matcher:
      require:
        - 'agent.([1-3])product.(1)comments.entry%4=\"${cpu}\"'
      extract:
        - 'DeviceCpu : 102:\"${value}\"'
  - matcher:
      require:
        - 'agent.([1-3])product.(1)comments.entry%5=\"${cpu}\"'
      extract:
        - 'DeviceCpu : 101:\"${value}\"'
  - matcher:
      require:
        - 'agent.([1-3])product.(1)comments.entry.product.version=\"${cpu}\"'
      extract:
        - 'DeviceCpu : 101:\"${value}\"'
"
done
) > ../CPUTypes.yaml
