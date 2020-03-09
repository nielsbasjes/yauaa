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

# Avoid using it as a static member
If you make the UserAgentAnalyzer a static member of a class then cleaning it up after use may be a problem.
One case where this happens is in the context of something like Tomcat where a webapp is loaded and then unloaded.
If the analyzer is a static member of your servlet then this unloading may retain a lot of the memory used for the internal
data structures.

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

In the nl.basjes.parse.useragent.UserAgent many (not all!!) of the provided variables are provided as a constant String.
You can choose to use these and avoid subtle typos in the requested attribute names.

# Building your project with -Xlint:all
If you are trying to get rid of all possible problems in your application and set the compiler flag -Xlint:all you will see warnings relating to the Kryo serialization system.

    [WARNING] COMPILATION WARNING :
    [INFO] -------------------------------------------------------------
    [WARNING] Cannot find annotation method 'value()' in type 'com.esotericsoftware.kryo.DefaultSerializer': class file for com.esotericsoftware.kryo.DefaultSerializer not found
    [WARNING] Cannot find annotation method 'value()' in type 'com.esotericsoftware.kryo.DefaultSerializer'
    [INFO] 2 warnings

The Yauaa analyzer has been prepared in such a way that it can be serialized using both the standard Java serialization and the Kryo serialization library.
The Kryo library has been configured as a "provided" dependency because many do not need it, and (to avoid version conflicts) those who do need it have this dependency already.

If your project does not use Kryo and you have this warning then there are several ways to work around this:

1. Disable the Xlint check that triggers this warning. Apparently this is the "classfile" so try to use **-Xlint:all,-classfile** instead.
2. Add the otherwise needless Kryo library as a "provided" dependency to your project.

       <dependency>
         <groupId>com.esotericsoftware</groupId>
         <artifactId>kryo</artifactId>
         <version>4.0.2</version>
         <scope>provided</scope>
       </dependency>

#Troubles with logging dependencies
The Yauaa engine uses SLF4J as the primary logging framework.

A few small pieces from Spring are also included and these expect Apache (Jakarta) Commons logging like org.apache.commons.logging.LogFactory.

It all depends on your exact context what the best solution is for you to make all of this logging work as intended.

Essentially you can redirect all JCL logging into SLF4J and then into the actual logging system with something as simple as this

    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>jcl-over-slf4j</artifactId>
      <version>1.7.29</version>
    </dependency>

    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-simple</artifactId>
      <version>1.7.29</version>
    </dependency>

or (if you really have to) can redirect all SLF4J into JCL by adding something like this

    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-jcl</artifactId>
      <version>1.7.29</version>
    </dependency>
    <dependency>
      <groupId>commons-logging</groupId>
      <artifactId>commons-logging</artifactId>
      <version>1.2</version>
    </dependency>

I strongly recommend you check https://www.slf4j.org/legacy.html for more detailed information to find out what is best for your project.

# IMPORTANT: This library is single threaded !
Because the internal analyzer code is not reentrant the main method has been synchronized on the instance.
So from the prespective of you the application developer this library is thread safe.

If you are in a multi threaded situation you should create a separate instance per thread or accept the speed limitation of the shared synchronized instance.

Note that you should really instantiate it only once per thread (and use a ThreadPool or something similar) because starting a new instance takes several seconds.

# Eclipse users
Be aware of there is a bug in Eclipse which will show you errors in perfectly valid Java code:
https://bugs.eclipse.org/bugs/show_bug.cgi?id=527475
The errors you see are related to the inheritance model used by the Builders in this project and the fact that Eclipse does not interpret it correctly.

# Scala usage
When using this library from a Scala application the way the Builders have been constructed turns out to be very unfriendly to use.

Starting with Yauaa version 5.14 the rewritten builders (contributed by [Robert Stoll](https://github.com/tegonal))
will become available which will make it a lot easier for Scala users to use:

    val uaa = UserAgentAnalyzer.newBuilder
      .withCache(10000)
      .hideMatcherLoadStats
      .withField("DeviceClass")
      .build

# Snapshots

Occasionally I publish a snapshot version.
If you want to use such a version then the repository can be configured in your maven with something like this

    <repositories>
      <repository>
        <id>sonatype-oss-snapshots</id>
        <name>Sonatype OSS Snapshots</name>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        <releases><enabled>false</enabled></releases>
        <snapshots><enabled>true</enabled></snapshots>
      </repository>
    </repositories>
