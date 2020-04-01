#!/bin/bash

( cd devtools && mvn package )
echo "==============================="
echo "===- CHECKING DEPENDENCIES -==="
echo "vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv"
mvn versions:display-dependency-updates versions:display-plugin-updates | fgrep ' -> ' | sort | uniq -c
echo "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
echo "===-         DONE!         -==="
echo "==============================="
