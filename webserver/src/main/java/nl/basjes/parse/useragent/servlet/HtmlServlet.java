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

import nl.basjes.parse.useragent.UserAgentAnalyzer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class HtmlServlet extends HttpServlet {

    private String message;
    private UserAgentAnalyzer uua;

    public void init() throws ServletException {
        uua = new UserAgentAnalyzer();
        // Do required initialization
        message = "Niels Basjes";
    }

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        try {
            String userAgentString = request.getHeader("User-Agent");

            // Actual logic goes here.
            out.println("<h1>" + message + "</h1>");
            out.println("<b>" + System.currentTimeMillis() + "</b>");

            if (userAgentString == null) {
                out.println("The useragent header was absent");
                return;
            }

            // FIXME: This is totally unsafe proof of concept (i.e. (java)script injection WILL work).
            out.println(uua.parse(userAgentString).toYamlTestCase().replaceAll("\n", "<br/>\n").replaceAll(" ", "&nbsp;"));
        } finally {
            // Set response content type
            response.setContentType("text/html");

            out.close();
        }
    }

    public void destroy() {
        // do nothing.
    }
}
