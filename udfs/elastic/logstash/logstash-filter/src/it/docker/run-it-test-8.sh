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

DOCKER_IMAGE=yauaa-logstash-8:latest
CONTAINER_NAME=yauaa-logstash-8


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
  echo -e "${Color_Off}${IWhite}[${IYellow}INFO${IWhite}] ${Color_Off}${1}"
}

function pass() {
  echo -e "${Color_Off}${IWhite}[${BIGreen}PASS${IWhite}] ${Color_Off}${1}"
}

function fail() {
  echo -e "${Color_Off}${IWhite}[${BIRed}FAIL${IWhite}] ${IYellow}${1}${Color_Off}"
}

# First we fully wipe any old instance of our integration test
info "Removing any remaining stuff from previous test runs."
docker kill "${CONTAINER_NAME}"
docker rm "${CONTAINER_NAME}"
docker rmi "${DOCKER_IMAGE}"

# Second we build a new image with the plugin installed
info "Building docker image for Elastic LogStash ${ELK_VERSION} with the plugin installed."
docker build --build-arg ELK_VERSION="${ELK_VERSION}" -t "${DOCKER_IMAGE}" -f "${DIR}/Dockerfile" "${DIR}/../../.."

TESTLOG="${DIR}/test-output-8.log"

# Third we start the instance
info "Running Elastic LogStash ${ELK_VERSION} with plugin installed."
docker run --rm --name "${CONTAINER_NAME}" "${DOCKER_IMAGE}" > ${TESTLOG} 2>&1

function checkLog() {
  expected="${1}"
  cat "${TESTLOG}" | \
    grep "${expected}" > /dev/null 2>&1

  RESULT=$?

  if [ "${RESULT}" == "0" ]; then
    pass "${expected}"
  else
    fail "${expected}"
    cat "${TESTLOG}"
    exit 255
  fi
}

checkLog "\"logstash.version\"=>\"${ELK_VERSION}\""
checkLog '"userAgentAgentNameVersion".*"Chrome 53.0.2785.124"'
