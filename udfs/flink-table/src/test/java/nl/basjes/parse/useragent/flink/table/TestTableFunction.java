/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2021 Niels Basjes
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
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.apache.flink.api.common.typeinfo.Types.MAP;
import static org.apache.flink.api.common.typeinfo.Types.STRING;
import static org.apache.flink.table.api.Expressions.$;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestTableFunction {

    public static DataStreamSource<Tuple3<String, String, String>> getTestAgentStream(StreamExecutionEnvironment env) {
        // Useragent, Expected DeviceClass, Expected AgentNameVersionMajor
        List<Tuple3<String, String, String>> data = new ArrayList<>();

        data.add(new Tuple3<>(
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36",
            "Desktop",
            "Chrome 70"));

        data.add(new Tuple3<>(
            "Mozilla/5.0 (Linux; Android 7.1.1; Nexus 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.57 Mobile Safari/537.36",
            "Phone",
            "Chrome 70"));

        data.add(new Tuple3<>(
            "Mozilla/5.0 (Linux; U; Android 4.4.2; PE-TL20 Build/HuaweiPE-TL20) AppleWebKit/533.1 (KHTML, like Gecko)Version/4.0 " +
                "MQQBrowser/5.4 TBS/025440 Mobile Safari/533.1 MicroMessenger/6.2.5.53_r2565f18.621 NetType/WIFI Language/zh_CN",
            "Phone",
            "WeChat 6"));

        return env.fromCollection(data);
    }

    @Test
    void testFunctionExtractDirect() throws Exception {
        // The base execution environment
        StreamExecutionEnvironment   senv        = StreamExecutionEnvironment.getExecutionEnvironment();

        // The table environment
        StreamTableEnvironment       tableEnv    = StreamTableEnvironment.create(senv);

        // The demo input stream
        DataStreamSource<Tuple3<String, String, String>> inputStream = getTestAgentStream(senv);

        // Give the stream a Table Name and name the fields
        tableEnv.createTemporaryView("AgentStream", inputStream, $("useragent"), $("expectedDeviceClass"), $("expectedAgentNameVersionMajor"));

        // Register the function
        tableEnv.createTemporarySystemFunction("ParseUserAgent", new AnalyzeUseragentFunction("DeviceClass", "AgentNameVersionMajor"));

        // The downside of doing it this way is that the parsing function (i.e. parsing and converting all results into a map)
        // is called for each field you want. So in this simple case twice.
        String sqlQuery =
            "SELECT useragent,"+
            "       ParseUserAgent(useragent)['DeviceClass'          ]  as DeviceClass," +
            "       ParseUserAgent(useragent)['AgentNameVersionMajor']  as AgentNameVersionMajor," +
            "       expectedDeviceClass," +
            "       expectedAgentNameVersionMajor " +
            "FROM AgentStream";

        Table  resultTable   = tableEnv.sqlQuery(sqlQuery);

        TypeInformation<Row> tupleType = new RowTypeInfo(STRING, STRING, STRING, STRING, STRING);
        DataStream<Row> resultSet = tableEnv.toAppendStream(resultTable, tupleType);

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

            assertEquals(expectedDeviceClass,           deviceClass,           "Wrong DeviceClass: "           + useragent);
            assertEquals(expectedAgentNameVersionMajor, agentNameVersionMajor, "Wrong AgentNameVersionMajor: " + useragent);
            return useragent.toString();
        }).printToErr();

        senv.execute();
    }

    @Test
    void testMapFunctionExtractInSQLSubSelect() throws Exception {
        // The base execution environment
        StreamExecutionEnvironment   senv        = StreamExecutionEnvironment.getExecutionEnvironment();

        // The table environment
        StreamTableEnvironment       tableEnv    = StreamTableEnvironment.create(senv);

        // The demo input stream
        DataStreamSource<Tuple3<String, String, String>> inputStream = getTestAgentStream(senv);

        // Give the stream a Table Name and name the fields
        tableEnv.createTemporaryView("AgentStream", inputStream, $("useragent"), $("expectedDeviceClass"), $("expectedAgentNameVersionMajor"));

        // Register the function
        tableEnv.createTemporarySystemFunction("ParseUserAgent", new AnalyzeUseragentFunction("DeviceClass", "AgentNameVersionMajor"));

        // Doing it this way the function is called once and then the results are picked from the map that was returned.
        String sqlQuery =
            "SELECT useragent,"+
            "       parsedUseragent['DeviceClass']              AS deviceClass," +
            "       parsedUseragent['AgentNameVersionMajor']    AS agentNameVersionMajor," +
            "       expectedDeviceClass," +
            "       expectedAgentNameVersionMajor " +
            "FROM ( " +
            "   SELECT useragent," +
            "          ParseUserAgent(useragent) AS parsedUseragent," +
            "          expectedDeviceClass," +
            "          expectedAgentNameVersionMajor " +
            "   FROM   AgentStream " +
            ")";

        Table  resultTable   = tableEnv.sqlQuery(sqlQuery);

        TypeInformation<Row> tupleType = new RowTypeInfo(STRING, STRING, STRING, STRING, STRING);
        DataStream<Row> resultSet = tableEnv.toAppendStream(resultTable, tupleType);

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

            assertEquals(expectedDeviceClass,           deviceClass,           "Wrong DeviceClass: "           + useragent);
            assertEquals(expectedAgentNameVersionMajor, agentNameVersionMajor, "Wrong AgentNameVersionMajor: " + useragent);
            return useragent.toString();
        }).printToErr();

        senv.execute();
    }

    @Test
    void testMapFunctionReturnMap() throws Exception {
        // The base execution environment
        StreamExecutionEnvironment   senv        = StreamExecutionEnvironment.getExecutionEnvironment();

        // The table environment
        StreamTableEnvironment       tableEnv    = StreamTableEnvironment.create(senv);

        // The demo input stream
        DataStreamSource<Tuple3<String, String, String>> inputStream = getTestAgentStream(senv);

        // Give the stream a Table Name and name the fields
        tableEnv.createTemporaryView("AgentStream", inputStream, $("useragent"), $("expectedDeviceClass"), $("expectedAgentNameVersionMajor"));

        // Register the function
        tableEnv.createTemporarySystemFunction("ParseUserAgent", new AnalyzeUseragentFunction("DeviceClass", "AgentNameVersionMajor"));

        // Here the query returns the entire map as one thing
        String sqlQuery =
            "SELECT useragent," +
            "       ParseUserAgent(useragent)        AS parsedUseragent," +
            "       expectedDeviceClass              AS expectedDeviceClass," +
            "       expectedAgentNameVersionMajor    AS expectedAgentNameVersionMajor " +
            "FROM   AgentStream";

        Table  resultTable   = tableEnv.sqlQuery(sqlQuery);

        TypeInformation<Row> tupleType = new RowTypeInfo(STRING, MAP(STRING, STRING), STRING, STRING);
        DataStream<Row> resultSet = tableEnv.toAppendStream(resultTable, tupleType);

        resultSet.map((MapFunction<Row, String>) row -> {
            Object useragent                     = row.getField(0);
            Object parsedUseragent               = row.getField(1);
            Object expectedDeviceClass           = row.getField(2);
            Object expectedAgentNameVersionMajor = row.getField(3);

            assertTrue(useragent                     instanceof String);
            assertTrue(parsedUseragent               instanceof Map<?, ?>);
            assertTrue(expectedDeviceClass           instanceof String);
            assertTrue(expectedAgentNameVersionMajor instanceof String);

            assertEquals(
                expectedDeviceClass,
                ((Map<?, ?>)parsedUseragent).get("DeviceClass"),
                "Wrong DeviceClass: "           + useragent);

            assertEquals(
                expectedAgentNameVersionMajor,
                ((Map<?, ?>)parsedUseragent).get("AgentNameVersionMajor"),
                "Wrong AgentNameVersionMajor: " + useragent);

            return useragent.toString();
        }).printToErr();

        senv.execute();
    }

    private static final String USERAGENT =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36";

    private void verifyFunction(AnalyzeUseragentFunction function){
        function.open(null);
        final Map<String, String> result = function.eval(USERAGENT);
        assertEquals(2,           result.size());
        assertEquals("Desktop",   result.get("DeviceClass"));
        assertEquals("Chrome 70", result.get("AgentNameVersionMajor"));
    }

    @Test
    void testMapFunctionList() {
        verifyFunction(new AnalyzeUseragentFunction(Arrays.asList("DeviceClass", "AgentNameVersionMajor")));
    }

    @Test
    void testMapFunctionListNoCache() {
        verifyFunction(new AnalyzeUseragentFunction(0, Arrays.asList("DeviceClass", "AgentNameVersionMajor")));
    }

    @Test
    void testMapFunctionArray() {
        verifyFunction(new AnalyzeUseragentFunction("DeviceClass", "AgentNameVersionMajor"));
    }

    @Test
    void testMapFunctionArrayNoCache() {
        verifyFunction(new AnalyzeUseragentFunction(0, "DeviceClass", "AgentNameVersionMajor"));
    }

    @Test
    void testMapFunctionAskNothingGetAll() {
        AnalyzeUseragentFunction function = new AnalyzeUseragentFunction();
        function.open(null);
        final Map<String, String> result = function.eval(USERAGENT);
        assertTrue(2 <= result.size());
        assertEquals("Desktop",   result.get("DeviceClass"));
        assertEquals("Chrome 70", result.get("AgentNameVersionMajor"));
    }

}
