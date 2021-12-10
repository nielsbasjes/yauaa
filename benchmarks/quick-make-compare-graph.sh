#!/usr/bin/env bash
#
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2021 Niels Basjes
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
echo '<p><div id="curve_chart" style="height: 1000px"></div></p>'
echo '<script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>'
echo '<script>'
echo "google.charts.load('current', {'packages':['corechart']});"
echo "google.charts.setOnLoadCallback(drawChart);"

echo "function drawChart() {"
echo "    var data = google.visualization.arrayToDataTable(["

echo -n "    ['Version'"
ls results/quick-speed-test-*.txt | sort --version-sort | head -1 | xargs grep '| Test |' | cut -d'|' -f3 | sed 's/^ //g;s/ +$//' | \
while read -r BenchMarkName ;
do
    echo -n ",'${BenchMarkName}'"
done
echo "],"


ls results/quick-speed-test-*txt | sort --version-sort | while read -r FileName ;
do
    VERSION=$(echo "${FileName}" | sed 's@^.*quick-speed-test-\(.*\).txt$@\1@g' )
    echo -n "    ['v${VERSION}'"
    ls results/quick-speed-test-*.txt | sort --version-sort | head -1 | xargs grep '| Test |' | cut -d'|' -f3 | sed 's/^ //g;s/ +$//' | \
    while read -r BenchMarkName ;
    do
        grep -F "| Test | ${BenchMarkName}  " "${FileName}" | \
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
echo "google.charts.load('current', {'packages':['corechart']});"
echo "google.charts.setOnLoadCallback(drawChart);"

# https://stackoverflow.com/a/20384135/114196
echo "//create trigger to resizeEnd event"
echo "\$(window).resize(function() {"
echo "    if(this.resizeTO) clearTimeout(this.resizeTO);"
echo "    this.resizeTO = setTimeout(function() {"
echo "        \$(this).trigger('resizeEnd');"
echo "    }, 500);"
echo "});"

echo "//redraw graph when window resize is completed"
echo "\$(window).on('resizeEnd', function() {"
echo "    drawChart();"
echo "});"

echo "</script>"


) > ../documentation/layouts/shortcodes/PerformanceGraph.html

echo "Wrote ../documentation/layouts/shortcodes/PerformanceGraph.html"
