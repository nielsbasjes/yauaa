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

package nl.basjes.parse.useragent.servlet;

import io.swagger.v3.oas.annotations.tags.Tag;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.servlet.api.OutputType;
import nl.basjes.parse.useragent.servlet.exceptions.YauaaIsBusyStarting;
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
public class ParseService {

    private static final Logger LOG = LogManager.getLogger(ParseService.class);

    private static ParseService instance;

    private static void setInstance(ParseService newInstance) {
        instance = newInstance;
    }

    public ParseService() {
        setInstance(this);
    }

    private UserAgentAnalyzer  userAgentAnalyzer               = null;
    private long               initStartMoment;
    private boolean            userAgentAnalyzerIsAvailable    = false;
    private String             userAgentAnalyzerFailureMessage = null;

    public static UserAgentAnalyzer getUserAgentAnalyzer() {
        return instance.userAgentAnalyzer;
    }

    public static long getInitStartMoment() {
        return instance.initStartMoment;
    }

    public static boolean userAgentAnalyzerIsAvailable() {
        return instance.userAgentAnalyzerIsAvailable;
    }

    public static String getUserAgentAnalyzerFailureMessage() {
        return instance.userAgentAnalyzerFailureMessage;
    }

    @PostConstruct
    public void automaticStartup() {
        if (!userAgentAnalyzerIsAvailable && userAgentAnalyzerFailureMessage == null) {
            initStartMoment = System.currentTimeMillis();
            new Thread(() -> {
                try {
                    userAgentAnalyzer = UserAgentAnalyzer.newBuilder()
                        .hideMatcherLoadStats()
                        .addOptionalResources("file:UserAgents*/*.yaml")
                        .immediateInitialization()
                        .keepTests()
                        .build();
                    userAgentAnalyzerIsAvailable = true;
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
            }).start();
        }
    }

    @PreDestroy
    public void preDestroy() {
        if (userAgentAnalyzer != null) {
            UserAgentAnalyzer uaa = userAgentAnalyzer;
            // First we disable it for all uses.
            userAgentAnalyzer = null;
            userAgentAnalyzerIsAvailable = false;
            userAgentAnalyzerFailureMessage = "UserAgentAnalyzer has been destroyed.";
            // Then we actually wipe it.
            uaa.destroy();
        }
    }

    public static void ensureStartedForApis(OutputType outputType) {
        if (!instance.userAgentAnalyzerIsAvailable) {
            throw new YauaaIsBusyStarting(outputType);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(ParseService.class, args);
    }
}
