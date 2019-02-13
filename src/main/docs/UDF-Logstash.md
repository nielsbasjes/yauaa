# User Defined Function for Logstash

# STATUS: ... EXPERIMENTAL ...
The logstash UDF java api is still experimental.

See https://github.com/logstash-plugins/logstash-filter-java_filter_example and https://github.com/elastic/logstash/issues/9215 for more information.

The only way you can get this filter right now is to build it yourself.

Because at this time none of the required dependencies are in maven central you first need to build logstash yourself.

```
git clone --branch 6.6 --single-branch https://github.com/elastic/logstash.git
cd logstash
./gradlew assemble
rake bootstrap
cd ..
```

The install the build jar file into your local maven repository 

```
mvn install:install-file        \
     -DgroupId=org.logstash     \
     -DartifactId=logstash-core \
     -Dpackaging=jar            \
     -Dversion=6.6.1            \
     -Dfile=./logstash/logstash-core/build/libs/logstash-core-6.6.1.jar 
```

Then go to where you downloaded the Yauaa sources and run

```
mvn clean package -Plogstash -DskipTests=true
```

After several minutes you'll find the filter gem with a name similar to this 

<pre><code>./udfs/logstash/target/logstash-filter-yauaa-{{ book.YauaaVersion }}.gem</code></pre>

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
