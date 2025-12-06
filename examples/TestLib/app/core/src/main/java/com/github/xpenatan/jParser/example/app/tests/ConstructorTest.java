package com.github.xpenatan.jParser.example.app.tests;

import com.github.xpenatan.jParser.example.app.CodeTest;
import com.github.xpenatan.jParser.example.testlib.TestAttributeClass;
import com.github.xpenatan.jParser.example.testlib.TestConstructorClass;
import com.github.xpenatan.jParser.example.testlib.TestEnumLib;
import com.github.xpenatan.jParser.idl.IDLBase;

public class ConstructorTest implements CodeTest {
    private static boolean testConstructorClass() {
        {
            int intValue01 = 40;
            TestConstructorClass test = new TestConstructorClass(intValue01);
            try {
                if(!(test.get_intValue01() == intValue01)) {
                    throw new RuntimeException("testConstructorClass Error: test.get_intValue01() == intValue01");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        {
            float floatValue01 = 4.654f;
            int intValue01 = 29;
            TestConstructorClass test = new TestConstructorClass(floatValue01, intValue01);
            try {
                if(!(test.get_floatValue01() == floatValue01)) {
                    throw new RuntimeException("testConstructorClass Error: test.get_floatValue01() == floatValue01");
                }
                if(!(test.get_intValue01() == intValue01)) {
                    throw new RuntimeException("testConstructorClass Error: test.get_intValue01() == intValue01");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        {
            int intValue01 = 40;
            int intValue02 = 47;
            float floatValue01 = 42.5f;
            float floatValue02 = 72.9f;
            TestConstructorClass test = new TestConstructorClass(intValue01, intValue02, floatValue01, floatValue02, TestEnumLib.TEST_SECOND);
            try {
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
                if(!(test.get_enumValue() == TestEnumLib.TEST_SECOND)) {
                    throw new RuntimeException("test.get_enumValue() == TestEnumLib.TEST_SECOND");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        {
            int intValue01 = 40;
            int intValue02 = 47;
            float floatValue01 = 42.5f;
            float floatValue02 = 72.9f;
            boolean boolValue01 = true;
            TestConstructorClass test = new TestConstructorClass(intValue01, intValue02, floatValue01, floatValue02, TestEnumLib.TEST_SECOND, boolValue01);
            try {
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
                if(!(test.get_enumValue() == TestEnumLib.TEST_SECOND)) {
                    throw new RuntimeException("test.get_enumValue() == TestEnumLib.TEST_SECOND");
                }
                if(!(test.get_boolValue01() == boolValue01)) {
                    throw new RuntimeException("testConstructorClass Error: test.get_boolValue01() == boolValue01");
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

    private static boolean testStringConstructorClass() {
        {
            TestAttributeClass test = new TestAttributeClass();
            try {
                test.set_enumValue(TestEnumLib.TEST_FIRST);
                if(!(test.get_enumValue() == TestEnumLib.TEST_FIRST)) {
                    throw new RuntimeException("test.get_enumValue() == TestEnumLib.TEST_FIRST)");
                }
            } catch(Throwable e){
                e.printStackTrace();
                test.dispose();
                return false;
            }
        }
        {
            TestAttributeClass test = new TestAttributeClass();
            try {
                IDLBase voidTest = new IDLBase();
                voidTest.native_setAddress(1000);
                test.set_voidPointer01(voidTest);
                IDLBase voidPointer01 = test.get_voidPointer01();
                if(!(voidPointer01.equals(voidTest))) {
                    throw new RuntimeException("voidPointer01.equals(voidTest)");
                }

            } catch(Throwable e){
                e.printStackTrace();
                test.dispose();
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean test() {
        return testConstructorClass() && testStringConstructorClass();
    }
}
