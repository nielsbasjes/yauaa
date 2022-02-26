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
import io.trino.spi.Plugin;
import io.trino.spi.type.MapType;
import io.trino.spi.type.TypeOperators;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.TreeMap;

import static io.trino.spi.type.VarcharType.VARCHAR;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestParseFunction
    extends AbstractTestFunctions {

    @BeforeAll
    public void setUp() {
        initTestFunctions();
        Plugin plugin = new YauaaPlugin();
        plugin.getFunctions().forEach(this::registerScalar);
    }

    @Test
    void testParser() {
        TreeMap<String, String> expected = new TreeMap<>();
        expected.put("DeviceClass",                     "Desktop");
        expected.put("DeviceName",                      "Linux Desktop");
        expected.put("DeviceBrand",                     "Unknown");
        expected.put("DeviceCpu",                       "Intel x86_64");
        expected.put("DeviceCpuBits",                   "64");
        expected.put("DeviceFirmwareVersion",           "??");
        expected.put("DeviceVersion",                   "??");
        expected.put("OperatingSystemClass",            "Desktop");
        expected.put("OperatingSystemName",             "Linux");
        expected.put("OperatingSystemVersion",          "??");
        expected.put("OperatingSystemVersionMajor",     "??");
        expected.put("OperatingSystemNameVersion",      "Linux ??");
        expected.put("OperatingSystemNameVersionMajor", "Linux ??");
        expected.put("OperatingSystemVersionBuild",     "??");
        expected.put("LayoutEngineClass",               "Browser");
        expected.put("LayoutEngineName",                "Blink");
        expected.put("LayoutEngineVersion",             "98.0");
        expected.put("LayoutEngineVersionMajor",        "98");
        expected.put("LayoutEngineNameVersion",         "Blink 98.0");
        expected.put("LayoutEngineNameVersionMajor",    "Blink 98");
        expected.put("LayoutEngineBuild",               "Unknown");
        expected.put("AgentClass",                      "Browser");
        expected.put("AgentName",                       "Chrome");
        expected.put("AgentVersion",                    "98.0.4758.102");
        expected.put("AgentVersionMajor",               "98");
        expected.put("AgentNameVersion",                "Chrome 98.0.4758.102");
        expected.put("AgentNameVersionMajor",           "Chrome 98");
        expected.put("AgentBuild",                      "Unknown");
        expected.put("AgentLanguage",                   "Unknown");
        expected.put("AgentLanguageCode",               "Unknown");
        expected.put("AgentInformationEmail",           "Unknown");
        expected.put("AgentInformationUrl",             "Unknown");
        expected.put("AgentSecurity",                   "Unknown");
        expected.put("AgentUuid",                       "Unknown");
        expected.put("WebviewAppName",                  "Unknown");
        expected.put("WebviewAppVersion",               "??");
        expected.put("WebviewAppVersionMajor",          "??");
        expected.put("WebviewAppNameVersion",           "Unknown ??");
        expected.put("WebviewAppNameVersionMajor",      "Unknown ??");
        expected.put("FacebookCarrier",                 "Unknown");
        expected.put("FacebookDeviceClass",             "Unknown");
        expected.put("FacebookDeviceName",              "Unknown");
        expected.put("FacebookDeviceVersion",           "??");
        expected.put("FacebookFBOP",                    "Unknown");
        expected.put("FacebookFBSS",                    "Unknown");
        expected.put("FacebookOperatingSystemName",     "Unknown");
        expected.put("FacebookOperatingSystemVersion",  "??");
        expected.put("Anonymized",                      "Unknown");
        expected.put("HackerAttackVector",              "Unknown");
        expected.put("HackerToolkit",                   "Unknown");
        expected.put("KoboAffiliate",                   "Unknown");
        expected.put("KoboPlatformId",                  "Unknown");
        expected.put("IECompatibilityVersion",          "??");
        expected.put("IECompatibilityVersionMajor",     "??");
        expected.put("IECompatibilityNameVersion",      "Unknown ??");
        expected.put("IECompatibilityNameVersionMajor", "Unknown ??");
        expected.put("__SyntaxError__",                 "false");
        expected.put("Carrier",                         "Unknown");
        expected.put("GSAInstallationID",               "Unknown");
        expected.put("NetworkType",                     "Unknown");

        assertFunction("parse_user_agent('Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36')",
            new MapType(VARCHAR, VARCHAR, new TypeOperators()), expected);
    }
}
