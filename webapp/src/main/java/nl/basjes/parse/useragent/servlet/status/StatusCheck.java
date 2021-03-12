/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2021 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.useragent.servlet.status;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import nl.basjes.parse.useragent.servlet.api.OutputType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static nl.basjes.parse.useragent.servlet.ParseService.ensureStartedForApis;

@RestController
public class StatusCheck {

    @ApiOperation(
        value = "Is the analyzer running? YES or NO",
        notes = "This endpoint is intended for checking if the service has been started up.<br>" +
            "So if you are deploying this on Kubernetes you can use this endpoint for both " +
            "the <b>readinessProbe</b> and the <b>livenessProbe</b> for your deployment:" +
            "<pre>" +
            "apiVersion: apps/v1</br>" +
            "kind: Deployment</br>" +
            "metadata:</br>" +
            "&nbsp;&nbsp;name: yauaa</br>" +
            "spec:</br>" +
            "&nbsp;&nbsp;selector:</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;matchLabels:</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;app: yauaa</br>" +
            "&nbsp;&nbsp;&nbsp;replicas: 3</br>" +
            "&nbsp;&nbsp;&nbsp;template:</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;metadata:</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;labels:</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;app: yauaa</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;spec:</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;containers:</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- name: yauaa</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;image: nielsbasjes/yauaa:latest</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;imagePullPolicy: Always</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ports:</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- containerPort: 8080</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name: yauaa</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;protocol: TCP</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;readinessProbe:</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;httpGet:</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;path: /running</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;port: yauaa</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;initialDelaySeconds: 2</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;periodSeconds: 3</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;livenessProbe:</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;httpGet:</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;path: /running</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;port: yauaa</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;initialDelaySeconds: 10</br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;periodSeconds: 30</br>" +
            "</pre>" +
            ""
    )
    @ApiResponses({
        @ApiResponse(
            code = 200, // HttpStatus.OK
            message = "The analyzer is running"
        ),
        @ApiResponse(
            code = 500, // HttpStatus.INTERNAL_SERVER_ERROR,
            message = "The analyzer is starting up"
        )
    })
    @GetMapping(
        path = "/running"
    )
    public String isRunning() {
        ensureStartedForApis(OutputType.TXT);
        return "YES";
    }

}
