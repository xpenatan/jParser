package com.github.xpenatan.jparser.example.app;

import com.github.xpenatan.jparser.example.testlib.TestConstructorClass;

public class TestLib {

    public static boolean test() {
        boolean constructorTest = testConstructorClass();
        boolean attributeTest = testAttributeClass();
        boolean staticAttributeTest = testStaticAttributeClass();
        boolean attributeArrayTest = testAttributeArrayClass();
        boolean methodTest = testMethodClass();
        boolean staticMethodTest = testStaticMethodClass();
        boolean callbackTest = testCallbackClass();

        System.out.println("constructorTest: " + constructorTest);
        System.out.println("attributeTest: " + attributeTest);
        System.out.println("staticAttributeTest: " + staticAttributeTest);
        System.out.println("attributeArrayTest: " + attributeArrayTest);
        System.out.println("methodTest: " + methodTest);
        System.out.println("staticMethodTest: " + staticMethodTest);
        System.out.println("callbackTest: " + callbackTest);
        return attributeTest && attributeArrayTest;
    }

    private static boolean testConstructorClass() {

        {
            int intValue01 = 40;
            TestConstructorClass test = new TestConstructorClass(intValue01);
            if(!(test.get_intValue01() == intValue01)) {
                throw new RuntimeException("testConstructorClass Error: test.get_intValue01() == intValue01");
            }
        }
        {
            float floatValue01 = 4.654f;
            int intValue01 = 29;
            TestConstructorClass test = new TestConstructorClass(floatValue01, intValue01);

            if(!(test.get_floatValue01() == floatValue01)) {
                throw new RuntimeException("testConstructorClass Error: test.get_floatValue01() == floatValue01");
            }
            if(!(test.get_intValue01() == intValue01)) {
                throw new RuntimeException("testConstructorClass Error: test.get_intValue01() == intValue01");
            }
        }
        {
            int intValue01 = 40;
            int intValue02 = 47;
            float floatValue01 = 42.5f;
            float floatValue02 = 72.9f;
            TestConstructorClass test = new TestConstructorClass(intValue01, intValue02, floatValue01, floatValue02);
            if(!(test.get_intValue01() == intValue01)) {
                throw new RuntimeException("testConstructorClass Error: test.get_intValue01() == intValue01");
            }
            if(!(test.get_intValue02() == intValue02)) {
                throw new RuntimeException("testConstructorClass Error: test.get_intValue02() == intValue02");
            }
            if(!(test.get_floatValue01() == floatValue01)) {
                throw new RuntimeException("testConstructorClass Error: test.get_floatValue01() == floatValue01");
            }
            if(!(test.get_floatValue02() == floatValue02)) {
                throw new RuntimeException("testConstructorClass Error: test.get_floatValue02() == floatValue02");
            }
        }
        {
            int intValue01 = 40;
            int intValue02 = 47;
            float floatValue01 = 42.5f;
            float floatValue02 = 72.9f;
            boolean boolValue01 = true;
            TestConstructorClass test = new TestConstructorClass(intValue01, intValue02, floatValue01, floatValue02, boolValue01);
            if(!(test.get_intValue01() == intValue01)) {
                throw new RuntimeException("testConstructorClass Error: test.get_intValue01() == intValue01");
            }
            if(!(test.get_intValue02() == intValue02)) {
                throw new RuntimeException("testConstructorClass Error: test.get_intValue02() == intValue02");
            }
            if(!(test.get_floatValue01() == floatValue01)) {
                throw new RuntimeException("testConstructorClass Error: test.get_floatValue01() == floatValue01");
            }
            if(!(test.get_floatValue02() == floatValue02)) {
                throw new RuntimeException("testConstructorClass Error: test.get_floatValue02() == floatValue02");
            }
            if(!(test.get_boolValue01() == boolValue01)) {
                throw new RuntimeException("testConstructorClass Error: test.get_boolValue01() == boolValue01");
            }
        }
        return true;
    }

    private static boolean testAttributeClass() {


        return false;
    }

    private static boolean testStaticAttributeClass() {


        return false;
    }

    private static boolean testAttributeArrayClass() {


        return false;
    }

    private static boolean testMethodClass() {


        return false;
    }

    private static boolean testStaticMethodClass() {


        return false;
    }

    private static boolean testCallbackClass() {


        return false;
    }
}