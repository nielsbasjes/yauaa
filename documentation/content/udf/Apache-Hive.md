+++
title = "Apache Hive"
+++
## Introduction
This is a User Defined Function for [Apache Hive](https://hive.apache.org)

## Getting the UDF

You can get the prebuilt UDF from [maven central (yauaa-hive-{{%YauaaVersion%}}-udf.jar)](https://search.maven.org/remotecontent?filepath=nl/basjes/parse/useragent/yauaa-hive/{{%YauaaVersion%}}/yauaa-hive-{{%YauaaVersion%}}-udf.jar).

NOTE: You MUST use the `-udf.jar`: yauaa-hive-{{%YauaaVersion%}}-udf.jar

If you use a maven based project simply add this dependency

```xml
<dependency>
  <groupId>nl.basjes.parse.useragent</groupId>
  <artifactId>yauaa-hive</artifactId>
  <classifier>udf</classifier>
  <version>{{%YauaaVersion%}}</version>
</dependency>
```

## Building
Simply install the normal build tools for a Java project (i.e. maven and jdk) and then simply do:

    mvn clean package

## Example usage

First the jar file must be 'known'
Either by doing

```sql
ADD JAR hdfs:///yauaa-hive-{{%YauaaVersion%}}-udf.jar
```

or by defining it as a [permanent function](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+DDL#LanguageManualDDL-PermanentFunctions)

```sql
CREATE FUNCTION ParseUserAgent
AS 'nl.basjes.parse.useragent.hive.ParseUserAgent'
USING JAR 'hdfs:///yauaa-hive-{{%YauaaVersion%}}-udf.jar';
```

If you are using a recent version of Hive you can also do

```sql
CREATE FUNCTION ParseUserAgent
AS 'nl.basjes.parse.useragent.hive.ParseUserAgent'
USING JAR 'ivy://nl.basjes.parse.useragent:yauaa-hive:{{%YauaaVersion%}}?classifier=udf';
```

or installing it locally with the Hive Server

***TODO: Document installation***

Verify if it has been installed

    DESCRIBE FUNCTION ParseUserAgent;

    +-----------------------------------------------------------------------+
    |                               tab_name                                |
    +-----------------------------------------------------------------------+
    | ParseUserAgent(str) - Parses the UserAgent into all possible pieces.  |
    +-----------------------------------------------------------------------+


    > DESCRIBE FUNCTION EXTENDED ParseUserAgent;
    +---------------------------------------------------------------------------+
    |                                   tab_name                                |
    +---------------------------------------------------------------------------+
    | parseuseragent(str) - Parses the UserAgent into all possible pieces.      |
    | Synonyms: default.parseuseragent                                          |
    | Example:                                                                  |
    | > SELECT ParseUserAgent(useragent).DeviceClass,                           |
    |          ParseUserAgent(useragent).OperatingsystemNameVersion,            |
    |          ParseUserAgent(useragent).AgentNameVersionMajor                  |
    |   FROM   clickLogs;                                                       |
    | +---------------+-----------------------------+------------------------+  |
    | |  deviceclass  | operatingsystemnameversion  | agentnameversionmajor  |  |
    | +---------------+-----------------------------+------------------------+  |
    | | Phone         | Android 6.0                 | Chrome 46              |  |
    | | Tablet        | Android 5.1                 | Chrome 40              |  |
    | | Desktop       | Linux ??                    | Chrome 59              |  |
    | | Game Console  | Windows 10.0                | Edge 13                |  |
    | +---------------+-----------------------------+------------------------+  |
    |                                                                           |
    +---------------------------------------------------------------------------+

Basic test it works (trimmed the output here)

    > SELECT ParseUserAgent('Mozilla/5.0 (Linux\; Android 6.0\; Nexus 6 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.76 Mobile Safari/537.36');
    +----------------------------------------------------+
    |                        _c0                         |
    +----------------------------------------------------+
    | { "deviceclass":"Phone",
        "devicename":"Google Nexus 6",
        "devicebrand":"Google",
        ...
        "operatingsystemnameversion":"Android 6.0",
        ...
        "layoutenginenameversion":"Blink 46.0",
        ...
        "agentclass":"Browser",
        ...
        "agentnameversion":"Chrome 46.0.2490.76",
        ...
        } |
    +----------------------------------------------------+
    1 row selected (5.682 seconds)


Usage example:

    CREATE TABLE useragents (useragent STRING COMMENT 'The useragent string');

    INSERT INTO TABLE useragents VALUES ('Mozilla/5.0 (Linux\; Android 6.0\; Nexus 6 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.76 Mobile Safari/537.36');
    INSERT INTO TABLE useragents VALUES ('Mozilla/5.0 (Linux\; Android 5.1\; Nexus 10 Build/LMY47D) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.109 Safari/537.36');
    INSERT INTO TABLE useragents VALUES ('Mozilla/5.0 (X11\; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36');
    INSERT INTO TABLE useragents VALUES ('Mozilla/5.0 (Windows NT 10.0\; Win64\; x64\; Xbox\; Xbox One) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Safari/537.36 Edge/13.10553');
    SELECT ParseUserAgent(useragent).DeviceClass, ParseUserAgent(useragent).OperatingsystemNameVersion, ParseUserAgent(useragent).AgentName, ParseUserAgent(useragent).AgentNameVersionMajor from useragents;
    +---------------+-----------------------------+------------+------------------------+
    |  deviceclass  | operatingsystemnameversion  | agentname  | agentnameversionmajor  |
    +---------------+-----------------------------+------------+------------------------+
    | Phone         | Android 6.0                 | Chrome     | Chrome 46              |
    | Tablet        | Android 5.1                 | Chrome     | Chrome 40              |
    | Desktop       | Linux ??                    | Chrome     | Chrome 59              |
    | Game Console  | Windows 10.0                | Edge       | Edge 13                |
    +---------------+-----------------------------+------------+------------------------+


# ClientHints example

If you pass only a single parameter it is assumed to be the "User-Agent".

If you want to make use of the support for the `User-Agent Client Hints` you must call the function from your SQL with a list of `header name` and `value`. The header names must be the same as what a browser would send to the webserver (see: [Specification](https://wicg.github.io/ua-client-hints/#http-ua-hints)).

Example:

    SELECT 'CLIENTHINTS',
        parsedUseragentAllFields.DeviceClass                   AS deviceClass,
        parsedUseragentAllFields.AgentNameVersionMajor         AS agentNameVersionMajor,
        parsedUseragentAllFields.OperatingSystemNameVersion    AS operatingSystemNameVersion
    FROM (
        SELECT ParseUserAgent(
            'user-Agent',                   useragent,
            'sec-CH-UA-Platform',           chPlatform,
            'sec-CH-UA-Platform-Version',   chPlatformVersion
        ) AS parsedUseragentAllFields
        FROM   clickLogs
    ) ParsedSubSelect;
