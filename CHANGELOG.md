This is intended as an overview of the major changes

v5.1
===
- Fix bug in UCBrowser detection (too often reported as Tablet) 
- More languages
- Many bug fixes found on the input provided by https://github.com/DaimonPl (Thank you)
- Windows NT 7/8/8.1/10 on ARM cpu = Windows Phone --> Mobile device, not Desktop
- Better detection and naming of the Amazon, HP, PocketBook, Manta and Prestigio devices

v5.0
===
- Fix bug in iOS detection with some apps
- Drop tests after preheat if dropTests is requested
- Changed the default to use the delayed initialization.
- Only calculate special fields if needed.
- Dropped support for old style constructors
- Flink UDF: Test both DataSet and DataStream use.
- Updated UDF dependencies
  - Apache Flink 1.5.0
  - Apache Drill 1.13
  - Apache Beam 2.5.0
  - Apache Nifi 1.7.0
- Use Spring 2.0.x for the webapp

v4.5
===
- Check (and fail) if two (possibly different) versions are both loaded.
- Default cache size was not used.

v4.4
===
- Added extra checks to avoid using the builder twice
- Completely dropped Platfora
- Detect the brand 'Cubot' as not a robot
- Improved performance by rewriting some Robot detection rules
- Fixed OWASMIME plugin problem.
- Improved Netfront detection
- Fallback to Stock Android Browser in a few more cases.

v4.3
===
- Bump Apache Beam version from 2.0.0 to 2.4.0
- Added Processor (UDF) for Apache Nifi 1.6.0
- Detect Opera Touch
- Optimized construction and memory usage.

v4.2
===
- Changed CPU classification regarding Intel/AMD x86_64 and Intel Itanium x64
- Fixed several problem cases with Internet Explorer
- Added more complete Docker scripting for the webapp.
 
v4.1
===
- Added newer Apple devices like iPhone 8 and X

v4.0
===
- Switch Analyzer to interface with default methods
- Rename package annonate to annotate (which was a typo)
- Upgrade Antlr to 4.7.1 and get rid of bug workaround
- Introduce "Email Client" as a new AgentClass value.
- Included the UDF for Apache Drill originally written by [Charles S. Givre](https://github.com/cgivre)

v3.3
===
- By default do not load all test cases into memory
- Fixed NPE in parse error situation (Hacker scenario)
- Never let any NPE disturb the calling application ever again

v3.2
===
- Detect Chromium, SugarLabs, Ubuntu Touch
- Detect few more robots
- New matching language feature: Is this value present in a predefined set of values?
- New matching language feature: Basic String concatenation
- New matching language feature: Variables
- Make doing 2,3 and 4 steps Next/Prev more efficient
- [Davide Magni] Small fixes on WEB-INF to test & deploy using NetBeans.
- [Davide Magni] Simple change in form POST action (Parse Service) to support deploying in both root and non-root paths on webservers.
- [Davide Magni] Massively improved Android brand detection for Sony, Xiaomi, Wiko, LG, Huawei, Alcatel, Vodafone, Meizu and Asus by Davide Magni (BIG THANKS!)
- Make matching prefix strings use the HashMap for performance improvement

v3.1
===
- Detect Microsoft Edge for Android & iOS

v3.0
===
- Avoid DDOS (too long parsing times). Without actual parsing: Useragent too tong == hacker.
- Separate the parser from the caching.
- Massive rewrite of the Builder system to avoid redundant code (hence 3.0)
- Platfora UDF is no longer part of the release.

v2.2
===
- Detect Firefox Focus and a more generic pattern for WebView applications
- Fixed NPE

v2.1
===
- Rewrote managing matches to reduce allocation/gc load (i.e. improve performance)
- Determine the Device Cpu Bits (i.e. 32 or 64) where possible.
- Stop release process if there are uncommitted changes.

v2.0
===
- Upgraded to need Java 1.8 (hence the major version number)
- Added generic annotation/setter based parsing
- Added Apache Flink mapper
- Added Apache Beam function
- Updated dependencies Antlr 4, Apache Hadoop, Apache Pig, Checkstyle
- Changed the structure of an extracted Keyvalue.
- Rearranged the structure of StepDown (core step in walking around).
- Added Apache Hive UDF
- Various performance improvements (overall effect is approx 30% less time needed)
- Refactored the webapp to use Spring-boot
- Switched from Cobertura to JaCoCo (because of Java 8)

v1.4
===
- Rewrote the yaml reading to allow reporting the filename and line numbers.
- Added overwrite for specific Lenovo Tablets that do not conform to normal conventions.
- Made the Analyzer Serializable which should make using it in distributed systems easier.
- Improve the detection of certain Robots and the old Tablet PC
- Fixed class of broken useragents.
- Fixed false positive "Honor Note" matches SQL fragment "OR NOT"
- Detect Coc Coc Browser
- Improved commandline tool
- Make order of the extracted fields stable (Set --> List)
- Fail when asking for a field that cannot be extracted
- Fixed stackoverflow (infinite recursion) in specific parse situations.
- Fixed regex problem.

v1.3
===
- Added simple classification tool
- Remove '_' from DeviceName
- Fixed NPE in Pig udf
- Shaded the used parts of Spring and the Antlr4 runtime to minimize the impact on downstream applications.

v1.2
===
- Limit use of IsNull operator.
- Fix root level fields.
- Fix issues with Hacker detection
- Implement DuckDuckGo app
- Implement several Tencent apps: WeChat, QQ Browser, QQ Client
- Cleanup and normalization of the language code
- [PIG] Return a schema with ONLY the requested fields
- Consistently add the Brand to the DeviceName to get a much more consistent dataset.
- Implement Google Pixel devices
- Improved the extraction of the DeviceName

v1.1
===
- Report on IE compatibility mode
- Walker language now supports nesting some functions
- Added NormalizeBrands as a new function
- Walker language now allows spaces for readability
- All 'Hacker' situations now have DeviceVersion field set.
- [BUG] Matchers with ONLY IsNull and Fixedstrings would never match.
- Added Opera Neon, Vivaldi and Firefox Focus

v1.0
===
- Various small improvements.
- Project is mature enough to call this 1.0

v0.12
===
- Changed the way the version and commit information is made available in the code.
- The UDFs now return the version of the analyzer engine
- Fixed nasty exception in rare cases where %20 appears in the useragent.

v0.11
===
- Implement test code coverage measuring
- Improve test coverage.
- Detect Bing related bots.

v0.10
===
- Cleanup of duplicate rules (small performance improvement)
- Added JMH based benchmark
- Added the option to remove all rules that only provide fields we're not interested in at that moment.
- Pig UDF support for requested fields
- Improved performance by detecting a mismatch faster.
- Change license to Apache 2.0

v0.9
===
- Detect very old Mac devices
- iPod is now a "Phone" because of the screensize.
- Split words also on _
- New Device class: "Handheld Game Console"

v0.8
===
- Detect SQL injection better.
- Detect some smartwatches
- Many updated and improved rules
- Allow setting the cache size

v0.7
===
- Allow wiping a value
- Allow setting all fields to a specific value (for example wiping them)
- Changed the matcher language to support extracting ranges from a string (like; extract 3rd to 5th word).

v0.6
===
- Fixed build (jars and uber jars)
- The top level 'agent' is the unparsed version needed for better hacker detection.
- Better hacker detection (SQL variants)
- Fixed bug in the way the value is passed during walking around
- Fixed bug in the tests

v0.5
===
- Restructured the project
- Added very simple testing servlet

v0.4
===
- Detect webviews
- Detect race conditions in the rules (unit tests fail when detected)
- Added missing IDs for new Apple devices
- Set-top boxes, Bada, Stock Android browser, ...
- A lot of documentation

v0.3.1
===
- Detect Opera better
- UDF for Platfora
- Cleanup of rules and results
- Ability to measure performance better
- 'Robot' and 'Mobile Robot'

v0.2
===
- Added checkstyle to the build process
- Improve detection Phone/Tablet
- Improve detection Operating system name
- Improved performance from ~90/sec to >4000/sec (measured on an i7).

v0.1
===
- Initial release of a parser and matcher version that seems to work pretty good.

License
=======
    Yet Another UserAgent Analyzer
    Copyright (C) 2013-2018 Niels Basjes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
