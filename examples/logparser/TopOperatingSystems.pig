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
             group                       AS useragent;

DUMP SumsPerOSName;

--STORE SumsPerOSName
--    INTO  'TopUseragents'
--    USING org.apache.pig.piggybank.storage.CSVExcelStorage('	','NO_MULTILINE', 'UNIX');

