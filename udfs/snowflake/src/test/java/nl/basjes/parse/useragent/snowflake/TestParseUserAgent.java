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

package nl.basjes.parse.useragent.snowflake;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestParseUserAgent {

    @Test
    void testBasic() {
        // This is an edge case where the webview fields are calulcated AND wiped again.
        String userAgent = "Mozilla/5.0 (Linux; Android 5.1.1; KFFOWI Build/LMY47O) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Version/4.0 Chrome/41.51020.2250.0246 Mobile Safari/537.36 cordova-amazon-fireos/3.4.0 AmazonWebAppPlatform/3.4.0;2.0";

        for (int i = 0; i < 100000; i++) {
            Map<String, String> row = ParseUserAgent.parse(userAgent);
            assertEquals("Tablet",       row.get("DeviceClass"));
            assertEquals("FireOS 3.4.0", row.get("OperatingSystemNameVersion"));
        }
    }

}
