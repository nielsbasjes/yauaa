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

package nl.basjes.parse.useragent.servlet.graphql;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUa;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaArch;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaBitness;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaFormFactors;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaFullVersion;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaFullVersionList;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaMobile;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaModel;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaPlatform;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaPlatformVersion;
import nl.basjes.parse.useragent.clienthints.parsers.ParseSecChUaWoW64;
import org.springframework.context.annotation.Description;

import java.util.Map;
import java.util.TreeMap;

@Description("The values of the HTTP request Headers that need to be analyzed.")
@Accessors(chain = true)
@Getter
@Setter
public class RequestHeaders {

    private String userAgent;               // User-Agent
    private String secChUa;                 // Sec-CH-UA
    private String secChUaArch;             // Sec-CH-UA-Arch
    private String secChUaBitness;          // Sec-CH-UA-Bitness
    private String secChUaFormFactor;       // Sec-CH-UA-Form-Factor
    private String secChUaFullVersion;      // Sec-CH-UA-Full-Version
    private String secChUaFullVersionList;  // Sec-CH-UA-Full-Version-List
    private String secChUaMobile;           // Sec-CH-UA-Mobile
    private String secChUaModel;            // Sec-CH-UA-Model
    private String secChUaPlatform;         // Sec-CH-UA-Platform
    private String secChUaPlatformVersion;  // Sec-CH-UA-Platform-Version
    private String secChUaWoW64;            // Sec-CH-UA-WoW64


    public RequestHeaders() {
        // Empty
    }

    public RequestHeaders(RequestHeaders requestHeaders) {
        this.userAgent              = requestHeaders.getUserAgent();
        this.secChUa                = requestHeaders.getSecChUa();
        this.secChUaArch            = requestHeaders.getSecChUaArch();
        this.secChUaBitness         = requestHeaders.getSecChUaBitness();
        this.secChUaFormFactor      = requestHeaders.getSecChUaFormFactor();
        this.secChUaFullVersion     = requestHeaders.getSecChUaFullVersion();
        this.secChUaFullVersionList = requestHeaders.getSecChUaFullVersionList();
        this.secChUaMobile          = requestHeaders.getSecChUaMobile();
        this.secChUaModel           = requestHeaders.getSecChUaModel();
        this.secChUaPlatform        = requestHeaders.getSecChUaPlatform();
        this.secChUaPlatformVersion = requestHeaders.getSecChUaPlatformVersion();
        this.secChUaWoW64           = requestHeaders.getSecChUaWoW64();
    }

    public Map<String, String> toMap() {
        Map<String, String> result = new TreeMap<>();
        result.put("User-Agent",                                        userAgent);
        putIfNotNull(result, ParseSecChUa.HEADER_FIELD,                 secChUa);
        putIfNotNull(result, ParseSecChUaArch.HEADER_FIELD,             secChUaArch);
        putIfNotNull(result, ParseSecChUaBitness.HEADER_FIELD,          secChUaBitness);
        putIfNotNull(result, ParseSecChUaFormFactors.HEADER_FIELD,       secChUaFormFactor);
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
