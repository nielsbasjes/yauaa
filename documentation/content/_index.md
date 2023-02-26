+++
title = "Yauaa: Yet Another UserAgent Analyzer"
linkTitle = "Yauaa"
+++
# Yauaa: Yet Another UserAgent Analyzer
This is a java library that tries to parse and analyze the useragent string (and when available the User-Agent Client Hints) and extract as many relevant attributes as possible.

Works with Java, Scala, Kotlin and provides ready for use UDFs for several processing systems.

The full documentation can be found here [https://yauaa.basjes.nl](https://yauaa.basjes.nl)

---

## ALL USERS OF THE CLIENTHINTS ANALYSIS FEATURE ARE URGED TO UPDATE TO 7.9.0
In a specific case of bad data in the Client Hints yauaa 7.0.0-7.8.0 will throw an ArrayIndexOutOfBoundsException.

See [Security Advisory: CVE-2022-23496](https://github.com/nielsbasjes/yauaa/security/advisories/GHSA-c4pm-63cg-9j7h)

## HIGH Profile release notes:

### Version 7.14.0
- New/improved detections
  - Alamofire/macOS Catalyst, Alamofire is NOT a LayoutEngine
- Build:
  - Pubish SBOM
  - Reproducible builds
- Analyzer
  - Workaround for resource loading problems works with dropTests

### Version 7.13.0
- Analyzer:
  - Adding VFS support back in.
- New/improved detections
  - Extra noise filter of unwanted extra fields
  - Handle a new form of iOS apps.

### Version 7.12.2
- Build
  - Reproducible builds
- New/improved detections
  - Extra noise filter of unwanted extra fields

### Version 7.12.1
- Analyzer:
  - Include the caffeine dependencies fix https://github.com/ben-manes/caffeine/discussions/867

### Version 7.12.0
- Analyzer:
    - Manually shaded in
      - httpcomponents-client v5.2.1.
        - One less logging dependency (no more SLF4j needed).
        - Fixes OSGI problems https://github.com/apache/unomi/pull/557
      - spring-core
        - One less logging dependency (no more JCL needed).
        - Dropping VFS support
- New/improved detections
    - NULL version is now assumes to be caused by broken plugins instead of Robot.

### Version 7.11.0
- New/improved detections
    - RancherDesktop client
    - Chrome/Edge 109 with Client Hints were reported as "Not_A Brand 99"

### Version 7.10.0
- Analyzer:
    - Fix Java8 caching and cache serialization.
- New/improved detections
    - MicrosoftPreview bot
    - Few Bingbot imitators
    - Handle frozen "rv:109.0" in Firefox

### Version 7.9.1
- New/improved detections
  - Bad secondary version is now assumes to be Anti fingerprinting instead of Robot.

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
