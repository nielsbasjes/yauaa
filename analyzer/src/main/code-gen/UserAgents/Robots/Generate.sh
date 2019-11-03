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
    - 'DeviceClass                         :    111 :\"Robot\"'
    - 'DeviceName                          :   3111 :@RobotName'
    - 'DeviceCpu                           :   1000 :\"<<<null>>>\"'
    - 'DeviceCpuBits                       :   1000 :\"<<<null>>>\"'
    - 'OperatingSystemClass                :    111 :\"Cloud\"'
    - 'OperatingSystemName                 :    111 :\"Cloud\"'
    - 'OperatingSystemVersion              :    111 :\"??\"'
    - 'LayoutEngineClass                   :    111 :\"Robot\"'
    - 'LayoutEngineName                    :    111 :@RobotName'
    - 'LayoutEngineVersion                 :    111 :\"??\"'
    - 'AgentClass                          :    111 :\"Robot\"'
    - 'AgentName                           :    111 :@RobotName'
    - 'AgentVersion                        :    111 :\"??\"'

- matcher:
    require:
    - 'agent.product.name~\"${tag}\"@~\"mobile\"'
    extract:
    - 'DeviceClass                         :   3112 :\"Robot Mobile\"'

- matcher:
    require:
    - 'agent.product.name=\"Mobile Safari\"'
    - 'agent.product.name~\"${tag}\"'
    extract:
    - 'DeviceClass                         :   3112 :\"Robot Mobile\"'

- matcher:
    variable:
    - 'RobotProduct:agent.product.version~\"${tag}\"^'
    extract:
    - 'DeviceClass                         :    102 :\"Robot\"'
    - 'DeviceName                          :    102 :@RobotProduct.name'
    - 'DeviceCpu                           :   1000 :\"<<<null>>>\"'
    - 'DeviceCpuBits                       :   1000 :\"<<<null>>>\"'
    - 'OperatingSystemClass                :    102 :\"Cloud\"'
    - 'OperatingSystemName                 :    102 :\"Cloud\"'
    - 'OperatingSystemVersion              :    102 :\"??\"'
    - 'LayoutEngineClass                   :    102 :\"Robot\"'
    - 'LayoutEngineName                    :    102 :@RobotProduct.name'
    - 'LayoutEngineVersion                 :    102 :@RobotProduct.(1)version'
    - 'AgentClass                          :    102 :\"Robot\"'
    - 'AgentName                           :    102 :@RobotProduct.name'
    - 'AgentVersion                        :    102 :@RobotProduct.(1)version'


- matcher:
    variable:
    - 'RobotVersion:agent.product.name~\"${tag}\"^.(1)version'
    extract:
    - 'LayoutEngineVersion                 :    112 :@RobotVersion'
    - 'AgentVersion                        :    112 :@RobotVersion'

- matcher:
"

if [[ ${tag} = "bot" ]]; then
echo "    require:
    - 'IsNull[agent.product.comments.entry.product.name[1]=\"cubot\"]'"
fi

echo "    variable:
    - 'RobotName:agent.product.comments.entry.product.name~\"${tag}\"'
    extract:
    - '__Set_ALL_Fields__                  :     99 :\"<<<null>>>\"' # Must be 1 lower than the rest (or you will wipe yourself too)
    - 'DeviceClass                         :   1100 :\"Robot\"'
    - 'DeviceName                          :   1100 :@RobotName'
    - 'DeviceCpu                           :   1000 :\"<<<null>>>\"'
    - 'DeviceCpuBits                       :   1000 :\"<<<null>>>\"'
    - 'OperatingSystemClass                :   2500 :\"Cloud\"'
    - 'OperatingSystemName                 :   2500 :\"Cloud\"'
    - 'OperatingSystemVersion              :   2500 :\"??\"'
    - 'LayoutEngineClass                   :   2500 :\"Robot\"'
    - 'LayoutEngineName                    :   2500 :@RobotName'
    - 'LayoutEngineVersion                 :   2500 :@RobotName^.version'
    - 'AgentClass                          :   2500 :\"Robot\"'
    - 'AgentName                           :   2500 :@RobotName'
    - 'AgentVersion                        :   2500 :@RobotName^.version'

- matcher:
    require:"
if [[ ${tag} = "bot" ]]; then
echo "    - 'IsNull[agent.product.comments.entry.product.name[1]=\"cubot\"]'"
fi
echo "    - 'agent.product.name=\"Mobile Safari\"'
    - 'agent.product.comments.entry.product.name~\"${tag}\"'
    extract:
    - 'DeviceClass                         :   3112 :\"Robot Mobile\"'


- matcher:"
if [[ ${tag} = "bot" ]]; then
echo "    require:
    - 'IsNull[agent.product.comments.entry.text[1]=\"cubot\"]'"
fi
echo "    variable:
    - 'RobotName:agent.product.comments.entry.text~\"${tag}\"'
    extract:
    - '__Set_ALL_Fields__                  :     99 :\"<<<null>>>\"' # Must be 1 lower than the rest (or you will wipe yourself too)
    - 'DeviceClass                         :   1100 :\"Robot\"'
    - 'DeviceName                          :   1100 :@RobotName'
    - 'DeviceCpu                           :   1000 :\"<<<null>>>\"'
    - 'DeviceCpuBits                       :   1000 :\"<<<null>>>\"'
    - 'OperatingSystemClass                :    101 :\"Cloud\"'
    - 'OperatingSystemName                 :    101 :\"Cloud\"'
    - 'OperatingSystemVersion              :    101 :\"??\"'
    - 'LayoutEngineClass                   :    100 :\"Robot\"'
    - 'LayoutEngineName                    :    100 :@RobotName'
    - 'LayoutEngineVersion                 :    100 :@RobotName'
    - 'AgentClass                          :    100 :\"Robot\"'
    - 'AgentName                           :    100 :@RobotName'
    - 'AgentVersion                        :    100 :@RobotName'

- matcher:"
if [[ ${tag} = "bot" ]]; then
echo "    require:
    - 'IsNull[agent.(1-10)keyvalue.key[1]=\"cubot\"]'"
fi
echo "    variable:
    - 'RobotName:agent.(1-10)keyvalue.key~\"${tag}\"'
    extract:
    - '__Set_ALL_Fields__                  :     99 :\"<<<null>>>\"' # Must be 1 lower than the rest (or you will wipe yourself too)
    - 'DeviceClass                         :    100 :\"Robot\"'
    - 'DeviceName                          :    100 :@RobotName'
    - 'DeviceCpu                           :   1000 :\"<<<null>>>\"'
    - 'DeviceCpuBits                       :   1000 :\"<<<null>>>\"'
    - 'OperatingSystemClass                :    100 :\"Cloud\"'
    - 'OperatingSystemName                 :    100 :\"Cloud\"'
    - 'OperatingSystemVersion              :    100 :\"??\"'
    - 'LayoutEngineClass                   :    100 :\"Robot\"'
    - 'LayoutEngineName                    :    100 :@RobotName'
    - 'LayoutEngineVersion                 :    100 :@RobotName'
    - 'AgentClass                          :    100 :\"Robot\"'
    - 'AgentName                           :    100 :@RobotName'
    - 'AgentVersion                        :    100 :@RobotName'
"

fi

echo "
- matcher:
    variable:
    - 'RobotName:agent.text~\"${tag}\"'
    extract:
    - 'DeviceClass                         :    101 :\"Robot\"'
    - 'DeviceName                          :    101 :@RobotName'
    - 'DeviceCpu                           :   1000 :\"<<<null>>>\"'
    - 'DeviceCpuBits                       :   1000 :\"<<<null>>>\"'
    - 'OperatingSystemClass                :    101 :\"Cloud\"'
    - 'OperatingSystemName                 :    101 :\"Cloud\"'
    - 'OperatingSystemVersion              :    101 :\"??\"'
    - 'LayoutEngineClass                   :    101 :\"Robot\"'
    - 'LayoutEngineName                    :    101 :@RobotName'
    - 'LayoutEngineVersion                 :    101 :\"??\"'
    - 'AgentClass                          :    101 :\"Robot\"'
    - 'AgentName                           :    101 :@RobotName'
    - 'AgentVersion                        :    101 :\"??\"'

- matcher:
    require:
    - 'agent.product.comments.entry.url~\"${tag}\"'
    extract:
    - 'DeviceClass                         :    101 :\"Robot\"'
    - 'DeviceCpu                           :   1000 :\"<<<null>>>\"'
    - 'DeviceCpuBits                       :   1000 :\"<<<null>>>\"'
    - 'OperatingSystemClass                :    101 :\"Cloud\"'
    - 'OperatingSystemName                 :    101 :\"Cloud\"'
    - 'OperatingSystemVersion              :    101 :\"??\"'
    - 'LayoutEngineClass                   :    101 :\"Robot\"'
    - 'AgentClass                          :    101 :\"Robot\"'

"

done

) > "${OUTPUT}"
