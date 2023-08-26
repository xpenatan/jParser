package com.github.xpenatan.jparser.example.lib;

import idl.IDLEnum;

public class EnumLib implements IDLEnum {
    public static int FIRST = FIRST_NATIVE();

    /*[-C++;-NATIVE]
        return (jlong)FIRST;
    */
    /*[-teaVM;-NATIVE]
        return exampleLib.FIRST;
    */
    private static native int FIRST_NATIVE();
}
