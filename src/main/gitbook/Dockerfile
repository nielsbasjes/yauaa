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

FROM node:10

RUN npm install --global gitbook-cli \
        && gitbook fetch 3.2.3 \
        && npm cache verify \
        && rm -rf /tmp/*

ADD configure-for-user.sh /configure-for-user.sh
WORKDIR /gitbook

VOLUME /gitbook

EXPOSE 4000

RUN gitbook init

#CMD gitbook install && gitbook serve
