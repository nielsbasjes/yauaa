Yauaa: Yet Another UserAgent Analyzer
========================================
[![Github actions Build status](https://img.shields.io/github/actions/workflow/status/nielsbasjes/yauaa/build.yml?branch=main)](https://github.com/nielsbasjes/yauaa/actions)
[![Coverage Status](https://img.shields.io/codecov/c/github/nielsbasjes/yauaa)](https://app.codecov.io/gh/nielsbasjes/yauaa)
[![License](https://img.shields.io/:license-apache-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://img.shields.io/maven-central/v/nl.basjes.parse.useragent/yauaa-parent.svg?label=Maven%20central)](https://central.sonatype.com/namespace/nl.basjes.parse.useragent)
[![Reproducible Builds](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/jvm-repo-rebuild/reproducible-central/master/content/nl/basjes/parse/useragent/yauaa/badge.json)](https://github.com/jvm-repo-rebuild/reproducible-central/blob/master/content/nl/basjes/parse/useragent/yauaa/README.md)
[![GitHub stars](https://img.shields.io/github/stars/nielsbasjes/yauaa?label=GitHub%20stars)](https://github.com/nielsbasjes/yauaa/stargazers)
[![Docker Image](https://img.shields.io/docker/v/nielsbasjes/yauaa/latest?arch=amd64&label=Docker%20image)](https://hub.docker.com/r/nielsbasjes/yauaa)
[![Docker Hub](https://img.shields.io/docker/pulls/nielsbasjes/yauaa?label=Docker%20pulls)](https://hub.docker.com/r/nielsbasjes/yauaa)
[![If this project has business value for you then don't hesitate to support me with a small donation.](https://img.shields.io/badge/Sponsor%20me-via%20Github-darkgreen.svg)](https://github.com/sponsors/nielsbasjes)
[![Website](https://img.shields.io/badge/https://-yauaa.basjes.nl-blue.svg)](https://yauaa.basjes.nl/)

This is a java library that tries to parse and analyze the useragent string (and when available the User-Agent Client Hints) and extract as many relevant attributes as possible.

Works with Java, Scala, Kotlin and provides ready for use UDFs for several processing systems.

The full documentation can be found here [https://yauaa.basjes.nl](https://yauaa.basjes.nl)

Try it!
=======
You can try it online with your own browser here: [https://try.yauaa.basjes.nl/](https://try.yauaa.basjes.nl/).

**NOTES**
1. This runs on a very slow and rate limited machine.
2. If you really like this then run it on your local systems. It's much faster that way.
   A Kubernetes ready Docker image is provided. See this page about the [WebServlet](https://yauaa.basjes.nl/using/webservlet) for more information.

License
=======
I'm publishing this under the Apache 2.0 license because I believe it is the best way to make these kinds of projects available to the world.

But do not underestimate how much work went into this. I have spent over a decade tuning this project to what you have now.

All I want to see in return is a little bit of gratitude from the people who use this.
If you are a home user/hobbyist/small business then a simple star on the project is enough for me. Seeing that people use and like the things I create is what I'm doing this for.
What also really helps are bug reports, headers from real/new devices/browsers that I have net yet seen before and discussions on things you think can be done better.

Despite there not being any obligation (because of the Apache 2.0 license); If you are a big corporation where my code really adds value to the products you make/sell then I would really appreciate it if you could do a small sponsor thing. Buy me lunch (€10), Buy me a game (€100) or what ever you think is the right way to say thank you for the work I have done.

[![If this project has business value for you then don't hesitate to support me with a small donation.](https://img.shields.io/badge/Sponsor%20me-via%20Github-darkgreen.svg)](https://github.com/sponsors/nielsbasjes)

    Yet Another UserAgent Analyzer
    Copyright (C) 2013-2026 Niels Basjes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
