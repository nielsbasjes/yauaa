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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class UserAgentVersionDissector extends Dissector {

    private static final String INPUT_TYPE = "HTTP.USERAGENT.VERSION";

    @Override
    public String getInputType() {
        return INPUT_TYPE;
    }

    @Override
    public boolean initializeFromSettingsParameter(String s) {
        return true;
    }

    @Override
    public void dissect(Parsable<?> parsable, String inputname) throws DissectionFailure {
        final ParsedField versionField = parsable.getParsableField(INPUT_TYPE, inputname);

        String versionString = versionField.getValue().getString();

        if (versionString == null || versionString.length() <= 1){
            return; // Nothing to do
        }

        String full = versionString.replaceAll("_", ".");

        parsable.addDissection(inputname, "STRING", "full", full);

        int firstDot = full.indexOf('.');
        if (firstDot == -1) {
            parsable.addDissection(inputname, "STRING", "major", full);
        } else {
            String major = full.substring(0, firstDot);
            parsable.addDissection(inputname, "STRING", "major", major);
            int secondDot = full.indexOf('.', firstDot+1);
            if (secondDot == -1) {
                parsable.addDissection(inputname, "STRING", "minor", full);
            } else {
                String minor = full.substring(0, secondDot);
                parsable.addDissection(inputname, "STRING", "minor", minor);
            }
        }
    }

    @Override
    public List<String> getPossibleOutput() {
        List<String> result = new ArrayList<>();
        result.add("STRING:major");
        result.add("STRING:minor");
        result.add("STRING:full");
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
        // Nothing to do here
    }
}
