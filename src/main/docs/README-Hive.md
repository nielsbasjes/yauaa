# User Defined Function for Apache Hive

## Getting the UDF
You can get the prebuilt UDF from maven central.
If you use a maven based project simply add this dependency

    <dependency>
      <groupId>nl.basjes.parse.useragent</groupId>
      <artifactId>yauaa-hive</artifactId>
      <classifier>udf</classifier>
      <version>5.0</version>
    </dependency>

## Building
Simply install the normal build tools for a Java project (i.e. maven and jdk) and then simply do:

    mvn clean package

## Example usage

First the jar file must be 'known'
Either by doing 

    ADD JAR hdfs:///yauaa-hive-4.3-udf.jar;

or by defining it as a [permanent function](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+DDL#LanguageManualDDL-PermanentFunctions) 

    CREATE FUNCTION ParseUserAgent 
    AS 'nl.basjes.parse.useragent.hive.ParseUserAgent' 
    USING JAR 'hdfs:///yauaa-hive-4.3-udf.jar';

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
    | | Desktop       | Linux Intel x86_64          | Chrome 59              |  |
    | | Game Console  | Windows 10.0                | Edge 13                |  |
    | +---------------+-----------------------------+------------------------+  |
    |                                                                           |
    +---------------------------------------------------------------------------+

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
    | Desktop       | Linux Intel x86_64          | Chrome     | Chrome 59              |
    | Game Console  | Windows 10.0                | Edge       | Edge 13                |
    +---------------+-----------------------------+------------+------------------------+


License
=======
    Yet Another UserAgent Analyzer
    Copyright (C) 2013-2018 Niels Basjes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
