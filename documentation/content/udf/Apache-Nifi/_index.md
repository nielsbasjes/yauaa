+++
title = "Apache Nifi"
+++
## Introduction
This is a User Defined Function for [Apache Nifi](https://nifi.apache.org)

## Introduction
This is an Apache Nifi Processor for parsing User Agent Strings.

## Getting the Processor

You can get the prebuilt NAR file from [maven central](https://repo1.maven.org/maven2/nl/basjes/parse/useragent/yauaa-nifi/{{%YauaaVersion%}}/yauaa-nifi-{{%YauaaVersion%}}.nar).

If you use a maven based project simply add this dependency

```xml
<dependency>
  <groupId>nl.basjes.parse.useragent</groupId>
  <artifactId>yauaa-nifi</artifactId>
  <type>nar</type>
  <version>{{%YauaaVersion%}}</version>
</dependency>
```

## Installation
To install this function put the nar file in the `<nifi-path>/lib` directory.

    cp ./udfs/nifi/nifi-nar/target/yauaa-nifi-<version>.nar <nifi-path>/lib

Make sure you replace `<nifi-path>` with your actual path to your nifi installation.
After you have added this nar file you will find the ParseUserAgent processor in the list.

![Add Processor dialog](ApacheNifi-Add-Processor.png)

## Usage and examples

1. First you make sure that the FlowFile going into this processor has the attributes needed as input.

2. In the configuration specify which attributes contain the values of the Request Headers that were logged. The only mandatory one is `RequestHeader.UserAgent`. The other properties refer to the original [User-Agent Client Hints](https://wicg.github.io/ua-client-hints/#http-ua-hints) request header names.
   ![Configure Processor dialog](ApacheNifi-Configure-Processor-1.png)

3. In the configuration enable the fields you need for analysis. By default none have been selected.
   ![Configure Processor dialog](ApacheNifi-Configure-Processor-2.png)

4. The output FlowFile will now have additional attributes for all of the selected attributes that are named
   Useragent.SelectedField.

       Key: 'Useragent.DeviceClass'
               Value: 'Phone'
       Key: 'Useragent.OperatingSystemNameVersion'
               Value: 'Android 4.1.2'

   In this log example the `XXsomethingXX` attributes are the input values and the `Useragent.something` are the outputs:
   ![Output from LogAttributes](ApacheNifi-Output.png)
