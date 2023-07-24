package com.github.xpenatan.jparser.example;

public class NormalClass extends ParentClass {
    /*[-C++;-NATIVE]
        #include "NormalClass.h"
    */

    public NormalClass() {
        long aNative = createNative();
        initObject(aNative, true);
    }

    /*[-teaVM;-NATIVE]
        var jsObj = new exampleLib.NormalClass();
        return exampleLib.getPointer(jsObj);
    */
    /*[-C++;-NATIVE]
        return (jlong)new NormalClass();
    */
    private static native long createNative();
}