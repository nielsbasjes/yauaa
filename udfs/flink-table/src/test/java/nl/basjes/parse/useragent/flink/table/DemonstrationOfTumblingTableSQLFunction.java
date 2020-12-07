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

package nl.basjes.parse.useragent.flink.table;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.apache.flink.api.common.typeinfo.Types.LONG;
import static org.apache.flink.api.common.typeinfo.Types.SQL_TIMESTAMP;
import static org.apache.flink.api.common.typeinfo.Types.STRING;
import static org.apache.flink.table.api.Expressions.$;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DemonstrationOfTumblingTableSQLFunction {

    private static final long BASETIME = 1546344000000L; // 2019-01-01 12:00 UTC
    private static final long SECOND   = 1000L;
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
                wait(200);
                if (minutes > 120) {
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
    @Disabled
    @Test
    public void runDemonstration() throws Exception {
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
            $("eventTime").rowtime(), $("useragent"), $("expectedDeviceClass"), $("expectedAgentNameVersionMajor"));

        // Register the function
        tableEnv.createTemporarySystemFunction("ParseUserAgent", new AnalyzeUseragentFunction("DeviceClass", "AgentNameVersionMajor"));

        int windowIntervalCount =  5;
        String windowIntervalScale =  "MINUTE";

        String sqlQuery = String.format(
            "SELECT" +
            "   TUMBLE_START(eventTime, INTERVAL '%d' %s) AS wStart," +
            "   deviceClass," +
            "   agentNameVersionMajor," +
            "   expectedDeviceClass," +
            "   expectedAgentNameVersionMajor," +
            "   Count('') " +
            "FROM ( "+
            "    SELECT " +
            "       eventTime, " +
            "       parsedUserAgent['DeviceClass'          ]  AS deviceClass," +
            "       parsedUserAgent['AgentNameVersionMajor']  AS agentNameVersionMajor," +
            "       expectedDeviceClass," +
            "       expectedAgentNameVersionMajor" +
            "    FROM ( "+
            "        SELECT " +
            "           eventTime, " +
            "           ParseUserAgent(useragent) AS parsedUserAgent," +
            "           expectedDeviceClass," +
            "           expectedAgentNameVersionMajor" +
            "        FROM AgentStream " +
            "    )" +
            ")" +
            "GROUP BY TUMBLE(eventTime, INTERVAL '%d' %s), " +
                "       deviceClass," +
                "       agentNameVersionMajor," +
                "       expectedDeviceClass," +
                "       expectedAgentNameVersionMajor",
            windowIntervalCount, windowIntervalScale,
            windowIntervalCount, windowIntervalScale
            );
        Table resultTable = tableEnv.sqlQuery(sqlQuery);

        TypeInformation<Row> tupleType = new RowTypeInfo(SQL_TIMESTAMP, STRING, STRING, STRING, STRING, LONG);
        DataStream<Row>      resultSet = tableEnv.toAppendStream(resultTable, tupleType);

        resultSet.print();

        resultSet.map((MapFunction<Row, String>) row -> {
            Object useragent                      = row.getField(0);
            Object deviceClass                    = row.getField(1);
            Object agentNameVersionMajor          = row.getField(2);
            Object expectedDeviceClass            = row.getField(3);
            Object expectedAgentNameVersionMajor  = row.getField(4);

            assertTrue(useragent                     instanceof String);
            assertTrue(deviceClass                   instanceof String);
            assertTrue(agentNameVersionMajor         instanceof String);
            assertTrue(expectedDeviceClass           instanceof String);
            assertTrue(expectedAgentNameVersionMajor instanceof String);

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
