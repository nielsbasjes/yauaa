+++
title = "Limitations"
weight = 30
+++
## It only analyzes the provided string
This system is based on analyzing the useragent string and looking for the patterns in the useragent string as they have been defined by parties like Google, Microsoft, Samsung and many others. These have been augmented with observations how developers apparently do things. There are really no (ok, very limited) lookup tables that define if a certain device name is a Phone or a Tablet. This makes this system very maintainable because there is no need to have a list of all possible devices.

As a consequence if a useragent does not follow these patterns the analysis will yield the 'wrong' answer.
Take for example these two (both were found exactly as shown here in the logs of a live website):

    Mozilla/5.0 (Linux; Android 5.1; SAMSUNG-T805s Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.94 Mobile Safari/537.36
    Mozilla/5.0 (Linux; Android 4.4.2; SAMSUNG-T805S Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.89 Safari/537.36

The difference between "Mobile Safari" and "Safari" has been defined for Google Chrome as the difference between "Phone" and "Tablet" (see the [Chrome documentation](https://developer.chrome.com/multidevice/user-agent) on this).

And as you can see in this example: we sometimes get it wrong (The `Samsung T805s` is in reality a Tablet).
The impact in this case is however very limited: Of the 445 visitors I found using this device only 2 were classified wrong all others were correct.

## Unexpected differences
Some cases that seem very similar to the previous example are in a way correct.

Take for example the Samsung Galaxy S8 (SM-G950F) which is a Phone.

In normal use the Useragent looks like this:

    Mozilla/5.0 (Linux; Android 9; SM-G950F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Mobile Safari/537.36

when this device is connected to a computer screen then "Samsung Dex" (Dex = Desktop Experience) takes over and the screen layout and usage of the phone changes dramatically.

    Mozilla/5.0 (Linux; Android 9; SM-G950F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36

This time the change (from `Mobile Safari` to just `Safari`) indicates the device is to be treated as a `Tablet` because the screen size and ways of interacting have changed.

## Apple iPads are a "Desktop"
Since about February 2025 the Apple iPads no longer are reported as a Tablet because their useragent has switched to reporting a Desktop:

A known device which was previously reporting these

    Mozilla/5.0 (iPad; CPU OS 18_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148
    Mozilla/5.0 (iPad; CPU OS 18_3_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko)

is now reporting this

    Mozilla/5.0 (Macintosh; Intel Mac OS 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.0 Safari/605.1.15

This last one is identical to an Apple Mac Desktop and this makes it impossible to determine that this is really a Tablet.

## Some are simply incorrect
When the Samsung Browser is installed on a non-Samsung device (in this example a Google Nexus 6) you get this:

    Mozilla/5.0 (Linux; Android 7.0; SAMSUNG Nexus 6 Build/NBD92G) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/5.4 Chrome/51.0.2704.106 Mobile Safari/537.36

As you can see this browser assumes it is only installed on Samsung devices so they 'force' the word Samsung in there.
In this case you will see this being reported as a "Samsung Nexus 6", which is obviously wrong.

Note that this specific case with the `SamsungBrowser` has now been fixed with a set of additional rules so this one is now correctly reported as a `Google Nexus 6`.

## Device Name and Device Brand
The detection of the brand and name of the device are the most brittle and unreliable part of the output.

There are a few reasons for this:

1. There are a LOT of different devices from a LOT of vendors.
To give you an impression; In november 2018 I did a count on the index page of [DeviceAtlas](https://deviceatlas.com/device-data/devices) and found over 4100 brands and a total of over 55000 different devices.
1. In most cases the useragent string ONLY include the model of the device and NOT the brand. So you need to be able to determine JUST from the model what brand it was.
1. This system tries to limit the number of lookup tables and rely on patterns as much as possible. As a consequence I really do not want to have a complete list of all devices in here. So what are the patterns in the mapping from model to brand?
1. I did an analysis on some of these brands and found that Acer, Lenovo, LG and QMobile all have a device called 'A200'.
So if the useragent only contains 'A200' there is no way to determine what device it really was.

So as a consequence I have chosen to limit this detection to

1. Brands that are included as the first word in the appropriate field.
1. Special cases (like robots)
1. The "most used brands" as good as possible.

**WARNING**: The detection of DeviceBrand will therefore never be complete and accurate.
