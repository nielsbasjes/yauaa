#
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2022 Niels Basjes
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
info "Loading pipeline (with client hints)."

curl -s -H 'Content-Type: application/json' -X PUT 'localhost:9200/_ingest/pipeline/yauaa-test-pipeline_clienthints' -d '
{
  "description": "A pipeline to do whatever",
  "processors": [
    {
      "yauaa" : {
        "field_to_header_mapping" : {
            "ua":                       "User-Agent",
            "uach_platform":            "Sec-CH-UA-Platform",
            "uach_platform_version":    "Sec-CH-UA-Platform-Version"
        },
        "target_field"  : "parsed",
        "fieldNames"    : [ "DeviceClass", "DeviceBrand", "DeviceName", "OperatingSystemNameVersion", "AgentNameVersionMajor", "FirstProductName" ],
        "cacheSize" : 10,
        "preheat"   : 10,
        "extraRules" : "config:\n- matcher:\n    extract:\n      - '"'"'FirstProductName     : 1 :agent.(1)product.(1)name'"'"'\n"
      }
    }
  ]
}
'

# =================================================="
# Get result"
info "Simulate the pipeline and check if it contains the desired values."

TESTLOG="${DIR}/es-simulation-clienthints.log"

curl -s -H 'Content-Type: application/json' -X POST 'localhost:9200/_ingest/pipeline/yauaa-test-pipeline_clienthints/_simulate' -d '
{
  "docs": [
    {
      "_source": {
        "ua":                       "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.60 Safari/537.36",
        "uach_platform":            "\"macOS\"",
        "uach_platform_version":    "\"12.3.1\""
      }
    }
  ]
}' > "${TESTLOG}"

function checkLog() {
  expected="${1}"
  cat "${TESTLOG}" | \
    python -m json.tool | \
    grep -F "${expected}" > /dev/null 2>&1

  RESULT=$?

  if [ "${RESULT}" == "0" ]; then
    pass "${expected}"
  else
    fail "${expected}"
    cat "${TESTLOG}" | \
      python -m json.tool
    exit 255
  fi
}

checkLog '"DeviceBrand": "Apple"'
checkLog '"DeviceClass": "Desktop"'
checkLog '"DeviceName": "Apple Macintosh"'
checkLog '"OperatingSystemName": "Mac OS"'
checkLog '"OperatingSystemVersion": "12.3.1"'
checkLog '"OperatingSystemNameVersion": "Mac OS 12.3.1"'
checkLog '"AgentName": "Chrome"'
checkLog '"AgentVersion": "100.0.4896.60"'
checkLog '"AgentVersionMajor": "100"'
checkLog '"AgentNameVersionMajor": "Chrome 100"'
checkLog '"FirstProductName": "Mozilla"'

# =================================================="
