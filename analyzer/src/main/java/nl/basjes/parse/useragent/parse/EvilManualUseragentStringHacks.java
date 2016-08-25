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

package nl.basjes.parse.useragent.parse;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public final class EvilManualUseragentStringHacks {
    private EvilManualUseragentStringHacks() {
    }

    /**
     * There are a few situations where in order to parse the useragent we need to 'fix it'.
     * Yes, all of this is pure evil but we "have to".
     *
     * @param useragent Raw useragent
     * @return Cleaned useragent
     */
    public static String fixIt(String useragent) {
        if (useragent == null) {
            return null;
        }
        String result = useragent;
        // This one is a single useragent that hold significant traffic
        if (result.contains(" (Macintosh); ")){
            result = replaceString(result, " (Macintosh); ", " (Macintosh; ");
        }

        // This happens in a broken version of the Facebook app a lot
        if (result.startsWith("(null")){
            // We simply prefix a fake product name to continue parsing.
            result = "Mozilla/5.0 " + result;
        } else {
            // This happens occasionally
            if (result.startsWith("/")) {
                // We simply prefix a fake product name to continue parsing.
                result = "Mozilla" + result;
            }
        }

        // Kick some garbage that sometimes occurs.
        if (useragent.endsWith(",gzip(gfe)")) {
            result = replaceString(result, ",gzip(gfe)", "");
        }

        // The Weibo useragent This one is a single useragent that hold significant traffic
        if (useragent.contains("__")){
            result = replaceString(result, "__", " ");
        }

        if (result.contains("%20")) {
            try {
                result = URLDecoder.decode(result, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // Ignore and continue.
            }
        }

        return result; // 99.99% of the cases nothing will have changed.
    }

    public static String replaceString(
            final String input,
            final String searchFor,
            final String replaceWith
    ){
        final StringBuilder result = new StringBuilder(input.length()+32);
        //startIdx and idxSearchFor delimit various chunks of input; these
        //chunks always end where searchFor begins
        int startIdx = 0;
        int idxSearchFor;
        while ((idxSearchFor = input.indexOf(searchFor, startIdx)) >= 0) {
            //grab a part of input which does not include searchFor
            result.append(input.substring(startIdx, idxSearchFor));
            //add replaceWith to take place of searchFor
            result.append(replaceWith);

            //reset the startIdx to just after the current match, to see
            //if there are any further matches
            startIdx = idxSearchFor + searchFor.length();
        }
        //the final chunk will go to the end of input
        result.append(input.substring(startIdx));
        return result.toString();
    }


}
