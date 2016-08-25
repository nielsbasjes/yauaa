/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2016 Niels Basjes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package nl.basjes.parse.useragent.servlet;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Locale;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

@Path("/")
public class ParseService {

    private static final UserAgentAnalyzer USER_AGENT_ANALYZER = new UserAgentAnalyzer();

    protected synchronized UserAgent parse(String userAgentString) {
        return USER_AGENT_ANALYZER.parse(userAgentString); // This class and method are NOT threadsafe/reentrant !
    }

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public Response getHtml(@HeaderParam("User-Agent") String userAgentString) {
        return doHTML(userAgentString);
    }

    @POST
    @Path("/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHtmlPOST(String userAgentString) {
        return doJSon(userAgentString);
    }

    @GET
    @Path("/parse/{UserAgent : .+}")
    @Produces(MediaType.TEXT_HTML)
    public Response getHtmlPath(@Context UriInfo uriInfo) {
        // PathParams go wrong with ';' and '/' in it. So we just get the entire Path and extract the stuff ourselves
        String userAgentString = uriInfo.getPath().replaceAll("^parse/", "");
        return doHTML(userAgentString);
    }

    @GET
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJSon(@HeaderParam("User-Agent") String userAgentString) {
        return doJSon(userAgentString);
    }

    @POST
    @Path("/json")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJSonPOST(String userAgentString) {
        return doJSon(userAgentString);
    }

    @GET
    @Path("/json/parse/{UserAgent : .+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJSonPath(@Context UriInfo uriInfo) {
        // PathParams go wrong with ';' and '/' in it. So we just get the entire Path and extract the stuff ourselves
        String userAgentString = uriInfo.getPath().replaceAll("^json/parse/", "");
        return doJSon(userAgentString);
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
            sb.append("<html><head>");
            sb.append("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
            sb.append("<title>Analyzing the useragent</title></head>");
            sb.append("<body>");

            // Actual logic goes here.
            sb.append("<h1>Analyzing your useragent string</h1>");

            UserAgent userAgent = parse(userAgentString);

//            System.sb.append("Useragent: " + userAgentString);
            sb.append("Received useragent header: <h2>").append(escapeHtml4(userAgent.getUserAgentString())).append("</h2>");
            sb.append("<table border=1>");
            sb.append("<tr><th>Field</th><th>Value</th></tr>");
            for (String fieldname : userAgent.getAvailableFieldNamesSorted()) {
                sb.append("<tr>").append(
                    "<td>").append(camelStretcher(escapeHtml4(fieldname))).append("</td>").append(
                    "<td>").append(escapeHtml4(userAgent.getValue(fieldname))).append("</td>").append(
                    "</tr>");
            }
            sb.append("</table>");

            sb.append("<ul>");
            sb.append("<li><a href=\"/\">HTML (from header)</a></li>");
            sb.append("<li><a href=\"/parse/").append(escapeHtml4(userAgentString)).append("\">HTML (from url)</a></li>");
            sb.append("<li><a href=\"/json\">Json (from header)</a></li>");
            sb.append("<li><a href=\"/json/parse/").append(escapeHtml4(userAgentString)).append("\">Json (from url)</a></li>");
            sb.append("</ul>");

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


}
