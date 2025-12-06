package com.github.xpenatan.jParser.example.app.tests;

import com.github.xpenatan.jParser.example.app.CodeTest;
import com.github.xpenatan.jParser.example.testlib.TestEnumClassWithinClass;
import com.github.xpenatan.jParser.example.testlib.TestEnumLib;
import com.github.xpenatan.jParser.example.testlib.core.enums.TestEnumWithinClass;

public class EnumTest implements CodeTest {

    private static boolean testEnum() {
        {
            TestEnumClassWithinClass testEnum = TestEnumClassWithinClass.testEnum;
            try {
                if(!(testEnum.getValue() == 35)) {
                    throw new RuntimeException("testEnum Error: !(testEnum == 35)");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                return false;
            }
        }
        {
            TestEnumLib testDefault = TestEnumLib.TEST_DEFAULT;
            try {
                if(!(testDefault.getValue() == 1 << 0)) {
                    throw new RuntimeException("testEnum Error: !(testDefault == 1 << 0)");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                return false;
            }
        }
        {
            TestEnumWithinClass eVal = TestEnumWithinClass.e_val_renamed;
            try {
                if(!(eVal.getValue() == 34)) {
                    throw new RuntimeException("testEnum Error: !(eVal == 34)");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean test() {
        return testEnum();
    }
}