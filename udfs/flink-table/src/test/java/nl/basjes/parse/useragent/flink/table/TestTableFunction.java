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

package nl.basjes.parse.useragent.flink.table;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.flink.api.common.typeinfo.Types.STRING;
import static org.junit.Assert.assertEquals;

public class TestTableFunction {

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
    public void testFunction() throws Exception {
        // The base input stream
        StreamExecutionEnvironment                       senv        = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<Tuple3<String, String, String>> inputStream = getTestAgentStream(senv);

        // The table environment
        StreamTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(senv);

        // Give the stream a Table Name
        tableEnv.registerDataStream("AgentStream", inputStream, "useragent, expectedDeviceClass, expectedAgentNameVersionMajor");

        // register the function
        tableEnv.registerFunction("ParseUserAgent", new AnalyzeUseragentFunction("DeviceClass", "AgentNameVersionMajor"));

        String sqlQuery =
            "SELECT useragent,"+
            "       ParseUserAgent(useragent, 'DeviceClass'          )  as DeviceClass," +
            "       ParseUserAgent(useragent, 'AgentNameVersionMajor')  as AgentNameVersionMajor," +
            "       expectedDeviceClass," +
            "       expectedAgentNameVersionMajor " +
            "FROM AgentStream";
        Table  resultTable   = tableEnv.sqlQuery(sqlQuery);

        TypeInformation<Row> tupleType = new RowTypeInfo(STRING, STRING, STRING, STRING, STRING);
        DataStream<Row> resultSet = tableEnv.toAppendStream(resultTable, tupleType);

        resultSet.map((MapFunction<Row, String>) row -> {
            assertEquals("Wrong DeviceClass: "           + row.getField(0), row.getField(3), row.getField(1));
            assertEquals("Wrong AgentNameVersionMajor: " + row.getField(0), row.getField(4), row.getField(2));
            return row.getField(0).toString();
        }).print();

        senv.execute();
    }


    private static final String USERAGENT =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36";

    @Test
    public void testFunctionList() {
        AnalyzeUseragentFunction function = new AnalyzeUseragentFunction(Arrays.asList("DeviceClass", "AgentNameVersionMajor"));
        function.open(null);
        assertEquals("Desktop",   function.eval(USERAGENT, "DeviceClass"));
        assertEquals("Chrome 70", function.eval(USERAGENT, "AgentNameVersionMajor"));
    }

    @Test
    public void testFunctionListNoCache() {
        AnalyzeUseragentFunction function = new AnalyzeUseragentFunction(0, Arrays.asList("DeviceClass", "AgentNameVersionMajor"));
        function.open(null);
        assertEquals("Desktop",   function.eval(USERAGENT, "DeviceClass"));
        assertEquals("Chrome 70", function.eval(USERAGENT, "AgentNameVersionMajor"));
    }

    @Test
    public void testFunctionArray() {
        AnalyzeUseragentFunction function = new AnalyzeUseragentFunction("DeviceClass", "AgentNameVersionMajor");
        function.open(null);
        assertEquals("Desktop",   function.eval(USERAGENT, "DeviceClass"));
        assertEquals("Chrome 70", function.eval(USERAGENT, "AgentNameVersionMajor"));
    }

    @Test
    public void testFunctionArrayNoCache() {
        AnalyzeUseragentFunction function = new AnalyzeUseragentFunction(0, "DeviceClass", "AgentNameVersionMajor");
        function.open(null);
        assertEquals("Desktop",   function.eval(USERAGENT, "DeviceClass"));
        assertEquals("Chrome 70", function.eval(USERAGENT, "AgentNameVersionMajor"));
    }

}
