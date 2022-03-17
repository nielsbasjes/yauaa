#!/bin/bash
#
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2022 Niels Basjes
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
#

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null && pwd)"

export ELK_VERSION=$1

DOCKER_IMAGE=yauaa-elasticsearch-8:latest
CONTAINER_NAME=yauaa-elasticsearch-8


#https://wiki.archlinux.org/index.php/Color_Bash_Prompt
# Reset
export Color_Off='\e[0m'      # Text Reset

# High Intensity
export IRed='\e[0;91m'        # Red
export IYellow='\e[0;93m'     # Yellow
export IBlue='\e[0;94m'       # Blue
export IWhite='\e[0;97m'      # White

# Bold High Intensity
export BIRed='\e[1;91m'       # Red
export BIGreen='\e[1;92m'     # Green
export BIBlue='\e[1;94m'      # Blue

function pass() {
  echo -e "${Color_Off}${IWhite}[${BIGreen}PASS${IWhite}] ${Color_Off}${1}"
}

function fail() {
  echo -e "${Color_Off}${IWhite}[${BIRed}FAIL${IWhite}] ${IYellow}${1}${Color_Off}"
}

# First we fully wipe any old instance of our integration test
echo "Removing any remaining stuff from previous test runs."
docker kill "${CONTAINER_NAME}"
docker rm   "${CONTAINER_NAME}"
docker rmi  "${DOCKER_IMAGE}"

# Second we build a new image with the plugin installed
echo "Building docker image for ElasticSearch ${ELK_VERSION} with the plugin installed."
docker build --build-arg ELK_VERSION="${ELK_VERSION}" -t "${DOCKER_IMAGE}" -f "${DIR}/Dockerfile" "${DIR}/../../.."

# Third we start the instance
echo "Starting ElasticSearch with plugin installed."
# NOTE: Using & instead of -d so you'll see the console logs of ES (which is useful if something goes wrong)
docker run --rm -p 9300:9300 -p 9200:9200 --name "${CONTAINER_NAME}" "${DOCKER_IMAGE}" &

killContainer() {
  docker kill ${CONTAINER_NAME}
}

trap killContainer EXIT

echo "Waiting for ElasticSearch to become operational"
# We wait for at most 60 seconds
count=60
until curl -s http://localhost:9200/_cluster/health?pretty; do
  sleep 1
  count=$((count - 1))
  if docker ps --filter "name=${CONTAINER_NAME}" | grep -F "${CONTAINER_NAME}" > /dev/null ;
  then
    echo -e "${Color_Off}${IWhite}[${IYellow}STARTING${IWhite}] ${Color_Off}Remaining waiting: ${count}"
    if [[ $count -eq 0 ]]; then
      fail "Had to wait too long for startup"
      exit 255
    fi
  else
    fail "Container has stopped"
    exit 255
  fi

done
echo "ElasticSearch is operational now."

# =================================================="
echo "Loading pipeline."

curl -s -H 'Content-Type: application/json' -X PUT 'localhost:9200/_ingest/pipeline/yauaa-test-pipeline_some' -d '
{
  "description": "A pipeline to do whatever",
  "processors": [
    {
      "yauaa" : {
        "field"         : "useragent",
        "target_field"  : "parsed",
        "fieldNames"    : [ "DeviceClass", "DeviceBrand", "DeviceName", "AgentNameVersionMajor", "FirstProductName" ],
        "cacheSize" : 10,
        "preheat"   : 10,
        "extraRules" : "config:\n- matcher:\n    extract:\n      - '"'"'FirstProductName     : 1 :agent.(1)product.(1)name'"'"'\n"
      }
    }
  ]
}
'
# =================================================="
# Get result"
echo "Simulate the pipeline and check if it contains the desired values."
TESTLOG="${DIR}/es-simulation.log"

curl -s -H 'Content-Type: application/json' -X POST 'localhost:9200/_ingest/pipeline/yauaa-test-pipeline_some/_simulate' -d '
{
  "docs": [
    {
      "_source": {
        "useragent" : "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36"
      }
    }
  ]
}' > "${TESTLOG}"


function checkLog() {
  expected="${1}"
  cat "${TESTLOG}" | \
    python -m json.tool | \
    grep -F "${expected}" > /dev/null 2>&1

  RESULT=$?

  if [ "${RESULT}" == "0" ]; then
    pass "${expected}"
  else
    fail "${expected}"
    cat "${TESTLOG}" | \
      python -m json.tool
    exit 255
  fi
}

checkLog '"AgentInformationEmail": "Unknown"'
checkLog '"AgentInformationUrl": "Unknown"'
checkLog '"AgentName": "Chrome"'
checkLog '"AgentNameVersionMajor": "Chrome 53"'
checkLog '"AgentVersion": "53.0.2785.124"'
checkLog '"AgentVersionMajor": "53"'
checkLog '"DeviceBrand": "Google"'
checkLog '"DeviceClass": "Phone"'
checkLog '"DeviceName": "Google Nexus 6"'
checkLog '"FirstProductName": "Mozilla"'
checkLog '"__SyntaxError__": "false"'

# =================================================="
