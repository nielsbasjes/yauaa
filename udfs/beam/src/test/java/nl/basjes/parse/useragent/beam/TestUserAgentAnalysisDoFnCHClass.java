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

package nl.basjes.parse.useragent.beam;

import nl.basjes.parse.useragent.annotate.YauaaField;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RunWith(JUnit4.class)
public class TestUserAgentAnalysisDoFnCHClass implements Serializable {

    public static class MyUserAgentAnalysisDoFn extends UserAgentAnalysisDoFn<TestRecord> {
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

    @Rule
    public final transient TestPipeline pipeline = TestPipeline.create();

    @Test
    public void testInlineDefinition() { // NOSONAR java:S2699 False positive because PAssert is unknown to Sonar
        List<TestRecord> useragents = Arrays.asList(
            new TestRecord("Mozilla/5.0 (X11; Linux x86_64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/48.0.2564.82 Safari/537.36"),
            new TestRecord("Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/53.0.2785.124 Mobile Safari/537.36"),
            new TestRecord("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/100.0.4896.60 Safari/537.36",
                "\"macOS\"",
                "\"12.3.1\"")
        );

        // Apply Create, passing the list and the coder, to create the PCollection.
        PCollection<TestRecord> testRecords = pipeline.apply(Create.of(useragents));

        PCollection<TestRecord> filledTestRecords = testRecords
            .apply("Extract Elements from Useragent",
                ParDo.of(new MyUserAgentAnalysisDoFn()));

        TestRecord expectedRecord1 = new TestRecord(useragents.get(0));
        expectedRecord1.deviceClass = "Desktop";
        expectedRecord1.operatingSystemNameVersion = "Linux ??";
        expectedRecord1.agentNameVersion = "Chrome 48.0.2564.82";

        TestRecord expectedRecord2 = new TestRecord(useragents.get(1));
        expectedRecord2.deviceClass = "Phone";
        expectedRecord2.operatingSystemNameVersion = "Android 7.0";
        expectedRecord2.agentNameVersion = "Chrome 53.0.2785.124";

        TestRecord expectedRecord3 = new TestRecord(useragents.get(2));
        expectedRecord3.deviceClass = "Desktop";
        expectedRecord3.operatingSystemNameVersion = "Mac OS 12.3.1";
        expectedRecord3.agentNameVersion = "Chrome 100.0.4896.60";

        PAssert.that(filledTestRecords).containsInAnyOrder(expectedRecord1, expectedRecord2, expectedRecord3);

        pipeline.run().waitUntilFinish();
    }

}
