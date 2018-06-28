/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2018 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.useragent.profile;

import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertTrue;

public class TestPerformance {
    private static final Logger LOG = LoggerFactory.getLogger(TestPerformance.class);

    @Ignore
    @Test
    public void validateAllPredefinedBrowsersPerformance() {
        UserAgentAnalyzerTester uaa =
            UserAgentAnalyzerTester.newBuilder()
            .showMatcherLoadStats()
            .immediateInitialization()
            .build();
        Assert.assertTrue(uaa.runTests(false, false, null, true, true));
    }

    @Test
    public void checkAllPossibleFieldsFastSpeed() {
        long start = System.nanoTime();
        LOG.info("Create analyzer");
        UserAgentAnalyzer uaa = UserAgentAnalyzer
            .newBuilder()
            .delayInitialization()
            .build();
        long loadTime = System.nanoTime();
        long msecs = (loadTime - start) / 1000000;
        LOG.info("Analyzer loaded after {}ms", msecs);
        assertTrue("Loading the analyzer did not finish within 2 seconds (is was " + msecs + "ms).", msecs < 2000);

        LOG.info("List fieldnames");
        uaa.getAllPossibleFieldNamesSorted()
            .forEach(LOG::debug);
        long stop = System.nanoTime();
        msecs = (stop - loadTime) / 1000000;
        LOG.info("Duration {}ms", msecs);
        assertTrue("Listing the fields did not finish within 200 ms (is was "+msecs+"ms).", msecs < 200);
    }

}
