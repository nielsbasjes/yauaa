# User Defined Function for Apache Pig

## Getting the UDF
You can get the prebuild UDF from maven central.
If you use a maven based project simply add this dependency

    <dependency>
      <groupId>nl.basjes.parse.useragent</groupId>
      <artifactId>yauaa</artifactId>
      <classifier>udf</classifier>
      <version>0.7</version>
    </dependency>

## Building
Simply install the normal build tools for a Java project (i.e. maven and jdk) and then simply do:

    mvn clean package

## Example usage
    -- Import the UDF jar file so this script can use it
    REGISTER ../target/*-udf.jar;

    -- Define a more readable name for the UDF
    DEFINE ParseUserAgent  nl.basjes.parse.useragent.pig.ParseUserAgent;

    rawData =
        LOAD 'testcases.txt'
        USING PigStorage()
        AS  ( useragent: chararray );

    UaData =
        FOREACH  rawData
        GENERATE useragent,
                 -- Do NOT specify a type for this field as the UDF provides the definitions
                 ParseUserAgent(useragent) AS parsedAgent;

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
