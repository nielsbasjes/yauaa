+++
title = "Apache Drill"
+++
## Introduction
This is UDF for [Apache Drill](https://drill.apache.org).
This function was originally created by [Charles S. Givre](https://github.com/cgivre) and was imported into
the main Yauaa project to ensure users would have a prebuilt and up-to-date version available.

This function is now also packaged as part of Apache Drill itself: [documentation](https://github.com/apache/drill/tree/master/contrib/udfs#user-agent-functions).

## Notable changes
With Yauaa 7.0.0
- the code for this UDF has been copied back from the Drill project to ensure it keeps working as expected.
- the `parse_user_agent_field` has been removed and `parse_user_agent` supports the same input/output now.

## Usage
I have copied/implemented the functions

    parse_user_agent ( <useragent> )
    parse_user_agent ( <useragent> , <desired fieldname> )

to support the analysis of the Client Hints it now also supports

    parse_user_agent ( <useragent> , [<header name>,<value>]+ )

or the variant which requires the presense of a `User-Agent` header.

    parse_user_agent ( [<header name>,<value>]+ )

## Installation
By default, Apache Drill has a version of this udf built in (part of the drill-udfs-*.jar)

In order to update the version of Yauaa in your drill installation you'll need to replace the yauaa related jars with the latest versions. This includes not only updating yauaa-{{%YauaaVersion%}}.jar and yauaa-logparser-{{%YauaaVersion%}}.jar but also the rellevant dependencies.

If you want to replace it completely you'll also need to remove the drill-udfs-*.jar and copy the [yauaa-drill-{{%YauaaVersion%}}.jar](https://repo1.maven.org/maven2/nl/basjes/parse/useragent/yauaa-drill/{{%YauaaVersion%}}/yauaa-drill-{{%YauaaVersion%}}.jar) and all relevant dependencies to `<drill-path>/jars/3rdparty`.

Similar:

    mvn -DgroupId=nl.basjes.parse.useragent -DartifactId=yauaa-drill -Dversion={{%YauaaVersion%}} dependency:get -DoutputDirectory="./deps/"


Make sure you replace `<drill-path>` with your actual path to your drill installation.
NOTE: You do not need the `yauaa-drill-{{%YauaaVersion%}}-sources.jar` to run in drill.

# Usage and examples

Working queries with a direct value

    SELECT parse_user_agent('Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.82 Safari/537.36', 'AgentNameVersion') AS ANV from (values(1));

    +---------------------+
    |         ANV         |
    +---------------------+
    | Chrome 48.0.2564.82 |
    +---------------------+

Or for the entire set of all values as a Drill map

    SELECT parse_user_agent('Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.82 Safari/537.36') AS UA from (values(1));

which is shown as a JSon in the commandline client:

    +----------------------------------------------------------------------------------+
    |                                        UA                                        |
    +----------------------------------------------------------------------------------+
    | {"DeviceClass":"Desktop","DeviceName":"Linux Desktop","DeviceBrand":"Unknown","DeviceCpu":"Intel x86_64", "DeviceCpuBits":"64","DeviceFirmwareVersion":"??","DeviceVersion":"??","OperatingSystemClass":"Desktop","OperatingSystemName":"Linux","OperatingSystemVersion":"??","OperatingSystemVersionMajor":"??","OperatingSystemNameVersion":"Linux ??","OperatingSystemNameVersionMajor":"Linux ??","OperatingSystemVersionBuild":"??","LayoutEngineClass":"Browser","LayoutEngineName":"Blink","LayoutEngineVersion":"48.0","LayoutEngineVersionMajor":"48","LayoutEngineNameVersion":"Blink 48.0","LayoutEngineNameVersionMajor":"Blink 48","LayoutEngineBuild":"Unknown","AgentClass":"Browser","AgentName":"Chrome","AgentVersion":"48.0.2564.82","AgentVersionMajor":"48","AgentNameVersion":"Chrome 48.0.2564.82","AgentNameVersionMajor":"Chrome 48","AgentBuild":"Unknown","AgentLanguage":"Unknown","AgentLanguageCode":"Unknown","AgentInformationEmail":"Unknown","AgentInformationUrl":"Unknown","AgentSecurity":"Unknown","AgentUuid":"Unknown","WebviewAppName":"Unknown","WebviewAppVersion":"??","WebviewAppVersionMajor":"??","WebviewAppNameVersion":"Unknown ??","WebviewAppNameVersionMajor":"Unknown ??","FacebookCarrier":"Unknown","FacebookDeviceClass":"Unknown","FacebookDeviceName":"Unknown","FacebookDeviceVersion":"??","FacebookFBOP":"Unknown","FacebookFBSS":"Unknown","FacebookOperatingSystemName":"Unknown","FacebookOperatingSystemVersion":"??","Anonymized":"Unknown","HackerAttackVector":"Unknown","HackerToolkit":"Unknown","KoboAffiliate":"Unknown","KoboPlatformId":"Unknown","IECompatibilityVersion":"??","IECompatibilityVersionMajor":"??","IECompatibilityNameVersion":"Unknown ??","IECompatibilityNameVersionMajor":"Unknown ??","Carrier":"Unknown","GSAInstallationID":"Unknown","NetworkType":"Unknown","__SyntaxError__":"false"} |
    +----------------------------------------------------------------------------------+

The function returns a Drill map, so you can access any of the fields using Drill's table.map.key notation.
For example, the query below illustrates how to extract a field from this map and summarize it:


    SELECT uadata.ua.AgentNameVersion AS Browser,
    COUNT( * ) AS BrowserCount
    FROM (
       SELECT parse_user_agent( columns[0] ) AS ua
       FROM dfs.`/tmp/testcase.tsv`
    ) AS uadata
    GROUP BY uadata.ua.AgentNameVersion
    ORDER BY BrowserCount DESC;

    +----------------------+---------------+
    |       Browser        | BrowserCount  |
    +----------------------+---------------+
    | Chrome 48.0.2564.82  | 1             |
    | Googlebot 2.1        | 1             |
    +----------------------+---------------+

# Full example analyzing with the User-Agent Client Hints

Assume an Apache Httpd webserver with the following LogFormat config:

    LogFormat "%a %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" \"%{Sec-CH-UA}i\" \"%{Sec-CH-UA-Arch}i\" \"%{Sec-CH-UA-Bitness}i\" \"%{Sec-CH-UA-Full-Version}i\" \"%{Sec-CH-UA-Full-Version-List}i\" \"%{Sec-CH-UA-Mobile}i\" \"%{Sec-CH-UA-Model}i\" \"%{Sec-CH-UA-Platform}i\" \"%{Sec-CH-UA-Platform-Version}i\" \"%{Sec-CH-UA-WoW64}i\" %V" combinedhintsvhost

Behind this Apache Httpd webserver is a website that returns the header

    Accept-CH: Sec-CH-UA, Sec-CH-UA-Arch, Sec-CH-UA-Bitness, Sec-CH-UA-Full-Version, Sec-CH-UA-Full-Version-List, Sec-CH-UA-Mobile, Sec-CH-UA-Model, Sec-CH-UA-Platform, Sec-CH-UA-Platform-Version, Sec-CH-UA-WoW64

With all of this in place: these are two of the lines that are found in the access log of this Apache Httpd webserver:

    45.138.228.54 - - [02/May/2022:12:25:10 +0200] "GET / HTTP/1.1" 200 16141 "-" "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36" "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"100\", \"Google Chrome\";v=\"100\"" "\"x86\"" "\"64\"" "\"100.0.4896.127\"" "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"100.0.4896.127\", \"Google Chrome\";v=\"100.0.4896.127\"" "?0" "\"\"" "\"Linux\"" "\"5.13.0\"" "?0" try.yauaa.basjes.nl
    45.138.228.54 - - [02/May/2022:12:25:34 +0200] "GET / HTTP/1.1" 200 15376 "-" "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.0.0 Mobile Safari/537.36" "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"101\", \"Google Chrome\";v=\"101\"" "\"\"" "-" "\"101.0.4951.41\"" "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"101.0.4951.41\", \"Google Chrome\";v=\"101.0.4951.41\"" "?1" "\"Nokia 7.2\"" "\"Android\"" "\"11.0.0\"" "?0" try.yauaa.basjes.nl

For this example the name of this file is `access.hints`

I start by doing a query on this data and ONLY use the User-Agent as the input:

    SELECT  uadata.ua.DeviceClass                AS DeviceClass,
            uadata.ua.AgentNameVersionMajor      AS AgentNameVersionMajor,
            uadata.ua.OperatingSystemNameVersion AS OperatingSystemNameVersion
    FROM (
        SELECT
                parse_user_agent(`request_user-agent`) AS ua
        FROM    table(
                    dfs.`/tmp/access.hints` (
                        type => 'httpd',
                        logFormat => '%a %l %u %t "%r" %>s %b "%{Referer}i" "%{User-Agent}i" "%{Sec-CH-UA}i" "%{Sec-CH-UA-Arch}i" "%{Sec-CH-UA-Bitness}i" "%{Sec-CH-UA-Full-Version}i" "%{Sec-CH-UA-Full-Version-List}i" "%{Sec-CH-UA-Mobile}i" "%{Sec-CH-UA-Model}i" "%{Sec-CH-UA-Platform}i" "%{Sec-CH-UA-Platform-Version}i" "%{Sec-CH-UA-WoW64}i" %V',
                        flattenWildcards => true
                    )
                )
    ) AS uadata;

which produces

    +-------------+-----------------------+----------------------------+
    | DeviceClass | AgentNameVersionMajor | OperatingSystemNameVersion |
    +-------------+-----------------------+----------------------------+
    | Desktop     | Chrome 100            | Linux ??                   |
    | Phone       | Chrome 101            | Android ??                 |
    +-------------+-----------------------+----------------------------+
    2 rows selected (0.183 seconds)

The first example here does not have the exact version of the operating system as part of the User-Agent and this results in `Linux ??`.

The second example shows `Android 10` but was recognized as being a `reduced` variant of the `User-Agent`, this means that the version `10` is an invalid standard value that is not true. So here you see `Android ??`.

Now let's repeat the same and use the recorded `User-Agent Client Hint` header values:

    SELECT  uadata.ua.DeviceClass                AS DeviceClass,
            uadata.ua.AgentNameVersionMajor      AS AgentNameVersionMajor,
            uadata.ua.OperatingSystemNameVersion AS OperatingSystemNameVersion
    FROM (
        SELECT
                parse_user_agent(
                    'User-Agent' ,                  `request_user-agent`,
                    'sec-ch-ua',                    `request_header_sec-ch-ua`,
                    'sec-ch-ua-arch',               `request_header_sec-ch-ua-arch`,
                    'sec-ch-ua-bitness',            `request_header_sec-ch-ua-bitness`,
                    'sec-ch-ua-full-version',       `request_header_sec-ch-ua-full-version`,
                    'sec-ch-ua-full-version-list',  `request_header_sec-ch-ua-full-version-list`,
                    'sec-ch-ua-mobile',             `request_header_sec-ch-ua-mobile`,
                    'sec-ch-ua-model',              `request_header_sec-ch-ua-model`,
                    'sec-ch-ua-platform',           `request_header_sec-ch-ua-platform`,
                    'sec-ch-ua-platform-version',   `request_header_sec-ch-ua-platform-version`,
                    'sec-ch-ua-wow64',              `request_header_sec-ch-ua-wow64`
                ) AS ua
        FROM    table(
                    dfs.`/tmp/access.hints` (
                        type => 'httpd',
                        logFormat => '%a %l %u %t "%r" %>s %b "%{Referer}i" "%{User-Agent}i" "%{Sec-CH-UA}i" "%{Sec-CH-UA-Arch}i" "%{Sec-CH-UA-Bitness}i" "%{Sec-CH-UA-Full-Version}i" "%{Sec-CH-UA-Full-Version-List}i" "%{Sec-CH-UA-Mobile}i" "%{Sec-CH-UA-Model}i" "%{Sec-CH-UA-Platform}i" "%{Sec-CH-UA-Platform-Version}i" "%{Sec-CH-UA-WoW64}i" %V',
                        flattenWildcards => true
                    )
                )
    ) AS uadata;


which produces

    +-------------+-----------------------+----------------------------+
    | DeviceClass | AgentNameVersionMajor | OperatingSystemNameVersion |
    +-------------+-----------------------+----------------------------+
    | Desktop     | Chrome 100            | Linux 5.13.0               |
    | Phone       | Chrome 101            | Android 11.0.0             |
    +-------------+-----------------------+----------------------------+
    2 rows selected (0.275 seconds)

The improvement after adding the Client Hints is evident.
