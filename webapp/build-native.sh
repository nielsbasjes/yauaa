#!/bin/bash
#
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2025 Niels Basjes
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

SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

echo "PWD: ${SCRIPTDIR}"

cd "${SCRIPTDIR}/.." || ( echo "This should not be possible" ; exit 1 )

PROJECTNAME=yauaa

if [ "$(uname -s)" == "Linux" ]; then
  USER_NAME=${SUDO_USER:=${USER}}
  USER_ID=$(id -u "${USER_NAME}")
  GROUP_ID=$(id -g "${USER_NAME}")
else # boot2docker uid and gid
  USER_NAME=${USER}
  USER_ID=1000
  GROUP_ID=50
fi

#mvn clean install -PskipQuality

docker run --rm=true -ti                                        \
       -u "${USER_ID}:${GROUP_ID}"                              \
       -v "${PWD}:/home/${USER_NAME}/${PROJECTNAME}${V_OPTS:-}" \
       -v "${HOME}/.m2:/home/${USER_NAME}/.m2${V_OPTS:-}"       \
       -e "HOME=/home/${USER_NAME}/"                            \
       -w "/home/${USER}/${PROJECTNAME}"                        \
       --entrypoint bash \
       "ghcr.io/graalvm/native-image-community:21.0.1"                   \
       ./mvnw clean native:compile -Pnative -PskipQuality -pl :yauaa-webapp
