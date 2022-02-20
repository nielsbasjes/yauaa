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

SOURCEDIR="${SCRIPTDIR}/commit-msg"
HOOKDIR="${SCRIPTDIR}/../.git/hooks"

if [ ! -f "${HOOKDIR}/commit-msg.sample" ];
then
  echo "Cannot find ${HOOKDIR}/commit-msg.sample"
  exit 0
fi

function listFiles() {
  cd "${SOURCEDIR}" || die "Unable to enter ${SOURCEDIR}"
  find . -type f | grep -F commit-msg | grep -F -v sample | sort
}

function calculateHashes() {
  DIR=$1
  cd "${DIR}" || die "Unable to enter ${DIR}"
  listFiles | xargs md5sum
}

function calculateHash() {
  DIR=$1
  cd "${DIR}" || die "Unable to enter ${DIR}"
  calculateHashes "${DIR}" | md5sum | cut -d' ' -f1
}

SCRIPTHASH=$(calculateHash "${SCRIPTDIR}/commit-msg" )
INSTALLEDHASH=$(calculateHash "${HOOKDIR}")

if [ "$SCRIPTHASH" == "$INSTALLEDHASH" ]
then
  echo "Git hooks are in place."
else

  #https://wiki.archlinux.org/index.php/Color_Bash_Prompt
  # Reset
  export Color_Off='\e[0m'      # Text Reset

  # High Intensity
  export IRed='\e[0;91m'        # Red
  export IYellow='\e[0;93m'     # Yellow
  export IBlue='\e[0;94m'       # Blue
  export IWhite='\e[0;97m'      # White

  # Bold High Intensity
  export BIRed='\e[1;91m'       # Red
  export BIBlue='\e[1;94m'      # Blue

  echo -e "${Color_Off}"
  echo -e "${IWhite}[${BIRed}WARN${IWhite}] ${IYellow}/========================================================================\\"
  echo -e "${IWhite}[${BIRed}WARN${IWhite}] ${IYellow}|                 ${BIRed}git commit hooks need to be updated !${IYellow}                  |"
  echo -e "${IWhite}[${BIRed}WARN${IWhite}] ${IYellow}| ${BIRed}Use ${SCRIPTDIR}/commit-msg/install.sh to install them.${IYellow} |"
  echo -e "${IWhite}[${BIRed}WARN${IWhite}] ${IYellow}\\========================================================================/"
  echo -e "${Color_Off}"

  calculateHashes "${SOURCEDIR}" > /tmp/checkCommitHooks_source_$$
  calculateHashes "${HOOKDIR}" > /tmp/checkCommitHooks_hook_$$

  echo "Difference between source(<) and hookdir(>)"
  diff /tmp/checkCommitHooks_source_$$ /tmp/checkCommitHooks_hook_$$
  rm /tmp/checkCommitHooks_source_$$ /tmp/checkCommitHooks_hook_$$
fi

