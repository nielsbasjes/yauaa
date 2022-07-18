/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2022 Niels Basjes
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
import org.eclipse.microprofile.graphql.Description;

import java.util.Map;
import java.util.TreeMap;

import static nl.basjes.parse.useragent.UserAgent.USERAGENT_HEADER;
import static nl.basjes.parse.useragent.UserAgent.USERAGENT_HEADER_SPEC;
import static nl.basjes.parse.useragent.UserAgent.USERAGENT_HEADER_SPEC_URL;

@Description("The values of the HTTP request Headers that need to be analyzed.")
public class RequestHeaders {

    @Description(USERAGENT_HEADER                           + ": " + USERAGENT_HEADER_SPEC                     + " See also " + USERAGENT_HEADER_SPEC_URL)                      public String userAgent;               // User-Agent
    @Description(ParseSecChUa.HEADER_FIELD                  + ": " + ParseSecChUa.HEADER_SPEC                  + " See also " + ParseSecChUa.HEADER_SPEC_URL)                   public String secChUa;                 // Sec-CH-UA
    @Description(ParseSecChUaArch.HEADER_FIELD              + ": " + ParseSecChUaArch.HEADER_SPEC              + " See also " + ParseSecChUaArch.HEADER_SPEC_URL)               public String secChUaArch;             // Sec-CH-UA-Arch
    @Description(ParseSecChUaBitness.HEADER_FIELD           + ": " + ParseSecChUaBitness.HEADER_SPEC           + " See also " + ParseSecChUaBitness.HEADER_SPEC_URL)            public String secChUaBitness;          // Sec-CH-UA-Bitness
    @Description(ParseSecChUaFullVersion.HEADER_FIELD       + ": " + ParseSecChUaFullVersion.HEADER_SPEC       + " See also " + ParseSecChUaFullVersion.HEADER_SPEC_URL)        public String secChUaFullVersion;      // Sec-CH-UA-Full-Version
    @Description(ParseSecChUaFullVersionList.HEADER_FIELD   + ": " + ParseSecChUaFullVersionList.HEADER_SPEC   + " See also " + ParseSecChUaFullVersionList.HEADER_SPEC_URL)    public String secChUaFullVersionList;  // Sec-CH-UA-Full-Version-List
    @Description(ParseSecChUaMobile.HEADER_FIELD            + ": " + ParseSecChUaMobile.HEADER_SPEC            + " See also " + ParseSecChUaMobile.HEADER_SPEC_URL)             public String secChUaMobile;           // Sec-CH-UA-Mobile
    @Description(ParseSecChUaModel.HEADER_FIELD             + ": " + ParseSecChUaModel.HEADER_SPEC             + " See also " + ParseSecChUaModel.HEADER_SPEC_URL)              public String secChUaModel;            // Sec-CH-UA-Model
    @Description(ParseSecChUaPlatform.HEADER_FIELD          + ": " + ParseSecChUaPlatform.HEADER_SPEC          + " See also " + ParseSecChUaPlatform.HEADER_SPEC_URL)           public String secChUaPlatform;         // Sec-CH-UA-Platform
    @Description(ParseSecChUaPlatformVersion.HEADER_FIELD   + ": " + ParseSecChUaPlatformVersion.HEADER_SPEC   + " See also " + ParseSecChUaPlatformVersion.HEADER_SPEC_URL)    public String secChUaPlatformVersion;  // Sec-CH-UA-Platform-Version
    @Description(ParseSecChUaWoW64.HEADER_FIELD             + ": " + ParseSecChUaWoW64.HEADER_SPEC             + " See also " + ParseSecChUaWoW64.HEADER_SPEC_URL)              public String secChUaWoW64;            // Sec-CH-UA-WoW64

    public Map<String, String> toMap() {
        Map<String, String> result = new TreeMap<>();
        result.put("User-Agent", userAgent);
        if (secChUa                != null) { result.put(ParseSecChUa.HEADER_FIELD,                 secChUa);                }
        if (secChUaArch            != null) { result.put(ParseSecChUaArch.HEADER_FIELD,             secChUaArch);            }
        if (secChUaBitness         != null) { result.put(ParseSecChUaBitness.HEADER_FIELD,          secChUaBitness);         }
        if (secChUaFullVersion     != null) { result.put(ParseSecChUaFullVersion.HEADER_FIELD,      secChUaFullVersion);     }
        if (secChUaFullVersionList != null) { result.put(ParseSecChUaFullVersionList.HEADER_FIELD,  secChUaFullVersionList); }
        if (secChUaMobile          != null) { result.put(ParseSecChUaMobile.HEADER_FIELD,           secChUaMobile);          }
        if (secChUaModel           != null) { result.put(ParseSecChUaModel.HEADER_FIELD,            secChUaModel);           }
        if (secChUaPlatform        != null) { result.put(ParseSecChUaPlatform.HEADER_FIELD,         secChUaPlatform);        }
        if (secChUaPlatformVersion != null) { result.put(ParseSecChUaPlatformVersion.HEADER_FIELD,  secChUaPlatformVersion); }
        if (secChUaWoW64           != null) { result.put(ParseSecChUaWoW64.HEADER_FIELD,            secChUaWoW64);           }
        return result;
    }

    @Override
    public String toString() {
        return "RequestHeaders:" + toMap();
    }
}
