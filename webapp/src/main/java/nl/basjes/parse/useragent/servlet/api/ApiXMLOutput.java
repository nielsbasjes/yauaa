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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.servlet.ParseService;
import nl.basjes.parse.useragent.servlet.exceptions.MissingUserAgentException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static nl.basjes.parse.useragent.servlet.ParseService.ensureStartedForApis;
import static nl.basjes.parse.useragent.servlet.ParseService.userAgentAnalyzerIsAvailable;
import static nl.basjes.parse.useragent.servlet.api.Utils.splitPerFilledLine;
import static nl.basjes.parse.useragent.servlet.utils.Constants.EXAMPLE_JSON;
import static nl.basjes.parse.useragent.servlet.utils.Constants.EXAMPLE_USERAGENT;
import static nl.basjes.parse.useragent.servlet.utils.Constants.EXAMPLE_XML;
import static nl.basjes.parse.useragent.servlet.utils.Constants.EXAMPLE_YAML;
import static nl.basjes.parse.useragent.servlet.utils.Constants.TEXT_XYAML_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@Api(tags = "Yauaa")
@RequestMapping(value = "/yauaa/v1")
@RestController
public class ApiXMLOutput {

    @ApiOperation(
        value = "Analyze the provided User-Agent",
        notes = "<b>Trying this in swagger does not work in Chrome as Chrome does not allow setting " +
                "a different User-Agent: https://github.com/swagger-api/swagger-ui/issues/5035</b>"
    )
    @ApiResponses({
        @ApiResponse(
            code = 200, // HttpStatus.OK
            message = "The agent was successfully analyzed",
            examples = @Example({
                @ExampleProperty(mediaType = APPLICATION_JSON_VALUE, value = EXAMPLE_JSON),
                @ExampleProperty(mediaType = APPLICATION_XML_VALUE,  value = EXAMPLE_XML),
                @ExampleProperty(mediaType = TEXT_XYAML_VALUE,       value = EXAMPLE_YAML),
                @ExampleProperty(mediaType = TEXT_PLAIN_VALUE,       value = EXAMPLE_YAML)
            })
        )
    })
    @GetMapping(
        value = { "/analyze", "/analyze/xml" },
        produces = APPLICATION_XML_VALUE
    )
    public String getXMLGET(
        @ApiParam(
            value = "The standard browser request header User-Agent is used as the input that is to be analyzed.",
            example = EXAMPLE_USERAGENT
        )
        @RequestHeader("User-Agent")
        String userAgentString
    ) {
        return createOutput(userAgentString);
    }

    // -------------------------------------------------

    @ApiOperation(
        value = "Analyze the provided User-Agent"
    )
    @PostMapping(
        value = { "/analyze", "/analyze/xml" },
        consumes = TEXT_PLAIN_VALUE,
        produces = APPLICATION_XML_VALUE
    )
    @ApiResponses({
        @ApiResponse(
            code = 200, // HttpStatus.OK
            message = "The agent was successfully analyzed",
            examples = @Example({
                @ExampleProperty(mediaType = APPLICATION_JSON_VALUE,    value = EXAMPLE_JSON),
                @ExampleProperty(mediaType = APPLICATION_XML_VALUE,     value = EXAMPLE_XML),
                @ExampleProperty(mediaType = TEXT_XYAML_VALUE,          value = EXAMPLE_YAML),
                @ExampleProperty(mediaType = TEXT_PLAIN_VALUE,          value = EXAMPLE_YAML)
            })
        )
    })
    public String handlePOST(
        @ApiParam(
            name ="Request body",
            type = "Map",
            defaultValue = EXAMPLE_USERAGENT,
            value = "The entire POSTed value is used as the input that is to be analyzed.",
            examples = @Example(@ExampleProperty(mediaType = TEXT_PLAIN_VALUE, value = EXAMPLE_USERAGENT))
        )
        @RequestBody String userAgentString
    ) {
        return createOutput(userAgentString);
    }

    // -------------------------------------------------

    private String createOutput(String userAgentString) {
        if (userAgentString == null) {
            throw new MissingUserAgentException();
        }
        ensureStartedForApis(OutputType.XML);
        if (userAgentAnalyzerIsAvailable()) {
            UserAgentAnalyzer userAgentAnalyzer = ParseService.getUserAgentAnalyzer();
            List<String> result = new ArrayList<>(2048);
            splitPerFilledLine(userAgentString)
                .forEach(ua -> result.add(userAgentAnalyzer.parse(ua).toXML()));
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + String.join("\n", result);
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Yauaa></Yauaa>";
    }

}
