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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.PrintWriter;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

public class HtmlServlet extends HttpServlet {

    private UserAgentAnalyzer uua;

    public void init() throws ServletException {
        uua = new UserAgentAnalyzer();
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        try {
            response.setContentType(MediaType.TEXT_HTML);
            out.println("<!DOCTYPE html>");
            out.println("<html><head>");
            out.println("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
            out.println("<title>Analyzing the useragent</title></head>");
            out.println("<body>");

            // Actual logic goes here.
            out.println("<h1>Analyzing your useragent string</h1>");

            String userAgentString = request.getHeader("User-Agent");
            if (userAgentString == null) {
                out.println("<b>The User-Agent header is missing</b>");
                return;
            }


            UserAgent userAgent = parseSynchronized(userAgentString);

            System.out.println("Useragent: " + userAgentString);
            out.println("Received useragent header:" + escapeHtml4(userAgent.getUserAgentString()));
            out.println("<table border=1>");
            out.println("<tr><th>Field</th><th>Value</th></tr>");
            for (String fieldname : userAgent.getAvailableFieldNamesSorted()) {
                out.println("<tr><td>"+escapeHtml4(fieldname)+"</td><td>"+escapeHtml4(userAgent.getValue(fieldname))+"</td></tr>");
            }
            out.println("</table>");

        } finally {
            out.println("</body>");
            out.println("</html>");
            out.close();
        }
    }

    private synchronized UserAgent parseSynchronized(String userAgentString) {
        return uua.parse(userAgentString); // This class is NOT reentrant !
    }


    public void destroy() {
        // do nothing.
    }
}
