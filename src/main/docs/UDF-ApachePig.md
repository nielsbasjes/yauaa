# User Defined Function for Apache Pig

## Getting the UDF
You can get the prebuilt UDF from [maven central](https://search.maven.org/remotecontent?filepath=nl/basjes/parse/useragent/yauaa-pig/{{ book.YauaaVersion }}/yauaa-pig-{{ book.YauaaVersion }}-udf.jar).

If you use a maven based project simply add this dependency

<pre><code>&lt;dependency&gt;
  &lt;groupId&gt;nl.basjes.parse.useragent&lt;/groupId&gt;
  &lt;artifactId&gt;yauaa-pig&lt;/artifactId&gt;
  &lt;classifier&gt;udf&lt;/classifier&gt;
  &lt;version&gt;{{ book.YauaaVersion }}&lt;/version&gt;
&lt;/dependency&gt;
</code></pre>

## Example usage
    -- Import the UDF jar file so this script can use it
    REGISTER ../target/*-udf.jar;

    ------------------------------------------------------------------------
    -- Define a more readable name for the UDF and pass optional parameters
    -- First parameter is ALWAYS the cache size (as a text string!)
    -- The parameters after that are the requested fields.
    ----------
    -- If you simply want 'everything'
    -- DEFINE ParseUserAgent  nl.basjes.parse.useragent.pig.ParseUserAgent;
    ----------
    -- If you just want to set the cache
    -- DEFINE ParseUserAgent  nl.basjes.parse.useragent.pig.ParseUserAgent('10000');
    ----------
    -- If you want to set the cache and only retrieve the specified fields
    DEFINE ParseUserAgent  nl.basjes.parse.useragent.pig.ParseUserAgent('10000', 'DeviceClass', 'DeviceBrand' );

    rawData =
        LOAD 'testcases.txt'
        USING PigStorage()
        AS  ( useragent: chararray );

    UaData =
        FOREACH  rawData
        GENERATE useragent,
                 -- Do NOT specify a type for this field as the UDF provides the definitions
                 ParseUserAgent(useragent) AS parsedAgent;
