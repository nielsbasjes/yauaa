The file hackers-access.log is a sample of the access logs from my home webserver.

This file is in the 'combined' LogFormat

    LogFormat "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"" combined

Although small (3456 lines) this is useful to do testing on parsing and aggregating the data.

All of these ips have accessed the /join_form URL.
Although it exists this URL is nowhere advertised. 
So only if you already 'know' this url should exist will a visitor try this.
No normal visitor knows this. Not even google knows this. 
So the only people who 'know' this are hackers trying to break in (and fail in this case).


Copyright
===
   
    Yet Another UserAgent Analyzer
    Copyright (C) 2013-2019 Niels Basjes
   
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
   
    https://www.apache.org/licenses/LICENSE-2.0
   
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an AS IS BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
   
