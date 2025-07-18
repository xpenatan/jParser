package com.github.xpenatan.jparser.example.app;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.github.xpenatan.jparser.example.testlib.TestEnumLib;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class EnumBenchmark {

    static long size = 3_000_000_000L;

    static void test() {
        if(Gdx.app.getType() == Application.ApplicationType.WebGL) {
            size = 200_000_000L;
        }

        System.out.println("=========== TEST size: " + size);
        for (int i = 0; i < 3; i++) {
            System.out.println("=========== TEST: " + i);
            benchmarkBitflags();
            benchmarkTestEnumLib();
            benchmarkTestEnumLibCustom_1();
            benchmarkTestEnumLibCustom_2();
//            benchmarkHashSet();
//            benchmarkHashSetCache();
//            benchmarkEnumSet();
        }
    }

    static enum Flag { A, B, C, D, E, F, G }

    static void benchmarkEnumSet() {
        System.gc();
        var beg = System.nanoTime();
        Set<Flag> a = EnumSet.of(Flag.A, Flag.B, Flag.G);
        for (long i = 0; i < size; i++) {
            Set<Flag> b = EnumSet.of(Flag.A, Flag.B, Flag.G);
            assert a.equals(b);
        }
        var end = System.nanoTime();
        System.out.println((end - beg)/1e9 + "\t\tEnumSet");
    }

    static void benchmarkHashSet() {
        System.gc();
        var beg = System.nanoTime();
        Set<Flag> a = new HashSet<>(Arrays.asList(Flag.A, Flag.B, Flag.G));
        for (long i = 0; i < size; i++) {
            Set<Flag> b = new HashSet<>(Arrays.asList(Flag.A, Flag.B, Flag.G));
            assert a.equals(b);
        }
        var end = System.nanoTime();
        System.out.println((end - beg)/1e9 + "\t\tHashSet");
    }

    static void benchmarkHashSetCache() {
        System.gc();
        var beg = System.nanoTime();
        Set<Flag> a = new HashSet<>(Arrays.asList(Flag.A, Flag.B, Flag.G));
        Set<Flag> b = new HashSet<>();
        for (long i = 0; i < size; i++) {
            b.clear();
            b.add(Flag.A);
            b.add(Flag.B);
            b.add(Flag.G);
            assert a.equals(b);
        }
        var end = System.nanoTime();
        System.out.println((end - beg)/1e9 + "\t\tHashSetCache");
    }

    static final int A = 1 << 0;
    static final int B = 1 << 1;
    static final int C = 1 << 2;
    static final int D = 1 << 3;
    static final int E = 1 << 4;
    static final int F = 1 << 5;
    static final int G = 1 << 6;

    static void benchmarkBitflags() {
        System.gc();
        var beg = System.nanoTime();
        var a = A | B | G;
        for (long i = 0; i < size; i++) {
            var b = A | B | G;
            assert a == b;
        }
        var end = System.nanoTime();
        System.out.println((end - beg)/1e9 + "\t\tbitfield");
    }

    static void benchmarkTestEnumLib() {
        System.gc();
        var beg = System.nanoTime();
        int value = TestEnumLib.TEST_DEFAULT.or(TestEnumLib.TEST_FIRST).or(TestEnumLib.TEST_SECOND).getValue();
        for (long i = 0; i < size; i++) {
            int val = TestEnumLib.TEST_DEFAULT.or(TestEnumLib.TEST_FIRST).or(TestEnumLib.TEST_SECOND).getValue();
            assert val == value;
        }
        var end = System.nanoTime();
        System.out.println((end - beg)/1e9 + "\t\tTestEnumLib");
    }

    static void benchmarkTestEnumLibCustom_1() {
        System.gc();
        var beg = System.nanoTime();
        int value11 = TestEnumLib.TEST_DEFAULT.getValue();
        int value22 = TestEnumLib.TEST_FIRST.getValue();
        int value33 = TestEnumLib.TEST_SECOND.getValue();
        int value = value11 | value22 | value33;
        for (long i = 0; i < size; i++) {
            int value1 = TestEnumLib.TEST_DEFAULT.getValue();
            int value2 = TestEnumLib.TEST_FIRST.getValue();
            int value3 = TestEnumLib.TEST_SECOND.getValue();
            int val = value1 | value2 | value3;
            assert val == value;
        }
        var end = System.nanoTime();
        System.out.println((end - beg)/1e9 + "\t\tTestEnumLibCustom1");
    }

    static void benchmarkTestEnumLibCustom_2() {
        System.gc();
        var beg = System.nanoTime();
        int value1 = TestEnumLib.TEST_DEFAULT.getValue();
        int value2 = TestEnumLib.TEST_FIRST.getValue();
        int value3 = TestEnumLib.TEST_SECOND.getValue();
        int value = value1 | value2 | value3;
        for (long i = 0; i < size; i++) {
            int val = value1 | value2 | value3;
            assert val == value;
        }
        var end = System.nanoTime();
        System.out.println((end - beg)/1e9 + "\t\tTestEnumLibCustom2");
    }
}
