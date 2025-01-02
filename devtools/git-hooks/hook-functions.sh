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

# Client side pre-commit hook to assist the committers to ensure the commit messages
# follow the chosen convention.

#https://wiki.archlinux.org/index.php/Color_Bash_Prompt
# Reset
export Color_Off='\033[0m'      # Text Reset

# High Intensity
export IRed='\033[0;91m'        # Red
export IYellow='\033[0;93m'     # Yellow
export IBlue='\033[0;94m'       # Blue
export IWhite='\033[0;97m'      # White

# Bold High Intensity
export BIRed='\033[1;91m'       # Red
export BIYellow='\033[1;93m'     # Yellow
export BIGreen='\033[1;92m'     # Green
export BIBlue='\033[1;94m'      # Blue

function pass() {
  echo -e "${Color_Off}${IWhite}[${BIGreen}PASS${IWhite}] ${Color_Off}${1}"
}

function warn() {
  echo -e "${Color_Off}${IWhite}[${BIYellow}WARN${IWhite}] ${IYellow}${1}${Color_Off}"
}

function fail() {
  echo -e "${Color_Off}${IWhite}[${BIRed}FAIL${IWhite}] ${IRed}${1}${Color_Off}"
}

function die() {
  echo -e "${Color_Off}"
  echo -e "${IWhite}[${BIRed}FAIL${IWhite}] ${IYellow}/========================================================================"
  echo -e "${IWhite}[${BIRed}FAIL${IWhite}] ${IYellow}| ${BIRed} ---------->>> THE CHANGES WERE NOT COMMITTED ! <<<---------- ${IYellow}"
  echo -e "${IWhite}[${BIRed}FAIL${IWhite}] ${IYellow}| ${BIRed} $@ ${IYellow}"
  echo -e "${IWhite}[${BIRed}FAIL${IWhite}] ${IYellow}\\========================================================================"
  echo -e "${Color_Off}"
  exit 1
}
