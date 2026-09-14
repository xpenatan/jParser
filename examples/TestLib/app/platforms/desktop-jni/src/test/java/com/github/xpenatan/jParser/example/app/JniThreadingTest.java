package com.github.xpenatan.jParser.example.app;

import com.github.xpenatan.jParser.example.testlib.CallbackClass;
import com.github.xpenatan.jParser.example.testlib.CallbackThreadingManual;
import com.github.xpenatan.jParser.example.testlib.TestMethodClass;
import com.github.xpenatan.jParser.example.testlib.TestStaticMethodClass;
import com.github.xpenatan.jParser.example.testlib.core.op.TestOperatorClass;
import com.github.xpenatan.jparser.runtime.helper.NativeString;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class JniThreadingTest {
    @BeforeClass
    public static void load() throws Exception {
        DesktopHeadlessTest.setUp();
    }

    @Test(timeout = 30000)
    public void callbackRegisteredOnMainThreadRunsOnJavaWorkers() throws Exception {
        AtomicInteger strings = new AtomicInteger();
        CallbackClass callback = callback(strings);
        ExecutorService workers = Executors.newFixedThreadPool(6);
        try {
            List<Future<Integer>> results = new ArrayList<>();
            for(int i = 0; i < 6; i++) {
                results.add(workers.submit(() -> CallbackThreadingManual.invoke(callback.native_address, 2000)));
            }
            for(Future<Integer> result : results) assertEquals(14000, (int)result.get(20, TimeUnit.SECONDS));
            assertEquals(12000, strings.get());
            // A callback worker can dispose a callback registered on another thread.
            workers.submit(callback::dispose).get(5, TimeUnit.SECONDS);
        }
        finally {
            workers.shutdownNow();
            if(callback.native_address != 0) callback.dispose();
        }
    }

    @Test(timeout = 30000)
    public void nativeWorkerIsAttachedAndDetachedForCallbacks() {
        AtomicInteger strings = new AtomicInteger();
        CallbackClass callback = callback(strings);
        try {
            assertEquals(1400, CallbackThreadingManual.invokeNativeWorker(callback.native_address, 200, false));
            assertEquals(200, strings.get());
        }
        finally {
            callback.dispose();
        }
    }

    @Test(timeout = 30000)
    public void alreadyAttachedNativeWorkerStaysAttachedAndReleasesStrings() {
        AtomicInteger strings = new AtomicInteger();
        CallbackClass callback = callback(strings);
        try {
            assertEquals(70000, CallbackThreadingManual.invokeNativeWorker(callback.native_address, 10000, true));
            assertEquals(10000, strings.get());
        }
        finally {
            callback.dispose();
        }
    }

    @Test(timeout = 30000)
    public void callbackExceptionsReturnToJavaCaller() {
        IllegalStateException expected = new IllegalStateException("expected JNI callback failure");
        CallbackClass callback = new CallbackClass() {
            @Override
            protected int onIntCallback(int first, int second) {
                throw expected;
            }
        };
        try {
            try {
                CallbackThreadingManual.invoke(callback.native_address, 1);
                fail("Callback exception must propagate to the Java caller");
            }
            catch(IllegalStateException actual) {
                assertSame(expected, actual);
            }
        }
        finally {
            callback.dispose();
        }
    }

    @Test(timeout = 30000)
    public void nativeWorkerExceptionStillDetachesAndReturnsDefault() {
        CallbackClass callback = new CallbackClass() {
            @Override
            protected int onIntCallback(int first, int second) {
                throw new IllegalStateException("expected native worker callback failure");
            }
        };
        try {
            assertEquals(0, CallbackThreadingManual.invokeNativeWorker(callback.native_address, 1, false));
        }
        finally {
            callback.dispose();
        }
    }

    @Test(timeout = 30000)
    public void independentValueGettersDoNotOverwriteOtherThreads() throws Exception {
        ExecutorService workers = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(2);
        try {
            List<Future<?>> results = new ArrayList<>();
            for(int i = 0; i < 2; i++) {
                String expected = "value on worker " + i;
                int operandValue = 10 * (i + 1);
                results.add(workers.submit(() -> {
                    TestMethodClass value = new TestMethodClass(expected);
                    TestOperatorClass operand = new TestOperatorClass(operandValue);
                    TestOperatorClass other = new TestOperatorClass(1);
                    NativeString staticCopy = NativeString.native_new();
                    try {
                        for(int iteration = 0; iteration < 200; iteration++) {
                            NativeString copy = value.getStrValue01();
                            TestOperatorClass sum = operand.addValue(other);
                            // Serialize the shared native source and Java cache, then retain
                            // the native temporary in this worker's own borrowed wrapper.
                            synchronized(TestStaticMethodClass.class) {
                                TestStaticMethodClass.setMethod05(expected);
                                staticCopy.internal_reset(TestStaticMethodClass.getStrValue01().native_address, false);
                            }
                            barrier.await(5, TimeUnit.SECONDS);
                            assertEquals(expected, copy.c_str());
                            assertEquals(expected, staticCopy.c_str());
                            assertEquals(operand.getValue() + 1, sum.getValue(), 0);
                            barrier.await(5, TimeUnit.SECONDS);
                        }
                    }
                    finally {
                        value.dispose();
                        operand.dispose();
                        other.dispose();
                    }
                    return null;
                }));
            }
            for(Future<?> result : results) result.get(20, TimeUnit.SECONDS);
        }
        finally {
            workers.shutdownNow();
        }
    }

    private static CallbackClass callback(AtomicInteger strings) {
        return new CallbackClass() {
            @Override
            protected int onIntCallback(int first, int second) {
                return first + second;
            }

            @Override
            protected void onStringCallback(String value) {
                assertEquals("worker", value);
                strings.incrementAndGet();
            }
        };
    }
}
