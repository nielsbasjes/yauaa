Yauaa: Yet Another UserAgent Analyzer
========================================
[![Travis Build status](https://api.travis-ci.org/nielsbasjes/yauaa.png)](https://travis-ci.org/nielsbasjes/yauaa) [![Coverage Status](https://coveralls.io/repos/github/nielsbasjes/yauaa/badge.svg?branch=master)](https://coveralls.io/github/nielsbasjes/yauaa?branch=master) [![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html) [![If this project has business value for you then don't hesitate to support me with a small donation.](https://img.shields.io/badge/Donations-via%20Paypal-blue.svg)](https://www.paypal.me/nielsbasjes)

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

    Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36

is converted into this set of fields:

| Field name | Value |
| --- | --- |
|  **Device**Class                      | 'Phone'                |
|  **Device**Name                       | 'Nexus 6'              |
|  **OperatingSystem**Class             | 'Mobile'               |
|  **OperatingSystem**Name              | 'Android'              |
|  **OperatingSystem**Version           | '7.0'                  |
|  **OperatingSystem**NameVersion       | 'Android 7.0'          |
|  **OperatingSystem**VersionBuild      | 'NBD90Z'               |
|  **LayoutEngine**Class                | 'Browser'              |
|  **LayoutEngine**Name                 | 'Blink'                |
|  **LayoutEngine**Version              | '53.0'                 |
|  **LayoutEngine**VersionMajor         | '53'                   |
|  **LayoutEngine**NameVersion          | 'Blink 53.0'           |
|  **LayoutEngine**NameVersionMajor     | 'Blink 53'             |
|  **Agent**Class                       | 'Browser'              |
|  **Agent**Name                        | 'Chrome'               |
|  **Agent**Version                     | '53.0.2785.124'        |
|  **Agent**VersionMajor                | '53'                   |
|  **Agent**NameVersion                 | 'Chrome 53.0.2785.124' |
|  **Agent**NameVersionMajor            | 'Chrome 53'            |

You can try it online with your own browser here: [https://analyze-useragent.appspot.com/](https://analyze-useragent.appspot.com/).

**NOTES**

1. This runs under a "Free quota" on Google AppEngine. If this quote is exceeded then it will simply become unavailable for that day.
2. After a while of inactivity the instance is terminated so the first page may take 15-30 seconds to load.
3. If you really like this then run it on your local systems. It's much faster that way.

Performance
===========
On my i7 system I see a speed ranging from 500 to 4000 useragents per second (depending on the length and ambiguities in the useragent).
On average the speed is around 2000 per second or ~0.5ms each.
A LRU cache is in place that does over 1M per second if they are in the cache.

Output from the benchmark ( [using this code](benchmarks/src/main/java/nl/basjes/parse/useragent/benchmarks/AnalyzerBenchmarks.java) ) on a Intel(R) Core(TM) i7-6820HQ CPU @ 2.70GHz:

| Benchmark                                 | Mode | Cnt | Score |   | Error | Units |
| ---                                       | ---  | --- | ---  | --- | ---  | ---   |
| AnalyzerBenchmarks.android6Chrome46       | avgt |  10 | 0.538 | ± | 0.021 | ms/op |
| AnalyzerBenchmarks.androidPhone           | avgt |  10 | 0.688 | ± | 0.025 | ms/op |
| AnalyzerBenchmarks.googleAdsBot           | avgt |  10 | 0.111 | ± | 0.002 | ms/op |
| AnalyzerBenchmarks.googleAdsBotMobile     | avgt |  10 | 0.385 | ± | 0.021 | ms/op |
| AnalyzerBenchmarks.googleBotMobileAndroid | avgt |  10 | 0.575 | ± | 0.021 | ms/op |
| AnalyzerBenchmarks.googlebot              | avgt |  10 | 0.199 | ± | 0.004 | ms/op |
| AnalyzerBenchmarks.hackerSQL              | avgt |  10 | 0.096 | ± | 0.003 | ms/op |
| AnalyzerBenchmarks.hackerShellShock       | avgt |  10 | 0.084 | ± | 0.002 | ms/op |
| AnalyzerBenchmarks.iPad                   | avgt |  10 | 0.344 | ± | 0.011 | ms/op |
| AnalyzerBenchmarks.iPhone                 | avgt |  10 | 0.341 | ± | 0.006 | ms/op |
| AnalyzerBenchmarks.iPhoneFacebookApp      | avgt |  10 | 0.695 | ± | 0.026 | ms/op |
| AnalyzerBenchmarks.win10Chrome51          | avgt |  10 | 0.307 | ± | 0.011 | ms/op |
| AnalyzerBenchmarks.win10Edge13            | avgt |  10 | 0.336 | ± | 0.013 | ms/op |
| AnalyzerBenchmarks.win10IE11              | avgt |  10 | 0.282 | ± | 0.008 | ms/op |
| AnalyzerBenchmarks.win7ie11               | avgt |  10 | 0.279 | ± | 0.007 | ms/op |

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
      <version>1.1</version>
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

# Limiting to only certain fields
In some scenarios you only want a specific field and all others are unwanted.
This can be achieved by creating the analyzer in Java like this:

    UserAgentAnalyzer uaa;
    public ThreadState() {
        uaa = UserAgentAnalyzer
                .newBuilder()
                .withoutCache()
                .withField("DeviceClass")
                .withField("AgentNameVersionMajor")
                .build();

One important effect is that this speeds up the system because it will kick any rules that do not help in getting the desired fields.
The above example showed an approximate 40% speed increase (i.e. times dropped from ~1ms to ~0.6ms).

# User Defined Functions
Several external computation systems support the concept of a User Defined Function (UDF).
A UDF is simply a way of making functionality (in this case the analysis of useragents)
available in such a system.

For two such systems Apache Pig and Platfora (both are used within bol.com (where I work)) we have written such
a UDF which are both part of this project.

* [Apache Pig](README-Pig.md)
* [Platfora](README-Platfora.md)

UDFs written by other people:

* Apache Drill [https://github.com/cgivre/drill-useragent-function](https://github.com/cgivre/drill-useragent-function)


Values explained
================

DeviceClass
-----------

| Value | Meaning |
| --- | --- |
| Desktop               | The device is assessed as a Desktop/Laptop class device |
| Anonymized            | In some cases the useragent has been altered by anonimization software |
| Unknown               | We really don't know, these are usually useragents that look normal yet contain almost no information about the device |
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

OperatingSystemClass 
-----------

| Value | Meaning |
| --- | --- |
| Desktop      | The type of OS you would run on a dekstop or laptop |
| Mobile       | The type of OS you would run on a Phone, Tablet or Watch |
| Cloud        | Looks like a thing that runs in a cloud environment |
| Embedded     | Apparently embedded into something like a TV |
| Game Console | A game console like PS4, Xbox |
| Hacker       | A hacker, so it can really be anything. |
| Anonymized   | It was explicitly hidden |
| Unknown      | We don't know |

LayoutEngineClass
-----------

| Value | Meaning |
| --- | --- |
| Browser    | A regular browser |
| Mobile App | A mobile app which probably includes a regular webbrowser |
| Hacker     | A hacker, so it can really be anything. |
| Robot      | A robot spidering the site |
| Unknown    | We don't know |
             
AgentClass
-----------

| Value | Meaning |
| --- | --- |
| Browser           | A regular browser |
| Browser Webview   | A regular browser being used as part of a mobile app |
| Mobile App        | A mobile app |
| Robot             | A robot that wants to be treated as a desktop device |
| Robot Mobile      | A robot that wants to be treated as a mobile device |
| Cloud Application | Something running in a cloud (but not a regular robot) |
| Special           | Something special we cannot fully classify |
| Testclient        | A website testing tool |
| Hacker            | A hacker, so it can really be anything. |
| Unknown           | We don't know |

AgentSecurity
-----------

| Value | Meaning |
| --- | --- |
| Weak security   | Indicated to use deliberately weakened encryption (usually due to export restrictions or local laws). |
| Strong security | Indicated to use strong (normal) encryption. |
| Unknown         | It was not specified (very common) |
| Hacker          | A hacker, so it can really be anything. |

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
In all other cases we have matchers that are triggered if a specific value is found by the parser.
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

Donations
===
If this project has business value for you then don't hesitate to support me with a small donation.

[![Donations via PayPal](https://img.shields.io/badge/Donations-via%20Paypal-blue.svg)](https://www.paypal.me/nielsbasjes)

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
