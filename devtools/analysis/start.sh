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

cd "${DIR}" || exit 1

ELK_VERSION=$(cd .. && mvn help:evaluate -Dexpression=elasticsearch-8.version -q -DforceStdout)
YAUAA_VERSION=$(cd .. && mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

docker rmi analysis_elasticsearch:latest

echo "ELK_VERSION=${ELK_VERSION}" > .env
echo "YAUAA_VERSION=${YAUAA_VERSION}" >>.env

#cp ../../udfs/elastic/elasticsearch-7/target/yauaa-elasticsearch-7-${YAUAA_VERSION}.zip .

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

function info() {
  echo -e "${Color_Off}${IWhite}[${BIBlue}INFO${IWhite}] ${Color_Off}${1}"
}

function pass() {
  echo -e "${Color_Off}${IWhite}[${BIGreen}PASS${IWhite}] ${Color_Off}${1}"
}

function fail() {
  echo -e "${Color_Off}${IWhite}[${BIRed}FAIL${IWhite}] ${IYellow}${1}${Color_Off}"
}

# Third we start the instance
echo "Building ElasticSearch image with plugin installed."
docker-compose build

# Third we start the instance
echo "Starting ElasticSearch with plugin installed."
docker-compose up -d

#killContainer() {
#  docker-compose down
#}
#
##trap killContainer EXIT
#
info "Waiting for ElasticSearch to become operational"
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
pass "ElasticSearch is operational now."

## =================================================="
#info "Loading pipeline."
#
#curl -s -H 'Content-Type: application/json' -X PUT 'localhost:9200/_ingest/pipeline/yauaa' -d '
#{
#  "description": "A pipeline parse the useragents",
#  "processors": [
#    {
#      "yauaa" : {
#        "field_to_header_mapping" : {
#            "ua":                       "User-Agent"
#        },
#        "target_field"  : "parsed",
#        "cacheSize" : 10000
#      }
#    }
#  ]
#}
#'
