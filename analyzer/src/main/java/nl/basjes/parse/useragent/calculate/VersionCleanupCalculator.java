/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2024 Niels Basjes
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

package nl.basjes.parse.useragent.calculate;

import nl.basjes.parse.useragent.AgentField;
import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;

import java.util.Collections;
import java.util.Set;

import static nl.basjes.parse.useragent.UserAgent.UNKNOWN_VERSION;

public class VersionCleanupCalculator extends FieldCalculator {

    private String versionName;

    public VersionCleanupCalculator(String versionName) {
        this.versionName = versionName;
    }

    @SuppressWarnings("unused") // Private constructor for serialization systems ONLY (like Kryo)
    private VersionCleanupCalculator() { }

    @Override
    public void calculate(MutableUserAgent userAgent) {
        AgentField agentVersionMajor = userAgent.get(versionName);
        if (agentVersionMajor.isDefaultValue()) {
            return; // No cleanups
        }
        if ("NULL".equalsIgnoreCase(agentVersionMajor.getValue())) {
            userAgent.setForced(
                versionName,
                UNKNOWN_VERSION,
                agentVersionMajor.getConfidence());
        }
    }

    @Override
    public String getCalculatedFieldName() {
        return versionName;
    }

    @Override
    public Set<String> getDependencies() {
        return Collections.emptySet();
    }

    @Override
    public String toString() {
        return "Cleanup " + versionName;
    }
}
