#!/bin/bash
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

# Fail if someone tries to commit a generated file
for filename in $(git diff --cached --name-only | grep -F -v git-hooks);
do
  grep -F 'THIS FILE WAS GENERATED; DO NOT EDIT MANUALLY' $filename > /dev/null 2>&1
  STATUS=$?
  if [ "${STATUS}" -eq 0 ];
  then
    warn "YOU ARE COMMITTING GENERATED FILES: $filename"
    warn '!!!! MAKE SURE YOU HAVE NOT DONE ANY MANUAL EDITING !!!!'
    warn "Press any key abort or 'c' to continue"
    read -n 1 k <&1
    if [[ $k = c ]] ;
    then
      pass "Continuing with committing"
    else
      die "Please regenerate the updated files and try again"
    fi
  fi
done

pass "No generated files"
