# User Defined Function for Apache Beam

## Getting the UDF
You can get the prebuilt UDF from [maven central](https://search.maven.org/artifact/nl.basjes.parse.useragent/yauaa-beam/{{ book.YauaaVersion }}/jar).

If you use a maven based project simply add this dependency to your project.

<pre><code>&lt;dependency&gt;
  &lt;groupId&gt;nl.basjes.parse.useragent&lt;/groupId&gt;
  &lt;artifactId&gt;yauaa-beam&lt;/artifactId&gt;
  &lt;version&gt;{{ book.YauaaVersion }}&lt;/version&gt;
&lt;/dependency&gt;
</code></pre>

## Example usage
Assume you have a PCollection with your records.
In most cases I see (clickstream data) these records (In this example this class is called "TestRecord") contain the useragent string in a field and the parsed results must be added to these fields.

Now you must do two things:

1. Determine the names of the fields you need.
1. Add an instance of the (abstract) UserAgentAnalysisDoFn function and implement the functions as shown in the example below. Use the YauaaField annotation to get the setter for the requested fields.

Note that the name of the two setters is not important, the system looks at the annotation.

    .apply("Extract Elements from Useragent",
        ParDo.of(new UserAgentAnalysisDoFn<TestRecord>(15000) { // Setting the cacheSize
            @Override
            public String getUserAgentString(TestRecord record) {
                return record.useragent;
            }

            @SuppressWarnings("unused")
            @YauaaField("DeviceClass")
            public void setDC(TestRecord record, String value) {
                record.deviceClass = value;
            }

            @SuppressWarnings("unused")
            @YauaaField("AgentNameVersion")
            public void setANV(TestRecord record, String value) {
                record.agentNameVersion = value;
            }
        }));

## Immutable instances in Apache Beam
Apache Beam requires a DoFn to never modify the provided instance and to always return a new instance that is then passed to the next processing step.
To handle this in a generic way UserAgentAnalysisDoFn has a "clone" method that does this by means of doing a round trip through serialization. If you can do a more efficient way for your specific class then please override the clone method.

## NOTES on defining it as an anonymous class
An anonymous inner class in Java is [by default private](https://stackoverflow.com/questions/319765/accessing-inner-anonymous-class-members).

If you define it as an anonymous inner class as shown above then the system will try to make this class to become public by means of the method .setAccessible(true).
There are situations in which this will fail (amongst others the SecurityManager can block this). If you run into such a scenario then simply 'not' define it inline as an anonymous class and define it as a named (public) class instead.

So the earlier example will look something like this:


    public class MyUserAgentAnalysisDoFn extends UserAgentAnalysisDoFn<TestRecord> {
        @Override
        public String getUserAgentString(TestRecord record) {
            return record.useragent;
        }

        @SuppressWarnings("unused")
        @YauaaField("DeviceClass")
        public void setDC(TestRecord record, String value) {
            record.deviceClass = value;
        }

        @SuppressWarnings("unused")
        @YauaaField("AgentNameVersion")
        public void setANV(TestRecord record, String value) {
            record.agentNameVersion = value;
        }
    }

and then in the topology simply do this

    .apply("Extract Elements from Useragent",
        ParDo.of(new MyUserAgentAnalysisDoFn()));
