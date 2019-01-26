Using the analyzer
==================
To use this analyzer you can use it either directly in your Java based applications or use one of 
the User Defined Functions that are available for many of Apache bigdata tools (Pig, Hive, Flink, Beam, ...) as described [here](UDFs.md).

Using in Java applications
==========================
To use the library you must first add it as a dependency to your application.
The library has been published to [maven central](https://search.maven.org/artifact/nl.basjes.parse.useragent/yauaa/{{ book.YauaaVersion }}/jar) so that should work in almost any environment.

If you use a maven based project simply add this dependency to your project.

<pre><code>&lt;dependency&gt;
  &lt;groupId&gt;nl.basjes.parse.useragent&lt;/groupId&gt;
  &lt;artifactId&gt;yauaa&lt;/artifactId&gt;
  &lt;version&gt;{{ book.YauaaVersion }}&lt;/version&gt;
&lt;/dependency&gt;
</code></pre>

and in your application you can use it as simple as this

    UserAgentAnalyzer uaa = UserAgentAnalyzer
                .newBuilder()
                .hideMatcherLoadStats()
                .withCache(10000)
                .build();

    UserAgent agent = uaa.parse("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.11) Gecko/20071127 Firefox/2.0.0.11");

    for (String fieldName: agent.getAvailableFieldNamesSorted()) {
        System.out.println(fieldName + " = " + agent.getValue(fieldName));
    }

Please instantiate a new UserAgentAnalyzer as few times as possible because the initialization step for a full UserAgentAnalyzer (i.e. all fields) usually takes something in the range of 2-5 seconds.
If you need multiple instances of the UserAgentAnalyzer then you MUST create a new Builder instance for each of those.

Note that not all fields are available after every parse. So be prepared to receive a 'null' if you extract a specific name.

# Cache size setting
I recommend you leave the cache to a size that is roughly the unique number of useragents your site finds
in a limited timespan. Something like 15/30/60 minutes usually gives you a fine cache size.
On a very busy website I see ~50K-60K distinct useragents per day and ~10K per hour. 
So in my opinion a cache size of 5K-10K elements is a good choice.

# Limiting to only certain fields
In some scenarios you only want a specific field and all others are unwanted.
This can be achieved by creating the analyzer in Java like this:

    UserAgentAnalyzer uaa;

    uaa = UserAgentAnalyzer
            .newBuilder()
            .withField("DeviceClass")
            .withField("AgentNameVersionMajor")
            .build();

One important effect is that this speeds up the system because it will kick any rules that do not help in getting the desired fields.
The above example showed an approximate 40% speed increase (i.e. times dropped from ~1ms to ~0.6ms).

# IMPORTANT: This library is single threaded !
Because the analyzer code is not reentrant the main method has been synchronized on the instance. 
So if you are in a multi threaded situation you should create a separate instance per thread or accept the speed limitation of the shared instance.
Note that you should really instantiate it only once per thread (and use a ThreadPool or something similar) because starting a new instance takes several seconds.

# Eclipse users 
Be aware of there is a bug in Eclipse which will show you errors in perfectly valid Java code: 
https://bugs.eclipse.org/bugs/show_bug.cgi?id=527475 
The errors you see are related to the inheritance model used by the Builders in this project and the fact that Eclipse does not interpret it correctly. 
