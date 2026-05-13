package com.github.xpenatan.jparser.benchmark;

/**
 * Lightweight benchmark runner for CI smoke checks.
 */
public class PerfSmokeMain {

    private static final int WARMUP_ITERS = 3;
    private static final int MEASURE_ITERS = 5;
    private static final int OP_COUNT = 200_000;

    public static void main(String[] args) {
        byte[] src = new byte[1024];
        byte[] dst = new byte[1024];
        for(int i = 0; i < src.length; i++) {
            src[i] = (byte)(i & 0x7F);
        }

        long scalarNs = runBenchmark("scalar-loop", () -> scalarCopy(src, dst));
        long arrayCopyNs = runBenchmark("system-arraycopy", () -> systemArrayCopy(src, dst));

        System.out.println("--- Perf Smoke Summary ---");
        System.out.println("scalar-loop ns/op: " + scalarNs);
        System.out.println("system-arraycopy ns/op: " + arrayCopyNs);
        System.out.println("checksum: " + checksum(dst));
    }

    private static long runBenchmark(String name, Runnable action) {
        for(int i = 0; i < WARMUP_ITERS; i++) {
            action.run();
        }

        long start = System.nanoTime();
        for(int i = 0; i < MEASURE_ITERS; i++) {
            action.run();
        }
        long elapsed = System.nanoTime() - start;
        long nsPerOp = elapsed / (MEASURE_ITERS * (long)OP_COUNT);
        System.out.println(name + " raw elapsed ns: " + elapsed);
        return nsPerOp;
    }

    private static void scalarCopy(byte[] src, byte[] dst) {
        for(int op = 0; op < OP_COUNT; op++) {
            int len = src.length;
            for(int i = 0; i < len; i++) {
                dst[i] = src[i];
            }
        }
    }

    private static void systemArrayCopy(byte[] src, byte[] dst) {
        for(int op = 0; op < OP_COUNT; op++) {
            System.arraycopy(src, 0, dst, 0, src.length);
        }
    }

    private static long checksum(byte[] data) {
        long sum = 0;
        for(byte b : data) {
            sum += (b & 0xFF);
        }
        return sum;
    }
}

