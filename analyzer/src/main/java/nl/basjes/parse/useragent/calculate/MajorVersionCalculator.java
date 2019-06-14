/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2019 Niels Basjes
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

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgent.AgentField;
import nl.basjes.parse.useragent.utils.VersionSplitter;

public class MajorVersionCalculator implements FieldCalculator {

    private String versionName;
    private String majorVersionName;

    public MajorVersionCalculator(String versionName, String majorVersionName) {
        this.versionName = versionName;
        this.majorVersionName = majorVersionName;
    }

    // Private constructor for serialization systems ONLY (like Kyro)
    private MajorVersionCalculator() { }

    @Override
    public void calculate(UserAgent userAgent) {
        AgentField agentVersionMajor = userAgent.get(majorVersionName);
        if (agentVersionMajor == null || agentVersionMajor.getConfidence() == -1) {
            AgentField agentVersion = userAgent.get(versionName);
            if (agentVersion != null) {
                String version = agentVersion.getValue();
                if (version != null) {
                    version = VersionSplitter.getInstance().getSingleSplit(agentVersion.getValue(), 1);
                } else {
                    version = "??";
                }
                userAgent.setForced(
                    majorVersionName,
                    version,
                    agentVersion.getConfidence());
            }
        }
    }
}
