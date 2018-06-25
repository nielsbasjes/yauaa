#!/bin/bash
#
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2018 Niels Basjes
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

OS=centos7
PROJECTNAME=yauaa
CONTAINER_NAME=${PROJECTNAME}-${OS}-${USER}-$$
DOCKER_BUILD="docker build"

if [[ "$(docker images -q ${PROJECTNAME}-${OS} 2> /dev/null)" == "" ]]; then
  DOCKER_BUILD="docker build"
cat << "Welcome-message"

 _____      _   _   _                                             _                                      _
/  ___|    | | | | (_)                                           (_)                                    | |
\ `--.  ___| |_| |_ _ _ __   __ _   _   _ _ __     ___ _ ____   ___ _ __ ___  _ __  _ __ ___   ___ _ __ | |_
 `--. \/ _ \ __| __| | '_ \ / _` | | | | | '_ \   / _ \ '_ \ \ / / | '__/ _ \| '_ \| '_ ` _ \ / _ \ '_ \| __|
/\__/ /  __/ |_| |_| | | | | (_| | | |_| | |_) | |  __/ | | \ V /| | | | (_) | | | | | | | | |  __/ | | | |_
\____/ \___|\__|\__|_|_| |_|\__, |  \__,_| .__/   \___|_| |_|\_/ |_|_|  \___/|_| |_|_| |_| |_|\___|_| |_|\__|
                             __/ |       | |
                            |___/        |_|

 For building Yet Another UserAgent Analyzer

 This will take a few minutes...
Welcome-message
else
  DOCKER_BUILD="docker build -q"
  echo "Loading Yauaa development environment"
fi

${DOCKER_BUILD} -t ${PROJECTNAME}-${OS} docker/${OS}

if [ "$(uname -s)" == "Linux" ]; then
  USER_NAME=${SUDO_USER:=${USER}}
  USER_ID=$(id -u ${USER_NAME})
  GROUP_ID=$(id -g ${USER_NAME})
else # boot2docker uid and gid
  USER_NAME=${USER}
  USER_ID=1000
  GROUP_ID=50
fi

${DOCKER_BUILD} -t ${PROJECTNAME}-${OS}-${USER_NAME} - <<UserSpecificDocker
FROM ${PROJECTNAME}-${OS}
RUN bash /scripts/configure-for-user.sh ${USER_NAME} ${USER_ID} ${GROUP_ID} "$(fgrep vboxsf /etc/group)"
UserSpecificDocker

# Do NOT Map the real ~/.m2 directory !!!
[ -d ${PWD}/docker/_m2    ] || mkdir ${PWD}/docker/_m2
[ -d ${PWD}/docker/_gnupg ] || mkdir ${PWD}/docker/_gnupg

docker run --rm=true -t -i                                    \
           -u ${USER_NAME}                                    \
           -v ${PWD}:/home/${USER_NAME}/${PROJECTNAME}        \
           -v ${PWD}/docker/_m2:/home/${USER_NAME}/.m2        \
           -v ${PWD}/docker/_gnupg:/home/${USER_NAME}/.gnupg  \
           -w /home/${USER}/${PROJECTNAME}                    \
           --name "${CONTAINER_NAME}"                         \
           ${PROJECTNAME}-${OS}-${USER_NAME}                  \
           bash

exit 0
