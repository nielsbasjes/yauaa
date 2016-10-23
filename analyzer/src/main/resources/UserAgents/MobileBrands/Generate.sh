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
echo ""
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


echo "- lookup:"
echo "    name: 'MobileBrands'"
echo "    map:"

cat "MobileBrands.csv" | fgrep -v '#' | grep . | while read line ; \
do
    prefix=$(echo ${line} | cut -d'|' -f1)
    brand=$(echo ${line} | cut -d'|' -f2)
    echo "      \"${prefix}\" : \"${brand}\""
done

echo "
# ===================================================================================================
"

cat "MobileBrands.csv" | fgrep -v '#' | grep . | while read line ; \
do
    prefix=$(echo ${line} | cut -d'|' -f1)
    brand=$(echo ${line} | cut -d'|' -f2)
echo "
- matcher:
    require:
    - 'agent.(1-2)product.comments.entry.product.name=\"Android\"'
    - 'agent.(1-2)product.(1)comments.entry.product.name=\"Build\"^<{\"${prefix}\"'
    extract:
    - 'DeviceBrand                 :  105:\"${brand}\"'

- matcher:
    require:
    - 'agent.(1-2)product.comments.entry.product.name=\"Android\"'
    - 'agent.(1-2)product.(1)comments.entry.product.name[2]=\"Build\"^<{\"${prefix}\"'
    extract:
    - 'DeviceBrand                 :  105:\"${brand}\"'

- matcher:
    require:
    - 'agent.(1-2)product.comments.entry.product.name=\"Android\"'
    - 'agent.(1-2)product.(1)comments.entry.product.name[3]=\"Build\"^<{\"${prefix}\"'
    extract:
    - 'DeviceBrand                 :  105:\"${brand}\"'

- matcher:
    require:
    - 'agent.(1-2)product.comments.entry.product.name=\"Android\"'
    - 'agent.(1-2)product.(1)comments.entry.product.name[1]=\"Build\"@{\"${prefix}\"'
    extract:
    - 'DeviceBrand                 :  104:\"${brand}\"'

- matcher:
    require:
    - 'agent.(1-2)product.comments.entry.product.name=\"Android\"'
    - 'agent.(1-2)product.(1)comments.entry.product.name[2]=\"Build\"@{\"${prefix}\"'
    extract:
    - 'DeviceBrand                 :  103:\"${brand}\"'

- matcher:
    require:
    - 'agent.(1-2)product.comments.entry.product.name=\"Android\"'
    - 'agent.(1-2)product.(1)comments.entry.product.name[3]=\"Build\"@{\"${prefix}\"'
    extract:
    - 'DeviceBrand                 :  102:\"${brand}\"'

- matcher:
    require:
    - 'agent.(1-2)product.comments.entry.product.name=\"Android\"'
    - 'agent.(1-2)product.(1)comments.entry.product.name[4]=\"Build\"@{\"${prefix}\"'
    extract:
    - 'DeviceBrand                 :  101:\"${brand}\"'

- matcher:
    require:
    - 'agent.(1-2)product.comments.entry.product.name=\"Android\"'
    - 'agent.(1-2)product.(1)comments.entry.product.name[5]=\"Build\"@{\"${prefix}\"'
    extract:
    - 'DeviceBrand                 :  100:\"${brand}\"'

- matcher:
    require:
    - 'agent.(1-2)product.comments.entry.product.name=\"Android\"'
    - 'agent.(1-2)product.name{\"${prefix}\"'
    extract:
    - 'DeviceBrand                 :  110:\"${brand}\"'

- matcher:
    require:
    - 'agent.product.name{\"${prefix}\"'
    extract:
    - 'DeviceBrand                 :  103:\"${brand}\"'

- matcher:
    require:
    - 'agent.(1-2)product.(1)comments.entry.product.name{\"${prefix}\"'
    extract:
    - 'DeviceBrand                 :  102:\"${brand}\"'

- matcher:
    require:
    - 'agent.(1-2)product.(1)comments.entry.text{\"${prefix}\"'
    extract:
    - 'DeviceBrand                 :  101:\"${brand}\"'
"
done

cat "MobileBrands.csv" | fgrep -v '#' | grep . | cut -d'|' -f2 | sort -u | while read brand;
do
echo "

- matcher:
    require:
    - 'agent.(1-2)product.(1)comments.entry.product.name[1]=\"Build\"^.version{\"${brand}\"'
    extract:
    - 'DeviceBrand                 :   94:\"${brand}\"'

- matcher:
    require:
    - 'agent.(1-2)product.(1)comments.entry.product.name[2]=\"Build\"^.version{\"${brand}\"'
    extract:
    - 'DeviceBrand                 :   93:\"${brand}\"'

- matcher:
    require:
    - 'agent.(1-2)product.(1)comments.entry.product.name[3]=\"Build\"^.version{\"${brand}\"'
    extract:
    - 'DeviceBrand                 :   92:\"${brand}\"'

- matcher:
    require:
    - 'agent.(1-2)product.(1)comments.entry.product.name[4]=\"Build\"^.version{\"${brand}\"'
    extract:
    - 'DeviceBrand                 :   91:\"${brand}\"'

- matcher:
    require:
    - 'agent.(1-2)product.(1)comments.entry.product.name[5]=\"Build\"^.version{\"${brand}\"'
    extract:
    - 'DeviceBrand                 :   90:\"${brand}\"'

# ===================================================================================================

"
done
) > ../MobileBrands.yaml
