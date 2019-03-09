Performance
===========
On my i7 system I see a speed ranging from 500 to 4000 useragents per second (depending on the length and ambiguities in the useragent).
On average the speed is around 2000 per second or ~0.5ms each.
A LRU cache is in place that does over 1M per second if they are in the cache.

Please note that the current system take approx 220MiB of RAM just for the engine (without any caching!!).

In the canonical usecase of analysing clickstream data you will see a <1ms hit per visitor (or better: per new non-cached useragent)
and for all the other clicks the values are retrieved from this cache at a speed of < 1 microsecond (i.e. close to 0).

The graph below gives you some insight of how the performance of Yauaa has progressed over time.

You can clearly see the increase in the time needed when adding a lot more rules. 
Also the periodic drops in time needed are clearly visible when a performance improvement was found.

Between version 5.5 and 5.6 a lot of extra rules to detect more brands of mobile devices on Android (at one point during development the needed time to reached ~ 3ms).
Followed by a few steps in a rewrite of that part resulting in effectively the fastest versions to date.

Output from the benchmark ( [using this code](https://github.com/nielsbasjes/yauaa/blob/master/benchmarks/src/test/java/nl/basjes/parse/useragent/benchmarks/RunBenchmarks.java) ) on a Intel(R) Core(TM) i7-6820HQ CPU @ 2.70GHz from version 3.0 onwards:

<div id="curve_chart" style="height: 1000px"></div>
<script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
<script type="text/javascript" src="PerformanceGraph.js"></script>
<script type="text/javascript" >google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawChart);
</script>
