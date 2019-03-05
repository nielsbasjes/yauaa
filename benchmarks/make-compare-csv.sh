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

ls results/version*.txt | \
    sed 's@^.*version-\(.*\).txt$@\1@g' | sort -V | \
    while read version
do
    echo -n ";v${version}"
done
echo ""

for BenchMarkName in $(ls results/version*.txt | head -1 | xargs grep '^AnalyzerBenchmarks.' | cut -d' ' -f1);
do
    echo -n "${BenchMarkName}"
    grep "^${BenchMarkName} " results/version*txt | \
    sed 's/  */ /g' |\
    sed 's@^.*version-\(.*\).txt:@\1 @g' |\
    sort -V | \
    cut -d' ' -f 5 |\
    xargs -n1 -iXXX echo -n ";XXX"
    echo ""
done

) > output.csv

echo "Wrote output.csv"
