#!/bin/bash -x
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
cd "${SCRIPTDIR}"

# Start the Hive installation
docker-compose up -d

sleep 5s

# Figure out what the name of the actual Jar file is.
JARNAME=$( cd ../../target/ ;  ls yauaa-hive-*-udf.jar )

# Store the jar file in HDFS
docker exec -t -i hive-server bash  hdfs dfs -mkdir '/udf/'
docker exec -t -i hive-server bash  hdfs dfs -put "/udf-target/${JARNAME}" '/udf/'
docker exec -t -i hive-server bash  hdfs dfs -ls '/udf/'

# Run the test Hive script
sed "s/@JARNAME@/${JARNAME}/g" create_useragents_table.hql.in > create_useragents_table.hql
docker exec -t -i hive-server hive -f '/useragents/create_useragents_table.hql'

# Allow for manual commands
echo "Console to try manually. First do 'use testdb;' !!"
echo "When you close this the test setup will be destroyed!!"
docker exec -t -i hive-server hive

# Shut it all down again.
docker-compose down
