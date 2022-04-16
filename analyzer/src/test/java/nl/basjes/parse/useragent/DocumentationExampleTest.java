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

package nl.basjes.parse.useragent;

import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentationExampleTest {

    private static final Logger LOG = LogManager.getLogger(DocumentationExampleTest.class);

    @Test
    void runDocumentationExample() {
        UserAgentAnalyzerTester uaa = UserAgentAnalyzerTester
            .newBuilder()
            .showMinimalVersion()
            .hideMatcherLoadStats()
            .dropDefaultResources()
            .addResources("classpath*:DocumentationExample.yaml")
            .build();
        assertTrue(uaa.runTests(false, true));
    }

    @Test
    void runNormalUsageExample() {
        UserAgentAnalyzer uaa = UserAgentAnalyzer
            .newBuilder()
            .showMinimalVersion()
            .build();

        Map<String, String> requestHeaders = new TreeMap<>();

        requestHeaders.put("User-Agent",                       "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.0.0 Safari/537.36");
        requestHeaders.put("Sec-Ch-Ua",                        "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"100\", \"Google Chrome\";v=\"100\"");
        requestHeaders.put("Sec-Ch-Ua-Arch",                   "\"x86\"");
        requestHeaders.put("Sec-Ch-Ua-Full-Version-List",      "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"100.0.4896.75\", \"Google Chrome\";v=\"100.0.4896.75\"");
        requestHeaders.put("Sec-Ch-Ua-Mobile",                 "?0");
        requestHeaders.put("Sec-Ch-Ua-Model",                  "\"\"");
        requestHeaders.put("Sec-Ch-Ua-Platform",               "\"Windows\"");
        requestHeaders.put("Sec-Ch-Ua-Platform-Version",       "\"0.1.0\"");
        requestHeaders.put("Sec-Ch-Ua-Wow64",                  "?0");

        UserAgent userAgent = uaa.parse(requestHeaders);

        LOG.info("Result: {}", userAgent);
        assertTrue(userAgent.toString().contains("'Windows 7'"));
    }



}
