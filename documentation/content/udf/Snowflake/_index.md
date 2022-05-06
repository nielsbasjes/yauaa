+++
title = "Snowflake"
+++

## Introduction
User Defined Function for [Snowflake](https://snowflake.com).

## STATUS: ... EXPERIMENTAL ...
The Snowflake UDF is very experimental for two reasons:
- Snowflake has marked (last checked on 2021-11-07) [Java based UDFs](https://docs.snowflake.com/en/developer-guide/udf/java/udf-java.html) as a [Preview Feature](https://docs.snowflake.com/en/release-notes/preview-features.html).
- I do not have Snowflake so I do not have any way of testing this other than getting feedback from you.

Thanks to [Luke Ambrosetti](https://github.com/lambrosetti) for helping out here!

See for more information:
- https://docs.snowflake.com/en/developer-guide/udf/java/udf-java.html

## Installation and usage
1. Download the UDF jar to the local file system and upload into a Snowflake internal or external stage.

   You can get the prebuilt UDF from [maven central](https://search.maven.org/artifact/nl.basjes.parse.useragent/yauaa-snowflake/{{%YauaaVersion%}}/jar).

2. Register the function in Snowflake with something like this:
```
create or replace function parse_useragent(useragent VARCHAR)
returns object
language java
imports = ('@cs_stage/yauaa-snowflake-{{%YauaaVersion%}}-udf.jar')
handler='nl.basjes.parse.useragent.snowflake.ParseUserAgent.parse';
```

3. And from there you can use it as a function in your SQL statements
```sql
select parse_useragent('Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36') as ua_obj, ua_obj:AgentClass::string as agent_class;
```

![Using Yauaa in Snowflake](Snowflake.png)


# Using User-Agent Client Hints
With version 7.0.0 you are now able to do this aswell

```sql
select parse_useragent(
    'User-Agent',                   'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36',
    'Sec-Ch-Ua',                    '\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"100\", \"Google Chrome\";v=\"100\"',
    'Sec-Ch-Ua-Arch',               '\"x86\"',
    'Sec-Ch-Ua-Bitness',            '\"64\"',
    'Sec-Ch-Ua-Full-Version',       '\"100.0.4896.127\"',
    'Sec-Ch-Ua-Full-Version-List',  '\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"100.0.4896.127\", \"Google Chrome\";v=\"100.0.4896.127\"',
    'Sec-Ch-Ua-Mobile',             '?0',
    'Sec-Ch-Ua-Model',              '\"\"',
    'Sec-Ch-Ua-Platform',           '\"Linux\"',
    'Sec-Ch-Ua-Platform-Version',   '\"5.13.0\"',
    'Sec-Ch-Ua-Wow64',              '?0'
) as ua_obj, ua_obj:AgentClass::string as agent_class;
```
