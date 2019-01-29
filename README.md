Yauaa: Yet Another UserAgent Analyzer
========================================
[![Travis Build status](https://api.travis-ci.org/nielsbasjes/yauaa.png?branch=master)](https://travis-ci.org/nielsbasjes/yauaa)
[![Coverage Status](https://coveralls.io/repos/github/nielsbasjes/yauaa/badge.svg?branch=master)](https://coveralls.io/github/nielsbasjes/yauaa?branch=master)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=nielsbasjes_yauaa&metric=alert_status)](https://sonarcloud.io/dashboard?id=nielsbasjes_yauaa)
[![License](https://img.shields.io/:license-apache-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://img.shields.io/maven-central/v/nl.basjes.parse.useragent/yauaa-parent.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22nl.basjes.parse.useragent%22)
[![If this project has business value for you then don't hesitate to support me with a small donation.](https://img.shields.io/badge/Donations-via%20Paypal-blue.svg)](https://www.paypal.me/nielsbasjes)

This is a java library that tries to parse and analyze the useragent string and extract as many relevant attributes as possible.

The full documentation can be found here [https://yauaa.basjes.nl](https://yauaa.basjes.nl)

HIGH Profile release notes:
===========================

Version 5.8
---
The detection for Microsoft Edge now no longer reports the 'EdgeHtml' version as if it is the version of 'Edge' itself. Microsoft made a mess of things again.

Version 5.6
---
In version 5.6 the number of detected DeviceBrands is greatly increased.
The detection system for the DeviceBrand has been rewritten and as a consequence both the memory usage and the time needed for the analysis have been decreased.

Version 5.5
---
With Google Chrome 70 the useragent string pattern has been changed on Android ( https://www.chromestatus.com/feature/4558585463832576 ) .
As a consequence the detection of the DeviceBrand failed and you always get "Unknown". 
This has been fixed in Yauaa 5.5.

Version 5.1 is bad.
---
If you are using version 5.1 you should upgrade IMMEDIATELY because 5.1 crashes over specific useragent patterns caused by a simple packaging mistake.

    Mozilla/1.2.3 (http://basjes.nl)

Blog post 
=========
A bit more background about this useragent parser can be found in this blog which I wrote about it: [https://techlab.bol.com/making-sense-user-agent-string/](https://partnerprogramma.bol.com/click/click?p=1&t=url&s=2171&f=TXL&url=https%3A%2F%2Ftechlab.bol.com%2Fmaking-sense-user-agent-string%2F&name=yauaa)

Example output
==============
As an example the useragent of my phone:

> Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36

is converted into this set of fields:

| Field name | Value |
| --- | --- |
|  **Device** Class                         | Phone                |
|  **Device** Name                          | Google Nexus 6       |
|  **Device** Brand                         | Google               |
|  **Operating System** Class               | Mobile               |
|  **Operating System** Name                | Android              |
|  **Operating System** Version             | 7.0                  |
|  **Operating System** Name Version        | Android 7.0          |
|  **Operating System** Version Build       | NBD90Z               |
|  **Layout Engine** Class                  | Browser              |
|  **Layout Engine** Name                   | Blink                |
|  **Layout Engine** Version                | 53.0                 |
|  **Layout Engine** Version Major          | 53                   |
|  **Layout Engine** Name Version           | Blink 53.0           |
|  **Layout Engine** Name Version Major     | Blink 53             |
|  **Agent** Class                          | Browser              |
|  **Agent** Name                           | Chrome               |
|  **Agent** Version                        | 53.0.2785.124        |
|  **Agent** Version Major                  | 53                   |
|  **Agent** Name Version                   | Chrome 53.0.2785.124 |
|  **Agent** Name Version Major             | Chrome 53            |

Try it!
=======
You can try it online with your own browser here: [https://try.yauaa.basjes.nl/](https://try.yauaa.basjes.nl/).

**NOTES**

1. This runs under a "Free quota" on Google AppEngine. If this quota is exceeded then it will simply become unavailable for that day.
2. After a while of inactivity the instance is terminated so the first page may take 15-30 seconds to load.
3. If you really like this then run it on your local systems. It's much faster that way.

Related projects
===
[Stefano Balzarotti](https://github.com/OrbintSoft) is putting a lot of effort into porting Yauaa to run in .NET standard.

You can track his efforts here on Github: [Yauaa .NET standard](https://github.com/OrbintSoft/yauaa.netstandard) and
download his releases via [Nuget](https://www.nuget.org/packages/OrbintSoft.Yauaa.NetStandard).

Donations
===
If this project has business value for you then don't hesitate to support me with a small donation.

[![Donations via PayPal](https://img.shields.io/badge/Donations-via%20Paypal-blue.svg)](https://www.paypal.me/nielsbasjes)

License
=======

    Yet Another UserAgent Analyzer
    Copyright (C) 2013-2019 Niels Basjes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
