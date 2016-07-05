/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2016 Niels Basjes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package nl.basjes.parse.useragent;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestPerformance {
    private static final Logger LOG = LoggerFactory.getLogger(TestPerformance.class);

    @Test
    @Ignore
    public void performanceTestNoCache() {
        runPerformanceTest(false,    1000);
    }

    @Test
    @Ignore
    public void performanceTestCached() {
        runPerformanceTest(true, 10000000);
    }

    private void runPerformanceTest(boolean cached, long count) {
        UserAgentAnalyzer uaa = new UserAgentAnalyzer();
        if (!cached) {
            uaa.disableCaching();
        }

        UserAgent agent = new UserAgent("Mozilla/5.0 (Linux; Android 6.0; Nexus 6 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.76 Mobile Safari/537.36");

        // Preheat the jit compiler.
        for (int i = 0; i < 500; i++) {
            agent.reset();
            uaa.parse(agent);
        }

        long start = System.nanoTime();
        LOG.info("Start @ {}", start);
        // Try to repeatedly parse a 'normal' agent without any caching.
        for (int i = 0; i < count; i++) {
            agent.reset();
            uaa.parse(agent);
        }
        long stop = System.nanoTime();
        LOG.info("Stop  @ {}", stop);
        LOG.info("Did {} in {} ns ({} sec)--> {}/sec", count, stop-start, (stop-start)/1000000000 , (1000000000*count)/(stop-start));
    }


}
