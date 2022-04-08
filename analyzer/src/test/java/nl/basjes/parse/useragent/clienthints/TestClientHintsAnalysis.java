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

package nl.basjes.parse.useragent.clienthints;

import nl.basjes.parse.useragent.Analyzer;
import nl.basjes.parse.useragent.UserAgent.ImmutableUserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.config.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.MethodName.class)
class TestClientHintsAnalysis {

    private static final Logger LOG = LogManager.getLogger(TestClientHintsAnalysis.class);

    public void assertValid(TestCase testCase, String testVariantName) {
        LOG.info("Checking \"{}\" ({})", testCase.getTestName(), testVariantName);
        TestCase.TestResult result = testCase.verify(analyzer);
        if (result.testFailed()) {
            fail("TestCase \"" + testCase.getTestName() + "\" (\""+testVariantName+"\") failed: ." + result.getErrorReport());
        }
    }

    // ------------------------------------------

    static Analyzer analyzer;

    @BeforeAll
    static void beforeAll() {
        analyzer = UserAgentAnalyzer.newBuilder().immediateInitialization().build();
    }

    public void checkExpectations(ImmutableUserAgent userAgent, Map<String, String> expectations){
        for (Map.Entry<String, String> expectation : expectations.entrySet()) {
            assertEquals(expectation.getValue(), userAgent.getValue(expectation.getKey()));
        }
    }

    @Test
    void testChromeWindows11() {
        String userAgent =              "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.51 Safari/537.36";
        String secChUa =                "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"99\", \"Google Chrome\";v=\"99\"";
        String secChUaArch =            "\"x86\"";
        String secChUaFullVersionList = "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"99.0.4844.51\", \"Google Chrome\";v=\"99.0.4844.51\"";
        String secChUaMobile =          "?0";
        String secChUaModel =           "";
        String secChUaPlatform =        "\"Windows\"";
        String secChUaPlatformVersion = "\"14.0.0\"";

        TestCase testCase = new TestCase(userAgent, "Chrome on Windows 11");

        // ------------------------------------------
        // Without ClientHints
        testCase.expect("DeviceClass",                     "Desktop");
        testCase.expect("DeviceName",                      "Desktop");
        testCase.expect("DeviceBrand",                     "Unknown");
        testCase.expect("DeviceCpu",                       "Intel x86_64");
        testCase.expect("DeviceCpuBits",                   "64");
        testCase.expect("OperatingSystemClass",            "Desktop");
        testCase.expect("OperatingSystemName",             "Windows NT");
        testCase.expect("OperatingSystemVersion",          ">=10");
        testCase.expect("OperatingSystemVersionMajor",     ">=10");
        testCase.expect("OperatingSystemNameVersion",      "Windows >=10");
        testCase.expect("OperatingSystemNameVersionMajor", "Windows >=10");
        testCase.expect("LayoutEngineClass",               "Browser");
        testCase.expect("LayoutEngineName",                "Blink");
        testCase.expect("LayoutEngineVersion",             "99.0");
        testCase.expect("LayoutEngineVersionMajor",        "99");
        testCase.expect("LayoutEngineNameVersion",         "Blink 99.0");
        testCase.expect("LayoutEngineNameVersionMajor",    "Blink 99");
        testCase.expect("AgentClass",                      "Browser");
        testCase.expect("AgentName",                       "Chrome");
        testCase.expect("AgentVersion",                    "99.0.4844.51");
        testCase.expect("AgentVersionMajor",               "99");
        testCase.expect("AgentNameVersion",                "Chrome 99.0.4844.51");
        testCase.expect("AgentNameVersionMajor",           "Chrome 99");

        // Verify
        assertValid(testCase, "No ClientHints");

        // ------------------------------------------
        // Add Standard ClientHints
        testCase.addHeader("Sec-CH-UA",                    secChUa);
        testCase.addHeader("Sec-CH-UA-Mobile",             secChUaMobile);
        testCase.addHeader("Sec-CH-UA-Platform",           secChUaPlatform);

        // No change in expectations.

        // Verify
        assertValid(testCase, "Standard ClientHints");

        // ------------------------------------------
        // Add the extra ClientHints (i.e. after the server requested everything)
        testCase.addHeader("Sec-CH-UA-Arch",                secChUaArch);
        testCase.addHeader("Sec-CH-UA-Full-Version-List",   secChUaFullVersionList);
        testCase.addHeader("Sec-CH-UA-Model",               secChUaModel);
        testCase.addHeader("Sec-CH-UA-Platform-Version",    secChUaPlatformVersion);

        // Changed expectations.
        testCase.expect("OperatingSystemVersion",          "11");
        testCase.expect("OperatingSystemVersionMajor",     "11");
        testCase.expect("OperatingSystemNameVersion",      "Windows 11");
        testCase.expect("OperatingSystemNameVersionMajor", "Windows 11");

        testCase.expect("LayoutEngineVersion",             "99.0.4844.51");
        testCase.expect("LayoutEngineNameVersion",         "Blink 99.0.4844.51");

        testCase.expect("AgentVersion",                    "99.0.4844.51");
        testCase.expect("AgentNameVersion",                "Chrome 99.0.4844.51");

        // Verify
        assertValid(testCase, "Full ClientHints");
    }

    @Test
    void testChromeWindows11ReducedUA() {
        String userAgent =              "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.0.0 Safari/537.36";
        String secChUa =                "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"99\", \"Google Chrome\";v=\"99\"";
        String secChUaArch =            "\"x86\"";
        String secChUaFullVersionList = "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"99.0.4844.51\", \"Google Chrome\";v=\"99.0.4844.51\"";
        String secChUaMobile =          "?0";
        String secChUaModel =           "\"\"";
        String secChUaPlatform =        "\"Windows\"";
        String secChUaPlatformVersion = "\"14.0.0\"";

        TestCase testCase = new TestCase(userAgent, "Chrome on Windows 11 with Reduced User-Agent");

        // ------------------------------------------
        // Without ClientHints
        testCase.expect("DeviceClass",                     "Desktop");
        testCase.expect("DeviceName",                      "Desktop");
        testCase.expect("DeviceBrand",                     "Unknown");
        testCase.expect("DeviceCpu",                       "Intel x86_64");
        testCase.expect("DeviceCpuBits",                   "64");
        testCase.expect("OperatingSystemClass",            "Desktop");
        testCase.expect("OperatingSystemName",             "Windows NT");
        testCase.expect("OperatingSystemVersion",          "??");
        testCase.expect("OperatingSystemVersionMajor",     "??");
        testCase.expect("OperatingSystemNameVersion",      "Windows NT ??");
        testCase.expect("OperatingSystemNameVersionMajor", "Windows NT ??");
        testCase.expect("LayoutEngineClass",               "Browser");
        testCase.expect("LayoutEngineName",                "Blink");
        testCase.expect("LayoutEngineVersion",             "99");
        testCase.expect("LayoutEngineVersionMajor",        "99");
        testCase.expect("LayoutEngineNameVersion",         "Blink 99");
        testCase.expect("LayoutEngineNameVersionMajor",    "Blink 99");
        testCase.expect("AgentClass",                      "Browser");
        testCase.expect("AgentName",                       "Chrome");
        testCase.expect("AgentVersion",                    "99");
        testCase.expect("AgentVersionMajor",               "99");
        testCase.expect("AgentNameVersion",                "Chrome 99");
        testCase.expect("AgentNameVersionMajor",           "Chrome 99");

        // Verify
        assertValid(testCase, "No ClientHints");

        // ------------------------------------------
        // Add Standard ClientHints
        testCase.addHeader("Sec-CH-UA",                     secChUa);
        testCase.addHeader("Sec-CH-UA-Mobile",              secChUaMobile);
        testCase.addHeader("Sec-CH-UA-Platform",            secChUaPlatform);

        // No change in expectations.

        // Verify
        assertValid(testCase, "Standard ClientHints");

        // ------------------------------------------
        // Add Full ClientHints (i.e. after the server requested everything)
        testCase.addHeader("Sec-CH-UA-Arch",                secChUaArch);
        testCase.addHeader("Sec-CH-UA-Full-Version-List",   secChUaFullVersionList);
        testCase.addHeader("Sec-CH-UA-Model",               secChUaModel);
        testCase.addHeader("Sec-CH-UA-Platform-Version",    secChUaPlatformVersion);

        // Changed expectations.
        testCase.expect("OperatingSystemVersion",          "11");
        testCase.expect("OperatingSystemVersionMajor",     "11");
        testCase.expect("OperatingSystemNameVersion",      "Windows 11");
        testCase.expect("OperatingSystemNameVersionMajor", "Windows 11");

        testCase.expect("LayoutEngineVersion",             "99.0.4844.51");
        testCase.expect("LayoutEngineNameVersion",         "Blink 99.0.4844.51");

        testCase.expect("AgentVersion",                    "99.0.4844.51");
        testCase.expect("AgentNameVersion",                "Chrome 99.0.4844.51");

        // Verify
        assertValid(testCase, "Full ClientHints");
    }

    @Test
    void testChromeAndroid11ReducedUA() {
        // Real values Chrome on Linux with the reduced header enabled
        String userAgent =              "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.0.0 Mobile Safari/537.36";
        String secChUa =                "\"(Not(A:Brand\";v=\"8\", \"Chromium\";v=\"100\", \"Google Chrome\";v=\"100\"";
        String secChUaArch =            "";
        String secChUaFullVersionList = "\"(Not(A:Brand\";v=\"8.0.0.0\", \"Chromium\";v=\"100.0.4896.30\", \"Google Chrome\";v=\"100.0.4896.30\"";
        String secChUaMobile =          "?1";
        String secChUaModel =           "\"Nokia 7.2\"";
        String secChUaPlatform =        "\"Android\"";
        String secChUaPlatformVersion = "\"11.0.0\"";

        TestCase testCase = new TestCase(userAgent, "Chrome on Android 11 with Reduced User-Agent");

        // ------------------------------------------
        // Without ClientHints
        testCase.expect("DeviceClass",                     "Phone");
        testCase.expect("DeviceName",                      "Unknown");
        testCase.expect("DeviceBrand",                     "Unknown");
        testCase.expect("OperatingSystemClass",            "Mobile");
        testCase.expect("OperatingSystemName",             "Android");
        testCase.expect("OperatingSystemVersion",          "??");
        testCase.expect("OperatingSystemVersionMajor",     "??");
        testCase.expect("OperatingSystemNameVersion",      "Android ??");
        testCase.expect("OperatingSystemNameVersionMajor", "Android ??");
        testCase.expect("LayoutEngineClass",               "Browser");
        testCase.expect("LayoutEngineName",                "Blink");
        testCase.expect("LayoutEngineVersion",             "100");
        testCase.expect("LayoutEngineVersionMajor",        "100");
        testCase.expect("LayoutEngineNameVersion",         "Blink 100");
        testCase.expect("LayoutEngineNameVersionMajor",    "Blink 100");
        testCase.expect("AgentClass",                      "Browser");
        testCase.expect("AgentName",                       "Chrome");
        testCase.expect("AgentVersion",                    "100");
        testCase.expect("AgentVersionMajor",               "100");
        testCase.expect("AgentNameVersion",                "Chrome 100");
        testCase.expect("AgentNameVersionMajor",           "Chrome 100");

        // Verify
        assertValid(testCase, "No ClientHints");

        // ------------------------------------------
        // Add Standard ClientHints
        testCase.addHeader("Sec-CH-UA",                    secChUa);
        testCase.addHeader("Sec-CH-UA-Mobile",             secChUaMobile);
        testCase.addHeader("Sec-CH-UA-Platform",           secChUaPlatform);

        // No change in expectations.

        // Verify
        assertValid(testCase, "Standard ClientHints");

        // ------------------------------------------
        // Add Full ClientHints (i.e. after the server requested everything)
        testCase.addHeader("Sec-CH-UA-Arch",                secChUaArch);
        testCase.addHeader("Sec-CH-UA-Full-Version-List",   secChUaFullVersionList);
        testCase.addHeader("Sec-CH-UA-Model",               secChUaModel);
        testCase.addHeader("Sec-CH-UA-Platform-Version",    secChUaPlatformVersion);

        // Changed expectations.
        testCase.expect("DeviceClass",                      "Phone");
        testCase.expect("DeviceBrand",                      "Nokia");
        testCase.expect("DeviceName",                       "Nokia 7.2");

        testCase.expect("OperatingSystemVersion",           "11.0.0");
        testCase.expect("OperatingSystemVersionMajor",      "11");
        testCase.expect("OperatingSystemNameVersion",       "Android 11.0.0");
        testCase.expect("OperatingSystemNameVersionMajor",  "Android 11");

        testCase.expect("LayoutEngineVersion",              "100.0.4896.30");
        testCase.expect("LayoutEngineNameVersion",          "Blink 100.0.4896.30");

        testCase.expect("AgentVersion",                     "100.0.4896.30");
        testCase.expect("AgentNameVersion",                 "Chrome 100.0.4896.30");

        // Verify
        assertValid(testCase, "Full ClientHints");
    }

    @Test
    void testChromeLinuxReducedUA() {
        // Real values Chrome on Linux with the reduced header enabled
        String userAgent =              "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.0.0 Safari/537.36";
        String secChUa =                "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"99\", \"Google Chrome\";v=\"99\"";
        String secChUaArch =            "\"x86\"";
        String secChUaFullVersionList = "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"99.0.4844.51\", \"Google Chrome\";v=\"99.0.4844.51\"";
        String secChUaMobile =          "?0";
        String secChUaModel =           "";
        String secChUaPlatform =        "\"Linux\"";
        String secChUaPlatformVersion = "\"5.13.0\"";

        TestCase testCase = new TestCase(userAgent, "Chrome on Linux with Reduced User-Agent");

        // ------------------------------------------
        // Without ClientHints
        testCase.expect("DeviceClass",                     "Desktop");
        testCase.expect("DeviceName",                      "Linux Desktop");
        testCase.expect("DeviceBrand",                     "Unknown");
        testCase.expect("DeviceCpu",                       "Intel x86_64");
        testCase.expect("DeviceCpuBits",                   "64");

        testCase.expect("OperatingSystemClass",            "Desktop");
        testCase.expect("OperatingSystemName",             "Linux");
        testCase.expect("OperatingSystemVersion",          "??");
        testCase.expect("OperatingSystemVersionMajor",     "??");
        testCase.expect("OperatingSystemNameVersion",      "Linux ??");
        testCase.expect("OperatingSystemNameVersionMajor", "Linux ??");
        testCase.expect("LayoutEngineClass",               "Browser");
        testCase.expect("LayoutEngineName",                "Blink");
        testCase.expect("LayoutEngineVersion",             "99");
        testCase.expect("LayoutEngineVersionMajor",        "99");
        testCase.expect("LayoutEngineNameVersion",         "Blink 99");
        testCase.expect("LayoutEngineNameVersionMajor",    "Blink 99");
        testCase.expect("AgentClass",                      "Browser");
        testCase.expect("AgentName",                       "Chrome");
        testCase.expect("AgentVersion",                    "99");
        testCase.expect("AgentVersionMajor",               "99");
        testCase.expect("AgentNameVersion",                "Chrome 99");
        testCase.expect("AgentNameVersionMajor",           "Chrome 99");

        // Verify
        assertValid(testCase, "No ClientHints");

        // ------------------------------------------
        // Add Standard ClientHints
        testCase.addHeader("Sec-CH-UA",                    secChUa);
        testCase.addHeader("Sec-CH-UA-Mobile",             secChUaMobile);
        testCase.addHeader("Sec-CH-UA-Platform",           secChUaPlatform);

        // No change in expectations.

        // Verify
        assertValid(testCase, "Standard ClientHints");

        // ------------------------------------------
        // Add Full ClientHints (i.e. after the server requested everything)
        testCase.addHeader("Sec-CH-UA-Arch",                secChUaArch);
        testCase.addHeader("Sec-CH-UA-Full-Version-List",   secChUaFullVersionList);
        testCase.addHeader("Sec-CH-UA-Model",               secChUaModel);
        testCase.addHeader("Sec-CH-UA-Platform-Version",    secChUaPlatformVersion);

        // Changed expectations.
        testCase.expect("OperatingSystemVersion",           "5.13.0");
        testCase.expect("OperatingSystemVersionMajor",      "5");
        testCase.expect("OperatingSystemNameVersion",       "Linux 5.13.0");
        testCase.expect("OperatingSystemNameVersionMajor",  "Linux 5");

        testCase.expect("LayoutEngineVersion",              "99.0.4844.51");
        testCase.expect("LayoutEngineNameVersion",          "Blink 99.0.4844.51");

        testCase.expect("AgentVersion",                     "99.0.4844.51");
        testCase.expect("AgentNameVersion",                 "Chrome 99.0.4844.51");

        // Verify
        assertValid(testCase, "Full ClientHints");
    }

}
