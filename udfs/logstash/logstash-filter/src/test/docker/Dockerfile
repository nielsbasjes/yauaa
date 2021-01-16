#
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2021 Niels Basjes
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

FROM docker.elastic.co/logstash/logstash:${ELK_VERSION}

COPY ./target/*.gem                /plugin/
RUN rm  /usr/share/logstash/pipeline/logstash.conf
COPY src/test/docker/run-test.conf /usr/share/logstash/pipeline/
COPY src/test/docker/logstash.yml  /usr/share/logstash/config/

RUN echo -e "\n\n\nThis next step can take quite long (about 1 minute is 'normal').\n\n\n"
RUN bin/logstash-plugin install $(ls /plugin/*.gem | head -1)
