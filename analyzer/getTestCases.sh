#!/bin/bash
#
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2025 Niels Basjes
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

[ -d "${DIR}/target/" ] || mkdir "${DIR}/target"

cat "${DIR}/src/main/resources/UserAgents/"*.yaml | \
  grep "  user_agent_string *:" | \
  grep -v '^#' | \
  sed "
    s@^ \+user_agent_string *: *['\"]\(.*\)['\"] *\$@\1@g;
    s@\\\\@\\\\\\\\@g;
    s@\\\"@\\\\\\\"@g;
  " > "${DIR}/target/temp-agents-list-1.txt"

cat "${DIR}/src/main/resources/UserAgents/"*.yaml | \
  grep "  User-Agent *:" | \
  grep -v '^#' | \
  sed "
    s@^ \+User-Agent *: *['\"]\(.*\)['\"] *\$@\1@g;
    s@\\\\@\\\\\\\\@g;
    s@\\\"@\\\\\\\"@g;
  " > "${DIR}/target/temp-agents-list-2.txt"

# This is needed to ensure reproducible results from sort
LC_ALL=C

cat "${DIR}/target/temp-agents-list-1.txt" \
    "${DIR}/target/temp-agents-list-2.txt" | \
  sed "
    s@^@            \"@;
    s@\$@\",\n@;
  " | grep . | sort -u > "${DIR}/target/temp-agents-list.txt"
