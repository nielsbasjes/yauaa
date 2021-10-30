+++
title = "Memory usage"
weight = 50
+++

The system relies heavily on HashMaps to quickly find the rules that need to be fired.

Some fields only require a handful of rules where others have a lot of them.
This means that it depends on the fields that have been requested how many rules are kept in the system and
thus how much memory is used to store the rules in.
To get an idea of the relative memory impact of the rules needed for a specific field.

This table was constructed by running all testcases against the engine where we only request 1 field.
Then after forcing a GC in the JVM we retrieve the memory footprint.
The DeviceClass field is always extracted and as such can be seen as the baseline against not having
this engine running at all.

Because most rules determine several fields there is a lot of overlap in the rules used.
If you keep all rules we see that version 5.6 uses about 37 MiB of memory for all rules
on top of the rules for the DeviceClass (which is always extracted).

Extracting everything will currently have a memory impact (without caching!) of about 114 MiB

| Field | Relative Memory usage  |
| :--- | ---: |
| DeviceClass *(required)*        |   90.8 MiB |
| DeviceName                      |   10.0 MiB |
| DeviceBrand                     |    9.1 MiB |
| DeviceCpu                       |    0.7 MiB |
| DeviceCpuBits                   |    0.5 MiB |
| DeviceFirmwareVersion           |    1.1 MiB |
| DeviceVersion                   |    0.4 MiB |
| OperatingSystemClass            |    1.2 MiB |
| OperatingSystemName             |    1.3 MiB |
| OperatingSystemVersion          |    1.3 MiB |
| OperatingSystemVersionMajor     |    1.5 MiB |
| OperatingSystemNameVersion      |    2.0 MiB |
| OperatingSystemNameVersionMajor |    2.2 MiB |
| OperatingSystemVersionBuild     |    0.4 MiB |
| LayoutEngineClass               |    2.8 MiB |
| LayoutEngineName                |    2.8 MiB |
| LayoutEngineVersion             |    2.8 MiB |
| LayoutEngineVersionMajor        |    3.0 MiB |
| LayoutEngineNameVersion         |    3.2 MiB |
| LayoutEngineNameVersionMajor    |    3.4 MiB |
| LayoutEngineBuild               |    0.6 MiB |
| AgentClass                      |    5.0 MiB |
| AgentName                       |    5.2 MiB |
| AgentVersion                    |    5.1 MiB |
| AgentVersionMajor               |    5.3 MiB |
| AgentNameVersion                |    5.7 MiB |
| AgentNameVersionMajor           |    5.8 MiB |
| AgentBuild                      |    0.5 MiB |
| AgentLanguage                   |    0.4 MiB |
| AgentLanguageCode               |    0.4 MiB |
| AgentInformationEmail           |    0.1 MiB |
| AgentInformationUrl             |    0.1 MiB |
| AgentSecurity                   |    0.2 MiB |
| AgentUuid                       |    0.3 MiB |
| WebviewAppName                  |    1.0 MiB |
| WebviewAppVersion               |    1.0 MiB |
| WebviewAppVersionMajor          |    1.0 MiB |
| WebviewAppNameVersionMajor      |    1.1 MiB |
| FacebookCarrier                 |    0.2 MiB |
| FacebookDeviceClass             |    0.2 MiB |
| FacebookDeviceName              |    0.2 MiB |
| FacebookDeviceVersion           |    0.2 MiB |
| FacebookFBOP                    |    0.2 MiB |
| FacebookFBSS                    |    0.5 MiB |
| FacebookOperatingSystemName     |    0.5 MiB |
| FacebookOperatingSystemVersion  |    0.5 MiB |
| Anonymized                      |    0.1 MiB |
| HackerAttackVector              |    0.1 MiB |
| HackerToolkit                   |    0.1 MiB |
| KoboAffiliate                   |    0.1 MiB |
| KoboPlatformId                  |    0.1 MiB |
| IECompatibilityVersion          |    0.4 MiB |
| IECompatibilityVersionMajor     |    0.4 MiB |
| IECompatibilityNameVersion      |    0.4 MiB |
| IECompatibilityNameVersionMajor |    0.4 MiB |
| Carrier                         |    0.2 MiB |
| GSAInstallationID               |    0.1 MiB |
| NetworkType                     |    0.1 MiB |
