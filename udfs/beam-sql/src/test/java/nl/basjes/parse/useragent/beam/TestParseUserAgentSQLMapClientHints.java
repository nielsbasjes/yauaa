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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Category(ValidatesRunner.class)
public class TestParseUserAgentSQLMapClientHints implements Serializable {

    private static final Logger LOG = LogManager.getLogger(TestParseUserAgentSQLMapClientHints.class);

    @Rule
    public final transient TestPipeline pipeline = TestPipeline.create();

    public static Row listToRow(List<Object> values, Schema schema) {
        Row.Builder appRowBuilder = Row.withSchema(schema);
        values.forEach(appRowBuilder::addValues);
        return appRowBuilder.build();
    }

    private static class TestCase {
        String useragent;
        String chPlatform;
        String chPlatformVersion;
        String expectedDeviceClass;
        String expectedAgentNameVersionMajor;
        String expectedOperatingSystemNameVersion;
    }

    @Test
    @Category(NeedsRunner.class)
    public void testClientHintSQLAllFields() { // NOSONAR java:S2699 Tests should include assertions: Uses PAssert
        runTestOnProvidedQuery(
            "SELECT userAgent," +
            "       parsedUseragentAllFields['DeviceClass']                   AS deviceClass," +
            "       parsedUseragentAllFields['AgentNameVersionMajor']         AS agentNameVersionMajor," +
            "       parsedUseragentAllFields['OperatingSystemNameVersion']    AS operatingSystemNameVersion " +
            "FROM ( " +
            "   SELECT userAgent," +
            "          ParseUserAgent(" +
            "               'User-Agent',                   userAgent,   " +
            "               'Sec-CH-UA-Platform',           chPlatform,  " +
            "               'Sec-CH-UA-Platform-Version',   chPlatformVersion" +
            "          ) AS parsedUseragentAllFields" +
            "   FROM   AgentStream " +
            ")"
        );
    }

    @Test
    @Category(NeedsRunner.class)
    public void testClientHintSQLSomeFields() { // NOSONAR java:S2699 Tests should include assertions: Uses PAssert
        runTestOnProvidedQuery(
            "SELECT userAgent," +
            "       parsedUseragentSomeFields['DeviceClass']                   AS deviceClass," +
            "       parsedUseragentSomeFields['AgentNameVersionMajor']         AS agentNameVersionMajor," +
            "       parsedUseragentSomeFields['OperatingSystemNameVersion']    AS operatingSystemNameVersion " +
            "FROM ( " +
            "   SELECT userAgent," +
            "          ParseUserAgent(" +
            "               'DeviceClass', " +
            "               'user-Agent',                   userAgent,   " +
            "               'AgentNameVersionMajor', " +
            "               'sec-CH-UA-Platform',           chPlatform,  " +
            "               'OperatingSystemNameVersion', " +
            "               'sec-CH-UA-Platform-Version',   chPlatformVersion" +
            "          ) AS parsedUseragentSomeFields" +
            "   FROM   AgentStream " +
            ")"
        );
    }

    @Test
    @Category(NeedsRunner.class)
    public void testClientHintSQLSomeFieldsJson() { // NOSONAR java:S2699 Tests should include assertions: Uses PAssert
        // ---------------
        // The expected test output
        List<List<Object>> expectedList = Collections.singletonList(
            Collections.singletonList(
                "{\"DeviceClass\":\"Desktop\",\"AgentNameVersionMajor\":\"Chrome 100\",\"OperatingSystemNameVersion\":\"Mac OS 12.3.1\"}"
            )
        );

        List<Row> expectedRows = new ArrayList<>(expectedList.size());
        Schema expectedSchema = Schema
            .builder()
            .addStringField("parsedUseragentSomeFieldsJson")
            .build();

        for(List<Object> expectedResult:expectedList) {
            expectedRows.add(listToRow(expectedResult, expectedSchema));
        }

        runTestOnProvidedQuery(
            "SELECT ParseUserAgentJson(" +
            "            'DeviceClass', " +
            "            'user-Agent',                   userAgent,   " +
            "            'AgentNameVersionMajor', " +
            "            'sec-CH-UA-Platform',           chPlatform,  " +
            "            'OperatingSystemNameVersion', " +
            "            'sec-CH-UA-Platform-Version',   chPlatformVersion" +
            "       ) AS parsedUseragentSomeFieldsJson " +
            "FROM   AgentStream",
            expectedRows
        );
    }

    @Test
    @Category(NeedsRunner.class)
    public void testClientHintSQLBadParameterList2() { // NOSONAR java:S2699 Tests should include assertions: Uses PAssert
        assertThrows(RuntimeException.class, () ->
            runTestOnProvidedQuery(
                "SELECT userAgent," +
                "       parsedUseragentAllFields['DeviceClass']                   AS deviceClass," +
                "       parsedUseragentAllFields['AgentNameVersionMajor']         AS agentNameVersionMajor," +
                "       parsedUseragentAllFields['OperatingSystemNameVersion']    AS operatingSystemNameVersion " +
                "FROM ( " +
                "   SELECT userAgent," +
                "          ParseUserAgent(" +
                "               userAgent,   " +
                "               'SomethingUnsupported'" +
                "          ) AS parsedUseragentAllFields" +
                "   FROM   AgentStream " +
                ")"
            )
        );
    }


    private TestCase testAgent = new TestCase();

    @Before
    public void initTestAgent() {
        testAgent.useragent                             = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.60 Safari/537.36";
        testAgent.chPlatform                            = "\"macOS\"";
        testAgent.chPlatformVersion                     = "\"12.3.1\"";
        testAgent.expectedDeviceClass                   = "Desktop";
        testAgent.expectedAgentNameVersionMajor         = "Chrome 100";
        testAgent.expectedOperatingSystemNameVersion    = "Mac OS 12.3.1";
    }

    private List<Row> expectedRows() { // NOSONAR java:S2699 Tests should include assertions: Uses PAssert
        // ---------------
        // The expected test output
        List<List<Object>> expectedList = Collections.singletonList(
            Arrays.asList(
                testAgent.useragent,
                testAgent.expectedDeviceClass,
                testAgent.expectedAgentNameVersionMajor,
                testAgent.expectedOperatingSystemNameVersion
            )
        );

        List<Row> expectedRows = new ArrayList<>(expectedList.size());
        Schema expectedSchema = Schema
            .builder()
            .addStringField("userAgent")
            .addStringField("deviceClass")
            .addStringField("agentNameVersionMajor")
            .addStringField("operatingSystemNameVersion")
            .build();

        for(List<Object> expectedResult:expectedList) {
            expectedRows.add(listToRow(expectedResult, expectedSchema));
        }
        return expectedRows;
    }

    private void runTestOnProvidedQuery(String query) { // NOSONAR java:S2699 Tests should include assertions: Uses PAssert
        runTestOnProvidedQuery(query, expectedRows());
    }

    private void runTestOnProvidedQuery(String query, List<Row> expectedRows) { // NOSONAR java:S2699 Tests should include assertions: Uses PAssert
        // ============================================================

        // ---------------
        // The base test input
        List<List<Object>> inputValues = Collections.singletonList(
            Arrays.asList(
                testAgent.useragent,
                testAgent.chPlatform,
                testAgent.chPlatformVersion
            )
        );

        List<Row> inputRows = new ArrayList<>(inputValues.size());
        Schema inputSchema = Schema
            .builder()
            .addStringField("userAgent")
            .addStringField("chPlatform")
            .addStringField("chPlatformVersion")
            .build();

        for (List<Object> inputValue: inputValues) {
            inputRows.add(listToRow(inputValue, inputSchema));
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
                .of("AgentStream", input)
                // Apply the SQL with the UDFs we need.
                .apply("Execute SQL",
                    SqlTransform
                    // The SQL query that needs to be applied.
                    .query(query)
                    // Register each of the custom functions that must be available
                    .registerUdf("ParseUserAgent",  ParseUserAgent.class)
                    .registerUdf("ParseUserAgentJson", ParseUserAgentJson.class)
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
