/*-------------------------------------------------------
 * This file was generated by JParser
 *
 * Do not make changes to this file
 *-------------------------------------------------------*/
package com.github.xpenatan.jparser.example.lib;

import com.github.xpenatan.jparser.example.lib.idl.IDLEnum;

public class EnumLib implements IDLEnum {

    public static final int FIRST = FIRST_NATIVE();

    /*[-C++;-NATIVE]
return (jlong)FIRST;
*/
    private static native int FIRST_NATIVE();

    public static final int SECOND = SECOND_NATIVE();

    /*[-C++;-NATIVE]
return (jlong)SECOND;
*/
    private static native int SECOND_NATIVE();
}