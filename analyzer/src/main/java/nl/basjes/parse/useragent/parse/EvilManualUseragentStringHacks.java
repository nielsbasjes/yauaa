/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2025 Niels Basjes
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

import static nl.basjes.parse.useragent.utils.Normalize.replaceString;

public final class EvilManualUseragentStringHacks {
    private EvilManualUseragentStringHacks() {}

    private static final Pattern MISSING_SPACE =
        Pattern.compile("(/\\d+\\.\\d+)([A-Z][a-z][a-z][a-z]+ )");
    private static final Pattern MULTIPLE_SPACES =
        Pattern.compile(" {2,}");
    private static final Pattern AVOID_BASE64_MATCH =
        Pattern.compile("(android/\\d+)(/)", Pattern.CASE_INSENSITIVE);

    private static final Pattern ANDROID_DASH_VERSION =
        Pattern.compile("(android)-(\\d+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern PROPERTY_PRODUCT_VERSION_FIX =
        Pattern.compile("(NetType|Language|AppName|app_version|ByteLocale|ByteFullLocale|Region)/([\\da-z._-]+)", Pattern.CASE_INSENSITIVE);
    // NOTE: "Channel" is separate because we see "Channel/googleplay" and "Channel/App Store"

    private static final Pattern GLUED_VERSION_FIX =
        Pattern.compile("(Java|Wazzup)(\\d)", Pattern.CASE_INSENSITIVE);

    private static final Pattern GLUED_VERSION_FIX_2 =
        Pattern.compile("(OS|[a-z][a-z_][a-z_][a-z_]+[a-z])_([\\d_.]+) ", Pattern.CASE_INSENSITIVE);

    private static final Pattern MISSING_COMMENT_BRACES =
        Pattern.compile("^(Mozilla/[^ ]+) ([^(][^)]+?)( Gecko/| AppleWebKit/| Safari/| Mobile Safari/)", Pattern.CASE_INSENSITIVE);

    private static final Pattern MISSING_COMMENT_BRACES_OPERA =
        Pattern.compile("^(Opera/[^ ]+) ([^(][^)]+?)( Presto/| Version/)", Pattern.CASE_INSENSITIVE);

    private static final Pattern STRIP_SURROUNDING_QUOTES =
        Pattern.compile("^'(.*)'$", Pattern.CASE_INSENSITIVE);

    private static final Pattern STRIP_PIPESYMBOLS =
        Pattern.compile("([a-z0-9])\\|([a-z0-9])", Pattern.CASE_INSENSITIVE);

    // As found with the Snorlax examples
    // Using regex found here https://stackoverflow.com/a/475217/114196
    private static final Pattern STRIP_BASE64_BLOCK =
        Pattern.compile(" H@(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)@h ");

    private static final Pattern PICO_OS_TAG =
        Pattern.compile("(Pico [a-zA-Z0-9 ._-]+ OS)", Pattern.CASE_INSENSITIVE);

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

        result = MULTIPLE_SPACES.matcher(result).replaceAll(" ");

        // The first one is a special kind of space: https://unicodemap.org/details/0x2002/index.html
        result = replaceString(result, "\u2002", " ");

        if (result.charAt(0) == ' ') {
            result = result.trim();
        }

        result = STRIP_SURROUNDING_QUOTES.matcher(result).replaceAll("$1");
        result = MISSING_COMMENT_BRACES.matcher(result).replaceAll("$1 ($2)$3");
        result = MISSING_COMMENT_BRACES_OPERA.matcher(result).replaceAll("$1 ($2)$3");
        result = STRIP_BASE64_BLOCK.matcher(result).replaceAll(" ");

        // The NetType and Language tags as used by Tencent and ByteDance are hard to parse.
        // Some example snippets from Tencent/Alibaba style agents:
        //    Core/UIWebView NetType/WIFI
        //    Core/UIWebView NetType/2G
        //    Process/tools NetType/portalmmm.nl Language/zh_CN
        //    Process/tools NetType/NON_NETWORK Language/zh_CN
        //    NetType/WIFI Channel/googleplay AppName/musical_ly
        // The 'fix' is to force an extra comment block in there.
        // NOTE: "Channel" is separate because we see "Channel/googleplay" and "Channel/App Store"
        result = replaceString(result, "Channel/App Store", "()Channel/App Store()");
        result = PROPERTY_PRODUCT_VERSION_FIX.matcher(result).replaceAll("() $1/$2()");
        if (result.startsWith("()")) {
            result = result.substring(2).trim();
        }

        if (result.contains("|")) {
            // Turn for example "MAGAPPX|6.1.6" into "MAGAPPX/6.1.6"
            result = STRIP_PIPESYMBOLS.matcher(result).replaceAll("$1/$2");
        }

        result = replaceString(result, "SSL/TLS", "SSL TLS");

        // Quant specific hacks (this also affects the " h QwantMobile" case !!)
        result = replaceString(result, "QwantMobile", " QwantMobile");

        if (result.contains("MSIE")) {
            result = replaceString(result, "MSIE7", "MSIE 7");
            result = replaceString(result, "MSIE8", "MSIE 8");
            result = replaceString(result, "MSIE9", "MSIE 9");
        }

        result = replaceString(result, "MSOffice", "MS-Office");

        result = replaceString(result, "Ant.com Toolbar", "Ant.com_Toolbar");

        // Something like Android-4.0.3 is seen as a text instead of a product.
        result = ANDROID_DASH_VERSION.matcher(result).replaceAll("$1 $2");

        // We have seen problem cases like " Version/4.0Mobile Safari/530.17"
        result = MISSING_SPACE.matcher(result).replaceAll("$1 $2");

        // Sometimes a case like  "Android/9/something/" matches the pattern of Base84 which breaks everything
        // So those cases we simply insert a space to avoid this match and without changing the resulting tree.
        result = AVOID_BASE64_MATCH.matcher(result).replaceAll("$1 $2");

        // We have seen problem cases like "Java1.0.21.0" and "Wazzup1.1.100"
        result = GLUED_VERSION_FIX.matcher(result).replaceAll("$1 $2");

        // For musical_ly_123 and trill_123
        result = GLUED_VERSION_FIX_2.matcher(result).replaceAll("$1 $2 ");

        // This one is a single useragent that hold significant traffic
        result = replaceString(result, " (Macintosh); ", " (Macintosh; ");

        // This one is a single useragent that hold significant traffic
        result = replaceString(result, "Microsoft Windows NT 6.2.9200.0);", "Microsoft Windows NT 6.2.9200.0;");

        // The VM_Vertis 4010 You Build/VM is missing a ')'
        result = replaceString(result, "You Build/VM", "You Build/VM)");

        // Some agents are providing comment values that are ONLY a version
        result = replaceString(result, "(/", "(Unknown/");
        result = replaceString(result, "; /", "; Unknown/");

        result = replaceString(result, ", _TV_", " _TV_");

        // Repair certain cases of broken useragents (like we see for the Facebook app a lot)
        char firstChar = result.charAt(0);
        if (firstChar == '(' || firstChar == '[') {
            // We simply prefix a fake product name to continue parsing.
            result = "FakeYauaaProduct/3.14 " + result;
        } else {
            // This happens occasionally
            if (firstChar == '/') {
                // We simply prefix a fake product name to continue parsing.
                result = "FakeYauaaProduct" + result;
            }
        }

        // Kick some garbage that sometimes occurs.
        result = replaceString(result, ",gzip(gfe)", "");

        // The Weibo useragent This one is a single useragent that hold significant traffic
        result = replaceString(result, "__", " ");

        if (
            (result.indexOf('%') != -1) &&
                (result.contains("%20") ||
                 result.contains("%3B") ||
                 result.contains("%25") ||
                 result.contains("%2F") ||
                 result.contains("%28"))) {
            try {
                result = URLDecoder.decode(result, "UTF-8");
            } catch (UnsupportedEncodingException | IllegalArgumentException e) {
                // UnsupportedEncodingException: Can't happen because the UTF-8 is hardcoded here.
                // IllegalArgumentException: Probably bad % encoding in there somewhere.
                // Ignore and continue.
            }
        }

        // As found with the PICO 4 examples: Add an extra space behind ' OS' (handle: "Pico 4 OS1.2.3").
        result = PICO_OS_TAG.matcher(result).replaceAll("$1 ");

        return result; // 99.99% of the cases nothing will have changed.
    }

}
