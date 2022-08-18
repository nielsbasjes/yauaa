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
# distributed under the License is distributed on an AS IS BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

. "/scripts/build_env_checks.sh"
. "/scripts/bashcolors.sh"

export JDK_VERSION="???"

function __INTERNAL__SwitchJDK {
    JDK=$1
    echo -e "${IRed}${On_Black}Setting JDK to version ${JDK}${Color_Off}"
    sudo update-java-alternatives --set $(update-java-alternatives -l | fgrep "${JDK}" | sed 's@ \+@ @g' | cut -d' ' -f1);
    export JAVA_HOME=$(update-java-alternatives -l | fgrep "${JDK}" | sed 's@ \+@ @g' | cut -d' ' -f3);
#    export JDK_VERSION="JDK ${JDK}"
}
echo "Use switch-jdk8, switch-jdk11 or switch-jdk17 to select the desired JDK to build with."
alias switch-jdk8="__INTERNAL__SwitchJDK 1.8.0; export JDK_VERSION=JDK-8"
alias switch-jdk11="__INTERNAL__SwitchJDK 1.11.0; export JDK_VERSION=JDK-11"
alias switch-jdk17="__INTERNAL__SwitchJDK 1.17.0 ; export JDK_VERSION=JDK-17"
#alias switch-jdk13="__INTERNAL__SwitchJDK latest; export JDK_VERSION=JDK-13"

switch-jdk17

. "/usr/lib/git-core/git-sh-prompt"
export PS1='\['${IBlue}${On_Black}'\] \u@\['${IWhite}${On_Red}'\][Yauaa Builder \['${BWhite}${On_Blue}'\]<'\${JDK_VERSION}'>\['${IWhite}${On_Red}'\]]\['${IBlue}${On_Black}'\]:\['${Cyan}${On_Black}'\]\w$(declare -F __git_ps1 &>/dev/null && __git_ps1 " \['${BIPurple}'\]{\['${BIGreen}'\]%s\['${BIPurple}'\]}")\['${BIBlue}'\] ]\['${Color_Off}'\]\n$ '

alias documentation-serve="cd ~/yauaa/documentation && hugo server -b http://localhost --bind=0.0.0.0"
