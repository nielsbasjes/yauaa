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

package nl.basjes.parse.useragent;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestConcatenation {

    private UserAgent createUserAgent() {
        UserAgent         userAgent = new UserAgent();
        userAgent.setForced("MinusOne", "MinusOne", -1);
        userAgent.setForced("Zero", "Zero", 0);
        userAgent.setForced("One", "One", 1);
        userAgent.setForced("Two", "Two", 2);
        userAgent.setForced("One Two", "One Two", 12);
        return userAgent;
    }

    @Test
    public void testFieldConcatenation() {
        UserAgentAnalyzer uaa       = UserAgentAnalyzer.newBuilder().dropTests().dropDefaultResources().build();
        UserAgent         userAgent = createUserAgent();

        uaa.concatFieldValuesNONDuplicated(userAgent, "Combined1", "One", "Two");
        assertEquals("One Two", userAgent.getValue("Combined1"));

        uaa.concatFieldValuesNONDuplicated(userAgent, "Combined2", "One", "One");
        assertEquals("One", userAgent.getValue("Combined2"));

        uaa.concatFieldValuesNONDuplicated(userAgent, "Combined3", "MinusOne", "One");
        assertEquals("MinusOne One", userAgent.getValue("Combined3"));

        uaa.concatFieldValuesNONDuplicated(userAgent, "Combined4", "One", "MinusOne");
        assertEquals("One MinusOne", userAgent.getValue("Combined4"));
    }

    @Test
    public void testFieldConcatenationNulls() {
        UserAgentAnalyzer uaa       = UserAgentAnalyzer.newBuilder().dropTests().dropDefaultResources().build();
        UserAgent         userAgent = createUserAgent();

        uaa.concatFieldValuesNONDuplicated(userAgent, "Combined3", "MinusOne", null);
        assertEquals("Unknown", userAgent.getValue("Combined3"));

        uaa.concatFieldValuesNONDuplicated(userAgent, "Combined4", null, "MinusOne");
        assertEquals("Unknown", userAgent.getValue("Combined4"));

        uaa.concatFieldValuesNONDuplicated(userAgent, "Combined3", null, "One");
        assertEquals("One", userAgent.getValue("Combined3"));

        uaa.concatFieldValuesNONDuplicated(userAgent, "Combined4", "One", null);
        assertEquals("One", userAgent.getValue("Combined4"));
    }

    @Test
    public void testFieldConcatenationSamePrefix() {
        UserAgentAnalyzer uaa       = UserAgentAnalyzer.newBuilder().dropTests().dropDefaultResources().build();
        UserAgent         userAgent = createUserAgent();

        uaa.concatFieldValuesNONDuplicated(userAgent, "Combined1", "One", "Two");
        assertEquals("One Two", userAgent.getValue("Combined1"));

        uaa.concatFieldValuesNONDuplicated(userAgent, "Combined2", "One", "One");
        assertEquals("One", userAgent.getValue("Combined2"));

        uaa.concatFieldValuesNONDuplicated(userAgent, "Combined3", "One", "One Two");
        assertEquals("One Two", userAgent.getValue("Combined3"));
    }


    @Test
    public void testFieldConcatenationNonExistent() {
        UserAgentAnalyzer uaa       = UserAgentAnalyzer.newBuilder().dropTests().dropDefaultResources().build();
        UserAgent         userAgent = createUserAgent();

        uaa.concatFieldValuesNONDuplicated(userAgent, "Combined2", "One", "NonExistent");
        assertEquals("One", userAgent.getValue("Combined2"));

        uaa.concatFieldValuesNONDuplicated(userAgent, "Combined3", "NonExistent", "Two");
        assertEquals("Two", userAgent.getValue("Combined3"));

        uaa.concatFieldValuesNONDuplicated(userAgent, "Combined4", "NonExistent1", "NonExistent2");
        assertEquals("Unknown", userAgent.getValue("Combined4"));
    }

    @Test
    public void testFieldConcatenationNull() {
        UserAgentAnalyzer uaa       = UserAgentAnalyzer.newBuilder().dropTests().dropDefaultResources().build();
        UserAgent         userAgent = createUserAgent();

        uaa.concatFieldValuesNONDuplicated(userAgent, "Combined2", "One", null);
        assertEquals("One", userAgent.getValue("Combined2"));

        uaa.concatFieldValuesNONDuplicated(userAgent, "Combined3", null, "Two");
        assertEquals("Two", userAgent.getValue("Combined3"));

        uaa.concatFieldValuesNONDuplicated(userAgent, "Combined4", null, null);
        assertEquals("Unknown", userAgent.getValue("Combined4"));
    }

    @Test
    public void testFieldConcatenationNoConfidence() {
        UserAgentAnalyzer uaa       = UserAgentAnalyzer.newBuilder().dropTests().dropDefaultResources().build();
        UserAgent         userAgent = createUserAgent();

        uaa.concatFieldValuesNONDuplicated(userAgent, "Combined2", "One", "MinusOne");
        assertEquals("One MinusOne", userAgent.getValue("Combined2"));

        uaa.concatFieldValuesNONDuplicated(userAgent, "Combined3", "MinusOne", "Two");
        assertEquals("MinusOne Two", userAgent.getValue("Combined3"));
    }

    @Test
    public void testFieldConcatenationUnwanted() {
        UserAgentAnalyzer uaa       = UserAgentAnalyzer.newBuilder().dropTests().withField("DeviceClass").build();
        UserAgent         userAgent = createUserAgent();

        uaa.concatFieldValuesNONDuplicated(userAgent, "Unwanted", "One", "Two");
        assertEquals("Unknown", userAgent.getValue("Unwanted"));
    }
}
