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

package nl.basjes.parse.useragent.flink;

import nl.basjes.parse.useragent.annotate.YauaaField;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.LocalEnvironment;
import org.apache.flink.api.java.io.LocalCollectionOutputFormat;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestUserAgentAnalysisMapperCHClass {

    public static class MyUserAgentAnalysisMapper extends UserAgentAnalysisMapper<TestRecord> {
        @Override
        public Map<String, String> getRequestHeaders(TestRecord element) {
            return element.getHeaders();
        }

        @SuppressWarnings("unused") // Called via the annotation
        @YauaaField("DeviceClass")
        public void setDeviceClass(TestRecord record, String value) {
            record.deviceClass = value;
        }

        @SuppressWarnings("unused") // Called via the annotation
        @YauaaField("AgentNameVersion")
        public void setAgentNameVersion(TestRecord record, String value) {
            record.agentNameVersion = value;
        }

        @SuppressWarnings("unused") // Called via the annotation
        @YauaaField("OperatingSystemNameVersion")
        public void setOperatingSystemNameVersion(TestRecord record, String value) {
            record.operatingSystemNameVersion = value;
        }
    }

    @Test
    void testClassDefinitionDataSet() throws Exception {
        ExecutionEnvironment environment = LocalEnvironment.getExecutionEnvironment();

        TestRecord testRecord1 = new TestRecord("Mozilla/5.0 (X11; Linux x86_64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/48.0.2564.82 Safari/537.36");
        TestRecord testRecord2 = new TestRecord("Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/53.0.2785.124 Mobile Safari/537.36");
        TestRecord testRecord3 = new TestRecord("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/100.0.4896.60 Safari/537.36",
                "\"macOS\"",
                "\"12.3.1\"");

        DataSet<TestRecord> resultDataSet = environment
            .fromElements(
                testRecord1, testRecord2, testRecord3
            )

            .map(new MyUserAgentAnalysisMapper());

        List<TestRecord> result = new ArrayList<>(5);
        resultDataSet
            .output(new LocalCollectionOutputFormat<>(result));

        environment.execute();

        assertEquals(3, result.size());

        TestRecord expectedRecord1 = new TestRecord(testRecord1)
            .expectDeviceClass("Desktop")
            .expectOperatingSystemNameVersion("Linux ??")
            .expectAgentNameVersion("Chrome 48.0.2564.82");

        TestRecord expectedRecord2 = new TestRecord(testRecord2)
            .expectDeviceClass("Phone")
            .expectOperatingSystemNameVersion("Android 7.0")
            .expectAgentNameVersion("Chrome 53.0.2785.124");

        TestRecord expectedRecord3 = new TestRecord(testRecord3)
            .expectDeviceClass("Desktop")
            .expectOperatingSystemNameVersion("Mac OS 12.3.1")
            .expectAgentNameVersion("Chrome 100.0.4896.60");

        assertThat(result, hasItems(
            expectedRecord1, expectedRecord2, expectedRecord3
        ));

    }

    @Test
    void testClassDefinitionDataStream() throws Exception {
        StreamExecutionEnvironment environment = LocalStreamEnvironment.getExecutionEnvironment();

        TestRecord testRecord1 = new TestRecord("Mozilla/5.0 (X11; Linux x86_64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/48.0.2564.82 Safari/537.36");
        TestRecord testRecord2 = new TestRecord("Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/53.0.2785.124 Mobile Safari/537.36");
        TestRecord testRecord3 = new TestRecord("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/100.0.4896.60 Safari/537.36",
            "\"macOS\"",
            "\"12.3.1\"");

        DataStream<TestRecord> resultDataStream = environment
            .fromElements(
                testRecord1, testRecord2, testRecord3
            )
            .map(new MyUserAgentAnalysisMapper());

        List<TestRecord> result = resultDataStream.executeAndCollect(100);

        assertEquals(3, result.size());

        TestRecord expectedRecord1 = new TestRecord(testRecord1)
            .expectDeviceClass("Desktop")
            .expectOperatingSystemNameVersion("Linux ??")
            .expectAgentNameVersion("Chrome 48.0.2564.82");

        TestRecord expectedRecord2 = new TestRecord(testRecord2)
            .expectDeviceClass("Phone")
            .expectOperatingSystemNameVersion("Android 7.0")
            .expectAgentNameVersion("Chrome 53.0.2785.124");

        TestRecord expectedRecord3 = new TestRecord(testRecord3)
            .expectDeviceClass("Desktop")
            .expectOperatingSystemNameVersion("Mac OS 12.3.1")
            .expectAgentNameVersion("Chrome 100.0.4896.60");

        assertThat(result, hasItems(
            expectedRecord1, expectedRecord2, expectedRecord3
        ));

    }

}
