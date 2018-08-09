#!/bin/bash

#if [[ "$(docker images -q local-gitbook 2> /dev/null)" == "" ]]; then
  docker build -t local-gitbook main/gitbook
#fi

if [ -z "${USER+x}" ];
then
    USER=$(id -u)
fi

if [ "$(uname -s)" == "Linux" ]; then
  USER_NAME=${SUDO_USER:=${USER}}
  USER_ID=$(id -u "${USER_NAME}")
  GROUP_ID=$(id -g "${USER_NAME}")
else # boot2docker uid and gid
  USER_NAME=${USER}
  USER_ID=1000
  GROUP_ID=50
fi

IMGDIR="/tmp/local-gitbook-docker-${USER_NAME}-$$/"
mkdir -p "${IMGDIR}"
cat - >  "${IMGDIR}/Dockerfile" <<UserSpecificDocker
FROM local-gitbook
RUN bash /configure-for-user.sh ${USER_NAME} ${USER_ID} ${GROUP_ID} "$(grep -Fs vboxsf /etc/group)"
UserSpecificDocker

USER_IMAGE_NAME="local-gitbook-docker-${USER_NAME}"

DOCKER="docker run --rm=true -t -i"
DOCKER=${DOCKER}" -u $(id ${USER_NAME} -u)"

DOCKER=${DOCKER}" -p 4000:4000"

# Work in the current directory
DOCKER=${DOCKER}" -v ${PWD}/main/docs:/gitbook"
DOCKER=${DOCKER}" -v ${PWD}/../docs/:/docs"
DOCKER=${DOCKER}" -w /gitbook"

# What do we run?
#DOCKER=${DOCKER}" --name local-gitbook-docker-${USER_NAME}"
DOCKER=${DOCKER}" ${USER_IMAGE_NAME}"

# At this point we have everything ready to run.

# First we build the image
docker build -t "${USER_IMAGE_NAME}" "${IMGDIR}"

rm -rf "${IMGDIR}"

BUILDSTATUS=$?
if [ ${BUILDSTATUS} -ne 0 ]; then
    echo "================================="
    echo "Building the docker image failed."
    echo "================================="
    exit ${BUILDSTATUS}
fi

# Now actually start it
if [ $# -eq 0 ]
then
    ${DOCKER} gitbook build /gitbook /docs
else
    ${DOCKER} gitbook $@
fi


exit 0
