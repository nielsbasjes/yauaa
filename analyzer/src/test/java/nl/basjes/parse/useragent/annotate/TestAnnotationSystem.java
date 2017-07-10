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

package nl.basjes.parse.useragent.annotate;

import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import nl.basjes.parse.useragent.annonate.UserAgentAnnotationAnalyzer;
import nl.basjes.parse.useragent.annonate.UseragentAnnotationMapper;
import nl.basjes.parse.useragent.annonate.YauaaField;
import org.junit.Test;

import java.io.Serializable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestAnnotationSystem {

    public static class TestRecord implements Serializable {
        String useragent;
        String deviceClass;
        String agentNameVersion;

        public TestRecord(String useragent) {
            this.useragent = useragent;
        }
    }

    @SuppressWarnings("unused")
    public static class MyMapper
        implements UseragentAnnotationMapper<TestRecord>, Serializable {
        private transient UserAgentAnnotationAnalyzer<TestRecord> userAgentAnalyzer = null;

        public MyMapper() {
            userAgentAnalyzer = new UserAgentAnnotationAnalyzer<>();
            userAgentAnalyzer.initialize(this);
        }

        public TestRecord enrich(TestRecord record) {
            return userAgentAnalyzer.map(record);
        }

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

        @YauaaField("DeviceClass")
        public void wrongSetDeviceClass1(String record, String value, String extra) {
            fail("May NEVER call this method");
        }

        @YauaaField("DeviceClass")
        public void wrongSetDeviceClass2(TestRecord record, String value, String extra) {
            fail("May NEVER call this method");
        }

        @YauaaField("DeviceClass")
        public void wrongSetDeviceClass3(TestRecord record, Double value) {
            fail("May NEVER call this method");
        }

    }

    @Test
    public void testAnnotationBasedParser(){
        MyMapper mapper = new MyMapper();

        TestRecord record = new TestRecord("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.82 Safari/537.36");

        record = mapper.enrich(record);

        assertEquals("Desktop", record.deviceClass);
        assertEquals("Chrome 48.0.2564.82", record.agentNameVersion);
    }

    @SuppressWarnings("unused")
    public static class TestImpossibleFieldMapper
        implements UseragentAnnotationMapper<TestRecord>, Serializable {
        private transient UserAgentAnnotationAnalyzer<TestRecord> userAgentAnalyzer = null;

        public TestImpossibleFieldMapper() {
            userAgentAnalyzer = new UserAgentAnnotationAnalyzer<>();
            userAgentAnalyzer.initialize(this);
        }

        public TestRecord enrich(TestRecord record) {
            return userAgentAnalyzer.map(record);
        }

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
    public void testImpossibleField() throws Exception {
        new TestImpossibleFieldMapper();
    }

}
