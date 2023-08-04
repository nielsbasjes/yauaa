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

package nl.basjes.parse.useragent.servlet;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.servlet.api.OutputType;
import nl.basjes.parse.useragent.servlet.exceptions.YauaaIsBusyStarting;
import nl.basjes.parse.useragent.utils.YauaaVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Tag(name = "Yauaa", description = "Analyzing the useragents")
@SpringBootApplication
@RestController
@Getter
public class ParseService {

    private static final Logger LOG = LogManager.getLogger(ParseService.class);

    private UserAgentAnalyzer  userAgentAnalyzer               = null;
    private long               initStartMoment;
    private boolean            userAgentAnalyzerAvailable = false;
    private String             userAgentAnalyzerFailureMessage = null;

    @PostConstruct
    public void automaticStartup() {
        if (!userAgentAnalyzerAvailable && userAgentAnalyzerFailureMessage == null) {
            initStartMoment = System.currentTimeMillis();
            try {
                LOG.info("Yauaa: Starting {}", YauaaVersion.getVersion());
                userAgentAnalyzer = UserAgentAnalyzer.newBuilder()
                    .showMatcherLoadStats()
                    .addOptionalResources("file:UserAgents*/*.yaml")
                    .addOptionalResources("classpath*:UserAgents-*/*.yaml")
                    .immediateInitialization()
                    .keepTests()
                    .build();
                userAgentAnalyzerAvailable = true;
            } catch (Exception e) {
                userAgentAnalyzerFailureMessage =
                    e.getClass().getSimpleName() + "<br/>" +
                        e.getMessage().replace("\n", "<br/>");
                LOG.fatal("Fatal error during startup: {}\n" +
                        "=======================================================\n" +
                        "{}\n" +
                        "=======================================================\n",
                        e.getClass().getCanonicalName(), e.getMessage());
            }
        }
    }

    @PreDestroy
    public void preDestroy() {
        if (userAgentAnalyzer != null) {
            UserAgentAnalyzer uaa = userAgentAnalyzer;
            // First we disable it for all uses.
            userAgentAnalyzer = null;
            userAgentAnalyzerAvailable = false;
            userAgentAnalyzerFailureMessage = "UserAgentAnalyzer has been destroyed.";
            // Then we actually wipe it.
            uaa.destroy();
        }
    }

    public void ensureStartedForApis(OutputType outputType) {
        if (!userAgentAnalyzerAvailable) {
            throw new YauaaIsBusyStarting(outputType);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(ParseService.class, args);
    }

}
