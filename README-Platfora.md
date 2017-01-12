# User Defined Function for Platfora
## Getting the UDF
You can get the prebuild UDF from maven central.
If you use a maven based project simply add this dependency

    <dependency>
      <groupId>nl.basjes.parse.useragent</groupId>
      <artifactId>yauaa-platfora</artifactId>
      <classifier>udf</classifier>
      <version>1.0</version>
    </dependency>

Or simply download it via this URL:
http://repo1.maven.org/maven2/nl/basjes/parse/useragent/yauaa-platfora/
Then go the the right version and download the file named yauaa-platfora-*version*-udf.jar

## Example usage
Once installed you will see two new functions that can be used in computed fields:

To get all fields from the analysis as a Json:

    ANALYZE_USERAGENT_JSON( useragent )

To get a single specific field from the analysis:

    ANALYZE_USERAGENT( useragent , "AgentName" )

Note that due to caching in the parser itself the performance of the latter is expected (not tested yet) to be slightly faster.

## Building
In order to build the Platfora UDF the platfora-udf.jar is needed that is currently only distributed by Platfora as
a file that is part of their installation. This file is normally installed at ${PLATFORA_HOME}/tools/udf/platfora-udf.jar

So to build this UDF you need the platfora-udf.jar in a place where maven can find it.

At the time of writing we were running Platfora 5.2.0 so we chose these values to deploy it as:

    group:    com.platfora.udf
    artifact: platfora-udf
    version:  5.2.0-LOCAL

By deliberately appending 'LOCAL' to the version we aim to avoid naming conflicts in case Platfora decides to put
this jar into a public repo like Maven central.

Installing it locally on your development system can be simply done like this:
( See https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html )

    mvn install:install-file \
        -Dfile=platfora-udf.jar \
        -DgroupId=com.platfora.udf \
        -DartifactId=platfora-udf \
        -Dversion=5.2.0-LOCAL \
        -Dpackaging=jar

Installing it in your corporate maven repo will make things a lot easier for all developers:
https://maven.apache.org/guides/mini/guide-3rd-party-jars-remote.html

Simply install the normal build tools for a Java project (i.e. maven and jdk) and then simply do:

    mvn clean package -Pplatfora

The ./target/yauaa-<version>-udf.jar file will now include the both the Apache Pig and Platfora udfs and also
the file udf-classes.txt to aid in installing it in Platfora.

## Installing
See http://documentation.platfora.com/webdocs/#reference/expression_language/udf/install_udf_class.html

So your IT operations needs a list of all classes that must be registered with Platfora as a UDF
As part of the build a file called udf-classes.txt is generated that contains these classnames.

So with even multiple UDFs installed (that follow this pattern!!) your IT-operations can now do:

    CLASS_LIST=$(
        for UDF_JAR in ${PLATFORA_DATA_DIR}/extlib/*jar ; \
        do \
            unzip -p ${UDF_JAR} udf-classes.txt ;\
        done | sort -u | \
        while read class ; do echo -n "${class}," ; done | sed 's/,$//'
        )

    ${PLATFORA_HOME}/bin/platfora-config set --key platfora.udf.class.names --value ${CLASS_LIST}

    ${PLATFORA_HOME}/bin/platfora-services restart


License
=======
    Yet Another UserAgent Analyzer
    Copyright (C) 2013-2017 Niels Basjes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
