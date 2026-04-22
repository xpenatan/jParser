package com.github.xpenatan.jParser.example.app;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Comprehensive native bridge benchmark comparing JNI, FFM, and TeaVM performance.
 * <p>
 * Each benchmark measures the cost of crossing the Java → native boundary for
 * different parameter types. The native work is intentionally trivial so the
 * measurement isolates bridge overhead, not native computation.
 * <p>
 * Results are printed to stdout. Run this on desktop with either lib-core (JNI)
 * or lib-ffm (FFM) on the classpath to compare. On TeaVM the iteration count
 * is reduced automatically.
 */
public class NativeBridgeBenchmark {

    // ---------------------------------------------------------------------------
    // Configuration
    // ---------------------------------------------------------------------------

    /** Number of warm-up iterations (allows JIT to stabilise). */
    private static final int WARMUP_ITERATIONS = 2;
    /** Number of timed iterations whose median is reported. */
    private static final int TIMED_ITERATIONS = 3;

    /** Calls per timed iteration – adjusted for TeaVM. */
    private static long CALLS_PER_ITERATION = 1_000_000L;

    /** Shared scenario definitions and native resources. */
    private static NativeBridgeWorkloads workloads;

    /** Collected results for CSV export. */
    private static final ArrayList<BenchmarkResult> results = new ArrayList<>();

    /** Simple data holder for one benchmark measurement. */
    public static class BenchmarkResult {
        public final String label;
        public final double medianMs;
        public final double mcallsPerSec;

        public BenchmarkResult(String label, double medianMs, double mcallsPerSec) {
            this.label = label;
            this.medianMs = medianMs;
            this.mcallsPerSec = mcallsPerSec;
        }
    }

    // ---------------------------------------------------------------------------
    // Public entry point
    // ---------------------------------------------------------------------------

    public static void run() {
        if(Gdx.app != null && Gdx.app.getType() == Application.ApplicationType.WebGL) {
            CALLS_PER_ITERATION = 500_000L;
        }

        System.out.println("=======================================================");
        System.out.println("  Native Bridge Benchmark");
        System.out.println("  Warm-up rounds : " + WARMUP_ITERATIONS);
        System.out.println("  Timed rounds   : " + TIMED_ITERATIONS);
        System.out.println("  Calls/round    : " + CALLS_PER_ITERATION);
        System.out.println("=======================================================");

        results.clear();

        workloads = new NativeBridgeWorkloads();
        try {
            runAllBenchmarks();
        } finally {
            workloads.dispose();
            workloads = null;
        }

        System.out.println("=======================================================");
        System.out.println("  Benchmark complete");
        System.out.println("=======================================================");

        // Write CSV if output path is specified via system property
        String outputPath = System.getProperty("benchmark.output");
        if(outputPath != null && !outputPath.isEmpty()) {
            writeCsv(outputPath);
        }
    }

    /**
     * Returns the collected results from the last {@link #run()} call.
     */
    public static ArrayList<BenchmarkResult> getResults() {
        return results;
    }

    /**
     * Writes collected results to a CSV file.
     */
    private static void writeCsv(String path) {
        try(PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            pw.println("# warmup=" + WARMUP_ITERATIONS);
            pw.println("# timed=" + TIMED_ITERATIONS);
            pw.println("# calls=" + CALLS_PER_ITERATION);
            pw.println("label,medianMs,mcallsPerSec");
            for(BenchmarkResult r : results) {
                pw.printf("%s,%.4f,%.4f%n", r.label, r.medianMs, r.mcallsPerSec);
            }
            System.out.println("  Results written to: " + path);
        } catch(IOException e) {
            System.err.println("  Failed to write CSV: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------------------
    // Benchmark runner
    // ---------------------------------------------------------------------------

    private static void runAllBenchmarks() {
        for(NativeBridgeWorkloads.Scenario scenario : workloads.getScenarios()) {
            benchmark(scenario.label, () -> scenario.run(CALLS_PER_ITERATION));
        }
    }

    /**
     * Runs warm-up + timed iterations, then prints the median time and
     * throughput (million calls/sec).
     */
    private static void benchmark(String label, Runnable body) {
        // Warm-up
        for(int i = 0; i < WARMUP_ITERATIONS; i++) {
            body.run();
        }

        // Timed
        long[] times = new long[TIMED_ITERATIONS];
        for(int i = 0; i < TIMED_ITERATIONS; i++) {
            long t0 = System.nanoTime();
            body.run();
            long t1 = System.nanoTime();
            times[i] = t1 - t0;
        }

        // Sort for median
        java.util.Arrays.sort(times);
        long medianNs = times[TIMED_ITERATIONS / 2];
        double medianMs = medianNs / 1_000_000.0;
        double mcallsPerSec = (CALLS_PER_ITERATION / (medianNs / 1_000_000_000.0)) / 1_000_000.0;

        // Trim label for display but keep original for CSV
        String trimmed = label.trim();
        results.add(new BenchmarkResult(trimmed, medianMs, mcallsPerSec));

        System.out.printf("  %-38s %8.1f ms   %8.2f Mcalls/s%n", label, medianMs, mcallsPerSec);
    }
}








