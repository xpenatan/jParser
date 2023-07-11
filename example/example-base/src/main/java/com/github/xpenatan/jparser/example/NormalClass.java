package com.github.xpenatan.jparser.example;

public class NormalClass extends ParentClass {
    /*[-C++;-NATIVE]
        #include "NormalClass.h"
        #include "ParamClass.h"
        #include "ReturnClass.h"
    */

    public NormalClass() {
        long aNative = createNative();
        initObject(aNative, true);
    }

    /*[-C++;-NATIVE]
        return (jlong)new NormalClass();
    */
    private static native long createNative();
}