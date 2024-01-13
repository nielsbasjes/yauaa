/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2024 Niels Basjes
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

package nl.basjes.parse.useragent;

import nl.basjes.parse.useragent.config.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public interface AnalyzerPreHeater extends Analyzer {
    Logger LOG = LogManager.getFormatterLogger(AnalyzerPreHeater.class);

    List<TestCase> getPreheatTestCases();

    default List<TestCase> internalGetTestCasesForPreheat() {
        List<TestCase> result = getPreheatTestCases();
        if (result == null || result.isEmpty()) {
            result = PreHeatCases.USERAGENTS.stream()
                .map(ua -> new TestCase(ua, "PreHeat fallback"))
                .collect(Collectors.toList());
        }
        return result;
    }

    /**
     * Runs all testcases once to heat up the JVM.
     * @return Number of actually done testcases.
     */
    default long preHeat() {
        List<TestCase> testCases = internalGetTestCasesForPreheat();
        return preHeat(testCases, testCases.size(), true);
    }
    /**
     * Runs the number of specified testcases to heat up the JVM.
     * @param preheatIterations Number of desired tests to run.
     * @return Number of actually done testcases.
     */
    default long preHeat(long preheatIterations) {
        return preHeat(preheatIterations, true);
    }

    long MAX_PRE_HEAT_ITERATIONS = 1_000_000L;

    /**
     * Runs the number of specified testcases to heat up the JVM.
     * @param preheatIterations Number of desired tests to run.
     * @param log Enable logging?
     * @return Number of actually done testcases.
     */
    default long preHeat(long preheatIterations, boolean log) {
        return preHeat(internalGetTestCasesForPreheat(), preheatIterations, log);
    }

    default long preHeat(List<TestCase> testCases, long preheatIterations, boolean log) {
        if (testCases == null || testCases.isEmpty()) {
            LOG.fatal("NO PREHEAT WAS DONE. This should never occur.");
            return 0;
        }
        if (preheatIterations <= 0) {
            LOG.warn("NO PREHEAT WAS DONE. Simply because you asked for %d to run.", preheatIterations);
            return 0;
        }
        if (preheatIterations > MAX_PRE_HEAT_ITERATIONS) {
            LOG.warn("NO PREHEAT WAS DONE. Simply because you asked for too many (%d > %d) to run.", preheatIterations, MAX_PRE_HEAT_ITERATIONS);
            return 0;
        }
        if (log) {
            LOG.info("Preheating JVM by running %d testcases.", preheatIterations);
        }
        long remainingIterations = preheatIterations;
        long syntaxErrors = 0;
        long startTime = System.nanoTime();
        while (remainingIterations > 0) {
            for (TestCase testCase : testCases) {
                remainingIterations--;
                // Calculate and use result to guarantee not optimized away.
                if(parse(testCase.getHeaders()).hasSyntaxError()) {
                    syntaxErrors++;
                }
                if (remainingIterations <= 0) {
                    break;
                }
            }
        }
        long stopTime = System.nanoTime();

        if (log) {
            LOG.info("Preheating JVM completed. Parsing %d testcases took %d ms (average %6.6f ms)",
                preheatIterations, (stopTime-startTime)/1_000_000, (stopTime-startTime)/1_000_000.0/preheatIterations);
            // We only have this next line to avoid the optimizer not running the parsing
            LOG.trace("Of those %d had a Syntax Error", syntaxErrors);
        }
        return preheatIterations;
    }
}
