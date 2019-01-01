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

package nl.basjes.parse.useragent.drill;

import org.apache.drill.common.config.DrillConfig;
import org.apache.drill.exec.expr.DrillSimpleFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.fn.impl.StringFunctionHelpers;
import org.apache.drill.exec.expr.holders.NullableVarCharHolder;
import org.apache.drill.exec.expr.holders.VarCharHolder;
import org.apache.drill.exec.memory.BufferAllocator;
import org.apache.drill.exec.memory.RootAllocatorFactory;
import org.apache.drill.test.BaseDirTestWatcher;
import org.apache.drill.test.ClusterFixture;
import org.apache.drill.test.ClusterFixtureBuilder;
import org.apache.drill.test.ClusterTest;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestParseUserAgentFunctionField extends ClusterTest {

    private static final String TEST_INPUT =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.82 Safari/537.36";
    private static final String EXPECTED_DEVICE_CLASS = "Desktop";
    private static final String EXPECTED_AGENT_NAME_VERSION = "Chrome 48.0.2564.82";

    @ClassRule
    public static final BaseDirTestWatcher DIR_TEST_WATCHER = new BaseDirTestWatcher();

    @BeforeClass
    public static void setup() throws Exception {
        ClusterFixtureBuilder builder = ClusterFixture.builder(DIR_TEST_WATCHER)
            .configProperty("drill.classpath.scanning.cache.enabled", false);
        startCluster(builder);
    }

    // -------------------------------------------------------------------------------------------------------

    @Test
    public void testAnnotation() {
        Class<? extends DrillSimpleFunc> fnClass = ParseUserAgentFunctionField.class;
        FunctionTemplate fnDefn = fnClass.getAnnotation(FunctionTemplate.class);
        assertNotNull(fnDefn);
        assertEquals("parse_user_agent_field", fnDefn.name());
        assertEquals(FunctionTemplate.FunctionScope.SIMPLE, fnDefn.scope());
        assertEquals(FunctionTemplate.NullHandling.NULL_IF_NULL, fnDefn.nulls());
    }

    @Test
    public void testDirect() {
        ParseUserAgentFunctionField function = new ParseUserAgentFunctionField();
        function.setup();

        DrillConfig c = DrillConfig.create();
        BufferAllocator allocator = RootAllocatorFactory.newRoot(c);

        // Input useragent
        function.useragent = new NullableVarCharHolder();
        function.useragent.buffer = allocator.buffer(1000);
        setStringInVarCharHolder(TEST_INPUT, function.useragent);

        // Input the field we want
        function.fieldName = new VarCharHolder();
        function.fieldName.buffer = allocator.buffer(1000);
        setStringInVarCharHolder("AgentNameVersion", function.fieldName);

        // Output the result
        function.value = new VarCharHolder();
        // The buffer for the output
        function.outBuffer = allocator.buffer(1000);

        function.eval();

        assertTrue(function.allFields.contains("DeviceClass"));
        assertTrue(function.allFields.contains("AgentNameVersion"));

        assertEquals(EXPECTED_AGENT_NAME_VERSION, StringFunctionHelpers.getStringFromVarCharHolder(function.value));
    }

    @Test
    public void testParseUserAgentField() throws Exception {
        final String query = "SELECT " +
            "parse_user_agent_field('"+TEST_INPUT+"', 'AgentNameVersion') as AgentNameVersion " +
//            ", parse_user_agent_field('"+TEST_INPUT+"', 'DeviceClass'     ) as DeviceClass " +
            "from (values(1))";
        testBuilder().sqlQuery(query).ordered()
            .baselineColumns("AgentNameVersion")    .baselineValues(EXPECTED_AGENT_NAME_VERSION)
//            .baselineColumns("DeviceClass")         .baselineValues(EXPECTED_DEVICE_CLASS)
            .go();
    }

    public static void setStringInVarCharHolder(String input, VarCharHolder varCharHolder) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        varCharHolder.buffer.reallocIfNeeded(bytes.length);
        varCharHolder.buffer.setBytes(0, bytes);
        varCharHolder.start = 0;
        varCharHolder.end = bytes.length;
    }

    public static void setStringInVarCharHolder(String input, NullableVarCharHolder nullableVarCharHolder) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        nullableVarCharHolder.buffer.reallocIfNeeded(bytes.length);
        nullableVarCharHolder.buffer.setBytes(0, bytes);
        nullableVarCharHolder.start = 0;
        nullableVarCharHolder.end = bytes.length;
        nullableVarCharHolder.isSet = 1;
    }

}
