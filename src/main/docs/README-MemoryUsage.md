Memory usage
============
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

Extracting everything will currently have a memory impact (without caching!) of about 218 MiB

| Field | Relative Memory usage  |
| :--- | ---: |
| DeviceClass *(required)*        |  181.1 MiB |
| DeviceName                      |   10.3 MiB |
| DeviceBrand                     |    9.7 MiB |
| DeviceCpu                       |    1.5 MiB |
| DeviceCpuBits                   |    1.5 MiB |
| DeviceFirmwareVersion           |    3.9 MiB |
| DeviceVersion                   |    0.3 MiB |
| OperatingSystemClass            |    1.2 MiB |
| OperatingSystemName             |    1.3 MiB |
| OperatingSystemVersion          |    1.3 MiB |
| OperatingSystemNameVersion      |    1.6 MiB |
| OperatingSystemVersionBuild     |    0.8 MiB |
| LayoutEngineClass               |    9.7 MiB |
| LayoutEngineName                |    9.7 MiB |
| LayoutEngineVersion             |    9.7 MiB |
| LayoutEngineVersionMajor        |    9.7 MiB |
| LayoutEngineNameVersion         |    9.8 MiB |
| LayoutEngineNameVersionMajor    |    9.8 MiB |
| LayoutEngineBuild               |    3.0 MiB |
| AgentClass                      |   14.3 MiB |
| AgentName                       |   14.3 MiB |
| AgentVersion                    |   14.3 MiB |
| AgentVersionMajor               |   14.3 MiB |
| AgentNameVersion                |   14.5 MiB |
| AgentNameVersionMajor           |   14.5 MiB |
| AgentBuild                      |    0.5 MiB |
| AgentLanguage                   |    0.4 MiB |
| AgentLanguageCode               |    0.4 MiB |
| AgentInformationEmail           |    0.0 MiB |
| AgentInformationUrl             |    0.0 MiB |
| AgentSecurity                   |    0.7 MiB |
| AgentUuid                       |    0.3 MiB |
| WebviewAppName                  |    1.8 MiB |
| WebviewAppVersion               |    1.8 MiB |
| WebviewAppVersionMajor          |    1.8 MiB |
| WebviewAppNameVersionMajor      |    1.8 MiB |
| FacebookCarrier                 |    0.2 MiB |
| FacebookDeviceClass             |    0.2 MiB |
| FacebookDeviceName              |    0.2 MiB |
| FacebookDeviceVersion           |    0.2 MiB |
| FacebookFBOP                    |    0.2 MiB |
| FacebookFBSS                    |    0.4 MiB |
| FacebookOperatingSystemName     |    0.4 MiB |
| FacebookOperatingSystemVersion  |    0.4 MiB |
| Anonymized                      |    0.0 MiB |
| HackerAttackVector              |    0.0 MiB |
| HackerToolkit                   |    0.0 MiB |
| KoboAffiliate                   |    0.0 MiB |
| KoboPlatformId                  |    0.0 MiB |
| IECompatibilityVersion          |    1.6 MiB |
| IECompatibilityVersionMajor     |    1.6 MiB |
| IECompatibilityNameVersion      |    1.6 MiB |
| IECompatibilityNameVersionMajor |    1.6 MiB |
| Carrier                         |    0.2 MiB |
| GSAInstallationID               |    0.0 MiB |
| NetworkType                     |    0.1 MiB |
