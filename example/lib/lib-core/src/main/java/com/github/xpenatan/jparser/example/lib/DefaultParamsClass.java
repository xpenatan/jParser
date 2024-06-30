/*-------------------------------------------------------
 * This file was generated by JParser
 *
 * Do not make changes to this file
 *-------------------------------------------------------*/
package com.github.xpenatan.jparser.example.lib;

import com.github.xpenatan.jparser.example.lib.idl.IDLBase;

public class DefaultParamsClass extends IDLBase {

    static public final DefaultParamsClass T_01 = new DefaultParamsClass((byte) 1, (char) 1);

    static public final DefaultParamsClass T_02 = new DefaultParamsClass((byte) 1, (char) 1);

    static public final DefaultParamsClass T_03 = new DefaultParamsClass((byte) 1, (char) 1);

    public DefaultParamsClass(int a, int b, float c, float d) {
        long addr = createNATIVE(a, b, c, d);
        initNative(addr, true);
    }

    /*[-C++;-NATIVE]
return (jlong)new DefaultParamsClass(a, b, c, d);
*/
    private static native long createNATIVE(int a, int b, float c, float d);

    public DefaultParamsClass(int a, int b, float c) {
        long addr = createNATIVE(a, b, c);
        initNative(addr, true);
    }

    /*[-C++;-NATIVE]
return (jlong)new DefaultParamsClass(a, b, c);
*/
    private static native long createNATIVE(int a, int b, float c);

    public DefaultParamsClass(int a, int b) {
        long addr = createNATIVE(a, b);
        initNative(addr, true);
    }

    /*[-C++;-NATIVE]
return (jlong)new DefaultParamsClass(a, b);
*/
    private static native long createNATIVE(int a, int b);

    public DefaultParamsClass(byte b, char c) {
    }

    public void defaultMethodParams(int a, int b, float c, float d) {
        defaultMethodParamsNATIVE(getCPointer(), a, b, c, d);
    }

    /*[-C++;-NATIVE]
DefaultParamsClass* nativeObject = (DefaultParamsClass*)this_addr;
nativeObject->defaultMethodParams(a, b, c, d);
*/
    private static native void defaultMethodParamsNATIVE(long this_addr, int a, int b, float c, float d);

    public void defaultMethodParams(int a, int b, float c) {
        defaultMethodParamsNATIVE(getCPointer(), a, b, c);
    }

    /*[-C++;-NATIVE]
DefaultParamsClass* nativeObject = (DefaultParamsClass*)this_addr;
nativeObject->defaultMethodParams(a, b, c);
*/
    private static native void defaultMethodParamsNATIVE(long this_addr, int a, int b, float c);

    public void defaultMethodParams(int a, int b) {
        defaultMethodParamsNATIVE(getCPointer(), a, b);
    }

    /*[-C++;-NATIVE]
DefaultParamsClass* nativeObject = (DefaultParamsClass*)this_addr;
nativeObject->defaultMethodParams(a, b);
*/
    private static native void defaultMethodParamsNATIVE(long this_addr, int a, int b);
}
