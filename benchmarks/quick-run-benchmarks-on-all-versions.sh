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

# Skipping versions 0.x, 1.x and 2.x because of missing API
# Skipping version 5.1 because it is broken (bad packaging: java.lang.NoClassDefFoundError: nl/basjes/shaded/com/google/common/net/InternetDomainName)

git tag | \
sed 's/v//' | \
egrep -v '^(0\.|1\.|2\.|5\.1)' | while read version ;
do
  OUTPUT="results/quick-speed-test-${version}.txt"
  if [ -f "${OUTPUT}" ];
  then
    echo "- Already have version ${version}"
  else
    echo '/ ============================================== ' && \
    echo "| Testing version ${version}" && \
    echo '\ ============================================== ' && \
    mvn clean test -Dyauaa.version="${version}" -Dtest=RunBenchmarks > "${OUTPUT}"
  fi
done

