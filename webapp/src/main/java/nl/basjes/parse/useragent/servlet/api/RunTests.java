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

package nl.basjes.parse.useragent.servlet.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester;
import nl.basjes.parse.useragent.servlet.ParseService;
import nl.basjes.parse.useragent.servlet.exceptions.YauaaTestsFailed;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static nl.basjes.parse.useragent.AnalyzerPreHeater.LOG;
import static nl.basjes.parse.useragent.debug.AbstractUserAgentAnalyzerTester.runTests;
import static nl.basjes.parse.useragent.servlet.ParseService.ensureStartedForApis;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@Tag(name = "Performance", description = "Heating up the JVM and checking the analyzer configuration")
@RequestMapping(value = "/yauaa/v1")
@RestController
public class RunTests {
    @Operation(
        description = "Fire all available test cases against the analyzer to heat up the JVM"
    )
    @ApiResponse(
        responseCode = "200", // HttpStatus.OK
        description = "The number of reported tests were done to preheat the engine",
        content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    examples = @ExampleObject("{\n" +
                    "  \"status\": \"Ran tests\",\n" +
                    "  \"testsDone\": 2337,\n" +
                    "  \"timeInMs\": 3123\n" +
                    "}")
        )
    )
    @GetMapping(
        value = "/preheat",
        produces = APPLICATION_JSON_VALUE
    )
    public String getPreHeat() {
        ensureStartedForApis(OutputType.JSON);
        UserAgentAnalyzer userAgentAnalyzer = ParseService.getUserAgentAnalyzer();

        final int cacheSize = userAgentAnalyzer.getCacheSize();
        userAgentAnalyzer.disableCaching();
        long       start     = System.nanoTime();
        final long testsDone = userAgentAnalyzer.preHeat();
        long       stop      = System.nanoTime();
        userAgentAnalyzer.setCacheSize(cacheSize);
        if (testsDone == 0) {
            return "{ \"status\": \"No testcases available\", \"testsDone\": 0 , \"timeInMs\" : -1 } ";
        }
        return "{ \"status\": \"Ran tests\", \"testsDone\": " + testsDone + " , \"timeInMs\" : " + (stop - start) / 1000000 + " } ";
    }

    // ===========================================

    @Operation(
        description = "Fire all available test cases against the analyzer and return 200 if all tests were good"
    )
    @ApiResponse(
        responseCode = "200", // HttpStatus.OK
        description = "All tests were good",
        content = @Content(
            mediaType = TEXT_PLAIN_VALUE,
            examples = @ExampleObject("All tests passed")
        )
    )
    @ApiResponse(
        responseCode = "400", // HttpStatus.INTERNAL_SERVER_ERROR
        description = "A test failed",
        content = @Content(
            mediaType = TEXT_PLAIN_VALUE,
            examples = @ExampleObject("Extensive text describing what went wrong in the test that failed")
        )
    )
    @GetMapping(
        value = "/runtests",
        produces = TEXT_PLAIN_VALUE
    )
    public String getRunTests() {
        // FIXME: See if we can do this without creating a new instance.
        LOG.warn("Creating new instance of the analyzer to run the tests.");
        UserAgentAnalyzerTester tester = UserAgentAnalyzerTester.newBuilder()
            .hideMatcherLoadStats()
            .addOptionalResources("file:UserAgents*/*.yaml")
            .immediateInitialization()
            .keepTests()
            .build();
        StringBuilder errorMessage = new StringBuilder();
        boolean ok = runTests(tester, false, true, null, false, false, errorMessage);
        if (ok) {
            return "All tests passed";
        }
        throw new YauaaTestsFailed(errorMessage.toString());
    }

    // ===========================================


}
