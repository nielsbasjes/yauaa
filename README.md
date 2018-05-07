Yauaa: Yet Another UserAgent Analyzer
========================================
[![Travis Build status](https://api.travis-ci.org/nielsbasjes/yauaa.png?branch=master)](https://travis-ci.org/nielsbasjes/yauaa)
[![Coverage Status](https://coveralls.io/repos/github/nielsbasjes/yauaa/badge.svg?branch=master)](https://coveralls.io/github/nielsbasjes/yauaa?branch=master)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://img.shields.io/maven-central/v/nl.basjes.parse.useragent/yauaa-parent.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22nl.basjes.parse.useragent%22)
[![If this project has business value for you then don't hesitate to support me with a small donation.](https://img.shields.io/badge/Donations-via%20Paypal-blue.svg)](https://www.paypal.me/nielsbasjes)

This is a java library that tries to parse and analyze the useragent string and extract as many relevant attributes as possible.

A bit more background about this useragent parser can be found in this blog which I wrote about it: [https://techlab.bol.com/making-sense-user-agent-string/](https://partnerprogramma.bol.com/click/click?p=1&t=url&s=2171&f=TXL&url=https%3A%2F%2Ftechlab.bol.com%2Fmaking-sense-user-agent-string%2F&name=yauaa)

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
|  **Device**Name                       | 'Google Nexus 6'       |
|  **Device**Brand                      | 'Google'               |
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

Limitations
===========
This system is based on analyzing the useragent string and looking for the patterns in the useragent string as they have been defined by parties like Google, Microsoft, Samsung and many others. These have been augmented with observations how developers apparently do things. There are really no (ok, very limited) lookup tables that define if a certain device name is a Phone or a Tablet. This makes this system very maintainable because there is no need to have a list of all possible devices.

As a consequence if a useragent does not follow these patterns the analysis will yield the 'wrong' answer.
Take for example these two (both were found exactly as shown here in the logs of a live website):

    Mozilla/5.0 (Linux; Android 5.1; SAMSUNG-T805s Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.94 Mobile Safari/537.36
    Mozilla/5.0 (Linux; Android 4.4.2; SAMSUNG-T805S Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.89 Safari/537.36

The difference between "Mobile Safari" and "Safari" has been defined for Google Chrome as the difference between "Phone" and "Tablet" (see https://developer.chrome.com/multidevice/user-agent ).

And as you can see in this example: we sometimes get it wrong.
The impact in this case is however very limited: Of the 445 visitors I found using this device only 2 were classified wrong all others were correct.

A second example is when the Samsung Browser is installed on a non-Samsung device (in this example a Google Nexus 6):

    Mozilla/5.0 (Linux; Android 7.0; SAMSUNG Nexus 6 Build/NBD92G) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/5.4 Chrome/51.0.2704.106 Mobile Safari/537.36

As you can see this browser assumes it is only installed on Samsung devices so they 'force' the word Samsung in there.
In this case you will see this being reported as a "Samsung Nexus 6", which is obviously wrong.

Performance
===========
On my i7 system I see a speed ranging from 500 to 4000 useragents per second (depending on the length and ambiguities in the useragent).
On average the speed is around 2000 per second or ~0.5ms each.
A LRU cache is in place that does over 1M per second if they are in the cache.

Please note that the current system take approx 256MiB of RAM just for the engine (without any caching!!).

Output from the benchmark ( [using this code](benchmarks/src/main/java/nl/basjes/parse/useragent/benchmarks/AnalyzerBenchmarks.java) ) on a Intel(R) Core(TM) i7-6820HQ CPU @ 2.70GHz:

| Benchmark                                 | Mode | Cnt | Score |   | Error | Units |
| ---                                       | ---  | --- | ---  | --- | ---  | ---   |
| AnalyzerBenchmarks.android6Chrome46       | avgt |  10 | 0.561 | ± | 0.011 | ms/op |
| AnalyzerBenchmarks.androidPhone           | avgt |  10 | 0.726 | ± | 0.010 | ms/op |
| AnalyzerBenchmarks.googleAdsBot           | avgt |  10 | 0.120 | ± | 0.002 | ms/op |
| AnalyzerBenchmarks.googleAdsBotMobile     | avgt |  10 | 0.378 | ± | 0.001 | ms/op |
| AnalyzerBenchmarks.googleBotMobileAndroid | avgt |  10 | 0.616 | ± | 0.006 | ms/op |
| AnalyzerBenchmarks.googlebot              | avgt |  10 | 0.197 | ± | 0.007 | ms/op |
| AnalyzerBenchmarks.hackerSQL              | avgt |  10 | 0.093 | ± | 0.004 | ms/op |
| AnalyzerBenchmarks.hackerShellShock       | avgt |  10 | 0.069 | ± | 0.003 | ms/op |
| AnalyzerBenchmarks.iPad                   | avgt |  10 | 0.339 | ± | 0.003 | ms/op |
| AnalyzerBenchmarks.iPhone                 | avgt |  10 | 0.343 | ± | 0.003 | ms/op |
| AnalyzerBenchmarks.iPhoneFacebookApp      | avgt |  10 | 0.717 | ± | 0.004 | ms/op |
| AnalyzerBenchmarks.win10Chrome51          | avgt |  10 | 0.290 | ± | 0.010 | ms/op |
| AnalyzerBenchmarks.win10Edge13            | avgt |  10 | 0.328 | ± | 0.003 | ms/op |
| AnalyzerBenchmarks.win10IE11              | avgt |  10 | 0.334 | ± | 0.006 | ms/op |
| AnalyzerBenchmarks.win7ie11               | avgt |  10 | 0.329 | ± | 0.006 | ms/op |


In the canonical usecase of analysing clickstream data you will see a <1ms hit per visitor (or better: per new non-cached useragent)
and for all the other clicks the values are retrieved from this cache at a speed of < 1 microsecond (i.e. close to 0).

Using the analyzer
==================
In addition to the UDFs for many of Apache tools (Pig, Hive, Flink, Beam, ... see below) this analyzer
can also directly be used in Java based applications.

First add the library as a dependency to your application.
This has been published to maven central so that should work in almost any environment.

    <dependency>
      <groupId>nl.basjes.parse.useragent</groupId>
      <artifactId>yauaa</artifactId>
      <version>4.3</version>
    </dependency>

and in your application you can use it as simple as this

        UserAgentAnalyzer uaa = UserAgentAnalyzer
                    .newBuilder()
                    .hideMatcherLoadStats()
                    .withCache(25000)
                    .build();

        UserAgent agent = uaa.parse("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.11) Gecko/20071127 Firefox/2.0.0.11");

        for (String fieldName: agent.getAvailableFieldNamesSorted()) {
            System.out.println(fieldName + " = " + agent.getValue(fieldName));
        }

Please instantiate a new UserAgentAnalyzer as few times as possible because the initialization step for a full UserAgentAnalyzer (i.e. all fields) usually takes something in the range of 2-5 seconds.

Note that not all fields are available after every parse. So be prepared to receive a 'null' if you extract a specific name.

**IMPORTANT: This library is single threaded !**
Because the code is not reentrant the main method has been synchronized on the instance. 
So if you are in a multi threaded situation you should create a separate instance per thread or accept the speed limitation.
Note that you should really instantiate it only once per thread (and use a ThreadPool or something similar) because starting a new instance takes several seconds.

**Eclipse users**: Be aware of there is a bug in Eclipse which will show you errors in perfectly valid Java code: 
https://bugs.eclipse.org/bugs/show_bug.cgi?id=527475 
The errors you see are related to the inheritance model used by the Builders in this project and the fact that Eclipse does not interpret it correctly. 

# Limiting to only certain fields
In some scenarios you only want a specific field and all others are unwanted.
This can be achieved by creating the analyzer in Java like this:

    UserAgentAnalyzer uaa;

    uaa = UserAgentAnalyzer
            .newBuilder()
            .withField("DeviceClass")
            .withField("AgentNameVersionMajor")
            .build();

One important effect is that this speeds up the system because it will kick any rules that do not help in getting the desired fields.
The above example showed an approximate 40% speed increase (i.e. times dropped from ~1ms to ~0.6ms).

# Memory usage
The system relies heavily on HashMaps to quickly find the rules that need to be fired.

Some fields only require a handful of rules where others have a lot of them.
This means that it depends on the fields that have been requested how many rules are kept in the system and
thus how much memory is used to store the rules in.
To get an idea of the relative memory impact of the rules needed for a specific field.

This table was constructed by running all testcases against the engine where we only request 1 field.
Then after forcing a GC in the JVM we retrieve the memory footprint.
Because there are no rules for the field `__SyntaxError__` we assumed that to be the baseline
against which we determine the relative memory usage.

Because most rules determine several fields there is a lot of overlap in the rules used.
If you keep all rules we see that version 3.2 uses about 224 MiB of memory for all rules which shows that
the most expensive rules related to finding the DeviceName and DeviceBrand because both need
to determine the brand of the device at hand.

| Field | Relative Memory usage  |
| :--- | ---: |
| DeviceClass                     |  74.6 MiB |
| DeviceName                      | 193.8 MiB |
| DeviceBrand                     | 179.0 MiB |
| DeviceCpu                       |   3.2 MiB |
| DeviceCpuBits                   |   2.4 MiB |
| DeviceFirmwareVersion           |   4.1 MiB |
| DeviceVersion                   |  12.5 MiB |
| OperatingSystemClass            |  50.5 MiB |
| OperatingSystemName             |  49.5 MiB |
| OperatingSystemVersion          |  49.5 MiB |
| OperatingSystemNameVersion      |  49.7 MiB |
| OperatingSystemVersionBuild     |   2.2 MiB |
| LayoutEngineClass               |   9.0 MiB |
| LayoutEngineName                |   9.0 MiB |
| LayoutEngineVersion             |   9.0 MiB |
| LayoutEngineVersionMajor        |   9.0 MiB |
| LayoutEngineNameVersion         |   9.0 MiB |
| LayoutEngineNameVersionMajor    |   9.0 MiB |
| LayoutEngineBuild               |   1.5 MiB |
| AgentClass                      |  15.9 MiB |
| AgentName                       |  15.6 MiB |
| AgentVersion                    |  15.6 MiB |
| AgentVersionMajor               |  15.6 MiB |
| AgentNameVersion                |  15.7 MiB |
| AgentNameVersionMajor           |  15.7 MiB |
| AgentBuild                      |   0.5 MiB |
| AgentLanguage                   |   0.3 MiB |
| AgentLanguageCode               |   0.3 MiB |
| AgentInformationEmail           |   4.1 MiB |
| AgentInformationUrl             |   5.9 MiB |
| AgentSecurity                   |   0.3 MiB |
| AgentUuid                       |   0.3 MiB |
| FacebookCarrier                 |   0.2 MiB |
| FacebookDeviceClass             |   0.5 MiB |
| FacebookDeviceName              |   0.5 MiB |
| FacebookDeviceVersion           |   0.5 MiB |
| FacebookFBOP                    |   0.2 MiB |
| FacebookFBSS                    |   0.5 MiB |
| FacebookOperatingSystemName     |   0.5 MiB |
| FacebookOperatingSystemVersion  |   0.5 MiB |
| Anonymized                      |   1.2 MiB |
| HackerAttackVector              |   0.2 MiB |
| HackerToolkit                   |   0.2 MiB |
| KoboAffiliate                   |   0.0 MiB |
| KoboPlatformId                  |   0.0 MiB |
| IECompatibilityVersion          |   1.6 MiB |
| IECompatibilityVersionMajor     |   1.6 MiB |
| IECompatibilityNameVersion      |   1.6 MiB |
| IECompatibilityNameVersionMajor |   1.6 MiB |
| Carrier                         |   0.2 MiB |
| GSAInstallationID               |   0.0 MiB |
| WebviewAppName                  |   1.9 MiB |
| WebviewAppNameVersionMajor      |   2.0 MiB |
| WebviewAppVersion               |   1.9 MiB |
| WebviewAppVersionMajor          |   1.9 MiB |



# User Defined Functions
Several external computation systems support the concept of a User Defined Function (UDF).
A UDF is simply a way of making functionality (in this case the analysis of useragents)
available in such a system.

For several systems (tools used within
[bol.com](https://partnerprogramma.bol.com/click/click?p=1&t=url&s=2483&f=TXL&url=http%3A%2F%2Fwww.bol.com%2F&name=yauaa) (where I work))
I have written such a UDF which are all part of this project.

* [Apache Pig](README-Pig.md)
* [Apache Flink](README-Flink.md)
* [Apache Beam](README-Beam.md)
* [Apache Hive](README-Hive.md)
* [Commandline tool](README-Commandline.md)
* [Apache Drill](README-Drill.md) which was originally written by [Charles S. Grive](https://github.com/cgivre/drill-useragent-function)
* [Apache Nifi](README-Nifi.md)

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
| Desktop      | The type of OS you would run on a Desktop or Laptop |
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
| Email Client      | This is an email application that did the request |
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
- for "compatibility" the AppleWebKit lie about being "KHTML" and that it is similar to "Gecko" are also copied
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

A much more in depth explanation can be found in the documentation on [how to create new rules](MAKING_NEW_RULES.md)

-------

Donations
===
If this project has business value for you then don't hesitate to support me with a small donation.

[![Donations via PayPal](https://img.shields.io/badge/Donations-via%20Paypal-blue.svg)](https://www.paypal.me/nielsbasjes)

License
=======
    Yet Another UserAgent Analyzer
    Copyright (C) 2013-2018 Niels Basjes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
