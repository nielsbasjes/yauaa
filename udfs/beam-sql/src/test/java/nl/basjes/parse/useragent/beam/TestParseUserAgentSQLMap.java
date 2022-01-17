/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2022 Niels Basjes
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

import org.apache.beam.sdk.coders.RowCoder;
import org.apache.beam.sdk.extensions.sql.SqlTransform;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.testing.NeedsRunner;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.testing.ValidatesRunner;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.Row;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static nl.basjes.parse.useragent.UserAgent.USERAGENT_FIELDNAME;

@Category(ValidatesRunner.class)
public class TestParseUserAgentSQLMap implements Serializable {

    private static final Logger LOG = LogManager.getLogger(TestParseUserAgentSQLMap.class);

    @Rule
    public final transient TestPipeline pipeline = TestPipeline.create();

    public static Row listToRow(List<Object> values, Schema schema) {
        Row.Builder appRowBuilder = Row.withSchema(schema);
        values.forEach(appRowBuilder::addValues);
        return appRowBuilder.build();
    }

    @Test
    @Category(NeedsRunner.class)
    public void testUserAgentAnalysisSQL() { // NOSONAR java:S2699 Tests should include assertions: Uses PAssert
        // ============================================================

        String agent1 =
            "Mozilla/5.0 (X11; Linux x86_64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/48.0.2564.82 Safari/537.36";

        Map<String, String> agent1ParsedAllFields = new TreeMap<>();
        agent1ParsedAllFields.put(USERAGENT_FIELDNAME, agent1);

        agent1ParsedAllFields.put("DeviceClass",                      "Desktop");
        agent1ParsedAllFields.put("DeviceName",                       "Linux Desktop");
        agent1ParsedAllFields.put("DeviceBrand",                      "Unknown");
        agent1ParsedAllFields.put("DeviceCpu",                        "Intel x86_64");
        agent1ParsedAllFields.put("DeviceCpuBits",                    "64");
        agent1ParsedAllFields.put("OperatingSystemClass",             "Desktop");
        agent1ParsedAllFields.put("OperatingSystemName",              "Linux");
        agent1ParsedAllFields.put("OperatingSystemVersion",           "??");
        agent1ParsedAllFields.put("OperatingSystemVersionMajor",      "??");
        agent1ParsedAllFields.put("OperatingSystemNameVersion",       "Linux ??");
        agent1ParsedAllFields.put("OperatingSystemNameVersionMajor",  "Linux ??");
        agent1ParsedAllFields.put("LayoutEngineClass",                "Browser");
        agent1ParsedAllFields.put("LayoutEngineName",                 "Blink");
        agent1ParsedAllFields.put("LayoutEngineVersion",              "48.0");
        agent1ParsedAllFields.put("LayoutEngineVersionMajor",         "48");
        agent1ParsedAllFields.put("LayoutEngineNameVersion",          "Blink 48.0");
        agent1ParsedAllFields.put("LayoutEngineNameVersionMajor",     "Blink 48");
        agent1ParsedAllFields.put("AgentClass",                       "Browser");
        agent1ParsedAllFields.put("AgentName",                        "Chrome");
        agent1ParsedAllFields.put("AgentVersion",                     "48.0.2564.82");
        agent1ParsedAllFields.put("AgentVersionMajor",                "48");
        agent1ParsedAllFields.put("AgentNameVersion",                 "Chrome 48.0.2564.82");
        agent1ParsedAllFields.put("AgentNameVersionMajor",            "Chrome 48");

        Map<String, String> agent1ParsedSomeFields = new TreeMap<>();
        agent1ParsedSomeFields.put("DeviceClass",                      "Desktop");
        agent1ParsedSomeFields.put("AgentNameVersion",                 "Chrome 48.0.2564.82");

        String agent2 =
            "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/53.0.2785.124 Mobile Safari/537.36";

        Map<String, String> agent2ParsedAllFields = new TreeMap<>();

        agent2ParsedAllFields.put(USERAGENT_FIELDNAME, agent2);
        agent2ParsedAllFields.put("DeviceClass",                      "Phone");
        agent2ParsedAllFields.put("DeviceName",                       "Google Nexus 6");
        agent2ParsedAllFields.put("DeviceBrand",                      "Google");
        agent2ParsedAllFields.put("OperatingSystemClass",             "Mobile");
        agent2ParsedAllFields.put("OperatingSystemName",              "Android");
        agent2ParsedAllFields.put("OperatingSystemVersion",           "7.0");
        agent2ParsedAllFields.put("OperatingSystemVersionMajor",      "7");
        agent2ParsedAllFields.put("OperatingSystemNameVersion",       "Android 7.0");
        agent2ParsedAllFields.put("OperatingSystemNameVersionMajor",  "Android 7");
        agent2ParsedAllFields.put("OperatingSystemVersionBuild",      "NBD90Z");
        agent2ParsedAllFields.put("LayoutEngineClass",                "Browser");
        agent2ParsedAllFields.put("LayoutEngineName",                 "Blink");
        agent2ParsedAllFields.put("LayoutEngineVersion",              "53.0");
        agent2ParsedAllFields.put("LayoutEngineVersionMajor",         "53");
        agent2ParsedAllFields.put("LayoutEngineNameVersion",          "Blink 53.0");
        agent2ParsedAllFields.put("LayoutEngineNameVersionMajor",     "Blink 53");
        agent2ParsedAllFields.put("AgentClass",                       "Browser");
        agent2ParsedAllFields.put("AgentName",                        "Chrome");
        agent2ParsedAllFields.put("AgentVersion",                     "53.0.2785.124");
        agent2ParsedAllFields.put("AgentVersionMajor",                "53");
        agent2ParsedAllFields.put("AgentNameVersion",                 "Chrome 53.0.2785.124");
        agent2ParsedAllFields.put("AgentNameVersionMajor",            "Chrome 53");

        Map<String, String> agent2ParsedSomeFields = new TreeMap<>();
        agent2ParsedSomeFields.put("DeviceClass",                     "Phone");
        agent2ParsedSomeFields.put("AgentNameVersion",                "Chrome 53.0.2785.124");

        // ---------------
        // The base test input
        List<List<Object>> inputValues = Arrays.asList(
            Collections.singletonList(agent1),
            Collections.singletonList(agent2)
        );

        List<Row> inputRows = new ArrayList<>(inputValues.size());
        Schema inputSchema = Schema
            .builder()
            .addStringField("userAgent")
            .build();

        for (List<Object> inputValue: inputValues) {
            inputRows.add(listToRow(inputValue, inputSchema));
        }

        // ---------------
        // The expected test output
        List<List<Object>> expectedList = Arrays.asList(
            Arrays.asList(
                agent1,
                agent1ParsedAllFields.get("DeviceClass"),
                agent1ParsedAllFields.get("AgentNameVersion"),
//                agent1ParsedAllFields,
                agent1ParsedSomeFields
            ),
            Arrays.asList(
                agent2,
                agent2ParsedAllFields.get("DeviceClass"),
                agent2ParsedAllFields.get("AgentNameVersion"),
//                agent2ParsedAllFields,
                agent2ParsedSomeFields
            )
        );

        List<Row> expectedRows = new ArrayList<>(expectedList.size());
        Schema expectedSchema = Schema
            .builder()
            .addStringField("userAgent")
            .addStringField("deviceClass")
            .addStringField("agentNameVersion")
//            .addMapField("allFields", Schema.FieldType.STRING, Schema.FieldType.STRING)
            .addMapField("someFields", Schema.FieldType.STRING, Schema.FieldType.STRING)
            .build();

        for (List<Object> expectedResult: expectedList) {
            expectedRows.add(listToRow(expectedResult, expectedSchema));
        }

        // ============================================================
        // Convert into a PCollection<Row>

        PCollection<Row> input = pipeline
            .apply(Create.of(inputRows))
            .setCoder(RowCoder.of(inputSchema));

        // ============================================================

        // Create and apply the PTransform representing the query.
        // Register the UDFs used in the query by calling '.registerUdf()' with
        // either a class which implements BeamSqlUdf or with
        // an instance of the SerializableFunction;
        PCollection<Row> result =
            PCollectionTuple
                // This way we give a name to the input stream for use in the SQL
                .of("InputStream", input)
                // Apply the SQL with the UDFs we need.
                .apply("Execute SQL",
                    SqlTransform
                    // The SQL query that needs to be applied.
                    .query(
                        "SELECT" +
                        // The input
                        "    userAgent                                                    AS userAgent, " +
                        // Ask for a single field an extract it
                        "    ParseUserAgent(userAgent, 'DeviceClass')['DeviceClass']      AS deviceClass, " +
                        // Ask for a all fields and extract a single one
                        "    ParseUserAgent(userAgent)['AgentNameVersion']                AS agentNameVersion, " +
                        // Ask for a all fields and return the full map with all.
// Disable as the list of possible values is a nightmare to maintain the test for.
//                        "    ParseUserAgent(userAgent)                                    AS allFields, " +
                        // Ask for a some fields and return the full map with all.
                        "    ParseUserAgent(userAgent, 'DeviceClass', 'AgentNameVersion') AS someFields " +
                        "FROM InputStream")
                    // Register each of the custom functions that must be available
                    .registerUdf("ParseUserAgent",  ParseUserAgent.class)
                );

        // Just to see the output of the query while debugging
        result.apply(ParDo.of(new RowPrinter()));

        // Assert on the results.
        PAssert.that(result)
            .containsInAnyOrder(expectedRows);

        pipeline.run().waitUntilFinish();
    }

    public static class RowPrinter extends DoFn<Row, Void> {
        @SuppressWarnings("unused") // Called via the annotation
        @ProcessElement
        public void processElement(ProcessContext c) {
            final Row row = c.element();
            LOG.info("ROW: {}", row);
        }
    }

}
