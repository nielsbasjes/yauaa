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

import org.apache.beam.sdk.coders.ListCoder;
import org.apache.beam.sdk.coders.RowCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
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
import java.util.Objects;

@Category(ValidatesRunner.class)
public class TestParseUserAgentSQL implements Serializable {

    private static final Logger LOG = LogManager.getLogger(TestParseUserAgentSQL.class);

    @Rule
    public final transient TestPipeline pipeline = TestPipeline.create();

    public static Row listToRow(List<String> strings, Schema schema) {
        Row.Builder appRowBuilder = Row.withSchema(schema);
        strings.forEach(appRowBuilder::addValues);
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
        String agent2 =
            "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/53.0.2785.124 Mobile Safari/537.36";

        // The base test input
        List<List<String>> useragents = Arrays.asList(
            Collections.singletonList(agent1),
            Collections.singletonList(agent2));

        List<List<String>> expectedList = Arrays.asList(
            Arrays.asList(
                agent1,

                "Desktop",

                "Chrome 48.0.2564.82",

                "{\"Useragent\":\"Mozilla\\/5.0 (X11; Linux x86_64) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/48.0.2564.82 Safari\\/537.36\"," +
                    "\"DeviceClass\":\"Desktop\"," +
                    "\"DeviceName\":\"Linux Desktop\"," +
                    "\"DeviceBrand\":\"Unknown\"," +
                    "\"DeviceCpu\":\"Intel x86_64\"," +
                    "\"DeviceCpuBits\":\"64\"," +
                    "\"DeviceFirmwareVersion\":\"??\"," +
                    "\"DeviceVersion\":\"??\"," +
                    "\"OperatingSystemClass\":\"Desktop\"," +
                    "\"OperatingSystemName\":\"Linux\"," +
                    "\"OperatingSystemVersion\":\"??\"," +
                    "\"OperatingSystemVersionMajor\":\"??\"," +
                    "\"OperatingSystemNameVersion\":\"Linux ??\"," +
                    "\"OperatingSystemNameVersionMajor\":\"Linux ??\"," +
                    "\"OperatingSystemVersionBuild\":\"??\"," +
                    "\"LayoutEngineClass\":\"Browser\"," +
                    "\"LayoutEngineName\":\"Blink\"," +
                    "\"LayoutEngineVersion\":\"48.0\"," +
                    "\"LayoutEngineVersionMajor\":\"48\"," +
                    "\"LayoutEngineNameVersion\":\"Blink 48.0\"," +
                    "\"LayoutEngineNameVersionMajor\":\"Blink 48\"," +
                    "\"LayoutEngineBuild\":\"Unknown\"," +
                    "\"AgentClass\":\"Browser\"," +
                    "\"AgentName\":\"Chrome\"," +
                    "\"AgentVersion\":\"48.0.2564.82\"," +
                    "\"AgentVersionMajor\":\"48\"," +
                    "\"AgentNameVersion\":\"Chrome 48.0.2564.82\"," +
                    "\"AgentNameVersionMajor\":\"Chrome 48\"," +

                    "\"AgentBuild\":\"Unknown\"," +
                    "\"AgentLanguage\":\"Unknown\"," +
                    "\"AgentLanguageCode\":\"Unknown\"," +
                    "\"AgentInformationEmail\":\"Unknown\"," +
                    "\"AgentInformationUrl\":\"Unknown\"," +
                    "\"AgentSecurity\":\"Unknown\"," +
                    "\"AgentUuid\":\"Unknown\"," +
                    "\"WebviewAppName\":\"Unknown\"," +
                    "\"WebviewAppVersion\":\"??\"," +
                    "\"WebviewAppVersionMajor\":\"??\"," +
                    "\"WebviewAppNameVersion\":\"Unknown ??\"," +
                    "\"WebviewAppNameVersionMajor\":\"Unknown ??\"," +
                    "\"FacebookCarrier\":\"Unknown\"," +
                    "\"FacebookDeviceClass\":\"Unknown\"," +
                    "\"FacebookDeviceName\":\"Unknown\"," +
                    "\"FacebookDeviceVersion\":\"??\"," +
                    "\"FacebookFBOP\":\"Unknown\"," +
                    "\"FacebookFBSS\":\"Unknown\"," +
                    "\"FacebookOperatingSystemName\":\"Unknown\"," +
                    "\"FacebookOperatingSystemVersion\":\"??\"," +
                    "\"RemarkablePattern\":\"Nothing remarkable found\"," +
                    "\"HackerAttackVector\":\"Unknown\"," +
                    "\"HackerToolkit\":\"Unknown\"," +
                    "\"KoboAffiliate\":\"Unknown\"," +
                    "\"KoboPlatformId\":\"Unknown\"," +
                    "\"IECompatibilityVersion\":\"??\"," +
                    "\"IECompatibilityVersionMajor\":\"??\"," +
                    "\"IECompatibilityNameVersion\":\"Unknown ??\"," +
                    "\"IECompatibilityNameVersionMajor\":\"Unknown ??\"," +
                    "\"Carrier\":\"Unknown\"," +
                    "\"FederatedSocialNetwork\":\"Unknown\"," +
                    "\"GSAInstallationID\":\"Unknown\"," +
                    "\"NetworkType\":\"Unknown\","+
                    "\"__SyntaxError__\":\"false\"}",

                "{\"OperatingSystemClass\":\"Desktop\"}",

                "{\"OperatingSystemClass\":\"Desktop\"," +
                    "\"OperatingSystemName\":\"Linux\"}",

                "{\"OperatingSystemClass\":\"Desktop\"," +
                    "\"OperatingSystemName\":\"Linux\"," +
                    "\"OperatingSystemVersion\":\"??\"}",

                "{\"OperatingSystemClass\":\"Desktop\"," +
                    "\"OperatingSystemName\":\"Linux\"," +
                    "\"OperatingSystemVersion\":\"??\"," +
                    "\"OperatingSystemVersionMajor\":\"??\"}",

                "{\"OperatingSystemClass\":\"Desktop\"," +
                    "\"OperatingSystemName\":\"Linux\"," +
                    "\"OperatingSystemVersion\":\"??\"," +
                    "\"OperatingSystemVersionMajor\":\"??\"," +
                    "\"OperatingSystemNameVersion\":\"Linux ??\"}",

                "{\"OperatingSystemClass\":\"Desktop\"," +
                    "\"OperatingSystemName\":\"Linux\"," +
                    "\"OperatingSystemVersion\":\"??\"," +
                    "\"OperatingSystemVersionMajor\":\"??\"," +
                    "\"OperatingSystemNameVersion\":\"Linux ??\"," +
                    "\"OperatingSystemNameVersionMajor\":\"Linux ??\"}",

                "{\"OperatingSystemClass\":\"Desktop\"," +
                    "\"OperatingSystemName\":\"Linux\"," +
                    "\"OperatingSystemVersion\":\"??\"," +
                    "\"OperatingSystemVersionMajor\":\"??\"," +
                    "\"OperatingSystemNameVersion\":\"Linux ??\"," +
                    "\"OperatingSystemNameVersionMajor\":\"Linux ??\"," +
                    "\"AgentClass\":\"Browser\"}",

                "{\"OperatingSystemClass\":\"Desktop\"," +
                    "\"OperatingSystemName\":\"Linux\"," +
                    "\"OperatingSystemVersion\":\"??\"," +
                    "\"OperatingSystemVersionMajor\":\"??\"," +
                    "\"OperatingSystemNameVersion\":\"Linux ??\"," +
                    "\"OperatingSystemNameVersionMajor\":\"Linux ??\"," +
                    "\"AgentClass\":\"Browser\"," +
                    "\"AgentName\":\"Chrome\"}",

                "{\"OperatingSystemClass\":\"Desktop\"," +
                    "\"OperatingSystemName\":\"Linux\"," +
                    "\"OperatingSystemVersion\":\"??\"," +
                    "\"OperatingSystemVersionMajor\":\"??\"," +
                    "\"OperatingSystemNameVersion\":\"Linux ??\"," +
                    "\"OperatingSystemNameVersionMajor\":\"Linux ??\"," +
                    "\"AgentClass\":\"Browser\"," +
                    "\"AgentName\":\"Chrome\"," +
                    "\"AgentVersion\":\"48.0.2564.82\"}",

                "{\"OperatingSystemClass\":\"Desktop\"," +
                    "\"OperatingSystemName\":\"Linux\"," +
                    "\"OperatingSystemVersion\":\"??\"," +
                    "\"OperatingSystemVersionMajor\":\"??\"," +
                    "\"OperatingSystemNameVersion\":\"Linux ??\"," +
                    "\"OperatingSystemNameVersionMajor\":\"Linux ??\"," +
                    "\"AgentClass\":\"Browser\"," +
                    "\"AgentName\":\"Chrome\"," +
                    "\"AgentVersion\":\"48.0.2564.82\"," +
                    "\"AgentVersionMajor\":\"48\"}"
                ),

            Arrays.asList(
                agent2,

               "Phone",

               "Chrome 53.0.2785.124",

               "{\"Useragent\":\"Mozilla\\/5.0 (Linux; Android 7.0; Nexus 6 Build\\/NBD90Z) AppleWebKit\\/537.36 (KHTML, like Gecko) Chrome\\/53.0.2785.124 Mobile Safari\\/537.36\"," +
                   "\"DeviceClass\":\"Phone\"," +
                   "\"DeviceName\":\"Google Nexus 6\"," +
                   "\"DeviceBrand\":\"Google\"," +
                   "\"DeviceCpu\":\"Unknown\"," +
                   "\"DeviceCpuBits\":\"Unknown\"," +
                   "\"DeviceFirmwareVersion\":\"??\"," +
                   "\"DeviceVersion\":\"??\"," +
                   "\"OperatingSystemClass\":\"Mobile\"," +
                   "\"OperatingSystemName\":\"Android\"," +
                   "\"OperatingSystemVersion\":\"7.0\"," +
                   "\"OperatingSystemVersionMajor\":\"7\"," +
                   "\"OperatingSystemNameVersion\":\"Android 7.0\"," +
                   "\"OperatingSystemNameVersionMajor\":\"Android 7\"," +
                   "\"OperatingSystemVersionBuild\":\"NBD90Z\"," +
                   "\"LayoutEngineClass\":\"Browser\"," +
                   "\"LayoutEngineName\":\"Blink\"," +
                   "\"LayoutEngineVersion\":\"53.0\"," +
                   "\"LayoutEngineVersionMajor\":\"53\"," +
                   "\"LayoutEngineNameVersion\":\"Blink 53.0\"," +
                   "\"LayoutEngineNameVersionMajor\":\"Blink 53\"," +
                   "\"LayoutEngineBuild\":\"Unknown\"," +
                   "\"AgentClass\":\"Browser\"," +
                   "\"AgentName\":\"Chrome\"," +
                   "\"AgentVersion\":\"53.0.2785.124\"," +
                   "\"AgentVersionMajor\":\"53\"," +
                   "\"AgentNameVersion\":\"Chrome 53.0.2785.124\"," +
                   "\"AgentNameVersionMajor\":\"Chrome 53\"," +
                   "\"AgentBuild\":\"Unknown\"," +
                   "\"AgentLanguage\":\"Unknown\"," +
                   "\"AgentLanguageCode\":\"Unknown\"," +
                   "\"AgentInformationEmail\":\"Unknown\"," +
                   "\"AgentInformationUrl\":\"Unknown\"," +
                   "\"AgentSecurity\":\"Unknown\"," +
                   "\"AgentUuid\":\"Unknown\"," +
                   "\"WebviewAppName\":\"Unknown\"," +
                   "\"WebviewAppVersion\":\"??\"," +
                   "\"WebviewAppVersionMajor\":\"??\"," +
                   "\"WebviewAppNameVersion\":\"Unknown ??\"," +
                   "\"WebviewAppNameVersionMajor\":\"Unknown ??\"," +
                   "\"FacebookCarrier\":\"Unknown\"," +
                   "\"FacebookDeviceClass\":\"Unknown\"," +
                   "\"FacebookDeviceName\":\"Unknown\"," +
                   "\"FacebookDeviceVersion\":\"??\"," +
                   "\"FacebookFBOP\":\"Unknown\"," +
                   "\"FacebookFBSS\":\"Unknown\"," +
                   "\"FacebookOperatingSystemName\":\"Unknown\"," +
                   "\"FacebookOperatingSystemVersion\":\"??\"," +
                   "\"RemarkablePattern\":\"Nothing remarkable found\"," +
                   "\"HackerAttackVector\":\"Unknown\"," +
                   "\"HackerToolkit\":\"Unknown\"," +
                   "\"KoboAffiliate\":\"Unknown\"," +
                   "\"KoboPlatformId\":\"Unknown\"," +
                   "\"IECompatibilityVersion\":\"??\"," +
                   "\"IECompatibilityVersionMajor\":\"??\"," +
                   "\"IECompatibilityNameVersion\":\"Unknown ??\"," +
                   "\"IECompatibilityNameVersionMajor\":\"Unknown ??\"," +
                   "\"Carrier\":\"Unknown\"," +
                   "\"FederatedSocialNetwork\":\"Unknown\"," +
                   "\"GSAInstallationID\":\"Unknown\"," +
                   "\"NetworkType\":\"Unknown\"," +
                   "\"__SyntaxError__\":\"false\"}",

               "{\"OperatingSystemClass\":\"Mobile\"}",

               "{\"OperatingSystemClass\":\"Mobile\"," +
                   "\"OperatingSystemName\":\"Android\"}",

               "{\"OperatingSystemClass\":\"Mobile\"," +
                   "\"OperatingSystemName\":\"Android\"," +
                   "\"OperatingSystemVersion\":\"7.0\"}",

               "{\"OperatingSystemClass\":\"Mobile\"," +
                   "\"OperatingSystemName\":\"Android\"," +
                   "\"OperatingSystemVersion\":\"7.0\"," +
                   "\"OperatingSystemVersionMajor\":\"7\"}",

               "{\"OperatingSystemClass\":\"Mobile\"," +
                   "\"OperatingSystemName\":\"Android\"," +
                   "\"OperatingSystemVersion\":\"7.0\"," +
                   "\"OperatingSystemVersionMajor\":\"7\"," +
                   "\"OperatingSystemNameVersion\":\"Android 7.0\"}",

               "{\"OperatingSystemClass\":\"Mobile\"," +
                   "\"OperatingSystemName\":\"Android\"," +
                   "\"OperatingSystemVersion\":\"7.0\"," +
                   "\"OperatingSystemVersionMajor\":\"7\"," +
                   "\"OperatingSystemNameVersion\":\"Android 7.0\"," +
                   "\"OperatingSystemNameVersionMajor\":\"Android 7\"}",

               "{\"OperatingSystemClass\":\"Mobile\"," +
                   "\"OperatingSystemName\":\"Android\"," +
                   "\"OperatingSystemVersion\":\"7.0\"," +
                   "\"OperatingSystemVersionMajor\":\"7\"," +
                   "\"OperatingSystemNameVersion\":\"Android 7.0\"," +
                   "\"OperatingSystemNameVersionMajor\":\"Android 7\"," +
                   "\"AgentClass\":\"Browser\"}",

               "{\"OperatingSystemClass\":\"Mobile\"," +
                   "\"OperatingSystemName\":\"Android\"," +
                   "\"OperatingSystemVersion\":\"7.0\"," +
                   "\"OperatingSystemVersionMajor\":\"7\"," +
                   "\"OperatingSystemNameVersion\":\"Android 7.0\"," +
                   "\"OperatingSystemNameVersionMajor\":\"Android 7\"," +
                   "\"AgentClass\":\"Browser\"," +
                   "\"AgentName\":\"Chrome\"}",

               "{\"OperatingSystemClass\":\"Mobile\"," +
                   "\"OperatingSystemName\":\"Android\"," +
                   "\"OperatingSystemVersion\":\"7.0\"," +
                   "\"OperatingSystemVersionMajor\":\"7\"," +
                   "\"OperatingSystemNameVersion\":\"Android 7.0\"," +
                   "\"OperatingSystemNameVersionMajor\":\"Android 7\"," +
                   "\"AgentClass\":\"Browser\"," +
                   "\"AgentName\":\"Chrome\"," +
                   "\"AgentVersion\":\"53.0.2785.124\"}",

               "{\"OperatingSystemClass\":\"Mobile\"," +
                   "\"OperatingSystemName\":\"Android\"," +
                   "\"OperatingSystemVersion\":\"7.0\"," +
                   "\"OperatingSystemVersionMajor\":\"7\"," +
                   "\"OperatingSystemNameVersion\":\"Android 7.0\"," +
                   "\"OperatingSystemNameVersionMajor\":\"Android 7\"," +
                   "\"AgentClass\":\"Browser\"," +
                   "\"AgentName\":\"Chrome\"," +
                   "\"AgentVersion\":\"53.0.2785.124\"," +
                   "\"AgentVersionMajor\":\"53\"}"
            )
        );

        List<Row> expectedRows = new ArrayList<>(expectedList.size());
        Schema expectedSchema = Schema
            .builder()
            .addStringField("userAgent")
            .addStringField("deviceClass")
            .addStringField("agentNameVersion")
            .addStringField("parsedUserAgentJson0")
            .addStringField("parsedUserAgentJson1")
            .addStringField("parsedUserAgentJson2")
            .addStringField("parsedUserAgentJson3")
            .addStringField("parsedUserAgentJson4")
            .addStringField("parsedUserAgentJson5")
            .addStringField("parsedUserAgentJson6")
            .addStringField("parsedUserAgentJson7")
            .addStringField("parsedUserAgentJson8")
            .addStringField("parsedUserAgentJson9")
            .addStringField("parsedUserAgentJson10")
            .build();

        for (List<String> expectedStrings: expectedList) {
            expectedRows.add(listToRow(expectedStrings, expectedSchema));
        }

        // ============================================================
        // Convert into a PCollection<Row>
        Schema inputSchema = Schema
            .builder()
            .addStringField("userAgent")
            .build();

        PCollection<Row> input = pipeline
            .apply(Create.of(useragents))
            .setCoder(ListCoder.of(StringUtf8Coder.of()))
            .apply(ParDo.of(new DoFn<List<String>, Row>() {
                @SuppressWarnings("unused") // Called via the annotation
                @ProcessElement
                public void processElement(ProcessContext c) {
                    c.output(listToRow(Objects.requireNonNull(c.element()), inputSchema));
                }
            }))
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
                        "    userAgent                                            AS userAgent, " +
                        "    ParseUserAgentField(userAgent, 'DeviceClass')        AS deviceClass, " +
                        "    ParseUserAgentField(userAgent, 'AgentNameVersion')   AS agentNameVersion, " +

                        "    ParseUserAgentJson(userAgent" +
                        "                      ) AS parsedUserAgentJson0, " +

                        "    ParseUserAgentJson(userAgent," +
                        "                       'OperatingSystemClass'" +
                        "                      ) AS parsedUserAgentJson1, " +

                        "    ParseUserAgentJson(userAgent," +
                        "                       'OperatingSystemClass'," +
                        "                       'OperatingSystemName'" +
                        "                      ) AS parsedUserAgentJson2, " +

                        "    ParseUserAgentJson(userAgent," +
                        "                       'OperatingSystemClass'," +
                        "                       'OperatingSystemName'," +
                        "                       'OperatingSystemVersion'" +
                        "                      ) AS parsedUserAgentJson3, " +

                        "    ParseUserAgentJson(userAgent," +
                        "                       'OperatingSystemClass'," +
                        "                       'OperatingSystemName'," +
                        "                       'OperatingSystemVersion'," +
                        "                       'OperatingSystemVersionMajor'" +
                        "                      ) AS parsedUserAgentJson4, " +

                        "    ParseUserAgentJson(userAgent," +
                        "                       'OperatingSystemClass'," +
                        "                       'OperatingSystemName'," +
                        "                       'OperatingSystemVersion'," +
                        "                       'OperatingSystemVersionMajor'," +
                        "                       'OperatingSystemNameVersion'" +
                        "                      ) AS parsedUserAgentJson5, " +

                        "    ParseUserAgentJson(userAgent," +
                        "                       'OperatingSystemClass'," +
                        "                       'OperatingSystemName'," +
                        "                       'OperatingSystemVersion'," +
                        "                       'OperatingSystemVersionMajor'," +
                        "                       'OperatingSystemNameVersion'," +
                        "                       'OperatingSystemNameVersionMajor'" +
                        "                      ) AS parsedUserAgentJson6, " +

                        "    ParseUserAgentJson(userAgent," +
                        "                       'OperatingSystemClass'," +
                        "                       'OperatingSystemName'," +
                        "                       'OperatingSystemVersion'," +
                        "                       'OperatingSystemVersionMajor'," +
                        "                       'OperatingSystemNameVersion'," +
                        "                       'OperatingSystemNameVersionMajor'," +
                        "                       'AgentClass'" +
                        "                      ) AS parsedUserAgentJson7, " +

                        "    ParseUserAgentJson(userAgent," +
                        "                       'OperatingSystemClass'," +
                        "                       'OperatingSystemName'," +
                        "                       'OperatingSystemVersion'," +
                        "                       'OperatingSystemVersionMajor'," +
                        "                       'OperatingSystemNameVersion'," +
                        "                       'OperatingSystemNameVersionMajor'," +
                        "                       'AgentClass'," +
                        "                       'AgentName'" +
                        "                      ) AS parsedUserAgentJson8, " +

                        "    ParseUserAgentJson(userAgent," +
                        "                       'OperatingSystemClass'," +
                        "                       'OperatingSystemName'," +
                        "                       'OperatingSystemVersion'," +
                        "                       'OperatingSystemVersionMajor'," +
                        "                       'OperatingSystemNameVersion'," +
                        "                       'OperatingSystemNameVersionMajor'," +
                        "                       'AgentClass'," +
                        "                       'AgentName'," +
                        "                       'AgentVersion'" +
                        "                      ) AS parsedUserAgentJson9, " +

                        "    ParseUserAgentJson(userAgent," +
                        "                       'OperatingSystemClass'," +
                        "                       'OperatingSystemName'," +
                        "                       'OperatingSystemVersion'," +
                        "                       'OperatingSystemVersionMajor'," +
                        "                       'OperatingSystemNameVersion'," +
                        "                       'OperatingSystemNameVersionMajor'," +
                        "                       'AgentClass'," +
                        "                       'AgentName'," +
                        "                       'AgentVersion'," +
                        "                       'AgentVersionMajor'" +
                        "                      ) AS parsedUserAgentJson10 " +

                        "FROM InputStream")
                    // Register each of the custom functions that must be available
                    .registerUdf("ParseUserAgentJson",  ParseUserAgentJson.class)
                    .registerUdf("ParseUserAgentField", ParseUserAgentField.class)
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
