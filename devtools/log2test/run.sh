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


mvn clean package
JAR=$(ls target/yauaa-*.jar | fgrep -v source)

FORMAT="%a %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" \"%{Sec-CH-UA}i\" \"%{Sec-CH-UA-Arch}i\" \"%{Sec-CH-UA-Bitness}i\" \"%{Sec-CH-UA-Full-Version}i\" \"%{Sec-CH-UA-Full-Version-List}i\" \"%{Sec-CH-UA-Mobile}i\" \"%{Sec-CH-UA-Model}i\" \"%{Sec-CH-UA-Platform}i\" \"%{Sec-CH-UA-Platform-Version}i\" \"%{Sec-CH-UA-WoW64}i\" %V"

java -jar "${JAR}" "${FORMAT}" __logs/access_clienthints_log-20220925

