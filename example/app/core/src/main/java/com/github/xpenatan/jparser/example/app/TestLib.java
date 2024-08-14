package com.github.xpenatan.jparser.example.app;

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


        return false;
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