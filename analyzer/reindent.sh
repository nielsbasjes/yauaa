#!/usr/bin/env bash
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
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

sed -i "
s@^  *\([\"a-zA-Z_]\)@      \1@
s@^  *config:@config:@
s@^  *\(- '\)@    \1@
s@^  *\(- [a-z]\)@\1@
s@^  *variable:@    variable:@
s@^  *require:@    require:@
s@^  *extract:@    extract:@
s@^  *options:@    options:@
s@^  *values:@    values:@
s@^  *input:@    input:@
s@^  *expected:@    expected:@
s@^  *name:@    name:@
s@^  *map:@    map:@
s@^\(    \+[A-Z_][a-zA-Z_]\+\) \+:@\1                                                  :@
s@^\(    \+[A-Z_][a-zA-Z_ ]\{36\}\) \+:@\1:@
s@^  \+\(- '[A-Z_][a-zA-Z_]\+\) *: *\([0-9]\+\) *:@    \1                                                :            \2:@g
s@^  \+\(- '[A-Z_][a-zA-Z_ ]\{34\}\) *: *\([0-9 ]\{6\}\) *:@    \1 : \2 :@g
 " "${DIR}/src/main/resources/UserAgents/"*.yaml

