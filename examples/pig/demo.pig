-- This is a very simple demo Pig script that has only one purpose: Show how you can use the provided UDF.

-- Import the UDF jar file so this script can use it
REGISTER ../../target/udfs/pig/*-udf.jar;

-- Define a more readable name for the UDF
DEFINE ParseUserAgent  nl.basjes.parse.useragent.pig.ParseUserAgent;

-- Assume this input
-- Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.82 Safari/537.36
rawData =
    LOAD 'testcases.txt'
    USING PigStorage()
    AS  ( useragent: chararray );

UaData =
    FOREACH  rawData
    GENERATE useragent,
             -- Do NOT specify a type for this field as the UDF provides the definitions
             ParseUserAgent(useragent) AS parsedAgent;

--DESCRIBE UaData;

--Output looks like:
--UaData: {
--    useragent: chararray,
--    parsedAgent: (
--        DeviceClass: chararray,
--        DeviceName: chararray,
--        OperatingSystemClass: chararray,
--        OperatingSystemName: chararray,
--        OperatingSystemVersion: chararray,
--        LayoutEngineClass: chararray,
--        LayoutEngineName: chararray,
--        LayoutEngineVersion: chararray,
--        AgentClass: chararray,
--        AgentName: chararray,
--        AgentVersion: chararray,
--        AgentBuild: chararray,
--        AgentInformationEmail: chararray,
--        AgentInformationUrl: chararray,
--        AgentLanguage: chararray,
--        AgentSecurity: chararray,
--        AgentUuid: chararray,
--        DeviceBrand: chararray,
--        DeviceCpu: chararray,
--        DeviceFirmwareVersion: chararray,
--        DeviceVersion: chararray,
--        FacebookCarrier: chararray,
--        FacebookDeviceClass: chararray,
--        FacebookDeviceName: chararray,
--        FacebookDeviceVersion: chararray,
--        FacebookFBOP: chararray,
--        FacebookFBSS: chararray,
--        FacebookOperatingSystemName: chararray,
--        FacebookOperatingSystemVersion: chararray,
--        HackerAttackVector: chararray,
--        HackerToolkit: chararray,
--        KoboAffiliate: chararray,
--        KoboPlatformId: chararray,
--        LayoutEngineBuild: chararray,
--        OperatingSystemVersionBuild: chararray
--    )
--}


DUMP UaData;
--Output looks like:
-- (
--    Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.82 Safari/537.36
--   ,
--   (
--      Desktop,Linux Desktop,
--      Desktop,Linux,Intel x86_64,
--      Browser,AppleWebKit,537.36,
--      Browser,Chrome,48.0.2564.82,
--      ,,,,,,Unknown,Intel x86_64,,,,,,,,,,,,,,,,
--   )
-- )

