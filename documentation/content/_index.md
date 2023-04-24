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

### Version v7.19.1
- New/improved detections:
    - Updated language tag detection to pickup more language tags.
    - Detect the Google Generic Crawler, Google StoreBot.

### Version v7.19.0
- Analyzer:
    - Automatic switch to the Java 8 compatible caching implementation (Multi Release Jar)

### Version v7.18.0
- UDFs
  - **Dropping support for logstash.** More than 3 years after GA they have not yet published the needed dependencies. https://github.com/elastic/logstash/issues/11002

### Version v7.17.0
- Analyzer:
  - Use all testcases (including ClientHints) when doing preheat.

### Version 7.16.0
- Build:
  - Updated docker based environment to Ubuntu 22.04 LTS
  - The Quarkus example can now also be built into a native executable
  - Workaround Multi Release jar and maven-shade-plugin with relocation.

---
## Regarding the recent Log4J2 issues
The Yauaa analyzer uses the Log4J2 API to do the logging.

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
    Copyright (C) 2013-2023 Niels Basjes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
