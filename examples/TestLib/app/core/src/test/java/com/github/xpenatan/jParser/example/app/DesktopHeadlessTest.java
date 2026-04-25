package com.github.xpenatan.jParser.example.app;

import com.github.xpenatan.jParser.example.testlib.TestLibLoader;
import com.github.xpenatan.jparser.idl.IDLLoader;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertTrue;

public class DesktopHeadlessTest {

    @BeforeClass
    public static void setUp() throws Exception {
        // Allow CI to force which native architecture to load via environment variable.
        // Set `JPARSE_FORCE_ARCH=intel` or `JPARSE_FORCE_ARCH=arm` in the job to force loader selection.
        String forced = System.getenv("JPARSE_FORCE_ARCH");
        if(forced != null) {
            if(forced.equalsIgnoreCase("intel") || forced.equalsIgnoreCase("x86_64")) {
                // Force the os.arch system property for the test JVM so the loader picks the intel dylib
                System.setProperty("os.arch", "x86_64");
            }
            else if(forced.equalsIgnoreCase("arm") || forced.equalsIgnoreCase("aarch64") || forced.equalsIgnoreCase("arm64")) {
                // Force the os.arch system property for the test JVM so the loader picks the arm dylib
                System.setProperty("os.arch", "aarch64");
            }
        }

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Throwable> err = new AtomicReference<>();

        IDLLoader.init((idl_isSuccess, idl_e) -> {
            if(idl_e != null) {
                err.set(idl_e);
                latch.countDown();
                return;
            }
            TestLibLoader.init((isSuccess, e) -> {
                if(e != null) {
                    err.set(e);
                }
                latch.countDown();
            });
        });

        boolean ok = latch.await(30, TimeUnit.SECONDS);
        if(!ok) {
            throw new RuntimeException("Timeout waiting for native initialization");
        }
        if(err.get() != null) {
            throw new RuntimeException("Native initialization failed", err.get());
        }
    }

    @Test
    public void testNativeBridge() {
        boolean pass = TestLib.test();
        assertTrue("TestLib.test() must return true", pass);
    }
}

