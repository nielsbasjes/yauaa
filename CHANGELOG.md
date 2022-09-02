
This is intended as an overview of the major changes

v7.6.0-SNAPSHOT
===
- Build
  - Require JDK 8, 11 and 17 installed and the appropriate toolchains.xml.
- Updated UDF dependencies
  - Elastic Search          7.17.6 & 8.4.1
  - Elastic Logstash        8.4.1
  - Trino                   394


v7.5.0
===
- Build
  - Because of dependencies and plugins the build now requires Java 17.
- New/improved detections
  - Report frozen Mac OS as `Mac OS >=10.15.7` for all browsers (Chrome, Firefox, etc.).
  - Browsers: Ekioh Flow, Aoyou
  - Arris Set-top boxes
  - Privoxy/PrivacyWall
  - The Ask Toolbar UA modification is no longer seen as a browser.
  - Analyze https://cobalt.dev/ based devices
  - Robots with `null`.
  - Better classify `AppleWebKit` and `Safari` without a version.
  - Extract the Agent name which was appended to a normal UA.
  - Optimized the Language detection (less recursion & lookups needed)
- Analyzer:
    - Made the `NotIn` operator in the rules consistent (`?!` is now `!?`)
    - Improve extracting versions like `PHP/7.1.11-1+ubuntu16.04.1+deb.sury.org+1`
    - Extract comments that are ',' separated.

v7.4.0
===
- Analyzer:
  - IsValidVersion function to determine if a version looks like a normal version.
- Webservlet:
  - GraphQL support
- New/improved detections
  - Report frozen `Mac OS` on Safari as `Mac OS >=10.15.7`
  - New DeviceClass: "Home Appliance" (Fridges and such)
  - Browsers that extend the Edge useragent.
  - Oculus VR devices/OculusBrowser
  - Some historical systems (IRIX, CP/M, RISC OS, AmigaOS, ...)
  - Handle a lot of cases where the braces were removed.
  - Anonymization
    - All anonymized agents have DeviceClass: 'Anonymized'
    - More extensive anonymization detection by looking at version patterns
  - New field "RemarkablePattern" that will hold exceptional things that were detected.
    - The 'Anonymized' field has been dropped and is now RemarkablePattern: 'Anonymized'
  - Improved CPU detection performance
- Extra testcases
  - Fuchsia
  - Mobile Apps, Robots, Hackers, Modification cases
  - Windows 9 (which does not exist)
- Updated UDF dependencies
  - Apache Beam             2.40.0
  - Apache Flink            1.15.1
  - Apache Nifi             1.17.0
  - Apache Drill            1.20.2
  - Elastic Search          7.17.5 & 8.3.2
  - Elastic Logstash        8.3.2
  - Trino                   392

v7.3.0
===
- New/improved detections (Thanks to https://github.com/FrancoisDevemy5 for providing many of the testcases)
  - Apple
    - Use information from Apple to complete the list of devices.
    - Apple CFNetwork+Darwin with CPU tag is macOS instead of iOS
    - Apple M1 CPU (hardly ever seen)
    - Apple watchOS
  - Qwant browser
  - Robots: PindomTMS, Yahoo and many http libraries.
  - Hackers: More security scanners.
  - Google Docs (Google Document Conversion)
  - Facebook Messenger Lite for iOS
  - Electron
  - "TV Safari" means TV
  - Improve the naming of all Google bot variants.
- Webservlet:
  - The input UserAgent is now also part of the output to allow easier processing.

v7.2.0
===
- New/improved detections
  - Chromium
  - Improve priotities with multiple 'Windows NT' tags.
  - Updown.io robot
- Updated UDF dependencies
    - Apache Beam             2.39.0
    - Apache Nifi             1.16.2
    - Elastic Search          7.17.4 & 8.2.2
    - Elastic Logstash        8.2.2
    - Trino                   385

v7.1.0
===
- Analyzer
  - Simplify using Java 8 compliant caching
- New/improved detections
  - Chrome OS/Chromebooks:
    - Handle new "frozen Chrome OS" tag
    - The DeviceBrand of a Chromebook is "Unknown" instead of Google
    - Fixed Version and OS Build tags

v7.0.0
===
- Client Hints!
  - Support for analyzing the User-Agent in combination with the User-Agent Client Hints.
- Security:
  - Update for jackson-databind
- New/improved detections
  - Microsoft Edge Webview
  - Sailfish OS & Browser
  - ReactorNetty, Node.JS, Alamofire
  - Improve Apple detection/reporting
    - "Mac OS X" / "OS X" / "macOS"
        - The Major Version for the 10.x.x versions is now 2 parts (like '10.6') instead of just '10'.
        - Although the marketing name has changed several times: For all versions `OperatingSystemName = 'Mac OS'` is used to ensure stable reporting.
    - Darwin will be reported as the most likely iOS version instead.
- Build
  - Use new feature in maven-shade-plugin to use the correct pom.xml in the jar.
  - Restructure integration tests and examples to run outside the maven reactor.
- Analyzer
  - Option to show only minimal version information during startup.
- Updated UDF dependencies
  - Apache Beam             2.38.0
  - Apache Flink            1.15.0
  - Apache Nifi             1.16.1
  - Elastic Search          7.17.3 & 8.1.3
  - Elastic Logstash        8.1.3
  - Trino                   380
- Dropping support for
  - Apache Pig. The last release was in 2017: about 5 years ago.

v6.12
===
- Build:
  - Apache Flink Table and Hive UDFs now also build under JDK 17
  - Apache Hive UDF has docker based integration test
- UDFs
  - New UDF for Elastic Search 8.1.1
  - Trino UDF uses ThreadLocal for better performance
- Updated UDF dependencies
  - Apache Flink            1.14.4
  - Elastic Search          7.17.1
  - Elastic Logstash        8.1.1

v6.11
===
- New/improved detections
  - Handle the Chrome 99+ chrome://flags/#force-major-version-to-minor
  - Handle the Edge 99+ edge://flags/#force-major-version-to-minor
  - Report frozen `Windows 10` on Chrome/Edge/... 92+ as `Windows >=10`
  - Specific edge case in Internet Explorer UA.
- Updated UDF dependencies
  - Apache Beam             2.37.0

v6.10
===
- UDFs
  - New UDF for Trino (a.k.a. Presto SQL)
- Updated UDF dependencies
  - Apache Drill            1.20.0

v6.9
===
- Analyzer
  - New IsNotInLookUpContains function used to speedup Robot pattern matching.
- New/improved detections
  - Report frozen `Windows 10` on Firefox 88+ as `Windows >=10`
  - Handle cases like this `LM-Q710(FGN) Build/OPM1.171019.019` ([Sam Hendley](https://github.com/samhendley/))
  - Detect Samsung Tizen TVs, Vizio and Comcast ([Sam Hendley](https://github.com/samhendley/))
- Build
  - Run tests against created docker image.
- Webservlet:
  - Omit the non standard fields with a default value.
- Updated UDF dependencies
  - Apache Beam             2.36.0

v6.8
===
- Analyzer
  - The XML/JSon/Yaml output now always contains the requested AND all needed "Unknown" value fields.
  - Switched the default caching implementation to Caffeine
- New/improved detections
  - Classify more Apache Log4j RCE CVE-2021-44228 useragents as "Code Injection".
  - Fix Samsung SC-... devices (were detected as Huawei SC-... devices).
- Updated UDF dependencies
  - Apache Flink            1.14.3
  - Apache Nifi             1.15.3
  - Elastic Search/Logstash 7.16.2

v6.7
===
- Security
  - Updated log4j dependency to 2.17.1
- Analyzer
  - Allow specifying a custom caching implementation.
- Updated UDF dependencies
  - Apache Beam             2.35.0
  - Elastic Search/Logstash 7.16.0
- Build:
  - Builds under JDK 17 (by disabling incompatble UDFs)

v6.6
===
- Security
  - Updated log4j dependency to 2.17.0
- Updated UDF dependencies
  - Apache Nifi             1.15.2
- Examples
  - Added a Java example using SLF4J (so no Log4j2).

v6.5
===
- Security
  - Updated log4j dependency to 2.16.0
- New/improved detections
  - Classify Apache Log4j RCE CVE-2021-44228 useragents as "Code Injection".
  - Handle the SalesforceMobileSDK apps better.
- Updated UDF dependencies
  - Apache Flink            1.14.1
  - Apache Nifi             1.15.1

v6.4
===
- Security
  - Updated log4j dependency to 2.15.0
- Analyzer
  - Added WebviewAppNameVersion.
  - Added a 'not contains' operator '!~'
  - Parser can handle a few more tricky cases
  - Change synchronization to improve concurrent performance.
- New/improved detections
  - More Apple devices
  - VivoBrowser often implies Vivo DeviceBrand
  - Handle the SalesforceMobileSDK apps better.
  - More Amazon devices
- Updated UDF dependencies
  - Apache Beam             2.34.0
  - Elastic Search/Logstash 7.15.0
- Build:
  - Change developer building docker image to Ubuntu 20.04

v6.3
===
- New/improved detections
  - Since Chrome 90 the version of MacOS is already frozen for everyone.
  - Since Firefox 87 the version of MacOS is already frozen for everyone.
- Updated UDF dependencies
  - Apache Nifi             1.15.0

v6.2
===
- Analyzer
  - The toMap incorrectly filtered out "default" values you asked for.
- New/improved detections
  - Handle webconference plugins in IE
  - Detect MorphOS
  - The type of CPU is no longer (incorrectly) used as the version (Linux i386 --> Linux ??) .
  - Dalvik is not a browser
- UDFs
  - New UDF for Snowflake

v6.1
===
- Build:
  - Only support JDK 11+ due to plugins dropping java 8 support. Build target remains JRE 8.
- New/improved detections
  - Google Glass OS
  - Language flags in specific special places
  - Microsoft Word
  - Google Chrome reduced/frozen useragent.
  - Tesla car based browsers
  - DuckDuckGo variants
- Webservlet:
  - Switch from SpringFox to SpringDoc.
- Updated UDF dependencies
  - Apache Flink            1.14.0
  - Apache Beam             2.33.0
  - Apache Nifi             1.14.0

v6.0
===
- Analyzer
  - Breaking change: Replace SLF4J with Log4j2
- New/improved detections
  - Detect Apple TV better
  - Handle 'no spaces' effects like "WindowsNT6.1" better.
  - Added testcase for the real PS5 useragent.
  - Added missing locale en-jp
  - Improve classification of very old Apple Macintosh systems.
  - MacOS X >= 10.8 is only 64 bit
  - Improved handling of the '@' character.
  - Huawei: HarmonyOS/HMSCore/new devices/...
- Updated UDF dependencies
  - Apache Flink            1.13.1
  - Apache Beam             2.30.0
  - Apache Nifi             1.13.2
  - Apache Drill            1.19.0
  - Elastic Search/Logstash 7.13.0
- WebServlet:
  - Cleaning up the code
  - Also show the classifications based upon the DeviceClass
- Build:
  - Disabling usage of docker hub in CI build to avoid random failures over "You have reached your pull rate limit."

v5.23
===
- Analyzer
  - Updated the list of recognized Apple device ids.
  - Fixed "Dell Streak 7"
- Updated UDF dependencies
  - Apache Beam             2.28.0

v5.22
===
- Analyzer:
  - Files with '-tests' in the directory/filename contain only tests and are only loaded when needed.
  - ExtractBrandFromUrl function
  - Added rule analyzer and optimized some of the rules.
  - Special handling of the "require IsNull" cases to increase performance.
  - Merging lookups to reduce number of rules.
  - For the preHeat the testcases are loaded separately (i.e. reducing the memory usage a bit)
- New/improved detections
  - Added > 1000 extra testcases
  - Detect Google Glass 1, Amazon Kindle, Palm, Roku, PlayStation 2, PS3, PS Portable, Tizen, WebOS.
  - Extra variants of Japanese locale codes
  - Extra variants of MS Windows recognized and Wine64.
  - Old Maxthon naming (myIE2)
  - Brands: KDDI, O2, T-Mobile, MDA
  - Skip plugins and ISP names
  - AgentName "Mobile Safari" is now "Safari" (on iOS) or "Stock Android Browser"
  - AgentName "Mobile xxx" is now "xxx"
  - Less cases of DeviceClass "Mobile"
  - Hackers: Report more generic "Code Injection" (specifics are too unreliable).
- WebServlet:
  - Yaml output retains # comments from the input
- Updated UDF dependencies
  - Apache Flink            1.12.1
  - Apache Beam             2.27.0
  - Apache Nifi             1.13.0
  - Elastic Search/Logstash 7.11.1
- New UDF: Beam SQL

v5.21
===
- Analyzer:
  - Improve the Calculator setup.
- New/improved detections
  - Tests: Edge on Linux
  - Frozen Chrome/Edge:
    - Minor version of x.0.0.0 is removed.
    - With Edge on Linux we cannot assume Windows 10 for Edge.
  - Handle Facebook agents with lists of keyvalues
  - Classify facebookcatalog as a Robot
- Updated UDF dependencies
  - Apache Flink            1.12.0
  - Apache Beam             2.26.0

v5.20
===
- Analyzer:
  - Improved the brand extraction from the url (github and gitlab urls).
  - Improved support for Kryo
- UDFs:
  - Fixed bug in LogParser UDF that causes setters to be called twice
- New/improved detections
  - Ghostery 3.0
  - OpenConnect VPN client, Cisco AnyConnect, NMap imitating AnyConnect
  - Windows Phone (via Opera)
  - LinkedIn app (on iPhone)
  - HTC Sensation is a 'Phone'
  - Made "Microsoft Outlook", "MacOutlook", "Outlook-Express", "Outlook-iOS" consistent and "Email Client"
- Build
  - Disable sonarcloud on JDK 8
- Updated UDF dependencies
  - Apache Drill            1.18.0
  - Apache Nifi             1.12.1
  - Apache Flink            1.11.2
  - Apache Beam             2.25.0
  - Elastic Search/Logstash 7.10.0

v5.19
===
- Analyzer:
  - Added ReplaceString to support extracting texts
  - Added additional fallback for resource finding problems (like in Quarkus)
- New/improved detections
  - Extract Google tools name from the provided url
  - Apache Beam useragent
- Updated UDF dependencies
  - Elastic Search/Logstash 7.8.1
  - Apache Flink            1.11.1
  - Apache Beam             2.22.0
- Build
  - Improved support for building using JDK 11
- Examples
  - Basic testing example to verify it works with Quarkus

v5.18
===
- Analyzer:
  - Fixed caches for getAllPossibleFieldNames and getAllPossibleFieldNamesSorted
  - Updated dependencies
  - Fixed bug returning default values from cached results (no incorrect results, just too many)
  - The output is now "immutable" which makes returning from cache faster.
  - Rewrite the handling of "Default" values
  - Rewrote Calculators.
  - Allow creating a set by merging others in it.
  - Match if value is NOT in "set"/"lookup"/"lookup prefix"
- Updated UDF dependencies
  - Apache Flink     1.10.1
  - Apache Beam      2.22.0
  - Elastic Logstash 7.7.1
- New UDF: ElasticSearch 7.7.1
- New/improved detections
  - Detect a very rare iPhone variant
  - Detect RetroZilla browser
  - Detect Windows CE better and related Minimo browser
  - Avoid "OneDrive" being classified as the DeviceBrand "OnePlus"
  - Improved Robot classifications, no more urls as 'version'
  - Voice/Audio devices: Spotify, Alexa, Echo, Sonos
  - Improve PlayStation 3,4 OS names
  - Handle the Frozen User-Agent as used in Chrome and Edge.
  - Detect the "new" Fuchsia OS
  - Browsers: Maxthon Nitro, Aloha, Phoenix, Cake, Kode, Mint, Nox, Tenta, Sleipnir, GreenBrowser, SlimBrowser, Lunascape,
  - Browsers: More automatically detect MS-IE derivatives
  - Languages: Detect language variants as "nlbe" in addition to "nl-be" and "nl_be"
- Build
  - Include OWASP check for bad dependencies

v5.17
===
- Analyzer:
  - Make Yauaa usable as an automatic module in Java 9+ (Fix by [James Pether Sörling](https://github.com/pethers))
  - Updated dependencies
  - Cache the result of getAllPossibleFieldNames and getAllPossibleFieldNamesSorted
  - Improved behaviour regarding the default value of a field ("Unknown" is not always the default)

v5.16
===
- Analyzer:
  - Windows NT 9 --> Hacker
  - Can now load a resource string as 'Optional' (so it does not fail if these are missing)
  - Updated to HttpComponents Client 5.0
  - Added new function DefaultIfNull
  - Added new string split by '|' to make some lookups easier.
  - Added destroy() method to clean up all memory usage.
  - Fixed inconsistent behaviour regarding the default value of a field
  - Rules can now test if there was a syntax error while parsing.
- New/improved detections
  - Test cases for the 'frozen' Chrome strings.
  - Fixed edge case with duplicated tags.
  - Mytheresa app, Catchpoint Analyzer, Dynatrace Ruxit.
  - More Google related bots
- Updated UDF dependencies
  - Apache Nifi      1.11.4
  - Apache Flink     1.10.0
  - Elastic Logstash 7.6.1
- Demonstration WebServlet
  - Support multiple useragents in one API request/manual test (line separated)
  - Support for custom rules
- Other
  - Added simple Kotlin example
  - Improvements in the Drill UDF and tests

v5.15
===
- Analyzer:
  - Better url extraction.
  - Fix NPE when defining invalid rules (walk left/right/up from the top of the tree)
  - The Tencent/Alibaba NetType and Language tags are now better extracted.
  - AgentName is now more consistent (konqueror/Konqueror)
  - Handle the BlackBerry Leap devices a bit better
- New/improved detections
  - App: Desktop apps using Electron (https://electronjs.org/)
  - Devices: Improve detection of Apple devices
  - Brand: Huawei/Honor
  - Robots: 'probe' and 'alyzer' both imply 'Robot', new Bing useragents.
- Updated UDF dependencies
  - Apache Flink     1.9.2
  - Apache Beam      2.19.0
  - Apache Nifi      1.11.0
  - Elastic Logstash 7.5.2
- Build
  - Improve scripting for generated files
  - Only build the docker image for the webservlet if docker is available.
  - Automatically download the logstash dependency from the elastic website.
  - Use SpotBugs (=FindBugs) to detect subtle problems.
- Demonstration WebServlet
  - Updated to SpringFox 3.0.0-SNAPSHOT to support better examples in the Swagger UI.
  - Support for custom rules
- UDF changes
  - Flink Table function now returns a Map&gt;String, Stringi&lt; with all the values in a single call.

v5.14.1
===
- Use the latest STABLE slf4j version 1.7.29

v5.14
===
- The Builders no longer expose any generic parameters to ease use by Scala users (contributed by [Robert Stoll](https://github.com/tegonal)).
- Replace Guava with Apache HTTPComponents for Public Suffix matching
- Migrate to JUnit 5 for everything except specific UDFs
- Analyzer:
  - Handles a COMMA better.
  - New LookUpContains and IsInLookUpContains functions used to speedup Hacker and Robot pattern matching.
- Bugs:
  - LayoutEngine for Chrome on iOS is AppleWebKit (not Blink)
  - Matchers with only IsNull rules did not fire after deserialization with Kryo.
  - Fixed nasty problem in the serialization of various classes. Added many toString implementations to track it down.
- New/improved detections
  - Agent: EmbeddedBrowser
- Updated UDF dependencies
  - Apache Nifi   1.10.0
  - Apache Hive   3.1.2
  - Apache Hadoop 3.2.1

v5.13
===
- Analyzer improvement by allocating less AgentFields (i.e. less memory).
- Optimized rules to reduce startup time and memory usage.
- Update public suffix list for detecting hostnames.
- Added a basic API and Swagger UI to the demo webservlet
- New/improved detections
  - Agent: Latest Edge, HeadlessChrome, CrMo (=very old Chrome), Hawk
  - Robots: Apache Nifi, Wget, Curl, Bytedance Bytespider, Popular product "con_aff" robot., TencentTraveler, EmbeddedWB
  - Device: Improved Xiaomi detection., Improved RaspberryPi
- Fixes:
  - Check if a used variable actually exists.
  - Many TODO items (mostly corner cases).
  - Domains like Github and Gmail are no longer used as "DeviceBrand" when they occur in URL or Email.
  - Edgecase where much of the useragent was incorrectly seen as a BASE64 fragment.
- Updated UDF dependencies
  - Apache Flink  1.9.1
  - Apache Beam   2.16.0

v5.12
===
- New detections
  - Agent: AdobeAir, Whale, Tungsten, Kinza, Iridium, Superbird, Avast, Comodo Dragon & IceDragon
  - Device: Raspberry PI
  - OS: KaiOS
  - Brands: Lyf
  - Robots: Naver Yeti, TrueClicks
  - Anonymized: Google Web Light (proxy)
- Updated UDF dependencies
  - Apache Flink  1.9.0
  - Apache Beam   2.15.0

v5.11
===
- Finalized detection of Chromium/Blink based Edge both for Windows 10 and Mac
- Detect Liebao Browser
- Make compiler a bit stricter, fixed the warnings.
- Updated UDF dependencies
  - Apache Flink  1.8.1
  - Apache Beam   2.13.0
  - Apache Drill  1.16.0
- Added two new fields: OperatingSystemVersionMajor and OperatingSystemNameVersionMajor
- Fix detection of iOS in specific edge case
- Modularized and optimized the postprocessing of the found fields.
- Updated Kryo
- Updated all dependencies and build plugins.
- Now builds under both the JDK 8 and JDK 11
- Improve detection of Maemo / Nokia N900
- Extra testcases for Firefox (They implemented some small useragent changes)

v5.10
===
- Document Scala usage.
- Updated UDF dependencies
  - Apache Flink  1.8.0
  - Apache Beam   2.12.0
- Improved SpeedCurve Robot detection (thanks to Ben Rogers)
- Detection for Chromium/Blink based Edge on Windows 10
- Detect Sogou Explorer (Sogou Browser)

v5.9
===
- Speedup in handling IsNull cases.
- Speedup in skipping the untouched Matchers.
- Detection for Google Go, Google Docs, Google Docs Script
- New class of Device and Agent: Voice
- Added experimental filter for logstash
- Detection for CAT, General Mobile, Wileyfox, ZTE, Fairphone, Gigaset, PlayStation 3, Kobo Desktop Edition
- Improved Robot detection, most of them are now "Cloud" instead of "normal" hardware/os.
- Updated the way yaml files are loaded. An analyzer without any rules will fail faster.
- An imperfect fallback is attempted when the classloader cannot find the yaml files via a wildcard (happens on Android, OSGi, ...).
- Improved detection of Ubuntu
- Detection for very old Windows Phones, Nikto, Dell devices
- Updated UDF dependencies
  - Apache Flink  1.7.2
  - Apache Beam   2.11.0
  - Apache Nifi   1.9.2
  - Apache Hadoop 3.2.0
  - Apache Hive   3.1.1
- Massive improvement in detection of URLs.
- New more realistic performance benchmark test
- Renamed DeviceClass "Spy" to "Robot Imitator"
- More consistently add the DeviceBrand to the DeviceName
- Detect Apple iOS Universal Links updater, Netsparker, CasperJs
- Fix the AirWatch scenario

v5.8
===
- Lookup for MS-Edge versions (which are a MESS!)
- Fixed detection Chromium running in a snap on Ubuntu.
- Fixed detection Epiphany (Gnome Web)
- Report the actual version of Edge using a lookup.
- Detection MSOffice, Falkon, QupZilla
- Improved OS detection, added BeOS/Haiku
- Detection Colibri, Midori, Arora, WebKitGTK / luakit, Kodi
- Detection of Android on Sony Bravia TV
- Updated UDF dependencies
  - Apache Flink  1.7.1
  - Apache Beam   2.9.0
  - Apache Drill  1.15.0
  - Apache Hadoop 2.8.5
  - Apache Hive   2.3.4
  - Apache Nifi   1.8.0
  - Logparser     5.2
- Support for serialization using Kryo

v5.7
===
- Fixed exception handling if an illegal TLD is used in the domainname of an email address or website url.

v5.6
===
- Added Flink Table scalar function
- Better extraction of NetType and Language tags as used by Tencent.
- Detect the brand better for many more devices (Blackberry, Huawei, HiSense, HTC, OnePlus, Oppo, QMobile, Wiko)
- Added two new functions for prefix matching lookups.
- Rewrote DeviceBrand detection to improve speed and memory usage.
- Allow setting cache size in Flink, Flink Table and Beam UDFs
- Updated UDF dependencies
  - Apache Flink 1.6.2
  - Apache Beam 2.8.0

v5.5
===
- Fixed the Chrome 70 pattern for many brands
- Detect Alibaba Apps (like DingTalk)

v5.4
===
- Detect more Iron variations
- Major change in the Android Chrome 70 pattern --> broke DeviceBrand
- Detect Vivo brand

v5.3
===
- Detect Iron, Quark and Otter browsers
- Handle the 'too many spaces' effect.
- Fixed Rat checking
- Major update of the UDF for logparser
- Updated dependencies: Jacoco, Spring
- Updated UDF dependencies
  - Apache Flink 1.6.1
  - Apache Beam 2.7.0

v5.2
===
- Documentation website generated with gitbook
- Added layout to web servlet.
- Fixed shading of Guava

v5.1
===
- Massive change of the OS Version for all Windows variants. Much cleaner now.
- Windows NT 7/8/8.1/10 on ARM cpu = Windows Phone --> Mobile device, not Desktop
- Get the DeviceBrand from the URL/Email if no other is available
- Split version field by a '-' too.
- Fix bug in UCBrowser detection (too often reported as Tablet)
- More language-region codes detected
- Many bug fixes found on the input provided by https://github.com/DaimonPl (Thank you)
- Better detection and naming of the Amazon, HP, PocketBook, Manta and Prestigio devices
- Detect the new Gecko fork Goanna (used by Palemoon)
- Updated UDF dependencies
  - Apache Flink 1.6.0
  - Apache Drill 1.14.0
  - Apache Beam 2.6.0
  - Apache Nifi 1.7.1

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
