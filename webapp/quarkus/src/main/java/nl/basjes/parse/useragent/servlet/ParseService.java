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

import io.quarkus.runtime.annotations.RegisterForReflection;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.Version;
import nl.basjes.parse.useragent.servlet.api.OutputType;
import nl.basjes.parse.useragent.servlet.exceptions.YauaaIsBusyStarting;
import nl.basjes.parse.useragent.utils.YauaaVersion;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Application;

//@SpringBootApplication
//@RestController
// See https://quarkus.io/guides/cache#going-native
@RegisterForReflection(
    classNames = {
        "com.github.benmanes.caffeine.cache.PSMS",
        "com.github.benmanes.caffeine.cache.SSMS"
    },
    targets = {
        org.apache.logging.log4j.message.ReusableMessageFactory.class,
        org.apache.logging.log4j.message.DefaultFlowMessageFactory.class
    }
)
@OpenAPIDefinition(
    info = @Info(
        title="Yauaa",
        version = Version.PROJECT_VERSION,
        contact = @Contact(
            name = "Yauaa website",
            url = "https://yauaa.basjes.nl"),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"))
)
@ApplicationScoped
public class ParseService extends Application {

    private static final Logger LOG = Logger.getLogger(ParseService.class);

    private UserAgentAnalyzer  userAgentAnalyzer               = null;
    private long               initStartMoment;
    private boolean            userAgentAnalyzerIsAvailable    = false;
    private String             userAgentAnalyzerFailureMessage = null;

    public UserAgentAnalyzer getUserAgentAnalyzer() {
        return userAgentAnalyzer;
    }

    public long getInitStartMoment() {
        return initStartMoment;
    }

    public boolean userAgentAnalyzerIsAvailable() {
        return userAgentAnalyzerIsAvailable;
    }

    public String getUserAgentAnalyzerFailureMessage() {
        return userAgentAnalyzerFailureMessage;
    }

    @PostConstruct
    public void automaticStartup() {
        if (!userAgentAnalyzerIsAvailable && userAgentAnalyzerFailureMessage == null) {
            initStartMoment = System.currentTimeMillis();
            try {
                LOG.info("Yauaa: Starting " + YauaaVersion.getVersion());
                userAgentAnalyzer = UserAgentAnalyzer.newBuilder()
                    .hideMatcherLoadStats()
                    .addOptionalResources("file:UserAgents*/*.yaml")
                    .addOptionalResources("classpath*:UserAgents-*/*.yaml")
                    .immediateInitialization()
                    .keepTests()
                    .build();
                userAgentAnalyzerIsAvailable = true;
            } catch (Exception | Error e) {
                // Extensive error logging
                StringBuilder msgBuilder = new StringBuilder();

                Throwable t = e;
                while (t != null) {
                    msgBuilder.append("==========================================================<br/>\n");
                    msgBuilder.append(t.getClass().getSimpleName()).append("<br/>\n");

                    String message = t.getMessage();
                    if (message != null) {
                        msgBuilder.append(message.replace("\n", "<br/>\n"));
                    }

                    for (StackTraceElement stackTraceElement : t.getStackTrace()) {
                        msgBuilder.append("\tat ").append(stackTraceElement).append("<br/>\n");
                    }
                    t = t.getCause();
                }

                userAgentAnalyzerFailureMessage = msgBuilder.toString();

                LOG.error(
                    "Fatal error during startup: "+e.getClass().getCanonicalName() + "\n" +
                    "=======================================================\n" +
                    userAgentAnalyzerFailureMessage.replace("<br/>","")+
                    "=======================================================" );
            }
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

    public void ensureStartedForApis(OutputType outputType) {
        if (!userAgentAnalyzerIsAvailable) {
            throw new YauaaIsBusyStarting(outputType);
        }
    }
}
