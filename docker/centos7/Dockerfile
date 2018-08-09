#
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2018 Niels Basjes
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
RUN mkdir -p /usr/local/apache-maven
RUN wget "https://www.apache.org/dyn/closer.lua?action=download&filename=/maven/maven-3/3.5.4/binaries/apache-maven-3.5.4-bin.tar.gz" -O "apache-maven-3.5.4-bin.tar.gz"
RUN tar xzf apache-maven-3.5.4-bin.tar.gz --strip-components 1 -C /usr/local/apache-maven
ENV M2_HOME /usr/local/apache-maven
ENV PATH ${M2_HOME}/bin:${PATH}

# Avoid out of memory errors in builds
ENV MAVEN_OPTS -Xms256m -Xmx512m

# --------------------------------
# Install Pig
RUN rpm --import https://www.apache.org/dist/bigtop/bigtop-1.2.1/repos/GPG-KEY-bigtop
RUN yum-config-manager --add-repo https://www.apache.org/dist/bigtop/bigtop-1.2.1/repos/centos7/bigtop.repo
RUN yum install -y pig

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
