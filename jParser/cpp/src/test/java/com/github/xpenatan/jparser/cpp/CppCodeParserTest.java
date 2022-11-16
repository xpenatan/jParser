package com.github.xpenatan.jparser.cpp;

import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.cpp.tests.CppTestClass;
import com.github.xpenatan.jparser.loader.JParserLibraryLoader;
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

        String buildPath = "build/jparser/generated";
        String jniDir = buildPath + "/jni";
        String genDir = buildPath + "/java";

        CppCodeParser parser = new CppCodeParser(classpathStr, jniDir);

        JParser.generate(parser, "src/test/java", genDir, null);

        CPPBuildHelper.build("test", buildPath);

        new JParserLibraryLoader(buildPath + "/libs/test-natives.jar").load("test");
    }

    @Test
    public void testStaticInt() {
        assertEquals(10, CppTestClass.testStaticInt());
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
    public void testInnerStaticBool() {
        assertTrue(CppTestClass.TestInner.getStaticInnerBool());
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

    @Test
    public void testStaticFloat() {
        assertEquals(33, CppTestClass.getStaticFloat(), 0.01f);
    }

}

