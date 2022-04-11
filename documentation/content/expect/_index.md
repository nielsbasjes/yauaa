+++
title = "What to expect"
weight = 10
+++

This library extracts as many as possible fields from the provided User-Agent value and (if available) the provided [Client Hints](https://wicg.github.io/ua-client-hints/).

As an example the useragent of my phone (from a while ago):

```
Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36
```

is converted into this set of fields:

| Field name | Value |
| --- | --- |
|  **Device** Class                         | Phone                |
|  **Device** Name                          | Google Nexus 6       |
|  **Device** Brand                         | Google               |
|  **Operating System** Class               | Mobile               |
|  **Operating System** Name                | Android              |
|  **Operating System** Version             | 7.0                  |
|  **Operating System** Name Version        | Android 7.0          |
|  **Operating System** Version Build       | NBD90Z               |
|  **Layout Engine** Class                  | Browser              |
|  **Layout Engine** Name                   | Blink                |
|  **Layout Engine** Version                | 53.0                 |
|  **Layout Engine** Version Major          | 53                   |
|  **Layout Engine** Name Version           | Blink 53.0           |
|  **Layout Engine** Name Version Major     | Blink 53             |
|  **Agent** Class                          | Browser              |
|  **Agent** Name                           | Chrome               |
|  **Agent** Version                        | 53.0.2785.124        |
|  **Agent** Version Major                  | 53                   |
|  **Agent** Name Version                   | Chrome 53.0.2785.124 |
|  **Agent** Name Version Major             | Chrome 53            |

