# User Defined Function for Apache Hive

### ___*** DRAFT ***___
This UDF is a work in progress.


## Getting the UDF
You can get the prebuilt UDF from maven central.
If you use a maven based project simply add this dependency

**FIXME: This has not yet been release to Maven Central. 
At this moment you must build it from sources yourself**

    <dependency>
      <groupId>nl.basjes.parse.useragent</groupId>
      <artifactId>yauaa-hive</artifactId>
      <classifier>udf</classifier>
      <version>2.0</version>
    </dependency>

## Building
Simply install the normal build tools for a Java project (i.e. maven and jdk) and then simply do:

    mvn clean package

## Example usage

First the jar file must be 'known'
Either by doing 

    ADD JAR hdfs:///yauaa-hive-2.0-SNAPSHOT-udf.jar;

or by defining it as a [permanent function](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+DDL#LanguageManualDDL-PermanentFunctions) 

    CREATE FUNCTION ParseUserAgent 
    AS 'nl.basjes.parse.useragent.hive.ParseUserAgent' 
    USING JAR 'hdfs:///yauaa-hive-2.0-SNAPSHOT-udf.jar';

or installing it locally with the Hive Server

***TODO*** 

Then use it

    DESCRIBE FUNCTION ParseUserAgent;

    +-----------------------------------------------------------------------+--+
    |                               tab_name                                |
    +-----------------------------------------------------------------------+--+
    | ParseUserAgent(str) - Parses the UserAgent into all possible pieces.  |
    +-----------------------------------------------------------------------+--+


    SELECT ParseUserAgent('Mozilla/5.0 (X11\; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36');
    
    SELECT useragent,  ParseUserAgent(useragent).deviceclass from useragents;

    > SELECT useragent,  ParseUserAgent(useragent).deviceclass from useragents;
    +------------------------------------------------------------------------------------------------------------+--------------+--+
    |                                                 useragent                                                  | deviceclass  |
    +------------------------------------------------------------------------------------------------------------+--------------+--+
    | Mozilla                                                                                                    | Hacker       |
    | Mozilla/5.0                                                                                                | Unknown      |
    | Mozilla/5.0 (X11 Linux x86_64)                                                                             | Unknown      |
    | Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36  | Desktop      |
    +------------------------------------------------------------------------------------------------------------+--------------+--+


    SELECT useragent,  ParseUserAgent(useragent).deviceclass, ParseUserAgent(useragent).agentname, ParseUserAgent(useragent).agentnameversionmajor from useragents;
    
    +------------------------------------------------------------------------------------------------------------+--------------+------------+------------------------+--+
    |                                                 useragent                                                  | deviceclass  | agentname  | agentnameversionmajor  |
    +------------------------------------------------------------------------------------------------------------+--------------+------------+------------------------+--+
    | Mozilla                                                                                                    | Hacker       | Hacker     | Hacker                 |
    | Mozilla/5.0                                                                                                | Unknown      | Netscape   | Netscape 5             |
    | Mozilla/5.0 (X11 Linux x86_64)                                                                             | Unknown      | X11 Linux  | X11 Linux x86          |
    | Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36  | Desktop      | Chrome     | Chrome 59              |
    +------------------------------------------------------------------------------------------------------------+--------------+------------+------------------------+--+



License
=======
    Yet Another UserAgent Analyzer
    Copyright (C) 2013-2017 Niels Basjes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
