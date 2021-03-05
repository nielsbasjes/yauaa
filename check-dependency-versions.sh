#!/bin/bash

( cd devtools && mvn install -PskipQuality )
echo "==============================="
echo "===- CHECKING DEPENDENCIES -==="
echo "vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv"
echo "---------->"
echo "----------> IGNORE UPDATE MESSAGES ABOUT com.esotericsoftware:kryo ... 4.0.2 -> 5.x.x"
echo "----------> These are needed to test for backwards compatibility when an older Kryo is used."
echo "---------->"
mvn versions:display-dependency-updates versions:display-plugin-updates | grep -E '( -> | Building )'
echo "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
echo "===-         DONE!         -==="
echo "==============================="
