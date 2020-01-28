#!/bin/bash
#
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2020 Niels Basjes
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

VERSION=$1

[ -z "$VERSION" ] && echo "Usage: $0 <logstash version>" && exit 1

[ -f ${HOME}/.m2/repository/org/logstash/logstash-core/${VERSION}/logstash-core-${VERSION}.jar ] && echo "Logstash ${VERSION} was already downloaded" && exit 0;

cd /tmp || exit 1

echo "Logstash ${VERSION}: Downloading"

curl https://artifacts.elastic.co/downloads/logstash/logstash-oss-${VERSION}.tar.gz | tar xzf - --to-stdout logstash-${VERSION}/logstash-core/lib/jars/logstash-core.jar > logstash-core.jar

echo "Logstash ${VERSION}: Installing"

mvn install:install-file        \
     -DgroupId=org.logstash     \
     -DartifactId=logstash-core \
     -Dpackaging=jar            \
     -Dversion=${VERSION}       \
     -Dfile=logstash-core.jar

echo "Logstash ${VERSION}: Cleanup"

rm -rf logstash-core.jar

echo "Logstash ${VERSION}: Done"
