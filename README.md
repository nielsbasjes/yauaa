YaUAA: Yet Another UserAgent Analyzer [![Travis Build status](https://api.travis-ci.org/nielsbasjes/yauaa.png)](https://travis-ci.org/nielsbasjes/yauaa)
====================================
This is a java library that tries to parse and analyze the useragent string and extract as many relevant attributes as possible.

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

Note that **not all fields are always available**. So if you look at a specific field you will in general find null values and "Unknown" in there as well.

There are as little as possible lookup tables included the system really tries to analyze the useragent and extract values from it.
The aim of this approach is to have a system that can classify as much traffic as possible yet require as little as possible maintenance
because all versions and in many places also the names of the used components are extracted without knowing them beforehand.

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

Performance
===========
On my i7 system I see a speed ranging from 500 to 4000 useragents per second (depending on the length and ambiguities in the useragent).
On average the speed is above 1000 per second or <1ms each.
A LRU cache is in place that does over 1M per second if they are in the cache.

In the canonical usecase of analysing clickstream data you will see a <1ms hit per visitor (or better: per new non-cached useragent)
and for all the other clicks the values are retrieved from this cache at close to 0 time.

Using the analyzer
==================
In addition to the UDFs for Apache Pig and Platfora (see below) this analyzer
can also be used in Java based applications.

First add the library as a dependency to your application.
This has been published to maven central so that should work in almost any environment.

    <dependency>
      <groupId>nl.basjes.parse.useragent</groupId>
      <artifactId>yauaa</artifactId>
      <version>0.9</version>
    </dependency>

and in your application you can use it as simple as this

        UserAgentAnalyzer uaa = new UserAgentAnalyzer();

        UserAgent agent = uaa.parse("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.11) Gecko/20071127 Firefox/2.0.0.11");

        for (String fieldName: agent.getAvailableFieldNamesSorted()) {
            System.out.println(fieldName + " = " + agent.getValue(fieldName));
        }

Note that not all fields are available after every parse. So be prepared to receive a 'null' if you extract a specific name.

**IMPORTANT: This library is NOT threadsafe/reentrant!**
So if you need it in a multi threaded situation you either need to synchronize using it or create a separate instance per thread.

# User Defined Functions
Several external computation systems support the concept of a User Defined Function (UDF).
A UDF is simply a way of making functionality (in this case the analysis of useragents)
available in such a system.

For two such systems Apache Pig and Platfora (both are used within bol.com (where I work)) we have written such
a UDF which are both part of this project.

* [Apache Pig](README-Pig.md)
* [Platfora](README-Platfora.md)


Values explained
================

DeviceClass
-----------

| Value | Meaning |
| --- | --- |
| Desktop               | The device is assessed as a Desktop/Laptop class device |
| Anonymized            | In some cases the useragent has been altered by anonimization software |
| Mobile                | A device that is mobile yet we do not know if it is a eReader/Tablet/Phone or Watch |
| Tablet                | A mobile device with a rather large screen (common > 7") |
| Phone                 | A mobile device with a small screen (common < 7") |
| Watch                 | A mobile device with a tiny screen (common < 2"). Normally these are an additional screen for a phone/tablet type device. |
| Virtual Reality       | A mobile device with a VR capabilities |
| eReader               | Similar to a Tablet yet in most cases with an eInk screen |
| Set-top box           | A connected device that allows interacting via a TV sized screen |
| TV                    | Similar to Set-top box yet here this is built into the TV |
| Game Console          | 'Fixed' game systems like the PlayStation and XBox |
| Handheld Game Console | 'Mobile' game systems like the 3DS |
| Robot                 | Robots that visit the site |
| Robot Mobile          | Robots that visit the site indicating they want to be seen as a Mobile visitor |
| Spy                   | Robots that visit the site pretending they are robots like google, but they are not |
| Hacker                | In case scripting is detected in the useragent string, also fallback in really broken situations |
| Unknown               | We really don't know |

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

-------


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
