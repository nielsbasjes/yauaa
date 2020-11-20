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

SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

echo "PWD: ${SCRIPTDIR}"

cd "${SCRIPTDIR}" || ( echo "This should not be possible" ; exit 1 )

OS=centos8
PROJECTNAME=yauaa
USER=$(id -un)
CONTAINER_NAME=${PROJECTNAME}-${OS}-${USER}-$$
DOCKER_BUILD="docker build"

if [ -n "${INSIDE_DOCKER+x}" ];
then
  echo "Nothing to do: You are already INSIDE the docker environment"
  exit 1;
fi

if [[ "$(docker images -q ${PROJECTNAME}-${OS} 2> /dev/null)" == "" ]]; then
#  DOCKER_BUILD="docker build"
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
#  DOCKER_BUILD="docker build -q"
  echo "Loading Yauaa development environment"
fi

${DOCKER_BUILD} -t ${PROJECTNAME}-${OS} docker/${OS}

buildStatus=$?

if [ ${buildStatus} -ne 0 ];
then
    echo "Building the docker image failed."
    exit ${buildStatus}
fi

DOCKER_SOCKET=/var/run/docker.sock

if [ "$(uname -s)" == "Linux" ]; then
  USER_NAME=${SUDO_USER:=${USER}}
  USER_ID=$(id -u "${USER_NAME}")
  GROUP_ID=$(id -g "${USER_NAME}")
else # boot2docker uid and gid
  USER_NAME=${USER}
  USER_ID=1000
  GROUP_ID=50
fi

EXTRA_DOCKER_STEPS=""

if [ -f "${HOME}/.gitconfig" ];
then
  cp "${HOME}/.gitconfig" ___git_config_for_docker
  EXTRA_DOCKER_STEPS="ADD ___git_config_for_docker /home/${USER}/.gitconfig"
fi

DOCKER_GROUP_ID=$(getent group docker | cut -d':' -f3)

cat - > ___UserSpecificDockerfile << UserSpecificDocker
FROM ${PROJECTNAME}-${OS}
RUN bash /scripts/configure-for-user.sh "${USER_NAME}" "${USER_ID}" "${GROUP_ID}" "$(grep -F vboxsf /etc/group)"
#RUN groupmod -g ${DOCKER_GROUP_ID} docker
${EXTRA_DOCKER_STEPS}
UserSpecificDocker

${DOCKER_BUILD} -t "${PROJECTNAME}-${OS}-${USER_NAME}" -f ___UserSpecificDockerfile .

buildStatus=$?

if [ ${buildStatus} -ne 0 ];
then
    echo "Building the user specific docker image failed."
    exit ${buildStatus}
fi

rm -f ___git_config_for_docker ___UserSpecificDockerfile

echo ""
echo "Docker image build completed."
echo "=============================================================================================="
echo ""

# Do NOT Map the real ~/.m2 directory !!!
[ -d "${PWD}/docker/_m2"    ] || mkdir "${PWD}/docker/_m2"
[ -d "${PWD}/docker/_gnupg" ] || mkdir "${PWD}/docker/_gnupg"

MOUNTGPGDIR="${PWD}/docker/_gnupg"

if [[ "${1}" == "RELEASE" ]];
then
  cp "${HOME}"/.m2/*.xml "${PWD}/docker/_m2"
  MOUNTGPGDIR="${HOME}/.gnupg"

  echo "Setting up for release process"
fi

DOCKER_INTERACTIVE_RUN=${DOCKER_INTERACTIVE_RUN-"-i -t"}

DOCKER_SOCKET_MOUNT=""
if [ -S /var/run/docker.sock ];
then
  DOCKER_SOCKET_MOUNT="-v /var/run/docker.sock:/var/run/docker.sock${V_OPTS:-}"
  echo "Enabling Docker support with the docker build environment."
else
  echo "There is NO Docker support with the docker build environment."
fi

COMMAND=( "$@" )
if [ $# -eq 0 ];
then
  COMMAND=( "bash" "-i" )
fi

# man docker-run
# When using SELinux, mounted directories may not be accessible
# to the container. To work around this, with Docker prior to 1.7
# one needs to run the "chcon -Rt svirt_sandbox_file_t" command on
# the directories. With Docker 1.7 and later the z mount option
# does this automatically.
# Since Docker 1.7 was release 5 years ago we only support 1.7 and newer.
V_OPTS=:z

docker run --rm=true -i -t                                      \
       -u "${USER_NAME}"                                        \
       -v "${PWD}:/home/${USER_NAME}/${PROJECTNAME}${V_OPTS:-}" \
       -v "${HOME}/.m2:/home/${USER_NAME}/.m2${V_OPTS:-}" \
       -v "${MOUNTGPGDIR}:/home/${USER_NAME}/.gnupg${V_OPTS:-}" \
       ${DOCKER_SOCKET_MOUNT}                                   \
       -w "/home/${USER}/${PROJECTNAME}"                        \
       -p 4000:4000                                             \
       -p 35729:35729                                           \
       --name "${CONTAINER_NAME}"                               \
       "${PROJECTNAME}-${OS}-${USER_NAME}"                      \
       "${COMMAND[@]}"

exit 0
