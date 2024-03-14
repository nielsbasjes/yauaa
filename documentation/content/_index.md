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

These are only the highlights for the last few releases, the full changelog can be found [here](https://github.com/nielsbasjes/yauaa/blob/main/CHANGELOG.md).

### NEXT RELEASE
- New/improved detections:
  - ...

### Version v7.26.0
- Analyzer:
  - Improve handling of invalid ClientHints
- New/improved detections:
  - Ecosia browser
  - Phoenix browser (has very bad ClientHints)
  - Lookup Amazon device tags from ClientHints
  - Report the agent version 'NULL' as '??'
  - Handle Edge case of unknown device names
- Updated UDF dependencies
    - Trino 439 changed their API

### Version v7.25.0
- New/improved detections:
  - Mitigate the invalid ClientHints given by Opera GX 2.1

### Version v7.24.0
- Analyzer:
    - Allow cloning an instance and share the configuration (reducing memory footprint)
- New/improved detections:
    - CamScanner is not a robot
    - NextCloud app
- Build:
    - Update commons-text required shading to make it work.
    - Build under Java 21 and also run all tests with Java 21

---
## Donations
If this project has business value for you then don't hesitate to support me with a small donation either via [Github Sponsors](https://github.com/sponsors/nielsbasjes) or [Paypal](https://www.paypal.me/nielsbasjes).

---
## License

    Yet Another UserAgent Analyzer
    Copyright (C) 2013-2024 Niels Basjes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
