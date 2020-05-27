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

package nl.basjes.parse.useragent;

import nl.basjes.parse.useragent.UserAgent.MutableUserAgent;
import nl.basjes.parse.useragent.calculate.CalculateAgentName;
import nl.basjes.parse.useragent.calculate.CalculateDeviceBrand;
import nl.basjes.parse.useragent.calculate.ConcatNONDuplicatedCalculator;
import nl.basjes.parse.useragent.calculate.MajorVersionCalculator;
import org.junit.jupiter.api.Test;

import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME_VERSION;
import static nl.basjes.parse.useragent.UserAgent.AGENT_NAME_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.AGENT_VERSION;
import static nl.basjes.parse.useragent.UserAgent.AGENT_VERSION_MAJOR;
import static nl.basjes.parse.useragent.UserAgent.DEVICE_BRAND;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCalculators {

    @Test
    public void testFieldAgentNameVersionFallback() {
        MutableUserAgent userAgent = new MutableUserAgent();
        userAgent.setForced(UserAgent.DEVICE_BRAND, "some_thing", 1);
        userAgent.setForced(AGENT_VERSION, "1.2.3", 1);

        new CalculateDeviceBrand().calculate(userAgent);
        new CalculateAgentName().calculate(userAgent);

        new MajorVersionCalculator(AGENT_VERSION_MAJOR, AGENT_VERSION).calculate(userAgent);
        new ConcatNONDuplicatedCalculator(AGENT_NAME_VERSION, AGENT_NAME, AGENT_VERSION).calculate(userAgent);
        new ConcatNONDuplicatedCalculator(AGENT_NAME_VERSION_MAJOR, AGENT_NAME, AGENT_VERSION_MAJOR).calculate(userAgent);

        assertEquals("Some_Thing", userAgent.getValue(DEVICE_BRAND));
        assertEquals("Some_Thing", userAgent.getValue(AGENT_NAME));
        assertEquals("Some_Thing 1.2.3", userAgent.getValue(AGENT_NAME_VERSION));
        assertEquals("1", userAgent.getValue(AGENT_VERSION_MAJOR));
        assertEquals("Some_Thing 1", userAgent.getValue(AGENT_NAME_VERSION_MAJOR));
    }

}
