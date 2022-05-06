+++
title = "Apache Flink Table/SQL"
+++
## Introduction
This is a User Defined Function for [Apache Flink](https://flink.apache.org) Table

## Getting the UDF
You can get the prebuilt UDF from [maven central](https://search.maven.org/artifact/nl.basjes.parse.useragent/yauaa-flink-table/{{%YauaaVersion%}}/jar).

If you use a maven based project simply add this dependency to your project.

```xml
<dependency>
  <groupId>nl.basjes.parse.useragent</groupId>
  <artifactId>yauaa-flink-table</artifactId>
  <version>{{%YauaaVersion%}}</version>
</dependency>
```

## Syntax
Assume you register this function under the name `ParseUserAgent`
Then the generic usage in your SQL is

```sql
ParseUserAgent(<useragent>)
```

This returns a `Map<String, String>` with all the requested values in one go.

If you want to make use of the support for the `User-Agent Client Hints` you must call the function from your SQL with a list of `header name` and `value`. The header names must be the same as what a browser would send to the webserver (see: [Specification](https://wicg.github.io/ua-client-hints/#http-ua-hints)).

Essentially two forms are now possible:

    ParseUserAgent ( <useragent> , [<header name>,<value>]+ )

and the variant which requires the presense of a `User-Agent` header.

    ParseUserAgent ( [<header name>,<value>]+ )

For example:
```sql
ParseUserAgent(
     'User-Agent',                   useragent,
     'Sec-CH-UA-Platform',           chPlatform,
     'Sec-CH-UA-Platform-Version',   chPlatformVersion
) AS parsedUseragent
```

## Example usage (Java)
Assume you have either a BatchTableEnvironment or a StreamTableEnvironment in which you have defined your records as a table.
In most cases I see (clickstream data) these records contain the useragent string in a column.

```java
// Give the stream a Table Name
tableEnv.registerDataStream("AgentStream", inputStream, "timestamp, url, useragent");
```

Now you must do four things:

* Determine the names of the fields you need.
* Register the function with the full list of all the fields you want under the name you want.
* Use the function in your SQL to do the parsing and extract the fields from that.
* Run the query


```java
// Register the function with all the desired fieldnames and optionally the size of the cache
tableEnv.registerFunction("ParseUserAgent", new AnalyzeUseragentFunction(15000, "DeviceClass", "AgentNameVersionMajor"));

// Define the query.
String sqlQuery =
    "SELECT useragent,"+
    "       ParseUserAgent(useragent)   as parsedUseragent" +
    "FROM AgentStream";

Table  resultTable   = tableEnv.sqlQuery(sqlQuery);

// A String and the Map with all results
TypeInformation<Row> tupleType = new RowTypeInfo(STRING, MAP(STRING, STRING));
DataStream<Row> resultSet = tableEnv.toAppendStream(resultTable, tupleType);
```

or something like this

```java
// Register the function with all the desired fieldnames and optionally the size of the cache
tableEnv.registerFunction("ParseUserAgent", new AnalyzeUseragentFunction(15000, "DeviceClass", "AgentNameVersionMajor"));

// Define the query.
String sqlQuery =
    "SELECT useragent,"+
    "       parsedUseragent['DeviceClass']              AS deviceClass," +
    "       parsedUseragent['AgentNameVersionMajor']    AS agentNameVersionMajor " +
    "FROM ( " +
    "   SELECT useragent," +
    "          ParseUserAgent(useragent) AS parsedUseragent" +
    "   FROM   AgentStream " +
    ")";

Table  resultTable   = tableEnv.sqlQuery(sqlQuery);

// 3 Strings
TypeInformation<Row> tupleType = new RowTypeInfo(STRING, STRING, STRING);
DataStream<Row> resultSet = tableEnv.toAppendStream(resultTable, tupleType);
```

