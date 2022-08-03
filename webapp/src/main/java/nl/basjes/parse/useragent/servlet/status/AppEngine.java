/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2022 Niels Basjes
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import nl.basjes.parse.useragent.servlet.ParseService;
import nl.basjes.parse.useragent.servlet.api.OutputType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@Tag(name = "System status")
@RestController
public class AppEngine {

    private final ParseService parseService;

    @Autowired
    public AppEngine(ParseService parseService) {
        this.parseService = parseService;
    }

    // -------------------------------------------------

    /**
     * <a href="https://cloud.google.com/appengine/docs/flexible/java/how-instances-are-managed#health_checking">
     * App Engine health checking</a> requires responding with 200 to {@code /_ah/health}.
     *
     * @return Returns a non empty message body.
     */
    @SuppressWarnings("SameReturnValue")
    @Operation(
        summary = "Is the analyzer engine running?",
        description = "The old style AppEngine status endpoint."
    )
    @ApiResponse(
        responseCode = "200", // HttpStatus.OK
        description = "The analyzer is running",
        content = @Content(examples = @ExampleObject("YES"))
    )
    @ApiResponse(
        responseCode = "500", // HttpStatus.INTERNAL_SERVER_ERROR,
        description = "The analyzer is starting up",
        content = @Content(examples = @ExampleObject())
    )
    @GetMapping(
        path = "/_ah/health",
        produces = TEXT_PLAIN_VALUE
    )
    public String isHealthy() {
        parseService.ensureStartedForApis(OutputType.TXT);
        return "YES";
    }

}
