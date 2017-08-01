#!/usr/bin/env bash

( cd .. && mvn clean package ) && java -jar target/benchmarks.jar
