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

cat "AndroidBrands.csv" | fgrep -v '#' | grep . | while read line ; \
do
    prefix=$(echo ${line} | cut -d'|' -f1)
    brand=$(echo ${line} | cut -d'|' -f2)
echo "
  - matcher:
      require:
        - 'agent.(1)product.comments.entry.product.name=\"Android\"'
        - 'agent.(1)product.(1)comments.entry.product.name=\"Build\"^<{\"${prefix}\"'
      extract:
        - 'DeviceBrand                 :  100:\"${brand}\"'

  - matcher:
      require:
        - 'agent.(1)product.comments.entry.product.name=\"Android\"'
        - 'agent.(1)product.(1)comments.entry.product.name%1=\"Build\"@{\"${prefix}\"'
      extract:
        - 'DeviceBrand                 :  100:\"${brand}\"'

  - matcher:
      require:
        - 'agent.(1)product.comments.entry.product.name=\"Android\"'
        - 'agent.(1)product.(1)comments.entry.product.name%2=\"Build\"@{\"${prefix}\"'
      extract:
        - 'DeviceBrand                 :  100:\"${brand}\"'

  - matcher:
      require:
        - 'agent.(1)product.comments.entry.product.name=\"Android\"'
        - 'agent.(1)product.(1)comments.entry.product.name%3=\"Build\"@{\"${prefix}\"'
      extract:
        - 'DeviceBrand                 :  100:\"${brand}\"'

  - matcher:
      require:
        - 'agent.(1)product.comments.entry.product.name=\"Android\"'
        - 'agent.(1)product.(1)comments.entry.product.name%4=\"Build\"@{\"${prefix}\"'
      extract:
        - 'DeviceBrand                 :  100:\"${brand}\"'

  - matcher:
      require:
        - 'agent.(1)product.comments.entry.product.name=\"Android\"'
        - 'agent.(1)product.(1)comments.entry.product.name%5=\"Build\"@{\"${prefix}\"'
      extract:
        - 'DeviceBrand                 :  100:\"${brand}\"'
"
done
) > ../AndroidBrands.yaml
