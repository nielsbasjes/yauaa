+++
title = "Yauaa: Yet Another UserAgent Analyzer"
linkTitle = "Yauaa"
+++
# Yauaa: Yet Another UserAgent Analyzer
This is a java library that tries to parse and analyze the useragent string and extract as many relevant attributes as possible.

Works with Java, Scala, Kotlin and provides ready for use UDFs for several processing systems.

The full documentation can be found here [https://yauaa.basjes.nl](https://yauaa.basjes.nl)

---
## HIGH Profile release notes:

### Version 6.12 (Unreleased)
- New UDF for ElasticSearch 8 besides the ES 7 variant because of breaking change in ES 7->8

### Version 6.11
- Handle the Edge 99+ edge://flags/#force-major-version-to-minor
- Report frozen `Windows 10` on Chrome/Edge/... 92+ as `Windows >=10`

### Version 6.10
- New UDF for [Trino](https://trino.io/) (a.k.a. Presto SQL)
  - See [https://yauaa.basjes.nl/udf/trino/](https://yauaa.basjes.nl/udf/trino/) for usage information.

### Version 6.9
- Report frozen `Windows 10` on Firefox 88+ as `Windows >=10`
- Detect several types of TVs and Set-top boxes better. Contributed by [Sam Hendley](https://github.com/samhendley/).

### Version 6.8
- The list of returned fields is more consistent (including the "Default" values).
- Fix detection of Samsung SC-... devices.
- Switched the default caching implementation to [Caffeine](https://github.com/ben-manes/caffeine)
  NOTE: Caffeine needs Java 11. If you still need to run on Java 8 you can change the caching implementation used back to any other implementation.

### Version 6.7
- Updated log4j to 2.17.1
- Allow providing a custom caching implementation.
- Builds with JDK 17

### Version 6.3
- Report frozen (=manipulated) Mac OS X versions as `??`:
  - Always [10_15_7](https://bugs.chromium.org/p/chromium/issues/detail?id=1175225) since Chrome 90.
  - Always [10.15](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/User-Agent/Firefox#macintosh) since Firefox 87.

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
To assist in running Yauaa without the logj4-core jar an example was created that only uses SLF4J: [here](https://github.com/nielsbasjes/yauaa/tree/main/examples/java-slf4j).

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
