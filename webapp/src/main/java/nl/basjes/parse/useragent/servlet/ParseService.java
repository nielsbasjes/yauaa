/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2018 Niels Basjes
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

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

@SpringBootApplication
@RestController
public class ParseService {

    private static final UserAgentAnalyzer USER_AGENT_ANALYZER = UserAgentAnalyzer.newBuilder().dropTests().hideMatcherLoadStats().build();
    private static final String ANALYZER_VERSION = UserAgentAnalyzer.getVersion();

    @ResponseStatus(value = HttpStatus.PRECONDITION_FAILED, reason = "The User-Agent header is missing")
    private class MissingUserAgentException extends RuntimeException {
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
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

    @GetMapping(value = "/preheat", produces = MediaType.TEXT_HTML_VALUE)
    public String getHtmlPreHeat(@RequestHeader("User-Agent") String userAgentString) {
        USER_AGENT_ANALYZER.preHeat();
        return doHTML(userAgentString);
    }

    @GetMapping(value = "/{userAgent}", produces = MediaType.TEXT_HTML_VALUE)
    public String getHtmlPath(@PathVariable String userAgent) {
        return doHTML(userAgent);
    }

    @GetMapping(value = "/json", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String getJSon(@RequestHeader("User-Agent") String userAgentString) {
        return doJSon(userAgentString);
    }

    @PostMapping(
        value = "/json",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public String getJSonPOST(@ModelAttribute("useragent") String userAgentString) {
        return doJSon(userAgentString);
    }

    @GetMapping(value = "/json/{userAgent}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String getJSonPath(@PathVariable String userAgent) {
        return doJSon(userAgent);
    }

    private String doHTML(String userAgentString) {
        long start = System.nanoTime();
        long startParse=0;
        long stopParse=0;

        if (userAgentString == null) {
            throw new MissingUserAgentException();
        }

        StringBuilder sb = new StringBuilder(4096);
        try {
            sb.append("<!DOCTYPE html>");
            sb.append("<html><head profile=\"http://www.w3.org/2005/10/profile\">");
            sb.append("<meta name=viewport content=\"width=device-width, initial-scale=1\">");
            insertFavIcons(sb);
            sb.append("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
            sb.append("<link rel=\"stylesheet\" href=\"style.css\">");
            sb.append("<title>Analyzing the useragent</title>");

            sb.append("</head>");
            sb.append("<body>");
            sb.append("<div class=\"header\">");
            sb.append("<h1 class=\"title\">Yet Another UserAgent Analyzer.</h1>" );
            sb.append("<p class=\"version\">").append(ANALYZER_VERSION).append("</p>");
            sb.append("</div>\n");

            sb.append("<hr/>");

            startParse = System.nanoTime();
            UserAgent userAgent = USER_AGENT_ANALYZER.parse(userAgentString);
            stopParse = System.nanoTime();

            sb.append("<p class=\"input\">").append(escapeHtml4(userAgent.getUserAgentString())).append("</p>");
            sb.append("<table id=\"result\">");
            sb.append("<tr><th colspan=2>Field</th><th>Value</th></tr>");

            Map<String, Integer> fieldGroupCounts = new HashMap<>();
            List<Pair<String, Pair<String, String>>> fields = new ArrayList<>(32);
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
                String fieldname = field.getLeft();
                String groupName = field.getRight().getLeft();
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

            sb.append("<p class=\"bug\">");
            addBugReportButton(sb, userAgent);
            sb.append("</p>");
//            sb.append("<ul>");
//            sb.append("<li><a href=\"/\">HTML (from header)</a></li>");
//            sb.append("<li><a href=\"/json\">Json (from header)</a></li>");
//
//            String urlEncodedUserAgent = "";
//            try {
//                urlEncodedUserAgent = URLEncoder.encode(userAgentString, "utf-8");
//                urlEncodedUserAgent = urlEncodedUserAgent.replace("+", "%20");
//                sb.append("<li><a href=\"/").append(urlEncodedUserAgent).append("\">HTML (from url)</a></li>");
//                sb.append("<li><a href=\"/json/").append(urlEncodedUserAgent).append("\">Json (from url)</a></li>");
//            } catch (UnsupportedEncodingException e) {
//                // Do nothing
//            }
//            sb.append("</ul>");
            sb.append("<p class=\"source\">This project is opensource: <a href=\"https://github.com/nielsbasjes/yauaa\">https://github.com/nielsbasjes/yauaa</a></p>\n");
            sb.append("<p class=\"contribute\">Creating this free software is a lot of work. If this has business value for your then don't hesitate to <a href=\"https://www.paypal.me/nielsbasjes\">contribute a little something back</a>.</p>\n");
            sb.append("<hr/>");
            sb.append("<form class=\"tryyourown\" action=\"\" method=\"post\">");
            sb.append("<label for=\"useragent\">Manual testing of a useragent:</label>");
            sb.append("<input type=\"text\" id=\"useragent\" name=\"useragent\" placeholder=\"Paste the useragent you want to test...\" size=\"1000\" value=\"").append(escapeHtml4(userAgentString)).append("\">");
            sb.append("<input type=\"submit\" value=\"Analyze\">");
            sb.append("</form>");
            sb.append("<br/>");

//            sb.append("<hr/>");
//            userAgentString = "Mozilla/5.0 (Linux; Android 7.8.9; nl-nl ; Niels Ultimate 42 demo phone Build/42 ; nl-nl; " +
//                "https://github.com/nielsbasjes/yauaa ) AppleWebKit/8.4.7.2 (KHTML, like Gecko) Yet another browser/3.1415926 Mobile Safari/6.6.6";
//            sb.append("<form class=\"tryyourown\" action=\"\" method=\"post\">");
//            sb.append("Try this demo: ");
//            sb.append("<input type=\"hidden\" name=\"useragent\"  size=\"100\" value=\"").append(escapeHtml4(userAgentString)).append("\">");
//            sb.append("<input type=\"submit\" value=\"Analyze Demo\">");
//            sb.append("</form>");

            sb.append("<hr/>");
        } finally {
            long stop = System.nanoTime();
            double pageMilliseconds = (stop - start) / 1000000.0;
            double parseMilliseconds = (stopParse - startParse) / 1000000.0;
            sb.append("<p class=\"speed\">Building this page took ").append(String.format(Locale.ENGLISH, "%3.3f", pageMilliseconds)).append(" ms.</p>");
            sb.append("<p class=\"speed\">Parsing took ").append(String.format(Locale.ENGLISH, "%3.3f", parseMilliseconds)).append(" ms.</p>");
            sb.append("<p class=\"copyright\">Copyright (C) 2013-2018 <a href=\"https://niels.basjes.nl\">Niels Basjes</a></p>");
            sb.append("</body>");
            sb.append("</html>");
        }
        return sb.toString();
    }

    private void insertFavIcons(StringBuilder sb) {
        sb.append("<link rel=\"apple-touch-icon\" sizes=\"180x180\" href=\"/apple-touch-icon.png\">");
        sb.append("<link rel=\"icon\" type=\"image/png\" sizes=\"32x32\" href=\"/favicon-32x32.png\">");
        sb.append("<link rel=\"icon\" type=\"image/png\" sizes=\"16x16\" href=\"/favicon-16x16.png\">");
        sb.append("<link rel=\"manifest\" href=\"/manifest.json\">");
        sb.append("<link rel=\"mask-icon\" href=\"/safari-pinned-tab.svg\" color=\"#5bbad5\">");
        sb.append("<meta name=\"msapplication-TileColor\" content=\"#2d89ef\">");
        sb.append("<meta name=\"msapplication-TileImage\" content=\"/mstile-144x144.png\">");
        sb.append("<meta name=\"theme-color\" content=\"#ffffff\">");
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
        return result;
    }

    private String doJSon(String userAgentString) {
        if (userAgentString == null) {
            throw new MissingUserAgentException();
        }
        UserAgent userAgent = USER_AGENT_ANALYZER.parse(userAgentString);
        return userAgent.toJson();
    }

    private void addBugReportButton(StringBuilder sb, UserAgent userAgent) {
        // https://github.com/nielsbasjes/yauaa/issues/new?title=Bug%20report&body=bar

        try {
            StringBuilder reportUrl = new StringBuilder("https://github.com/nielsbasjes/yauaa/issues/new?title=Bug%20report&body=");

            String report =  "I found a problem with this useragent.\n" +
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

    /**
     * <a href="https://cloud.google.com/appengine/docs/flexible/java/how-instances-are-managed#health_checking">
     * App Engine health checking</a> requires responding with 200 to {@code /_ah/health}.
     */
    @RequestMapping("/_ah/health")
    public String healthy() {
        // Message body required though ignored
        return "Still surviving.";
    }


    public static void main(String[] args) {
        SpringApplication.run(ParseService.class, args);
    }
}
