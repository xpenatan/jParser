package com.github.xpenatan.jParser.example.app;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class EnumBenchmark {

    private static final int WARMUP_ITERATIONS = 1;
    private static final int TIMED_ITERATIONS = 5;
    private static long size = 3_000_000_000L;
    private static final ArrayList<Result> results = new ArrayList<>();

    public static class Result {
        public final String label;
        public final double medianMs;
        public final double mopsPerSec;

        public Result(String label, double medianMs, double mopsPerSec) {
            this.label = label;
            this.medianMs = medianMs;
            this.mopsPerSec = mopsPerSec;
        }
    }

    static void test() {
        if(Gdx.app.getType() == Application.ApplicationType.WebGL) {
            size = 200_000_000L;
        }

        System.out.println("=======================================================");
        System.out.println("  Enum Benchmark");
        System.out.println("  Warm-up rounds : " + WARMUP_ITERATIONS);
        System.out.println("  Timed rounds   : " + TIMED_ITERATIONS);
        System.out.println("  Iterations     : " + size);
        System.out.println("=======================================================");

        results.clear();
        benchmark("bitfield", EnumBenchmark::runBitflags);

        // Keep existing placeholders to preserve source compatibility.
        benchmarkTestEnumLib();
        benchmarkTestEnumLibCustom_1();
        benchmarkTestEnumLibCustom_2();

        String outputPath = System.getProperty("benchmark.enum.output");
        if(outputPath != null && !outputPath.isEmpty()) {
            writeCsv(outputPath);
        }
    }

    static enum Flag { A, B, C, D, E, F, G }

    static void benchmarkEnumSet() {
        System.gc();
        long beg = System.nanoTime();
        Set<Flag> a = EnumSet.of(Flag.A, Flag.B, Flag.G);
        for (long i = 0; i < size; i++) {
            Set<Flag> b = EnumSet.of(Flag.A, Flag.B, Flag.G);
            assert a.equals(b);
        }
        long end = System.nanoTime();
        System.out.println((end - beg)/1e9 + "\t\tEnumSet");
    }

    static void benchmarkHashSet() {
        System.gc();
        long beg = System.nanoTime();
        Set<Flag> a = new HashSet<>(Arrays.asList(Flag.A, Flag.B, Flag.G));
        for (long i = 0; i < size; i++) {
            Set<Flag> b = new HashSet<>(Arrays.asList(Flag.A, Flag.B, Flag.G));
            assert a.equals(b);
        }
        long end = System.nanoTime();
        System.out.println((end - beg)/1e9 + "\t\tHashSet");
    }

    static void benchmarkHashSetCache() {
        System.gc();
        long beg = System.nanoTime();
        Set<Flag> a = new HashSet<>(Arrays.asList(Flag.A, Flag.B, Flag.G));
        Set<Flag> b = new HashSet<>();
        for (long i = 0; i < size; i++) {
            b.clear();
            b.add(Flag.A);
            b.add(Flag.B);
            b.add(Flag.G);
            assert a.equals(b);
        }
        long end = System.nanoTime();
        System.out.println((end - beg)/1e9 + "\t\tHashSetCache");
    }

    static final int A = 1 << 0;
    static final int B = 1 << 1;
    static final int C = 1 << 2;
    static final int D = 1 << 3;
    static final int E = 1 << 4;
    static final int F = 1 << 5;
    static final int G = 1 << 6;

    private static void runBitflags() {
        System.gc();
        long a = A | B | G;
        for (long i = 0; i < size; i++) {
            long b = A | B | G;
            assert a == b;
        }
    }

    private static void benchmark(String label, Runnable body) {
        for(int i = 0; i < WARMUP_ITERATIONS; i++) {
            body.run();
        }

        long[] times = new long[TIMED_ITERATIONS];
        for(int i = 0; i < TIMED_ITERATIONS; i++) {
            long t0 = System.nanoTime();
            body.run();
            long t1 = System.nanoTime();
            times[i] = t1 - t0;
        }

        java.util.Arrays.sort(times);
        long medianNs = times[TIMED_ITERATIONS / 2];
        double medianMs = medianNs / 1_000_000.0;
        double mopsPerSec = (size / (medianNs / 1_000_000_000.0)) / 1_000_000.0;

        results.add(new Result(label, medianMs, mopsPerSec));
        System.out.printf("  %-28s %10.1f ms   %8.2f Mops/s%n", label, medianMs, mopsPerSec);
    }

    private static void writeCsv(String path) {
        try(PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            pw.println("# warmup=" + WARMUP_ITERATIONS);
            pw.println("# timed=" + TIMED_ITERATIONS);
            pw.println("# iterations=" + size);
            pw.println("label,medianMs,mopsPerSec");
            for(Result result : results) {
                pw.printf("%s,%.4f,%.4f%n", result.label, result.medianMs, result.mopsPerSec);
            }
            System.out.println("  Results written to: " + path);
        } catch(IOException e) {
            System.err.println("  Failed to write CSV: " + e.getMessage());
        }
    }

    static void benchmarkTestEnumLib() {
//        System.gc();
//        long beg = System.nanoTime();
//        int value = TestEnumLib.TEST_DEFAULT.or(TestEnumLib.TEST_FIRST).or(TestEnumLib.TEST_SECOND).getValue();
//        for (long i = 0; i < size; i++) {
//            int val = TestEnumLib.TEST_DEFAULT.or(TestEnumLib.TEST_FIRST).or(TestEnumLib.TEST_SECOND).getValue();
//            assert val == value;
//        }
//        long end = System.nanoTime();
//        System.out.println((end - beg)/1e9 + "\t\tTestEnumLib");
    }

    static void benchmarkTestEnumLibCustom_1() {
//        System.gc();
//        long beg = System.nanoTime();
//        int value11 = TestEnumLib.TEST_DEFAULT.getValue();
//        int value22 = TestEnumLib.TEST_FIRST.getValue();
//        int value33 = TestEnumLib.TEST_SECOND.getValue();
//        int value = value11 | value22 | value33;
//        for (long i = 0; i < size; i++) {
//            int value1 = TestEnumLib.TEST_DEFAULT.getValue();
//            int value2 = TestEnumLib.TEST_FIRST.getValue();
//            int value3 = TestEnumLib.TEST_SECOND.getValue();
//            int val = value1 | value2 | value3;
//            assert val == value;
//        }
//        long end = System.nanoTime();
//        System.out.println((end - beg)/1e9 + "\t\tTestEnumLibCustom1");
    }

    static void benchmarkTestEnumLibCustom_2() {
//        System.gc();
//        long beg = System.nanoTime();
//        int value1 = TestEnumLib.TEST_DEFAULT.getValue();
//        int value2 = TestEnumLib.TEST_FIRST.getValue();
//        int value3 = TestEnumLib.TEST_SECOND.getValue();
//        int value = value1 | value2 | value3;
//        for (long i = 0; i < size; i++) {
//            int val = value1 | value2 | value3;
//            assert val == value;
//        }
//        long end = System.nanoTime();
//        System.out.println((end - beg)/1e9 + "\t\tTestEnumLibCustom2");
    }
}
