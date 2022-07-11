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

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.servlet.ParseService;
import nl.basjes.parse.useragent.servlet.exceptions.MissingUserAgentException;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.ws.rs.HeaderParam;
import java.util.ArrayList;
import java.util.List;

import static nl.basjes.parse.useragent.UserAgent.USERAGENT_FIELDNAME;
import static nl.basjes.parse.useragent.servlet.api.Utils.splitPerFilledLine;
import static nl.basjes.parse.useragent.servlet.utils.Constants.EXAMPLE_JSON;
import static nl.basjes.parse.useragent.servlet.utils.Constants.EXAMPLE_TWO_USERAGENTS;
import static nl.basjes.parse.useragent.servlet.utils.Constants.EXAMPLE_USERAGENT;
import static nl.basjes.parse.useragent.servlet.utils.Constants.EXAMPLE_XML;
import static nl.basjes.parse.useragent.servlet.utils.Constants.EXAMPLE_YAML;
import static nl.basjes.parse.useragent.servlet.utils.Constants.TEXT_XYAML_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@Tag(name = "Analyze", description = "Analyzing the useragents")
@RequestMapping(value = "/yauaa/v1")
@RestController
public class ApiXMLOutput {

    // FIXME: The XML examples do NOT show: the schema.xml.name must be set for swagger.
    //        https://github.com/eclipse/microprofile-open-api/issues/530
    //        https://github.com/swagger-api/swagger-ui/issues/4423

    // -------------------------------------------------
    // GET /analyze + accept --> XML

    @Operation(
        summary = "Analyze the provided User-Agent",
        description = "<b>Trying this in swagger does not work in Chrome as Chrome does not allow setting " +
            "a different User-Agent: <a href=\"https://github.com/swagger-api/swagger-ui/issues/5035\">swagger-ui issue 5035</a></b>"
    )
    @APIResponse(
        responseCode = "200", // HttpStatus.OK
        description = "The agent was successfully analyzed",
        content = {
            @Content(mediaType = APPLICATION_JSON_VALUE,    examples = @ExampleObject(name="JSon Example", value = EXAMPLE_JSON), schema = @Schema(type = SchemaType.STRING)),
            @Content(mediaType = APPLICATION_XML_VALUE,     examples = @ExampleObject(name="XML Example",  value = EXAMPLE_XML),  schema = @Schema(type = SchemaType.STRING)),
            @Content(mediaType = TEXT_XYAML_VALUE,          examples = @ExampleObject(name="Yaml Example", value = EXAMPLE_YAML), schema = @Schema(type = SchemaType.STRING)),
            @Content(mediaType = TEXT_PLAIN_VALUE,          examples = @ExampleObject(name="Yaml Example", value = EXAMPLE_YAML), schema = @Schema(type = SchemaType.STRING))
        }
    )
    @GetMapping(
        value = "/analyze",
        produces = APPLICATION_XML_VALUE
    )
    public String handleGETAnalyze(
        @Parameter(
            name = "User-Agent",
            description = "The standard browser request header User-Agent is used as the input that is to be analyzed.",
            example = EXAMPLE_USERAGENT
        )
        @HeaderParam("User-Agent") String userAgentString
    ) {
        return createOutput(userAgentString);
    }

    // -------------------------------------------------
    // GET /analyze + accept --> XML

    @Operation(
        summary = "Analyze the provided User-Agent",
        description = "<b>Trying this in swagger does not work in Chrome as Chrome does not allow setting " +
            "a different User-Agent: <a href=\"https://github.com/swagger-api/swagger-ui/issues/5035\">swagger-ui issue 5035</a></b>"
    )
    @APIResponse(
        responseCode = "200", // HttpStatus.OK
        description = "The agent was successfully analyzed",
        content = {
            @Content(mediaType = APPLICATION_XML_VALUE,     examples = @ExampleObject(name="XML Example", value = EXAMPLE_XML), schema = @Schema(type = SchemaType.STRING))
        }
    )
    @GetMapping(
        value = "/analyze/xml",
        produces = APPLICATION_XML_VALUE
    )
    public String handleGETAnalyzeXML(
        @Parameter(
            name = "User-Agent",
            description = "The standard browser request header User-Agent is used as the input that is to be analyzed.",
            example = EXAMPLE_USERAGENT
        )
        @HeaderParam("User-Agent") String userAgentString
    ) {
        return createOutput(userAgentString);
    }

    // -------------------------------------------------
    // POST /analyze + accept --> Json

    @Operation(
        summary = "Analyze the provided User-Agent"
    )
    @PostMapping(
        value ="/analyze",
        consumes = TEXT_PLAIN_VALUE,
        produces = APPLICATION_XML_VALUE
    )
    @APIResponse(
        responseCode = "200", // HttpStatus.OK
        description = "The agent was successfully analyzed",
        content = {
            @Content(mediaType = APPLICATION_JSON_VALUE,    examples = @ExampleObject(name="JSon Example", value = EXAMPLE_JSON), schema = @Schema(type = SchemaType.STRING)),
            @Content(mediaType = APPLICATION_XML_VALUE,     examples = @ExampleObject(name="XML Example",  value = EXAMPLE_XML),  schema = @Schema(type = SchemaType.STRING)),
            @Content(mediaType = TEXT_XYAML_VALUE,          examples = @ExampleObject(name="Yaml Example", value = EXAMPLE_YAML), schema = @Schema(type = SchemaType.STRING)),
            @Content(mediaType = TEXT_PLAIN_VALUE,          examples = @ExampleObject(name="Yaml Example", value = EXAMPLE_YAML), schema = @Schema(type = SchemaType.STRING))
        }
    )
    public String handlePOSTAnalyze(
        @Parameter(
            name = "Request body",
            schema = @Schema(
                type = SchemaType.STRING, // "A newline separated list of useragent strings",
                description = "A newline separated list of useragent strings. The entire POSTed value is used as the input that is to be analyzed.",
                example = EXAMPLE_TWO_USERAGENTS)
        )
        @RequestBody String userAgentString
    ) {
        return createOutput(userAgentString);
    }

    // -------------------------------------------------
    // POST /analyze/xml --> XML

    @Operation(
        summary = "Analyze the provided User-Agent"
    )
    @PostMapping(
        value ="/analyze/xml",
        consumes = TEXT_PLAIN_VALUE,
        produces = APPLICATION_XML_VALUE
    )
    @APIResponse(
        responseCode = "200", // HttpStatus.OK
        description = "The agent was successfully analyzed",
        content = {
            @Content(mediaType = APPLICATION_XML_VALUE,     examples = @ExampleObject(name="XML Example", value = EXAMPLE_XML),
                schema = @Schema(type = SchemaType.STRING, properties = { @SchemaProperty(type = SchemaType.STRING, name = "xml.name", defaultValue = "Yauaa")}))
        }
    )
    public String handlePOSTAnalyzeXML(
        @Parameter(
            name = "Request body",
            schema = @Schema(
                type = SchemaType.STRING, // "A newline separated list of useragent strings",
                description = "A newline separated list of useragent strings. The entire POSTed value is used as the input that is to be analyzed.",
                example = EXAMPLE_TWO_USERAGENTS)
        )
        @RequestBody String userAgentString
    ) {
        return createOutput(userAgentString);
    }

    // -------------------------------------------------

    @Inject
    ParseService parseService;

    private String createOutput(String userAgentString) {
        if (userAgentString == null) {
            throw new MissingUserAgentException();
        }
        parseService.ensureStartedForApis(OutputType.XML);
        UserAgentAnalyzer userAgentAnalyzer = parseService.getUserAgentAnalyzer();
        List<String> result = new ArrayList<>(2048);
        for (String input : splitPerFilledLine(userAgentString)) {
            UserAgent userAgent = userAgentAnalyzer.parse(input);
            List<String> fieldNamesSorted = new ArrayList<>();
            fieldNamesSorted.add(USERAGENT_FIELDNAME);
            fieldNamesSorted.addAll(userAgent.getCleanedAvailableFieldNamesSorted());
            result.add(userAgent.toXML(fieldNamesSorted));
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + String.join("\n", result);
    }
}
