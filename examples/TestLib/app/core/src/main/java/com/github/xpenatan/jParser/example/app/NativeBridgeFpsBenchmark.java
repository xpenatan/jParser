package com.github.xpenatan.jParser.example.app;

import com.badlogic.gdx.Gdx;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
    private static final int CALLS_PER_FRAME = 1_000_000;
    /** Seconds to warm up before measuring (lets JIT stabilise). */
    private static final float WARMUP_SECONDS = 5f;
    /** Seconds to measure FPS after warm-up. */
    private static final float MEASURE_SECONDS = 5f;
    /** Seconds between FPS log lines in interactive mode. */
    private static final float INTERACTIVE_LOG_INTERVAL_SECONDS = 1f;

    public enum Mode { SEQUENTIAL, INTERACTIVE_SINGLE }

    // ---------------------------------------------------------------------------
    // State machine
    // ---------------------------------------------------------------------------

    private enum State { IDLE, WARMUP, MEASURE, NEXT, DONE }

    private State state = State.IDLE;
    private Mode mode = Mode.SEQUENTIAL;
    private float elapsed;
    private int frameCount;
    private float minFrameTime;   // worst (longest) frame during measurement
    private int scenarioIndex;
    private float interactiveLogElapsed;
    private int interactiveLogFrames;

    // Shared scenario definitions/resources
    private NativeBridgeWorkloads workloads;
    private NativeBridgeWorkloads.Scenario[] scenarios;

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
        workloads = new NativeBridgeWorkloads();
        scenarios = workloads.getScenarios();
        mode = readMode();
        results.clear();
        scenarioIndex = 0;
        if(mode == Mode.SEQUENTIAL) {
            beginScenario();
        }
        else {
            beginInteractiveScenario();
        }

        System.out.println("=======================================================");
        System.out.println("  FPS Benchmark");
        System.out.println("  Mode           : " + mode);
        System.out.println("  Calls/frame    : " + CALLS_PER_FRAME);
        System.out.println("  Warm-up (sec)  : " + (int) WARMUP_SECONDS);
        System.out.println("  Measure (sec)  : " + (int) MEASURE_SECONDS);
        System.out.println("  Scenarios      : " + scenarios.length);
        if(mode == Mode.INTERACTIVE_SINGLE) {
            System.out.println("  Controls       : LEFT=previous, RIGHT=next");
        }
        System.out.println("=======================================================");
    }

    /**
     * Call every frame from {@code render()}. Returns {@code true} when all
     * scenarios are finished.
     */
    public boolean update() {
        if(mode == Mode.INTERACTIVE_SINGLE) {
            return updateInteractive();
        }
        return updateSequential();
    }

    public boolean isInteractiveMode() {
        return mode == Mode.INTERACTIVE_SINGLE;
    }

    public void nextScenario() {
        if(scenarios == null || scenarios.length == 0) {
            return;
        }
        scenarioIndex = (scenarioIndex + 1) % scenarios.length;
        beginInteractiveScenario();
    }

    public void previousScenario() {
        if(scenarios == null || scenarios.length == 0) {
            return;
        }
        scenarioIndex = (scenarioIndex - 1 + scenarios.length) % scenarios.length;
        beginInteractiveScenario();
    }

    public void dispose() {
        if(workloads != null) {
            workloads.dispose();
            workloads = null;
            scenarios = null;
        }
        state = State.DONE;
    }

    private boolean updateSequential() {
        if(state == State.DONE) return true;

        float dt = Gdx.graphics.getDeltaTime();

        // Execute the workload for this frame
        if(state == State.WARMUP || state == State.MEASURE) {
            scenarios[scenarioIndex].run(CALLS_PER_FRAME);
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
                    results.add(new FpsResult(scenarios[scenarioIndex].label, avgFps, minFps));

                    System.out.printf("  %-38s avg %6.1f FPS   min %6.1f FPS%n",
                            scenarios[scenarioIndex].label, avgFps, minFps);

                    state = State.NEXT;
                }
                break;

            case NEXT:
                scenarioIndex++;
                if(scenarioIndex >= scenarios.length) {
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

    private boolean updateInteractive() {
        if(scenarios == null || scenarios.length == 0) {
            return false;
        }

        float dt = Gdx.graphics.getDeltaTime();
        scenarios[scenarioIndex].run(CALLS_PER_FRAME);

        interactiveLogElapsed += dt;
        interactiveLogFrames++;

        if(interactiveLogElapsed >= INTERACTIVE_LOG_INTERVAL_SECONDS) {
            float avgFps = interactiveLogFrames / interactiveLogElapsed;
            float currentFps = dt > 0f ? 1f / dt : 0f;
            System.out.printf("  [Interactive] %-30s current %6.1f FPS   avg %6.1f FPS%n",
                    scenarios[scenarioIndex].label, currentFps, avgFps);
            interactiveLogElapsed = 0f;
            interactiveLogFrames = 0;
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

    private void beginInteractiveScenario() {
        interactiveLogElapsed = 0f;
        interactiveLogFrames = 0;
        if(scenarios != null && scenarios.length > 0) {
            System.out.println("  [Interactive] Selected scenario: " + scenarios[scenarioIndex].label);
        }
    }

    private Mode readMode() {
        String rawMode = System.getProperty("benchmark.fps.mode", "sequence");
        if(rawMode != null && rawMode.equalsIgnoreCase("interactive")) {
            return Mode.INTERACTIVE_SINGLE;
        }
        return Mode.SEQUENTIAL;
    }

    private void finish() {
        System.out.println("=======================================================");
        System.out.println("  FPS Benchmark complete");
        System.out.println("=======================================================");

        String outputPath = System.getProperty("benchmark.fps.output");
        if(outputPath != null && !outputPath.isEmpty()) {
            writeCsv(outputPath);
        }

        dispose();
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
}

