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

import nl.basjes.parse.useragent.debug.AbstractUserAgentAnalyzerTester;
import nl.basjes.parse.useragent.debug.DebugUserAgent;
import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester;
import nl.basjes.parse.useragent.utils.LogMessagesScraper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestErrorHandlingLogOutput {

    private static final Logger LOG = LogManager.getLogger(TestErrorHandlingLogOutput.class);

    @RegisterExtension
    private final LogMessagesScraper logMessages = new LogMessagesScraper(
        DebugUserAgent.class,
        AbstractUserAgentAnalyzerTester.class
    );

    @Test
    void testConflictingRules() {
        assertDoesNotThrow(() -> {
            UserAgentAnalyzerTester uaa = UserAgentAnalyzerTester
                .newBuilder()
                .dropDefaultResources()
                .keepTests()
                .addResources("classpath*:BadDefinitions/ConflictingRules.yaml")
                .build();
            assertFalse(uaa.runTests(false, false));
            LOG.info("Analyzer:\n{}", uaa);
        });

        assertTrue(logMessages.getOutput().contains("Found different value for \"Name\" with SAME confidence 1:"));
    }
}
