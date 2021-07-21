/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2021 Niels Basjes
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

import nl.basjes.parse.useragent.analyze.Analyzer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface AnalyzerPreHeater extends Analyzer {
    Logger LOG = LogManager.getLogger(AnalyzerPreHeater.class);

    /**
     * Runs all testcases once to heat up the JVM.
     * @return Number of actually done testcases.
     */
    default long preHeat() {
        return preHeat(PreHeatCases.USERAGENTS.size(), true);
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
        if (PreHeatCases.USERAGENTS.isEmpty()) {
            LOG.warn("NO PREHEAT WAS DONE. This should never occur.");
            return 0;
        }
        if (preheatIterations <= 0) {
            LOG.warn("NO PREHEAT WAS DONE. Simply because you asked for {} to run.", preheatIterations);
            return 0;
        }
        if (preheatIterations > MAX_PRE_HEAT_ITERATIONS) {
            LOG.warn("NO PREHEAT WAS DONE. Simply because you asked for too many ({} > {}) to run.", preheatIterations, MAX_PRE_HEAT_ITERATIONS);
            return 0;
        }
        if (log) {
            LOG.info("Preheating JVM by running {} testcases.", preheatIterations);
        }
        long remainingIterations = preheatIterations;
        long goodResults = 0;
        while (remainingIterations > 0) {
            for (String userAgentString : PreHeatCases.USERAGENTS) {
                remainingIterations--;
                // Calculate and use result to guarantee not optimized away.
                if(!parse(userAgentString).hasSyntaxError()) {
                    goodResults++;
                }
                if (remainingIterations <= 0) {
                    break;
                }
            }
        }
        if (log) {
            LOG.info("Preheating JVM completed. ({} of {} were proper results)", goodResults, preheatIterations);
        }
        return preheatIterations;
    }
}
