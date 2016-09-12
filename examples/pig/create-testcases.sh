#!/bin/bash

find ../../analyzer/src/main/resources/UserAgents/ -type f -name '*.yaml' | xargs cat | fgrep user_a | cut -d':' -f2- | sed 's@^ *@@g' | sed 's@^"@@' | sed "s@^'@@" | sed "s@'\$@@" > testcases.txt
