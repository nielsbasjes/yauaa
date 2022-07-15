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

package nl.basjes.parse.useragent.servlet.api.graphql;

import java.util.Map;
import java.util.TreeMap;

public class RequestHeaders {
    public String userAgent;               // User-Agent
    public String secChUa;                 // Sec-CH-UA
    public String secChUaArch;             // Sec-CH-UA-Arch
    public String secChUaBitness;          // Sec-CH-UA-Bitness
    public String secChUaFullVersion;      // Sec-CH-UA-Full-Version
    public String secChUaFullVersionList;  // Sec-CH-UA-Full-Version-List
    public String secChUaMobile;           // Sec-CH-UA-Mobile
    public String secChUaModel;            // Sec-CH-UA-Model
    public String secChUaPlatform;         // Sec-CH-UA-Platform
    public String secChUaPlatformVersion;  // Sec-CH-UA-Platform-Version
    public String secChUaWoW64;            // Sec-CH-UA-WoW64

    public Map<String, String> toMap() {
        Map<String, String> result = new TreeMap<>();
        result.put("User-Agent", userAgent);
        if (secChUa                != null) { result.put("Sec-CH-UA",                   secChUa);                }
        if (secChUaArch            != null) { result.put("Sec-CH-UA-Arch",              secChUaArch);            }
        if (secChUaBitness         != null) { result.put("Sec-CH-UA-Bitness",           secChUaBitness);         }
        if (secChUaFullVersion     != null) { result.put("Sec-CH-UA-Full-Version",      secChUaFullVersion);     }
        if (secChUaFullVersionList != null) { result.put("Sec-CH-UA-Full-Version-List", secChUaFullVersionList); }
        if (secChUaMobile          != null) { result.put("Sec-CH-UA-Mobile",            secChUaMobile);          }
        if (secChUaModel           != null) { result.put("Sec-CH-UA-Model",             secChUaModel);           }
        if (secChUaPlatform        != null) { result.put("Sec-CH-UA-Platform",          secChUaPlatform);        }
        if (secChUaPlatformVersion != null) { result.put("Sec-CH-UA-Platform-Version",  secChUaPlatformVersion); }
        if (secChUaWoW64           != null) { result.put("Sec-CH-UA-WoW64",             secChUaWoW64);           }
        return result;
    }

    @Override
    public String toString() {
        return "RequestHeaders:" + toMap();
    }
}
