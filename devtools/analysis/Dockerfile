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

ARG ELK_VERSION

# https://github.com/elastic/elasticsearch-docker
FROM docker.elastic.co/elasticsearch/elasticsearch:${ELK_VERSION}

#COPY *.zip ./plugin/
COPY elasticsearch.yml /usr/share/elasticsearch/config/elasticsearch.yml

#RUN bin/elasticsearch-plugin -v -v -v install file:///$(ls /usr/share/elasticsearch/plugin/*.zip | head -1)

ENV ES_JAVA_OPTS "-Xmx8192m -Xms1024m"

#RUN ls -laF /usr/share/elasticsearch/plugin

EXPOSE 9200
EXPOSE 9300
