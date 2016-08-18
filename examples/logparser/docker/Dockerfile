FROM centos:7

WORKDIR /root

ENV INSIDE_DOCKER Yes

# Install required tools
RUN yum install -y curl tar java-1.8.0-openjdk-devel expect rpm-build rpm-sign vim-enhanced

# Install Maven
RUN mkdir -p /usr/local/apache-maven
RUN curl -O http://archive.apache.org/dist/maven/maven-3/3.2.5/binaries/apache-maven-3.2.5-bin.tar.gz
RUN tar xzf apache-maven-3.2.5-bin.tar.gz --strip-components 1 -C /usr/local/apache-maven
ENV M2_HOME /usr/local/apache-maven
ENV PATH ${M2_HOME}/bin:${PATH}

# Install Pig
RUN cd /etc/yum.repos.d && curl -O http://www.apache.org/dist/bigtop/bigtop-1.1.0/repos/centos7/bigtop.repo
RUN yum install -y pig
