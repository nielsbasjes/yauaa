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

package nl.basjes.parse.useragent.beam;

import org.apache.beam.sdk.coders.ListCoder;
import org.apache.beam.sdk.coders.RowCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.extensions.sql.SqlTransform;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.testing.NeedsRunner;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.testing.ValidatesRunner;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.Row;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Category(ValidatesRunner.class)
public class TestParseUserAgentSQL implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(TestParseUserAgentSQL.class);

    @Rule
    public final transient TestPipeline pipeline = TestPipeline.create();


    @Test
    @Category(NeedsRunner.class)
    public void testUserAgentAnalysisSQL() {
        // ============================================================
        // The base test input
        // List of Input UserAgent and expected { DeviceClass, AgentNameVersion }
        List<List<String>> useragents = Arrays.asList(
            Arrays.asList(
                "Mozilla/5.0 (X11; Linux x86_64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/48.0.2564.82 Safari/537.36",
                "Desktop",
                "Chrome 48.0.2564.82"),

            Arrays.asList(
                "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/53.0.2785.124 Mobile Safari/537.36",
                "Phone",
                "Chrome 53.0.2785.124")
        );

        // ============================================================
        // Convert into a PCollection<Row>
        Schema inputSchema = Schema
            .builder()
            .addStringField("userAgent")
            .addStringField("expectedDeviceClass")
            .addStringField("expectedAgentNameVersion")
            .build();

        PCollection<Row> input = pipeline
            .apply(Create.of(useragents))
            .setCoder(ListCoder.of(StringUtf8Coder.of()))
            .apply(ParDo.of(new DoFn<List<String>, Row>() {
                @ProcessElement
                public void processElement(ProcessContext c) {
                    // Get the current POJO instance
                    List<String> inputValue = c.element();

                    // Create a Row with the appSchema schema
                    // and values from the current POJO
                    Row appRow =
                        Row
                            .withSchema(inputSchema)
                            .addValues(inputValue.get(0))
                            .addValues(inputValue.get(1))
                            .addValues(inputValue.get(2))
                            .build();

                    // Output the Row representing the current POJO
                    c.output(appRow);
                }
            }))
            .setCoder(RowCoder.of(inputSchema));


        // ============================================================

        // Define a SQL query which calls the UDF
        String sql =
            "SELECT" +
                " userAgent                                         AS userAgent " +
                ", expectedDeviceClass                              AS expectedDeviceClass " +
                ", expectedAgentNameVersion                         AS expectedAgentNameVersion " +
                ", ParseUserAgent(userAgent)                        AS parsedUserAgent " +
                ", ParseUserAgent(userAgent)['DeviceClass']         AS deviceClass " +
                ", ParseUserAgent(userAgent)['AgentNameVersion']    AS agentNameVersion " +
                "FROM InputStream";

        // Create and apply the PTransform representing the query.
        // Register the UDFs used in the query by calling '.registerUdf()' with
        // either a class which implements BeamSqlUdf or with
        // an instance of the SerializableFunction;
        PCollection<Row> result =
            // This way we give a name to the input stream for use in the SQL
            PCollectionTuple.of("InputStream", input)
                // Apply the SQL with the UDFs we need.
                .apply("Execute SQL", SqlTransform
                    .query(sql)
                    .registerUdf("ParseUserAgent", new ParseUserAgent())
                );

        result.apply(ParDo.of(new RowPrinter()));

//        // Assert on the results.
//        PAssert.that(result)
//            .containsInAnyOrder((Row)null);

        // FIXME: This DOES NOT work at the time of writing.
        //  waiting for https://issues.apache.org/jira/browse/BEAM-9267
        //  to be implemented which depends on Calcite 1.22 to be released.

        pipeline.run().waitUntilFinish();

    }

    public static class RowPrinter extends DoFn<Row, Row> {
        @DoFn.ProcessElement
        public void processElement(ProcessContext c) {
            final Row row = c.element();
            LOG.info("ROW: {} --> {}", row, row.getSchema());
        }
    }

}
