#!/bin/bash
find analyzer/src -type f -name Generate.sh | sed 's@/Generate.sh@@' | while read dir ; 
do
  ( cd ${dir} ; ./Generate.sh )
done
