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
import nl.basjes.parse.useragent.utils.Splitter;
import nl.basjes.parse.useragent.utils.VersionSplitter;

import java.util.Collections;
import java.util.Set;

import static nl.basjes.parse.useragent.UserAgent.NULL_VALUE;

public class MajorVersionCalculator extends FieldCalculator {

    private String versionName;
    private String majorVersionName;

    public MajorVersionCalculator(String majorVersionName, String versionName) {
        this.majorVersionName = majorVersionName;
        this.versionName = versionName;
    }

    @SuppressWarnings("unused") // Private constructor for serialization systems ONLY (like Kryo)
    private MajorVersionCalculator() { }

    private static final Splitter VERSION_SPLITTER = new VersionSplitter() {
        @Override
        public boolean isSeparator(char c) {
            switch (c) {
                case '|': // Next segment
                case '.':
//                case '_':  --> We do NOT use this one as a separator here.
//                case '-':  --> We do NOT use this one as a separator here.
                case ',':
                    return true;
                default:
                    return false;
            }
        }
    };

    @Override
    public void calculate(MutableUserAgent userAgent) {
        AgentField agentVersionMajor = userAgent.get(majorVersionName);
        if (agentVersionMajor.isDefaultValue()) {
            AgentField agentVersion = userAgent.get(versionName);
            String version = NULL_VALUE;
            if (!agentVersion.isDefaultValue()) {
                version = VERSION_SPLITTER.getSingleSplit(agentVersion.getValue(), 1);
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
        return Collections.singleton(versionName);
    }

    @Override
    public String toString() {
        return "Calculate " + versionName + " --> " + majorVersionName;
    }
}
