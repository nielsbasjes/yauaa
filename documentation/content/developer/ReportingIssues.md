+++
title = "Reporting issues"
weight = 50
+++
## Introduction
All software has bugs and things that it should do better.

Yauaa is no exception; there are bugs, inaccuracies and there is lots of room for improvement.

So if you find something please report it via the [issue tracker](https://github.com/nielsbasjes/yauaa/issues).

However...

## These are not bugs
I get quite a few bug reports and questions that Yauaa is not extracting the right version number from the provided User-Agent.

Take for example this User-Agent:

    Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.0.0 Safari/537.36

Most people expect to get

    OperatingSystemNameVersion           : 'Windows 10.0'
    AgentNameVersion                     : 'Chrome 100.0.0.0'

but instead they get

    OperatingSystemNameVersion           : 'Windows NT ??'
    AgentNameVersion                     : 'Chrome 100'

and then report that as a bug.

**This is not a bug.**

This example was recorded on a `Windows 7` system and there is nothing in the `User-Agent` to extract this anymore.

There are so many [manipulations and lies]({{< relref "expect/Manipulations.md" >}}) in the `User-Agent`s that simply looking at the User-Agent will yield the wrong answer.
Yauaa will try to give the best possible answer and some classes of lies are reported as such.

So in addition to simply looking at the `User-Agent` it will also overrule these values if a documented manipulations is detected.

## Your best workaround

At this point in time (mid 2022) the best way around much of these [manipulations and lies]({{< relref "expect/Manipulations.md" >}}) is by asking for and recording the [User-Agent Client Hints]({{< relref "using/ClientHints.md" >}}) on your website.

If you ask for these User-Agent Client Hints you can get something like these extra request headers in addition to the `User-Agent` from the browser.

| Header                      | Value                                                                                        |
|:----------------------------|:---------------------------------------------------------------------------------------------|
| Sec-Ch-Ua                   | " Not A;Brand";v="99", "Chromium";v="100", "Google Chrome";v="100"                           |
| Sec-Ch-Ua-Arch              | "x86"                                                                                        |
| Sec-Ch-Ua-Full-Version-List | " Not A;Brand";v="99.0.0.0", "Chromium";v="100.0.4896.75", "Google Chrome";v="100.0.4896.75" |
| Sec-Ch-Ua-Mobile            | ?0                                                                                           |
| Sec-Ch-Ua-Model             | ""                                                                                           |
| Sec-Ch-Ua-Platform          | "Windows"                                                                                    |
| Sec-Ch-Ua-Platform-Version  | "0.1.0"                                                                                      |
| Sec-Ch-Ua-Wow64             | ?0                                                                                           |

With all of this extra information Yauaa can now correctly report

    OperatingSystemNameVersion           : 'Windows 7'
    AgentNameVersion                     : 'Chrome 100.0.4896.75'
