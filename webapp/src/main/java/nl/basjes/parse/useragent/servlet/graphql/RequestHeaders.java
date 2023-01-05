/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2023 Niels Basjes
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

package nl.basjes.parse.useragent.servlet.graphql;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUa;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaArch;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaBitness;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaFullVersion;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaFullVersionList;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaMobile;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaModel;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaPlatform;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaPlatformVersion;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaWoW64;

import java.util.Map;
import java.util.TreeMap;

//@Description("The values of the HTTP request Headers that need to be analyzed.")
@Accessors(chain = true)
public class RequestHeaders {

    @Getter @Setter /* @Description(USERAGENT_HEADER                           + ": " + USERAGENT_HEADER_SPEC                     + " See also " + USERAGENT_HEADER_SPEC_URL)                    */  private String userAgent;               // User-Agent
    @Getter @Setter /* @Description(ParseSecChUa.HEADER_FIELD                  + ": " + ParseSecChUa.HEADER_SPEC                  + " See also " + ParseSecChUa.HEADER_SPEC_URL)                 */  private String secChUa;                 // Sec-CH-UA
    @Getter @Setter /* @Description(ParseSecChUaArch.HEADER_FIELD              + ": " + ParseSecChUaArch.HEADER_SPEC              + " See also " + ParseSecChUaArch.HEADER_SPEC_URL)             */  private String secChUaArch;             // Sec-CH-UA-Arch
    @Getter @Setter /* @Description(ParseSecChUaBitness.HEADER_FIELD           + ": " + ParseSecChUaBitness.HEADER_SPEC           + " See also " + ParseSecChUaBitness.HEADER_SPEC_URL)          */  private String secChUaBitness;          // Sec-CH-UA-Bitness
    @Getter @Setter /* @Description(ParseSecChUaFullVersion.HEADER_FIELD       + ": " + ParseSecChUaFullVersion.HEADER_SPEC       + " See also " + ParseSecChUaFullVersion.HEADER_SPEC_URL)      */  private String secChUaFullVersion;      // Sec-CH-UA-Full-Version
    @Getter @Setter /* @Description(ParseSecChUaFullVersionList.HEADER_FIELD   + ": " + ParseSecChUaFullVersionList.HEADER_SPEC   + " See also " + ParseSecChUaFullVersionList.HEADER_SPEC_URL)  */  private String secChUaFullVersionList;  // Sec-CH-UA-Full-Version-List
    @Getter @Setter /* @Description(ParseSecChUaMobile.HEADER_FIELD            + ": " + ParseSecChUaMobile.HEADER_SPEC            + " See also " + ParseSecChUaMobile.HEADER_SPEC_URL)           */  private String secChUaMobile;           // Sec-CH-UA-Mobile
    @Getter @Setter /* @Description(ParseSecChUaModel.HEADER_FIELD             + ": " + ParseSecChUaModel.HEADER_SPEC             + " See also " + ParseSecChUaModel.HEADER_SPEC_URL)            */  private String secChUaModel;            // Sec-CH-UA-Model
    @Getter @Setter /* @Description(ParseSecChUaPlatform.HEADER_FIELD          + ": " + ParseSecChUaPlatform.HEADER_SPEC          + " See also " + ParseSecChUaPlatform.HEADER_SPEC_URL)         */  private String secChUaPlatform;         // Sec-CH-UA-Platform
    @Getter @Setter /* @Description(ParseSecChUaPlatformVersion.HEADER_FIELD   + ": " + ParseSecChUaPlatformVersion.HEADER_SPEC   + " See also " + ParseSecChUaPlatformVersion.HEADER_SPEC_URL)  */  private String secChUaPlatformVersion;  // Sec-CH-UA-Platform-Version
    @Getter @Setter /* @Description(ParseSecChUaWoW64.HEADER_FIELD             + ": " + ParseSecChUaWoW64.HEADER_SPEC             + " See also " + ParseSecChUaWoW64.HEADER_SPEC_URL)            */  private String secChUaWoW64;            // Sec-CH-UA-WoW64

    public Map<String, String> toMap() {
        Map<String, String> result = new TreeMap<>();
        result.put("User-Agent",                                        userAgent);
        putIfNotNull(result, ParseSecChUa.HEADER_FIELD,                 secChUa);
        putIfNotNull(result, ParseSecChUaArch.HEADER_FIELD,             secChUaArch);
        putIfNotNull(result, ParseSecChUaBitness.HEADER_FIELD,          secChUaBitness);
        putIfNotNull(result, ParseSecChUaFullVersion.HEADER_FIELD,      secChUaFullVersion);
        putIfNotNull(result, ParseSecChUaFullVersionList.HEADER_FIELD,  secChUaFullVersionList);
        putIfNotNull(result, ParseSecChUaMobile.HEADER_FIELD,           secChUaMobile);
        putIfNotNull(result, ParseSecChUaModel.HEADER_FIELD,            secChUaModel);
        putIfNotNull(result, ParseSecChUaPlatform.HEADER_FIELD,         secChUaPlatform);
        putIfNotNull(result, ParseSecChUaPlatformVersion.HEADER_FIELD,  secChUaPlatformVersion);
        putIfNotNull(result, ParseSecChUaWoW64.HEADER_FIELD,            secChUaWoW64);
        return result;
    }

    private void putIfNotNull(Map<String, String> result, String key, String value) {
        if (value != null) {
            result.put(key, value);
        }
    }

    @Override
    public String toString() {
        return "RequestHeaders:" + toMap();
    }
}
