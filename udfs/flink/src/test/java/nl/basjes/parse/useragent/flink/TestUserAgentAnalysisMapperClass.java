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

import nl.basjes.parse.useragent.annotate.YauaaField;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.LocalEnvironment;
import org.apache.flink.api.java.io.LocalCollectionOutputFormat;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TestUserAgentAnalysisMapperClass {

    public static class MyUserAgentAnalysisMapper extends UserAgentAnalysisMapper<TestRecord> {
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
    public void testClassDefinitionDataSet() throws Exception {
        ExecutionEnvironment environment = LocalEnvironment.getExecutionEnvironment();

        DataSet<TestRecord> testRecordDataSet = environment
            .fromElements(
                "Mozilla/5.0 (X11; Linux x86_64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/48.0.2564.82 Safari/537.36",

                "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/53.0.2785.124 Mobile Safari/537.36"
            )

            .map((MapFunction<String, TestRecord>) TestRecord::new)

            .map(new MyUserAgentAnalysisMapper());

        List<TestRecord> result = new ArrayList<>(5);
        testRecordDataSet
            .output(new LocalCollectionOutputFormat<>(result));

        environment.execute();

        assertEquals(2, result.size());

        assertThat(result, hasItems(
            new TestRecord(
                "Mozilla/5.0 (X11; Linux x86_64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/48.0.2564.82 Safari/537.36",
                "Desktop",
                "Chrome 48.0.2564.82",
                null),

            new TestRecord(
                "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/53.0.2785.124 Mobile Safari/537.36",
                "Phone",
                "Chrome 53.0.2785.124",
                null)
        ));
    }

    @Test
    public void testClassDefinitionDataStream() throws Exception {
        StreamExecutionEnvironment environment = LocalStreamEnvironment.getExecutionEnvironment();

        DataStream<TestRecord> testRecordDataSet = environment
            .fromElements(
                "Mozilla/5.0 (X11; Linux x86_64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/48.0.2564.82 Safari/537.36",

                "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/53.0.2785.124 Mobile Safari/537.36"
            )

            .map((MapFunction<String, TestRecord>) TestRecord::new)

            .map(new MyUserAgentAnalysisMapper());

        List<TestRecord> result = new ArrayList<>(5);
        testRecordDataSet
            .writeUsingOutputFormat(new LocalCollectionOutputFormat<>(result));

        environment.execute();

        assertEquals(2, result.size());

        assertThat(result, hasItems(
            new TestRecord(
                "Mozilla/5.0 (X11; Linux x86_64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/48.0.2564.82 Safari/537.36",
                "Desktop",
                "Chrome 48.0.2564.82",
                null),

            new TestRecord(
                "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/53.0.2785.124 Mobile Safari/537.36",
                "Phone",
                "Chrome 53.0.2785.124",
                null)
        ));
    }

}
