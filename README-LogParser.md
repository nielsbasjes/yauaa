# User Defined Function for LogParser

## Getting the UDF
You can get the prebuild UDF from maven central.
If you use a maven based project simply add this dependency

    <dependency>
      <groupId>nl.basjes.parse.useragent</groupId>
      <artifactId>yauaa-logparser</artifactId>
      <classifier>udf</classifier>
      <version>0.8</version>
    </dependency>

## Building
Simply install the normal build tools for a Java project (i.e. maven and jdk) and then simply do:

    mvn clean package

## Example usage
    -- Import the UDF jar file so this script can use it
    REGISTER *.jar;

    %declare LOGFILE   'access.log'
    %declare LOGFORMAT '%h %l %u %t "%r" %>s %b "%{Referer}i" "%{User-Agent}i"'

    OSName =
      LOAD '$LOGFILE'
      USING nl.basjes.pig.input.apachehttpdlog.Loader( '$LOGFORMAT'
            , '-load:nl.basjes.parse.useragent.dissector.UserAgentDissector:'
            , 'STRING:request.user-agent.operating_system_name_version'
        ) AS (
            os_name:chararray
        );

    OSNameCount =
        FOREACH  OSName
        GENERATE os_name AS os_name:chararray,
                 1L      AS clicks:long;

    CountsPerOSName =
        GROUeP OSNameCount
        BY    (os_name);

    SumsPerOSName =
        FOREACH  CountsPerOSName
        GENERATE SUM(OSNameCount.clicks) AS clicks,
                 group                   AS useragent;

    DUMP SumsPerOSName;

    --STORE SumsPerOSName
    --    INTO  'TopUseragents'
    --    USING org.apache.pig.piggybank.storage.CSVExcelStorage('	','NO_MULTILINE', 'UNIX');


License
=======
    Yet Another UserAgent Analyzer
    Copyright (C) 2013-2016 Niels Basjes

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
