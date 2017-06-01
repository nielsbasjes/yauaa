#!/usr/bin/env bash

( cd .. && mvn clean package -DskipTests=true )
java -jar target/benchmarks.jar
