/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2018 Niels Basjes
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

package nl.basjes.parse.useragent.profile;

import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMemoryFootprint {

    private static final Logger LOG = LoggerFactory.getLogger(TestMemoryFootprint.class);

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
    public void checkForMemoryLeaks() { //NOSONAR: Do not complain about ignored performance test
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
    public void assesMemoryImpactPerFieldName() { //NOSONAR: Do not complain about ignored performance test
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


    private void printCurrentMemoryProfile(String label){
        // Get the Java runtime
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        LOG.info(String.format(
            "----- %8s: Used memory is %10d bytes (%5d MiB / %5d MiB)",
            label, memory, bytesToMegabytes(memory), bytesToMegabytes(runtime.totalMemory())));
    }

    @Ignore
    @Test
    public void profileMemoryFootprint() { //NOSONAR: Do not complain about ignored performance test
        printCurrentMemoryProfile("Before ");

        UserAgentAnalyzer uaa = UserAgentAnalyzer
            .newBuilder()
            .hideMatcherLoadStats()
            .withoutCache()
            .keepTests()
            .build();
        printCurrentMemoryProfile("Loaded ");

        uaa.initializeMatchers();
        printCurrentMemoryProfile("Init   ");

        Runtime.getRuntime().gc();
        printCurrentMemoryProfile("Post GC");

        uaa.setCacheSize(1000);
        uaa.preHeat();
        Runtime.getRuntime().gc();
        printCurrentMemoryProfile("Cache 1K");

        uaa.setCacheSize(10000);
        uaa.preHeat();
        Runtime.getRuntime().gc();
        printCurrentMemoryProfile("Cache 10K");

        uaa.dropTests();
        Runtime.getRuntime().gc();
        printCurrentMemoryProfile("NoTest ");

    }




}
