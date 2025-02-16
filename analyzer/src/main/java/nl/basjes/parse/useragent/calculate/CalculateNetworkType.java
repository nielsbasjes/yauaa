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

package nl.basjes.parse.useragent.calculate;

import nl.basjes.parse.useragent.AgentField;
import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;
import nl.basjes.parse.useragent.utils.Normalize;

import static nl.basjes.parse.useragent.UserAgent.NETWORK_TYPE;

public class CalculateNetworkType extends FieldCalculator {

    @Override
    public void calculate(MutableUserAgent userAgent) {
        // Make sure the DeviceName always starts with the DeviceBrand
        AgentField networkType = userAgent.get(NETWORK_TYPE);
        if (!networkType.isDefaultValue()) {
            userAgent.setForced(
                NETWORK_TYPE,
                Normalize.brand(networkType.getValue()),
                networkType.getConfidence());
        }
    }

    @Override
    public String getCalculatedFieldName() {
        return NETWORK_TYPE;
    }

    @Override
    public String toString() {
        return "Calculate " + NETWORK_TYPE;
    }

}
