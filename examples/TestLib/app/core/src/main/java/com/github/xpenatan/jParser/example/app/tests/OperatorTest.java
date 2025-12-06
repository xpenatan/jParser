package com.github.xpenatan.jParser.example.app.tests;

import com.github.xpenatan.jParser.example.app.CodeTest;
import com.github.xpenatan.jParser.example.testlib.core.op.TestOperatorClass;

public class OperatorTest implements CodeTest {
    private static boolean testOperatorClass() {
        boolean ret = true;
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
            {
                TestOperatorClass data = operatorClass.getData(0);
                if(!(data.equals(operatorClass))) {
                    throw new RuntimeException("data.equals(operatorClass)");
                }
            }
            operatorClass.dispose();
            otherOPClass.dispose();

            return true;
        } catch(Throwable e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public boolean test() {
        return testOperatorClass();
    }
}
