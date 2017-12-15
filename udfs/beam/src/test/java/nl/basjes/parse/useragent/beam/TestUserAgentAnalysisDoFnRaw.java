/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2017 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.useragent.beam;

import nl.basjes.parse.useragent.annonate.YauaaField;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFnTester;
import org.apache.beam.sdk.util.UserCodeException;
import org.junit.Test;

import java.io.Serializable;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestUserAgentAnalysisDoFnRaw implements Serializable {
    public static class TestDoFn extends UserAgentAnalysisDoFn<TestRecord> {
        @Override
        public String getUserAgentString(TestRecord record) {
            return record.useragent;
        }

        @YauaaField("DeviceClass")
        public void setDeviceClass(TestRecord record, String value) {
            record.deviceClass = value;
        }

        @YauaaField("AgentNameVersion")
        public void setAgentNameVersion(TestRecord record, String value) {
            record.agentNameVersion = value;
        }
    }

    @Test
    public void testUserAgentAnalysisDoFn() throws Exception {
        DoFn<TestRecord, TestRecord> fn = new TestDoFn();

        DoFnTester<TestRecord, TestRecord> fnTester = DoFnTester.of(fn);

        // Process a bundle containing a single input element:
        TestRecord testInput = new TestRecord("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/48.0.2564.82 Safari/537.36");
        List<TestRecord> testOutputs = fnTester.processBundle(testInput);

        assertEquals(1, testOutputs.size());
        TestRecord record = testOutputs.get(0);
        assertEquals("Desktop", record.deviceClass);
        assertEquals("Chrome 48.0.2564.82", record.agentNameVersion);
        assertNull(record.shouldRemainNull);
    }

    @Test(expected = UserCodeException.class)
    public void testImpossibleField() throws Exception {
        DoFn<TestRecord, TestRecord> fn = new UserAgentAnalysisDoFn<TestRecord>() {
            @Override
            public String getUserAgentString(TestRecord record) {
                return record.useragent;
            }

            @YauaaField("NielsBasjes")
            public void setImpossibleField(TestRecord record, String value) {
                record.agentNameVersion = value;
            }
        };

        DoFnTester<TestRecord, TestRecord> fnTester = DoFnTester.of(fn);

        // Process a bundle containing a single input element:
        TestRecord testInput = new TestRecord("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/48.0.2564.82 Safari/537.36");
        fnTester.processBundle(testInput);
    }
}
