package com.github.xpenatan.jParser.example.app;

import libA.LibA;
import libA.LibAData;
import libB.LibB;

public class SharedLibTest {

    public static boolean test() {
        boolean fail = false;

        {
            try {
                LibA libAClass = new LibA();
                LibAData obj1 = new LibAData();
                obj1.set_intValue(4);
                LibAData obj2 = new LibAData();
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

        if(fail) {
            return false;
        }
        return true;
    }
}