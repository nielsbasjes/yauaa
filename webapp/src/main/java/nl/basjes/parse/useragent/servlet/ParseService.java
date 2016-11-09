/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2016 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import nl.basjes.parse.useragent.Version;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

@Path("parse")
public class ParseService {

    private static final UserAgentAnalyzer USER_AGENT_ANALYZER = new UserAgentAnalyzer();

    private static String gittag = Version.getGitCommitIdDescribeShort();
    private static String version = Version.getProjectVersion();
    private static String buildtime = Version.getBuildTimestamp();

    protected synchronized UserAgent parse(String userAgentString) {
        return USER_AGENT_ANALYZER.parse(userAgentString); // This class and method are NOT threadsafe/reentrant !
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getHtml(@HeaderParam("User-Agent") String userAgentString) {
        return doHTML(userAgentString);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response getHtmlPOST(@FormParam("useragent") String userAgentString) {
        return doHTML(userAgentString);
    }

    @GET
    @Path("/{UserAgent}")
    @Produces(MediaType.TEXT_HTML)
    public Response getHtmlPath(@PathParam("UserAgent") String userAgent) {
        return doHTML(userAgent);
    }

    @GET
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJSon(@HeaderParam("User-Agent") String userAgentString) {
        return doJSon(userAgentString);
    }

    @POST
    @Path("/json")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJSonPOST(@FormParam("useragent") String userAgentString) {
        return doJSon(userAgentString);
    }

    @GET
    @Path("/json/{UserAgent}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJSonPath(@PathParam("UserAgent") String userAgent) {
        return doJSon(userAgent);
    }

    private Response doHTML(String userAgentString) {
        long start = System.nanoTime();

        if (userAgentString == null) {
            return Response
                .status(Response.Status.PRECONDITION_FAILED)
                .entity("<b><u>The User-Agent header is missing</u></b>")
                .build();
        }

        Response.ResponseBuilder responseBuilder = Response.status(200);

        StringBuilder sb = new StringBuilder(4096);
        try {
            sb.append("<!DOCTYPE html>");
            sb.append("<html><head profile=\"http://www.w3.org/2005/10/profile\">");
            sb.append("<link rel=\"icon\" type=\"image/ico\" href=\"/static/favicon.ico\" />\n");
            sb.append("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
            sb.append("<title>Analyzing the useragent</title></head>");
            sb.append("<body>");

            sb.append("<h1>Analyzing your useragent string</h1>");

            sb.append("Build using <a href=\"https://github.com/nielsbasjes/yauaa\">Yauaa (Yet another UserAgent analyzer)</a>.<br/>");
            sb.append("Version    : ").append(version).append("<br/>");
            sb.append("Git tag    : ").append(gittag).append("<br/>");
            sb.append("Build time : ").append(buildtime).append("<br/>");
            sb.append("<hr/>");

            UserAgent userAgent = parse(userAgentString);

            sb.append("Received useragent: <B>").append(escapeHtml4(userAgent.getUserAgentString())).append("</B>");
            sb.append("<table border=1>");
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


            addBugReportButton(sb, userAgent);
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

            sb.append("<br/>");
            sb.append("<hr/>");
            sb.append("<form action=\"/\" method=\"post\">");
            sb.append("Manual testing of a useragent:<br>");
            sb.append("<input type=\"text\" name=\"useragent\"  size=\"100\" value=\"").append(escapeHtml4(userAgentString)).append("\">");
            sb.append("<input type=\"submit\" value=\"Analyze\">");
            sb.append("</form>");
            sb.append("<br/>");

            sb.append("<hr/>");
            userAgentString = "Mozilla/5.0 (Linux; Android 7.8.9; nl-nl ; Niels Ultimate 42 demo phone Build/42 ; nl-nl; " +
                "https://github.com/nielsbasjes/yauaa ) AppleWebKit/8.4.7.2 (KHTML, like Gecko) Yet another browser/3.1415926 Mobile Safari/6.6.6";
            sb.append("<form action=\"/\" method=\"post\">");
            sb.append("Try this demo: ");
            sb.append("<input type=\"hidden\" name=\"useragent\"  size=\"100\" value=\"").append(escapeHtml4(userAgentString)).append("\">");
            sb.append("<input type=\"submit\" value=\"Analyze Demo\">");
            sb.append("</form>");

            sb.append("<hr/>");
        } finally {
            long stop = System.nanoTime();
            double milliseconds = (stop - start) / 1000000.0;

            sb.append("<br/>");
            sb.append("<u>Building this page took ").append(String.format(Locale.ENGLISH, "%3.3f", milliseconds)).append(" ms.</u><br/>");
            sb.append("</body>");
            sb.append("</html>");
        }
        return responseBuilder.entity(sb.toString()).build();
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

    private Response doJSon(String userAgentString) {
        if (userAgentString == null) {
            return Response
                .status(Response.Status.PRECONDITION_FAILED)
                .entity("{ error: \"The User-Agent header is missing\" }")
                .build();
        }
        Response.ResponseBuilder responseBuilder = Response.status(200);
        UserAgent userAgent = parse(userAgentString);
        return responseBuilder.entity(userAgent.toJson()).build();
    }

    private StringBuilder addBugReportButton(StringBuilder sb, UserAgent userAgent) {
        // https://github.com/nielsbasjes/yauaa/issues/new?title=Bug%20report&body=bar
        sb.append("If you find a problem with this result then please " +
            "report a bug here: <a href=\"https://github.com/nielsbasjes/yauaa/issues/new?title=Bug%20report&body=");
        String report =  "I found a problem with this useragent.\n" +
            "[Please update the output below to match what you expect it should be]\n" +
            "\n```\n" +
            userAgent.toYamlTestCase() +
            "\n```\n";
        try {
            sb.append(URLEncoder.encode(report, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // Never happens.
        }
        sb.append("\">Yauaa issue report</a>");
        return sb;
    }
}
