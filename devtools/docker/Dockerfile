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
FROM ubuntu:20.04

WORKDIR /root

ENV INSIDE_DOCKER Yes

ARG DEBIAN_FRONTEND=noninteractive

WORKDIR /root

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

#####
# Disable suggests/recommends
#####
RUN echo APT::Install-Recommends "0"\; > /etc/apt/apt.conf.d/10disableextras
RUN echo APT::Install-Suggests "0"\; >>  /etc/apt/apt.conf.d/10disableextras

ENV DEBIAN_FRONTEND noninteractive
ENV DEBCONF_TERSE true

###
# Update and install common packages
###
RUN apt -q update \
   && apt install -y software-properties-common apt-utils apt-transport-https ca-certificates \
   && add-apt-repository -y ppa:deadsnakes/ppa

RUN apt-get -q install -y --no-install-recommends \
    bash-completion \
    build-essential \
    bzip2 \
    wget \
    curl \
    docker.io \
    git \
    gnupg-agent \
    rsync \
    sudo \
    vim \
    locales \
    wget \
    time \
    ruby \
    openjdk-8-jdk \
    openjdk-11-jdk \
    openjdk-17-jdk

###
# Set the locale ( see https://stackoverflow.com/a/28406007/114196 )
###
RUN sed -i '/en_US.UTF-8/s/^# //g' /etc/locale.gen && \
    locale-gen
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

# --------------------------------
# Install Maven
ENV MAVEN_VERSION=3.8.6
RUN mkdir -p /usr/local/apache-maven
RUN cd /usr/local/ && wget "https://www.apache.org/dyn/closer.lua?action=download&filename=/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" -O "apache-maven-${MAVEN_VERSION}-bin.tar.gz"
RUN cd /usr/local/ && tar xzf apache-maven-${MAVEN_VERSION}-bin.tar.gz --strip-components 1 -C /usr/local/apache-maven
ENV M2_HOME /usr/local/apache-maven
ENV PATH ${M2_HOME}/bin:${PATH}

# Avoid out of memory errors in builds
ENV MAVEN_OPTS -Xms256m -Xmx512m

# Install command line completion for maven
RUN wget https://raw.githubusercontent.com/juven/maven-bash-completion/master/bash_completion.bash -O /etc/bash_completion.d/maven

# --------------------------------
# Install shellcheck
ENV SHELLCHECK_VERSION=0.8.0
RUN cd /usr/local/bin && \
    wget https://github.com/koalaman/shellcheck/releases/download/v${SHELLCHECK_VERSION}/shellcheck-v${SHELLCHECK_VERSION}.linux.x86_64.tar.xz && \
    tar xJf shellcheck-v${SHELLCHECK_VERSION}.linux.x86_64.tar.xz && \
    mv shellcheck-v${SHELLCHECK_VERSION}/shellcheck . && \
    rm -rf shellcheck-v${SHELLCHECK_VERSION} shellcheck-v${SHELLCHECK_VERSION}.linux.x86_64.tar.xz

# --------------------------------
# Install Hugo
ENV HUGO_VERSION=0.102.3
RUN cd /usr/local/bin && \
    wget https://github.com/gohugoio/hugo/releases/download/v${HUGO_VERSION}/hugo_${HUGO_VERSION}_Linux-64bit.tar.gz && \
    tar xzf hugo_${HUGO_VERSION}_Linux-64bit.tar.gz && \
    rm -f hugo_${HUGO_VERSION}_Linux-64bit.tar.gz

# --------------------------------
# Add a welcome message and environment checks.
RUN mkdir /scripts
ADD *.sh /scripts/
RUN chmod 755 /scripts/*.sh

# --------------------------------
# For serving the documentation site
EXPOSE 1313
