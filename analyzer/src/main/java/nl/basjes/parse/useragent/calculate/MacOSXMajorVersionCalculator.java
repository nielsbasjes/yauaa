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

package nl.basjes.parse.useragent.calculate;

import nl.basjes.parse.useragent.AgentField;
import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;
import nl.basjes.parse.useragent.utils.VersionSplitter;

import java.util.Set;
import java.util.TreeSet;

import static nl.basjes.parse.useragent.UserAgent.NULL_VALUE;
import static nl.basjes.parse.useragent.UserAgent.OPERATING_SYSTEM_NAME;

public class MacOSXMajorVersionCalculator extends FieldCalculator {

    private String versionName;
    private String majorVersionName;

    public MacOSXMajorVersionCalculator(String majorVersionName, String versionName) {
        this.majorVersionName = majorVersionName;
        this.versionName = versionName;
    }

    @SuppressWarnings("unused") // Private constructor for serialization systems ONLY (like Kryo)
    private MacOSXMajorVersionCalculator() { }

    @Override
    public void calculate(MutableUserAgent userAgent) {
        if (!userAgent.get(OPERATING_SYSTEM_NAME).getValue().equals("Mac OS")) {
            return;
        }
        AgentField agentVersionMajor = userAgent.get(majorVersionName);
        if (agentVersionMajor.isDefaultValue()) {
            AgentField agentVersion = userAgent.get(versionName);
            String version = NULL_VALUE;
            if (!agentVersion.isDefaultValue()) {
                String value = agentVersion.getValue();
                if (value == null || !value.contains("10.")) {
                    return; // We only do this very different versioning scheme for Mac OS X 10.x
                }
                version = VersionSplitter.getInstance().getFirstSplits(value, 2);
            }
            userAgent.setForced(
                majorVersionName,
                version,
                agentVersion.getConfidence());
        }
    }

    @Override
    public String getCalculatedFieldName() {
        return majorVersionName;
    }

    @Override
    public Set<String> getDependencies() {
        Set<String> dependencies = new TreeSet<>();
        dependencies.add(versionName);
        dependencies.add(OPERATING_SYSTEM_NAME);
        return dependencies;
    }

    @Override
    public String toString() {
        return "Calculate MacOSX Major Version" + versionName + " --> " + majorVersionName;
    }
}
