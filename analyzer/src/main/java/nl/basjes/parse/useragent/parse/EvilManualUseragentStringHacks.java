/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2019 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.useragent.parse;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

public final class EvilManualUseragentStringHacks {
    private EvilManualUseragentStringHacks() {}

    private static final Pattern MISSING_PRODUCT_AT_START =
        Pattern.compile("^\\(( |;|null|compatible|windows|android|linux).*", Pattern.CASE_INSENSITIVE);
    private static final Pattern MISSING_SPACE =
        Pattern.compile("(/[0-9]+\\.[0-9]+)([A-Z][a-z][a-z][a-z]+ )");
    private static final Pattern MULTIPLE_SPACES =
        Pattern.compile("(?: {2,})");

    /**
     * There are a few situations where in order to parse the useragent we need to 'fix it'.
     * Yes, all of this is pure evil but we "have to".
     *
     * @param useragent Raw useragent
     * @return Cleaned useragent
     */
    public static String fixIt(String useragent) {
        if (useragent == null || useragent.isEmpty()) {
            return useragent;
        }
        String result = useragent;

        result = MULTIPLE_SPACES .matcher(result).replaceAll(" ");

        if (result.charAt(0) == ' ') {
            result = result.trim();
        }

        result = replaceString(result, "SSL/TLS", "SSL TLS");

        if (result.contains("MSIE")) {
            result = replaceString(result, "MSIE7", "MSIE 7");
            result = replaceString(result, "MSIE8", "MSIE 8");
            result = replaceString(result, "MSIE9", "MSIE 9");
        }

        result = replaceString(result, "Ant.com Toolbar", "Ant.com_Toolbar");

        // We have seen problem cases like " Version/4.0Mobile Safari/530.17"
        result = MISSING_SPACE.matcher(result).replaceAll("$1 $2");

        // We have seen problem cases like "Wazzup1.1.100"
        result = replaceString(result, "Wazzup", "Wazzup ");

        // This one is a single useragent that hold significant traffic
        result = replaceString(result, " (Macintosh); ", " (Macintosh; ");

        // This one is a single useragent that hold significant traffic
        result = replaceString(result, "Microsoft Windows NT 6.2.9200.0);", "Microsoft Windows NT 6.2.9200.0;");

        // The VM_Vertis 4010 You Build/VM is missing a ')'
        result = replaceString(result, "You Build/VM", "You Build/VM)");

        // Some agents are providing comment values that are ONLY a version
        result = replaceString(result, "(/", "(Unknown/");
        result = replaceString(result, "; /", "; Unknown/");

        // Repair certain cases of broken useragents (like we see for the Facebook app a lot)
        if (MISSING_PRODUCT_AT_START.matcher(result).matches()){
            // We simply prefix a fake product name to continue parsing.
            result = "Mozilla/5.0 " + result;
        } else {
            // This happens occasionally
            if (result.charAt(0) == '/') {
                // We simply prefix a fake product name to continue parsing.
                result = "Mozilla" + result;
            }
        }

        // Kick some garbage that sometimes occurs.
        result = replaceString(result, ",gzip(gfe)", "");

        // The Weibo useragent This one is a single useragent that hold significant traffic
        result = replaceString(result, "__", " ");

        if (result.contains("%20")) {
            try {
                result = URLDecoder.decode(result, "UTF-8");
            } catch (UnsupportedEncodingException | IllegalArgumentException e) {
                // UnsupportedEncodingException: Can't happen because the UTF-8 is hardcoded here.
                // IllegalArgumentException: Probably bad % encoding in there somewhere.
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
        //startIdx and idxSearchFor delimit various chunks of input; these
        //chunks always end where searchFor begins
        int startIdx = 0;
        int idxSearchFor = input.indexOf(searchFor, startIdx);
        if (idxSearchFor < 0) {
            return input;
        }
        final StringBuilder result = new StringBuilder(input.length()+32);

        while (idxSearchFor >= 0) {
            //grab a part of input which does not include searchFor
            result.append(input, startIdx, idxSearchFor);
            //add replaceWith to take place of searchFor
            result.append(replaceWith);

            //reset the startIdx to just after the current match, to see
            //if there are any further matches
            startIdx = idxSearchFor + searchFor.length();
            idxSearchFor = input.indexOf(searchFor, startIdx);
        }
        //the final chunk will go to the end of input
        result.append(input.substring(startIdx));
        return result.toString();
    }


}
