#!/bin/bash
#
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
# distributed under the License is distributed on an AS IS BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

[ -f "httpdlog-pigloader-4.0-udf.jar" ] || wget https://repo1.maven.org/maven2/nl/basjes/parse/httpdlog/httpdlog-pigloader/4.0/httpdlog-pigloader-4.0-udf.jar
rsync ../../udfs/logparser/target/yauaa-logparser-*-udf.jar yauaa-logparser.jar

if [ -z "${USER}" ]; then
  USER=$(id -un)
fi

PROJECTNAME=examples
CONTAINER_NAME=${PROJECTNAME}-${USER}-$$

docker build -t ${PROJECTNAME} docker

if [ "$(uname -s)" == "Linux" ]; then
  USER_NAME=${SUDO_USER:=${USER}}
  USER_ID=$(id -u "${USER_NAME}")
  GROUP_ID=$(id -g "${USER_NAME}")
else # boot2docker uid and gid
  USER_NAME="${USER}"
  USER_ID=1000
  GROUP_ID=50
fi

docker build -t "${PROJECTNAME}-${USER_NAME}" - <<UserSpecificDocker
FROM ${PROJECTNAME}
RUN groupadd -g ${GROUP_ID} ${USER_NAME} || true
RUN useradd -g ${GROUP_ID} -u ${USER_ID} -k /root -m ${USER_NAME}
ENV HOME /home/${USER_NAME}
UserSpecificDocker

docker run --rm=true -t -i                               \
           -u "${USER_NAME}"                             \
           -v "${PWD}:/home/${USER_NAME}/${PROJECTNAME}" \
           -w "/home/${USER}/${PROJECTNAME}"             \
           --name "${CONTAINER_NAME}"                    \
           "${PROJECTNAME}-${USER_NAME}"                 \
           pig -x local TopOperatingSystems.pig

exit 0
