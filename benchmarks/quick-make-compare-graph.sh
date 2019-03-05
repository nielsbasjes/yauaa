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
echo "google.charts.load('current', {'packages':['corechart']});"
echo "google.charts.setOnLoadCallback(drawChart);"

echo "function drawChart() {"
echo "    var data = google.visualization.arrayToDataTable(["

echo -n "['Version'"
ls results/quick-speed-test-*.txt | head -1 | xargs grep '| Test |' | cut -d'|' -f3 | sed 's/^ //g;s/ +$//' | \
while read BenchMarkName ;
do
    echo -n ",'${BenchMarkName}'"
done
echo "],"


ls results/quick-speed-test-*txt | while read FileName ;
do
    VERSION=$(echo "${FileName}" | sed 's@^.*quick-speed-test-\(.*\).txt$@\1@g' )
    echo -n "['v${VERSION}'"
    ls results/quick-speed-test-*.txt | head -1 | xargs grep '| Test |' | cut -d'|' -f3 | sed 's/^ //g;s/ +$//' | \
    while read BenchMarkName ;
    do
        fgrep "| Test | ${BenchMarkName}  " "${FileName}" | \
        cut -d'|' -f5 | \
        sed 's/ *//g' | \
        xargs -n1 -iXXX echo -n ",XXX"
    done
    echo "],"
done
echo "    ]);"

echo "    var options = {"
echo "        title: 'Yauaa Performance (uncached speed in milliseconds)',"
echo "        chartArea:{ left: 50, right:50 , top: 50, bottom:300},"
echo "        legend: { position: 'top', maxLines:4 }"
echo "    };"

echo "    var chart = new google.visualization.LineChart(document.getElementById('curve_chart'));"

echo "    chart.draw(data, options);"
echo "}"

) > ../src/main/docs/PerformanceGraph.js

echo "Wrote ../src/main/docs/PerformanceGraph.js"
