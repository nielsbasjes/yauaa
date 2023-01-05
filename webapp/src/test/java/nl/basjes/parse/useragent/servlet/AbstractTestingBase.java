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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;

import static io.restassured.RestAssured.given;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

abstract class AbstractTestingBase {
    protected static final Logger LOG = LogManager.getLogger(AbstractTestingBase.class);

    boolean runTestsThatNeedResourceFiles = true;

    abstract int getPort();

    private static int attemptsRemaining = 50;

    static boolean isRunning = false;
    @Before
    public void ensureRunning() throws InterruptedException {
        if (!isRunning) {
            LOG.info("Status: Checking status...");

            while(attemptsRemaining > 0) {
                boolean live = given().port(getPort()).accept(TEXT_PLAIN_VALUE).get("/liveness").getBody().asString().equals("YES");
                if (live) {
                    break;
                }
                LOG.warn("Status: Dead");
                // noinspection BusyWait
                Thread.sleep(100);  // NOSONAR java:S2925 Sleeping in a while loop is safe!
                attemptsRemaining--;
            }

            while(attemptsRemaining > 0) {
                boolean ready = given().port(getPort()).accept(TEXT_PLAIN_VALUE).get("/readiness").getBody().asString().equals("YES");
                if (ready) {
                    break;
                }
                LOG.warn("Status: Alive (NOT yet ready for processing)");
                // noinspection BusyWait
                Thread.sleep(100);  // NOSONAR java:S2925 Sleeping in a while loop is safe!
                attemptsRemaining--;
            }

            if (attemptsRemaining <= 0) {
                LOG.fatal("Status: Stayed dead and not ready for too long.");
                throw new IllegalStateException("Unable to initialize the parser.");
            }

            LOG.info("Status: Alive and Ready for processing");
            isRunning = true;
        }
    }

    public static final String BASEURL                     = "/yauaa/v1";
    public static final String NONSENSE_USER_AGENT         = "Niels Basjes/42";
    public static final String TEST_USER_AGENT             = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.89 Safari/537.36";
    public static final String EXPECTED_AGENTNAMEVERSION   = "Chrome 84.0.4147.89";

}
