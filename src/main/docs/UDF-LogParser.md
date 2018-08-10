# User Defined Function for LogParser

## Getting the UDF
You can get the prebuilt UDF from [maven central](https://search.maven.org/remotecontent?filepath=nl/basjes/parse/useragent/yauaa-logparser/{{ book.YauaaVersion }}/yauaa-logparser-{{ book.YauaaVersion }}-udf.jar).

If you use a maven based project simply add this dependency

<pre><code>&lt;dependency&gt;
  &lt;groupId&gt;nl.basjes.parse.useragent&lt;/groupId&gt;
  &lt;artifactId&gt;yauaa-logparser&lt;/artifactId&gt;
  &lt;classifier&gt;udf&lt;/classifier&gt;
  &lt;version&gt;{{ book.YauaaVersion }}&lt;/version&gt;
&lt;/dependency&gt;
</code></pre>


## Example usage
    -- Import the UDF jar file so this script can use it
    REGISTER *.jar;

    %declare LOGFILE   'access.log'
    %declare LOGFORMAT '%h %l %u %t "%r" %>s %b "%{Referer}i" "%{User-Agent}i"'

    OSName =
      LOAD '$LOGFILE'
      USING nl.basjes.pig.input.apachehttpdlog.Loader( '$LOGFORMAT'
            , '-load:nl.basjes.parse.useragent.dissector.UserAgentDissector:'
            , 'STRING:request.user-agent.operating_system_name_version'
        ) AS (
            os_name:chararray
        );

    OSNameCount =
        FOREACH  OSName
        GENERATE os_name AS os_name:chararray,
                 1L      AS clicks:long;

    CountsPerOSName =
        GROUeP OSNameCount
        BY    (os_name);

    SumsPerOSName =
        FOREACH  CountsPerOSName
        GENERATE SUM(OSNameCount.clicks) AS clicks,
                 group                   AS useragent;

    DUMP SumsPerOSName;

    --STORE SumsPerOSName
    --    INTO  'TopUseragents'
    --    USING org.apache.pig.piggybank.storage.CSVExcelStorage('	','NO_MULTILINE', 'UNIX');

