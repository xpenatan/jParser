/*-------------------------------------------------------
 * This file was generated by JParser
 *
 * Do not make changes to this file
 *-------------------------------------------------------*/
package com.github.xpenatan.jparser.example.lib;

import com.github.xpenatan.jparser.example.lib.idl.IDLEnum;

public class EnumInNamespace implements IDLEnum {

    public static final int e_namespace_val = e_namespace_val_NATIVE();

    /*[-C++;-NATIVE]
return (jlong)EnumNamespace::e_namespace_val;
*/
    private static native int e_namespace_val_NATIVE();
}