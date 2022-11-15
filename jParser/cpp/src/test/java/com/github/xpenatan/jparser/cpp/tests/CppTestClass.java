package com.github.xpenatan.jparser.cpp.tests;

import java.nio.Buffer;

public class CppTestClass {

    /*[-cpp;-NATIVE]
        #include <iostream>
    */

    /*[-cpp;-NATIVE]
        static int STATIC_INT = 10;
    */

    /*[-cpp;-NATIVE]
        std::cout << "STATIC_INT:" << STATIC_INT << std::endl;
        return STATIC_INT;
    */
    public static native int testStaticInt();

    /*[-cpp;-NATIVE]
        return boolArg;
    */
    public static native boolean testBoolean(boolean boolArg);

    /*[-cpp;-NATIVE]
        return byteArg;
    */
    public static native byte testByte(byte byteArg);

    /*[-cpp;-NATIVE]
        return charArg;
    */
    public static native char testChar(char charArg);

    /*[-cpp;-NATIVE]
        return shortArg;
    */
    public static native short testShort(short shortArg);

    /*[-cpp;-NATIVE]
        return intArg;
    */
    public static native int testInt(int intArg);

    /*[-cpp;-NATIVE]
        return longArg;
    */
    public static native long testLong(long longArg);

    /*[-cpp;-NATIVE]
        return floatArg;
    */
    public static native float testFloat(float floatArg);

    /*[-cpp;-NATIVE]
        return doubleArg;
    */
    public static native double testDouble(double doubleArg);

    /*[-cpp;-NATIVE]
        printf("boolean: %s\n", boolArg ? "true" : "false");
        printf("byte: %d\n", byteArg);
        printf("char: %c\n", charArg);
        printf("short: %d\n" , shortArg);
        printf("int: %d\n", intArg);
        printf("long: %l\n", longArg);
        printf("float: %f\n", floatArg);
        printf("double: %d\n", doubleArg);
        printf("byteBuffer: %d\n", byteBuffer [0]);
        printf("bool[0]: %s\n", boolArray [0]?"true" : "false");
        printf("char[0]: %c\n", charArray [0]);
        printf("short[0]: %d\n", shortArray [0]);
        printf("int[0]: %d\n", intArray [0]);
        printf("long[0]: %ll\n", longArray [0]);
        printf("float[0]: %f\n", floatArray [0]);
        printf("double[0]: %f\n", doubleArray [0]);
        printf("string: %s fuck this nuts\n", string);
        return true;
    */
    public static native boolean test(boolean boolArg, byte byteArg, char charArg, short shortArg, int intArg, long longArg,
                                      float floatArg, double doubleArg, Buffer byteBuffer, boolean[] boolArray, char[] charArray, short[] shortArray,
                                      int[] intArray, long[] longArray, float[] floatArray, double[] doubleArray, double[][] multidim,
                                      String string, Class classy, Throwable thr, Object obj);

    public static class TestInner {

        /*[-cpp;-NATIVE]
            static bool STATIC_INNER_BOOL = true;
        */

        /*[-cpp;-NATIVE]
            return STATIC_INNER_BOOL;
        */
        public static native boolean getStaticInnerBool();

        /*[-cpp;-NATIVE]
            return arg + 1;
        */
        public native static int testInner(int arg);
    }

    /*[-cpp;-NATIVE]
        static float STATIC_FLOAT = 33.0;
    */

    /*[-cpp;-NATIVE]
        return STATIC_FLOAT;
    */
    public static native float getStaticFloat();
}
