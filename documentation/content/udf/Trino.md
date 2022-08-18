+++
title = "Trino"
+++
## Introduction
This is a User Defined Function for [Trino](https://trino.io) (a.k.a. Presto SQL)

## STATUS: ... EXPERIMENTAL ...
The Trino plugin is very new.
Please tell if it works or not in your case.

## Installation
You can get the prebuilt UDF from [maven central](https://search.maven.org/remotecontent?filepath=nl/basjes/parse/useragent/yauaa-trino/{{%YauaaVersion%}}/yauaa-trino-{{%YauaaVersion%}}-udf.jar).

In the plugin directory of your Trino server create a subdirectory and copy the `yauaa-trino-{{%YauaaVersion%}}-udf.jar` to that new directory.

In the trino docker image this is `/usr/lib/trino/plugin/` so putting the jar in something like `/usr/lib/trino/plugin/yauaa` is a fine choice.

**Important note:** This directory may only contain this jar file; no other files may be present!

## Usage
This UDF provides two new functions `parse_user_agent(<useragent>)` and `parse_user_agent(array(<parameters>))`.

This first function needs one input which is the UserAgent string that needs to be analyzed.

The return value is a `map(varchar, varchar)` which is a key value map of all possible properties.


The second function needs a list of `header name, value` pairs to define the headers on which the provided values were originally received.

### Example : Just the User-Agent string.

```sql
SELECT parsedUseragent['DeviceClass']                   AS DeviceClass,
       parsedUseragent['AgentNameVersionMajor']         AS AgentNameVersionMajor,
       parsedUseragent['OperatingSystemNameVersion']    AS OperatingSystemNameVersion
FROM (
    SELECT  useragent,
        parse_user_agent(useragent) AS parsedUseragent
    FROM (
        SELECT useragent
        FROM (
            VALUES ('Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.60 Safari/537.36')
        ) AS t (useragent)
    )
);
```

Outputs:
```
 DeviceClass | AgentNameVersionMajor | OperatingSystemNameVersion
-------------+-----------------------+----------------------------
 Desktop     | Chrome 100            | Mac OS >=10.15.7
(1 row)
```

### Example : User-Agent string and ClientHints.

```sql
SELECT parsedUseragent['DeviceClass']                   AS DeviceClass,
       parsedUseragent['AgentNameVersionMajor']         AS AgentNameVersionMajor,
       parsedUseragent['OperatingSystemNameVersion']    AS OperatingSystemNameVersion
FROM (
    SELECT  useragent, chPlatform, chPlatformVersion,
        parse_user_agent(
            ARRAY[
                'user-Agent',                  useragent,
                'sec-CH-UA-Platform',          chPlatform,
                'sec-CH-UA-Platform-Version',  chPlatformVersion
            ]
        ) AS parsedUseragent
    FROM (
        SELECT useragent, chPlatform, chPlatformVersion
        FROM (
            VALUES ('Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.60 Safari/537.36',
                    '"macOS"',
                    '"12.3.1"')
        ) AS t (useragent, chPlatform, chPlatformVersion)
    )
);
```

Outputs:
```
 DeviceClass | AgentNameVersionMajor | OperatingSystemNameVersion
-------------+-----------------------+----------------------------
 Desktop     | Chrome 100            | Mac OS 12.3.1
(1 row)
```
