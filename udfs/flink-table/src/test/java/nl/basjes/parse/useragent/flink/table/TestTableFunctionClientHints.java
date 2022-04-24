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

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple6;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestTableFunctionClientHints {

    public static DataStreamSource<Tuple6<String, String, String, String, String, String>> getTestAgentStream(StreamExecutionEnvironment env) {
        // Useragent, Expected DeviceClass, Expected AgentNameVersionMajor
        List<Tuple6<String, String, String, String, String, String>> data = new ArrayList<>();

        data.add(new Tuple6<>(
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36",
            null,
            null,
            "Desktop",
            "Chrome 70",
            "Linux ??"));

        data.add(new Tuple6<>(
            "Mozilla/5.0 (Linux; Android 7.1.1; Nexus 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.57 Mobile Safari/537.36",
            null,
            null,
            "Phone",
            "Chrome 70",
            "Android 7.1.1"));

        data.add(new Tuple6<>(
            "Mozilla/5.0 (Linux; U; Android 4.4.2; PE-TL20 Build/HuaweiPE-TL20) AppleWebKit/533.1 (KHTML, like Gecko)Version/4.0 " +
                "MQQBrowser/5.4 TBS/025440 Mobile Safari/533.1 MicroMessenger/6.2.5.53_r2565f18.621 NetType/WIFI Language/zh_CN",
            null,
            null,
            "Phone",
            "WeChat 6",
            "Android 4.4.2"));

        data.add(new Tuple6<>(
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.60 Safari/537.36",
            "\"macOS\"",
            "\"12.3.1\"",
            "Desktop",
            "Chrome 100",
            "Mac OS 12.3.1"));

        return env.fromCollection(data, TypeInformation.of(new TypeHint<Tuple6<String, String, String, String, String, String>>(){}));
    }

    public static Schema getTestAgentStreamSchema() {
        return Schema
            .newBuilder()
            .columnByExpression("useragent", "f0")
            .columnByExpression("chPlatform", "f1")
            .columnByExpression("chPlatformVersion", "f2")
            .columnByExpression("expectedDeviceClass", "f3")
            .columnByExpression("expectedAgentNameVersionMajor", "f4")
            .columnByExpression("expectedOperatingSystemNameVersion", "f5")
            .build();
    }

    @Test
    void testMapFunctionExtractInSQLSubSelect() throws Exception {
        // The base execution environment
        StreamExecutionEnvironment   senv        = StreamExecutionEnvironment.getExecutionEnvironment();

        // The table environment
        StreamTableEnvironment       tableEnv    = StreamTableEnvironment.create(senv);

        // The demo input stream
        DataStream<Tuple6<String, String, String, String, String, String>> inputStream = getTestAgentStream(senv);

        // Give the stream a Table Name and name the fields
        tableEnv.createTemporaryView("AgentStream", inputStream, getTestAgentStreamSchema());

        // Register the function
        tableEnv.createTemporarySystemFunction("ParseUserAgent", new AnalyzeUseragentFunction("DeviceClass", "AgentNameVersionMajor", "OperatingSystemNameVersion"));

        // Doing it this way the function is called once and then the results are picked from the map that was returned.
        String sqlQuery =
            "SELECT useragent,"+
            "       parsedUseragent['DeviceClass']                   AS deviceClass," +
            "       parsedUseragent['AgentNameVersionMajor']         AS agentNameVersionMajor," +
            "       parsedUseragent['OperatingSystemNameVersion']    AS operatingSystemNameVersion," +
            "       expectedDeviceClass," +
            "       expectedAgentNameVersionMajor," +
            "       expectedOperatingSystemNameVersion " +
            "FROM ( " +
            "   SELECT useragent," +
            "          ParseUserAgent(" +
            "               'User-Agent',                   useragent,   " +
            "               'Sec-CH-UA-Platform',           chPlatform,  " +
            "               'Sec-CH-UA-Platform-Version',   chPlatformVersion" +
            "          ) AS parsedUseragent," +
            "          expectedDeviceClass," +
            "          expectedAgentNameVersionMajor," +
            "          expectedOperatingSystemNameVersion " +
            "   FROM   AgentStream " +
            ")";

        Table  resultTable   = tableEnv.sqlQuery(sqlQuery);

        DataStream<Row> resultSet = tableEnv.toDataStream(resultTable);

        resultSet.map((MapFunction<Row, String>) row -> {
            Object useragent                           = row.getField(0);
            Object deviceClass                         = row.getField(1);
            Object agentNameVersionMajor               = row.getField(2);
            Object operatingSystemNameVersion          = row.getField(3);
            Object expectedDeviceClass                 = row.getField(4);
            Object expectedAgentNameVersionMajor       = row.getField(5);
            Object expectedOperatingSystemNameVersion  = row.getField(6);

            assertTrue(useragent                          instanceof String);
            assertTrue(deviceClass                        instanceof String);
            assertTrue(agentNameVersionMajor              instanceof String);
            assertTrue(expectedDeviceClass                instanceof String);
            assertTrue(expectedAgentNameVersionMajor      instanceof String);
            assertTrue(expectedOperatingSystemNameVersion instanceof String);

            System.err.println("----- Checking: " + useragent);
            assertEquals(expectedDeviceClass,                deviceClass,                "Wrong DeviceClass: "           + useragent);
            assertEquals(expectedAgentNameVersionMajor,      agentNameVersionMajor,      "Wrong AgentNameVersionMajor: " + useragent);
            assertEquals(expectedOperatingSystemNameVersion, operatingSystemNameVersion, "Wrong OperatingSystemNameVersion: " + useragent);
            return useragent.toString();
        });

        senv.execute();
    }

    @Test
    void testMapFunctionReturnMap() throws Exception {
        // The base execution environment
        StreamExecutionEnvironment   senv        = StreamExecutionEnvironment.getExecutionEnvironment();

        // The table environment
        StreamTableEnvironment       tableEnv    = StreamTableEnvironment.create(senv);

        // The demo input stream
        DataStream<Tuple6<String, String, String, String, String, String>> inputStream = getTestAgentStream(senv);

        // Give the stream a Table Name and name the fields
        tableEnv.createTemporaryView("AgentStream", inputStream, getTestAgentStreamSchema());

        // Register the function
        tableEnv.createTemporarySystemFunction("ParseUserAgent", new AnalyzeUseragentFunction("DeviceClass", "AgentNameVersionMajor", "OperatingSystemNameVersion"));

        // Here the query returns the entire map as one thing
        String sqlQuery =
            "SELECT useragent," +
            "       ParseUserAgent(" +
            "            'User-Agent',                   useragent,   " +
            "            'Sec-CH-UA-Platform',           chPlatform,  " +
            "            'Sec-CH-UA-Platform-Version',   chPlatformVersion" +
            "       ) AS parsedUseragent," +
            "       expectedDeviceClass," +
            "       expectedAgentNameVersionMajor," +
            "       expectedOperatingSystemNameVersion " +
            "FROM   AgentStream ";

        Table  resultTable   = tableEnv.sqlQuery(sqlQuery);

        DataStream<Row> resultSet = tableEnv.toDataStream(resultTable);

        resultSet.map((MapFunction<Row, String>) row -> {
            Object useragent                     = row.getField(0);
            Object parsedUseragent               = row.getField(1);
            Object expectedDeviceClass           = row.getField(2);
            Object expectedAgentNameVersionMajor = row.getField(3);
            Object expectedOperatingSystemNameVersion = row.getField(4);

            assertTrue(useragent                     instanceof String);
            assertTrue(parsedUseragent               instanceof Map<?, ?>);
            assertTrue(expectedDeviceClass           instanceof String);
            assertTrue(expectedAgentNameVersionMajor instanceof String);
            assertTrue(expectedOperatingSystemNameVersion instanceof String);

            assertEquals(
                expectedDeviceClass,
                ((Map<?, ?>)parsedUseragent).get("DeviceClass"),
                "Wrong DeviceClass: "           + useragent);

            assertEquals(
                expectedAgentNameVersionMajor,
                ((Map<?, ?>)parsedUseragent).get("AgentNameVersionMajor"),
                "Wrong AgentNameVersionMajor: " + useragent);

            assertEquals(
                expectedOperatingSystemNameVersion,
                ((Map<?, ?>)parsedUseragent).get("OperatingSystemNameVersion"),
                "Wrong OperatingSystemNameVersion: " + useragent);

            System.err.println("----- Checking: " + useragent);
            return useragent.toString();
        });

        senv.execute();
    }

    private void verifyFunction(AnalyzeUseragentFunction function) {
        verifyFunction(function, results -> results.size() == 3);
    }

    private void verifyFunction(AnalyzeUseragentFunction function, Function<Map<String, String>, Boolean> checkSizeFunction){
        function.open(null);
        final Map<String, String> result = function.eval(
            "User-Agent",                   "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.60 Safari/537.36",
            "Sec-CH-UA-Platform",           "\"macOS\"",
            "Sec-CH-UA-Platform-Version",   "\"12.3.1\""
        );
        assertTrue(checkSizeFunction.apply(result));
        assertEquals("Desktop",             result.get("DeviceClass"));
        assertEquals("Chrome 100",          result.get("AgentNameVersionMajor"));
        assertEquals("Mac OS 12.3.1",       result.get("OperatingSystemNameVersion"));
    }

    @Test
    void testMapFunctionList() {
        verifyFunction(new AnalyzeUseragentFunction(Arrays.asList("DeviceClass", "AgentNameVersionMajor", "OperatingSystemNameVersion")));
    }

    @Test
    void testMapFunctionListNoCache() {
        verifyFunction(new AnalyzeUseragentFunction(0, Arrays.asList("DeviceClass", "AgentNameVersionMajor", "OperatingSystemNameVersion")));
    }

    @Test
    void testMapFunctionArray() {
        verifyFunction(new AnalyzeUseragentFunction("DeviceClass", "AgentNameVersionMajor", "OperatingSystemNameVersion"));
    }

    @Test
    void testMapFunctionArrayNoCache() {
        verifyFunction(new AnalyzeUseragentFunction(0, "DeviceClass", "AgentNameVersionMajor", "OperatingSystemNameVersion"));
    }

    @Test
    void testMapFunctionAskNothingGetAll() {
        verifyFunction(new AnalyzeUseragentFunction(), results -> results.size() >= 3);
    }

}
