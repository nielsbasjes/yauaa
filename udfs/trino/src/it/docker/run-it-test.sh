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

MODULE_DIR="${DIR}/../../.."
TARGET_IT_DIR="${MODULE_DIR}/target/it/"

mkdir -p "${TARGET_IT_DIR}"

export TRINO_VERSION=$1
export YAUAA_VERSION=$2

DOCKER_IMAGE=yauaa-trino-it-test:${YAUAA_VERSION}
CONTAINER_NAME=yauaa-trino-it-test

# ---------------------------------------------------------------------------
# First we fully wipe any old instance of our integration test
echo "Removing any remaining stuff from previous test runs."
docker kill "${CONTAINER_NAME}"
docker rm "${CONTAINER_NAME}"
docker rmi "${DOCKER_IMAGE}"

# ---------------------------------------------------------------------------
# Second we build a new image with the plugin installed
echo "Building docker image for Trino ${TRINO_VERSION} with the plugin installed."
docker build --build-arg TRINO_VERSION="${TRINO_VERSION}" --build-arg YAUAA_VERSION="${YAUAA_VERSION}" -t "${DOCKER_IMAGE}" -f "${DIR}/Dockerfile" "${MODULE_DIR}"

# ---------------------------------------------------------------------------
# Third we start the instance
echo "Starting Trino with plugin installed."
docker run --rm -d --name "${CONTAINER_NAME}" "${DOCKER_IMAGE}"

# ---------------------------------------------------------------------------
# Wait for the server to start
SERVER_LOG="${TARGET_IT_DIR}/ServerOutput.log"
STATUS="Stopped"
# Startup may take at most 60 seconds
SERVER_START_TTL=60

while [ "${STATUS}" == "Stopped" ];
do
  echo "Waiting for Trino server to complete startup... (${SERVER_START_TTL}) "
  sleep  1s
  docker logs "${CONTAINER_NAME}" > "${SERVER_LOG}" 2>&1
  grep -F "SERVER STARTED" "${SERVER_LOG}"
  RESULT=$?
  if [ "${RESULT}" == "0" ];
  then
    echo "Trino server is running!"
    STATUS="Running"
  fi
  SERVER_START_TTL=$((SERVER_START_TTL-1))
  if [ ${SERVER_START_TTL} -le 0 ];
  then
    echo "The Server did not start within 60 seconds (normal is < 10 seconds)"
    echo "Server output:"
    cat "${SERVER_LOG}"
    exit 255
  fi
done

# ---------------------------------------------------------------------------
# First test: Is the function visible?
TEST_OUTPUT_FILE="${TARGET_IT_DIR}/OutputOfShowFunctions_1.log"
docker exec "${CONTAINER_NAME}" \
  bash -c "echo 'show functions;' | trino" \
  > "${TEST_OUTPUT_FILE}" 2>&1

grep -F -i yauaa "${TEST_OUTPUT_FILE}"
RESULT=$?

echo -n "Test 1: Is function registered: "
if [ "${RESULT}" == "0" ]; then
  echo "PASS"
else
  echo "FAIL"
  cat "${TEST_OUTPUT_FILE}"
  exit 255
fi

# ---------------------------------------------------------------------------
# Second test: Does the function provide the expected output?
TEST_OUTPUT_FILE="${TARGET_IT_DIR}/OutputOfUsingTheFunction_2.log"

docker exec "${CONTAINER_NAME}" \
  bash -c "echo \"select parse_user_agent('Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36');\" | trino" \
  > "${TEST_OUTPUT_FILE}"  2>&1

grep -F 'AgentNameVersion=Chrome 98.0.4758.102' "${TEST_OUTPUT_FILE}"
RESULT=$?

echo -n "Test 2: Parsing works: "
if [ "${RESULT}" == "0" ]; then
  echo "PASS"
else
  echo "FAIL"
  cat "${TEST_OUTPUT_FILE}"
  exit 255
fi

# ---------------------------------------------------------------------------
# Third test: Does the function provide the expected output (i.e. a map of values) ?
TEST_OUTPUT_FILE="${TARGET_IT_DIR}/OutputOfUsingTheFunction_3.log"

docker exec "${CONTAINER_NAME}" \
  bash -c "echo \"select parse_user_agent('Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36')['AgentNameVersion'];\" | trino" \
  > "${TEST_OUTPUT_FILE}"  2>&1

grep -F 'Chrome 98.0.4758.102' "${TEST_OUTPUT_FILE}"
RESULT=$?

echo -n "Test 3: Parsing works: "
if [ "${RESULT}" == "0" ]; then
  echo "PASS"
else
  echo "FAIL"
  cat "${TEST_OUTPUT_FILE}"
  exit 255
fi

# ---------------------------------------------------------------------------
# Cleanup
docker kill "${CONTAINER_NAME}"
docker rm "${CONTAINER_NAME}"
docker rmi "${DOCKER_IMAGE}"
