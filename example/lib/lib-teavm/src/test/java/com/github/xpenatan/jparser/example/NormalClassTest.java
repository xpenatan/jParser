package com.github.xpenatan.jparser.example;

import com.github.xpenatan.jparser.loader.JParserLibraryLoader;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.teavm.jso.JSBody;
import org.teavm.jso.ajax.ReadyStateChangeHandler;
import org.teavm.jso.ajax.XMLHttpRequest;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.junit.SkipJVM;
import org.teavm.junit.TeaVMTestRunner;
import org.teavm.junit.WholeClassCompilation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(TeaVMTestRunner.class)
@SkipJVM
@WholeClassCompilation
public class NormalClassTest {
//    static HTMLDocument document;
//
//    @JSBody(params = { "text" }, script = "console.log(text);")
//    static native int log(String text); // only show when using browser mode

    @Before
    public void setUp() throws Exception {

//        Window current = Window.current();
//        document = current.getDocument();


//        XMLHttpRequest httpRequest = XMLHttpRequest.create();
//        httpRequest.setOnReadyStateChange(new ReadyStateChangeHandler() {
//            @Override
//            public void stateChanged() {
//
//            }
//        });
//
//        httpRequest.open("GET", "url", false);
//        httpRequest.setRequestHeader("Content-Type", "text/plain; charset=utf-8");
//        httpRequest.send();
////
//        String libDir = "src/main/resources/example-test-natives.jar";
//        new JParserLibraryLoader(libDir).load("example-test");
    }

//    @Test
//    public void loadsResources() {
//        String s = loadResource("9");
//        assertEquals("qwertyuiopasdfghjklzxcvbnm", s);
//    }

//    @Test
//    public void test_js_not_null() {
//        String js = loadResource("exampleLib.js");
//        assertNotNull(js);
//        HTMLElement scriptElement = document.createElement("script");
//        scriptElement.appendChild(document.createTextNode(js));
//        document.getBody().appendChild(scriptElement);
//
//        int test =123123;
//        log("" + test);
//
//        NormalClass normalClass = new NormalClass();
//        int ret = normalClass.addIntValue(10, 10);
//        assertEquals(20, ret);
//    }
//
//    @Test
//    public void test_add_one_add_one() {
//        int value = 1 + 1;
//        assertEquals(2, value);
//    }

//    @Test
//    public void test_add_int() {
//        NormalClass normalClass = new NormalClass();
//        int ret = normalClass.addIntValue(10, 10);
//        assertEquals(20, ret);
//    }
//
//    @Test
//    public void test_static_sub_int() {
//        int ret = NormalClass.subIntValue(11, 10);
//        assertEquals(1, ret);
//    }
//
//    @Test
//    public void test_static_sub_int_subValue() {
//        int ret = NormalClass.subIntValue(11, 10, 1);
//        assertEquals(0, ret);
//    }
//
//    @Test
//    public void test_add_float() {
//        NormalClass normalClass = new NormalClass();
//        float ret = normalClass.addFloatValue(10.3f, 10.3f);
//        assertEquals(20.6, ret, 1.0f);
//    }
//
//    @Test
//    public void test_invert_boolean_should_be_false() {
//        NormalClass normalClass = new NormalClass();
//        boolean ret = normalClass.invertBoolean(true);
//        assertFalse(ret);
//    }
//
//    @Test
//    public void test_invert_boolean_should_be_true() {
//        NormalClass normalClass = new NormalClass();
//        boolean ret = normalClass.invertBoolean(false);
//        assertTrue(ret);
//    }
//
//    @Test
//    public void test_attribute() {
//        NormalClass normalClass = new NormalClass();
//        normalClass.hiddenInt(10);
//        int retValue = normalClass.hiddenInt();
//        assertEquals(10, retValue);
//    }


//    private static String loadResource(String name) {
//        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
//        InputStream inputStream = classLoader.getResourceAsStream("resources-for-test/" + name);
//        try {
//            ByteArrayOutputStream into = new ByteArrayOutputStream();
//            byte[] buf = new byte[4096];
//            for (int n; 0 < (n = inputStream.read(buf));) {
//                into.write(buf, 0, n);
//            }
//            into.close();
//            return new String(into.toByteArray(), "UTF-8"); // Or whatever encoding
//        } catch(IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
}

