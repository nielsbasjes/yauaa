+++
title = "Yauaa: Yet Another UserAgent Analyzer"
linkTitle = "Yauaa"
description = "This is a java library that tries to parse and analyze the useragent string (and when available the User-Agent Client Hints) and extract as many relevant attributes as possible."
+++
This is a java library that tries to parse and analyze the useragent string (and when available the User-Agent Client Hints) and extract as many relevant attributes as possible.

Works with Java, Scala, Kotlin and provides ready for use UDFs for several processing systems.

The full documentation can be found here [https://yauaa.basjes.nl](https://yauaa.basjes.nl)

---

## HIGH Profile release notes:

These are only the highlights for the last few releases, the full changelog can be found [here](https://github.com/nielsbasjes/yauaa/blob/main/CHANGELOG.md).

### NEXT RELEASE
- New/improved detections:
  - Updated the list of Amazon devices (2023, 2024 models)
  - Fix phones with real browser name at the end (like AAB does)
  - Presearch browser, Citrix WorxWeb, Klarna, Budbee, MAGAPPX, Yandex, Albert Heijn App, Ghostery, Dalvik,
  - ZTE Nubia
  - Very old Samsung Browser is a webview
  - Whitelabel "Safe" Browser apps (iOS): Ziggo, KPN, VandenBorre, F-Secure
  - Handle "Windows 11.0" (the '.0' is very rare)
  - CPU tag arm_64
  - Handle URLs better with Robots/Hackers/Spammers
  - UltraBlock useragent randomizer

### Version v7.29.0
- Build
  - Require JDK 23 installed for Trino support.
  - Leverage new toolchains plugin: no longer needs toolchains.xml.
- New/improved detections:
  - Do tag lookups for Webviews (Yandex showed wrong)
  - SamsungBrowser with a newer "reduced" version on a Phone doing DEX.
  - Snorlax useragent with BASE64 encoded part
  - Devices from OX Tab, Xiaomi
  - Partially handle broken: Safari "Mobile" on Mac OS X
  - Gitlab CI Runner
  - HUAWEI Quick App Center (+ false positive of it being a Hacker)
  - TV Bro
- Analyzer:
  - Renamed Sec-CH-UA-Form-Factor to Sec-CH-UA-Form-Factors (no rules yet)

### Version v7.28.1
- Build/Release:
  - Trino UDF was not released with 7.28.0
  - Fix reproducibility issue

### Version v7.28.0
- Analyzer:
  - Fix edge case if ONLY a specific useragent is given in SQL class UDFs.
- New/improved detections:
  - TikTok, Trill

### Version v7.27.0
- New/improved detections:
    - Many Email Clients: Yahoo Y!mail, Mailapp, Foxmail, Superhuman, PostboxApp,
      Mailspring, Mailbird, Spark, Zoho Mail, Zoho Trident, MailMaster,
      NewtonMail, Tempo, Lark, Sunsama, Monday, Tuta, Fastmail, OneOutlook
  - Lenovo SLBrowser
- Updated UDF dependencies
    - The Trino UDF requires Java 22 which is non-LTS. This UDF will simply not be built if Java 22 is missing.
- Webapp
  - Improved the layout in case of very long single words.

### Version v7.26.1
- New/improved detections:
  - Samsung Browser 24 now does Frozen UA

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
