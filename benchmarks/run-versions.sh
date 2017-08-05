#!/bin/bash
#
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2017 Niels Basjes
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an AS IS BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

git tag | \
sed 's/v//' | \
while read version ;
do
  if [ ! -f version-${version}.txt ];
  then
    mvn clean package -Dyauaa.version=${version} && \
    echo "Testing version ${version}" && \
    java -jar target/benchmarks.jar > version-${version}.txt
  fi
done

