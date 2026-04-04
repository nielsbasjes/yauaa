/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2026 Niels Basjes
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

package nl.basjes.parse.useragent.servlet;

import lombok.Getter;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.utils.YauaaVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class ParseService {

    private static final Logger LOG = LogManager.getLogger(ParseService.class);

    @Getter
    private final UserAgentAnalyzer userAgentAnalyzer;

    public ParseService() {
        LOG.info("Yauaa: Starting {}", YauaaVersion.getVersion());
        userAgentAnalyzer = UserAgentAnalyzer.newBuilder()
            .showMatcherLoadStats()
            .addOptionalResources("file:UserAgents*/*.yaml")
            .addOptionalResources("classpath*:UserAgents-*/*.yaml")
            .immediateInitialization()
            .keepTests()
            .build();
    }

}
