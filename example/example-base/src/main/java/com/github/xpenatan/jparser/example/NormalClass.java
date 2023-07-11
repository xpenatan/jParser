package com.github.xpenatan.jparser.example;

public class NormalClass extends ParentClass {
    /*[-C++;-NATIVE]
        #include "NormalClass.h"
    */

    public NormalClass() {
        System.out.println("POINTER BEFORE: " + cPointer);
        long aNative = createNative();
        initObject(aNative, true);
        System.out.println("POINTER AFTER: " + cPointer);
    }

    /*[-C++;-NATIVE]
        return (jlong)new NormalClass();
    */
    private static native long createNative();
}