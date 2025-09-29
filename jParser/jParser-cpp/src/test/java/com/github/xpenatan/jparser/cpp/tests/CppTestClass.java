package com.github.xpenatan.jparser.cpp.tests;

import java.nio.Buffer;

public class CppTestClass {

    /*[-JNI;-NATIVE]
        #include <iostream>
    */

    /*[-JNI;-NATIVE]
        static int STATIC_INT = 10;
    */

    /*[-JNI;-NATIVE]
        std::cout << "STATIC_INT:" << STATIC_INT << std::endl;
        return STATIC_INT;
    */
    public static native int testStaticInt();

    /*[-JNI;-NATIVE]
        return boolArg;
    */
    public static native boolean testBoolean(boolean boolArg);

    /*[-JNI;-NATIVE]
        return byteArg;
    */
    public static native byte testByte(byte byteArg);

    /*[-JNI;-NATIVE]
        return charArg;
    */
    public static native char testChar(char charArg);

    /*[-JNI;-NATIVE]
        return shortArg;
    */
    public static native short testShort(short shortArg);

    /*[-JNI;-NATIVE]
        return intArg;
    */
    public static native int testInt(int intArg);

    /*[-JNI;-NATIVE]
        return longArg;
    */
    public static native long testLong(long longArg);

    /*[-JNI;-NATIVE]
        return floatArg;
    */
    public static native float testFloat(float floatArg);

    /*[-JNI;-NATIVE]
        return doubleArg;
    */
    public static native double testDouble(double doubleArg);

    /*[-JNI;-NATIVE]
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

        /*[-JNI;-NATIVE]
            static bool STATIC_INNER_BOOL = true;
        */

        /*[-JNI;-NATIVE]
            return STATIC_INNER_BOOL;
        */
        public static native boolean getStaticInnerBool();

        /*[-JNI;-NATIVE]
            return arg + 1;
        */
        public native static int testInner(int arg);
    }

    /*[-JNI;-NATIVE]
        static float STATIC_FLOAT = 33.0;
    */

    /*[-JNI;-NATIVE]
        return STATIC_FLOAT;
    */
    public static native float getStaticFloat();
}
