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

(cd "${DIR}/../../.." && mvn clean package)

export ELK_VERSION=7.7.0

DOCKER_IMAGE=yauaa-elasticsearch:latest
CONTAINER_NAME=yauaa-elasticsearch

# First we fully wipe the old instance of our integration test
docker kill ${CONTAINER_NAME}
docker rm ${CONTAINER_NAME}
docker rmi ${DOCKER_IMAGE}

# Second we build a new image with the plugin installed
docker build --build-arg ELK_VERSION="${ELK_VERSION}" -t "${DOCKER_IMAGE}" -f "${DIR}/Dockerfile" "${DIR}/../../.."

# Third we start the instance
echo "Starting ElasticSearch with plugin installed"
docker run -d --rm -p 9300:9300 -p 9200:9200 --name yauaa-elasticsearch "${DOCKER_IMAGE}"

killContainer() {
  docker kill ${CONTAINER_NAME}
}

trap killContainer EXIT

echo "Waiting for ElasticSearch to become operational"
count=0
until curl http://localhost:9200/_cluster/health?pretty; do
  sleep 1
  count=$((count + 1))
  if [[ $count -ge 20 ]]; then
    echo "Test FAILED"
    exit 255
  fi
done

echo "ElasticSearch is operational now"

# =================================================="
echo "Loading pipeline "

curl -H 'Content-Type: application/json' -X PUT 'localhost:9200/_ingest/pipeline/yauaa-test-pipeline_some' -d '
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
echo " done"

# =================================================="
echo "Putting record "

curl -H 'Content-Type: application/json' -X PUT 'localhost:9200/my-index/my-type/2?pipeline=yauaa-test-pipeline_some' -d '
{
  "useragent" : "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36"
}
'
echo " done"

# =================================================="
# Get result"

curl -s -H 'Content-Type: application/json' -X GET 'localhost:9200/my-index/my-type/2' |
  python -m json.tool |
  fgrep '"AgentNameVersionMajor": "Chrome 53"'

RESULT=$?

if [ "${RESULT}" == "0" ]; then
  echo "Test passed"
else
  echo "Test FAILED"
  exit 255
fi

# =================================================="
