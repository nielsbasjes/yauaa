+++
title = "Client Hints"
weight = 15
+++

## The User-Agent and the User-Agent Client Hints

From about 2019 onward several of the main browsers (Firefox/Chromium/Chrome/Edge/...) have been making steps to reduce the information in the User-Agent. The main reason is that the User-Agents so far have so much detailled information that it became so unique that some could be used as a device id for tracking purposes.

In addition, steps are taken to provide information to website builders that is intended to be sufficient for running a website and less prone to tracking people.

As part of this an extention to the `Client Hints` have been documented and implemented in the Chromium based browsers to provide the `User-Agent Client Hints` via the HTTP request headers.

See:
- https://wicg.github.io/ua-client-hints/#http-ua-hints
- https://web.dev/user-agent-client-hints/
-
## Analyzing the User-Agent Client Hints
Starting with Yauaa 7.0.0 the main Java library supports parsing the available client hints along with the User-Agent.

    UserAgentAnalyzer uaa = UserAgentAnalyzer
        .newBuilder()
        .build();

    Map<String, String> requestHeaders = new TreeMap<>();

    requestHeaders.put("User-Agent",                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.0.0 Safari/537.36");
    requestHeaders.put("Sec-CH-UA",                     "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"100\", \"Google Chrome\";v=\"100\"");
    requestHeaders.put("Sec-CH-UA-Arch",                "\"x86\"");
    requestHeaders.put("Sec-CH-UA-Full-Version-List",   "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"100.0.4896.75\", \"Google Chrome\";v=\"100.0.4896.75\"");
    requestHeaders.put("Sec-CH-UA-Mobile",              "?0");
    requestHeaders.put("Sec-CH-UA-Model",               "\"\"");
    requestHeaders.put("Sec-CH-UA-Platform",            "\"Linux\"");
    requestHeaders.put("Sec-CH-UA-Platform-Version",    "\"5.13.0\"");

    UserAgent userAgent = uaa.parse(requestHeaders);


