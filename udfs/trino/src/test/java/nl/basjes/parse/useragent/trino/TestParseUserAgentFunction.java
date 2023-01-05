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
package nl.basjes.parse.useragent.trino;

import io.trino.metadata.InternalFunctionBundle;
import io.trino.spi.type.MapType;
import io.trino.spi.type.TypeOperators;
import io.trino.sql.query.QueryAssertions;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Map;

import static io.trino.spi.type.VarcharType.VARCHAR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
class TestParseUserAgentFunction {

    private QueryAssertions assertions;

    @BeforeAll
    public void setUp() {
        assertions = new QueryAssertions();
        assertions.addFunctions(InternalFunctionBundle.extractFunctions(new YauaaPlugin().getFunctions()));
    }

    @AfterAll
    public void teardown() {
        assertions.close();
        assertions = null;
    }

    @Test
    void testNormalUsage() {
        UserAgentAnalyzer analyzer = UserAgentAnalyzer.newBuilder().showMinimalVersion().build();

        String useragent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.60 Safari/537.36";
        // To avoid the need to update this with new features we simply use the analyzer to determine what the outcome should be.
        Map<String, String> expected = analyzer.parse(useragent).toMap(analyzer.getAllPossibleFieldNamesSorted());

        assertThat(assertions.function("parse_user_agent", "'"+useragent+"'"))
            .hasType(new MapType(VARCHAR, VARCHAR,  new TypeOperators()))
            .isEqualTo(expected);
    }

}
