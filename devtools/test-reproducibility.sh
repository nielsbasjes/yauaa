#!/bin/bash
#
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2024 Niels Basjes
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

# Release procedure.
# This uses the maven-release-plugin which has been configured to ONLY modify the local git repo.

# ----------------------------------------------------------------------------------------------------
SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

echo "PWD: ${SCRIPTDIR}"

cd "${SCRIPTDIR}/.." || ( echo "This should not be possible" ; exit 1 )

# Working directory is now the root of the project

# ----------------------------------------------------------------------------------------------------
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
export BIYellow='\e[1;93m'     # Yellow
export BIGreen='\e[1;92m'     # Green
export BIBlue='\e[1;94m'      # Blue

function info() {
  echo -e "${Color_Off}${IWhite}[${BIBlue}INFO${IWhite}] ${Color_Off}${1}"
}

function pass() {
  echo -e "${Color_Off}${IWhite}[${BIGreen}PASS${IWhite}] ${Color_Off}${1}"
}

function warn() {
  echo -e "${Color_Off}${IWhite}[${BIYellow}WARN${IWhite}] ${IYellow}${1}${Color_Off}"
}

function fail() {
  echo -e "${Color_Off}${IWhite}[${BIRed}FAIL${IWhite}] ${IRed}${1}${Color_Off}"
}

function die() {
  echo -e "${Color_Off}"
  echo -e "${IWhite}[${BIRed}FAIL${IWhite}] ${IYellow}/========================================================================"
  echo -e "${IWhite}[${BIRed}FAIL${IWhite}] ${IYellow}| ${BIRed} ---------->>> PROCESS WAS ABORTED <<<---------- ${IYellow}"
  echo -e "${IWhite}[${BIRed}FAIL${IWhite}] ${IYellow}| ${BIRed} $* ${IYellow}"
  echo -e "${IWhite}[${BIRed}FAIL${IWhite}] ${IYellow}\\========================================================================"
  echo -e "${Color_Off}"
  exit 1
}

# ----------------------------------------------------------------------------------------------------
info "Publishing SNAPSHOT to Local reproduceTest Repo"
pushd .
TMPDIR=YauaaRebuild-$$
mkdir -p "/tmp/${TMPDIR}"
cp -a . "/tmp/${TMPDIR}"
cd "/tmp/${TMPDIR}"
./mvnw clean install -PskipQuality
cleanInstallStatus=$?
if [ ${cleanInstallStatus} -ne 0 ];
then
    fail "Building and installing Failed."
    exit ${cleanInstallStatus}
else
    pass "Building and installing Success."
fi
popd || exit 1
rm -rf "/tmp/${TMPDIR}"

# ----------------------------------------------------------------------------------------------------
info "Comparing the build ... "
./mvnw clean verify -PskipQuality -PartifactCompare
cleanCompareStatus=$?
if [ ${cleanCompareStatus} -ne 0 ];
then
    fail "Comparing SNAPSHOT failed."
    exit ${cleanCompareStatus}
else
    pass "Comparing SNAPSHOT Success."
fi

# -----------------------------------------------

diffoscope target/reference/yauaa-webapp-*.war webapp/target/yauaa-webapp-*.war | grep '[0-9] META-INF/'
