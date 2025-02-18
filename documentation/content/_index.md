+++
title = "Yauaa: Yet Another UserAgent Analyzer"
linkTitle = "Yauaa"
description = "This is a java library that tries to parse and analyze the useragent string (and when available the User-Agent Client Hints) and extract as many relevant attributes as possible."
+++
This is a java library that tries to parse and analyze the useragent string (and when available the User-Agent Client Hints) and extract as many relevant attributes as possible.

Works with Java, Scala, Kotlin and provides ready for use UDFs for several processing systems.

The full documentation can be found here [https://yauaa.basjes.nl](https://yauaa.basjes.nl)

If you just want to give it a quick try then you can do that with your own browser here: [https://try.yauaa.basjes.nl/](https://try.yauaa.basjes.nl/) (runs on a very slow and rate limited machine).

---

## HIGH Profile release notes:

These are only the highlights for the last few releases, the full changelog can be found [here](https://github.com/nielsbasjes/yauaa/blob/main/CHANGELOG.md).

### NEXT RELEASE
- New/improved detections:
  - ...

### Version v7.30.0
- New/improved detections:
  - Updated the list of Amazon devices (2023, 2024 models)
  - Fix phones with real browser name at the end (like AAB does)
  - Presearch browser, Citrix WorxWeb, Klarna, Budbee, MAGAPPX, Yandex, Albert Heijn App, Ghostery, Dalvik, Nu.nl (iOS)
  - ZTE Nubia
  - Pico 3 and Pico 4 VR Headset
  - Very old Samsung Browser is a webview
  - Whitelabel "Safe" Browser apps (iOS): Ziggo, KPN, VandenBorre, F-Secure
  - Handle "Windows 11.0" (the '.0' is very rare)
  - CPU tag arm_64
  - Handle URLs better with Robots/Hackers/Spammers
  - UltraBlock useragent randomizer
  - Improve DuckDuckGo
  - Mapping ClientHint value for AgentName (i.e. ClientHint "YaBrowser" --> "Yandex Browser")
  - Handle edgecases:
      - 'OpenBSD != Linux amd64'
      - 'Linux x86_64:108.0'
  - Robots (Generic, Fediverse and AI Related):
      - AmazonBot, Bravebot, PetalBot
      - FediIndex, vmcrawl, Nonsensebot, Caveman-hunter, ...
      - OpenAI/ChatGPT, Claudebot (Anthropic), PerplexityBot
  - Codeberg.org is a code hosting site (not a brand for a bot)
  - Updated the ISO 639-3 language code table

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


---
## Donations
If this project has business value for you then don't hesitate to support me with a small donation either via [Github Sponsors](https://github.com/sponsors/nielsbasjes) or [Paypal](https://www.paypal.me/nielsbasjes).

---
## License

    Yet Another UserAgent Analyzer
    Copyright (C) 2013-2025 Niels Basjes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
