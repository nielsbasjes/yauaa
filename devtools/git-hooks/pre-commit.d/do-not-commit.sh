#!/bin/bash
#
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2023 Niels Basjes
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

# Fail if there is a DONOTCOMMIT in one of the files to be committed
for filename in $(git diff --cached --name-only | grep -F -v git-hooks);
do
  git diff --cached $filename | grep -F "DONOTCOMMIT"  > /dev/null 2>&1
  STATUS=$?
  if [ "${STATUS}" -eq 0 ];
  then
    die "Blocking commit because string DONOTCOMMIT detected in patch: $filename"
  fi
done
pass "No files with DONOTCOMMIT: Ok"
