#!/bin/bash
#
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2020 Niels Basjes
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

DOCKER_IMAGE=yauaa-logstash:latest
CONTAINER_NAME=yauaa-logstash

# First we fully wipe any old instance of our integration test
echo "Removing any remaining stuff from previous test runs."
docker kill "${CONTAINER_NAME}"
docker rm "${CONTAINER_NAME}"
docker rmi "${DOCKER_IMAGE}"

# Second we build a new image with the plugin installed
echo "Building docker image for Elastic LogStash ${ELK_VERSION} with the plugin installed."
docker build --build-arg ELK_VERSION="${ELK_VERSION}" -t "${DOCKER_IMAGE}" -f "${DIR}/Dockerfile" "${DIR}/../../.."

# Third we start the instance
echo "Starting Elastic LogStash with plugin installed."
docker run --rm --name "${CONTAINER_NAME}" "${DOCKER_IMAGE}" | \
  grep -F '"userAgentAgentNameVersion"' | \
  grep -F '"Chrome 53.0.2785.124"'

RESULT=$?

if [ "${RESULT}" == "0" ]; then
  echo "Test passed"
else
  echo "Test FAILED"
  exit 255
fi
