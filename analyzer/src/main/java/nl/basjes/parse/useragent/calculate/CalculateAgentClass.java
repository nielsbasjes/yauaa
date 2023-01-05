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

package nl.basjes.parse.useragent.calculate;

import nl.basjes.parse.useragent.AgentField;
import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;

import java.util.Collections;
import java.util.Set;

import static nl.basjes.parse.useragent.UserAgent.AGENT_CLASS;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME;

public class CalculateAgentClass extends FieldCalculator {
    @Override
    public void calculate(MutableUserAgent userAgent) {
        // Cleanup the class of the useragent
        AgentField agentClass = userAgent.get(AGENT_CLASS);
        if (agentClass.isDefaultValue()) {
            AgentField agentName = userAgent.get(AGENT_NAME);
            if (!agentName.isDefaultValue()) {
                userAgent.setForced(
                    AGENT_CLASS,
                    "Special",
                    1);
            }
        }
    }

    @Override
    public String getCalculatedFieldName() {
        return AGENT_CLASS;
    }

    @Override
    public Set<String> getDependencies() {
        return Collections.singleton(AGENT_NAME);
    }
}
