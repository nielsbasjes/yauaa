# What is this?
These are just a few scripts I occasionally use to run Yauaa as part of a pipeline in Elasticsearch... locally on my machine in docker.

I usually copy these to a separate "working" location out or sight of IntelliJ as that would make it see a LOT of files ... which makes everything very slow.

I use this to find unexpected agents and patterns to base new rules upon.

In the loader directory is a Kotlin script that reads the tab-separated input files, analyzes user agents in parallel, and bulk-loads the results into Elasticsearch. Set `PARALLELISM` near the top of `load-into-es.main.kts` to control the number of analyzer threads. The script reads the supplied file paths, or `*.txt` in the current directory when no paths are given.

Install the current analyzer snapshot with `./mvnw -pl analyzer -am install`, then run `kotlin -J-Xmx8g devtools/analysis/loader/load-into-es.main.kts <file...>` from the repository root. Adjust the JVM heap for the chosen parallelism because each worker owns a separate analyzer. `ES_URL` defaults to `http://127.0.0.1:9200`; set `ES_USERNAME` and `ES_PASSWORD` together when the cluster uses basic authentication.

<!--
   - Yet Another UserAgent Analyzer
   - Copyright (C) 2013-2026 Niels Basjes
   -
   - Licensed under the Apache License, Version 2.0 (the \"License\");
   - you may not use this file except in compliance with the License.
   - You may obtain a copy of the License at
   -
   - https://www.apache.org/licenses/LICENSE-2.0
   -
   - Unless required by applicable law or agreed to in writing, software
   - distributed under the License is distributed on an "AS IS" BASIS,
   - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   - See the License for the specific language governing permissions and
   - limitations under the License.
-->
