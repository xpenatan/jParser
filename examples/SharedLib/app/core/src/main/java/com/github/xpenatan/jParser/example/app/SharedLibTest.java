package com.github.xpenatan.jParser.example.app;

import libA.LibAClass;
import libA.LibAInstanceClass;
import libB.LibBClass;

public class SharedLibTest {

    public static boolean test() {
        boolean fail = false;

        {
            try {
                LibAClass libAClass = new LibAClass();
                LibAInstanceClass obj1 = new LibAInstanceClass();
                obj1.set_intValue(4);
                LibAInstanceClass obj2 = new LibAInstanceClass();
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
                LibAClass libAClass = new LibAClass();
                LibBClass libBClass = new LibBClass();
                LibAInstanceClass obj1 = new LibAInstanceClass();
                obj1.set_intValue(4);
                LibAInstanceClass obj2 = libBClass.createInstance(6);
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