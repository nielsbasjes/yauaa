#!/bin/bash
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2018 Niels Basjes
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

INPUT=MobileBrands.csv
OUTPUT=../MobileBrands.yaml

if [ "Generate.sh" -ot "${OUTPUT}" ]; then
    if [ "${INPUT}" -ot "${OUTPUT}" ]; then
        echo "${OUTPUT} is up to date";
        exit;
    fi
fi

echo "Generating ${OUTPUT}";

(
echo "# ============================================="
echo "# THIS FILE WAS GENERATED; DO NOT EDIT MANUALLY"
echo "# ============================================="
echo "#"
echo "# Yet Another UserAgent Analyzer"
echo "# Copyright (C) 2013-2018 Niels Basjes"
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

fgrep -v '#' "${INPUT}" | grep . | while read line
do
    prefix=$(echo "${line}" | cut -d'|' -f1)
    brand=$(echo "${line}" | cut -d'|' -f2)
    echo "      \"${prefix}\" : \"${brand}\""
done

echo "
# ===================================================================================================
"

fgrep -v '#' "${INPUT}" | grep . | while read line
do
    prefix=$(echo "${line}" | cut -d'|' -f1)
    brand=$(echo "${line}" | cut -d'|' -f2)
echo "
- matcher:
    variable:
    - 'BuildProduct: agent.(1-2)product.(1)comments.entry.product.name=\"Build\"'
    require:
    - 'agent.(1-2)product.comments.entry.product.name[1]=\"Android\"'
    - 'agent.(1-2)product.(1)comments.entry.product{\"${prefix}\"' # Performance trick
    extract:
    - 'DeviceBrand                 :  325:\"${brand}\"'
    - 'DeviceName                  :  325:@BuildProduct^<{\"${prefix}\"'

- matcher:
    variable:
    - 'BuildProduct: agent.(1-2)product.(1)comments.entry.product.name[2]=\"Build\"'
    require:
    - 'agent.(1-2)product.comments.entry.product.name[1]=\"Android\"'
    - 'agent.(1-2)product.(1)comments.entry.product{\"${prefix}\"' # Performance trick
    extract:
    - 'DeviceBrand                 :  325:\"${brand}\"'
    - 'DeviceName                  :  325:@BuildProduct^<{\"${prefix}\"'

    variable:
    - 'BuildProduct: agent.(1-2)product.(1)comments.entry.product.name[3]=\"Build\"'
    require:
    - 'agent.(1-2)product.comments.entry.product.name[1]=\"Android\"'
    - 'agent.(1-2)product.(1)comments.entry.product{\"${prefix}\"' # Performance trick
    extract:
    - 'DeviceBrand                 :  325:\"${brand}\"'
    - 'DeviceName                  :  325:@BuildProduct^<{\"${prefix}\"'

- matcher:
    require:
    - 'agent.(1-2)product.comments.entry.product.name[1]=\"Android\"'
    - 'agent.(1-2)product.(1)comments.entry.product.name{\"${prefix}\"' # Performance trick
    extract:
    - 'DeviceBrand                 :  324:\"${brand}\"'
    - 'DeviceName                  :  324:agent.(1-2)product.(1)comments.entry.product.name[2]=\"Build\"@{\"${prefix}\"[-1]'

- matcher:
    require:
    - 'agent.(1-2)product.comments.entry.product.name[1]=\"Android\"'
    - 'agent.(1-2)product.(1)comments.entry.product.name{\"${prefix}\"' # Performance trick
    extract:
    - 'DeviceBrand                 :  324:\"${brand}\"'
    - 'DeviceName                  :  324:agent.(1-2)product.(1)comments.entry.product.name[3]=\"Build\"@{\"${prefix}\"[-2]'

- matcher:
    require:
    - 'agent.(1-2)product.comments.entry.product.name[1]=\"Android\"'
    - 'agent.(1-2)product.(1)comments.entry.product.name{\"${prefix}\"' # Performance trick
    extract:
    - 'DeviceBrand                 :  324:\"${brand}\"'
    - 'DeviceName                  :  324:agent.(1-2)product.(1)comments.entry.product.name[4]=\"Build\"@{\"${prefix}\"[-3]'

- matcher:
    require:
    - 'agent.(1-2)product.comments.entry.product.name[1]=\"Android\"'
    - 'agent.(1-2)product.(1)comments.entry.product.name{\"${prefix}\"' # Performance trick
    extract:
    - 'DeviceBrand                 :  324:\"${brand}\"'
    - 'DeviceName                  :  324:agent.(1-2)product.(1)comments.entry.product.name[5]=\"Build\"@{\"${prefix}\"[-4]'

- matcher:
    require:
    - 'agent.(1-2)product.comments.entry.product.name[1]=\"Android\"'
    extract:
    - 'DeviceBrand                 :  330:\"${brand}\"'
    - 'DeviceName                  :  330:agent.(1-2)product.name{\"${prefix}\"'

- matcher:
    require:
    - 'IsNull[agent.(1-2)product.comments.entry.product.name[1]=\"Android\"]'
    extract:
    - 'DeviceBrand                 :  313:\"${brand}\"'
    - 'DeviceName                  :  313:agent.product.name{\"${prefix}\"'

- matcher:
    require:
    - 'IsNull[agent.(1-2)product.comments.entry.product.name[1]=\"Android\"]'
    extract:
    - 'DeviceBrand                 :  324:\"${brand}\"'
    - 'DeviceName                  :  324:agent.(1-2)product.name=\"${prefix}\"^.version'

- matcher:
    require:
    - 'IsNull[agent.(1-2)product.comments.entry.product.name[1]=\"Android\"]'
    extract:
    - 'DeviceBrand                 :  323:\"${brand}\"'
    - 'DeviceName                  :  323:agent.(1-2)product.(1)comments.entry.(1)product.(1)name=\"${prefix}\"^.version'

"

if [ "${prefix}" == "${brand}" ];
then
echo "
- matcher:
    require:
    - 'IsNull[agent.(1-2)product.comments.entry.product.name[1]=\"Android\"]'
    extract:
    - 'DeviceBrand                 :  322:\"${brand}\"'
    - 'DeviceName                  :  125:agent.(1-2)product.(1)comments.entry.(1)product.(1)name{\"${prefix}\"@'

- matcher:
    require:
    - 'IsNull[agent.(1-2)product.comments.entry.product.name[1]=\"Android\"]'
    extract:
    - 'DeviceBrand                 :  322:\"${brand}\"'
    - 'DeviceName                  :  322:agent.(1-2)product.(1)comments.entry.(1)product.(1)name{\"${prefix}\"@!=\"${prefix}\"'

- matcher:
    require:
    - 'IsNull[agent.(1-2)product.comments.entry.product.name[1]=\"Android\"]'
    extract:
    - 'DeviceBrand                 :  320:\"${brand}\"'
    - 'DeviceName                  :  124:agent.(1-2)product.(1)comments.entry.text{\"${prefix}\"'

- matcher:
    require:
    - 'IsNull[agent.(1-2)product.comments.entry.product.name[1]=\"Android\"]'
    extract:
    - 'DeviceBrand                 :  320:\"${brand}\"'
    - 'DeviceName                  :  320:agent.(1-2)product.(1)comments.entry.text{\"${prefix}\"@!=\"${prefix}\"'
"
else
echo "
- matcher:
    require:
    - 'IsNull[agent.(1-2)product.comments.entry.product.name[1]=\"Android\"]'
    extract:
    - 'DeviceBrand                 :  322:\"${brand}\"'
    - 'DeviceName                  :  322:agent.(1-2)product.(1)comments.entry.(1)product.(1)name{\"${prefix}\"'

- matcher:
    require:
    - 'IsNull[agent.(1-2)product.comments.entry.product.name[1]=\"Android\"]'
    extract:
    - 'DeviceBrand                 :  320:\"${brand}\"'
    - 'DeviceName                  :  320:agent.(1-2)product.(1)comments.entry.text{\"${prefix}\"'
"
fi


done

fgrep -v '#' "${INPUT}" | grep . | cut -d'|' -f2 | sort -u | while read brand;
do
echo "

- matcher:
    require:
    - 'agent.(1-2)product.(1)comments.entry.product.version{\"${brand}\"'
    - 'agent.(1-2)product.(1)comments.entry.product.name[1]=\"Build\"^.version{\"${brand}\"'
    extract:
    - 'DeviceBrand                 :   94:\"${brand}\"'

- matcher:
    require:
    - 'agent.(1-2)product.(1)comments.entry.product.version{\"${brand}\"'
    - 'agent.(1-2)product.(1)comments.entry.product.name[2]=\"Build\"^.version{\"${brand}\"'
    extract:
    - 'DeviceBrand                 :   93:\"${brand}\"'

- matcher:
    require:
    - 'agent.(1-2)product.(1)comments.entry.product.version{\"${brand}\"'
    - 'agent.(1-2)product.(1)comments.entry.product.name[3]=\"Build\"^.version{\"${brand}\"'
    extract:
    - 'DeviceBrand                 :   92:\"${brand}\"'

- matcher:
    require:
    - 'agent.(1-2)product.(1)comments.entry.product.version{\"${brand}\"'
    - 'agent.(1-2)product.(1)comments.entry.product.name[4]=\"Build\"^.version{\"${brand}\"'
    extract:
    - 'DeviceBrand                 :   91:\"${brand}\"'

- matcher:
    require:
    - 'agent.(1-2)product.(1)comments.entry.product.version{\"${brand}\"'
    - 'agent.(1-2)product.(1)comments.entry.product.name[5]=\"Build\"^.version{\"${brand}\"'
    extract:
    - 'DeviceBrand                 :   90:\"${brand}\"'

# ===================================================================================================

"
done
) > ${OUTPUT}
