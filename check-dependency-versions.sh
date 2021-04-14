#!/bin/bash

( cd devtools && mvn install -PskipQuality )
echo "==============================="
echo "===- CHECKING DEPENDENCIES -==="
echo "vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv"
# NOTE: WE IGNORE UPDATE MESSAGES ABOUT com.esotericsoftware:kryo ... 4.0.2 -> 5.x.x
#       These are needed to test for backwards compatibility when an older Kryo is used.
mvn -PpackageForRelease -PEnableReportPlugins versions:display-dependency-updates versions:display-plugin-updates | grep -E '( -> | Building )' | \
   grep -v 'com\.esotericsoftware:kryo .\+ 4.0.2 -> 5' | sort -u
echo "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
echo "===-         DONE!         -==="
echo "==============================="
