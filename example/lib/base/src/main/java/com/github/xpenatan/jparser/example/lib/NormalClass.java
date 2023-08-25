package com.github.xpenatan.jparser.example.lib;

import idl.IDLBase;

public class NormalClass extends IDLBase {

    public int enumParam(int value) {
        return enumParamNATIVE(getCPointer(), value);
    }

    /*[-C++;-NATIVE]
        NormalClass* nativeObject = (NormalClass*)this_addr;
        return nativeObject->enumParam((EnumLib)value);
    */
    /*[-teaVM;-NATIVE]
        var jsObj = exampleLib.wrapPointer(this_addr, exampleLib.NormalClass);
        return jsObj.enumParam(value);
    */
    private static native int enumParamNATIVE(long this_addr, int value);
}