#!/bin/sh

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

# This script is used to tweak the environment at the moment we know the real username of the person using this
# By making this script a part of the image we can extend and update it to fit future needs.

# Native Linux (direct or via sudo)
USER_NAME=$1
USER_ID=$2
GROUP_ID=$3

groupadd --non-unique -g "${GROUP_ID}" "${USER_NAME}"
useradd -l -g "${GROUP_ID}" -u "${USER_ID}" -k /root -m "${USER_NAME}"
echo "export HOME=/home/${USER_NAME}" >> ~/.bashrc
echo "export USER=${USER_NAME}" >> ~/.bashrc

VBOXSF_GROUP_LINE=$4
if [ -n "${VBOXSF_GROUP_LINE}" ];
then
    echo "${VBOXSF_GROUP_LINE}" >> /etc/group
    usermod -aG vboxsf "${USER_NAME}"
fi
