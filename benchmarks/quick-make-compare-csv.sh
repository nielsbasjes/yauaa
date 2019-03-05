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

(
echo -n "Name"

ls results/quick-speed-test-*.txt | \
    sed 's@^.*quick-speed-test-\(.*\).txt$@\1@g' | sort -V | \
    while read version
do
    echo -n ";v${version}"
done
echo ""

ls results/quick-speed-test-*.txt | head -1 | xargs grep '| Test |' | cut -d'|' -f3 | sed 's/^ //g;s/ +$//' | \
while read BenchMarkName ;
do
    echo -n "${BenchMarkName}"
    ls results/quick-speed-test-*txt | xargs fgrep "| Test | ${BenchMarkName}  "  | \
    cut -d'|' -f5 | \
    sed 's/ *//g' | \
    xargs -n1 -iXXX echo -n ";XXX"
    echo ""
done

) > quick-output.csv

echo "Wrote quick-output.csv"
