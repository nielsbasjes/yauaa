# User Defined Function for Apache Flink Table

## Getting the UDF
You can get the prebuilt UDF from [maven central](https://search.maven.org/artifact/nl.basjes.parse.useragent/yauaa-flink-table/{{ book.YauaaVersion }}/jar).

If you use a maven based project simply add this dependency to your project.

<pre><code>&lt;dependency&gt;
  &lt;groupId&gt;nl.basjes.parse.useragent&lt;/groupId&gt;
  &lt;artifactId&gt;yauaa-flink-table&lt;/artifactId&gt;
  &lt;version&gt;{{ book.YauaaVersion }}&lt;/version&gt;
&lt;/dependency&gt;
</code></pre>

## Syntax
Assume you register this function under the name `ParseUserAgent`
Then the generic usage in your SQL is

<pre><code>ParseUserAgent(&lt;useragent&gt;, &lt;requested fieldname&gt;)</code></pre>

## Example usage (Java)
Assume you have a either a BatchTableEnvironment or a StreamTableEnvironment in which you have defined your records as a table.
In most cases I see (clickstream data) these records contain the useragent string in a column.

    // Give the stream a Table Name
    tableEnv.registerDataStream("AgentStream", inputStream, "timestamp, url, useragent");

Now you must do four things:

* Determine the names of the fields you need.
* Register the function with the full list of all the fields you want under the name you want.
* Use the function in your SQL to extract one field at a time.
* Run the query


    // Register the function with all the desired fieldnames and optionally the size of the cache
    tableEnv.registerFunction("ParseUserAgent", new AnalyzeUseragentFunction(15000, "DeviceClass", "AgentNameVersionMajor"));

    // Define the query.
    String sqlQuery =
        "SELECT useragent,"+
        "       ParseUserAgent(useragent, 'DeviceClass')            as DeviceClass," +
        "       ParseUserAgent(useragent, 'AgentNameVersionMajor')  as AgentNameVersionMajor" +
        "FROM AgentStream";

    Table  resultTable   = tableEnv.sqlQuery(sqlQuery);

