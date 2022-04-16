#!/bin/bash
#
# Copyright (C) 2018-2021 Niels Basjes
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
cd "${SCRIPTDIR}" || exit 1
LOGFILE=hive-it-test.log

# Ensure clean starting point
docker-compose down

# Start the Hive installation
docker-compose up -d

sleep 5s

# Figure out what the name of the actual Jar file is.
JARNAME=$( cd ../../../target/ || exit 1;  ls yauaa-hive-*-udf.jar )

# Store the jar file in HDFS
echo "==========================================="
echo "Installing Yauaa UDF on HDFS"
docker exec hive-server bash  hdfs dfs -mkdir '/udf/'
docker exec hive-server bash  hdfs dfs -put "/udf-target/${JARNAME}" '/udf/'
docker exec hive-server bash  hdfs dfs -ls '/udf/'

# Run the test Hive script
echo "==========================================="
echo "Running tests"
sed "s/@JARNAME@/${JARNAME}/g" create_useragents_table.hql.in > create_useragents_table.hql

docker exec hive-server hive -f '/useragents/create_useragents_table.hql' > "${LOGFILE}" 2>&1

# Shut it all down again.
echo "==========================================="
echo "Shutting down the test setup"
docker-compose down

echo "==========================================="
echo "Verify the output of the tests"

EXIT_CODE=0

#https://wiki.archlinux.org/index.php/Color_Bash_Prompt
# Reset
export Color_Off='\e[0m'      # Text Reset

# High Intensity
export IRed='\e[0;91m'        # Red
export IYellow='\e[0;93m'     # Yellow
#export IBlue='\e[0;94m'       # Blue
export IWhite='\e[0;97m'      # White

# Bold High Intensity
export BIRed='\e[1;91m'       # Red
export BIGreen='\e[1;92m'     # Green
#export BIBlue='\e[1;94m'      # Blue

function pass() {
  echo -e "${Color_Off}${IWhite}[${BIGreen}PASS${IWhite}] ${Color_Off}${1}"
}

function fail() {
  echo -e "${Color_Off}${IWhite}[${BIRed}FAIL${IWhite}] ${IYellow}${1}${Color_Off}"
}

function ensure() {
  STEP=$1
  STRING=$2
  ERRORMSG=$3
  grep -F "${STEP}" "${LOGFILE}" | grep -F "${STRING}" > /dev/null 2>&1
  STATUS=$?
  if [ "${STATUS}" -eq 0 ];
  then
    pass "$ERRORMSG"
  else
    fail "$ERRORMSG"
    EXIT_CODE=1
  fi
}

ensure 'TMP'  'Desktop	Linux ??	Chrome 92' "TMP  Function: Specific fields query"
ensure 'PERM' 'Desktop	Linux ??	Chrome 92' "PERM Function: Specific fields query"
ensure 'TMP'  '"devicename":"Google Nexus 6"' "TMP  Function: Map with all fields query"
ensure 'PERM' '"devicename":"Google Nexus 6"' "PERM Function: Map with all fields query"

exit ${EXIT_CODE}
