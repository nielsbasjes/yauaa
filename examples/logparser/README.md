The file hackers-access.log is a sample of the access logs from my home webserver.

This file is in the 'combined' LogFormat

    LogFormat "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"" combined

Although small (3456 lines) this is usefull to do testing on parsing and aggregating the data.

All of these ips have accessed the /join_form URL.
Although it exists this URL is nowhere advertised. 
So only if you already 'know' this url should exist will a visitor try this.
No normal visitor knows this. Not even google knows this. 
So the only people who 'know' this are hackers trying to break in (and fail in this case).
