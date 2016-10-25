#!/bin/bash
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2016 Niels Basjes
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

(
echo "# ============================================="
echo "# THIS FILE WAS GENERATED; DO NOT EDIT MANUALLY"
echo "# ============================================="
echo "#"
echo "# Yet Another UserAgent Analyzer"
echo "# Copyright (C) 2013-2016 Niels Basjes"
echo "#"
echo "# Licensed under the Apache License, Version 2.0 (the \"License\");"
echo "# you may not use this file except in compliance with the License."
echo "# You may obtain a copy of the License at"
echo "#"
echo "# http://www.apache.org/licenses/LICENSE-2.0"
echo "#"
echo "# Unless required by applicable law or agreed to in writing, software"
echo "# distributed under the License is distributed on an \"AS IS\" BASIS,"
echo "# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied."
echo "# See the License for the specific language governing permissions and"
echo "# limitations under the License."
echo "#"
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
