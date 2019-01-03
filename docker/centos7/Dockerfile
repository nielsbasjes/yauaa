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
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
FROM centos:7

WORKDIR /root

ENV INSIDE_DOCKER Yes

# --------------------------------
# Install the basics
RUN yum install -y epel-release
RUN yum install -y curl wget git tar expect rpm-build rpm-sign vim-enhanced bash-completion sudo

# --------------------------------
# Java Development
RUN yum install -y java-1.8.0-openjdk-devel
ENV JAVA_HOME /usr/lib/jvm/java

# --------------------------------
# Install Maven
ENV MAVEN_VERSION=3.6.0
RUN mkdir -p /usr/local/apache-maven
RUN wget "https://www.apache.org/dyn/closer.lua?action=download&filename=/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" -O "apache-maven-${MAVEN_VERSION}-bin.tar.gz"
RUN tar xzf apache-maven-${MAVEN_VERSION}-bin.tar.gz --strip-components 1 -C /usr/local/apache-maven
ENV M2_HOME /usr/local/apache-maven
ENV PATH ${M2_HOME}/bin:${PATH}

# Avoid out of memory errors in builds
ENV MAVEN_OPTS -Xms256m -Xmx512m

# --------------------------------
# Install Pig
ENV PIG_VERSION=0.17.0
RUN cd /usr/local/ && wget "https://www.apache.org/dyn/closer.lua?action=download&filename=/pig/pig-${PIG_VERSION}/pig-${PIG_VERSION}.tar.gz" -O "pig-${PIG_VERSION}.tar.gz"
RUN cd /usr/local/ && tar xzf pig-${PIG_VERSION}.tar.gz
ENV PIG_HOME /usr/local/pig-*/
ENV PATH ${PIG_HOME}/bin:${PATH}
RUN chmod a+rwX -R ${PIG_HOME}/bin

# --------------------------------
# Install shellcheck
RUN yum install -y ShellCheck

# --------------------------------
# Install node.js, npm and gitbook
RUN yum install -y nodejs npm
RUN npm install --global gitbook-cli \
        && gitbook fetch 3.2.3

# --------------------------------
# Add a welcome message and environment checks.
RUN mkdir /scripts
ADD *.sh /scripts/
RUN chmod 755 /scripts/*.sh

# --------------------------------
# For serving the documentation site
EXPOSE 40000
EXPOSE 35729
