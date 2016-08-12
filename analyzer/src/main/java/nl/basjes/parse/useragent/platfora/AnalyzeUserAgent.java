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

package nl.basjes.parse.useragent.platfora;

import nl.basjes.parse.useragent.UserAgent;

import java.util.List;

public class AnalyzeUserAgent extends AbstractAnalyzeUserAgent {

    public String getFunctionName() {
        return "ANALYZE_USERAGENT";
    }

    public String getReturnType() {
        return "STRING";
    }

    public String[] getArgumentTypes() {
        return new String[]{"STRING", "STRING"};
    }

    public String getDescription() {
        List<String> fieldNames = getAllPossibleFieldNamesSorted();
        StringBuilder sb = new StringBuilder(1024);
        for (String fieldName: fieldNames) {
            sb.append('"').append(fieldName).append("\" ");
        }
        return "The ANALYZE_USERAGENT function returns the value of a single field from " +
            "the analysis of the specified useragent string. " +
            "Available field names are: " + sb;
    }

    public String getReturnValueDescription() {
        return getDescription();
    }

    public String getExampleUsage() {
        return "ANALYZE_USERAGENT( useragent , \"AgentName\" )";
    }

    public String compute(List arguments) {
        try {
            String useragent = (String) arguments.get(0);
            String attribute = (String) arguments.get(1);

            if (!getAllPossibleFieldNamesSorted().contains(attribute)){
                return "[[ERROR: The fieldname \""+attribute+"\" does not exist]]";
            }

            UserAgent parsedUserAgent = parseUserAgent(useragent);
            if (parsedUserAgent == null) {
                return null;
            }
            return parsedUserAgent.getValue(attribute);
        } catch (Exception e) {
            return "[[ERROR: Exception: "+ e.getMessage() + "]]";
        }
    }
}
