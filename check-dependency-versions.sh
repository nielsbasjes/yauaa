#!/bin/bash

( cd devtools && mvn install -PskipQuality )
echo "==============================="
echo "===- CHECKING DEPENDENCIES -==="
echo "vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv"
echo "---------->"
echo "----------> IGNORE UPDATE MESSAGES ABOUT com.esotericsoftware:kryo ... 4.0.2 -> 5.0.2"
echo "---------->"
mvn versions:display-dependency-updates versions:display-plugin-updates | grep -E '( -> | Building )'
echo "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
echo "===-         DONE!         -==="
echo "==============================="
