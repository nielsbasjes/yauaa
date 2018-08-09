Limitations
===========
This system is based on analyzing the useragent string and looking for the patterns in the useragent string as they have been defined by parties like Google, Microsoft, Samsung and many others. These have been augmented with observations how developers apparently do things. There are really no (ok, very limited) lookup tables that define if a certain device name is a Phone or a Tablet. This makes this system very maintainable because there is no need to have a list of all possible devices.

As a consequence if a useragent does not follow these patterns the analysis will yield the 'wrong' answer.
Take for example these two (both were found exactly as shown here in the logs of a live website):

    Mozilla/5.0 (Linux; Android 5.1; SAMSUNG-T805s Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.94 Mobile Safari/537.36
    Mozilla/5.0 (Linux; Android 4.4.2; SAMSUNG-T805S Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.89 Safari/537.36

The difference between "Mobile Safari" and "Safari" has been defined for Google Chrome as the difference between "Phone" and "Tablet" (see https://developer.chrome.com/multidevice/user-agent ).

And as you can see in this example: we sometimes get it wrong.
The impact in this case is however very limited: Of the 445 visitors I found using this device only 2 were classified wrong all others were correct.

A second example is when the Samsung Browser is installed on a non-Samsung device (in this example a Google Nexus 6):

    Mozilla/5.0 (Linux; Android 7.0; SAMSUNG Nexus 6 Build/NBD92G) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/5.4 Chrome/51.0.2704.106 Mobile Safari/537.36

As you can see this browser assumes it is only installed on Samsung devices so they 'force' the word Samsung in there.
In this case you will see this being reported as a "Samsung Nexus 6", which is obviously wrong.
