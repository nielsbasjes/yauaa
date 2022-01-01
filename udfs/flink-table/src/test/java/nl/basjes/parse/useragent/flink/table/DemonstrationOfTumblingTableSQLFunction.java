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

package nl.basjes.parse.useragent.flink.table;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.apache.flink.table.api.Expressions.$;
import static org.apache.flink.table.api.Expressions.lit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DemonstrationOfTumblingTableSQLFunction {

    private static final long BASETIME = Instant.from(ISO_OFFSET_DATE_TIME.parse("2021-01-01T12:00:00+00:00")).getEpochSecond();
    private static final long SECOND   = 1L;
    private static final long MINUTE   = 60 * SECOND;

    // ============================================================================================================

    public static class UAStreamSource implements SourceFunction<Tuple4<Long, String, String, String>> {

        volatile boolean running = true;

        @Override
        public void run(SourceContext<Tuple4<Long, String, String, String>> sourceContext) throws Exception {
            int minutes = 0;
            while (running) {
                sourceContext.collect(new Tuple4<>(
                    BASETIME + (minutes * MINUTE) + (5 * SECOND),
                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36",
                    "Desktop",
                    "Chrome 70"));

                sourceContext.collect(new Tuple4<>(
                    BASETIME + (minutes * MINUTE) + (15 * SECOND),
                    "Mozilla/5.0 (Linux; Android 7.1.1; Nexus 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.57 Mobile Safari/537.36",
                    "Phone",
                    "Chrome 70"));

                sourceContext.collect(new Tuple4<>(
                    BASETIME + (minutes * MINUTE) + (25 * SECOND),
                    "Mozilla/5.0 (Linux; U; Android 4.4.2; PE-TL20 Build/HuaweiPE-TL20) AppleWebKit/533.1 (KHTML, like Gecko)Version/4.0 " +
                        "MQQBrowser/5.4 TBS/025440 Mobile Safari/533.1 MicroMessenger/6.2.5.53_r2565f18.621 NetType/WIFI Language/zh_CN",
                    "Phone",
                    "WeChat 6"));

                minutes++;
                Thread.sleep(20);
                if (minutes > 30) {
                    running = false;
                }
            }
        }

        @Override
        public void cancel() {
            running = false;
        }
    }

    // ============================================================================================================

    // This "test" takes too long to run in the build.
//    @Disabled
    @Test
    void runDemonstration() throws Exception {
        // The base execution environment
        StreamExecutionEnvironment senv = StreamExecutionEnvironment.getExecutionEnvironment();
        senv.getConfig().setAutoWatermarkInterval(1000);

        // The table environment
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(senv);

        // Setup the watermark system
        WatermarkStrategy<Tuple4<Long, String, String, String>> watermarkStrategy = WatermarkStrategy
            .forBoundedOutOfOrderness(Duration.of(1, ChronoUnit.MINUTES));

        watermarkStrategy
            .withTimestampAssigner((SerializableTimestampAssigner<Tuple4<Long, String, String, String>>) (element, recordTimestamp) -> element.f0);

        // The demo input stream
        DataStream<Tuple4<Long, String, String, String>> inputStream = senv
            .addSource(new UAStreamSource())
            .assignTimestampsAndWatermarks(watermarkStrategy);

        // Give the stream a Table Name and name the fields
        tableEnv.createTemporaryView("AgentStream", inputStream,
            Schema
                .newBuilder()
                .columnByExpression("eventTime", "TO_TIMESTAMP(FROM_UNIXTIME(f0))")
                .columnByExpression("useragent", "f1")
                .columnByExpression("expectedDeviceClass", "f2")
                .columnByExpression("expectedAgentNameVersionMajor", "f3")
                .watermark("eventTime", $("eventTime").minus(lit(5).seconds()))
                .build());

        // Register the function
        tableEnv.createTemporarySystemFunction("ParseUserAgent", new AnalyzeUseragentFunction("DeviceClass", "AgentNameVersionMajor"));

        int windowIntervalCount =  5;
        String windowIntervalScale =  "MINUTE";

        String sqlQuery = String.format(
            "SELECT" +
            "   TUMBLE_START(eventTime, INTERVAL '%d' %s) AS wStart," +
            "   useragent, " +
            "   deviceClass," +
            "   agentNameVersionMajor," +
            "   expectedDeviceClass," +
            "   expectedAgentNameVersionMajor," +
            "   Count('') " +
            "FROM ( "+
            "    SELECT " +
            "       eventTime, " +
            "       useragent, " +
            "       parsedUserAgent['DeviceClass'          ]  AS deviceClass," +
            "       parsedUserAgent['AgentNameVersionMajor']  AS agentNameVersionMajor," +
            "       expectedDeviceClass," +
            "       expectedAgentNameVersionMajor" +
            "    FROM ( "+
            "        SELECT " +
            "           eventTime, " +
            "           useragent, " +
            "           ParseUserAgent(useragent) AS parsedUserAgent," +
            "           expectedDeviceClass," +
            "           expectedAgentNameVersionMajor" +
            "        FROM AgentStream " +
            "    )" +
            ")" +
            "GROUP BY TUMBLE(eventTime, INTERVAL '%d' %s), " +
            "       useragent, " +
            "       deviceClass," +
            "       agentNameVersionMajor," +
            "       expectedDeviceClass," +
            "       expectedAgentNameVersionMajor",
            windowIntervalCount, windowIntervalScale,
            windowIntervalCount, windowIntervalScale
            );
        Table resultTable = tableEnv.sqlQuery(sqlQuery);

//        TypeInformation<Row> tupleType = new RowTypeInfo(SQL_TIMESTAMP, STRING, STRING, STRING, STRING, STRING, LONG);
//        DataStream<Row>      resultSet = tableEnv.toAppendStream(resultTable, tupleType);

        DataStream<Row>      resultSet = tableEnv.toDataStream(resultTable);
        resultSet.print();

        resultSet.map((MapFunction<Row, String>) row -> {
            Object wStart                         = row.getField(0);
            Object useragent                      = row.getField(1);
            Object deviceClass                    = row.getField(2);
            Object agentNameVersionMajor          = row.getField(3);
            Object expectedDeviceClass            = row.getField(4);
            Object expectedAgentNameVersionMajor  = row.getField(5);
            Object count                          = row.getField(6);

            assertTrue(wStart                        instanceof LocalDateTime);
            assertTrue(useragent                     instanceof String);
            assertTrue(deviceClass                   instanceof String);
            assertTrue(agentNameVersionMajor         instanceof String);
            assertTrue(expectedDeviceClass           instanceof String);
            assertTrue(expectedAgentNameVersionMajor instanceof String);
            assertTrue(count                         instanceof Long);

            assertEquals(
                expectedDeviceClass,
                deviceClass,
                "Wrong DeviceClass: " + useragent);

            assertEquals(
                expectedAgentNameVersionMajor,
                agentNameVersionMajor,
                "Wrong AgentNameVersionMajor: " + useragent);

            return useragent.toString();
        });

        senv.execute();
    }

}
