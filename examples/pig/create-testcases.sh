#!/bin/bash
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

find ../../analyzer/src/main/resources/UserAgents/ -type f -name '*.yaml' -print0 | \
    xargs -0 cat | \
    fgrep user_agent | \
    cut -d':' -f2- | \
    sed 's@^ *@@g' | \
    sed 's@^"@@' | \
    sed "s@^'@@" | \
    sed "s@'\$@@" \
    > testcases.txt
