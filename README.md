Introduction
============
This is a library that tries to parse and analyze the useragent string and extract as many relevant attributes as possible.
There are as little as possible lookup tables included the system really tries to analyze the useragent and extract values from it.

The resulting output fields can be classified into several categories:

- The **Device**:
The hardware that was used.
- The **Operating System**:
The base software that runs on the hardware
- The **Layout Engine**:
The underlying core that converts the 'HTML' into a visual/interactive
- The **Agent**:
The actual "Browser" that was used.
- **Extra fields**:
In some cases we have additional fields to describe the agent. These fields are among others specific fields for the Facebook and Kobo apps,
and fields to describe deliberate useragent manipulation situations (Anonymization, Hackers, etc.)

Note that **not all fields are always available**. So if you look at a specific field you will in general find null values and "Unknown" in there aswell.

Example output
==============
As an example the useragent of my phone:

    Mozilla/5.0 (Linux; Android 6.0.1; Nexus 6 Build/MOB30M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.81 Mobile Safari/537.36

is converted into this set of fields:

| Field name | Value |
| --- | --- |
|  **Device**Class                      | 'Phone'                |
|  **Device**Name                       | 'Nexus 6'              |
|  **OperatingSystem**Class             | 'Mobile'               |
|  **OperatingSystem**Name              | 'Android'              |
|  **OperatingSystem**Version           | '6.0.1'                |
|  **OperatingSystem**NameVersion       | 'Android 6.0.1'        |
|  **OperatingSystem**VersionBuild      | 'MOB30M'               |
|  **LayoutEngine**Class                | 'Browser'              |
|  **LayoutEngine**Name                 | 'Blink'                |
|  **LayoutEngine**Version              | '51.0'                 |
|  **LayoutEngine**VersionMajor         | '51'                   |
|  **LayoutEngine**NameVersion          | 'Blink 51.0'           |
|  **LayoutEngine**NameVersionMajor     | 'Blink 51'             |
|  **Agent**Class                       | 'Browser'              |
|  **Agent**Name                        | 'Chrome'               |
|  **Agent**Version                     | '51.0.2704.81'         |
|  **Agent**VersionMajor                | '51'                   |
|  **Agent**NameVersion                 | 'Chrome 51.0.2704.81'  |
|  **Agent**NameVersionMajor            | 'Chrome 51'            |


Using the analyzer
==================
In addition to the UDFs for Apache Pig and Platfora (see below) this analyzer
can also be used in Java based applications.

First add the library as a dependency to your application.
This has been published to maven central so that should work in almost any environment.

    <dependency>
      <groupId>nl.basjes.parse.useragent</groupId>
      <artifactId>yauaa</artifactId>
      <version>0.4</version>
    <dependency>

and in your application you can use it as simple as this

        UserAgentAnalyzer uaa = new UserAgentAnalyzer();

        UserAgent agent = uaa.parse("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.11) Gecko/20071127 Firefox/2.0.0.11");

        for (String fieldName: agent.getAvailableFieldNamesSorted()) {
            System.out.println(fieldName + " = " + agent.getValue(fieldName));
        }

Note that not all fields are available after every parse. So be prepared to receive a 'null' if you extract a specific name.

Parsing Useragents
==================
Parsing useragents is considered by many to be a ridiculously hard problem.
The main problems are:

- Although there seems to be a specification, many do not follow it.
- Useragents LIE that they are their competing predecessor with an extra flag.

The pattern the 'normal' browser builders are following is that they all LIE about the ancestor they are trying to improve upon.

The reason this system (historically) works is because a lot of website builders do a very simple check to see if they can use a specific feature.

    if (useragent.contains("Chrome")) {
       // Use the chrome feature we need.
    }

Some may improve on this an actually check the (major) version that follows.

A good example of this is the Edge browser:

    Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.10136

It says it:

- is Mozilla/5.0
- uses AppleWebKit/537.36
- for "compatibility" the AppleWebKit lie about being "KHTML" and that it is siliar to "Gecko" are also copied
- is Chrome 42
- is Safari 537
- is Edge 12

So any website looking for the word it triggers upon will find it and enable the right features.

How many other analyzers work
=============================
When looking at most implementations of analysing the useragents I see that most implementations are based around
lists of regular expressions.
These are (in the systems I have seen) executed in a specific order to find the first one that matches.

In this solution direction the order in which things occur determines if the patterns match or not.

Regular expressions are notoriously hard to write and debug and (unless you make them really complex) the order in which parts of the pattern occur is fixed.

Core design idea
================
I wanted to see if a completely different approach would work: Can we actually parse these things into a tree and work from there.

The parser (ANTLR4 based) will be able to parse a lot of the agents but not all.
Tests have shown that it will parse >99% of all useragents on a large website which is more than 99.99% of the traffic.

Now the ones that it is not able to parse are the ones that have been set manually to a invalid value.
So if that happens we assume you are a hacker.
In all other cases we have matchers that are triggered if a sepcific value is found by the parser.
Such a matcher then tells this class is has found a match for a certain attribute with a certain confidence level (0-10000).
In the end the matcher that has found a match with the highest confidence for a value 'wins'.


High level implementation overview
==================================================
The main concept of this useragent parser is that we have two things:

1. A Parser (ANTLR4) that converts the useragent into a nice tree through which we can walk along.
2. A collection of matchers.
  - A matcher triggers if a set of patterns is present in the tree.
  - Each pattern is detected by a "matcher action" that triggers and can fill a single attribute.
    If a matcher triggers a set of attributes get set with a value and a confidence level
  - All results from all triggered matchers (and actions) are combined and for each individual attribute the 'highest value' wins.

As a performance optimization we walk along the parsed tree once and fire everything we find into a precomputed hashmap that
points to all the applicable matcher actions. As a consequence

  - the matching is relatively fast even though the number of matchers already runs into the few hundreds.
  - the startup is "slow"
  - the memory footprint is pretty big due to the number of matchers, the size of the hashmap and the cache of the parsed useragents.

Performance
===========
On my i7 system I see a speed ranging from 500 to 4000 useragents per second (depending on the length and ambiguities in the useragent).
On average the speed is above 1000 per second or <1ms each.
A LRU cache is in place that does over 1M per second if they are in the cache.

In the canonical usecase of analysing clickstream data you will see a <1ms hit per visitor (or better: per new non-cached useragent)
and for all the other clicks the values are retrieved from this cache at close to 0 time.

-------

# User Defined Functions
## Introduction
Several external computation systems support the concept of a User Defined Function (UDF).
A UDF is simply a way of making functionality (in this can the calculation of the Financial reporting periods
available in such a system.

For two such systems Apache Pig and Platfora (both are used within bol.com (where I work)) we have written such
a UDF which are both part of this project.

-------

## Apache Pig
### Building
Simply install the normal build tools for a Java project (i.e. maven and jdk) and then simply do:

    mvn clean package

### Example usage
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
-------

## Platfora
### Example usage
Once installed you will see two new functions that can be used in computed fields:

To get all fields from the analysis as a Json:

    ANALYZE_USERAGENT_JSON( useragent )

To get a single specific field from the analysis:

    ANALYZE_USERAGENT( useragent , "AgentName" )

Note that due to caching in the parser itself the performance of the latter is expected (not tested yet) to be slightly faster.

### Building
In order to build the Platfora UDF the platfora-udf.jar is needed that is currently only distributed by Platfora as
a file that is part of their installation. This file is normally installed at ${PLATFORA_HOME}/tools/udf/platfora-udf.jar

So to build this UDF you need the platfora-udf.jar in a place where maven can find it.

At the time of writing we were running Platfora 5.2.0 so we chose these values to deploy it as:

    group:    com.platfora.udf
    artifact: platfora-udf
    version:  5.2.0-LOCAL

By deliberately appending 'LOCAL' to the version we aim to avoid naming conflicts in case Platfora decides to put
this jar into a public repo like Maven central.

Installing it locally on your development system can be simply done like this:
( See https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html )

    mvn install:install-file \
        -Dfile=platfora-udf.jar \
        -DgroupId=com.platfora.udf \
        -DartifactId=platfora-udf \
        -Dversion=5.2.0-LOCAL \
        -Dpackaging=jar

Installing it in your corporate maven repo will make things a lot easier for all developers:
https://maven.apache.org/guides/mini/guide-3rd-party-jars-remote.html

Simply install the normal build tools for a Java project (i.e. maven and jdk) and then simply do:

    mvn clean package -Pplatfora

The ./target/yauaa-<version>-udf.jar file will now include the both the Apache Pig and Platfora udfs and also
the file udf-classes.txt to aid in installing it in Platfora.

### Installing
See http://documentation.platfora.com/webdocs/#reference/expression_language/udf/install_udf_class.html

So your IT operations needs a list of all classes that must be registered with Platfora as a UDF
As part of the build a file called udf-classes.txt is generated that contains these classnames.

So with even multiple UDFs installed (that follow this pattern!!) your IT-operations can now do:

    CLASS_LIST=$(
        for UDF_JAR in ${PLATFORA_DATA_DIR}/extlib/*jar ; \
        do \
            unzip -p ${UDF_JAR} udf-classes.txt ;\
        done | sort -u | \
        while read class ; do echo -n "${class}," ; done | sed 's/,$//'
        )

    ${PLATFORA_HOME}/bin/platfora-config set --key platfora.udf.class.names --value ${CLASS_LIST}

    ${PLATFORA_HOME}/bin/platfora-services restart


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
