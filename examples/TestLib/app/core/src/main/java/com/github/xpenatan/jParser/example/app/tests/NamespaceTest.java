package com.github.xpenatan.jParser.example.app.tests;

import com.github.xpenatan.jParser.example.app.CodeTest;
import com.github.xpenatan.jParser.example.testlib.core.sub.TestNamespaceClass;

public class NamespaceTest implements CodeTest {

    private static boolean testNamespaceClass() {
        {
            TestNamespaceClass test = new TestNamespaceClass();
            try {
                int value = 20;
                test.setMethod01Value(value);
                if(!(value == test.getMethod01Value())) {
                    throw new RuntimeException("value == test.getMethod01Value()");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        {
            TestNamespaceClass test = new TestNamespaceClass();
            try {
                int value = 40;
                test.set_intValue01(value);
                if(!(value == test.get_intValue01())) {
                    throw new RuntimeException("value == test.get_intValue01()");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        return true;
    }

    @Override
    public boolean test() {
        return testNamespaceClass();
    }
}
