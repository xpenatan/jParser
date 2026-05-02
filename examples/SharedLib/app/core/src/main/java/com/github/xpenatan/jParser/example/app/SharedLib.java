package com.github.xpenatan.jParser.example.app;

import com.github.xpenatan.jparser.runtime.helper.NativeInt;
import libA.LibA;
import libA.LibAData;
import libB.LibB;

public class SharedLib {

    public static boolean test() {
        boolean fail = false;

        {
            try {
                LibA libAClass = new LibA();
                LibAData obj1 = new LibAData();
                obj1.set_intValue(4);
                LibAData obj2 = new LibAData();
                LibA.setGlobalData(obj1);
                obj2.set_intValue(6);
                int total = libAClass.addInt(obj1, obj2);
                if(total != 10) {
                    fail = true;
                }
            } catch(Throwable t) {
                t.printStackTrace();
                fail = true;
            }
        }
        {
            // Share memory. LibB creates LibAInstanceClass and used by LibA addInt
            try {
                LibA libAClass = new LibA();
                LibB libBClass = new LibB();
                LibAData obj1 = new LibAData();
                obj1.set_intValue(4);
                LibAData obj2 = libBClass.createInstance(6);
                int total = libAClass.addInt(obj1, obj2);
                if(total != 10) {
                    fail = true;
                }
            } catch(Throwable t) {
                t.printStackTrace();
                fail = true;
            }
        }
        {
            // Share RuntimeHelper.
            try {
                LibA libAClass = new LibA();
                NativeInt int1 = new NativeInt();
                int1.set(4);
                NativeInt int2 = new NativeInt();
                int2.set(6);
                int total = libAClass.addIntPtr(int1, int2);
                if(total != 10) {
                    fail = true;
                }
            } catch(Throwable t) {
                t.printStackTrace();
                fail = true;
            }
            try {
                LibB libBClass = new LibB();
                NativeInt int1 = new NativeInt();
                int1.set(2);
                NativeInt int2 = new NativeInt();
                int2.set(8);
                int total = libBClass.addIntPtr(int1, int2);
                if(total != 10) {
                    fail = true;
                }
            } catch(Throwable t) {
                t.printStackTrace();
                fail = true;
            }
        }
        {
            try {
                LibAData globalData1 = LibA.getGlobalData();
                LibAData globalData2 = LibB.getGlobalData();
                System.out.println("LibA GData isNull: " + globalData1.native_isNULL());
                System.out.println("LibB GData isNull: " + globalData2.native_isNULL());

                if(globalData2.native_isNULL()) {
                    throw new RuntimeException("LibB getGlobalData is null");
                }
            } catch(Throwable t) {
                t.printStackTrace();
                fail = true;
            }
        }

        if(fail) {
            return false;
        }
        return true;
    }
}
