+++
title = "Building from source"
weight = 10
+++
## Building
Requirements:
- A Linux class machine (can be a VM).
  - Some of the build scripts rely in bash/sed/grep and related tools, so it will not build on a Windows machine. I'm unsure if it will build on a Mac.
- The normal build tools for a Java project
  - JDK 8, 11, 17, 21 and 23 all need to be installed and defined in the `~/.m2/toolchains.xml`
    - All of these are needed to ensure the code works in all UDFs.
     Some of them only run on Java 8 (like Hive), some (like Flink) only work on Java 11 and some UDFs (like ElasticSearch and Trino) only work on Java 17.

The `./start-docker.sh` script launches a docker based build environment with all needed tools and configs.

and then simply do:

```bash
mvn clean package
```

# Toolchains
This is the content of my `~/.m2/toolchains.xml` on my Ununtu 22.04 LTS machine.

```xml
<?xml version="1.0" encoding="UTF8"?>
<toolchains>
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>8</version>
    </provides>
    <configuration>
      <jdkHome>/usr/lib/jvm/java-8-openjdk-amd64</jdkHome>
    </configuration>
  </toolchain>
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>11</version>
    </provides>
    <configuration>
      <jdkHome>/usr/lib/jvm/java-11-openjdk-amd64</jdkHome>
    </configuration>
  </toolchain>
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>17</version>
    </provides>
    <configuration>
      <jdkHome>/usr/lib/jvm/java-17-openjdk-amd64</jdkHome>
    </configuration>
  </toolchain>
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>21</version>
    </provides>
    <configuration>
      <jdkHome>/usr/lib/jvm/java-21-openjdk-amd64</jdkHome>
    </configuration>
  </toolchain>
</toolchains>
```
