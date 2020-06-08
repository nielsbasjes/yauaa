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
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

export ELK_VERSION=7.7.0

DOCKER_IMAGE=yauaa-elasticsearch:latest
CONTAINER_NAME=yauaa-elasticsearch

# First we wipe the old version of the image
docker kill ${CONTAINER_NAME}
docker rm ${CONTAINER_NAME}
docker rmi ${DOCKER_IMAGE}

# Second we build a new image with the plugin installed
docker build --build-arg ELK_VERSION="${ELK_VERSION}" -t "${DOCKER_IMAGE}" -f "${DIR}/Dockerfile" "${DIR}/../../.."

# Third we start the instance
echo "Starting ElasticSearch with plugin installed"
docker run -d --rm -p 9300:9300 -p 9200:9200 --name yauaa-elasticsearch  "${DOCKER_IMAGE}"

echo "Waiting for ElasticSearch to become operational"
until curl http://localhost:9200/_cluster/health?pretty; do sleep 5; done

echo "ElasticSearch is operational now"
