package com.github.xpenatan.jparser.example.app;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.github.xpenatan.jparser.example.testlib.CallbackClass;
import com.github.xpenatan.jparser.example.testlib.CallbackClassManual;
import com.github.xpenatan.jparser.example.testlib.CallbackExceptionManual;
import com.github.xpenatan.jparser.example.testlib.DefaultCallbackClass;
import com.github.xpenatan.jparser.example.testlib.IDLArrayTestObjectClass;
import com.github.xpenatan.jparser.example.testlib.TestAttributeArrayClass;
import com.github.xpenatan.jparser.example.testlib.TestCallbackClass;
import com.github.xpenatan.jparser.example.testlib.TestConstructorClass;
import com.github.xpenatan.jparser.example.testlib.TestEnumClassWithinClass;
import com.github.xpenatan.jparser.example.testlib.TestEnumLib;
import com.github.xpenatan.jparser.example.testlib.TestExceptionManual;
import com.github.xpenatan.jparser.example.testlib.TestMethodClass;
import com.github.xpenatan.jparser.example.testlib.TestObjectClass;
import com.github.xpenatan.jparser.example.testlib.core.enums.TestEnumWithinClass;
import com.github.xpenatan.jparser.example.testlib.core.op.TestOperatorClass;
import com.github.xpenatan.jparser.example.testlib.core.sub.TestNamespaceClass;
import com.github.xpenatan.jparser.example.testlib.idl.helper.IDLString;

public class TestLib {

    public static boolean test() {
        boolean enumTest = testEnum();
        boolean constructorTest = testConstructorClass();
        boolean stringConstructorTest = testStringConstructorClass();
        boolean attributeTest = testAttributeClass();
        boolean staticAttributeTest = testStaticAttributeClass();
        boolean attributeArrayTest = testAttributeArrayClass();
        boolean methodTest = testMethodClass();
        boolean staticMethodTest = testStaticMethodClass();
        boolean callbackTest = testCallbackClass();
        boolean callbackTestManual = testCallbackClassManual();
        boolean namespaceTest = testNamespaceClass();
        boolean operatorTest = testOperatorClass();
        boolean exceptionTest = testExceptionManual();

        System.out.println("enumTest: " + enumTest);
        System.out.println("constructorTest: " + constructorTest);
        System.out.println("stringConstructorTest: " + stringConstructorTest);
        System.out.println("attributeTest: " + attributeTest);
        System.out.println("staticAttributeTest: " + staticAttributeTest);
        System.out.println("attributeArrayTest: " + attributeArrayTest);
        System.out.println("methodTest: " + methodTest);
        System.out.println("staticMethodTest: " + staticMethodTest);
        System.out.println("callbackTest: " + callbackTest);
        System.out.println("namespaceTest: " + namespaceTest);
        System.out.println("operatorTest: " + operatorTest);
        System.out.println("exceptionTest: " + exceptionTest);

        return enumTest && constructorTest && stringConstructorTest && attributeTest && staticAttributeTest
                && attributeArrayTest && methodTest && staticMethodTest && callbackTest
                && callbackTestManual && namespaceTest && operatorTest && exceptionTest;
    }

    private static boolean testEnum() {
        {
            int testEnum = TestEnumClassWithinClass.testEnum;
            try {
                if(!(testEnum == 35)) {
                    throw new RuntimeException("testEnum Error: !(testEnum == 35)");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                return false;
            }
        }
        {
            int testDefault = TestEnumLib.TEST_DEFAULT;
            try {
                if(!(testDefault == 7)) {
                    throw new RuntimeException("testEnum Error: !(testDefault == 7)");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                return false;
            }
        }
        {
            int eVal = TestEnumWithinClass.e_val_renamed;
            try {
                if(!(eVal == 34)) {
                    throw new RuntimeException("testEnum Error: !(eVal == 34)");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
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
                test.setMethod07(array);
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
                test.setMethod08(longLongValue01);
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
        return true;
    }

    private static boolean testStaticMethodClass() {


        return true;
    }

    private static boolean testCallbackClass() {
        {
            TestCallbackClass test = new TestCallbackClass();
            try {
                boolean[] internal_onVoidCallback = { false };
                CallbackClass callback = new CallbackClass() {
                    @Override
                    public void onVoidCallback(TestObjectClass refData, TestObjectClass pointerData) {
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
                CallbackClass callback = new CallbackClass() {
                    @Override
                    public int onIntCallback(int intValue01, int intValue02) {
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
                test.set_intValue01(10);
                test.set_intValue02(3);
                CallbackClass callback = new DefaultCallbackClass();
                int value = test.callIntCallback(callback);
                if(!(value == 7)) {
                    throw new RuntimeException("value == 7");
                }
                callback.dispose();
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
                CallbackClass callback = new CallbackClass() {
                    @Override
                    public float onFloatCallback(float floatValue01, float floatValue02) {
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
                CallbackClass callback = new CallbackClass() {
                    @Override
                    public boolean onBoolCallback(boolean boolValue01) {
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
                CallbackClass callback = new CallbackClass() {
                    @Override
                    public void onStringCallback(String strValue01) {
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
        {
            TestCallbackClass test = new TestCallbackClass();
            try {
                int[] onUnsignedIntCallback = { 0 };
                CallbackClass callback = new CallbackClass() {
                    @Override
                    public int onUnsignedIntCallback(int unsignedInt) {
                        onUnsignedIntCallback[0] = unsignedInt;
                        return 2;
                    }
                };
                int i = test.callUnsignedIntCallback(callback);
                if(!(onUnsignedIntCallback[0] == 13 && i == 2)) {
                    throw new RuntimeException("onUnsignedIntCallback[0] == 13 && i == 2");
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
                short[] onUnsignedShortCallback = { 0 };
                CallbackClass callback = new CallbackClass() {
                    @Override
                    public short onUnsignedShortCallback(short unsignedShort) {
                        onUnsignedShortCallback[0] = unsignedShort;
                        return 3;
                    }
                };
                short i = test.callUnsignedShortCallback(callback);
                if(!(onUnsignedShortCallback[0] == 12 && i == 3)) {
                    throw new RuntimeException("onUnsignedShortCallback[0] == 12 && i == 3");
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
                test.callManualVoidCallback(callback);
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
                test.callManualIntCallback(callback);
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
                test.callManualFloatCallback(callback);
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
                test.callManualBoolCallback(callback);
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
                test.callManualStringCallback(callback);
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

    private static boolean testOperatorClass() {
        try {
            TestOperatorClass operatorClass = new TestOperatorClass();
            TestOperatorClass otherOPClass = new TestOperatorClass();
            otherOPClass.setValue(3);

            // Arithmetic Operators
            {
                operatorClass.setValue(10);
                TestOperatorClass obj = operatorClass.addValue(otherOPClass);
                float value = obj.getValue();
                if(!(value == 13 && (obj != operatorClass && obj != otherOPClass))) {
                    throw new RuntimeException("testOperatorClass '+' !(value == 13)");
                }
            }
            {
                operatorClass.setValue(10);
                TestOperatorClass obj = operatorClass.subValue(otherOPClass);
                float value = obj.getValue();
                if(!(value == 7 && (obj != operatorClass && obj != otherOPClass))) {
                    throw new RuntimeException("testOperatorClass '-' !(value == 7)");
                }
            }
            {
                operatorClass.setValue(10);
                TestOperatorClass obj = operatorClass.mulValue(otherOPClass);
                float value = obj.getValue();
                if(!(value == 30 && (obj != operatorClass && obj != otherOPClass))) {
                    throw new RuntimeException("testOperatorClass '*' !(value == 30)");
                }
            }
            {
                operatorClass.setValue(30);
                TestOperatorClass obj = operatorClass.divValue(otherOPClass);
                float value = obj.getValue();
                if(!(value == 10 && (obj != operatorClass && obj != otherOPClass))) {
                    throw new RuntimeException("testOperatorClass '/' !(value == 10)");
                }
            }
            // Compound Assignment Operators
            {
                operatorClass.setValue(10);
                TestOperatorClass obj = operatorClass.addRef(otherOPClass);
                float value = obj.getValue();
                if(!(value == 13 && (obj == operatorClass))) {
                    throw new RuntimeException("testOperatorClass '+' !(value == 13)");
                }
            }
            {
                operatorClass.setValue(10);
                TestOperatorClass obj = operatorClass.subRef(otherOPClass);
                float value = obj.getValue();
                if(!(value == 7 && (obj == operatorClass))) {
                    throw new RuntimeException("testOperatorClass '-' !(value == 7)");
                }
            }
            {
                operatorClass.setValue(10);
                TestOperatorClass obj = operatorClass.mulRef(otherOPClass);
                float value = obj.getValue();
                if(!(value == 30 && (obj == operatorClass))) {
                    throw new RuntimeException("testOperatorClass '*' !(value == 30)");
                }
            }
            {
                operatorClass.setValue(30);
                TestOperatorClass obj = operatorClass.divRef(otherOPClass);
                float value = obj.getValue();
                if(!(value == 10 && (obj == operatorClass))) {
                    throw new RuntimeException("testOperatorClass '/' !(value == 10)");
                }
            }
            operatorClass.dispose();
            otherOPClass.dispose();

            return true;
        } catch(Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean testExceptionManual() {
        if(Gdx.app.getType() != Application.ApplicationType.WebGL) {
            // TODO add JNI support
            return true;
        }
        boolean ret;
        try {
            ret = false;

            CallbackExceptionManual callback = new CallbackExceptionManual() {
                @Override
                public void internal_callJava() {
                    System.out.println("CALL_JAVA");
                    throw new IllegalStateException("This is expected");
                }
            };

            TestExceptionManual exceptionManual = new TestExceptionManual();
            exceptionManual.callJavaMethod(callback);
        }
        catch(Throwable t) {
            t.printStackTrace();
            ret = true;
        }

        try {
            ret = false;
            // This test is calling a c++ method and the C++ code tries to set a value to a null object pointer.
            // It will trigger an exception in native side.
            TestExceptionManual exceptionManual = new TestExceptionManual();
            int value = exceptionManual.setDataToNullPointer();
            System.out.println("Value: " + value);
        }
        catch(Throwable t) {
            // Javascript/Wasm catches the error in native side automatically. C++ does not, need to implement.
            t.printStackTrace();
            ret = true;
        }
        return ret;
    }
}