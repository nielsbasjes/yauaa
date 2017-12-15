/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2017 Niels Basjes
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

package nl.basjes.parse.useragent.dissector;

import nl.basjes.parse.core.Casts;
import nl.basjes.parse.core.Dissector;
import nl.basjes.parse.core.Parsable;
import nl.basjes.parse.core.ParsedField;
import nl.basjes.parse.core.exceptions.DissectionFailure;
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
            if (s != null && !(s.trim().isEmpty())) {
                userAgentAnalyzer.loadResources(s);
            }
        }
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
            return;  // Weird, but it happens
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
    public void prepareForRun() {
        // Nothing to do here
    }

    @Override
    protected void initializeNewInstance(Dissector dissector) {
    }

    private static final Map<String, String> FIELD_NAME_MAPPING_CACHE = new HashMap<>(64);

    static String fieldNameToDissectionName(String fieldName) {
        return FIELD_NAME_MAPPING_CACHE
            .computeIfAbsent(fieldName,
                n -> n  .replaceAll("([A-Z])", "_$1")
                        .toLowerCase(Locale.ENGLISH)
                        .replaceFirst("_", ""));
    }

}
