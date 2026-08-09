/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2026 Niels Basjes
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

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "System status", description = "Checking if the servlet is running")
@RestController
public class StatusCheck {

    // -------------------------------------------------

    private static final String KUBERNETES_CONFIG_EXAMPLE =
        """
            <pre>
            apiVersion: apps/v1
            kind: Deployment
            metadata:
            &nbsp;&nbsp;name: yauaa
            spec:
            &nbsp;&nbsp;selector:
            &nbsp;&nbsp;&nbsp;&nbsp;matchLabels:
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;app: yauaa
            &nbsp;&nbsp;&nbsp;&nbsp;replicas: 3
            &nbsp;&nbsp;&nbsp;&nbsp;template:
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;metadata:
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;labels:
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;app: yauaa
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;spec:
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;containers:
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- name: yauaa
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;image: nielsbasjes/yauaa:latest
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;imagePullPolicy: Always
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ports:
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- containerPort: 8080
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name: yauaa
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;protocol: TCP
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;livenessProbe:
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;httpGet:
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;path: /liveness
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;port: yauaa
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;initialDelaySeconds: 2
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;periodSeconds: 3
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;readinessProbe:
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;httpGet:
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;path: /readiness
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;port: yauaa
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;initialDelaySeconds: 10
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;periodSeconds: 30
            </pre>
        """;

    // ------------------------------------------

    @SuppressWarnings("SameReturnValue")
    @Operation(
        summary = "Is the servlet running?",
        description = "This endpoint is intended for checking if the service has been started up.<br>" +
            "If you are deploying this on Kubernetes you can use this endpoint for the " +
            "<b>livenessProbe</b> for your deployment:" + KUBERNETES_CONFIG_EXAMPLE
    )
    @ApiResponse(
        responseCode = "200", // HttpStatus.OK
        description = "The analyzer is running",
        content = @Content(examples = @ExampleObject("YES"))
    )
    @GetMapping(
        path = "/liveness"
    )
    public String isLive() {
        return "YES";
    }

    // ------------------------------------------

    @SuppressWarnings("SameReturnValue")
    @Operation(
        summary = "Is the analyzer engine running?",
        description = "This endpoint is intended for checking if the service has been started up.<br>" +
            "If you are deploying this on Kubernetes you can use this endpoint for the " +
            "<b>readinessProbe</b> for your deployment:" + KUBERNETES_CONFIG_EXAMPLE
    )
    @ApiResponse(
        responseCode = "200", // HttpStatus.OK
        description = "The analyzer is running",
        content = @Content(examples = @ExampleObject("YES"))
    )
    @ApiResponse(
        responseCode = "500", // HttpStatus.INTERNAL_SERVER_ERROR,
        description = "The analyzer is still starting up or has failed to startup",
        content = @Content(examples = @ExampleObject())
    )
    @GetMapping(
        path = "/readiness"
    )
    public String isReady() {
        parseService.ensureStartedForApis(OutputType.TXT);
        return "YES";
    }

    // ------------------------------------------

    @Hidden
    @Operation(
        summary = "Is the analyzer engine running?",
        description = "Same as /readiness and left here for backwards compatibility"
    )
    @GetMapping(
        path = "/running"
    )
    public String isRunning() {
        return isReady();
    }

}
