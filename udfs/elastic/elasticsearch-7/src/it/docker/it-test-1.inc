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
info "Loading pipeline (old style)."

curl -s -H 'Content-Type: application/json' -X PUT 'localhost:9200/_ingest/pipeline/yauaa-test-pipeline_some' -d '
{
  "description": "A pipeline to do whatever",
  "processors": [
    {
      "yauaa" : {
        "field"         : "useragent",
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

TESTLOG="${DIR}/es-simulation.log"

curl -s -H 'Content-Type: application/json' -X POST 'localhost:9200/_ingest/pipeline/yauaa-test-pipeline_some/_simulate' -d '
{
  "docs": [
    {
      "_source": {
        "useragent" : "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36"
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

checkLog '"AgentInformationEmail": "Unknown"'
checkLog '"AgentInformationUrl": "Unknown"'
checkLog '"AgentName": "Chrome"'
checkLog '"AgentNameVersionMajor": "Chrome 53"'
checkLog '"AgentVersion": "53.0.2785.124"'
checkLog '"AgentVersionMajor": "53"'
checkLog '"DeviceBrand": "Google"'
checkLog '"DeviceClass": "Phone"'
checkLog '"DeviceName": "Google Nexus 6"'
checkLog '"FirstProductName": "Mozilla"'
checkLog '"__SyntaxError__": "false"'

# =================================================="
