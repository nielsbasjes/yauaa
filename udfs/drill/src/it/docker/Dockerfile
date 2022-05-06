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
ARG DRILL_VERSION

FROM apache/drill:${DRILL_VERSION}-openjdk-17

# New scope, must redefine the need for these ARGs
ARG DRILL_VERSION
ARG YAUAA_VERSION

# Remove the prepackaged Yauaa UDF to allow testing the new one.
RUN rm /opt/drill/jars/drill-udfs-${DRILL_VERSION}.jar
RUN rm /opt/drill/jars/3rdparty/yauaa-*jar /opt/drill/jars/3rdparty/httpdlog-parser-*.jar
RUN rm /opt/drill/jars/3rdparty/parser-core-5.7.jar

# Install the new UDF.
RUN mkdir -p /opt/drill/jars/3rdparty/
COPY ./target/it/deps/*.jar /opt/drill/jars/3rdparty/
COPY ./target/yauaa-drill-${YAUAA_VERSION}.jar /opt/drill/jars/3rdparty/

# Install the Client Hints access log
COPY ./src/it/docker/access.log /tmp/access.hints
