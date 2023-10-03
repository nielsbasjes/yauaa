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

These are only the highlights, the full changelog can be found [here](https://github.com/nielsbasjes/yauaa/blob/main/CHANGELOG.md).

### NEXT RELEASE
- New/improved detections:
    - Detect more of the Yahoo Japan bots
- Updated UDF dependencies
    - Trino 427 changed their API

### Version v7.22.0
- New/improved detections:
    - Handle version bug fixed in Opera 98.
    - Report all Opera variants as distinct browsers because they all have unrelated version numbers.

### Version v7.21.0
- New/improved detections:
    - New DeviceClass: "Smart Display"
    - Report Samsung DEX (Desktop Experience) as Tablet
    - Detect Amazon Echo Show better (a "Smart Display")
    - Detect Bitwarden Mobile app
    - Detect more Federated Social Servers and Robots
    - Detect Opera GX
    - Detect Fuchsia OS
    - Detect several situations of UserAgent changing plugins
- Updated UDF dependencies
    - Trino 422 (it needs 421 or newer!)

### Version v7.20.0
- New/improved detections:
  - Device brands from patterns in Client Hints.

---
## Donations
If this project has business value for you then don't hesitate to support me with a small donation either via [Github Sponsors](https://github.com/sponsors/nielsbasjes) or [Paypal](https://www.paypal.me/nielsbasjes).

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
