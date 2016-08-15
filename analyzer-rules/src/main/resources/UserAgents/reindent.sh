#!/usr/bin/env bash

sed -i "
s@^  *\([\"a-zA-Z_]\)@      \1@
s@^  *config:@config:@
s@^  *\(- '\)@    \1@
s@^  *\(- [a-z]\)@\1@
s@^  *require:@    require:@
s@^  *extract:@    extract:@
s@^  *options:@    options:@
s@^  *input:@    input:@
s@^  *expected:@    expected:@
s@^  *name:@    name:@
s@^  *map:@    map:@
 " *.yaml

