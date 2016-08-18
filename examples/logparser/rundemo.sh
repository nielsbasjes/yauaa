#!/bin/bash
[ -f "httpdlog-pigloader-2.7-udf.jar" ] || wget http://repo1.maven.org/maven2/nl/basjes/parse/httpdlog/httpdlog-pigloader/2.7/httpdlog-pigloader-2.7-udf.jar
rsync ../../udfs/dissector/target/yauaa-logparser-*-udf.jar yauaa-logparser.jar

pig -x local TopOperatingSystems.pig
