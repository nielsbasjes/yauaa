#!/bin/bash

mvn clean package -DskipTests=true && \
mvn test -PEnableReportPlugins jacoco:report
#mvn clean package -PEnableReportPlugins jacoco:report
