# Elasticsearch yauaa Ingest Processor

Explain the use case of this processor in a TLDR fashion.

https://stackoverflow.com/questions/51045201/using-the-elasticsearch-test-framework-in-intellij-how-to-resolve-the-jar-hell/51045272

4

I had the same problem when I stumbled across: https://github.com/elastic/elasticsearch/blob/master/CONTRIBUTING.md#configuring-ides-and-running-tests

In particular, I had to set idea.no.launcher=true and restart my IntelliJ.

In order to run tests directly from IDEA 2017.2 and above, it is required to disable the IDEA run launcher in order to avoid idea_rt.jar causing "jar hell". This can be achieved by adding the -Didea.no.launcher=true JVM option. Alternatively, idea.no.launcher=true can be set in the idea.properties


## Help --> Edit Custom properties

Also, you may need to:

For IDEA 2017.3 and above, in addition to the JVM option, you will need to go to Run->Edit Configurations->...->Defaults->JUnit and verify that the Shorten command line setting is set to user-local default: none. You may also need to remove ant-javafx.jar from your classpath if that is reported as a source of jar hell.

Finally, make sure you have asserts on:

The Elasticsearch codebase makes heavy use of Java asserts and the test runner requires that assertions be enabled within the JVM. This can be accomplished by passing the flag -ea to the JVM on startup.




## Usage


```
PUT _ingest/pipeline/yauaa-pipeline
{
  "description": "A pipeline to do whatever",
  "processors": [
    {
      "yauaa" : {
        "field" : "my_field"
      }
    }
  ]
}

PUT /my-index/my-type/1?pipeline=yauaa-pipeline
{
  "my_field" : "Some content"
}

GET /my-index/my-type/1
{
  "my_field" : "Some content"
  "potentially_enriched_field": "potentially_enriched_value"
}
```

## Configuration

| Parameter | Use |
| --- | --- |
| some.setting   | Configure x |
| other.setting  | Configure y |

## Setup

In order to install this plugin, you need to create a zip distribution first by running

```bash
gradle clean check
```

This will produce a zip file in `build/distributions`.

After building the zip file, you can install it like this

```bash
bin/elasticsearch-plugin install file:///path/to/ingest-yauaa/build/distribution/ingest-yauaa-0.0.1-SNAPSHOT.zip
```

## Bugs & TODO

* There are always bugs
* and todos...


# LICENSE

    Yet Another UserAgent Analyzer
    Copyright (C) 2013-2020 Niels Basjes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

