/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2017 Niels Basjes
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

package nl.basjes.parse.useragent;

import nl.basjes.parse.useragent.debug.UserAgentAnalyzerTester;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestPerformance {
    private static final Logger LOG = LoggerFactory.getLogger(TestPerformance.class);

    @Ignore
    @Test
    public void validateAllPredefinedBrowsersPerformance() {
        UserAgentAnalyzerTester uaa = new UserAgentAnalyzerTester();
        uaa.setShowMatcherStats(true);
        uaa.initialize();
        Assert.assertTrue(uaa.runTests(false, false, null, true, true));
    }


    private static final long MEGABYTE = 1024L * 1024L;

    public static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }

    private void printMemoryUsage(int iterationsDone, long averageNanos) {
        // Get the Java runtime
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        LOG.info(String.format(
            "After %7d iterations and GC --> Used memory is %10d bytes (%5d MiB), Average time per parse %7d ns ( ~ %4.3f ms)",
            iterationsDone, memory, bytesToMegabytes(memory), averageNanos, averageNanos / 1000000.0));
    }

    @Ignore
    @Test
    public void checkForMemoryLeaks() {
        UserAgentAnalyzer uaa = UserAgentAnalyzer
            .newBuilder()
            .withoutCache()
//            .withField("OperatingSystemName")
//            .withField("OperatingSystemVersion")
//            .withField("DeviceClass")
            .hideMatcherLoadStats()
            .keepTests()
            .build();

        LOG.info("Init complete");
        int iterationsDone = 0;
        final int iterationsPerLoop = 1000;
        for (int i = 0; i < 100; i++) {
            long start = System.nanoTime();
            uaa.preHeat(iterationsPerLoop, false);
            long stop = System.nanoTime();
            iterationsDone += iterationsPerLoop;

            long averageNanos = (stop - start) / iterationsPerLoop;
            printMemoryUsage(iterationsDone, averageNanos);
        }
    }


    @Ignore
    @Test
    public void assesMemoryImpactPerFieldName() {
        // Get the Java runtime
        Runtime runtime = Runtime.getRuntime();
        UserAgentAnalyzer uaa = UserAgentAnalyzer
            .newBuilder()
            .hideMatcherLoadStats()
            .withoutCache()
            .keepTests()
            .build();

        uaa.preHeat();
        runtime.gc();
        uaa.preHeat();
        runtime.gc();

        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        LOG.error(String.format(
            "Querying for 'All fields' and GC --> Used memory is %10d bytes (%5d MiB)",
            memory, bytesToMegabytes(memory)));

        for (String fieldName : uaa.getAllPossibleFieldNamesSorted()) {
            uaa = UserAgentAnalyzer
                .newBuilder()
                .withoutCache()
                .withField(fieldName)
                .hideMatcherLoadStats()
                .keepTests()
                .build();

            uaa.preHeat();
            runtime.gc();
            uaa.preHeat();
            runtime.gc();

            // Calculate the used memory
            memory = runtime.totalMemory() - runtime.freeMemory();
            LOG.error(String.format(
                "Querying for %s and GC --> Used memory is %10d bytes (%5d MiB)",
                fieldName, memory, bytesToMegabytes(memory)));
        }
    }

}
