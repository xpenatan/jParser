package com.github.xpenatan.jParser.example.app.tests;

import com.github.xpenatan.jParser.example.app.CodeTest;
import com.github.xpenatan.jParser.example.testlib.IDLArrayTestObjectClass;
import com.github.xpenatan.jParser.example.testlib.TestEnumLib;
import com.github.xpenatan.jParser.example.testlib.TestMethodClass;
import com.github.xpenatan.jParser.example.testlib.TestObjectClass;
import com.github.xpenatan.jparser.idl.helper.IDLString;

public class MethodTest implements CodeTest {

    private static boolean testMethodClass() {
        {
            TestMethodClass test = new TestMethodClass();
            test.setMethod01(10);
            int intValue01 = test.getIntValue01();
            try {
                if(!(intValue01 == 10)) {
                    test.dispose();
                    throw new RuntimeException("intValue01 == 10");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        {
            TestMethodClass test = new TestMethodClass();
            IDLArrayTestObjectClass array = new IDLArrayTestObjectClass(2);
            TestObjectClass obj1 = new TestObjectClass();
            TestObjectClass obj2 = new TestObjectClass();
            obj1.set_floatValue01(20.5f);
            obj1.set_intValue01(30);
            obj2.set_floatValue01(40.5f);
            obj2.set_intValue01(60);
            array.setValue(0, obj1);
            array.setValue(1, obj2);
            try {
                test.setPtrToPtrArray(array);
                {
                    float intValue01 = obj1.get_intValue01();
                    if(!(intValue01 == 20)) {
                        throw new RuntimeException("intValue01 == 20");
                    }
                    float floatValue01 = obj1.get_floatValue01();
                    if(!(floatValue01 == 10.4f)) {
                        throw new RuntimeException("floatValue01 == 10.4f");
                    }
                }
                {
                    float intValue01 = obj2.get_intValue01();
                    if(!(intValue01 == 40)) {
                        throw new RuntimeException("intValue01 == 40");
                    }
                    float floatValue01 = obj2.get_floatValue01();
                    if(!(floatValue01 == 30.8f)) {
                        throw new RuntimeException("floatValue01 == 30.8f");
                    }
                }
            } catch(Throwable e) {
                e.printStackTrace();
                obj1.dispose();
                obj2.dispose();
                array.dispose();
                test.dispose();
                return false;
            }
            obj1.dispose();
            obj2.dispose();
            array.dispose();
            test.dispose();
        }
        {
            TestMethodClass test = new TestMethodClass();
            try {
                String strValue01 = "Hello";
                test.setMethod05(strValue01);
                String stringValue = test.getStrValue01().data();
                if(!(strValue01.equals(stringValue))) {
                    throw new RuntimeException("strValue01.equals(stringValue)");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        {
            TestMethodClass test = new TestMethodClass();
            try {
                String strValue01 = "RefHello";
                test.setMethod05(strValue01);
                IDLString refStrValue01 = test.getRefStrValue01();
                String stringValue = refStrValue01.data();
                if(!(strValue01.equals(stringValue))) {
                    throw new RuntimeException("strValue01.equals(stringValue)");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        {
            TestMethodClass test = new TestMethodClass();
            try {
                long longLongValue01 = 4;
                test.setMethod09(longLongValue01);
                long retLongLongValue01 = test.getLongLongValue01();
                if(!(longLongValue01 == retLongLongValue01)) {
                    throw new RuntimeException("longLongValue01 == retLongLongValue01");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        {
            TestMethodClass test = new TestMethodClass();
            try {
                test.setMethod10(TestEnumLib.TEST_SECOND);
                TestEnumLib retEnum = test.getEnumValue();
                if(!(TestEnumLib.TEST_SECOND == retEnum)) {
                    throw new RuntimeException("TestEnumLib.TEST_SECOND == retEnum");
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

    private static boolean testStaticMethodClass() {


        return true;
    }

    @Override
    public boolean test() {
        return testMethodClass() && testStaticMethodClass();
    }
}