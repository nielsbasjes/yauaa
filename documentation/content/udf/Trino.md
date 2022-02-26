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

## Example usage
This UDF provides a single new function `parse_user_agent(<useragent>)`.

This function needs one input which is the UserAgent string that needs to be analyzed.

The return value is a `map(varchar, varchar)` which is a key value map of all possible properties.

Example:

```sql
SELECT parsedUseragent['DeviceClass']              AS DeviceClass,
       parsedUseragent['AgentNameVersionMajor']    AS AgentNameVersionMajor
FROM (
    SELECT  useragent,
            parse_user_agent(useragent) AS parsedUseragent
    FROM (
        SELECT useragent
        FROM (
            VALUES ('Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36')
        ) AS t (useragent)
    )
);
```

Outputs:
```
 DeviceClass | AgentNameVersionMajor
-------------+-----------------------
 Desktop     | Chrome 98
(1 row)
```
