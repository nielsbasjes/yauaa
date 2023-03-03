+++
title = "Apache Beam"
+++
## Introduction
This is a User Defined Function for [Apache Beam](https://beam.apache.org)

## Getting the UDF
You can get the prebuilt UDF from [maven central](https://central.sonatype.com/artifact/nl.basjes.parse.useragent/yauaa-beam/{{%YauaaVersion%}}/jar).

If you use a maven based project simply add this dependency to your project.

```xml
<dependency>
  <groupId>nl.basjes.parse.useragent</groupId>
  <artifactId>yauaa-beam</artifactId>
  <version>{{%YauaaVersion%}}</version>
</dependency>
```

## Usage
Assume you have a PCollection with your records.
In most cases I see (clickstream data) these records (In this example this class is called "TestRecord") contain the useragent string in a field and the parsed results must be added to these fields.

Now you must do two things:

1. Determine the names of the fields you need.
1. Add an instance of the (abstract) UserAgentAnalysisDoFn function and implement the functions as shown in the example below. Use the YauaaField annotation to get the setter for the requested fields.

Note that the name of the setters is not important, the system looks at the annotation.

### Example: Only User-Agent

```java
.apply("Extract Elements from Useragent and Client Hints",
    ParDo.of(new UserAgentAnalysisDoFn<TestRecord>(15000) { // Setting the cacheSize
        @Override
        public String getUserAgentString(TestRecord record) {
            return record.useragent;
        }

        @SuppressWarnings("unused") // Called via the annotation
        @YauaaField("DeviceClass")
        public void setDC(TestRecord record, String value) {
            record.deviceClass = value;
        }

        @SuppressWarnings("unused") // Called via the annotation
        @YauaaField("AgentNameVersion")
        public void setANV(TestRecord record, String value) {
            record.agentNameVersion = value;
        }

        @SuppressWarnings("unused") // Called via the annotation
        @YauaaField("OperatingSystemNameVersion")
        public void setOSNV(TestRecord record, String value) {
            record.operatingSystemNameVersion = value;
        }
    })
)
```

### Example: User-Agent Client Hints

The only difference with the "Only User-Agent" implementation is that the `getRequestHeaders` is overridden.
The resulting map should have the `original request header names` as keys and the actual `header value` as the value.

To illustrate:

```java
Map<String, String> requestHeaders = new TreeMap<>();

requestHeaders.put("User-Agent",                       "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.0.0 Safari/537.36");
requestHeaders.put("Sec-Ch-Ua",                        "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"100\", \"Google Chrome\";v=\"100\"");
requestHeaders.put("Sec-Ch-Ua-Arch",                   "\"x86\"");
requestHeaders.put("Sec-Ch-Ua-Full-Version-List",      "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"100.0.4896.75\", \"Google Chrome\";v=\"100.0.4896.75\"");
requestHeaders.put("Sec-Ch-Ua-Mobile",                 "?0");
requestHeaders.put("Sec-Ch-Ua-Model",                  "\"\"");
requestHeaders.put("Sec-Ch-Ua-Platform",               "\"Windows\"");
requestHeaders.put("Sec-Ch-Ua-Platform-Version",       "\"0.1.0\"");
requestHeaders.put("Sec-Ch-Ua-Wow64",                  "?0");
```

Using the analyzer whould then be something like this:

```java
.apply("Extract Elements from Useragent and Client Hints",
    ParDo.of(new UserAgentAnalysisDoFn<TestRecord>(15000) { // Setting the cacheSize
        @Override
        public Map<String, String> getRequestHeaders(TestRecord element) {
            return element.getHeaders();
        }

        @SuppressWarnings("unused") // Called via the annotation
        @YauaaField("DeviceClass")
        public void setDC(TestRecord record, String value) {
            record.deviceClass = value;
        }

        @SuppressWarnings("unused") // Called via the annotation
        @YauaaField("AgentNameVersion")
        public void setANV(TestRecord record, String value) {
            record.agentNameVersion = value;
        }

        @SuppressWarnings("unused") // Called via the annotation
        @YauaaField("OperatingSystemNameVersion")
        public void setOSNV(TestRecord record, String value) {
            record.operatingSystemNameVersion = value;
        }
    })
)
```


## Immutable instances in Apache Beam
Apache Beam requires a DoFn to never modify the provided instance and to always return a new instance that is then passed to the next processing step.
To handle this in a generic way UserAgentAnalysisDoFn has a "clone" method that does this by means of doing a round trip through serialization. If you can do a more efficient way for your specific class then please override the clone method.

## NOTES on defining it as an anonymous class
An anonymous inner class in Java is [by default private](https://stackoverflow.com/questions/319765/accessing-inner-anonymous-class-members).

If you define it as an anonymous inner class as shown above then the system will try to make this class to become public by means of the method .setAccessible(true).
There are situations in which this will fail (amongst others the SecurityManager can block this). If you run into such a scenario then simply 'not' define it inline as an anonymous class and define it as a named (public) class instead.

So the earlier example will look something like this:

```java
public class MyUserAgentAnalysisDoFn extends UserAgentAnalysisDoFn<TestRecord> {
  @Override
  public String getUserAgentString(TestRecord record) {
    return record.useragent;
  }

  @SuppressWarnings("unused") // Called via the annotation
  @YauaaField("DeviceClass")
  public void setDC(TestRecord record, String value) {
    record.deviceClass = value;
  }

  @SuppressWarnings("unused") // Called via the annotation
  @YauaaField("AgentNameVersion")
  public void setANV(TestRecord record, String value) {
    record.agentNameVersion = value;
  }
}
```
and then in the topology simply do this

```java
.apply("Extract Elements from Useragent",
  ParDo.of(new MyUserAgentAnalysisDoFn()));
```
