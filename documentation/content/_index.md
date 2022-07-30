+++
title = "Yauaa: Yet Another UserAgent Analyzer"
linkTitle = "Yauaa"
+++
# Yauaa: Yet Another UserAgent Analyzer
This is a java library that tries to parse and analyze the useragent string (and when available the User-Agent Client Hints) and extract as many relevant attributes as possible.

Works with Java, Scala, Kotlin and provides ready for use UDFs for several processing systems.

The full documentation can be found here [https://yauaa.basjes.nl](https://yauaa.basjes.nl)

---
## HIGH Profile release notes:

### Version 7.4.0 (Unreleased)
- Report frozen `Mac OS` on Safari as `Mac OS >=10.15.7`
- New DeviceClass: "Home Appliance" (Fridges and such)


### Version 7.3.0
- Thanks to https://github.com/FrancoisDevemy5 for providing many of the testcases used for improving this version
- Improve Apple detection/reporting
  - Apple
    - Use information from Apple to complete the list of devices.
    - Apple CFNetwork+Darwin with CPU tag is macOS instead of iOS
    - Apple M1 CPU (hardly ever seen)
    - Apple watchOS
  - Many browsers, security scanners and robots.
  - Improve the naming of all Google bot variants.
- Webservlet:
  - The input UserAgent is now also part of the output to allow easier processing.

### Version 7.1.0
- Analyzer
  - Simplify using Java 8 compliant caching
- New/improved detections
  - Chrome OS/Chromebooks:
    - Handle new "frozen Chrome OS" tag
    - The DeviceBrand of a Chromebook is "Unknown" instead of Google
    - Fixed Version and OS Build tags

### Version 7.0.0
- Dropping support for Apache Pig. The last release was in 2017: about 5 years ago.
- Support for using ClientHints in addition to the User-Agent
- Improve Apple detection/reporting
  - "Mac OS X" / "OS X" / "macOS"
    - The Major Version for the 10.x.x versions is now 2 parts (like '10.6') instead of just '10'.
    - Although the marketing name has changed several times: For all versions `OperatingSystemName = 'Mac OS'` is used to ensure stable reporting.
  - Darwin will be reported as the most likely iOS version instead.

### Main points from previous releases
- Many fixes around Reducing/Freezing the User-Agent by the Chromium and Firefox teams.
  - Handle the Edge 99+ edge://flags/#force-major-version-to-minor
  - Report frozen `Windows 10` on Chrome/Edge/... 92+ as `Windows >=10`
  - Report frozen `Windows 10` on Firefox 88+ as `Windows >=10`
  - Report frozen `Mac OS X` versions as `??`:
    - Always [10_15_7](https://bugs.chromium.org/p/chromium/issues/detail?id=1175225) since Chrome 90.
    - Always [10.15](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/User-Agent/Firefox#macintosh) since Firefox 87.
- Updates because of Log4Shell
- New UDF for [Trino](https://trino.io/) (a.k.a. Presto SQL)
  - See [https://yauaa.basjes.nl/udf/trino/](https://yauaa.basjes.nl/udf/trino/) for usage information.
- New UDF for ElasticSearch 8 besides the ES 7 variant because of breaking change in ES 7->8
- Switched the default caching implementation to [Caffeine](https://github.com/ben-manes/caffeine)

and ofcourse several improved detections
- Detect several types of TVs and Set-top boxes better. Contributed by [Sam Hendley](https://github.com/samhendley/).


---
## Regarding the recent Log4J2 issues
The Yauaa analyzer uses the Log4J2 API to do the logging and through the included dependencies also JCL and SLF4J are needed to run.

TL;DR:
- **The core of Yauaa is safe** as it does not include any logging dependencies and expects the application to provide everything.
- **In normal operations user input is not logged**.
- The **Snowflake UDF is affected** by these problems (due to shading the dependencies in).

### NO batteries included
By design the Yauaa library expects the application in which it is used to provide the actual logging dependencies and configuration.
If you do not provide the needed logging classes it will simply fail at startup.

So by design the Yauaa library expects all of these frameworks to be provided (and configured) and does not include any of them or any configuration for them.

This is true for most of the released artifacts (including the base library) except for the Snowflake UDF which does include almost all dependencies.
So the Snowflake UDF IS affected by this issue and all users are recommended to update.

### Minimal logging
Note that Yauaa does not log any user input and/or analysis results from user input during normal operation.
Only during development and during unit tests the Useragents are logged.

This is because it was designed to run in very large scale batch and streaming situations (very large as in "Let's analyze these 10^10 records").

### Bring your own batteries
To assist in running Yauaa without the logj4-core jar an example was created that only uses SLF4J: [here](https://github.com/nielsbasjes/yauaa/tree/main/analyzer/src/it/Examples/java-slf4j).

---
## Donations
If this project has business value for you then don't hesitate to support me with a small donation.

[![Donations via PayPal](https://img.shields.io/badge/Donations-via%20Paypal-blue.svg)](https://www.paypal.me/nielsbasjes)

---
## License

    Yet Another UserAgent Analyzer
    Copyright (C) 2013-2022 Niels Basjes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
