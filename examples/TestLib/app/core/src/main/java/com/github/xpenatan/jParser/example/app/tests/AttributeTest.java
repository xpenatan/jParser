package com.github.xpenatan.jParser.example.app.tests;

import com.github.xpenatan.jParser.example.app.CodeTest;
import com.github.xpenatan.jParser.example.testlib.TestAttributeArrayClass;
import com.github.xpenatan.jParser.example.testlib.TestObjectClass;

public class AttributeTest implements CodeTest {

    private static boolean testAttributeClass() {


        return true;
    }

    private static boolean testStaticAttributeClass() {


        return true;
    }

    private static boolean testAttributeArrayClass() {
        try {
            TestAttributeArrayClass attributeArrayClass = new TestAttributeArrayClass();
            TestObjectClass valueObjectArray1 = attributeArrayClass.get_valueObjectArray(0);
            valueObjectArray1.set_intValue01(11);
            TestObjectClass valueObjectArray2 = attributeArrayClass.get_valueObjectArray(0);
            int value = valueObjectArray2.get_intValue01();
            if(!(value == 11)) {
                throw new RuntimeException("testAttributeArrayClass !(value == 11)");
            }
        } catch(Throwable e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean test() {
        return testAttributeClass() && testStaticAttributeClass() && testAttributeArrayClass();
    }
}