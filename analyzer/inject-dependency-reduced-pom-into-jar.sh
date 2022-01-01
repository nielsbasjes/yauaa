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

groupId=$1
artifactId=$2
version=$3

DIR=META-INF/maven/${groupId}/${artifactId}

mkdir -p "target/${DIR}"

echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<!--
  ~ Yet Another UserAgent Analyzer
  ~ Copyright (C) 2013-2022 Niels Basjes
  ~
  ~ Licensed under the Apache License, Version 2.0 (the \"License\");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~    https://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an \"AS IS\" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

<!-- +======================================================================+ -->
<!-- | This file was generated manually from the dependency-reduced-pom.xml | -->
<!-- | as a workaround for https://issues.apache.org/jira/browse/MSHADE-36  | -->
<!-- +======================================================================+ -->

" > "target/${DIR}/pom.xml"

grep -F -v '<?xml version=' dependency-reduced-pom.xml >> "target/${DIR}/pom.xml"
jar -uf "target/${artifactId}-${version}.jar" -C "target" "${DIR}/pom.xml"

STATUS=$?

if [ "${STATUS}" -eq 0 ];
then
  echo "Replaced the pom.xml with the dependency-reduced-pom.xml"
else
  echo "Something went wrong when trying to replace the pom.xml with the dependency-reduced-pom.xml"
fi
exit "${STATUS}"
