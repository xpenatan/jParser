package com.github.xpenatan.jparser.example;

import com.github.xpenatan.jparser.loader.JParserLibraryLoader;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NormalClassTest {

    @BeforeClass
    public static void setUp() throws Exception {
        String libDir = "src/main/resources/example-test-natives.jar";
        new JParserLibraryLoader(libDir).load("example-test");
    }

    @Test
    public void test_add_int() {
        NormalClass normalClass = new NormalClass();
        int ret = normalClass.addIntValue(10, 10);
        assertEquals(20, ret);
    }

    @Test
    public void test_add_float() {
        NormalClass normalClass = new NormalClass();
        float ret = normalClass.addFloatValue(10.3f, 10.3f);
        assertEquals(20.6, ret, 1.0f);
    }

    @Test
    public void test_invert_boolean_should_be_false() {
        NormalClass normalClass = new NormalClass();
        boolean ret = normalClass.invertBoolean(true);
        assertFalse(ret);
    }

    @Test
    public void test_invert_boolean_should_be_true() {
        NormalClass normalClass = new NormalClass();
        boolean ret = normalClass.invertBoolean(false);
        assertTrue(ret);
    }

}

