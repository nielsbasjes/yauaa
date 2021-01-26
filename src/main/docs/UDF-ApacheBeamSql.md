# User Defined Function for [Apache Beam SQL](https://beam.apache.org)

## Getting the UDF
You can get the prebuilt UDF from [maven central](https://search.maven.org/artifact/nl.basjes.parse.useragent/yauaa-beam-sql/{{ book.YauaaVersion }}/jar).

If you use a maven based project simply add this dependency to your project.

<pre><code>&lt;dependency&gt;
  &lt;groupId&gt;nl.basjes.parse.useragent&lt;/groupId&gt;
  &lt;artifactId&gt;yauaa-beam-sql&lt;/artifactId&gt;
  &lt;version&gt;{{ book.YauaaVersion }}&lt;/version&gt;
&lt;/dependency&gt;
</code></pre>

## Available functions

Assuming the input

    Mozilla\/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36

To parse this input value into all possible values and returns the complete result as a single JSon string use this:

    ParseUserAgentJson(userAgent)

to give (single line)

    {"Useragent":"Mozilla\/5.0 (Linux; Android 7.0; Nexus 6 Build\/NBD90Z) AppleWebKit\/537.36 (KHTML, like Gecko) Chrome\/53.0.2785.124 Mobile Safari\/537.36",
    "DeviceClass":"Phone","DeviceName":"Google Nexus 6","DeviceBrand":"Google",
    "OperatingSystemClass":"Mobile","OperatingSystemName":"Android","OperatingSystemVersion":"7.0","OperatingSystemVersionMajor":"7","OperatingSystemNameVersion":"Android 7.0","OperatingSystemNameVersionMajor":"Android 7","OperatingSystemVersionBuild":"NBD90Z",
    "LayoutEngineClass":"Browser","LayoutEngineName":"Blink","LayoutEngineVersion":"53.0","LayoutEngineVersionMajor":"53","LayoutEngineNameVersion":"Blink 53.0","LayoutEngineNameVersionMajor":"Blink 53",
    "AgentClass":"Browser","AgentName":"Chrome","AgentVersion":"53.0.2785.124","AgentVersionMajor":"53","AgentNameVersion":"Chrome 53.0.2785.124","AgentNameVersionMajor":"Chrome 53"}

To get a JSon with only specific fields you can do (up to 10 fields van be requested this way)

    ParseUserAgentJson(userAgent, 'DeviceClass', 'AgentNameVersion')

to give

    {"DeviceClass":"Phone","AgentNameVersion":"Chrome 53.0.2785.124"}

To get a single value from the parse result use this one:

    ParseUserAgentField(userAgent, 'DeviceClass')

to give

    Phone

## Example usage
Assume you have a PCollection with your records.

    PCollection<Row> input = ...

You can then put that through an SQL statement to transform it.
You have to name the input (in this example we call it InputStream),
and you have to register the UDF classes you want to use with the name you want to have in your SQL statement.

    PCollection<Row> result =
        // This way we give a name to the input stream for use in the SQL
        PCollectionTuple.of("InputStream", input)
            // Apply the SQL with the UDFs we need.
            .apply("Execute SQL", SqlTransform
                .query("SELECT" +
                       "    userAgent                                            AS userAgent, " +
                       "    ParseUserAgentJson(userAgent)                        AS parsedUserAgentJson, " +
                       "    ParseUserAgentField(userAgent, 'DeviceClass')        AS deviceClass, " +
                       "    ParseUserAgentField(userAgent, 'AgentNameVersion')   AS agentNameVersion " +
                       "FROM InputStream")
                .registerUdf("ParseUserAgentJson",  ParseUserAgentJson.class)
                .registerUdf("ParseUserAgentField", ParseUserAgentField.class)
            );

## Future
At this point in time simply returning a Map of all keys and values is not yet possible because of a problem in Apache Beam.
See
- https://issues.apache.org/jira/browse/BEAM-9267
- https://issues.apache.org/jira/browse/BEAM-9379
- https://github.com/apache/beam/pull/12962
