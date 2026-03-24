package com.github.xpenatan.jParser.example.app;

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
 * FPS benchmark — measures how native bridge overhead affects frame rate.
 * <p>
 * Each frame executes a fixed number of native calls for the current scenario,
 * then returns to let GDX render. A state machine cycles through all scenarios,
 * measuring average and minimum FPS for each.
 * <p>
 * This complements {@link NativeBridgeBenchmark} which measures raw throughput
 * in a tight loop. The FPS benchmark shows real-world frame rate impact.
 */
public class NativeBridgeFpsBenchmark {

    // ---------------------------------------------------------------------------
    // Configuration
    // ---------------------------------------------------------------------------

    /** Native calls executed per frame for every scenario. */
    private static final int CALLS_PER_FRAME = 50_000;
    /** Seconds to warm up before measuring (lets JIT stabilise). */
    private static final float WARMUP_SECONDS = 2f;
    /** Seconds to measure FPS after warm-up. */
    private static final float MEASURE_SECONDS = 5f;

    // ---------------------------------------------------------------------------
    // State machine
    // ---------------------------------------------------------------------------

    private enum State { IDLE, WARMUP, MEASURE, NEXT, DONE }

    private State state = State.IDLE;
    private float elapsed;
    private int frameCount;
    private float minFrameTime;   // worst (longest) frame during measurement
    private int scenarioIndex;

    // Reusable native objects
    private TestMethodClass methodObj;
    private TestObjectClass objectA;
    private TestObjectClass objectB;
    private TestAttributeClass attrObj;
    private TestBufferManualClass bufferObj;
    private IDLIntArray intArray;
    private ByteBuffer byteBuffer;

    // Scenario definitions
    private String[] labels;
    private Runnable[] workloads;

    // Collected results
    private final ArrayList<FpsResult> results = new ArrayList<>();

    /** Data holder for one scenario measurement. */
    public static class FpsResult {
        public final String label;
        public final float avgFps;
        public final float minFps;

        public FpsResult(String label, float avgFps, float minFps) {
            this.label = label;
            this.avgFps = avgFps;
            this.minFps = minFps;
        }
    }

    // ---------------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------------

    /** Call once after native libraries are loaded. */
    public void start() {
        allocateResources();
        buildScenarios();
        results.clear();
        scenarioIndex = 0;
        beginScenario();

        System.out.println("=======================================================");
        System.out.println("  FPS Benchmark");
        System.out.println("  Calls/frame    : " + CALLS_PER_FRAME);
        System.out.println("  Warm-up (sec)  : " + (int) WARMUP_SECONDS);
        System.out.println("  Measure (sec)  : " + (int) MEASURE_SECONDS);
        System.out.println("  Scenarios      : " + labels.length);
        System.out.println("=======================================================");
    }

    /**
     * Call every frame from {@code render()}. Returns {@code true} when all
     * scenarios are finished.
     */
    public boolean update() {
        if(state == State.DONE) return true;

        float dt = Gdx.graphics.getDeltaTime();

        // Execute the workload for this frame
        if(state == State.WARMUP || state == State.MEASURE) {
            workloads[scenarioIndex].run();
        }

        switch(state) {
            case WARMUP:
                elapsed += dt;
                if(elapsed >= WARMUP_SECONDS) {
                    // Transition to measurement
                    state = State.MEASURE;
                    elapsed = 0f;
                    frameCount = 0;
                    minFrameTime = 0f;
                }
                break;

            case MEASURE:
                elapsed += dt;
                frameCount++;
                if(dt > minFrameTime) {
                    minFrameTime = dt;
                }
                if(elapsed >= MEASURE_SECONDS) {
                    // Record results
                    float avgFps = frameCount / elapsed;
                    float minFps = minFrameTime > 0 ? 1f / minFrameTime : 0f;
                    results.add(new FpsResult(labels[scenarioIndex], avgFps, minFps));

                    System.out.printf("  %-38s avg %6.1f FPS   min %6.1f FPS%n",
                            labels[scenarioIndex], avgFps, minFps);

                    state = State.NEXT;
                }
                break;

            case NEXT:
                scenarioIndex++;
                if(scenarioIndex >= labels.length) {
                    state = State.DONE;
                    finish();
                    return true;
                }
                beginScenario();
                break;

            default:
                break;
        }

        return false;
    }

    /** Returns collected results after benchmark is done. */
    public ArrayList<FpsResult> getResults() {
        return results;
    }

    // ---------------------------------------------------------------------------
    // Internals
    // ---------------------------------------------------------------------------

    private void beginScenario() {
        state = State.WARMUP;
        elapsed = 0f;
        frameCount = 0;
        minFrameTime = 0f;
    }

    private void finish() {
        System.out.println("=======================================================");
        System.out.println("  FPS Benchmark complete");
        System.out.println("=======================================================");

        String outputPath = System.getProperty("benchmark.fps.output");
        if(outputPath != null && !outputPath.isEmpty()) {
            writeCsv(outputPath);
        }

        freeResources();
    }

    private void writeCsv(String path) {
        try(PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            pw.println("# calls_per_frame=" + CALLS_PER_FRAME);
            pw.println("# warmup_sec=" + (int) WARMUP_SECONDS);
            pw.println("# measure_sec=" + (int) MEASURE_SECONDS);
            pw.println("label,avgFps,minFps");
            for(FpsResult r : results) {
                pw.printf("%s,%.2f,%.2f%n", r.label, r.avgFps, r.minFps);
            }
            System.out.println("  Results written to: " + path);
        } catch(IOException e) {
            System.err.println("  Failed to write CSV: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------------------
    // Resource lifecycle
    // ---------------------------------------------------------------------------

    private void allocateResources() {
        methodObj = new TestMethodClass();
        objectA = new TestObjectClass();
        objectB = new TestObjectClass();
        attrObj = new TestAttributeClass();
        bufferObj = new TestBufferManualClass();
        intArray = new IDLIntArray(64);

        byteBuffer = ByteBuffer.allocateDirect(256);
        byteBuffer.order(ByteOrder.nativeOrder());

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

    private void freeResources() {
        intArray.dispose();
        attrObj.dispose();
        objectB.dispose();
        objectA.dispose();
        methodObj.dispose();
    }

    // ---------------------------------------------------------------------------
    // Scenario definitions
    // ---------------------------------------------------------------------------

    private void buildScenarios() {
        labels = new String[] {
            "void(int)",
            "void(float, bool)",
            "void(int,int,float,float,bool)",
            "int getter",
            "float getter",
            "void(String)",
            "String getter",
            "void(Obj*,Obj*,Obj&,Obj&)",
            "Object* getter",
            "attribute set int",
            "attribute get int",
            "attribute set float",
            "attribute get float",
            "IDLIntArray set+get",
            "ByteBuffer update (256B)",
        };

        workloads = new Runnable[] {
            this::fpsSetInt,
            this::fpsSetFloatBool,
            this::fpsSetManyPrimitives,
            this::fpsGetInt,
            this::fpsGetFloat,
            this::fpsSetString,
            this::fpsGetString,
            this::fpsObjectPassing,
            this::fpsObjectReturn,
            this::fpsAttrSetInt,
            this::fpsAttrGetInt,
            this::fpsAttrSetFloat,
            this::fpsAttrGetFloat,
            this::fpsIDLIntArray,
            this::fpsByteBuffer,
        };
    }

    // ---------------------------------------------------------------------------
    // Per-frame workloads (same calls as throughput benchmark, but N per frame)
    // ---------------------------------------------------------------------------

    private void fpsSetInt() {
        for(int i = 0; i < CALLS_PER_FRAME; i++) {
            methodObj.setMethod01(i);
        }
    }

    private void fpsSetFloatBool() {
        for(int i = 0; i < CALLS_PER_FRAME; i++) {
            methodObj.setMethod02(1.5f, true);
        }
    }

    private void fpsSetManyPrimitives() {
        for(int i = 0; i < CALLS_PER_FRAME; i++) {
            methodObj.setMethod03(1, 2, 3.0f, 4.0f, true);
        }
    }

    private void fpsGetInt() {
        int sink = 0;
        for(int i = 0; i < CALLS_PER_FRAME; i++) {
            sink += methodObj.getIntValue01();
        }
        intSink = sink;
    }

    private void fpsGetFloat() {
        float sink = 0;
        for(int i = 0; i < CALLS_PER_FRAME; i++) {
            sink += methodObj.getFloatValue01();
        }
        floatSink = sink;
    }

    private void fpsSetString() {
        for(int i = 0; i < CALLS_PER_FRAME; i++) {
            methodObj.setMethod05("benchmark");
        }
    }

    private void fpsGetString() {
        for(int i = 0; i < CALLS_PER_FRAME; i++) {
            methodObj.getStrValue01().data();
        }
    }

    private void fpsObjectPassing() {
        for(int i = 0; i < CALLS_PER_FRAME; i++) {
            methodObj.setMethod06(objectA, objectB, objectA, objectB);
        }
    }

    private void fpsObjectReturn() {
        for(int i = 0; i < CALLS_PER_FRAME; i++) {
            methodObj.getPointerObject02();
        }
    }

    private void fpsAttrSetInt() {
        for(int i = 0; i < CALLS_PER_FRAME; i++) {
            attrObj.set_intValue01(i);
        }
    }

    private void fpsAttrGetInt() {
        int sink = 0;
        for(int i = 0; i < CALLS_PER_FRAME; i++) {
            sink += attrObj.get_intValue01();
        }
        intSink = sink;
    }

    private void fpsAttrSetFloat() {
        for(int i = 0; i < CALLS_PER_FRAME; i++) {
            attrObj.set_floatValue01((float) i);
        }
    }

    private void fpsAttrGetFloat() {
        float sink = 0;
        for(int i = 0; i < CALLS_PER_FRAME; i++) {
            sink += attrObj.get_floatValue01();
        }
        floatSink = sink;
    }

    private void fpsIDLIntArray() {
        int sink = 0;
        for(int i = 0; i < CALLS_PER_FRAME; i++) {
            int idx = i & 63;
            intArray.setValue(idx, i);
            sink += intArray.getValue(idx);
        }
        intSink = sink;
    }

    private void fpsByteBuffer() {
        for(int i = 0; i < CALLS_PER_FRAME; i++) {
            bufferObj.updateByteBuffer(byteBuffer, 256, (byte) 0x42);
        }
    }

    // Anti-optimisation fence
    private volatile int intSink;
    private volatile float floatSink;
}

