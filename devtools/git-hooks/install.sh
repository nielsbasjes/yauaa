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

SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

HOOKDIR="${SCRIPTDIR}/../../.git/hooks"

if [ ! -f "${HOOKDIR}/commit-msg.sample" ];
then
  echo "Cannot find ${HOOKDIR}/commit-msg.sample"
  exit 1
fi

( cd "${SCRIPTDIR}/hooks" && cp -a * "${HOOKDIR}/" )
chmod 755 "${HOOKDIR}/commit-msg"
chmod 755 "${HOOKDIR}/pre-commit"

echo "Installation completed"

${SCRIPTDIR}/check-git-commit-hooks.sh
