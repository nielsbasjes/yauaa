#!/bin/bash

mvn clean package -PEnableReportPlugins jacoco:report
