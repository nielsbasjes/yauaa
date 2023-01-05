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
# distributed under the License is distributed on an AS IS BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

COMMIT_MSG_FILE=$1

COMMIT_LINES=$(cat "${COMMIT_MSG_FILE}" | grep . | grep -v '^#' | wc -l)
if [ "${COMMIT_LINES}" -eq 1 ];
then
  pass "Single line commit message: Ok"
else
  fail "Single line commit message: Found ${COMMIT_LINES} lines"
  exit 1
fi
