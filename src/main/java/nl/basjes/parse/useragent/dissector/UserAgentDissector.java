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

package nl.basjes.parse.useragent.dissector;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserAgentDissector extends Dissector {
    private static UserAgentAnalyzer userAgentAnalyzer = null;

    private static final String INPUT_TYPE = "HTTP.USERAGENT";

    @Override
    public String getInputType() {
        return INPUT_TYPE;
    }

    @Override
    public boolean initializeFromSettingsParameter(String s) {
        if (userAgentAnalyzer == null) {
            userAgentAnalyzer = new UserAgentAnalyzer();
        }
        // TODO: Allow for the path of extra yaml files
        return true;
    }

    @Override
    public void dissect(Parsable<?> parsable, String inputname) throws DissectionFailure {
        if (userAgentAnalyzer == null) {
            userAgentAnalyzer = new UserAgentAnalyzer();
        }
        final ParsedField agentField = parsable.getParsableField(INPUT_TYPE, inputname);

        String userAgentString = agentField.getValue().getString();

        if (userAgentString == null) {
            return;  // TODO: Figure out why this happens (because it does)
        }

        UserAgent agent = userAgentAnalyzer.parse(userAgentString);

        for (String fieldName : agent.getAvailableFieldNames()) {
            parsable.addDissection(inputname, "STRING", fieldNameToDissectionName(fieldName), agent.getValue(fieldName));
        }
    }

    @Override
    public List<String> getPossibleOutput() {
        if (userAgentAnalyzer == null) {
            userAgentAnalyzer = new UserAgentAnalyzer();
        }

        List<String> result = new ArrayList<>();

        // First the standard fields in the standard order, then the non-standard fields alphabetically
        List<String> fieldNames = userAgentAnalyzer.getAllPossibleFieldNamesSorted();
        for (String fieldName : fieldNames) {
            result.add("STRING:" + fieldNameToDissectionName(fieldName));
        }
        return result;

    }

    @Override
    public EnumSet<Casts> prepareForDissect(String s, String s1) {
        return Casts.STRING_ONLY; // We ONLY do Strings here
    }

    @Override
    public void prepareForRun() throws InvalidDissectorException {
        // Nothing to do here
    }

    @Override
    protected void initializeNewInstance(Dissector dissector) {
    }

    private static final Map<String, String> FIELD_NAME_MAPPING_CACHE = new HashMap<>(64);

    static String fieldNameToDissectionName(String fieldName) {
        String dissectionName = FIELD_NAME_MAPPING_CACHE.get(fieldName);
        if (dissectionName == null) {
            dissectionName = fieldName.replaceAll("([A-Z])", "_$1").toLowerCase(Locale.ENGLISH).replaceFirst("_", "");
            FIELD_NAME_MAPPING_CACHE.put(fieldName, dissectionName);
        }
        return dissectionName;
    }

}
