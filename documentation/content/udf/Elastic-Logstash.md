+++
title = "Elastic LogStash"
+++
## Introduction
User Defined Function (Filter plugin) for [Elastic Logstash](https://www.elastic.co/guide/en/logstash/current/filter-plugins.html)

## STATUS: ... EXPERIMENTAL ...
The logstash UDF java api is still experimental.

See for more information:
- https://github.com/logstash-plugins/logstash-filter-java_filter_example
- https://github.com/elastic/logstash/issues/9215

## Getting the UDF
You can get the prebuilt filter from [the GitHub releases page](https://github.com/nielsbasjes/yauaa/releases/download/v{{%YauaaVersion%}}/logstash-filter-yauaa-{{%YauaaVersion%}}.gem).

## Building has side effects !
Because at this time none of the required dependencies are in maven central this build does something rarely seen:

It downloads the entire logstash distribution, extracts the logstash-core.jar and installs it locally for maven
to use as dependency.

So your local maven repo will get a jar injected that is not normally available via Maven central or something similar.

See these for more details:
- https://github.com/nielsbasjes/yauaa/issues/146
- https://github.com/elastic/logstash/issues/11002

## Installing the filter
You only need to install it into your logstash once per installation

```bash
logstash-plugin remove logstash-filter-yauaa
logstash-plugin install ./udfs/logstash/target/logstash-filter-yauaa-{{%YauaaVersion%}}.gem
```

## Example usage

You need to specify

1. The source field which maps the fields in the record to their `original` request headers.
2. For each Yauaa field you need the logstash field in which it needs to be placed.

```
filter {
  yauaa {
    source => {
      "ua"      => "User-Agent"
      "uap"     => "Sec-CH-UA-Platform"
      "uapv"    => "Sec-CH-UA-Platform-Version"
    }
    fields => {
      "DeviceClass"                      => "uaDC"
      "DeviceName"                       => "uaDN"
      "DeviceBrand"                      => "uaDB"
      "OperatingSystemClass"             => "uaOSC"
      "OperatingSystemNameVersion"       => "uaOSNV"
      "LayoutEngineClass"                => "uaLEC"
      "LayoutEngineNameVersion"          => "uaLENV"
      "AgentClass"                       => "uaAC"
      "AgentName"                        => "uaAN"
      "AgentNameVersion"                 => "uaAANV"
      "AgentNameVersionMajor"            => "uaANVM"
    }
  }
}
```

When running this example I get output like this (sorted the fields for readbility) that has the new fields:
```
{
    "@timestamp" => 2022-04-24T09:30:11.051Z,
      "@version" => "1",
          "host" => "b9bf088b8c92",
       "message" => "HeartBeat event to trigger the test",
            "ua" => "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.60 Safari/537.36",
           "uap" => "\"macOS\"",
          "uapv" => "\"12.3.1\"",
          "uaDC" => "Desktop",
          "uaDN" => "Apple Macintosh",
          "uaDB" => "Apple",
         "uaOSC" => "Desktop",
        "uaOSNV" => "Mac OS 12.3.1",
         "uaLEC" => "Browser",
        "uaLENV" => "Blink 100.0",
          "uaAC" => "Browser",
          "uaAN" => "Chrome",
        "uaANVM" => "Chrome 100",
        "uaAANV" => "Chrome 100.0.4896.60"
}
```
