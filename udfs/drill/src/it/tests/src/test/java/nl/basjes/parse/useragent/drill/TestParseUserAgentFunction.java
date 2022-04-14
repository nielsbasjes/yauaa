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

package nl.basjes.parse.useragent.drill;

import org.apache.drill.common.expression.ExpressionStringBuilder;
import org.apache.drill.exec.expr.DrillSimpleFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.test.ClusterFixture;
import org.apache.drill.test.ClusterFixtureBuilder;
import org.apache.drill.test.ClusterTest;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.apache.drill.test.TestBuilder.parsePath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestParseUserAgentFunction extends ClusterTest {

    @BeforeClass
    public static void setup() throws Exception {
        ClusterFixtureBuilder builder = ClusterFixture.builder(dirTestWatcher);
        startCluster(builder);
    }

    // -------------------------------------------------------------------------------------------------------

    @Test
    public void testAnnotation() {
        Class<? extends DrillSimpleFunc> fnClass = ParseUserAgentFunction.class;
        FunctionTemplate fnDefn = fnClass.getAnnotation(FunctionTemplate.class);
        assertNotNull(fnDefn);
        assertEquals("parse_user_agent", fnDefn.name());
        assertEquals(FunctionTemplate.FunctionScope.SIMPLE, fnDefn.scope());
        assertEquals(FunctionTemplate.NullHandling.INTERNAL, fnDefn.nulls());
    }

    @Test
    public void testParseUserAgentString() throws Exception {
        String query =
            "SELECT " +
            "   t1.ua.DeviceClass                     AS DeviceClass,\n" +
            "   t1.ua.DeviceName                      AS DeviceName,\n" +
            "   t1.ua.DeviceBrand                     AS DeviceBrand,\n" +
            "   t1.ua.DeviceCpuBits                   AS DeviceCpuBits,\n" +
            "   t1.ua.OperatingSystemClass            AS OperatingSystemClass,\n" +
            "   t1.ua.OperatingSystemName             AS OperatingSystemName,\n" +
            "   t1.ua.OperatingSystemVersion          AS OperatingSystemVersion,\n" +
            "   t1.ua.OperatingSystemVersionMajor     AS OperatingSystemVersionMajor,\n" +
            "   t1.ua.OperatingSystemNameVersion      AS OperatingSystemNameVersion,\n" +
            "   t1.ua.OperatingSystemNameVersionMajor AS OperatingSystemNameVersionMajor,\n" +
            "   t1.ua.LayoutEngineClass               AS LayoutEngineClass,\n" +
            "   t1.ua.LayoutEngineName                AS LayoutEngineName,\n" +
            "   t1.ua.LayoutEngineVersion             AS LayoutEngineVersion,\n" +
            "   t1.ua.LayoutEngineVersionMajor        AS LayoutEngineVersionMajor,\n" +
            "   t1.ua.LayoutEngineNameVersion         AS LayoutEngineNameVersion,\n" +
            "   t1.ua.LayoutEngineBuild               AS LayoutEngineBuild,\n" +
            "   t1.ua.AgentClass                      AS AgentClass,\n" +
            "   t1.ua.AgentName                       AS AgentName,\n" +
            "   t1.ua.AgentVersion                    AS AgentVersion,\n" +
            "   t1.ua.AgentVersionMajor               AS AgentVersionMajor,\n" +
            "   t1.ua.AgentNameVersionMajor           AS AgentNameVersionMajor,\n" +
            "   t1.ua.AgentLanguage                   AS AgentLanguage,\n" +
            "   t1.ua.AgentLanguageCode               AS AgentLanguageCode,\n" +
            "   t1.ua.AgentSecurity                   AS AgentSecurity\n" +
            "FROM (" +
            "   SELECT" +
            "       parse_user_agent('Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.11) Gecko/20071127 Firefox/2.0.0.11') AS ua " +
            "   FROM (values(1))" +
            ") AS t1";

        testBuilder()
            .sqlQuery(query)
            .unOrdered()
            .baselineRecords(Collections.singletonList(// Singleton list because we expect 1 record
                expectations(
                    "DeviceClass",                     "Desktop",
                    "DeviceName",                      "Desktop",
                    "DeviceBrand",                     "Unknown",
                    "DeviceCpuBits",                   "32",
                    "OperatingSystemClass",            "Desktop",
                    "OperatingSystemName",             "Windows NT",
                    "OperatingSystemVersion",          "XP",
                    "OperatingSystemVersionMajor",     "XP",
                    "OperatingSystemNameVersion",      "Windows XP",
                    "OperatingSystemNameVersionMajor", "Windows XP",
                    "LayoutEngineClass",               "Browser",
                    "LayoutEngineName",                "Gecko",
                    "LayoutEngineVersion",             "1.8.1.11",
                    "LayoutEngineVersionMajor",        "1",
                    "LayoutEngineNameVersion",         "Gecko 1.8.1.11",
                    "LayoutEngineBuild",               "20071127",
                    "AgentClass",                      "Browser",
                    "AgentName",                       "Firefox",
                    "AgentVersion",                    "2.0.0.11",
                    "AgentVersionMajor",               "2",
                    "AgentNameVersionMajor",           "Firefox 2",
                    "AgentLanguage",                   "English (United States)",
                    "AgentLanguageCode",               "en-us",
                    "AgentSecurity",                   "Strong security"
                )
            ))
            .go();
    }

    private Map<String, Object> expectations(String... strings) {
        Map<String, Object> expectations = new LinkedHashMap<>();
        int index = 0;
        assertEquals("The number of arguments for 'expectations' must be even", 0, strings.length % 2);

        while (index < strings.length) {
            expectations.put(ExpressionStringBuilder.toString(parsePath(strings[index])), strings[index+1]);
            index+=2;
        }
        return expectations;
    }

}
