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

## Significant change happened in Apple iPads
Since about February 2025 the Apple iPads no longer are reported as a Tablet because their useragent has switched to reporting a Desktop:

A known device which was previously reporting these useragents

    Mozilla/5.0 (iPad; CPU OS 18_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148
    Mozilla/5.0 (iPad; CPU OS 18_3_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko)

is now reporting this one

    Mozilla/5.0 (Macintosh; Intel Mac OS 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.0 Safari/605.1.15

This last one is identical to an Apple Mac Desktop and this makes it impossible to determine that this is really a Tablet.

Currently (April 2025) it seems that this effect is different depending on the used browser. Browsers like Chrome, Edge, Opera, Aloha and Yandex still report `iPad Tablet`, but browsers like Safari, Brave, Firefox, DuckDuckGo and Arc Search report `MacOS Desktop`

## HIGH Profile release notes:

These are only the highlights for the last few releases, the full changelog can be found [here](https://github.com/nielsbasjes/yauaa/blob/main/CHANGELOG.md).

### NEXT RELEASE
- Analyzer:
    - Needs Java 17 LTS to run.
- Build:
    - Needs Java 25 LTS to build.
- UDFs;
    - Dropping the Trino UDF
    - Dropping the ElasticSearch 7.x UDF (ES7 is EOL)
- New/improved detections:
    - Robot StatusCake (thanks to https://github.com/bdmendes)

### Version v7.32.0
- Analyzer:
    - Disable using the default constructor (thanks to https://github.com/izeye)
- Build
    - Require JDK 24 installed for Trino support.
    - Remove workaround for conjars.org going away.
- New/improved detections:
    - IntelliJ IDEA (now Desktop App on a Desktop)
    - Safari 26 on iOS 26
    - Electron Desktop App using special format
- UDFs:
    - Update UDF for Elasticsearch 9 to their API changes

### Version v7.31.0
- New/improved detections:
    - CrowBrowser (LG SmartTV),
    - Robots: Scrapy, HTTPie
    - OpenHarmony
    - ArkWeb (HuaweiOS browser engine)
- UDFs
    - New UDF for Elasticsearch 9
    - All Elasticsearch UDFs(Plugins) must be built by the user for their specific version.


---
## License
I'm publishing this under the Apache 2.0 license because I believe it is the best way to make these kinds of projects available to the world.

But do not underestimate how much work went into this. I have spent over a decade tuning this project to what you have now.

All I want to see in return is a little bit of gratitude from the people who use this.
If you are a home user/hobbyist/small business then a simple star on the project is enough for me. Seeing that people use and like the things I create is what I'm doing this for.
What also really helps are bug reports, headers from real/new devices/browsers that I have net yet seen before and discussions on things you think can be done better.

Despite there not being any obligation (because of the Apache 2.0 license); If you are a big corporation where my code really adds value to the products you make/sell then I would really appreciate it if you could do a small sponsor thing. Buy me lunch (€10), Buy me a game (€100) or what ever you think is the right way to say thank you for the work I have done.

[![If this project has business value for you then don't hesitate to support me with a small donation.](https://img.shields.io/badge/Sponsor%20me-via%20Github-darkgreen.svg)](https://github.com/sponsors/nielsbasjes)

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
