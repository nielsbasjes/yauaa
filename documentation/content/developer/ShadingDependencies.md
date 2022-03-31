+++
title = "Shading dependencies"
weight = 40
+++
## Introduction
This is a summary of the reasons WHY I have done the shading in this project the way it is now.

If someone has suggestions/hint on how this can be done better I'm really curious what the 'right' way of doing this is.

The base structure of this project is we have a module with the functionality and a set of 'UDFs'
that wrap this functionality so that it can be used in external processing frameworks (like Pig, Flink, Hive, etc.)

## Base goal
This library and the UDFs should be easy to use for all downstream users that want to use this in their projects.

## Problem 1: Problematic dependencies
Some of the dependencies (Antlr4, Spring and SnakeYaml) have proven to be problematic
for downstream users who need different versions of these in the same application.

## Solution 1: Shade and relocate
So for only these we include and relocate the used classes into the main jar.

In the pom.xml

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-shade-plugin</artifactId>

  <configuration>
    <minimizeJar>true</minimizeJar>
    <createDependencyReducedPom>true</createDependencyReducedPom>
    <relocations>
      <relocation>
        <pattern>org.springframework</pattern>
        <shadedPattern>nl.basjes.shaded.org.springframework</shadedPattern>
      </relocation>
      <relocation>
        <pattern>org.antlr</pattern>
        <shadedPattern>nl.basjes.shaded.org.antlr</shadedPattern>
      </relocation>
      <relocation>
        <pattern>org.yaml.snakeyaml</pattern>
        <shadedPattern>nl.basjes.shaded.org.yaml.snakeyaml</shadedPattern>
      </relocation>
    </relocations>
  </configuration>

  <executions>
    <execution>
      <id>inject-problematic-dependencies</id>
      <phase>package</phase>
      <goals>
        <goal>shade</goal>
      </goals>
      <configuration>
        <artifactSet>
          <includes>
            <include>org.antlr:antlr4-runtime</include>
            <include>org.springframework:spring-core</include>
            <include>org.yaml:snakeyaml</include>
          </includes>
        </artifactSet>
      </configuration>
    </execution>

  </executions>
</plugin>
```

## Problem 2: Transitive dependencies
Turns out that a shaded jar still contains the original pom.xml that references the shaded dependencies.
As a consequence the downstream users (like the udfs in this project) still include the entire set of
dependencies (not used by the code) in addition to the shaded versions (used by the code).

This is a known problem in the Maven shade plugin: https://issues.apache.org/jira/browse/MSHADE-36

For which I've put up a pull request: https://github.com/apache/maven-shade-plugin/pull/25

## Solution 2: Inject the dependency-reduced-pom.xml into the final jar
This way building an external project no longer includes things like Antlr a second time.

With the maven-shade-plugin version 3.3.0 (2022-03-24) this is now a built-in feature:

    <useDependencyReducedPomInJar>true</useDependencyReducedPomInJar>

## Problem 3: The other modules look at the original pom.xml
So after solution 2 it is all fine for external projects using the created jar file because they look at
the pom.xml in that jar file.

The remaining problem is that any other module in this (multi module) project will look at the original pom.xml
instead of the cleaned one in the jar file.

As a consequence the Hive UDF jar file contains

```bash
$ unzip -l udfs/hive/target/yauaa-hive-5.12-SNAPSHOT-udf.jar  | fgrep org/springframework/core/io/ResourceLoader.class
  494  2019-08-23 12:26 nl/basjes/shaded/org/springframework/core/io/ResourceLoader.class
  487  2019-02-13 05:32 org/springframework/core/io/ResourceLoader.class
```
I filed a bug report/ missing feature for this in the Maven shade plugin: https://issues.apache.org/jira/browse/MSHADE-326

For which I've put up a pull request: https://github.com/apache/maven-shade-plugin/pull/26

## Solution 3: Manually exclude them
So we exclude these 4 shaded dependencies in all modules in this project so they are no longer included double in the final jars.

## Problem 4: No such classfile ...
Which gives rise to a new problem: When building/developing these modules the code will complain about missing dependencies.
The dependencies have been shaded, relocated and excluded ... which means that any code looking for the 'original'
class name will find it to be missing.

## Solution 4: Include as 'provided'
The final step I had to take was to include these 4 dependencies again as 'provided' in all modules in this project.

## Additional notes
- Immediately setting these dependencies to 'provided' causes them not to be included by the shade plugin.
- Using the optional setting on the dependency caused "missing classes" errors in IntelliJ
- The open issue at the maven/maven-shade-plugin end for problems 3 and 4: https://issues.apache.org/jira/browse/MSHADE-326
