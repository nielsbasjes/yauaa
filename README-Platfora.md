# User Defined Function for Platfora
## Getting the UDF
You can get the prebuild UDF from maven central.
If you use a maven based project simply add this dependency

    <dependency>
      <groupId>nl.basjes.parse.useragent</groupId>
      <artifactId>yauaa</artifactId>
      <classifier>udf</classifier>
      <version>0.6</version>
    </dependency>

Or simply download it via this URL:
http://repo1.maven.org/maven2/nl/basjes/parse/useragent/yauaa/
Then go the the right version and download the file named yauaa-*version*-udf.jar

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
    Copyright (C) 2013-2016 Niels Basjes

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
