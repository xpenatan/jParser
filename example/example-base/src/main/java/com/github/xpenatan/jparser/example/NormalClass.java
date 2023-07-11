package com.github.xpenatan.jparser.example;

public class NormalClass extends ParentClass {
    /*[-C++;-NATIVE]
        #include "NormalClass.h"
    */

    public NormalClass() {
        initObject(createNative(), true);
    }

    /*[-C++;-NATIVE]
        return (jlong)new NormalClass();
    */
    private static native long createNative();
}