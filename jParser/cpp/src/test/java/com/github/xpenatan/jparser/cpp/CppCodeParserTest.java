package com.github.xpenatan.jparser.cpp;

import com.badlogic.gdx.jnigen.AntScriptGenerator;
import com.badlogic.gdx.jnigen.BuildConfig;
import com.badlogic.gdx.jnigen.BuildExecutor;
import com.badlogic.gdx.jnigen.BuildTarget;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.cpp.tests.CppTestClass;
import java.io.File;
import java.nio.ByteBuffer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

public class CppCodeParserTest {

    @BeforeClass
    public static void setUp() throws Exception {
        String classpathStr = System.getProperty("java.class.path") + File.pathSeparator;
        System.out.println("classpath: " + classpathStr);

        String jniDir = "build/jparser/generated/jni";

        CppCodeParser parser = new CppCodeParser(classpathStr, jniDir);

        JParser.generate(parser, "src/test/java", "build/jparser/generated/java", null);


        // generate build scripts
        BuildConfig buildConfig = new BuildConfig("test", "../../tmp/gdx-jnigen", "../../jparser/libs", "build/jparser/generated/jni");

        BuildTarget target;
        if (SharedLibraryLoader.isWindows)
            target = BuildTarget.newDefaultTarget(BuildTarget.TargetOs.Windows, SharedLibraryLoader.is64Bit);
        else if (SharedLibraryLoader.isLinux)
            target = BuildTarget.newDefaultTarget(BuildTarget.TargetOs.Linux, SharedLibraryLoader.is64Bit, SharedLibraryLoader.isARM);
        else if (SharedLibraryLoader.isMac)
            target = BuildTarget.newDefaultTarget(BuildTarget.TargetOs.MacOsX, SharedLibraryLoader.is64Bit, SharedLibraryLoader.isARM);
        else
            throw new RuntimeException("Unsupported OS to run tests.");

        new AntScriptGenerator().generate(buildConfig, target);

        if (SharedLibraryLoader.isMac) {
            String scriptToRun;
            if (SharedLibraryLoader.isARM) {
                scriptToRun = "build/jparser/generated/jni/build-macosxarm64.xml";
            } else {
                scriptToRun = "build/jparser/generated/jni/build-macosx64.xml";
            }
            boolean macAntExecutionStatus = BuildExecutor.executeAnt(scriptToRun, "-v");
            if (!macAntExecutionStatus) {
                throw new RuntimeException("Failure to execute mac ant.");
            }
        } else {
            if (SharedLibraryLoader.isLinux) {
                boolean antExecutionStatus = BuildExecutor.executeAnt("build/jparser/generated/jni/build-linux64.xml", "-v", "-Dhas-compiler=true", "postcompile");
                if (!antExecutionStatus) {
                    throw new RuntimeException("Failure to execute linux/windows ant.");
                }
            }
            else if(SharedLibraryLoader.isWindows) {
                boolean antExecutionStatus = BuildExecutor.executeAnt("build/jparser/generated/jni/build-windows64.xml", "-v", "-Dhas-compiler=true", "postcompile");
                if (!antExecutionStatus) {
                    throw new RuntimeException("Failure to execute linux/windows ant.");
                }
            }
        }
        boolean antExecutionStatus = BuildExecutor.executeAnt("build/jparser/generated/jni/build.xml", "-v", "compile-natives", "pack-natives");


        // compile and pack natives

        if (!antExecutionStatus) {
            throw new RuntimeException("Failure to execute ant.");
        }

        // load the test-natives.jar and from it the shared library, then execute the test.
        new SharedLibraryLoader("build/jparser/libs/test-natives.jar").load("test");
    }

    @Test
    public void testBoolean() {
        assertTrue(CppTestClass.testBoolean(true));
        assertFalse(CppTestClass.testBoolean(false));
    }

    @Test
    public void testByte() {
        assertEquals((byte) 0, CppTestClass.testByte((byte) 0));
        assertEquals((byte) 1, CppTestClass.testByte((byte) 1));
    }

    @Test
    public void testChar() {
        assertEquals('A', CppTestClass.testChar('A'));
        assertEquals('B', CppTestClass.testChar('B'));
    }

    @Test
    public void testShort() {
        assertEquals((short) 0, CppTestClass.testShort((short) 0));
        assertEquals((short) 1, CppTestClass.testShort((short) 1));
    }

    @Test
    public void testInt() {
        assertEquals(0, CppTestClass.testInt(0));
        assertEquals(1, CppTestClass.testInt(1));
    }

    @Test
    public void testLong() {
        assertEquals(0L, CppTestClass.testLong(0L));
        assertEquals(1L, CppTestClass.testLong(1L));
    }

    @Test
    public void testFloat() {
        assertEquals(0.0f, CppTestClass.testFloat(0.0f), 0.001);
        assertEquals(1.0f, CppTestClass.testFloat(1.0f), 0.001);
    }

    @Test
    public void testDouble() {
        assertEquals(0.0, CppTestClass.testDouble(0.0), 0.001);
        assertEquals(1.0, CppTestClass.testDouble(1.0), 0.001);
    }

    @Test
    public void testInner() {
        assertEquals(1, CppTestClass.TestInner.testInner(0));
        assertEquals(2, CppTestClass.TestInner.testInner(1));
    }

    @Test
    public void testAll() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1);
        buffer.put(0, (byte)8);

        assertTrue(CppTestClass.test(
                true, (byte)1, (char)2, (short)3, 4, 5, 6, 7,
                buffer, new boolean[] { false }, new char[] { 9 },
                new short[] { 10 }, new int[] { 11 }, new long[] { 12 },
                new float[] { 13 }, new double[] { 14 },
                null, "Hurray", CppTestClass.class, new RuntimeException(), new CppTestClass()));
    }
}

