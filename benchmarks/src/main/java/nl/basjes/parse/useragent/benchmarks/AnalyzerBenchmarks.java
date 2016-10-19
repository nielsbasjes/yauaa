package nl.basjes.parse.useragent.benchmarks;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AnalyzerBenchmarks {

    @State(Scope.Thread)
    public static class ThreadState {
        private Iterator<String> testUserAgentsIterator;
        List<String> testUserAgents = new ArrayList<>();
        UserAgentAnalyzer uaa;

        public String getNextTestCase() {
            if (!testUserAgentsIterator.hasNext()) {
                testUserAgentsIterator = testUserAgents.iterator();
            }
            return testUserAgentsIterator.next();
        }

        public ThreadState() {
            uaa = new UserAgentAnalyzer();
            uaa.setCacheSize(0);
            testUserAgents.add("Mozilla/5.0 (Linux; Android 6.0; Nexus 6 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/46.0.2490.76 Mobile Safari/537.36");
            testUserAgentsIterator = testUserAgents.iterator();
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public UserAgent testMethod(ThreadState state) {
        return state.uaa.parse(new UserAgent(state.getNextTestCase()));
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(AnalyzerBenchmarks.class.getSimpleName())
            .warmupIterations(5)
            .measurementIterations(5)
            .forks(1)
            .build();

        new Runner(opt).run();
    }
}
