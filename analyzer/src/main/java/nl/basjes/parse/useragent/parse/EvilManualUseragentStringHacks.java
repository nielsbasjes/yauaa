/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2016 Niels Basjes
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
        if (result.startsWith("(null") ||
            result.startsWith("(Windows")){
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
