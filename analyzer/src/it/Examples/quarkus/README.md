# IMPORTANT: There are some issues loading resources ...
In Quarkus the loading of resources has problems.

See https://github.com/nielsbasjes/yauaa/issues/216 and the reproduction of
the core problem https://github.com/nielsbasjes/BugReport-SpringQuarkus-ResourceLoading

The current status is that this problem is under investigation at Quarkus
- https://github.com/quarkusio/quarkus/issues/10943

The core is that there is something different when dynamically finding and loading resources which are located in subdirectories.

This affects the ability to dynamically find all the rule files and load them.
The resources are found, but because the indicated path is wrong actual loading of the content fails.

It behaves differently if this is
- a module within the Yauaa maven project (then it can find the files as 'file' resources)
- standalone project that includes Yauaa as a dependency (then it finds the files as 'URL' resources with an incorrect path)

When the resource loading problems appear then yauaa will fallback to only load the built in rulesets, so custom rulesets are expected to simply "not work".

With these fallbacks in place (and not loading your own rules) really everything works.

As a consequence you'll see different startup messages depending on either having a normal Java version or a Quarkus modified version.

1. Developer mode
- `mvn quarkus:dev`
1. Java variant
- `mvn clean package`
- `java -jar target/yauaa-example-quarkus-*-runner.jar`
1. Native variant
- `mvn clean package -Pnative -Dquarkus.native.container-build=true`
- `./target/yauaa-example-quarkus-*-runner`

# Using yauaa in your own Quarkus project
## Normal
Include `yauaa` and `jcl-over-slf4j` as a dependency.

## Native
1. Use the quarkus.native.additional-build-args properties as shown in the `pom.xml`.
1. Make sure you copy the `src/main/resources/resources-config.json` to your own project.

Standard Quarkus readme
========================

 ==============================================

# quarkus project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
mvn quarkus:dev
```

## Packaging and running the application

The application can be packaged using `mvn package`.
It produces the `quarkus-*-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/quarkus-*-runner.jar`.

## Creating a native executable

You can create a native executable using: `mvn package -Pnative`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `mvn package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your native executable with: `./target/quarkus-*-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image.

<!--
  ~ Yet Another UserAgent Analyzer
  ~ Copyright (C) 2013-2023 Niels Basjes
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ https://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->
