+++
title = "User-Agent Client Hints"
weight = 15
+++

## The User-Agent and the User-Agent Client Hints

From about 2019 onward several of the main browsers (Firefox/Chromium/Chrome/Edge/...) have been making steps to reduce the information in the User-Agent. The main reason is that the User-Agents so far have so much detailed information that it became so unique that some could be used as a device id for tracking purposes.

In addition, steps are taken to provide information to website builders that is intended to be sufficient for running a website and less prone to tracking people.

As part of this an extension to the `Client Hints` have been documented and implemented in the Chromium based browsers to provide the `User-Agent Client Hints` via the HTTP request headers.

See:
- https://wicg.github.io/ua-client-hints/#http-ua-hints
- https://web.dev/user-agent-client-hints/
- https://docs.microsoft.com/en-us/microsoft-edge/web-platform/user-agent-guidance
- https://docs.microsoft.com/en-us/microsoft-edge/web-platform/how-to-detect-win11
- https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Sec-CH-UA#directives

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

    Accept-CH: Sec-CH-UA, Sec-CH-UA-Arch, Sec-CH-UA-Bitness, Sec-CH-UA-Full-Version, Sec-CH-UA-Full-Version-List, Sec-CH-UA-Mobile, Sec-CH-UA-Model, Sec-CH-UA-Platform, Sec-CH-UA-Platform-Version, Sec-CH-UA-WoW64

If the additional headers are critical to your application you can send `Critical-CH` in addition of the  `Accept-CH` to indicate which are

    Critical-CH: Sec-CH-UA, Sec-CH-UA-Arch, Sec-CH-UA-Bitness, Sec-CH-UA-Full-Version, Sec-CH-UA-Full-Version-List, Sec-CH-UA-Mobile, Sec-CH-UA-Model, Sec-CH-UA-Platform, Sec-CH-UA-Platform-Version, Sec-CH-UA-WoW64

See:
- https://chromestatus.com/feature/5727177800679424
- https://github.com/WICG/client-hints-infrastructure/blob/main/reliability.md

Depending on the situation you may need to also set a `Permissions-Policy` HTTP header to actually get the desired headers.
- https://www.w3.org/TR/permissions-policy/
- https://github.com/w3c/webappsec-permissions-policy/blob/main/permissions-policy-explainer.md
- https://github.com/w3c/webappsec-permissions-policy/issues/129

The headers Yauaa can handle are shown in this table.

The shown example values are the real values recorded when running Chrome 100.0.4896.75 with the reduced User-Agent setting enabled on Windows 7.

| Request header              | Example value                                                                                                   |
|-----------------------------|-----------------------------------------------------------------------------------------------------------------|
| User-Agent                  | Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.0.0 Safari/537.36 |
| Sec-Ch-Ua                   | " Not A;Brand";v="99", "Chromium";v="100", "Google Chrome";v="100"                                              |
| Sec-Ch-Ua-Arch              | "x86"                                                                                                           |
| Sec-Ch-Ua-Bitness           | "64"                                                                                                            |
| Sec-CH-Ua-Full-Version      | "100.0.4896.75"                                                                                                 |
| Sec-Ch-Ua-Full-Version-List | " Not A;Brand";v="99.0.0.0", "Chromium";v="100.0.4896.75", "Google Chrome";v="100.0.4896.75"                    |
| Sec-Ch-Ua-Mobile            | ?0                                                                                                              |
| Sec-Ch-Ua-Model             | ""                                                                                                              |
| Sec-Ch-Ua-Platform          | "Windows"                                                                                                       |
| Sec-Ch-Ua-Platform-Version  | "0.1.0"                                                                                                         |
| Sec-Ch-Ua-Wow64             | ?0                                                                                                              |


## Logging the User-Agent Client Hints

If you happen to be using the Apache HTTPD webserver you can record these values with a LogFormat configuration something like this:

    LogFormat "%a %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" \"%{Sec-CH-UA}i\" \"%{Sec-CH-UA-Arch}i\" \"%{Sec-CH-UA-Bitness}i\" \"%{Sec-CH-UA-Full-Version}i\" \"%{Sec-CH-UA-Full-Version-List}i\" \"%{Sec-CH-UA-Mobile}i\" \"%{Sec-CH-UA-Model}i\" \"%{Sec-CH-UA-Platform}i\" \"%{Sec-CH-UA-Platform-Version}i\" \"%{Sec-CH-UA-WoW64}i\" %V" combinedhintsvhost

Behind this Apache Httpd webserver is a website that returns the header

    Accept-CH: Sec-CH-UA, Sec-CH-UA-Arch, Sec-CH-UA-Bitness, Sec-CH-UA-Full-Version, Sec-CH-UA-Full-Version-List, Sec-CH-UA-Mobile, Sec-CH-UA-Model, Sec-CH-UA-Platform, Sec-CH-UA-Platform-Version, Sec-CH-UA-WoW64

With all of this in place: these are two of the lines that are found in the access log of this Apache Httpd webserver:

    45.138.228.54 - - [02/May/2022:12:25:10 +0200] "GET / HTTP/1.1" 200 16141 "-" "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36" "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"100\", \"Google Chrome\";v=\"100\"" "\"x86\"" "\"64\"" "\"100.0.4896.127\"" "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"100.0.4896.127\", \"Google Chrome\";v=\"100.0.4896.127\"" "?0" "\"\"" "\"Linux\"" "\"5.13.0\"" "?0" try.yauaa.basjes.nl
    45.138.228.54 - - [02/May/2022:12:25:34 +0200] "GET / HTTP/1.1" 200 15376 "-" "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.0.0 Mobile Safari/537.36" "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"101\", \"Google Chrome\";v=\"101\"" "\"\"" "-" "\"101.0.4951.41\"" "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"101.0.4951.41\", \"Google Chrome\";v=\"101.0.4951.41\"" "?1" "\"Nokia 7.2\"" "\"Android\"" "\"11.0.0\"" "?0" try.yauaa.basjes.nl

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

## Which client hints do I really need?
A bit deeper dive into the fields and their "added value" and why to request and keep them (or not).

General considerations:
- The first request only has the low entropy headers `Sec-Ch-Ua`, `Sec-Ch-Ua-Mobile` and `Sec-Ch-Ua-Platform`. Only later requests can have more headers if requested and allowed.
- With the reduction/freeze of the useragents several parts are now static and no longer show correct values. This is most obvious are the version of the browser (only the major version), the device brand & name (absent on mobile) and the version of the operating system (i.e. always `Windows 10.0`, `Android 10` or `Mac OS X 10_15_7` ). This is where client hints do contain the correct values.

| Client hint                 | Example                                                                                      | Keep it? | Why                                                                                                                                      |
|-----------------------------|----------------------------------------------------------------------------------------------|----------|------------------------------------------------------------------------------------------------------------------------------------------|
| Sec-Ch-Ua                   | " Not A;Brand";v=“99”, “Chromium”;v=“100”, “Google Chrome”;v=“100”                           | Yes      | You may not have the Sec-Ch-Ua-Full-Version-List with the exact versions.                                                                |
| Sec-Ch-Ua-Full-Version-List | " Not A;Brand";v=“99.0.0.0”, “Chromium”;v=“100.0.4896.75”, “Google Chrome”;v=“100.0.4896.75” | Yes      | Is the better variant of Sec-Ch-Ua but it may not be present.                                                                            |
| Sec-CH-Ua-Full-Version      | “100.0.4896.75”                                                                              | No       | This field is deprecated in the standard. Also this info is also present in the Sec-Ch-Ua-Full-Version-List.                             |
| Sec-Ch-Ua-Mobile            | ?0                                                                                           | Yes      | In the (very rare) case where we cannot determine if it is a phone or tablet this flag determines the end result.                        |
| Sec-Ch-Ua-Platform          | “Windows”                                                                                    | Yes      | Needed in the very common case of bad version info in the useragent.                                                                     |
| Sec-Ch-Ua-Platform-Version  | “0.1.0”                                                                                      | Yes      | Needed in the very common case of bad version info in the useragent. This "0.1.0" means "Windows 7" because the Platform says "Windows". |
| Sec-Ch-Ua-Arch              | “x86”                                                                                        | Yes      | The only way to determine a MacOS system is running an M1/M2 (ARM) instead of an Intel CPU                                               |
| Sec-Ch-Ua-Bitness           | “64”                                                                                         | Yes      | Often not present in the useragent                                                                                                       |
| Sec-Ch-Ua-Model             | "Nokia 7.2"                                                                                  | Yes      | Often not present in the useragent (brand and device info).                                                                              |
| Sec-Ch-Ua-Wow64             | ?0                                                                                           | No       | The only thing this says is that this is Windows (use Platform) and that it is 32 bit software running on a 64 bit system.               |

To simplify it all I would
- Ask for all of them (regardless if I say No above) because the browsers are currently in flux in what they put in these fields.
- Persist all headers starting with `Sec-Ch-Ua` along with the `User-Agent`. The assumption is that later versions of browsers will change what they put in these fields which may yield possible analysis improvements.
