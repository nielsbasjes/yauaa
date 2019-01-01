--
-- Yet Another UserAgent Analyzer
-- Copyright (C) 2013-2019 Niels Basjes
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
-- https://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an AS IS BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

REGISTER *.jar;

%declare LOGFILE   'hackers-access.log'
%declare LOGFORMAT '%h %l %u %t "%r" %>s %b "%{Referer}i" "%{User-Agent}i"'

OSName =
  LOAD '$LOGFILE'
  USING nl.basjes.pig.input.apachehttpdlog.Loader( '$LOGFORMAT'
    , '-load:nl.basjes.parse.useragent.dissector.UserAgentDissector:'
    , 'STRING:request.user-agent.operating_system_name_version'
    ) AS ( os_name:chararray);

OSNameCount =
    FOREACH  OSName
    GENERATE os_name AS os_name:chararray,
             1L      AS clicks:long;

CountsPerOSName =
    GROUP OSNameCount
    BY    (os_name);

SumsPerOSName =
    FOREACH  CountsPerOSName
    GENERATE SUM(OSNameCount.clicks) AS clicks,
             group                   AS useragent;

DUMP SumsPerOSName;

--STORE SumsPerOSName
--    INTO  'TopUseragents'
--    USING org.apache.pig.piggybank.storage.CSVExcelStorage('	','NO_MULTILINE', 'UNIX');

