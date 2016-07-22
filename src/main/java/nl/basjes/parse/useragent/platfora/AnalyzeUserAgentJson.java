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

public class AnalyzeUserAgentJson extends AbstractAnalyzeUserAgent {

    public String getFunctionName() {
        return "ANALYZE_USERAGENT_JSON";
    }

    public String getReturnType() {
        return "STRING";
    }

    public String[] getArgumentTypes() {
        return new String[]{"STRING"};
    }

    public String getDescription() {
        return "The ANALYZE_USERAGENT_JSON function returns a key-value set of all attributes of the analysis of the specified useragent string.";
    }

    public String getReturnValueDescription() {
        return "A key-value set of all results of the analysis.";
    }

    public String getExampleUsage() {
        return "ANALYZE_USERAGENT_JSON( useragent )";
    }

    public String compute(List arguments) {
        UserAgent parsedUserAgent = parseUserAgent((String) arguments.get(0));
        if (parsedUserAgent == null) {
            return null;
        }
        return parsedUserAgent.toJson();
    }
}
