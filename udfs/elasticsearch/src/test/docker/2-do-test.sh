#!/bin/bash -x
#
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2020 Niels Basjes
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# =================================================="
# Load pipeline"

curl -H 'Content-Type: application/json' -X PUT 'localhost:9200/_ingest/pipeline/yauaa-test-pipeline_all' -d '
{
  "description": "A pipeline to do whatever",
  "processors": [
    {
      "yauaa" : {
        "field"         : "useragent",
        "target_field"  : "parsed"
      }
    }
  ]
}
'

curl -H 'Content-Type: application/json' -X PUT 'localhost:9200/_ingest/pipeline/yauaa-test-pipeline_some' -d '
{
  "description": "A pipeline to do whatever",
  "processors": [
    {
      "yauaa" : {
        "field"         : "useragent",
        "target_field"  : "parsed",
        "fieldNames"    : [ "DeviceClass", "DeviceBrand", "DeviceName", "AgentNameVersionMajor", "FirstProductName" ],
        "cacheSize" : 10,
        "preheat"   : 10,
        "extraRules" : "config:\n- matcher:\n    extract:\n      - '"'"'FirstProductName     : 1 :agent.(1)product.(1)name'"'"'\n"
      }
    }
  ]
}
'

# =================================================="
# Put record"

curl -H 'Content-Type: application/json' -X PUT 'localhost:9200/my-index/my-type/1?pipeline=yauaa-test-pipeline_all' -d '
{
  "useragent" : "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36"
}
'

curl -H 'Content-Type: application/json' -X PUT 'localhost:9200/my-index/my-type/2?pipeline=yauaa-test-pipeline_some' -d '
{
  "useragent" : "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36"
}
'


# =================================================="
# Get result"

curl -H 'Content-Type: application/json' -X GET 'localhost:9200/my-index/my-type/1' | python -m json.tool

curl -H 'Content-Type: application/json' -X GET 'localhost:9200/my-index/my-type/2' | python -m json.tool

# =================================================="
