+++
title = "Using the analyzer"
weight = 20
linkTitle = "Using Yauaa"
+++
## Using the analyzer
To use this analyzer you can use it either directly in your Java based applications or use one of
the User Defined Functions that are available for many of Apache bigdata tools (Hive, Flink, Beam, ...) as described [here](../udf/).

## Using in Java applications
To use the library you must first add it as a dependency to your application.
The library has been published to [maven central](https://search.maven.org/artifact/nl.basjes.parse.useragent/yauaa/{{%YauaaVersion%}}/jar) so that should work in almost any environment.

If you use a maven based project simply add this dependency to your project.

```xml
<dependency>
    <groupId>nl.basjes.parse.useragent</groupId>
    <artifactId>yauaa</artifactId>
    <version>{{%YauaaVersion%}}</version>
</dependency>
```

To actually use it in your application you need to create an instance of the `UserAgentAnalyzer`.

Please instantiate a new UserAgentAnalyzer as few times as possible because the initialization step for a full UserAgentAnalyzer (i.e. all fields) usually takes something in the range of 2-5 seconds and uses a few hundred MiB of memory to store all the analysis datastructures.

This analyzer can only do a single analysis at a time and to ensure correct working the main method is synchronized. If you have a high load you should simply create multiple instances and spread the load over those instances.
When running in a framework (like [Apache Flink](../udf/Apache-Flink.md) or [Apache Beam](../udf/Apache-Beam.md)) this is usually automatically managed by these frameworks. In other systems may choose something like a threadpool to do that.

Note that if you need multiple instances of the UserAgentAnalyzer then you MUST create a new Builder instance for each of those (or serialize an instance and deserialize it multiple times).

    UserAgentAnalyzer uaa = UserAgentAnalyzer
                .newBuilder()
                .hideMatcherLoadStats()
                .withCache(10000)
                .build();

Then for each useragent (or set of request headers) you call the `parse` method to give you the desired result:

    UserAgent agent = uaa.parse("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.11) Gecko/20071127 Firefox/2.0.0.11");

    for (String fieldName: agent.getAvailableFieldNamesSorted()) {
        System.out.println(fieldName + " = " + agent.getValue(fieldName));
    }

Note that not all fields are available after every parse. So be prepared to receive a `null`, `Unknown` or another field specific default.

## Custom caching implementation
Since version 6.7 you can specify a custom implementation for the cache by providing an instance of the factory interface `CacheInstantiator`.

Do note that Yauaa assumes the caching implementation to be threadsafe.
If you use a non-threadsafe implementation in a multithreaded context it will break.

The default caching implementation uses [Caffeine](https://github.com/ben-manes/caffeine) (since version 6.8).

    return Caffeine.newBuilder().maximumSize(cacheSize).<String, ImmutableUserAgent>build().asMap();

A custom implementation can be specified via the `Builder` using the `withCacheInstantiator(...)` method:

    UserAgentAnalyzer uaa = UserAgentAnalyzer
        .newBuilder()
        .withCacheInstantiator(
            new CacheInstantiator() {
                @Override
                public Map<String, ImmutableUserAgent> instantiateCache(int cacheSize) {
                    return new MyMuchBetterCacheImplementation(cacheSize);
                }
            }
        )
        .withClientHintCacheInstantiator(
            (ClientHintsCacheInstantiator<?>) size ->
                Collections.synchronizedMap(new LRUMap<>(size)))
        .withCache(10000)
        .build();

## Running on Java 8
Yauaa 7.x still allows running on Java 8, yet the default caching library needs Java 11.

If you are still using Java 8 then you can fix this problem by using the `LRUMap` caching implementation that is part of the Apache commons-collections library and do something like this:

    UserAgentAnalyzer uaa = UserAgentAnalyzer
        .newBuilder()
        .useJava8CompatibleCaching()
        .withCache(10000)
        .build();

## Logging dependencies
The Yauaa engine uses Log4j2 as the primary logging framework; yet some of the transitive dependencies
of this project use different logging frameworks.

To minimize the complexity of the dependency handling I have chosen to simply not include ANY logging framework and
expect the consuming system to provide what ever fits best.

So in the end to use this you must provide either an implementation or a bridge for:
- Apache Log4j2
- Apache (Jakarta) Commons logging (like org.apache.commons.logging.LogFactory) aka JCL.

So it all depends on your exact context (i.e. which logging framework are you going to use) what
the best solution is for you to make all of this logging work as intended.

In case you are using Apache Log4j2 you should have these dependencies in addition to Yauaa in your project

    <!-- The default logging implementation for Yauaa -->
    <dependency>
      <groupId>org.apache.logging.log4j</groupId>
      <artifactId>log4j-core</artifactId>
      <version>${log4j2.version}</version>
    </dependency>

    <dependency>
      <groupId>org.apache.logging.log4j</groupId>
      <artifactId>log4j-jcl</artifactId>
      <version>${log4j2.version}</version>
    </dependency>

NOTE: Some of those logging frameworks are only used in specific analysis situations (like robots with a url).
To avoid failing only when such an event occurs a `fail fast` construct has been added:
At the moment of creating the UserAgentAnalyzer (i.e. during the `.build()`) a temporary instance all of these Logging
frameworks are created. If one of them is not present it will immediately throw an `InvalidLoggingDependencyException` (=`RuntimeException`).

## Serialization
If your application needs to serialize the instance of the UserAgentAnalyzer then both the standard Java serialization and
Kryo are supported. Note that with Kryo 5.x you need to register all classes and configure Kryo correctly.

To facilitate doing this correctly the static method `configureKryo` was created.
So in general your code should look something like this:

        Kryo kryo = new Kryo();
        UserAgentAnalyzer.configureKryo(kryo);

Note that both the serializing and the deserializing instance of Kryo must be configured in the same way.

## Avoid using it as a static member
If you make the UserAgentAnalyzer a static member of a class then cleaning it up after use may be a problem.
One case where this happens is in the context of something like Tomcat where a webapp is loaded and then unloaded.
If the analyzer is a static member of your servlet then this unloading may retain a lot of the memory used for the internal
data structures.

## Cache size setting
I recommend you leave the cache to a size that is roughly the unique number of useragents your site finds
in a limited timespan. Something like 15/30/60 minutes usually gives you a fine cache size.
On a very busy website I see ~50K-60K distinct useragents per day and ~10K per hour.
So in my opinion a cache size of 5K-10K elements is a good choice.

## Limiting to only certain fields
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

Do note that some fields need other fields as input (i.e. intermediate result). For example the `DeviceBrand` also needs the `AgentInformationEmail` and `AgentInformationUrl` because in some cases the actual brand is derived from for example the domainname that usually is present in those. So if you only ask for the `DomainBrand` you will get those fields also.

In the nl.basjes.parse.useragent.UserAgent many (not all!!) of the provided variables are provided as a constant String.
You can choose to use these and avoid subtle typos in the requested attribute names.

    uaa = UserAgentAnalyzer
            .newBuilder()
            .withField(DEVICE_CLASS)
            .withField(AGENT_NAME_VERSION_MAJOR)
            .build();

## Building your project with -Xlint:all
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

```xml
<dependency>
    <groupId>com.esotericsoftware</groupId>
    <artifactId>kryo</artifactId>
    <version>5.4.0</version>
    <scope>provided</scope>
</dependency>
```

## IMPORTANT: This library is single threaded !
Because the internal analyzer code is not reentrant the main method has been synchronized on the instance.
So from the perspective of you the application developer this library is thread safe.

If you are in a multi threaded situation you should create a separate instance per thread or accept the speed limitation of the shared synchronized instance.

Note that you should really instantiate it only once per thread (and use a ThreadPool or something similar) because starting a new instance takes several seconds.

## Eclipse users
Be aware of there is a bug in Eclipse which will show you errors in perfectly valid Java code:
https://bugs.eclipse.org/bugs/show_bug.cgi?id=527475
The errors you see are related to the inheritance model used by the Builders in this project and the fact that Eclipse does not interpret it correctly.

## Scala usage
When using this library from a Scala application the way the Builders have been constructed turns out to be very unfriendly to use.

Starting with Yauaa version 5.14 the rewritten builders (contributed by [Robert Stoll](https://github.com/tegonal))
will become available which will make it a lot easier for Scala users to use:

    val uaa = UserAgentAnalyzer.newBuilder
      .withCache(10000)
      .hideMatcherLoadStats
      .withField("DeviceClass")
      .build

## Snapshots

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
