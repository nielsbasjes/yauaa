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

package nl.basjes.parse.useragent.servlet.api;

import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.config.TestCase;
import nl.basjes.parse.useragent.config.TestCase.TestResult;
import nl.basjes.parse.useragent.servlet.OutputType;
import nl.basjes.parse.useragent.servlet.ParseService;
import nl.basjes.parse.useragent.servlet.exceptions.YauaaTestsFailed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@Tag(name = "Performance", description = "Heating up the JVM and checking the analyzer configuration")
@RequestMapping(value = "/yauaa/v1")
@RestController
public class RunTests {

    @Inject
    ParseService parseService;

    @Operation(
        description = "Fire all available test cases against the analyzer to heat up the JVM"
    )
    @APIResponse(
        responseCode = "200", // HttpStatus.OK
        description = "The number of reported tests were done to preheat the engine",
        content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    examples = @ExampleObject("""
                        {
                          "status": "Ran tests",
                          "testsDone": 2337,
                          "timeInMs": 3123
                        }"""),
                    schema = @Schema(type = SchemaType.STRING)
        )
    )
    @GetMapping(
        value = "/preheat",
        produces = APPLICATION_JSON_VALUE
    )
    public String getPreHeat() {
        parseService.ensureStartedForApis(OutputType.JSON);
        UserAgentAnalyzer userAgentAnalyzer = parseService.getUserAgentAnalyzer();

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
    @APIResponse(
        responseCode = "200", // HttpStatus.OK
        description = "All tests were good",
        content = @Content(
            mediaType = TEXT_PLAIN_VALUE,
            examples = @ExampleObject("All 3866 tests passed in 2994ms (average 0.775ms per testcase)."),
            schema = @Schema(type = SchemaType.STRING)
        )
    )
    @APIResponse(
        responseCode = "500", // HttpStatus.INTERNAL_SERVER_ERROR
        description = "A test failed",
        content = @Content(
            mediaType = TEXT_PLAIN_VALUE,
            examples = @ExampleObject("Extensive text describing what went wrong in the test that failed"),
            schema = @Schema(type = SchemaType.STRING)
        )
    )
    @GetMapping(
        value = "/runtests",
        produces = TEXT_PLAIN_VALUE
    )
    public String getRunTests() {
        UserAgentAnalyzer userAgentAnalyzer = parseService.getUserAgentAnalyzer();
        List<TestCase> testCases = userAgentAnalyzer.getTestCases();

        long start = System.nanoTime();
        List<TestResult> failedTests = testCases
            .stream()
            .map(testCase -> testCase.verify(userAgentAnalyzer))
            .filter(TestResult::testFailed)
            .toList();
        long stop = System.nanoTime();

        if (failedTests.isEmpty()) {
            return String.format("All %d tests passed in %dms (average %4.3fms per testcase).",
                testCases.size(), (stop-start)/1_000_000, ((stop-start)/1_000_000D/testCases.size()));
        }
        throw new YauaaTestsFailed("There were " + failedTests.size() + " failed tests " +
            "(~"+ ((100.0D*failedTests.size()) / testCases.size()) +"%)");
    }

    // ===========================================


}
