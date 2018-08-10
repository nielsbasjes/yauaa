# User Defined Function for Apache Drill

## Introduction
This is an Apache Drill UDF for parsing User Agent Strings.
This function was originally created by [Charles S. Givre](https://github.com/cgivre) and was imported into
the main Yauaa project to ensure users would have a prebuilt and up to date version available.

# STATUS: ... Works on my machine ...
I have copied/implemented the functions

    parse_user_agent       ( <useragent> )
    parse_user_agent_field ( <useragent> , <desired fieldname> )

Able to do basic unit test during build for the parse_user_agent_field function.
NOT yet able to do automated testing for the parse_user_agent function.

When I run it in a local drill the things I try work for the most (see below).
I have not yet tested this in a distributed setup.

So please try it out and report any issues and/or improvements you see.

## Getting the UDF
You can get the prebuilt UDF from [maven central](https://search.maven.org/artifact/nl.basjes.parse.useragent/yauaa-drill/{{ book.YauaaVersion }}/jar).

If you use a maven based project simply add this dependency to your project.

<pre><code>&lt;dependency&gt;
  &lt;groupId&gt;nl.basjes.parse.useragent&lt;/groupId&gt;
  &lt;artifactId&gt;yauaa-drill&lt;/artifactId&gt;
  &lt;version&gt;{{ book.YauaaVersion }}&lt;/version&gt;
&lt;/dependency&gt;
</code></pre>

## Installation
To install this function put the jar file in the <drill-path>/jars/3rdparty directory.

    cp yauaa-drill-<version>.jar <drill-path>/jars/3rdparty

Make sure you replace `<drill-path>` with your actual path to your drill installation.
NOTE: You do not need the `yauaa-drill-<version>-sources.jar` to run in drill.

# Usage and examples

Working queries with a direct value

    SELECT parse_user_agent_field('Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.82 Safari/537.36', 'AgentNameVersion') from (values(1));

    +----------------------+
    |        EXPR$0        |
    +----------------------+
    | Chrome 48.0.2564.82  |
    +----------------------+


This one doesn't work

    SELECT parse_user_agent('Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.82 Safari/537.36') from (values(1));

    Error: PLAN ERROR: Failure while materializing expression in constant expression evaluator [PARSE_USER_AGENT('Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.82 Safari/537.36')].  Errors:
    Error in expression at index -1.  Error: Only ProjectRecordBatch could have complex writer function. You are using complex writer function parse_user_agent in a non-project operation!.  Full expression: --UNKNOWN EXPRESSION--.

Assume the file ```/tmp/testcase.tsv``` which contains 2 lines:

    Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.82 Safari/537.36
    Mozilla/5.0 (iPhone; CPU iPhone OS 8_3 like Mac OS X) AppleWebKit/600.1.4 (KHTML, like Gecko) Version/8.0 Mobile/12F70 Safari/600.1.4 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)

Working queries using the input file

    SELECT parse_user_agent_field(columns[0], 'AgentNameVersion') AS AgentNameVersion from dfs.`/tmp/testcase.tsv`;

    +----------------------+
    |   AgentNameVersion   |
    +----------------------+
    | Chrome 48.0.2564.82  |
    | Googlebot 2.1        |
    +----------------------+

and

    SELECT parse_user_agent(columns[0]) AS AgentNameVersion from dfs.`/tmp/testcase.tsv`;

    +------------------+
    | AgentNameVersion |
    +------------------+
    | {
        "DeviceClass":                   "Desktop",
        "DeviceName":                    "Linux Desktop",
        "DeviceBrand":                   "Unknown",
        "DeviceCpu":                     "Intel x86_64",
        "DeviceCpuBits":                 "64",
        "OperatingSystemClass":          "Desktop",
        "OperatingSystemName":           "Linux",
        "OperatingSystemVersion":        "Intel x86_64",
        "OperatingSystemNameVersion":    "Linux Intel x86_64",
        "LayoutEngineClass":             "Browser",
        "LayoutEngineName":              "Blink",
        "LayoutEngineVersion":           "48.0",
        "LayoutEngineVersionMajor":      "48",
        "LayoutEngineNameVersion":       "Blink 48.0",
        "LayoutEngineNameVersionMajor":  "Blink 48",
        "AgentClass":                    "Browser",
        "AgentName":                     "Chrome",
        "AgentVersion":                  "48.0.2564.82",
        "AgentVersionMajor":             "48",
        "AgentNameVersion":              "Chrome 48.0.2564.82",
        "AgentNameVersionMajor":         "Chrome 48"                        } |
    | {
        "DeviceClass":                   "Robot Mobile",
        "DeviceName":                    "Google",
        "DeviceBrand":                   "Google",
        "OperatingSystemClass":          "Cloud",
        "OperatingSystemName":           "Google",
        "OperatingSystemVersion":        "Google",
        "OperatingSystemNameVersion":    "Google",
        "LayoutEngineClass":             "Robot",
        "LayoutEngineName":              "Googlebot",
        "LayoutEngineVersion":           "Googlebot",
        "LayoutEngineVersionMajor":      "Googlebot",
        "LayoutEngineNameVersion":       "Googlebot",
        "LayoutEngineNameVersionMajor":  "Googlebot",
        "AgentClass":                    "Robot Mobile",
        "AgentName":                     "Googlebot",
        "AgentVersion":                  "2.1",
        "AgentVersionMajor":             "2",
        "AgentNameVersion":              "Googlebot 2.1",
        "AgentNameVersionMajor":         "Googlebot 2",
        "AgentInformationUrl":           "http://www.google.com/bot.html"   } |
    +------------------+

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
