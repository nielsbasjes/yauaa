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

package nl.example.logging.slf4j;

import nl.basjes.parse.useragent.CheckLoggingDependencies;
import nl.basjes.parse.useragent.CheckLoggingDependencies.InvalidLoggingDependencyException;
import nl.basjes.parse.useragent.UserAgentAnalyzerDirect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestFailingOverBadLoggingDependencies {
    @Test
    void failOverMissingLoggingCheck() {
        Exception exception =
            assertThrows(InvalidLoggingDependencyException.class,
                CheckLoggingDependencies::verifyLoggingDependencies);
        assertEquals("Failed testing SLF4J (Not present)", exception.getMessage());
    }

    @Test
    void failOverMissingLoggingUsage() {
        Exception exception =
            assertThrows(InvalidLoggingDependencyException.class,
                () -> UserAgentAnalyzerDirect.newBuilder().delayInitialization().build());
        assertEquals("Failed testing SLF4J (Not present)", exception.getMessage());
    }
}
