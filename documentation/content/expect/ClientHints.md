+++
title = "User-Agent Client Hints"
weight = 15
+++

## The User-Agent and the User-Agent Client Hints

From about 2019 onward several of the main browsers (Firefox/Chromium/Chrome/Edge/...) have been making steps to reduce the information in the User-Agent. The main reason is that the User-Agents so far have so much detailled information that it became so unique that some could be used as a device id for tracking purposes.

In addition, steps are taken to provide information to website builders that is intended to be sufficient for running a website and less prone to tracking people.

As part of this an extension to the `Client Hints` have been documented and implemented in the Chromium based browsers to provide the `User-Agent Client Hints` via the HTTP request headers.

See:
- https://wicg.github.io/ua-client-hints/#http-ua-hints
- https://web.dev/user-agent-client-hints/

## Getting the browser to send User-Agent Client Hints
Now the User-Agent Client Hints are provided by the browser in each request to the server via additional request headers.

First important thing is that they will only be send if the server is localhost or over a secured connection (https).

If you try a remote server over plain http you will see no User-Agent Client Hints at all.

By default the browsers that support this will send the "low entropy" values without the need to do anything special (other than going over https).

These headers are

| Request header     | Example value                                                      |
|--------------------|--------------------------------------------------------------------|
| Sec-Ch-Ua          | " Not A;Brand";v="99", "Chromium";v="100", "Google Chrome";v="100" |
| Sec-Ch-Ua-Mobile   | ?0                                                                 |
| Sec-Ch-Ua-Platform | "Windows"                                                          |

If additional headers are desired then the service should send an `Accept-CH` response header with the first response and then any subsequent requests will (if allowed) send the requested additional headers.

    Accept-CH: Sec-CH-UA, Sec-CH-UA-Arch, Sec-CH-UA-Full-Version-List, Sec-CH-UA-Mobile, Sec-CH-UA-Model, Sec-CH-UA-Platform, Sec-CH-UA-Platform-Version, Sec-CH-UA-WoW64

If the additional headers are critical to your application you can send `Critical-CH` in addition of the  `Accept-CH` to indicate which are

    Critical-CH: Sec-CH-UA, Sec-CH-UA-Arch, Sec-CH-UA-Full-Version-List, Sec-CH-UA-Mobile, Sec-CH-UA-Model, Sec-CH-UA-Platform, Sec-CH-UA-Platform-Version, Sec-CH-UA-WoW64


The headers Yauaa can handle are shown in this table.
The shown example values are the real values recorded when running Chrom 100.0.4896.75 with the reduced User-Agent setting enabled on Windows 7.

| Request header                     | Example value                                                                                                    |
|------------------------------------|------------------------------------------------------------------------------------------------------------------|
| User-Agent                         | Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.0.0 Safari/537.36 |
| Sec-Ch-Ua                          | " Not A;Brand";v="99", "Chromium";v="100", "Google Chrome";v="100"                                              |
| Sec-Ch-Ua-Arch                     | "x86"                                                                                                           |
| Sec-Ch-Ua-Full-Version-List        | " Not A;Brand";v="99.0.0.0", "Chromium";v="100.0.4896.75", "Google Chrome";v="100.0.4896.75"                    |
| Sec-Ch-Ua-Mobile                   | ?0                                                                                                              |
| Sec-Ch-Ua-Model                    | ""                                                                                                              |
| Sec-Ch-Ua-Platform                 | "Windows"                                                                                                       |
| Sec-Ch-Ua-Platform-Version         | "0.1.0"                                                                                                         |
| Sec-Ch-Ua-Wow64                    | ?0                                                                                                              |


## Logging the User-Agent Client Hints

If you happen to be using the Apache HTTPD webserver you can record these values with a LogFormat configuration something like this:

    LogFormat "%a %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" \"%{Sec-CH-UA}i\" \"%{Sec-CH-UA-Arch}i\" \"%{Sec-CH-UA-Full-Version-List}i\" \"%{Sec-CH-UA-Mobile}i\" \"%{Sec-CH-UA-Model}i\" \"%{Sec-CH-UA-Platform}i\" \"%{Sec-CH-UA-Platform-Version}i\" %V" combinedhintsvhost

Parsing these logfiles can easily be done with this library: https://github.com/nielsbasjes/logparser

## Analyzing the User-Agent Client Hints
Starting with Yauaa 7.0.0 the main Java library supports parsing the available client hints along with the User-Agent.

In its most basic form you can now do this:

    UserAgentAnalyzer uaa = UserAgentAnalyzer
        .newBuilder()
        .build();

    Map<String, String> requestHeaders = new TreeMap<>();

    requestHeaders.put("User-Agent",                       "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.0.0 Safari/537.36");
    requestHeaders.put("Sec-Ch-Ua",                        "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"100\", \"Google Chrome\";v=\"100\"");
    requestHeaders.put("Sec-Ch-Ua-Arch",                   "\"x86\"");
    requestHeaders.put("Sec-Ch-Ua-Full-Version-List",      "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"100.0.4896.75\", \"Google Chrome\";v=\"100.0.4896.75\"");
    requestHeaders.put("Sec-Ch-Ua-Mobile",                 "?0");
    requestHeaders.put("Sec-Ch-Ua-Model",                  "\"\"");
    requestHeaders.put("Sec-Ch-Ua-Platform",               "\"Windows\"");
    requestHeaders.put("Sec-Ch-Ua-Platform-Version",       "\"0.1.0\"");
    requestHeaders.put("Sec-Ch-Ua-Wow64",                  "?0");

    UserAgent userAgent = uaa.parse(requestHeaders);

this results in (among other things)

      OperatingSystemNameVersion           : 'Windows 7'
      AgentNameVersion                     : 'Chrome 100.0.4896.75'

Although the `User-Agent` contains `Windows NT 10.0` the (correct) answer provided by Yauaa is `Windows 7` because this is the reduced `User-Agent` (all minor versions of Chrome are `0`: `100.0.0.0`) and the Client hints indicate `Windows` and `0.1.0`.

