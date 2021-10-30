+++
title = "Elastic Search"
+++
## User Defined Function (ingest processor) for [Elastic Search](https://www.elastic.co/guide/en/elasticsearch/plugins/current/intro.html)

## STATUS: ... EXPERIMENTAL ...
The ElasticSearch ingest plugin is very new.

And yes it is similar to https://www.elastic.co/guide/en/elasticsearch/reference/master/user-agent-processor.html

## Getting the UDF
You can get the prebuilt ingest plugin from [maven central](https://search.maven.org/remotecontent?filepath=nl/basjes/parse/useragent/yauaa-elasticsearch/{{%YauaaVersion%}}/yauaa-elasticsearch-{{%YauaaVersion%}}.zip).

## Installing the plugin
You only need to install it into your elasticsearch once

```bash
bin/elasticsearch-plugin install file:///path/to/yauaa-elasticsearch-{{%YauaaVersion%}}.zip
```

## Usage
This plugin is intended to be used in an ingest pipeline.

You have to specify the name of the input `field` and the place where
the possible configuration flags are:

| Name | Mandatory/Optional | Description | Default | Example |
| --- | --- | --- | --- | --- |
| field        | M | The name of the input field that contains the UserAgent string | - | `"useragent"` |
| target_field | M | The name of the output structure that will be filled with the parse results | `"user_agent"` | `"parsed_ua"` |
| fieldNames   | O | A list of Yauaa fieldnames that are desired. When specified the system will limit processing to what is needed to get these. This means faster and less memory used. | All possible fields | `[ "DeviceClass", "DeviceBrand", "DeviceName", "AgentNameVersionMajor" ]` |
| cacheSize    | O | The number of entries in the LRU cache of the parser | `10000` | `100` |
| preheat      | O | How many testcases are put through the parser at startup to warmup the JVM | `0` | `1000` |
| extraRules   | O | A yaml expression that is a set of extra rules and testcases. | - | `"config:\n- matcher:\n    extract:\n      - '"'"'FirstProductName     : 1 :agent.(1)product.(1)name'"'"'\n"`


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
        "field"         : "useragent",
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
        "field"         : "useragent",
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
        "field"         : "useragent",
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

# NOTES for developers

The ElasticSearch testing tools are quick to complain about jar classloading issues: "jar hell".

To make it possible to test this in IntelliJ you'll need to set a custom property

1) Help --> Edit Custom properties
2) Make sure there is a line with `idea.no.launcher=true`
3) Restart IntelliJ

See also https://stackoverflow.com/questions/51045201/using-the-elasticsearch-test-framework-in-intellij-how-to-resolve-the-jar-hell/51045272
