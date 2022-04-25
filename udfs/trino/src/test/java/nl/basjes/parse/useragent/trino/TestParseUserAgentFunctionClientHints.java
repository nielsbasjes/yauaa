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
package nl.basjes.parse.useragent.trino;

import io.trino.operator.scalar.AbstractTestFunctions;
import io.trino.spi.type.MapType;
import io.trino.spi.type.TypeOperators;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.TreeMap;

import static io.trino.spi.type.VarcharType.VARCHAR;

public class TestParseUserAgentFunctionClientHints extends AbstractTestFunctions {

    @BeforeClass
    public void setUp() {
        functionAssertions.installPlugin(new YauaaPlugin());
    }

    @Test
    public void testNormalUsage() {
        UserAgentAnalyzer analyzer = UserAgentAnalyzer.newBuilder().showMinimalVersion().build();

        // To avoid the need to update this with new features we simply use the analyzer to determine what the outcome should be.
        Map<String, String> requestHeaders = new TreeMap<>();
        requestHeaders.put("user-Agent",                  "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.60 Safari/537.36");
        requestHeaders.put("sec-CH-UA-Platform",          "\"macOS\"");
        requestHeaders.put("sec-CH-UA-Platform-Version",  "\"12.3.1\"");
        Map<String, String> expected = analyzer.parse(requestHeaders).toMap(analyzer.getAllPossibleFieldNamesSorted());

        assertFunction(
            "parse_user_agent( " +
            "    ARRAY[" +
            "       'user-Agent',                  'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.60 Safari/537.36'," +
            "       'sec-CH-UA-Platform',          '\"macOS\"'," +
            "       'sec-CH-UA-Platform-Version',  '\"12.3.1\"'" +
            "    ]" +
            ")", new MapType(VARCHAR, VARCHAR,  new TypeOperators()), expected);
    }

}
