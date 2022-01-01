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

public class CheckLoggingDependencies {

    public static class InvalidLoggingDependencyException extends RuntimeException {
        public InvalidLoggingDependencyException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    void verifyLog4j2Dependencies() {
        try {
            org.apache.logging.log4j.LogManager.getLogger("Verify Logging Dependencies (Log4J2)");
        } catch (NoClassDefFoundError e) {
            throw new InvalidLoggingDependencyException("Failed loading Log4J2 (Not present)", e);
        }
    }

    void verifySlf4jDependencies() {
        try {
            // Check slf4j (used by PublicSuffixMatcherLoader from httpclient)
            org.slf4j.LoggerFactory.getLogger("Verify Logging Dependencies (Slf4j)");
        } catch (NoClassDefFoundError e) {
            throw new InvalidLoggingDependencyException("Failed testing SLF4J (Not present)", e);
        }
    }

    void verifyJCLDependencies() {
        try {
            // Check JCL (used by PathMatchingResourcePatternResolver from Springframework)
            org.apache.commons.logging.LogFactory.getLog("Verify Logging Dependencies (JCL)");
        } catch (NoClassDefFoundError e) {
            throw new InvalidLoggingDependencyException("Failed testing JCL (Not present)", e);
        }
    }

    /**
     * Checks if the logging dependencies needed for this library are available.
     * Throws an exception if something is wrong.
     */
    public static void verifyLoggingDependencies() {
        CheckLoggingDependencies ldt = new CheckLoggingDependencies();
        ldt.verifyLog4j2Dependencies();
        ldt.verifySlf4jDependencies();
        ldt.verifyJCLDependencies();
    }
}
