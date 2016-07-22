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

import com.platfora.udf.UserDefinedFunction;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

import java.util.List;

public abstract class AbstractAnalyzeUserAgent implements UserDefinedFunction {

    private static final UserAgentAnalyzer ANALYZER = new UserAgentAnalyzer();
    private static List<String> allPossibleFieldNamesSorted = null;

    protected List<String> getAllPossibleFieldNamesSorted() {
        if (allPossibleFieldNamesSorted == null) {
            allPossibleFieldNamesSorted = ANALYZER.getAllPossibleFieldNamesSorted();
        }
        return allPossibleFieldNamesSorted;
    }

    public String getReturnType() {
        return "STRING";
    }

    protected UserAgent parseUserAgent(String useragent) {
        if (useragent == null) {
            return null;
        }
        return ANALYZER.parse(useragent);
    }

}
