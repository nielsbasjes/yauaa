Memory usage
============
The system relies heavily on HashMaps to quickly find the rules that need to be fired.

Some fields only require a handful of rules where others have a lot of them.
This means that it depends on the fields that have been requested how many rules are kept in the system and
thus how much memory is used to store the rules in.
To get an idea of the relative memory impact of the rules needed for a specific field.

This table was constructed by running all testcases against the engine where we only request 1 field.
Then after forcing a GC in the JVM we retrieve the memory footprint.
Because there are no rules for the field `__SyntaxError__` we assumed that to be the baseline
against which we determine the relative memory usage.

Because most rules determine several fields there is a lot of overlap in the rules used.
If you keep all rules we see that version 5.0 uses about 232 MiB of memory for all rules which shows that
the most expensive rules related to finding the DeviceName and DeviceBrand because both need
to determine the brand of the device at hand.

| Field | Relative Memory usage  |
| :--- | ---: |
| DeviceClass                     |  81.9 MiB |
| DeviceName                      | 203.2 MiB |
| DeviceBrand                     | 181.2 MiB |
| DeviceCpu                       |   3.1 MiB |
| DeviceCpuBits                   |   9.5 MiB |
| DeviceFirmwareVersion           |   4.1 MiB |
| DeviceVersion                   |  14.3 MiB |
| OperatingSystemClass            |  56.9 MiB |
| OperatingSystemName             |  56.0 MiB |
| OperatingSystemVersion          |  55.9 MiB |
| OperatingSystemNameVersion      |  56.2 MiB |
| OperatingSystemVersionBuild     |   2.2 MiB |
| LayoutEngineClass               |   8.9 MiB |
| LayoutEngineName                |   8.9 MiB |
| LayoutEngineVersion             |   8.9 MiB |
| LayoutEngineVersionMajor        |   8.9 MiB |
| LayoutEngineNameVersion         |   9.0 MiB |
| LayoutEngineNameVersionMajor    |   9.0 MiB |
| LayoutEngineBuild               |   1.5 MiB |
| AgentClass                      |  17.1 MiB |
| AgentName                       |  16.7 MiB |
| AgentVersion                    |  16.7 MiB |
| AgentVersionMajor               |  16.7 MiB |
| AgentNameVersion                |  16.8 MiB |
| AgentNameVersionMajor           |  16.8 MiB |
| AgentBuild                      |   0.5 MiB |
| AgentLanguage                   |   0.3 MiB |
| AgentLanguageCode               |   0.3 MiB |
| AgentInformationEmail           |   4.0 MiB |
| AgentInformationUrl             |   5.9 MiB |
| AgentSecurity                   |   0.3 MiB |
| AgentUuid                       |   0.3 MiB |
| FacebookCarrier                 |   0.2 MiB |
| FacebookDeviceClass             |   0.5 MiB |
| FacebookDeviceName              |   0.5 MiB |
| FacebookDeviceVersion           |   0.5 MiB |
| FacebookFBOP                    |   0.2 MiB |
| FacebookFBSS                    |   0.5 MiB |
| FacebookOperatingSystemName     |   0.5 MiB |
| FacebookOperatingSystemVersion  |   0.5 MiB |
| Anonymized                      |   1.2 MiB |
| HackerAttackVector              |   0.2 MiB |
| HackerToolkit                   |   0.2 MiB |
| KoboAffiliate                   |   0.0 MiB |
| KoboPlatformId                  |   0.0 MiB |
| IECompatibilityVersion          |   1.6 MiB |
| IECompatibilityVersionMajor     |   1.6 MiB |
| IECompatibilityNameVersion      |   1.6 MiB |
| IECompatibilityNameVersionMajor |   1.6 MiB |
| Carrier                         |   0.2 MiB |
| GSAInstallationID               |   0.0 MiB |
| WebviewAppName                  |   1.9 MiB |
| WebviewAppNameVersionMajor      |   1.9 MiB |
| WebviewAppVersion               |   1.9 MiB |
| WebviewAppVersionMajor          |   1.9 MiB |
