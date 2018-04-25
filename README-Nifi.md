# User Defined Function for Apache Nifi

## Introduction
This is an Apache Nifi Processor for parsing User Agent Strings.

## Getting the Processor
You can get the prebuilt NAR file of the Processor from [maven central](http://search.maven.org/#search%7Cga%7C1%7Cyauaa-nifi).
This is the maven dependency definition where it can be downloaded.

    <dependency>
        <groupId>nl.basjes.parse.useragent</groupId>
        <artifactId>yauaa-nifi</artifactId>
        <version>4.3</version>
        <type>nar</type>
    </dependency>

## Building
Simply install the normal build tools for a Java project (i.e. maven and jdk 8+) and then simply do:

    mvn clean package

The nar file for Apache Nifi can be found as

    ./udfs/nifi/nifi-nar/target/yauaa-nifi-<version>.nar

## Installation
To install this function put the nar file in the <nifi-path>/lib directory.

    cp ./udfs/nifi/nifi-nar/target/yauaa-nifi-<version>.nar <nifi-path>/lib

Make sure you replace `<nifi-path>` with your actual path to your nifi installation.
After you have added this nar file you will find the ParseUserAgent processor in the list.

![Add Processor dialog](README-Nifi-Add-Processor.png)

# Usage and examples

1. First you make sure that the FLowFile going into this processor has the attribute "UseragentString" that contains the string to be analyzed.

2. In the configuration enable the fields you need for analysis. By default none have been selected.
   ![Configure Processor dialog](README-Nifi-Configure-Processor.png)

3. The output FlowFile will now have additional attributes for all of the selected attributes that are named
   Useragent.SelectedField.
   
       Key: 'Useragent.DeviceClass'
               Value: 'Phone'
       Key: 'Useragent.OperatingSystemNameVersion'
               Value: 'Android 4.1.2'

License
=======
    Yet Another UserAgent Analyzer
    Copyright (C) 2013-2018 Niels Basjes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
