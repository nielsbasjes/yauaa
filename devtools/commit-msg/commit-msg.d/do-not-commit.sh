#!/bin/bash

# Fail if there is a DONOTCOMMIT in one of the files to be committed
git-diff-index -p -M --cached HEAD -- | grep '^+' | grep -F DONOTCOMMIT && die "Blocking commit because string DONOTCOMMIT detected in patch"
