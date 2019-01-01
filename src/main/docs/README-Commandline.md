# Commandline tool

## Warning
This commandline tool is unfinished and has some problems reading files and such.

So at this time: **BETA** quality.

## Getting the commandline tool
You can get the prebuild jar from maven central.
If you use a maven based project simply add this dependency

    <dependency>
      <groupId>nl.basjes.parse.useragent</groupId>
      <artifactId>yauaa-commandline</artifactId>
      <version>5.1</version>
    </dependency>

## Building
Simply install the normal build tools for a Java project (i.e. maven and jdk) and then simply do:

    mvn clean package

## Example usage

# Getting commandline help

    $ java -jar yauaa-commandline-*.jar 
    
    /------------------------------------------------------------\
    | Yauaa 2.0-SNAPSHOT (v1.4-63 @ 2017-08-05T07:22:55Z)        |
    +------------------------------------------------------------+
    | For more information: https://github.com/nielsbasjes/yauaa |
    | Copyright (C) 2013-2019 Niels Basjes - License Apache 2.0  |
    \------------------------------------------------------------/
    
    Errors: No input specified.
    
    Usage: java jar <jar containing this class> <options>
     -bad             : Output only cases that have a problem (default: false)
     -cache N         : The number of elements that can be cached (LRU). (default:
                        10000)
     -csv             : Output in csv format (default: false)
     -debug           : Set to enable debugging. (default: false)
     -fields STRING[] : A list of the desired fieldnames (use 'Useragent' if you
                        want the input value aswell)
     -fullFlatten     : Set to flatten each parsed agent string. (default: false)
     -in VAL          : Location of input file
     -json            : Output in json format (default: false)
     -matchedFlatten  : Set to get the flattened values that were relevant for the
                        Matchers. (default: false)
     -ua VAL          : A single useragent string
     -yaml            : Output in yaml testcase format (default: false)

You will get the processing result via stdout and some informational stuff via stderr.


# Doing a single value

Note that this is relatively slow because the full 2-3 seconds of startup overhead is incurred each time you start this.

    AGENT="Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36"

    java -jar yauaa-commandline-*.jar -yaml -ua "${AGENT}" > results.yaml

    java -jar yauaa-commandline-*.jar -json -ua "${AGENT}" > results.json

# Doing a large number of values

For streaming a file in it is recommended to use stdin like this

    cat useragents.txt | java -jar yauaa-commandline-*.jar -json -in - > results.json

Running in this mode will also show some statistics via stderr.

    /------------------------------------------------------------\
    | Yauaa 2.0-SNAPSHOT (v1.4-63 @ 2017-08-05T07:22:55Z)        |
    +------------------------------------------------------------+
    | For more information: https://github.com/nielsbasjes/yauaa |
    | Copyright (C) 2013-2019 Niels Basjes - License Apache 2.0  |
    \------------------------------------------------------------/
    
    Loading from: "classpath*:UserAgents/**/*.yaml"
    Loaded 56 files
    Building all matchers
    Building 2314 (dropped    0) matchers from   52 files took  3278 msec resulted in   243070 hashmap entries
    Analyzer stats
    Lookups      : 24
    Matchers     : 2314 (total:2314 ; dropped: 0)
    Hashmap size : 243070
    Ranges map size : 5441
    Testcases    : 771
    Lines =     1000 (Ambiguities:    75 ; SyntaxErrors:     0) Analyze speed =   357/sec.
    Lines =     2000 (Ambiguities:    66 ; SyntaxErrors:     0) Analyze speed =   472/sec.
    Lines =     3000 (Ambiguities:    60 ; SyntaxErrors:     0) Analyze speed =   838/sec.
    Lines =     4000 (Ambiguities:    54 ; SyntaxErrors:     0) Analyze speed =   973/sec.
    Lines =     5000 (Ambiguities:    60 ; SyntaxErrors:     1) Analyze speed =   995/sec.
    Lines =     6000 (Ambiguities:    55 ; SyntaxErrors:     0) Analyze speed =  1153/sec.
    Lines =     7000 (Ambiguities:    56 ; SyntaxErrors:     0) Analyze speed =  1071/sec.
    Lines =     8000 (Ambiguities:    70 ; SyntaxErrors:     1) Analyze speed =  1015/sec.
    Lines =     9000 (Ambiguities:    70 ; SyntaxErrors:     0) Analyze speed =  1053/sec.
    Lines =    10000 (Ambiguities:    74 ; SyntaxErrors:     0) Analyze speed =  1202/sec.
    Lines =    11000 (Ambiguities:    42 ; SyntaxErrors:     0) Analyze speed =  1247/sec.
    Lines =    12000 (Ambiguities:    68 ; SyntaxErrors:     0) Analyze speed =  1204/sec.
    Lines =    13000 (Ambiguities:    37 ; SyntaxErrors:     0) Analyze speed =  1223/sec.
    Lines =    14000 (Ambiguities:    66 ; SyntaxErrors:     0) Analyze speed =  1164/sec.
    Lines =    15000 (Ambiguities:    43 ; SyntaxErrors:     0) Analyze speed =  1224/sec.
    -------------------------------------------------------------
    Performance: 15402 in 17 sec --> 897/sec
    -------------------------------------------------------------
    Parse results of 15402 lines
    Parsed without error:    15400 (= 99.99%)
    Parsed with    error:        2 (=  0.01%)
    Fully matched       :     2642 (= 17.15%)
    -------------------------------------------------------------
    Parse results of 996935 hits
    Parsed without error:   996917 (=100.00%)
    Parsed with    error:       18 (=  0.00%)
    Fully matched       :   248862 (= 24.96%)
    -------------------------------------------------------------
