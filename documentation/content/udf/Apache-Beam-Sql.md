+++
title = "Apache Beam SQL"
+++
## Introduction
This is a User Defined Function for [Apache Beam SQL](https://beam.apache.org).

## Getting the UDF
You can get the prebuilt UDF from [maven central](https://search.maven.org/artifact/nl.basjes.parse.useragent/yauaa-beam-sql/{{%YauaaVersion%}}/jar).

If you use a maven based project simply add this dependency to your project.

```xml
<dependency>
  <groupId>nl.basjes.parse.useragent</groupId>
  <artifactId>yauaa-beam-sql</artifactId>
  <version>{{%YauaaVersion%}}</version>
</dependency>
```

## Available functions

### Getting a single value

To get a single value from the parse result use this one:

    ParseUserAgentField(userAgent, 'DeviceClass')  AS deviceClassField

to give

    Phone

### Getting several values as a Map (requires Apache Beam 2.30.0 or newer)
You can ask for a all fields and return the full map with all of them in there.

    ParseUserAgent(userAgent)                                    AS allFields

If you need multiple fields but not all of them you can speed up the analysis by only asking for those specific fields:

    ParseUserAgent(userAgent, 'DeviceClass', 'AgentNameVersion') AS someFields

With such a map you can then extract the field you really need with SQL syntax similar to this:

    ParseUserAgent(userAgent, 'DeviceClass')['DeviceClass']      AS deviceClass

    ParseUserAgent(userAgent)['AgentNameVersion']                AS agentNameVersion

### Getting several values as JSon

Assuming the input

    Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36

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
          .query(
            "SELECT" +
            "    userAgent                                                     AS userAgent, " +
            "    ParseUserAgent(userAgent, 'DeviceClass', 'AgentNameVersion')  AS parsedUserAgentMap, " +
            "    ParseUserAgentJson(userAgent)                                 AS parsedUserAgentJson, " +
            "    ParseUserAgentField(userAgent, 'DeviceClass')                 AS deviceClass, " +
            "    ParseUserAgentField(userAgent, 'AgentNameVersion')            AS agentNameVersion " +
            "FROM InputStream")
          .registerUdf("ParseUserAgent",      ParseUserAgent.class)
          .registerUdf("ParseUserAgentJson",  ParseUserAgentJson.class)
          .registerUdf("ParseUserAgentField", ParseUserAgentField.class)
        );

## Limitations / Future
The `ParseUserAgent` and `ParseUserAgentJson` have a limitation of at most 10 fieldnames because Calcite does not yet support variable arguments for UDFs. If you need more than 10 fields you currently need to get `all` fields and then extract the fields you need from there.
- https://issues.apache.org/jira/browse/CALCITE-2772
