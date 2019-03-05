#!/bin/bash
#
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2019 Niels Basjes
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an AS IS BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

git tag | \
sed 's/v//' | \
egrep -v '^(0.1|0.10|0.3)$' | while read version ;
do
  if [ -f "results/version-${version}.txt" ];
  then
    echo "- Already have version ${version}"
  else
    echo '/ ============================================== ' && \
    echo "| Building version ${version}" && \
    echo '\ ============================================== ' && \
    mvn clean package -Dyauaa.version="${version}" && \
    echo '/ ============================================== ' && \
    echo "| Testing version ${version}" && \
    echo '\ ============================================== ' && \
    java -jar target/benchmarks.jar > "results/version-${version}.txt"
  fi
done

