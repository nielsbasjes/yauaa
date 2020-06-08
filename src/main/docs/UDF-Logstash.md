# User Defined Function for Elastic Logstash

# STATUS: ... EXPERIMENTAL ...
The logstash UDF java api is still experimental.

See https://github.com/logstash-plugins/logstash-filter-java_filter_example and https://github.com/elastic/logstash/issues/9215 for more information.

Starting with version 5.15 you can get the prebuilt filter from
[github](https://github.com/nielsbasjes/yauaa/releases/download/v{{ book.YauaaVersion }}/logstash-filter-yauaa-{{ book.YauaaVersion }}.gem).

# Building has side effects !
Because at this time none of the required dependencies are in maven central this build does something rarely seen:

It downloads the logstash distribution, extracts the logstash-core.jar and installs it locally for maven
to use as dependency.

So your local maven repo will get a jar injected that is not normally available via Maven central or something similar.

See these for more details:
- https://github.com/nielsbasjes/yauaa/issues/146
- https://github.com/elastic/logstash/issues/11002

## Installing the filter

You only need to install it into your logstash once per installation

<pre><code>logstash-plugin remove logstash-filter-yauaa
logstash-plugin install ./udfs/logstash/target/logstash-filter-yauaa-{{ book.YauaaVersion }}.gem</code></pre>

## Example usage

You need to specify

1. The source field
2. For each Yauaa field you need the logstash field in which it needs to be placed.


    filter {
      yauaa {
        source => "message"
        fields => {
           DeviceClass      => "DevCls"
           AgentNameVersion => "AgntNmVrsn"
        }
      }
    }

When running this example I get output like this that has the new fields "DevCls" and "AgntNmVrsn":

    {
        "@timestamp" => 2019-02-13T10:42:57.491Z,
           "message" => "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.82 Safari/537.36",
          "@version" => "1",
              "host" => "niels-laptop",
        "AgntNmVrsn" => "Chrome 48.0.2564.82",
            "DevCls" => "Desktop",
              "path" => "/tmp/useragent.txt"
    }
