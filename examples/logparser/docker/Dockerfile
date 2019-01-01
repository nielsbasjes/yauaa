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

FROM centos:7

WORKDIR /root

ENV INSIDE_DOCKER Yes

# Install required tools
RUN yum install -y curl tar java-1.8.0-openjdk-devel expect rpm-build rpm-sign vim-enhanced

# Install Maven
RUN mkdir -p /usr/local/apache-maven
RUN curl -O https://archive.apache.org/dist/maven/maven-3/3.2.5/binaries/apache-maven-3.2.5-bin.tar.gz
RUN tar xzf apache-maven-3.2.5-bin.tar.gz --strip-components 1 -C /usr/local/apache-maven
ENV M2_HOME /usr/local/apache-maven
ENV PATH ${M2_HOME}/bin:${PATH}

# Install Pig
RUN cd /etc/yum.repos.d && curl -O https://www.apache.org/dist/bigtop/bigtop-1.1.0/repos/centos7/bigtop.repo
RUN yum install -y pig
