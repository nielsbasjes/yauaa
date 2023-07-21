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

package nl.basjes.log2test;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import nl.basjes.parse.core.Field;

import java.util.Map;
import java.util.TreeMap;

@EqualsAndHashCode
public final class LogRecord implements Comparable<LogRecord> {
    @Getter @Setter(onMethod=@__(@Field("HTTP.USERAGENT:request.user-agent")))                      private String userAgent              = null;
    @Getter @Setter(onMethod=@__(@Field("HTTP.HEADER:request.header.sec-ch-ua")))                   private String secChUa                = null;
    @Getter @Setter(onMethod=@__(@Field("HTTP.HEADER:request.header.sec-ch-ua-arch")))              private String secChUaArch            = null;
    @Getter @Setter(onMethod=@__(@Field("HTTP.HEADER:request.header.sec-ch-ua-bitness")))           private String secChUaBitness         = null;
    @Getter @Setter(onMethod=@__(@Field("HTTP.HEADER:request.header.sec-ch-ua-full-version")))      private String secChUaFullVersion     = null;
    @Getter @Setter(onMethod=@__(@Field("HTTP.HEADER:request.header.sec-ch-ua-full-version-list"))) private String secChUaFullVersionList = null;
    @Getter @Setter(onMethod=@__(@Field("HTTP.HEADER:request.header.sec-ch-ua-mobile")))            private String secChUaMobile          = null;
    @Getter @Setter(onMethod=@__(@Field("HTTP.HEADER:request.header.sec-ch-ua-model")))             private String secChUaModel           = null;
    @Getter @Setter(onMethod=@__(@Field("HTTP.HEADER:request.header.sec-ch-ua-platform")))          private String secChUaPlatform        = null;
    @Getter @Setter(onMethod=@__(@Field("HTTP.HEADER:request.header.sec-ch-ua-platform-version")))  private String secChUaPlatformVersion = null;
    @Getter @Setter(onMethod=@__(@Field("HTTP.HEADER:request.header.sec-ch-ua-wow64")))             private String secChUaWow64           = null;

    private void putIfNotNull(Map<String, String> result, String header, String value) {
        if (value != null) {
            result.put(header, value);
        }
    }

    public Map<String, String> asHeadersMap() {
        Map<String, String> result = new TreeMap<>();
        putIfNotNull(result, "User-Agent",                  userAgent);
        putIfNotNull(result, "Sec-Ch-Ua",                   secChUa);
        putIfNotNull(result, "Sec-Ch-Ua-Arch",              secChUaArch);
        putIfNotNull(result, "Sec-Ch-Ua-Bitness",           secChUaBitness);
        putIfNotNull(result, "Sec-Ch-Ua-Full-Version",      secChUaFullVersion);
        putIfNotNull(result, "Sec-Ch-Ua-Full-Version-List", secChUaFullVersionList);
        putIfNotNull(result, "Sec-Ch-Ua-Mobile",            secChUaMobile);
        putIfNotNull(result, "Sec-Ch-Ua-Model",             secChUaModel);
        putIfNotNull(result, "Sec-Ch-Ua-Platform",          secChUaPlatform);
        putIfNotNull(result, "Sec-Ch-Ua-Platform-Version",  secChUaPlatformVersion);
        putIfNotNull(result, "Sec-Ch-Ua-Wow64",             secChUaWow64);
        return result;
    }

    private String escapeYaml(String input) {
        return input.replace("'", "''");
    }

    private void appendIfNotNull(StringBuilder sb, String header, String value) {
        if (value != null) {
            sb.append("      ").append(header).append(": '").append(escapeYaml(value)).append("'\n");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("- test:\n");
        sb.append("    input:\n");
        appendIfNotNull(sb, "User-Agent                   ", userAgent);
        appendIfNotNull(sb, "Sec-Ch-Ua                    ", secChUa);
        appendIfNotNull(sb, "Sec-Ch-Ua-Arch               ", secChUaArch);
        appendIfNotNull(sb, "Sec-Ch-Ua-Bitness            ", secChUaBitness);
        appendIfNotNull(sb, "Sec-Ch-Ua-Full-Version       ", secChUaFullVersion);
        appendIfNotNull(sb, "Sec-Ch-Ua-Full-Version-List  ", secChUaFullVersionList);
        appendIfNotNull(sb, "Sec-Ch-Ua-Mobile             ", secChUaMobile);
        appendIfNotNull(sb, "Sec-Ch-Ua-Model              ", secChUaModel);
        appendIfNotNull(sb, "Sec-Ch-Ua-Platform           ", secChUaPlatform);
        appendIfNotNull(sb, "Sec-Ch-Ua-Platform-Version   ", secChUaPlatformVersion);
        appendIfNotNull(sb, "Sec-Ch-Ua-Wow64              ", secChUaWow64);
        return sb.toString();
    }

    public int compareFields(String ours, String other) {
        if (ours == null){
            if (other == null) {
                return 0;
            } else {
                return -1;
            }
        }
        if (other == null) {
            return 1;
        }
        return ours.compareTo(other);
    }

    @Override
    public int compareTo(LogRecord other) {
        int compare;

        compare = compareFields(userAgent, other.userAgent);
        if (compare != 0) {
            return compare;
        }

        compare = compareFields(secChUa, other.secChUa);
        if (compare != 0) {
            return compare;
        }

        compare = compareFields(secChUaArch, other.secChUaArch);
        if (compare != 0) {
            return compare;
        }

        compare = compareFields(secChUaBitness, other.secChUaBitness);
        if (compare != 0) {
            return compare;
        }

        compare = compareFields(secChUaFullVersion, other.secChUaFullVersion);
        if (compare != 0) {
            return compare;
        }

        compare = compareFields(secChUaFullVersionList, other.secChUaFullVersionList);
        if (compare != 0) {
            return compare;
        }

        compare = compareFields(secChUaMobile, other.secChUaMobile);
        if (compare != 0) {
            return compare;
        }

        compare = compareFields(secChUaModel, other.secChUaModel);
        if (compare != 0) {
            return compare;
        }

        compare = compareFields(secChUaPlatform, other.secChUaPlatform);
        if (compare != 0) {
            return compare;
        }

        compare = compareFields(secChUaPlatformVersion, other.secChUaPlatformVersion);
        if (compare != 0) {
            return compare;
        }

        compare = compareFields(secChUaWow64, other.secChUaWow64);
        return compare;
    }
}
