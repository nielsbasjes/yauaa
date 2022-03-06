+++
title = "Manipulations"
weight = 40
+++
## Privacy
Useragents have had a lot of information about the device and the browser in it. This has been so detailled in the past that there were many situations where the useragents could be used for tracking visitors very reliably.

### Reducing/Freezing the UserAgent
So a few years ago in several browsers projects started to reduce the level of information in the UserAgent. As a direct consequence the analysis results will become less usefull over time as browsers will start taking away more and more information.

- https://www.chromium.org/updates/ua-reduction
- https://www.chromestatus.com/feature/5704553745874944

The (Q1 2022) DRAFT proposal of the User-Agent Client Hints is intended to contain the information needed in a cleaner way.

- https://wicg.github.io/ua-client-hints/
- https://github.com/WICG/ua-client-hints

At this point in time (Q1 2022):
- This is not a standard yet
- Not all browsers support this
  - Chromium based browsers support this.
  - Firefox 97 does not.

## Compatibility
Also (as I have written a long time ago in [this article](https://techlab.bol.com/making-sense-user-agent-string/)) the UserAgents set values to show to websites what they are compatible with.

In 2021 several browsers stopped updating the version number of the underlying operating system because of compatibility problems with poorly written websites.

Also several browsers are reaching version 100 which makes the version 3 digits; which leads to parsing problems if a website expects a 2 digit version.

This has led to some testing flags for website builders like
[chrome://flags/#force-major-version-to-100](chrome://flags/#force-major-version-to-100)

> **Force major version to 100 in User-Agent**

> Force the Chrome major version in the User-Agent string to 100, which allows testing the 3-digit major version number before the actual M100 release. This flag is only available from M96-M99. – Mac, Windows, Linux, Chrome OS, Android, Fuchsia

and also [chrome://flags/#force-minor-version-to-100](chrome://flags/#force-minor-version-to-100)

> **Force the minor version to 100 in the User-Agent string**

> Force the Chrome minor version in the User-Agent string to 100, which allows testing a 3-digit minor version number. Currently, the minor version is always reported as 0, but due to potential breakage with the upcoming major version 100, this flag allows us to test whether setting the major version in the minor version part of the User-Agent string would be an acceptable alternative. If force-major-version-to-100 is set, then this flag has no effect. See crbug.com/1278459 for details. – Mac, Windows, Linux, Chrome OS, Android, Fuchsia

**Hold on**: They are testing if they can set the major version in the minor version position ... to work around broken websites?

In Chrome/Edge 99 has actually implemented this flag:

[chrome://flags/#force-major-version-to-minor](chrome://flags/#force-major-version-to-minor)

> **Put major version in minor version position in User-Agent**

> Lock the Microsoft Edge major version in the User-Agent string to 99, and force the major version number to the minor version position. This flag is a backup plan for unexpected site-compatibility breakage with a three digit major version. – Mac, Windows, Linux, Chrome OS, Android, Fuchsia

So you get the effect:
- Chrome/99.0.1150.25   = Chrome 99
- Chrome/99.123.1150.25 = Chrome 123

Don't worry: Yauaa actually detects and handles this and reports the correct version.

### There is no MacOS 11
Both Chromium (=Chrome, Edge, ...) and Firefox have frozen the version of MacOS X to 10.15(.7) and as a consequence MacOS 11 ... simply does not appear in any of their UserAgents.
As a consequence these specific versions are reported as unknown version (`??`).

Back ground information:
- Always [10_15_7](https://bugs.chromium.org/p/chromium/issues/detail?id=1175225) since Chrome 90.
- Always [10.15](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/User-Agent/Firefox#macintosh) since Firefox 87.

### There is no Windows 11 in Chromium/Chrome/Edge/...
Microsoft has documented here https://docs.microsoft.com/en-us/microsoft-edge/web-platform/how-to-detect-win11

> There are two approaches for sites to access user agent information:
> - User-Agent strings (legacy).
> - User-Agent Client Hints (recommended).
>
> Websites can differentiate between users on Windows 11 and Windows 10  by using User-Agent Client Hints (UA-CH).`

and

> User-Agent strings won't be updated to differentiate between Windows 11 and Windows 10. We don't recommend using User-Agent strings to retrieve user agent data. Browsers that don't support User-Agent Client Hints won't be able to differentiate between Windows 11 and Windows 10.

And thus the Chrome UserAgent on Windows 11 looks like this:

> Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.51 Safari/537.36

Which clealy says: `Windows 10.0`

### There is no Windows 11 in Firefox
Firefox has an explicit issue called [Cap the Windows version in the User-Agent to 10.0.](https://bugzilla.mozilla.org/show_bug.cgi?id=1693295).

Their [code comment](https://hg.mozilla.org/mozilla-central/rev/3b60746765d3)  clearly describes why
> Cap the reported Windows version to 10.0. This way, Microsoft doesn't
> get to change Web compat-sensitive values without our veto. The
> compat-sensitivity keeps going up as 10.0 stays as the current value
> for longer and longer. If the system-reported version ever changes,
> we'll be able to take our time to evaluate the Web compat impact
> instead of having to scamble to react like happened with macOS
> changing from 10.x to 11.x.
