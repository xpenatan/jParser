package com.github.xpenatan.jParser.example.app;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.github.xpenatan.jParser.example.testlib.TestAttributeClass;
import com.github.xpenatan.jParser.example.testlib.TestBufferManualClass;
import com.github.xpenatan.jParser.example.testlib.TestMethodClass;
import com.github.xpenatan.jParser.example.testlib.TestObjectClass;
import com.github.xpenatan.jparser.idl.helper.IDLIntArray;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

    // Reusable native objects – allocated once to avoid measuring allocation.
    private static TestMethodClass methodObj;
    private static TestObjectClass objectA;
    private static TestObjectClass objectB;
    private static TestAttributeClass attrObj;
    private static TestBufferManualClass bufferObj;
    private static IDLIntArray intArray;
    private static ByteBuffer byteBuffer;

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

        allocateResources();
        try {
            runAllBenchmarks();
        } finally {
            freeResources();
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
        benchmark("void(int)               ", NativeBridgeBenchmark::benchSetInt);
        benchmark("void(float, bool)       ", NativeBridgeBenchmark::benchSetFloatBool);
        benchmark("void(int,int,float,float,bool)", NativeBridgeBenchmark::benchSetManyPrimitives);
        benchmark("int getter              ", NativeBridgeBenchmark::benchGetInt);
        benchmark("float getter            ", NativeBridgeBenchmark::benchGetFloat);
        benchmark("void(String)            ", NativeBridgeBenchmark::benchSetString);
        benchmark("String getter           ", NativeBridgeBenchmark::benchGetString);
        benchmark("void(Obj*,Obj*,Obj&,Obj&)", NativeBridgeBenchmark::benchObjectPassing);
        benchmark("Object* getter          ", NativeBridgeBenchmark::benchObjectReturn);
        benchmark("attribute set int       ", NativeBridgeBenchmark::benchAttrSetInt);
        benchmark("attribute get int       ", NativeBridgeBenchmark::benchAttrGetInt);
        benchmark("attribute set float     ", NativeBridgeBenchmark::benchAttrSetFloat);
        benchmark("attribute get float     ", NativeBridgeBenchmark::benchAttrGetFloat);
        benchmark("IDLIntArray set+get     ", NativeBridgeBenchmark::benchIDLIntArray);
        benchmark("ByteBuffer update (256B)", NativeBridgeBenchmark::benchByteBuffer);
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

    // ---------------------------------------------------------------------------
    // Resource lifecycle
    // ---------------------------------------------------------------------------

    private static void allocateResources() {
        methodObj = new TestMethodClass();
        objectA = new TestObjectClass();
        objectB = new TestObjectClass();
        attrObj = new TestAttributeClass();
        bufferObj = new TestBufferManualClass();
        intArray = new IDLIntArray(64);

        byteBuffer = ByteBuffer.allocateDirect(256);
        byteBuffer.order(ByteOrder.nativeOrder());

        // Pre-populate so native reads valid data
        objectA.set_intValue01(42);
        objectA.set_floatValue01(3.14f);
        objectB.set_intValue01(99);
        objectB.set_floatValue01(2.71f);
        methodObj.setMethod05("hello");
        for(int i = 0; i < 64; i++) {
            intArray.setValue(i, i);
        }
        for(int i = 0; i < 256; i++) {
            byteBuffer.put(i, (byte) i);
        }
    }

    private static void freeResources() {
        intArray.dispose();
        attrObj.dispose();
        objectB.dispose();
        objectA.dispose();
        methodObj.dispose();
        // bufferObj has no C++ allocation to free (extends IDLBase with 0 addr)
    }

    // ---------------------------------------------------------------------------
    // Individual benchmarks
    // ---------------------------------------------------------------------------

    /** Pass a single int primitive to native. */
    private static void benchSetInt() {
        for(long i = 0; i < CALLS_PER_ITERATION; i++) {
            methodObj.setMethod01((int) i);
        }
    }

    /** Pass float + boolean to native. */
    private static void benchSetFloatBool() {
        for(long i = 0; i < CALLS_PER_ITERATION; i++) {
            methodObj.setMethod02(1.5f, true);
        }
    }

    /** Pass 5 primitives (int, int, float, float, bool) in one call. */
    private static void benchSetManyPrimitives() {
        for(long i = 0; i < CALLS_PER_ITERATION; i++) {
            methodObj.setMethod03(1, 2, 3.0f, 4.0f, true);
        }
    }

    /** Return a single int from native. */
    private static void benchGetInt() {
        int sink = 0;
        for(long i = 0; i < CALLS_PER_ITERATION; i++) {
            sink += methodObj.getIntValue01();
        }
        preventOptimisation(sink);
    }

    /** Return a single float from native. */
    private static void benchGetFloat() {
        float sink = 0;
        for(long i = 0; i < CALLS_PER_ITERATION; i++) {
            sink += methodObj.getFloatValue01();
        }
        preventOptimisation(sink);
    }

    /** Pass a Java String to native. */
    private static void benchSetString() {
        for(long i = 0; i < CALLS_PER_ITERATION; i++) {
            methodObj.setMethod05("benchmark");
        }
    }

    /** Return a String (via IDLString) from native. */
    private static void benchGetString() {
        for(long i = 0; i < CALLS_PER_ITERATION; i++) {
            methodObj.getStrValue01().data();
        }
    }

    /** Pass multiple object pointers and references to native. */
    private static void benchObjectPassing() {
        for(long i = 0; i < CALLS_PER_ITERATION; i++) {
            methodObj.setMethod06(objectA, objectB, objectA, objectB);
        }
    }

    /** Return an object pointer from native. */
    private static void benchObjectReturn() {
        for(long i = 0; i < CALLS_PER_ITERATION; i++) {
            methodObj.getPointerObject02();
        }
    }

    /** Set an int attribute on a native object. */
    private static void benchAttrSetInt() {
        for(long i = 0; i < CALLS_PER_ITERATION; i++) {
            attrObj.set_intValue01((int) i);
        }
    }

    /** Get an int attribute from a native object. */
    private static void benchAttrGetInt() {
        int sink = 0;
        for(long i = 0; i < CALLS_PER_ITERATION; i++) {
            sink += attrObj.get_intValue01();
        }
        preventOptimisation(sink);
    }

    /** Set a float attribute on a native object. */
    private static void benchAttrSetFloat() {
        for(long i = 0; i < CALLS_PER_ITERATION; i++) {
            attrObj.set_floatValue01((float) i);
        }
    }

    /** Get a float attribute from a native object. */
    private static void benchAttrGetFloat() {
        float sink = 0;
        for(long i = 0; i < CALLS_PER_ITERATION; i++) {
            sink += attrObj.get_floatValue01();
        }
        preventOptimisation(sink);
    }

    /** Write + read IDLIntArray elements (2 native calls per iteration). */
    private static void benchIDLIntArray() {
        int sink = 0;
        for(long i = 0; i < CALLS_PER_ITERATION; i++) {
            int idx = (int) (i & 63);
            intArray.setValue(idx, (int) i);
            sink += intArray.getValue(idx);
        }
        preventOptimisation(sink);
    }

    /** Pass a direct ByteBuffer to native for bulk update. */
    private static void benchByteBuffer() {
        for(long i = 0; i < CALLS_PER_ITERATION; i++) {
            bufferObj.updateByteBuffer(byteBuffer, 256, (byte) 0x42);
        }
    }

    // ---------------------------------------------------------------------------
    // Anti-optimisation fence – prevents dead-code elimination
    // ---------------------------------------------------------------------------

    private static volatile int intSink;
    private static volatile float floatSink;

    private static void preventOptimisation(int value) {
        intSink = value;
    }

    private static void preventOptimisation(float value) {
        floatSink = value;
    }
}








