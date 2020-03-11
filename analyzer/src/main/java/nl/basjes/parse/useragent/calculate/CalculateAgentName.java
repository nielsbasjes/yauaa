/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2020 Niels Basjes
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
import nl.basjes.parse.useragent.utils.Normalize;

import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME;
import static nl.basjes.parse.useragent.utils.Normalize.isLowerCase;

public class CalculateAgentName implements FieldCalculator {
    @Override
    public void calculate(UserAgent userAgent) {
        // Cleanup the name of the useragent
        UserAgent.AgentField name = userAgent.get(AGENT_NAME);
        if (name != null && name.getConfidence() >= 0) {
            String value = name.getValue();
            if (isLowerCase(value)) {
                userAgent.setForced(
                    AGENT_NAME,
                    Normalize.brand(value),
                    name.getConfidence());
            }
        }
    }

    @Override
    public String toString() {
        return "Calculate " + AGENT_NAME;
    }

}
