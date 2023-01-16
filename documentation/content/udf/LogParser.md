+++
title = "LogParser"
+++
## Introduction
This is a User Defined Function for [LogParser](https://github.com/nielsbasjes/logparser/)

## Getting the UDF
You can get the prebuilt UDF from [maven central (yauaa-logparser-{{%YauaaVersion%}}-udf.jar)](https://search.maven.org/remotecontent?filepath=nl/basjes/parse/useragent/yauaa-logparser/{{%YauaaVersion%}}/yauaa-logparser-{{%YauaaVersion%}}-udf.jar).

NOTE: You MUST use the `-udf.jar`: yauaa-logparser-{{%YauaaVersion%}}-udf.jar

If you use a maven based project simply add this dependency

```xml
<dependency>
  <groupId>nl.basjes.parse.useragent</groupId>
  <artifactId>yauaa-logparser</artifactId>
  <classifier>udf</classifier>
  <version>{{%YauaaVersion%}}</version>
</dependency>
```

## Client hints
Because the logparser can only dissect a _single_ field into multiple pieces it is impossible to extend this to support User-Agent Client Hints.
