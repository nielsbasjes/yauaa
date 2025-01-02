#!/bin/bash
#
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
cd "${SCRIPTDIR}" || exit 1
CONTAINER_NAME=hive-server

# Ensure clean starting point
docker compose down

# Start the Hive installation
docker compose up -d

# ---------------------------------------------------------------------------
# Wait for the server to start
SERVER_LOG="${SCRIPTDIR}/hive-it-server.log"
STATUS="Stopped"
# Startup may take at most 60 seconds
SERVER_START_TTL=60

while [ "${STATUS}" == "Stopped" ];
do
  echo "Waiting for Hive server to complete startup... (${SERVER_START_TTL}) "
  sleep  1s
  docker exec "${CONTAINER_NAME}" hive -u 'jdbc:hive2://localhost:10000/' hive hive > "${SERVER_LOG}" 2>&1
  grep -F "Connected to: Apache Hive" "${SERVER_LOG}"
  RESULT=$?
  if [ "${RESULT}" == "0" ];
  then
    echo "Hive server is running!"
    STATUS="Running"
  fi
  SERVER_START_TTL=$((SERVER_START_TTL-1))
  if [ ${SERVER_START_TTL} -le 0 ];
  then
    echo "The Server did not start within 60 seconds (normal is < 10 seconds)"
    echo "Server output:"
    cat "${SERVER_LOG}"
    exit 255
  fi
done



# Figure out what the name of the actual Jar file is.
JARNAME=$( cd ../../../target/ || exit 1;  ls yauaa-hive-*-udf.jar )

# Store the jar file in HDFS
#echo "==========================================="
#echo "Installing Yauaa UDF on HDFS"
#docker exec -t -i hive-server bash  hdfs dfs -mkdir '/udf/'
#docker exec -t -i hive-server bash  hdfs dfs -put "/udf-target/${JARNAME}" '/udf/'
#docker exec -t -i hive-server bash  hdfs dfs -ls '/udf/'

# Create the test Hive script
echo "==========================================="
sed "s/@JARNAME@/${JARNAME}/g" create_useragents_table.hql.in > create_useragents_table.hql

echo "Console to try manually. First do 'use testdb;' !!"
echo "When you close this the test setup will be destroyed!!"
docker exec -t -i "${CONTAINER_NAME}" hive

# Shut it all down again.
echo "==========================================="
echo "Shutting down the test setup"
docker compose down
