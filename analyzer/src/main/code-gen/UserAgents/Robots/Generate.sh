#!/bin/bash
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2019 Niels Basjes
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
SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
TARGETDIR=$(cd "${SCRIPTDIR}/../../../resources/UserAgents"; pwd)

OUTPUT="${TARGETDIR}/RobotBaseRules.yaml"

if [ "Generate.sh" -ot "${OUTPUT}" ]; then
    echo "Up to date: ${OUTPUT}";
    exit;
fi

echo "Generating: ${OUTPUT}";

(
echo "# ============================================="
echo "# THIS FILE WAS GENERATED; DO NOT EDIT MANUALLY"
echo "# ============================================="
echo "#"
echo "# Yet Another UserAgent Analyzer"
echo "# Copyright (C) 2013-2019 Niels Basjes"
echo "#"
echo "# Licensed under the Apache License, Version 2.0 (the \"License\");"
echo "# you may not use this file except in compliance with the License."
echo "# You may obtain a copy of the License at"
echo "#"
echo "# https://www.apache.org/licenses/LICENSE-2.0"
echo "#"
echo "# Unless required by applicable law or agreed to in writing, software"
echo "# distributed under the License is distributed on an \"AS IS\" BASIS,"
echo "# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied."
echo "# See the License for the specific language governing permissions and"
echo "# limitations under the License."
echo "#"

echo "config:"

for tag in spider crawl bot scan checker;
do

echo "# =============== Robots with \"${tag}\" in it ====================="

if [[ ${tag} != "checker" ]]; then

echo "
- matcher:
    variable:
    - 'RobotName:agent.product.name~\"${tag}\"'
    extract:
    - 'DeviceClass                         :    11100 :\"Robot\"'
    - 'DeviceName                          :   311100 :@RobotName'
    - 'DeviceBrand                         :     1000 :\"<<<null>>>\"'
    - 'DeviceVersion                       :     1000 :\"<<<null>>>\"'
    - 'DeviceCpu                           :   100000 :\"<<<null>>>\"'
    - 'DeviceCpuBits                       :   100000 :\"<<<null>>>\"'
    - 'OperatingSystemClass                :    11100 :\"Cloud\"'
    - 'OperatingSystemName                 :    11100 :\"Cloud\"'
    - 'OperatingSystemVersion              :    11100 :\"??\"'
    - 'LayoutEngineClass                   :    11100 :\"Robot\"'
    - 'LayoutEngineName                    :    11100 :@RobotName'
    - 'LayoutEngineVersion                 :    11100 :\"??\"'
    - 'AgentClass                          :    11100 :\"Robot\"'
    - 'AgentName                           :    11100 :@RobotName'
    - 'AgentVersion                        :    11100 :\"??\"'

- matcher:
    require:
    - 'agent.product.name~\"${tag}\"@~\"mobile\"'
    extract:
    - 'DeviceClass                         :   311200 :\"Robot Mobile\"'

- matcher:
    require:
    - 'agent.product.name=\"Mobile Safari\"'
    - 'agent.product.name~\"${tag}\"'
    extract:
    - 'DeviceClass                         :   311200 :\"Robot Mobile\"'

- matcher:
    require:
    - 'agent.product.name=\"Mobile Safari\"'
    - 'agent.text~\"${tag}\"'
    extract:
    - 'DeviceClass                         :   311200 :\"Robot Mobile\"'


- matcher:
    variable:
    - 'RobotProduct:agent.product.version~\"${tag}\"^'
    extract:
    - 'DeviceClass                         :    10200 :\"Robot\"'
    - 'DeviceName                          :    10200 :@RobotProduct.name'
    - 'DeviceBrand                         :     1000 :\"<<<null>>>\"'
    - 'DeviceVersion                       :     1000 :\"<<<null>>>\"'
    - 'DeviceCpu                           :   100000 :\"<<<null>>>\"'
    - 'DeviceCpuBits                       :   100000 :\"<<<null>>>\"'
    - 'OperatingSystemClass                :    10200 :\"Cloud\"'
    - 'OperatingSystemName                 :    10200 :\"Cloud\"'
    - 'OperatingSystemVersion              :    10200 :\"??\"'
    - 'LayoutEngineClass                   :    10200 :\"Robot\"'
    - 'LayoutEngineName                    :    10200 :@RobotProduct.name'
    - 'LayoutEngineVersion                 :    10200 :@RobotProduct.(1)version'
    - 'AgentClass                          :    10200 :\"Robot\"'
    - 'AgentName                           :    10200 :@RobotProduct.name'
    - 'AgentVersion                        :    10200 :@RobotProduct.(1)version'


- matcher:
    variable:
    - 'RobotVersion:agent.product.name~\"${tag}\"^.(1)version'
    extract:
    - 'LayoutEngineVersion                 :    11200 :@RobotVersion'
    - 'AgentVersion                        :    11200 :@RobotVersion'

- matcher:
"

if [[ ${tag} = "bot" ]]; then
echo "    require:
    - 'IsNull[agent.product.comments.entry.product.name[1]=\"cubot\"]'"
fi

echo "    variable:
    - 'RobotName:agent.product.comments.entry.product.name~\"${tag}\"'
    extract:
    - '__Set_ALL_Fields__                  :     9900 :\"<<<null>>>\"' # Must be 1 lower than the rest (or you will wipe yourself too)
    - 'DeviceClass                         :   110000 :\"Robot\"'
    - 'DeviceName                          :   110000 :@RobotName'
    - 'DeviceBrand                         :     1000 :\"<<<null>>>\"'
    - 'DeviceVersion                       :     1000 :\"<<<null>>>\"'
    - 'DeviceCpu                           :   100000 :\"<<<null>>>\"'
    - 'DeviceCpuBits                       :   100000 :\"<<<null>>>\"'
    - 'OperatingSystemClass                :   250000 :\"Cloud\"'
    - 'OperatingSystemName                 :   250000 :\"Cloud\"'
    - 'OperatingSystemVersion              :   250000 :\"??\"'
    - 'LayoutEngineClass                   :   250000 :\"Robot\"'
    - 'LayoutEngineName                    :   250000 :@RobotName'
    - 'LayoutEngineVersion                 :   250000 :@RobotName^.version'
    - 'AgentClass                          :   250000 :\"Robot\"'
    - 'AgentName                           :   250000 :@RobotName'
    - 'AgentVersion                        :   250000 :@RobotName^.version'

- matcher:
    require:"
if [[ ${tag} = "bot" ]]; then
echo "    - 'IsNull[agent.product.comments.entry.product.name[1]=\"cubot\"]'"
fi
echo "    - 'agent.product.name=\"Mobile Safari\"'
    - 'agent.product.comments.entry.product.name~\"${tag}\"'
    extract:
    - 'DeviceClass                         :   311200 :\"Robot Mobile\"'


- matcher:"
if [[ ${tag} = "bot" ]]; then
echo "    require:
    - 'IsNull[agent.product.comments.entry.text[1]=\"cubot\"]'"
fi
echo "    variable:
    - 'RobotName:agent.product.comments.entry.text~\"${tag}\"'
    extract:
    - '__Set_ALL_Fields__                  :     9900 :\"<<<null>>>\"' # Must be 1 lower than the rest (or you will wipe yourself too)
    - 'DeviceClass                         :   110000 :\"Robot\"'
    - 'DeviceName                          :   110000 :@RobotName'
    - 'DeviceBrand                         :     1000 :\"<<<null>>>\"'
    - 'DeviceVersion                       :     1000 :\"<<<null>>>\"'
    - 'DeviceCpu                           :   100000 :\"<<<null>>>\"'
    - 'DeviceCpuBits                       :   100000 :\"<<<null>>>\"'
    - 'OperatingSystemClass                :    10100 :\"Cloud\"'
    - 'OperatingSystemName                 :    10100 :\"Cloud\"'
    - 'OperatingSystemVersion              :    10100 :\"??\"'
    - 'LayoutEngineClass                   :    10000 :\"Robot\"'
    - 'LayoutEngineName                    :    10000 :@RobotName'
    - 'LayoutEngineVersion                 :    10000 :@RobotName'
    - 'AgentClass                          :    10000 :\"Robot\"'
    - 'AgentName                           :    10000 :@RobotName'
    - 'AgentVersion                        :    10000 :@RobotName'

- matcher:"
if [[ ${tag} = "bot" ]]; then
echo "    require:
    - 'IsNull[agent.(1-10)keyvalue.key[1]=\"cubot\"]'"
fi
echo "    variable:
    - 'RobotName:agent.(1-10)keyvalue.key~\"${tag}\"'
    extract:
    - '__Set_ALL_Fields__                  :     9900 :\"<<<null>>>\"' # Must be 1 lower than the rest (or you will wipe yourself too)
    - 'DeviceClass                         :    10000 :\"Robot\"'
    - 'DeviceName                          :    10000 :@RobotName'
    - 'DeviceBrand                         :     1000 :\"<<<null>>>\"'
    - 'DeviceVersion                       :     1000 :\"<<<null>>>\"'
    - 'DeviceCpu                           :   100000 :\"<<<null>>>\"'
    - 'DeviceCpuBits                       :   100000 :\"<<<null>>>\"'
    - 'OperatingSystemClass                :    10000 :\"Cloud\"'
    - 'OperatingSystemName                 :    10000 :\"Cloud\"'
    - 'OperatingSystemVersion              :    10000 :\"??\"'
    - 'LayoutEngineClass                   :    10000 :\"Robot\"'
    - 'LayoutEngineName                    :    10000 :@RobotName'
    - 'LayoutEngineVersion                 :    10000 :@RobotName'
    - 'AgentClass                          :    10000 :\"Robot\"'
    - 'AgentName                           :    10000 :@RobotName'
    - 'AgentVersion                        :    10000 :@RobotName'
"

fi

echo "
- matcher:
    variable:
    - 'RobotName:agent.text~\"${tag}\"'
    extract:
    - 'DeviceClass                         :    10100 :\"Robot\"'
    - 'DeviceName                          :    10100 :@RobotName'
    - 'DeviceBrand                         :     1000 :\"<<<null>>>\"'
    - 'DeviceVersion                       :     1000 :\"<<<null>>>\"'
    - 'DeviceCpu                           :   100000 :\"<<<null>>>\"'
    - 'DeviceCpuBits                       :   100000 :\"<<<null>>>\"'
    - 'OperatingSystemClass                :    10100 :\"Cloud\"'
    - 'OperatingSystemName                 :    10100 :\"Cloud\"'
    - 'OperatingSystemVersion              :    10100 :\"??\"'
    - 'LayoutEngineClass                   :    10100 :\"Robot\"'
    - 'LayoutEngineName                    :    10100 :@RobotName'
    - 'LayoutEngineVersion                 :    10100 :\"??\"'
    - 'AgentClass                          :    10100 :\"Robot\"'
    - 'AgentName                           :    10100 :@RobotName'
    - 'AgentVersion                        :    10100 :\"??\"'

- matcher:
    require:
    - 'agent.product.comments.entry.url~\"${tag}\"'
    extract:
    - 'DeviceClass                         :    10100 :\"Robot\"'
    - 'DeviceBrand                         :     1000 :\"<<<null>>>\"'
    - 'DeviceVersion                       :     1000 :\"<<<null>>>\"'
    - 'DeviceCpu                           :   100000 :\"<<<null>>>\"'
    - 'DeviceCpuBits                       :   100000 :\"<<<null>>>\"'
    - 'OperatingSystemClass                :    10100 :\"Cloud\"'
    - 'OperatingSystemName                 :    10100 :\"Cloud\"'
    - 'OperatingSystemVersion              :    10100 :\"??\"'
    - 'LayoutEngineClass                   :    10100 :\"Robot\"'
    - 'AgentClass                          :    10100 :\"Robot\"'

"

done

) > "${OUTPUT}"
