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

package nl.basjes.parse.useragent.flink;

import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import nl.basjes.parse.useragent.annotate.YauaaField;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestUserAgentAnalysisMapperRaw {
    public static class TestMapper extends UserAgentAnalysisMapper<TestRecord> {
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
    public void testUserAgentParser() {
        TestMapper mapper = new TestMapper();

        mapper.open(null);

        TestRecord record = new TestRecord("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/48.0.2564.82 Safari/537.36");

        record = mapper.map(record);

        assertEquals(
            new TestRecord(
                "Mozilla/5.0 (X11; Linux x86_64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/48.0.2564.82 Safari/537.36",
                "Desktop",
                "Chrome 48.0.2564.82",
                null),
            record);
    }

    public static class TestImpossibleFieldMapper extends UserAgentAnalysisMapper<TestRecord> {
        @Override
        public String getUserAgentString(TestRecord record) {
            return record.useragent;
        }

        @YauaaField("NielsBasjes")
        public void setImpossibleField(TestRecord record, String value) {
            record.agentNameVersion = value;
        }
    }

    @Test(expected = InvalidParserConfigurationException.class)
    public void testImpossibleField() {
        TestImpossibleFieldMapper mapper = new TestImpossibleFieldMapper();
        mapper.open(null);
    }
}
