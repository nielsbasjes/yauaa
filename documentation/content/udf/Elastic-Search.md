+++
title = "Elastic Search"
+++
## Introduction
User Defined Function (ingest processor) for [Elastic Search](https://www.elastic.co/guide/en/elasticsearch/plugins/current/intro.html)

## STATUS: ... EXPERIMENTAL ...
The ElasticSearch ingest plugin is very new.

And yes it is similar to https://www.elastic.co/guide/en/elasticsearch/reference/master/user-agent-processor.html

## Getting the UDF
Starting with 7.31.0 the prebuilt UDF is no longer distributed by me.

The **ONLY** reason for this change is that Elastic Search is VERY picky about the version of ES the Plugin was built for. If you have a Yauaa Plugin that was built against ES 8.17.1 then that plugin will not load in ES 8.17.2.

The way now for you to get the right version of the plugin for your installation is to build de UDF yourself.

1) Get a Linux machine (I use Ubuntu 24.04 LTS) with docker and git installed. Running this in a VM or a recent WSL (running on Windows 11) is fine.

2) Get the sourcecode and open the latest released version
   ```bash
    git clone https://github.com/nielsbasjes/yauaa
    cd yauaa
    git checkout v{{%YauaaVersion%}}
    ```

3) Change to the exact version you need. Edit in the `pom.xml` the property for the version you need. Assume you want it for ES 8.17.3 then change the `elasticsearch-8.version` property to that version.
    This command does that:
   ```bash
   sed -i 's@<elasticsearch-8.version>[^<]\+</elasticsearch-8.version>@<elasticsearch-8.version>8.17.3</elasticsearch-8.version>@g'  pom.xml
   ```

    In this example it will look like this `<elasticsearch-8.version>8.17.3</elasticsearch-8.version>`.

4) Start the docker based build environment.
    ```bash
    ./start-docker.sh
    ```

5) In this environment build the plugin
    ```bash
    mvn package -pl :yauaa-devtools,:yauaa-elasticsearch-8
    ```

6) Exit the docker based environment again
    ```bash
    exit
    ```

7) Now you should have the file `udfs/elastic/elasticsearch-8/target/yauaa-elasticsearch-8-{{%YauaaVersion%}}.zip` which you can install on your installation.

Replace `elasticsearch-8` with `elasticsearch-9` in the above example incase you have an ElasticSearch 9.x installation.

## Installing the plugin
You only need to install it into your Elastic Search once

On Elastic Search 8.x
```bash
bin/elasticsearch-plugin install file:///path/to/yauaa-elasticsearch-8-{{%YauaaVersion%}}.zip
```

On Elastic Search 9.x
```bash
bin/elasticsearch-plugin install file:///path/to/yauaa-elasticsearch-9-{{%YauaaVersion%}}.zip
```

## Usage
This plugin is intended to be used in an `ingest pipeline`.

You have to specify the name of the input `field` and the place where
the possible configuration flags are:

| Name                    | Mandatory/Optional | Description                                                                                                                                                          | Default             | Example                                                                                                       |
|-------------------------|--------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------|---------------------------------------------------------------------------------------------------------------|
| field_to_header_mapping | M                  | The mapping from the input field name to the `original` request header name of this field                                                                            | -                   | `"field_to_header_mapping" : { "ua": "User-Agent" }`                                                          |
| target_field            | M                  | The name of the output structure that will be filled with the parse results                                                                                          | `"user_agent"`      | `"parsed_ua"`                                                                                                 |
| fieldNames              | O                  | A list of Yauaa fieldnames that are desired. When specified the system will limit processing to what is needed to get these. This means faster and less memory used. | All possible fields | `[ "DeviceClass", "DeviceBrand", "DeviceName", "AgentNameVersionMajor" ]`                                     |
| cacheSize               | O                  | The number of entries in the LRU cache of the parser                                                                                                                 | `10000`             | `100`                                                                                                         |
| preheat                 | O                  | How many testcases are put through the parser at startup to warmup the JVM                                                                                           | `0`                 | `1000`                                                                                                        |
| extraRules              | O                  | A yaml expression that is a set of extra rules and testcases.                                                                                                        | -                   | `"config:\n- matcher:\n    extract:\n      - '"'"'FirstProductName     : 1 :agent.(1)product.(1)name'"'"'\n"` |

## Example usage

### Basic pipeline
Create a pipeline that just extracts everything using the default settings:

```bash
curl -H 'Content-Type: application/json' -X PUT 'localhost:9200/_ingest/pipeline/yauaa-test-pipeline_basic' -d '
{
  "description": "A pipeline to do whatever",
  "processors": [
    {
      "yauaa" : {
        "field_to_header_mapping" : {
            "useragent":                "User-Agent",
            "uach_platform":            "Sec-CH-UA-Platform",
            "uach_platform_version":    "Sec-CH-UA-Platform-Version"
        },
        "target_field"  : "parsed"
      }
    }
  ]
}
'
```

### Common pipeline
In this example a pipeline is created that only gets the fields that are actually desired.

```bash
curl -H 'Content-Type: application/json' -X PUT 'localhost:9200/_ingest/pipeline/yauaa-test-pipeline_some' -d '
{
  "description": "A pipeline to do whatever",
  "processors": [
    {
      "yauaa" : {
        "field_to_header_mapping" : {
            "useragent":                "User-Agent",
            "uach_platform":            "Sec-CH-UA-Platform",
            "uach_platform_version":    "Sec-CH-UA-Platform-Version"
        },
        "target_field"  : "parsed",
        "fieldNames"    : [ "DeviceClass", "DeviceBrand", "DeviceName", "AgentNameVersionMajor", "FirstProductName" ],
      }
    }
  ]
}
'
```

### Advanced pipeline
In this example a pipeline is created that includes an example of a custom rule.
The hardest part is making the yaml (with quotes, newlines and the needed indentation) encode correctly inside a JSon structure.

```bash
curl -H 'Content-Type: application/json' -X PUT 'localhost:9200/_ingest/pipeline/yauaa-test-pipeline_full' -d '
{
  "description": "A pipeline to do whatever",
  "processors": [
    {
      "yauaa" : {
        "field_to_header_mapping" : {
            "useragent":                "User-Agent",
            "uach_platform":            "Sec-CH-UA-Platform",
            "uach_platform_version":    "Sec-CH-UA-Platform-Version"
        },
        "target_field"  : "parsed",
        "fieldNames"    : [ "DeviceClass", "DeviceBrand", "DeviceName", "AgentNameVersionMajor", "FirstProductName" ],
        "cacheSize" : 10,
        "preheat"   : 10,
        "extraRules" : "config:\n- matcher:\n    extract:\n      - '"'"'FirstProductName     : 1 :agent.(1)product.(1)name'"'"'\n"
      }
    }
  ]
}
'
```

## Put record

I put a record in ElasticSearch using the above mentioned `Advanced pipeline`

```bash
curl -H 'Content-Type: application/json' -X PUT 'localhost:9200/my-index/my-type/1?pipeline=yauaa-test-pipeline_full' -d '
{
  "useragent" : "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36"
}
'
```

which returns

```json
{"_index":"my-index","_type":"my-type","_id":"1","_version":1,"result":"created","_shards":{"total":2,"successful":1,"failed":0},"_seq_no":0,"_primary_term":1}
```

then I retrieve the record from elasticsearch and the additional parse results are now part of the indexed record.

    curl -s -H 'Content-Type: application/json' -X GET 'localhost:9200/my-index/my-type/1' | python -m json.tool

results in

    {
        "_id": "1",
        "_index": "my-index",
        "_primary_term": 1,
        "_seq_no": 0,
        "_source": {
            "parsed": {
                "AgentName": "Chrome",
                "AgentNameVersionMajor": "Chrome 53",
                "AgentVersion": "53.0.2785.124",
                "AgentVersionMajor": "53",
                "DeviceBrand": "Google",
                "DeviceClass": "Phone",
                "DeviceName": "Google Nexus 6",
                "FirstProductName": "Mozilla"
            },
            "useragent": "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36"
        },
        "_type": "my-type",
        "_version": 1,
        "found": true
    }

## NOTES for developers

The ElasticSearch testing tools are quick to complain about jar classloading issues: "jar hell".

To make it possible to test this in IntelliJ you'll need to set a custom property

1) Help --> Edit Custom properties
2) Make sure there is a line with `idea.no.launcher=true`
3) Restart IntelliJ

See also https://stackoverflow.com/questions/51045201/using-the-elasticsearch-test-framework-in-intellij-how-to-resolve-the-jar-hell/51045272
