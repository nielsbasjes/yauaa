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
function build {
  hash=$1
  version=$2
  if [ -f "version-${version}.txt" ];
  then
    echo "- Already have version ${version}"
  else
    git checkout ${hash}
    ( cd .. && mvn versions:set -DgenerateBackupPoms=false -DnewVersion=${version} && mvn clean install -DskipTests=true -Drat.skip=true && git checkout . )
  fi
}

function run {
  version=$1
  if [ -f "results/version-${version}.txt" ];
  then
    echo "- Already have version ${version}"
  else
    mvn clean package -Dyauaa.version="${version}"
    java -jar target/benchmarks.jar > "results/version-${version}.txt"
    echo '/ ============================================== ' && \
    echo "| Building version ${version}" && \
    echo '\ ============================================== ' && \
    mvn clean package -Dyauaa.version="${version}" && \
    echo '/ ============================================== ' && \
    echo "| Testing version ${version}" && \
    echo '\ ============================================== ' && \
    java -jar target/benchmarks.jar > "results/version-${version}.txt"
  fi
}

# First ensure that all dependencies for the current version are in place
( cd .. ; git checkout . ; git checkout master )
( cd .. ; mvn clean install -DskipTests=true -Drat.skip=true )

build 56a053a552c00fe72280e6e0db406906e64915df 5.6-SNAPSHOT-1-AllRules
build 9103baf9c7f27eecf41b4e6c7f0e623e1f5d4a2f 5.6-SNAPSHOT-2-Prefix-List
build 2364d7fa3eda6526004e458250dea3c031d5ac69 5.6-SNAPSHOT-3-Prefix-Trie-ToLower
build cc1e7db37cbc4fd56a5d54684db955fd45677be7 5.6-SNAPSHOT-4-Prefix-DualTrie

run 5.6-SNAPSHOT-1-AllRules
run 5.6-SNAPSHOT-2-Prefix-List
run 5.6-SNAPSHOT-3-Prefix-Trie-ToLower
run 5.6-SNAPSHOT-4-Prefix-DualTrie

