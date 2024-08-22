package com.github.xpenatan.jparser.example.app;

import com.github.xpenatan.jparser.example.testlib.CallbackClassManual;
import com.github.xpenatan.jparser.example.testlib.TestCallbackClass;
import com.github.xpenatan.jparser.example.testlib.TestConstructorClass;
import com.github.xpenatan.jparser.example.testlib.TestMethodClass;
import com.github.xpenatan.jparser.example.testlib.TestObjectClass;
import com.github.xpenatan.jparser.example.testlib.TestObjectClassArray;
import com.github.xpenatan.jparser.example.testlib.idl.helper.IDLString;

public class TestLib {

    public static boolean test() {
        boolean constructorTest = testConstructorClass();
        boolean stringConstructorTest = testStringConstructorClass();
        boolean attributeTest = testAttributeClass();
        boolean staticAttributeTest = testStaticAttributeClass();
        boolean attributeArrayTest = testAttributeArrayClass();
        boolean methodTest = testMethodClass();
        boolean staticMethodTest = testStaticMethodClass();
        boolean callbackTest = testCallbackClass();
        boolean callbackTestManual = testCallbackClassManual();

        System.out.println("constructorTest: " + constructorTest);
        System.out.println("stringConstructorTest: " + stringConstructorTest);
        System.out.println("attributeTest: " + attributeTest);
        System.out.println("staticAttributeTest: " + staticAttributeTest);
        System.out.println("attributeArrayTest: " + attributeArrayTest);
        System.out.println("methodTest: " + methodTest);
        System.out.println("staticMethodTest: " + staticMethodTest);
        System.out.println("callbackTest: " + callbackTest);
        return constructorTest && stringConstructorTest && attributeTest && staticAttributeTest
                && attributeArrayTest && methodTest && staticMethodTest && callbackTest && callbackTestManual;
    }

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
            TestConstructorClass test = new TestConstructorClass(intValue01, intValue02, floatValue01, floatValue02);
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
            TestConstructorClass test = new TestConstructorClass(intValue01, intValue02, floatValue01, floatValue02, boolValue01);
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

        return true;
    }

    private static boolean testAttributeClass() {


        return true;
    }

    private static boolean testStaticAttributeClass() {


        return true;
    }

    private static boolean testAttributeArrayClass() {


        return true;
    }

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
            TestObjectClassArray array = new TestObjectClassArray(2);
            TestObjectClass obj1 = new TestObjectClass();
            TestObjectClass obj2 = new TestObjectClass();
            obj1.set_floatValue01(20.5f);
            obj1.set_intValue01(30);
            obj2.set_floatValue01(40.5f);
            obj2.set_intValue01(60);
            array.setValue(0, obj1);
            array.setValue(1, obj2);
            try {
                TestMethodClass.native_setMethod07(test.getCPointer(), array.getPointer());
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
        return true;
    }

    private static boolean testStaticMethodClass() {


        return true;
    }

    private static boolean testCallbackClass() {
        return true;
    }

    private static boolean testCallbackClassManual() {

        {
            TestCallbackClass test = new TestCallbackClass();
            try {
                boolean[] internal_onVoidCallback = { false };
                CallbackClassManual callback = new CallbackClassManual() {
                    @Override
                    public void internal_onVoidCallback(long refData, long pointerData) {
                        internal_onVoidCallback[0] = true;
                    }
                };
                test.callVoidCallback(callback);
                if(!(internal_onVoidCallback[0] == true)) {
                    throw new RuntimeException("internal_onVoidCallback[0] == true");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        {
            TestCallbackClass test = new TestCallbackClass();
            try {
                boolean[] internal_onIntCallback = { false };
                CallbackClassManual callback = new CallbackClassManual() {
                    @Override
                    public int internal_onIntCallback(int intValue01, int intValue02) {
                        internal_onIntCallback[0] = true;
                        return 0;
                    }
                };
                test.callIntCallback(callback);
                if(!(internal_onIntCallback[0] == true)) {
                    throw new RuntimeException("internal_onIntCallback[0] == true");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        {
            TestCallbackClass test = new TestCallbackClass();
            try {
                boolean[] internal_onFloatCallback = { false };
                CallbackClassManual callback = new CallbackClassManual() {
                    @Override
                    public float internal_onFloatCallback(float floatValue01, float floatValue02) {
                        internal_onFloatCallback[0] = true;
                        return 0;
                    }
                };
                test.callFloatCallback(callback);
                if(!(internal_onFloatCallback[0] == true)) {
                    throw new RuntimeException("internal_onFloatCallback[0] == true");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        {
            TestCallbackClass test = new TestCallbackClass();
            try {
                boolean[] internal_onBoolCallback = { false };
                CallbackClassManual callback = new CallbackClassManual() {
                    @Override
                    public boolean internal_onBoolCallback(boolean boolValue01) {
                        internal_onBoolCallback[0] = true;
                        return false;
                    }
                };
                test.callBoolCallback(callback);
                if(!(internal_onBoolCallback[0] == true)) {
                    throw new RuntimeException("internal_onBoolCallback[0] == true");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        {
            TestCallbackClass test = new TestCallbackClass();
            try {
                String text = "HELLO_WORLD";
                test.get_strValue01().append(text);
                final String[] internal_onStringCallback = new String[1];
                CallbackClassManual callback = new CallbackClassManual() {
                    @Override
                    public void internal_onStringCallback(String strValue01) {
                        internal_onStringCallback[0] = strValue01;
                    }
                };
                test.callStringCallback(callback);
                if(!(text.equals(internal_onStringCallback[0]) == true)) {
                    throw new RuntimeException("text.equals(internal_onStringCallback[0]) == true");
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
}