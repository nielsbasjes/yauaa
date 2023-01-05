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

package nl.basjes.parse.useragent.beam;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import static nl.basjes.parse.useragent.UserAgent.USERAGENT_HEADER;

public class TestRecord implements Serializable {
    final TreeMap<String, String> headers = new TreeMap<>();
    String deviceClass;
    String operatingSystemNameVersion;

    String agentNameVersion;

    String shouldRemainNull = null;

    public TestRecord(String useragent) {
        headers.put(USERAGENT_HEADER, useragent);
    }
    public TestRecord(String useragent, String platform, String platformVersion) {
        headers.put(USERAGENT_HEADER,             useragent);
        headers.put("Sec-CH-UA-Platform",         platform);
        headers.put("Sec-CH-UA-Platform-Version", platformVersion);
    }

    public TestRecord(TestRecord original) {
        headers.putAll(original.headers);
        deviceClass                 = original.deviceClass;
        operatingSystemNameVersion  = original.operatingSystemNameVersion;
        agentNameVersion            = original.agentNameVersion;
        shouldRemainNull            = original.shouldRemainNull;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getUserAgent() {
        return headers.get(USERAGENT_HEADER);
    }

    @Override
    public String toString() {
        return "TestRecord{" +
            "headers='" + headers + '\'' +
            ", deviceClass='"                   + deviceClass + '\'' +
            ", operatingSystemNameVersion='"    + operatingSystemNameVersion + '\'' +
            ", agentNameVersion='"              + agentNameVersion + '\'' +
            ", shouldRemainNull='"              + shouldRemainNull + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TestRecord)) {
            return false;
        }

        TestRecord record = (TestRecord) o;

        return
            headers.equals(record.headers)         &&
            isSame(deviceClass,                record.deviceClass) &&
            isSame(operatingSystemNameVersion, record.operatingSystemNameVersion) &&
            isSame(agentNameVersion,           record.agentNameVersion) &&
            isSame(shouldRemainNull,           record.shouldRemainNull);
    }

    private boolean isSame(String a, String b){
        if (a == null || b == null) {
            return (a == null && b == null);
        }
        return a.equals(b);
    }

    @Override
    public int hashCode() {
        int result = headers.hashCode();
        result = 31 * result + (deviceClass                 != null ? deviceClass.hashCode()                : 0);
        result = 31 * result + (agentNameVersion            != null ? agentNameVersion.hashCode()           : 0);
        result = 31 * result + (operatingSystemNameVersion  != null ? operatingSystemNameVersion.hashCode() : 0);
        result = 31 * result + (shouldRemainNull            != null ? shouldRemainNull.hashCode()           : 0);
        return result;
    }
}
