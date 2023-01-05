#!/bin/bash
#
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2023 Niels Basjes
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

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null && pwd)"

MODULE_DIR="${DIR}/../../.."
TARGET_IT_DIR="${MODULE_DIR}/target/it/"

export DRILL_VERSION=$(cd "${MODULE_DIR}" && mvn help:evaluate -Dexpression=drill.version -q -DforceStdout)
export YAUAA_VERSION=$(cd "${MODULE_DIR}" && mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

mkdir -p "${TARGET_IT_DIR}"
mvn -DgroupId=nl.basjes.parse.useragent -DartifactId=yauaa -Dversion=${YAUAA_VERSION} dependency:get -DoutputDirectory="${TARGET_IT_DIR}/deps"
cd "${MODULE_DIR}" && mvn dependency:copy-dependencies -DoutputDirectory="${TARGET_IT_DIR}/deps" -DincludeScope=runtime
cp "${MODULE_DIR}/../logparser/target/yauaa-logparser-${YAUAA_VERSION}.jar" "${TARGET_IT_DIR}/deps"
cd "${MODULE_DIR}/../logparser/" && mvn dependency:copy-dependencies -DoutputDirectory="${TARGET_IT_DIR}/deps" -DincludeScope=runtime
cd "${MODULE_DIR}/../logparser/" && mvn dependency:copy-dependencies -DoutputDirectory="${TARGET_IT_DIR}/deps" -DincludeScope=provided
rm -f ${TARGET_IT_DIR}/deps/log4j*jar

ls -laF ${TARGET_IT_DIR}/deps/

DOCKER_IMAGE=yauaa-drill-it-test:${YAUAA_VERSION}
CONTAINER_NAME=yauaa-drill-it-test

# ---------------------------------------------------------------------------
# First we fully wipe any old instance of our integration test
echo "Removing any remaining stuff from previous test runs."
docker kill "${CONTAINER_NAME}"
docker rm "${CONTAINER_NAME}"
#docker rmi "${DOCKER_IMAGE}"

# ---------------------------------------------------------------------------
# Second we build a new image with the plugin installed
echo "Building docker image for drill ${DRILL_VERSION} with the plugin installed."
docker build --build-arg DRILL_VERSION="${DRILL_VERSION}" --build-arg YAUAA_VERSION="${YAUAA_VERSION}" -t "${DOCKER_IMAGE}" -f "${DIR}/Dockerfile" "${MODULE_DIR}"

# ---------------------------------------------------------------------------
# Third we start the instance
echo "Starting Apache Drill with plugin installed."

cat - << "SQLEXAMPLE"

To get you started an example statement that should work:


SELECT  uadata.ua.DeviceClass                AS DeviceClass,
        uadata.ua.AgentNameVersionMajor      AS AgentNameVersionMajor,
        uadata.ua.OperatingSystemNameVersion AS OperatingSystemNameVersion
FROM (
    SELECT
            parse_user_agent(
                'User-Agent' ,                  `request_user-agent`,
                'sec-ch-ua',                    `request_header_sec-ch-ua`,
                'sec-ch-ua-arch',               `request_header_sec-ch-ua-arch`,
                'sec-ch-ua-bitness',            `request_header_sec-ch-ua-bitness`,
                'sec-ch-ua-full-version',       `request_header_sec-ch-ua-full-version`,
                'sec-ch-ua-full-version-list',  `request_header_sec-ch-ua-full-version-list`,
                'sec-ch-ua-mobile',             `request_header_sec-ch-ua-mobile`,
                'sec-ch-ua-model',              `request_header_sec-ch-ua-model`,
                'sec-ch-ua-platform',           `request_header_sec-ch-ua-platform`,
                'sec-ch-ua-platform-version',   `request_header_sec-ch-ua-platform-version`,
                'sec-ch-ua-wow64',              `request_header_sec-ch-ua-wow64`
            ) AS ua
    FROM    table(
                dfs.`/tmp/access.hints` (
                    type => 'httpd',
                    logFormat => '%a %l %u %t "%r" %>s %b "%{Referer}i" "%{User-Agent}i" "%{Sec-CH-UA}i" "%{Sec-CH-UA-Arch}i" "%{Sec-CH-UA-Bitness}i" "%{Sec-CH-UA-Full-Version}i" "%{Sec-CH-UA-Full-Version-List}i" "%{Sec-CH-UA-Mobile}i" "%{Sec-CH-UA-Model}i" "%{Sec-CH-UA-Platform}i" "%{Sec-CH-UA-Platform-Version}i" "%{Sec-CH-UA-WoW64}i" %V',
                    flattenWildcards => true
                )
            )
) AS uadata;


this query should output something like this

+-------------+-----------------------+----------------------------+
| DeviceClass | AgentNameVersionMajor | OperatingSystemNameVersion |
+-------------+-----------------------+----------------------------+
| Desktop     | Chrome 100            | Linux 5.13.0               |
| Phone       | Chrome 101            | Android 11.0.0             |
+-------------+-----------------------+----------------------------+
2 rows selected (3.339 seconds)



SQLEXAMPLE


docker run --rm -ti --name "${CONTAINER_NAME}" "${DOCKER_IMAGE}"
