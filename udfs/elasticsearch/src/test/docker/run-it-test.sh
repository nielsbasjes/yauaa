#!/bin/bash
#
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2021 Niels Basjes
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

DOCKER_IMAGE=yauaa-elasticsearch:latest
CONTAINER_NAME=yauaa-elasticsearch

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
# NOTE: Using & instead of -d so you'll see the console logs os ES (which is useful if something goes wrong)
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
    echo -- "- Remaining waiting: ${count}"
    if [[ $count -eq 0 ]]; then
      echo "Test FAILED (had to wait too long)"
      exit 255
    fi
  else
    echo "Test FAILED (container has stopped)"
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
echo "Putting record."

curl -s -H 'Content-Type: application/json' -X PUT 'localhost:9200/my-index/my-type/2?pipeline=yauaa-test-pipeline_some' -d '
{
  "useragent" : "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36"
}
'

# =================================================="
# Get result"
echo "Retrieving result and checking if it contains the desired value."

curl -s -H 'Content-Type: application/json' -X GET 'localhost:9200/my-index/my-type/2' | \
  python -m json.tool | \
  grep -F '"AgentNameVersionMajor"' | \
  grep -F '"Chrome 53"'

RESULT=$?

if [ "${RESULT}" == "0" ]; then
  echo "Test passed"
else
  echo "Test FAILED"
  exit 255
fi

# =================================================="
