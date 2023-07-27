#!/bin/bash
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

# NOTE: This is a manual only script to extract the list of all devices from the lenovo website.
# This is extremely brittle and will most likely need revising everytime I use it.

[ -f en.js ] || wget https://pcsupport.lenovo.com/api/products/us/en.js

cat en.js | sed 's@"@\n@g' | fgrep -v 'function' | fgrep '(' | fgrep -vi 'idea' | fgrep -vi 'ThinkCentre' | fgrep -vi 'desktop'| fgrep -vi 'laptop' | fgrep -vi 'Chromebook' > t1.txt

cat t1.txt | sed 's@(@\n(@g;s@)@\n@g;' | fgrep '(' | sed 's@(@@g' | sed 's@Lenovo @@g' | sed 's@,@\n@g;s@^ @@g' | sort -u > t2.txt
