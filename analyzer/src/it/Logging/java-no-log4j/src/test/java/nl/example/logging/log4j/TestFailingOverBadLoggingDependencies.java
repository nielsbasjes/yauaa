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

package nl.example.logging.log4j;

import nl.basjes.parse.useragent.UserAgentAnalyzerDirect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TestFailingOverBadLoggingDependencies {
    @Test
    void failOverMissingLoggingUsage() {
        // Since Log4j is used immediately in the UserAgentAnalyzerDirect class this
        // test fails before the actual check code is run. So no clean error message.
        assertThrows(NoClassDefFoundError.class,
            () -> UserAgentAnalyzerDirect.newBuilder().delayInitialization().build());
    }
}
