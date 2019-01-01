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

package nl.basjes.parse.useragent.beam;

import nl.basjes.parse.useragent.analyze.InvalidParserConfigurationException;
import nl.basjes.parse.useragent.annotate.YauaaField;
import org.apache.beam.sdk.testing.NeedsRunner;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.testing.ValidatesRunner;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

import java.io.Serializable;

@Category(ValidatesRunner.class)
public class TestUserAgentAnalysisDoFnRaw implements Serializable {

    @Rule
    public final transient TestPipeline pipeline = TestPipeline.create();

    @Rule
    public final transient ExpectedException expectedEx = ExpectedException.none();

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
    @Category(NeedsRunner.class)
    public void testUserAgentAnalysisDoFn() {
        // Process a single input element:
        String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/48.0.2564.82 Safari/537.36";
        TestRecord testInput = new TestRecord(userAgent);

        TestRecord expectedOutput = new TestRecord(userAgent);
        expectedOutput.agentNameVersion = "Chrome 48.0.2564.82";
        expectedOutput.deviceClass = "Desktop";

        PCollection<TestRecord> input = pipeline
            .apply("Create", Create.of(testInput));

        PCollection<TestRecord> testOutputs = input
            .apply(ParDo.of(new TestDoFn()));

        PAssert.that(testOutputs).containsInAnyOrder(expectedOutput);
        pipeline.run();
    }

    @Test
    public void testImpossibleField() throws Throwable {
        expectedEx.expect(InvalidParserConfigurationException.class);
        expectedEx.expectMessage("We cannot provide these fields:[NielsBasjes]");
        try {
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

            String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/48.0.2564.82 Safari/537.36";
            TestRecord testInput = new TestRecord(userAgent);

            PCollection<TestRecord> input = pipeline
                .apply("Create", Create.of(testInput));

            PCollection<TestRecord> testOutputs = input
                .apply(ParDo.of(fn));

            pipeline.run();
        } catch (Exception e) {

// The actual class thrown here turns out to be
//   org.apache.beam.repackaged.beam_runners_direct_java.com.google.common.util.concurrent.UncheckedExecutionException
// which is a repackaged version of a Guava class.
// which is the same class that was thrown in Beam 2.4.0 but in a DIFFERENT package.
// Turns out Apache Beam 2.5.0 made a total mess of this because they repackaged this class in at least 5 package names
// in the beam-runners-direct-java and also in the beam-sdks-java-core.
// So to reduce the mess at this end I'm simply taking the underlying cause class ( org.apache.beam.sdk.util.UserCodeException )
// and pulling the real exception my code throws to do the check.
            throw e.getCause().getCause();
        }
    }
}
