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
////import nl.basjes.parse.useragent.analyze.WordRangeVisitor.Range;
////import nl.basjes.parse.useragent.utils.Splitter;
////import nl.basjes.parse.useragent.utils.WordSplitter;
////import org.apache.commons.lang3.tuple.Pair;
////import org.openjdk.jmh.annotations.BenchmarkMode;
////import org.openjdk.jmh.annotations.Fork;
////import org.openjdk.jmh.annotations.Measurement;
////import org.openjdk.jmh.annotations.Mode;
////import org.openjdk.jmh.annotations.OutputTimeUnit;
////import org.openjdk.jmh.annotations.Scope;
////import org.openjdk.jmh.annotations.State;
////import org.openjdk.jmh.annotations.Warmup;
////import org.openjdk.jmh.runner.Runner;
////import org.openjdk.jmh.runner.RunnerException;
////import org.openjdk.jmh.runner.options.Options;
////import org.openjdk.jmh.runner.options.OptionsBuilder;
////
////import java.util.ArrayList;
////import java.util.List;
////import java.util.concurrent.TimeUnit;
//
////@Warmup(iterations = 10)
////@Measurement(iterations = 10)
////@Fork(1)
////@BenchmarkMode(Mode.AverageTime)
////@OutputTimeUnit(TimeUnit.NANOSECONDS)
////public class SplitterBenchmarks {
////
////    @State(Scope.Benchmark)
////    public static class ThreadState {
////        final WordSplitter splitter;
////        final List<Range> allRanges;
////        final List<Range> ranges1;
////        final List<Range> ranges2;
////        final List<Range> ranges3;
////        final List<Range> ranges4;
////        final List<Range> ranges5;
////        final List<Range> ranges6;
////        final List<Range> ranges7;
////        final List<Range> ranges8;
////        final List<Range> ranges9;
////
////        public ThreadState() {
////            splitter = WordSplitter.getInstance();
////            allRanges = new ArrayList<>(32);
////            allRanges.add(new Range(1, 1));
////            allRanges.add(new Range(1, 2));
////            allRanges.add(new Range(3, 4));
////            allRanges.add(new Range(2, 4));
////            allRanges.add(new Range(4, 5));
////            allRanges.add(new Range(5, 6));
////            allRanges.add(new Range(3, 5));
////            allRanges.add(new Range(4, 6));
////            allRanges.add(new Range(2, 2));
////            allRanges.add(new Range(1, 3));
////
////            ranges1 = allRanges.subList(1, 1);
////            ranges2 = allRanges.subList(1, 2);
////            ranges3 = allRanges.subList(1, 3);
////            ranges4 = allRanges.subList(1, 4);
////            ranges5 = allRanges.subList(1, 5);
////            ranges6 = allRanges.subList(1, 6);
////            ranges7 = allRanges.subList(1, 7);
////            ranges8 = allRanges.subList(1, 8);
////            ranges9 = allRanges.subList(1, 9);
////        }
////    }
////
////    static final String TEXT = "one two/3 four-4 five(some more)";
////
////    public void runDirect(Splitter splitter, List<Range> ranges) {
////        for (Range range : ranges) {
////            splitter.getSplitRange(TEXT, range);
////        }
////    }
////
////    public void runSplitList(Splitter splitter, List<Range> ranges) {
////        List<Pair<Integer, Integer>> splitList = splitter.createSplitList(TEXT);
////        for (Range range : ranges) {
////            splitter.getSplitRange(TEXT, splitList, range);
////        }
////    }
////
////    @Benchmark
////    public void directRange1(ThreadState state) {
////        runDirect(state.splitter, state.ranges1);
////    }
////
////    @Benchmark
////    public void splitLRange1(ThreadState state) {
////        runSplitList(state.splitter, state.ranges1);
////    }
////
////    @Benchmark
////    public void directRange2(ThreadState state) {
////        runDirect(state.splitter, state.ranges2);
////    }
////
////    @Benchmark
////    public void splitLRange2(ThreadState state) {
////        runSplitList(state.splitter, state.ranges2);
////    }
////
////    @Benchmark
////    public void directRange3(ThreadState state) {
////        runDirect(state.splitter, state.ranges3);
////    }
////
////    @Benchmark
////    public void splitLRange3(ThreadState state) {
////        runSplitList(state.splitter, state.ranges3);
////    }
////
////    @Benchmark
////    public void directRange4(ThreadState state) {
////        runDirect(state.splitter, state.ranges4);
////    }
////
////    @Benchmark
////    public void splitLRange4(ThreadState state) {
////        runSplitList(state.splitter, state.ranges4);
////    }
////
////    @Benchmark
////    public void directRange5(ThreadState state) {
////        runDirect(state.splitter, state.ranges5);
////    }
////
////    @Benchmark
////    public void splitLRange5(ThreadState state) {
////        runSplitList(state.splitter, state.ranges5);
////    }
////
////    @Benchmark
////    public void directRange6(ThreadState state) {
////        runDirect(state.splitter, state.ranges6);
////    }
////
////    @Benchmark
////    public void splitLRange6(ThreadState state) {
////        runSplitList(state.splitter, state.ranges6);
////    }
////
////    @Benchmark
////    public void directRange7(ThreadState state) {
////        runDirect(state.splitter, state.ranges7);
////    }
////
////    @Benchmark
////    public void splitLRange7(ThreadState state) {
////        runSplitList(state.splitter, state.ranges7);
////    }
////
////    @Benchmark
////    public void directRange8(ThreadState state) {
////        runDirect(state.splitter, state.ranges8);
////    }
////
////    @Benchmark
////    public void splitLRange8(ThreadState state) {
////        runSplitList(state.splitter, state.ranges8);
////    }
////
////    @Benchmark
////    public void directRange9(ThreadState state) {
////        runDirect(state.splitter, state.ranges9);
////    }
////
////    @Benchmark
////    public void splitLRange9(ThreadState state) {
////        runSplitList(state.splitter, state.ranges9);
////    }
////
////    public static void main(String[] args) throws RunnerException {
////        Options opt = new OptionsBuilder()
////            .include(SplitterBenchmarks.class.getSimpleName())
////            .build();
////
////        new Runner(opt).run();
////    }
////}
