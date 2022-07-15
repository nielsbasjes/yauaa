+++
title = "The demonstration webservlet"
linkTitle = "Webservlet"
weight = 50
+++
Part of the distribution is a war file that is a servlet that has a webinterface and
some APIs that allow you to try things out.

This servlet can be downloaded via

```xml
<dependency>
  <groupId>nl.basjes.parse.useragent</groupId>
  <artifactId>yauaa-webapp</artifactId>
  <version>{{%YauaaVersion%}}</version>
  <type>war</type>
</dependency>
```

NOTE that this is a **DEMONSTRATION** servlet!

It is simply the library in a servlet, no optimizations or smart memory settings have been done at all.

## Docker
Starting with version 5.14.1 the webservlet is also published to the central docker registry.

- https://hub.docker.com/r/nielsbasjes/yauaa

So with docker installed and running on your (Linux) desktop
you should be able to so something as simple as

```bash
docker pull nielsbasjes/yauaa:{{%YauaaVersion%}}
docker run -p8080:8080 nielsbasjes/yauaa:{{%YauaaVersion%}}
```

and then open

- http://localhost:8080/

in your browser to get the output of the servlet.

## Custom rules

The servlet supports loading your own custom rules, which can be useful to classify internal monitoring systems.
It does this by looking in the folder that start with UserAgents for yaml files (i.e. `file:UserAgents*/*.yaml` ).

Based on the docker image this can be easily done with an additional layer where your entire `Dockerfile` looks like this

```dockerfile
FROM nielsbasjes/yauaa:{{%YauaaVersion%}}
ADD InternalTraffic.yaml UserAgents/
```

When you build that docker image and run it the logging should contain something like this:

    Loading 1 rule files using expression: file:UserAgents*/*.yaml
    - Preparing InternalTraffic.yaml (9608 bytes)
    - Loaded  1 files in   11 msec using expression: file:UserAgents*/*.yaml

## Quarkus based

# Native mode
Will make it crash horribly.

# GraphQL support

```graphql
query {
  yauaa(requestHeaders: {
      userAgent               : "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36"
      secChUa                 : "\".Not/A)Brand\";v=\"99\", \"Google Chrome\";v=\"103\", \"Chromium\";v=\"103\""
      secChUaArch             : "\"x86\""
      secChUaBitness          : "\"64\""
      secChUaFullVersion      : "\"103.0.5060.53\""
      secChUaFullVersionList  : "\".Not/A)Brand\";v=\"99.0.0.0\", \"Google Chrome\";v=\"103.0.5060.53\", \"Chromium\";v=\"103.0.5060.53\""
      secChUaMobile           : "?0"
      secChUaModel            : "\"Basjes was here 3.14\""
      secChUaPlatform         : "\"Linux\""
      secChUaPlatformVersion  : "\"5.13.0\""
      secChUaWoW64            : "?0"
  }) {

     yauaaVersion {
      projectVersion
      gitCommitId
    }

    requestHeaders {
      userAgent
      secChUa
      secChUaArch
      secChUaBitness
      secChUaFullVersion
      secChUaFullVersionList
      secChUaMobile
      secChUaModel
      secChUaPlatform
      secChUaPlatformVersion
      secChUaWoW64
    }

    deviceClass
    deviceName
    deviceBrand
    deviceCpu
    deviceCpuBits
    operatingSystemClass
    operatingSystemName
    operatingSystemVersion
    operatingSystemVersionMajor
    operatingSystemNameVersion
    operatingSystemNameVersionMajor
    layoutEngineClass
    layoutEngineName
    layoutEngineVersion
    layoutEngineVersionMajor
    layoutEngineNameVersion
    layoutEngineNameVersionMajor
    agentClass
    agentName
    agentVersion
    agentVersionMajor
    agentNameVersion
    agentNameVersionMajor
  }
}

```


