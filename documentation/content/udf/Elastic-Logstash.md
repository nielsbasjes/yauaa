+++
title = "Elastic LogStash"
+++
# User Defined Function (Filter plugin) for [Elastic Logstash](https://www.elastic.co/guide/en/logstash/current/filter-plugins.html)

# STATUS: ... EXPERIMENTAL ...
The logstash UDF java api is still experimental.

See for more information:
- https://github.com/logstash-plugins/logstash-filter-java_filter_example
- https://github.com/elastic/logstash/issues/9215

## Getting the UDF
You can get the prebuilt filter from [the github releases page](https://github.com/nielsbasjes/yauaa/releases/download/v{{%YauaaVersion%}}/logstash-filter-yauaa-{{%YauaaVersion%}}.gem).

# Building has side effects !
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

1. The source field
2. For each Yauaa field you need the logstash field in which it needs to be placed.

```json
filter {
  yauaa {
    source => "message"
    fields => {
       DeviceClass      => "DevCls"
       AgentNameVersion => "AgntNmVrsn"
    }
  }
}
```

When running this example I get output like this that has the new fields "DevCls" and "AgntNmVrsn":
```json
{
    "@timestamp" => 2019-02-13T10:42:57.491Z,
       "message" => "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.82 Safari/537.36",
      "@version" => "1",
          "host" => "niels-laptop",
    "AgntNmVrsn" => "Chrome 48.0.2564.82",
        "DevCls" => "Desktop",
          "path" => "/tmp/useragent.txt"
}
```
