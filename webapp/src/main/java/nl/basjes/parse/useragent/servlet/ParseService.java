/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2020 Niels Basjes
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

package nl.basjes.parse.useragent.servlet;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.Version;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static nl.basjes.parse.useragent.utils.YauaaVersion.getVersion;
import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;
import static org.apache.commons.text.StringEscapeUtils.escapeJson;
import static org.apache.commons.text.StringEscapeUtils.escapeXml10;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@SuppressWarnings("deprecation") // The description field may be deprecated but in SpringFox it is still used.
@Api(tags = "Yauaa", description = "Useragent parsing service")
@SpringBootApplication
@RestController
public class ParseService {

    private static final Logger LOG = LoggerFactory.getLogger(ParseService.class);

    private static       UserAgentAnalyzer userAgentAnalyzer               = null;
    private static       long              initStartMoment;
    private static       boolean           userAgentAnalyzerIsAvailable    = false;
    private static       String            userAgentAnalyzerFailureMessage = null;
    private static final String            ANALYZER_VERSION                = getVersion();
    private static final String            API_BASE_PATH                   = "/yauaa/v1";

    private static final String            TEXT_XYAML_VALUE = "text/x-yaml";


    private static final String            EXAMPLE_USERAGENT               =
        "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36";

    private static final String EXAMPLE_JSON = "[ {\n" +
        "  \"Useragent\": \"Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36\",\n" +
        "  \"DeviceClass\": \"Phone\",\n" +
        "  \"DeviceName\": \"Google Nexus 6\",\n" +
        "  \"DeviceBrand\": \"Google\",\n" +
        "  \"OperatingSystemClass\": \"Mobile\",\n" +
        "  \"OperatingSystemName\": \"Android\",\n" +
        "  \"OperatingSystemVersion\": \"7.0\",\n" +
        "  \"OperatingSystemVersionMajor\": \"7\",\n" +
        "  \"OperatingSystemNameVersion\": \"Android 7.0\",\n" +
        "  \"OperatingSystemNameVersionMajor\": \"Android 7\",\n" +
        "  \"OperatingSystemVersionBuild\": \"NBD90Z\",\n" +
        "  \"LayoutEngineClass\": \"Browser\",\n" +
        "  \"LayoutEngineName\": \"Blink\",\n" +
        "  \"LayoutEngineVersion\": \"53.0\",\n" +
        "  \"LayoutEngineVersionMajor\": \"53\",\n" +
        "  \"LayoutEngineNameVersion\": \"Blink 53.0\",\n" +
        "  \"LayoutEngineNameVersionMajor\": \"Blink 53\",\n" +
        "  \"AgentClass\": \"Browser\",\n" +
        "  \"AgentName\": \"Chrome\",\n" +
        "  \"AgentVersion\": \"53.0.2785.124\",\n" +
        "  \"AgentVersionMajor\": \"53\",\n" +
        "  \"AgentNameVersion\": \"Chrome 53.0.2785.124\",\n" +
        "  \"AgentNameVersionMajor\": \"Chrome 53\"\n" +
        "} ]";

    private static final String EXAMPLE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "  <Yauaa>\n" +
        "    <Useragent>Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36</Useragent>\n" +
        "    <DeviceClass>Phone</DeviceClass>\n" +
        "    <DeviceName>Google Nexus 6</DeviceName>\n" +
        "    <DeviceBrand>Google</DeviceBrand>\n" +
        "    <OperatingSystemClass>Mobile</OperatingSystemClass>\n" +
        "    <OperatingSystemName>Android</OperatingSystemName>\n" +
        "    <OperatingSystemVersion>7.0</OperatingSystemVersion>\n" +
        "    <OperatingSystemVersionMajor>7</OperatingSystemVersionMajor>\n" +
        "    <OperatingSystemNameVersion>Android 7.0</OperatingSystemNameVersion>\n" +
        "    <OperatingSystemNameVersionMajor>Android 7</OperatingSystemNameVersionMajor>\n" +
        "    <OperatingSystemVersionBuild>NBD90Z</OperatingSystemVersionBuild>\n" +
        "    <LayoutEngineClass>Browser</LayoutEngineClass>\n" +
        "    <LayoutEngineName>Blink</LayoutEngineName>\n" +
        "    <LayoutEngineVersion>53.0</LayoutEngineVersion>\n" +
        "    <LayoutEngineVersionMajor>53</LayoutEngineVersionMajor>\n" +
        "    <LayoutEngineNameVersion>Blink 53.0</LayoutEngineNameVersion>\n" +
        "    <LayoutEngineNameVersionMajor>Blink 53</LayoutEngineNameVersionMajor>\n" +
        "    <AgentClass>Browser</AgentClass>\n" +
        "    <AgentName>Chrome</AgentName>\n" +
        "    <AgentVersion>53.0.2785.124</AgentVersion>\n" +
        "    <AgentVersionMajor>53</AgentVersionMajor>\n" +
        "    <AgentNameVersion>Chrome 53.0.2785.124</AgentNameVersion>\n" +
        "    <AgentNameVersionMajor>Chrome 53</AgentNameVersionMajor>\n" +
        "  </Yauaa>";

    private static final String EXAMPLE_YAML =
        "- test:\n" +
        "    input:\n" +
        "      user_agent_string: 'Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36'\n" +
        "    expected:\n" +
        "      DeviceClass                           : 'Phone'\n" +
        "      DeviceName                            : 'Google Nexus 6'\n" +
        "      DeviceBrand                           : 'Google'\n" +
        "      OperatingSystemClass                  : 'Mobile'\n" +
        "      OperatingSystemName                   : 'Android'\n" +
        "      OperatingSystemVersion                : '7.0'\n" +
        "      OperatingSystemVersionMajor           : '7'\n" +
        "      OperatingSystemNameVersion            : 'Android 7.0'\n" +
        "      OperatingSystemNameVersionMajor       : 'Android 7'\n" +
        "      OperatingSystemVersionBuild           : 'NBD90Z'\n" +
        "      LayoutEngineClass                     : 'Browser'\n" +
        "      LayoutEngineName                      : 'Blink'\n" +
        "      LayoutEngineVersion                   : '53.0'\n" +
        "      LayoutEngineVersionMajor              : '53'\n" +
        "      LayoutEngineNameVersion               : 'Blink 53.0'\n" +
        "      LayoutEngineNameVersionMajor          : 'Blink 53'\n" +
        "      AgentClass                            : 'Browser'\n" +
        "      AgentName                             : 'Chrome'\n" +
        "      AgentVersion                          : '53.0.2785.124'\n" +
        "      AgentVersionMajor                     : '53'\n" +
        "      AgentNameVersion                      : 'Chrome 53.0.2785.124'\n" +
        "      AgentNameVersionMajor                 : 'Chrome 53'\n";

    private enum OutputType {
        YAML,
        HTML,
        JSON,
        XML,
        TXT
    }

    @PostConstruct
    public void automaticStartup() {
        if (!userAgentAnalyzerIsAvailable && userAgentAnalyzerFailureMessage == null) {
            initStartMoment = System.currentTimeMillis();
            new Thread(() -> {
                try {
                    userAgentAnalyzer = UserAgentAnalyzer.newBuilder()
                        .hideMatcherLoadStats()
                        .addResources("file:UserAgents/*.yaml")
                        .keepTests()
                        .build();
                    userAgentAnalyzer.initializeMatchers();
                    userAgentAnalyzerIsAvailable = true;
                } catch (Exception e) {
                    userAgentAnalyzerFailureMessage =
                        e.getClass().getSimpleName() + "<br/>" +
                            e.getMessage().replaceAll("\n", "<br/>");
                    LOG.error("Fatal error during startup: {}\n" +
                            "=======================================================\n" +
                            "{}\n" +
                            "=======================================================\n",
                            e.getClass().getCanonicalName(), e.getMessage());
                }
            }).start();
        }
    }

    public static class YauaaIsBusyStarting extends RuntimeException {
        private final OutputType outputType;

        public OutputType getOutputType() {
            return outputType;
        }

        public YauaaIsBusyStarting(OutputType outputType) {
            this.outputType = outputType;
        }
    }

    private void ensureStartedForApis(OutputType outputType) {
        if (!userAgentAnalyzerIsAvailable) {
            throw new YauaaIsBusyStarting(outputType);
        }
    }

    @ControllerAdvice
    public static class RestResponseEntityExceptionHandler
        extends ResponseEntityExceptionHandler {

        @ExceptionHandler({ YauaaIsBusyStarting.class })
        public ResponseEntity<Object> handleYauaaIsStarting(
            Exception ex,
            @SuppressWarnings("unused") WebRequest request) {
            final HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Retry-After", "5"); // Retry after 5 seconds.

            YauaaIsBusyStarting yauaaIsBusyStarting = (YauaaIsBusyStarting) ex;

            long timeSinceStart = System.currentTimeMillis() - initStartMoment;
            String message;

            if (userAgentAnalyzerFailureMessage == null) {
                switch (yauaaIsBusyStarting.getOutputType()) {
                    case YAML:
                        message = "status: \"Starting\"\ntimeInMs: " + timeSinceStart + "\n";
                        break;
                    case TXT:
                        message = "NO";
                        break;
                    case JSON:
                        message = "{ \"status\": \"Starting\", \"timeInMs\": " + timeSinceStart + " }";
                        break;
                    case XML:
                        message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><status>Starting</status><timeInMs>" + timeSinceStart + "</timeInMs>";
                        break;
                    case HTML:
                    default:
                        message = "Yauaa has been starting up for " + timeSinceStart + " seconds now.";
                        break;
                }
                return new ResponseEntity<>(message, httpHeaders, SERVICE_UNAVAILABLE);
            } else {
                switch (yauaaIsBusyStarting.getOutputType()) {
                    case YAML:
                        message = "status: \"Failed\"\nerrorMessage: |\n" + userAgentAnalyzerFailureMessage + "\n";
                        break;
                    case TXT:
                        message = "FAILED: \n" + userAgentAnalyzerFailureMessage;
                        break;
                    case JSON:
                        message = "{ \"status\": \"Failed\", \"errorMessage\": " + escapeJson(userAgentAnalyzerFailureMessage) + " }";
                        break;
                    case XML:
                        message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><status>Failed</status><errorMessage>" + escapeXml10(userAgentAnalyzerFailureMessage) + "</errorMessage>";
                        break;
                    case HTML:
                    default:
                        message = "Yauaa start up has failed with message \n" + userAgentAnalyzerFailureMessage;
                        break;
                }
                return new ResponseEntity<>(message, httpHeaders, INTERNAL_SERVER_ERROR);
            }

        }
    }

    private static final long MEGABYTE = 1024L * 1024L;

    public static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }

    private static String getMemoryUsage() {
        // Get the Java runtime
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        return String.format("Used memory is %d MiB", bytesToMegabytes(memory));
    }

    @ResponseStatus(value = HttpStatus.PRECONDITION_FAILED, reason = "The User-Agent header is missing")
    private static class MissingUserAgentException extends RuntimeException {
    }

    // =============== HTML (Human usable) OUTPUT ===============

    @GetMapping(
        value = "/",
        produces = MediaType.TEXT_HTML_VALUE
    )
    public String getHtml(@RequestHeader("User-Agent") String userAgentString) {
        return doHTML(userAgentString);
    }

    @PostMapping(
        value = "/",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.TEXT_HTML_VALUE
    )
    public String getHtmlPOST(@ModelAttribute("useragent") String userAgent) {
        return doHTML(userAgent);
    }

    // =============== YAML OUTPUT ===============

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
                @ExampleProperty(mediaType = APPLICATION_JSON_VALUE,    value = EXAMPLE_JSON),
                @ExampleProperty(mediaType = APPLICATION_XML_VALUE,     value = EXAMPLE_XML),
                @ExampleProperty(mediaType = TEXT_XYAML_VALUE,          value = EXAMPLE_YAML),
                @ExampleProperty(mediaType = TEXT_PLAIN_VALUE,          value = EXAMPLE_YAML)
            })
        )
    })
    @GetMapping(
        value = API_BASE_PATH + "/analyze",
        produces = { TEXT_XYAML_VALUE, TEXT_PLAIN_VALUE }
    )
    public String getYamlGET(
        @ApiParam(
            value = "The standard browser request header User-Agent is used as the input that is to be analyzed.",
            example = EXAMPLE_USERAGENT
        )
        @RequestHeader("User-Agent")
            String userAgentString
    ) {
        return doYaml(userAgentString);
    }

    // -------------------------------------------------

    @ApiOperation(
        value = "Analyze the provided User-Agent"
    )
    @PostMapping(
        value = API_BASE_PATH + "/analyze",
        consumes = TEXT_PLAIN_VALUE,
        produces = { TEXT_XYAML_VALUE, TEXT_PLAIN_VALUE }
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
    public String getYamlPOST(
        @ApiParam(
            name ="Request body",
            type = "Map",
            defaultValue = EXAMPLE_USERAGENT,
            value = "The entire POSTed value is used as the input that is to be analyzed.",
            examples = @Example(@ExampleProperty(mediaType = TEXT_PLAIN_VALUE, value = EXAMPLE_USERAGENT))
        )
        @RequestBody String userAgentString
    ) {
        return doYaml(userAgentString);
    }


    // =============== JSON OUTPUT ===============

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
                @ExampleProperty(mediaType = APPLICATION_JSON_VALUE,    value = EXAMPLE_JSON),
                @ExampleProperty(mediaType = APPLICATION_XML_VALUE,     value = EXAMPLE_XML),
                @ExampleProperty(mediaType = TEXT_XYAML_VALUE,          value = EXAMPLE_YAML),
                @ExampleProperty(mediaType = TEXT_PLAIN_VALUE,          value = EXAMPLE_YAML)
            })
        )
    })
    @GetMapping(
        value = API_BASE_PATH + "/analyze",
        produces = APPLICATION_JSON_VALUE
    )
    public String getJSonGET(
        @ApiParam(
            value = "The standard browser request header User-Agent is used as the input that is to be analyzed.",
            example = EXAMPLE_USERAGENT
        )
        @RequestHeader("User-Agent")
        String userAgentString
    ) {
        return doJSon(userAgentString);
    }

    // -------------------------------------------------

    @ApiOperation(
        value = "Analyze the provided User-Agent"
    )
    @PostMapping(
        value = API_BASE_PATH + "/analyze",
        consumes = TEXT_PLAIN_VALUE,
        produces = APPLICATION_JSON_VALUE
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
    public String getJSonPOST(
        @ApiParam(
            name ="Request body",
            type = "Map",
            defaultValue = EXAMPLE_USERAGENT,
            value = "The entire POSTed value is used as the input that is to be analyzed.",
            examples = @Example(@ExampleProperty(mediaType = TEXT_PLAIN_VALUE, value = EXAMPLE_USERAGENT))
        )
        @RequestBody String userAgentString
    ) {
        return doJSon(userAgentString);
    }

    // =============== XML OUTPUT ===============

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
        value = API_BASE_PATH + "/analyze",
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
        return doXML(userAgentString);
    }

    // -------------------------------------------------

    @ApiOperation(
        value = "Analyze the provided User-Agent"
    )
    @PostMapping(
        value = API_BASE_PATH + "/analyze",
        consumes = TEXT_PLAIN_VALUE,
        produces = APPLICATION_XML_VALUE
    )
    public String getXMLPOST(
        @ApiParam(
            name ="Request body",
            type = "Map",
            defaultValue = EXAMPLE_USERAGENT,
            value = "The entire POSTed value is used as the input that is to be analyzed.",
            examples = @Example(@ExampleProperty(mediaType = TEXT_PLAIN_VALUE, value = EXAMPLE_USERAGENT))
        )
        @RequestBody String userAgentString
    ) {
        return doXML(userAgentString);
    }

    // =============== Specials ===============

    @ApiOperation(
        value = "Fire all available test cases against the analyzer to heat up the JVM"
    )
    @ApiResponses({
        @ApiResponse(
            code = 200, // HttpStatus.OK
            message = "The number of reported tests were done to preheat the engine",
            examples = @Example(
                value = {
                    @ExampleProperty(mediaType = APPLICATION_JSON_VALUE, value = "{\n" +
                        "  \"status\": \"Ran tests\",\n" +
                        "  \"testsDone\": 2337,\n" +
                        "  \"timeInMs\": 3123\n" +
                        "}"),
                }
            )
        )
    })
    @GetMapping(
        value = API_BASE_PATH + "/preheat",
        produces = APPLICATION_JSON_VALUE
    )
    public String getHtmlPreHeat() {
        ensureStartedForApis(OutputType.HTML);
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

    private String doHTML(String userAgentString) {
        long start      = System.nanoTime();
        long startParse = 0;
        long stopParse  = 0;

        if (userAgentString == null) {
            throw new MissingUserAgentException();
        }

        StringBuilder sb = new StringBuilder(4096);
        try {
            sb.append("<!DOCTYPE html>");
            sb.append("<html lang=\"en\" ><head profile=\"http://www.w3.org/2005/10/profile\">");
            sb.append("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
            sb.append("<meta name=viewport content=\"width=device-width, initial-scale=1\">");
            insertFavIcons(sb);
            sb.append("<meta name=\"theme-color\" content=\"dodgerblue\" />");

            // While initializing automatically reload the page.
            if (!userAgentAnalyzerIsAvailable && userAgentAnalyzerFailureMessage == null) {
                sb.append("<meta http-equiv=\"refresh\" content=\"1\" >");
            }
            sb.append("<link rel=\"stylesheet\" href=\"style.css\">");
            sb.append("<title>Analyzing the useragent</title>");

            sb.append("</head>");
            sb.append("<body>");
            sb.append("<div class=\"header\">");
            sb.append("<h1 class=\"title\">Yet Another UserAgent Analyzer.</h1>");
            sb.append("<p class=\"version\">").append(ANALYZER_VERSION).append("</p>");
            sb.append("</div>");

            sb.append("<hr/>");

            if (userAgentAnalyzerIsAvailable) {
                startParse = System.nanoTime();
                UserAgent userAgent = userAgentAnalyzer.parse(userAgentString);
                stopParse = System.nanoTime();

                sb.append("<h2 class=\"title\">The UserAgent</h2>");
                sb.append("<p class=\"input\">").append(escapeHtml4(userAgent.getUserAgentString())).append("</p>");
                sb.append("<h2 class=\"title\">The analysis result</h2>");
                sb.append("<table id=\"result\">");
                sb.append("<tr><th colspan=2>Field</th><th>Value</th></tr>");

                Map<String, Integer>                     fieldGroupCounts = new HashMap<>();
                List<Pair<String, Pair<String, String>>> fields           = new ArrayList<>(32);
                for (String fieldname : userAgent.getAvailableFieldNamesSorted()) {
                    Pair<String, String> split = prefixSplitter(fieldname);
                    fields.add(new ImmutablePair<>(fieldname, split));
                    Integer count = fieldGroupCounts.get(split.getLeft());
                    if (count == null) {
                        count = 1;
                    } else {
                        count++;
                    }
                    fieldGroupCounts.put(split.getLeft(), count);
                }

                String currentGroup = "";
                for (Pair<String, Pair<String, String>> field : fields) {
                    String fieldname  = field.getLeft();
                    String groupName  = field.getRight().getLeft();
                    String fieldLabel = field.getRight().getRight();
                    sb.append("<tr>");
                    if (!currentGroup.equals(groupName)) {
                        currentGroup = groupName;
                        sb.append("<td rowspan=").append(fieldGroupCounts.get(currentGroup)).append("><b><u>")
                            .append(escapeHtml4(currentGroup)).append("</u></b></td>");
                    }
                    sb.append("<td>").append(camelStretcher(escapeHtml4(fieldLabel))).append("</td>")
                        .append("<td>").append(escapeHtml4(userAgent.getValue(fieldname))).append("</td>")
                        .append("</tr>");
                }
                sb.append("</table>");
                sb.append("<hr/>");

                sb.append("<p class=\"logobar documentation\">Read the online documentation at <a href=\"https://yauaa.basjes.nl\">" +
                    "https://yauaa.basjes.nl</a></p>");
                sb.append("<p class=\"logobar bug\">");
                addBugReportButton(sb, userAgent);
                sb.append("</p>");
                sb.append("<p class=\"logobar swagger\">A simple Swagger based API has been created for testing purposes: " +
                    "<a href=\"/swagger-ui.html\">Swagger UI</a></p>");

                sb.append("<p class=\"logobar source\">This project is opensource: <a href=\"https://github.com/nielsbasjes/yauaa\">" +
                    "https://github.com/nielsbasjes/yauaa</a></p>");
                sb.append("<p class=\"logobar contribute\">Creating this free software is a lot of work. " +
                    "If this has business value for your then don't hesitate to " +
                    "<a href=\"https://www.paypal.me/nielsbasjes\">contribute a little something back</a>.</p>");
                sb.append("<hr/>");
                sb.append("<form class=\"logobar tryyourown\" action=\"\" method=\"post\">");
                sb.append("<label for=\"useragent\">Manual testing of a useragent:</label>");

                sb.append("<textarea id=\"useragent\" name=\"useragent\" maxlength=\"2000\" rows=\"4\" " +
                    "placeholder=\"Paste the useragent you want to test...\">")
                    .append(escapeHtml4(userAgentString)).append("</textarea>");
                sb.append("<input class=\"testButton\" type=\"submit\" value=\"Analyze\">");
                sb.append("</form>");
                sb.append("<br/>");

                sb.append("<hr/>");
                userAgentString =
                    "Mozilla/5.0 (Linux; Android 7.8.9; nl-nl ; " +
                        "Niels Ultimate 42 demo phone Build/42 ; " +
                        "https://yauaa.basjes.nl ) " +
                        "AppleWebKit/8.4.7.2 (KHTML, like Gecko) " +
                        "Yet another browser/3.1415926 Mobile Safari/6.6.6";
                sb.append("<form class=\"logobar tryexample\" action=\"\" method=\"post\">");
                sb.append("<input type=\"hidden\" id=\"demouseragent\" name=\"useragent\" " +
                    "value=\"").append(escapeHtml4(userAgentString)).append("\">");
                sb.append("<input class=\"demoButton\" type=\"submit\" value=\"Try this demonstration UserAgent\">");
                sb.append("</form>");

                sb.append("<hr/>");

                sb.append("<hr/>");
            } else {
                long   now        = System.currentTimeMillis();
                long   millisBusy = now - initStartMoment;
                String timeString = String.format("%3.1f", ((double) millisBusy / 1000.0));

                if (userAgentAnalyzerFailureMessage == null) {
                    sb.append("<div class=\"notYetStartedBorder\">");
                    sb.append("<p class=\"notYetStarted\">The analyzer is currently starting up.</p>");
                    sb.append("<p class=\"notYetStarted\">It has been starting up for <u>").append(timeString).append("</u> seconds</p>");
                    sb.append("<p class=\"notYetStarted\">Anything between 5-15 seconds is normal.</p>");
                    sb.append("<p class=\"notYetStartedAppEngine\">On a free AppEngine 50 seconds is 'normal'.</p>");
                    sb.append("</div>");
                } else {
                    sb.append("<div class=\"failureBorder\">");
                    sb.append("<p class=\"failureContent\">The analyzer startup failed with this message</p>");
                    sb.append("<p class=\"failureContent\">").append(userAgentAnalyzerFailureMessage).append("</p>");
                    sb.append("</div>");
                }
            }
        } catch (Exception e) {
            sb.append("<div class=\"failureBorder\">");
            sb.append("<p class=\"failureContent\">An exception occurred during parsing</p>");
            sb.append("<p class=\"failureContent\">Exception: ").append(e.getClass().getSimpleName()).append("</p>");
            sb.append("<p class=\"failureContent\">Message: ").append(e.getMessage().replaceAll("\\n", "<br/>")).append("</p>");
            sb.append("</div>");
        } finally {
            long   stop              = System.nanoTime();
            double pageMilliseconds  = (stop - start) / 1000000.0;
            double parseMilliseconds = (stopParse - startParse) / 1000000.0;
            sb.append("<p class=\"speed\">Building this page took ")
                .append(String.format(Locale.ENGLISH, "%3.3f", pageMilliseconds))
                .append(" ms.</p>");

            if (parseMilliseconds > 0) {
                sb.append("<p class=\"speed\">Parsing took ")
                    .append(String.format(Locale.ENGLISH, "%3.3f", parseMilliseconds))
                    .append(" ms.</p>");
            }

            sb.append("<p class=\"speed\">").append(getMemoryUsage()).append("</p>");

            sb
                .append("<p class=\"copyright\">")
                .append(Version.COPYRIGHT.replace("Niels Basjes", "<a href=\"https://niels.basjes.nl\">Niels Basjes</a>"))
                .append("</p>")
                .append("</body>")
                .append("</html>");
        }
        return sb.toString();
    }

    private void insertFavIcons(StringBuilder sb) {
        sb.append("<link rel=\"apple-touch-icon\" href=\"/apple-touch-icon.png\">");
        sb.append("<link rel=\"icon\" type=\"image/png\" sizes=\"32x32\" href=\"/favicon-32x32.png\">");
        sb.append("<link rel=\"icon\" type=\"image/png\" sizes=\"16x16\" href=\"/favicon-16x16.png\">");
        sb.append("<link rel=\"manifest\" href=\"/manifest.json\">");
        sb.append("<link rel=\"mask-icon\" href=\"/safari-pinned-tab.svg\" color=\"#5bbad5\">");
        sb.append("<meta name=\"msapplication-TileColor\" content=\"#2d89ef\">");
        sb.append("<meta name=\"msapplication-TileImage\" content=\"/mstile-144x144.png\">");
    }

    private Pair<String, String> prefixSplitter(String input) {
        MutablePair<String, String> result = new MutablePair<>("", input);
        if (input.startsWith("Device")) {
            result.setLeft("Device");
            result.setRight(input.replaceFirst("Device", ""));
        } else if (input.startsWith("OperatingSystem")) {
            result.setLeft("Operating System");
            result.setRight(input.replaceFirst("OperatingSystem", ""));
        } else if (input.startsWith("LayoutEngine")) {
            result.setLeft("Layout Engine");
            result.setRight(input.replaceFirst("LayoutEngine", ""));
        } else if (input.startsWith("Agent")) {
            result.setLeft("Agent");
            result.setRight(input.replaceFirst("Agent", ""));
        }
        return result;
    }

    private String camelStretcher(String input) {
        String result = input.replaceAll("([A-Z])", " $1");
        result = result.replace("Device", "<b><u>Device</u></b>");
        result = result.replace("Operating System", "<b><u>Operating System</u></b>");
        result = result.replace("Layout Engine", "<b><u>Layout Engine</u></b>");
        result = result.replace("Agent", "<b><u>Agent</u></b>");
        return result.trim();
    }

    private List<String> splitPerFilledLine(String input) {
        String[] lines = input.split("\\r?\\n");
        List<String> result = new ArrayList<>(lines.length);
        for (String line: lines) {
            String trimmedLine = line.trim();
            if (!trimmedLine.isEmpty()) {
                result.add(trimmedLine);
            }
        }
        if (result.size() == 0) {
            // Apparently the only input was an empty string, we want to keep that.
            result.add("");
        }
        return result;
    }

    private String doYaml(String userAgentString) {
        if (userAgentString == null) {
            throw new MissingUserAgentException();
        }
        ensureStartedForApis(OutputType.JSON);
        if (userAgentAnalyzerIsAvailable) {
            List<String> result = new ArrayList<>(2048);
            splitPerFilledLine(userAgentString)
                .forEach(ua -> result.add(userAgentAnalyzer.parse(ua).toYamlTestCase()));
            return String.join("\n", result);
        }
        return "";
    }

    private String doJSon(String userAgentString) {
        if (userAgentString == null) {
            throw new MissingUserAgentException();
        }
        ensureStartedForApis(OutputType.JSON);
        if (userAgentAnalyzerIsAvailable) {
            List<String> result = new ArrayList<>(2048);
            splitPerFilledLine(userAgentString)
                .forEach(ua -> result.add(userAgentAnalyzer.parse(ua).toJson()));
            return "[" + String.join(",\n", result) + "]";
        }
        return "[{}]";
    }

    private String doXML(String userAgentString) {
        if (userAgentString == null) {
            throw new MissingUserAgentException();
        }
        ensureStartedForApis(OutputType.XML);
        if (userAgentAnalyzerIsAvailable) {
            List<String> result = new ArrayList<>(2048);
            splitPerFilledLine(userAgentString)
                .forEach(ua -> result.add(userAgentAnalyzer.parse(ua).toXML()));
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + String.join("\n", result);
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Yauaa></Yauaa>";
    }

    private void addBugReportButton(StringBuilder sb, UserAgent userAgent) {
        // https://github.com/nielsbasjes/yauaa/issues/new?title=Bug%20report&body=bar

        try {
            StringBuilder reportUrl = new StringBuilder("https://github.com/nielsbasjes/yauaa/issues/new?title=Bug%20report&body=");

            String report = "I found a problem with this useragent.\n" +
                "[Please update the output below to match what you expect it should be]\n" +
                "\n```\n" +
                userAgent.toYamlTestCase().replaceAll(" +:", "  :") +
                "\n```\n";

            reportUrl.append(URLEncoder.encode(report, "UTF-8"));
            String githubUrl = "https://github.com/login?return_to=" + URLEncoder.encode(reportUrl.toString(), "UTF-8");
            sb.append("If you find a problem with this result then please report a bug here: " +
                "<a href=\"").append(githubUrl).append("\">Yauaa issue report</a>");
        } catch (UnsupportedEncodingException e) {
            // Never happens.
        }
    }


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

    /**
     * <a href="https://cloud.google.com/appengine/docs/flexible/java/how-instances-are-managed#health_checking">
     * App Engine health checking</a> requires responding with 200 to {@code /_ah/health}.
     *
     * @return Returns a non empty message body.
     */
    @GetMapping(
        path = "/_ah/health",
        produces = TEXT_PLAIN_VALUE
    )
    public String isHealthy() {
        ensureStartedForApis(OutputType.TXT);
        return "YES";
    }

    public static void main(String[] args) {
        SpringApplication.run(ParseService.class, args);
    }
}
