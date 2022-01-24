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
package nl.basjes.parse.useragent.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.StringLayout;
import org.apache.logging.log4j.core.appender.WriterAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.CharArrayWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit rule for testing output to Log4j. Handy for verifying logging.
 * This sets up and tears down an Appender resource on a given Logger.
 */
public class LogMessagesScraper implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final String APPENDER_NAME = "LogMessageScraper";

    /**
     * Logged messages contains level and message only.
     * This allows us to test that level and message are set.
     */
    private static final String PATTERN = "%highlight{%d{ABSOLUTE} [%-5p] %-40c{1}:%5L: %m%n%throwable}{FATAL=bright red reverse, ERROR=bright red, WARN=bright yellow, INFO=default, DEBUG=cyan, TRACE=magenta}";

    private final List<Logger> loggers;
    private Appender appender;
    private final CharArrayWriter outContent = new CharArrayWriter();

    public LogMessagesScraper(Class<?>... classes) {
        loggers = new ArrayList<>();
        for (Class<?> clazz : classes) {
            org.apache.logging.log4j.Logger logger = LogManager.getLogger(clazz);
            // Only the Logger in log4j2-core support what we need here.
            // The clean Logger interface in the api does not.
            assertTrue(logger instanceof Logger);
            this.loggers.add((Logger) logger);
        }
    }

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) {
        StringLayout layout = PatternLayout.newBuilder().withPattern(PATTERN).build();
        appender = WriterAppender.newBuilder()
            .setTarget(outContent)
            .setLayout(layout)
            .setName(APPENDER_NAME).build();
        appender.start();
        for (Logger logger : loggers) {
            logger.addAppender(appender);
        }
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        for (Logger logger : loggers) {
            logger.removeAppender(appender);
        }
    }

    public String getOutput() {
        return outContent.toString();
    }
}
