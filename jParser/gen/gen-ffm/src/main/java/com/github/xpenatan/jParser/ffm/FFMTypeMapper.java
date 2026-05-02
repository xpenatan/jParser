package com.github.xpenatan.jParser.ffm;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps Java types to FFM ValueLayout constants and C types for the FFM code generator.
 */
public class FFMTypeMapper {

    private static final Map<String, String> javaToValueLayout = new HashMap<>();
    private static final Map<String, String> javaToCType = new HashMap<>();
    private static final Map<String, String> javaToFFMCast = new HashMap<>();

    static {
        // Java primitive → ValueLayout constant name
        javaToValueLayout.put("long", "ValueLayout.JAVA_LONG");
        javaToValueLayout.put("int", "ValueLayout.JAVA_INT");
        javaToValueLayout.put("float", "ValueLayout.JAVA_FLOAT");
        javaToValueLayout.put("double", "ValueLayout.JAVA_DOUBLE");
        javaToValueLayout.put("boolean", "ValueLayout.JAVA_BOOLEAN");
        javaToValueLayout.put("short", "ValueLayout.JAVA_SHORT");
        javaToValueLayout.put("byte", "ValueLayout.JAVA_BYTE");
        javaToValueLayout.put("char", "ValueLayout.JAVA_CHAR");
        javaToValueLayout.put("String", "ValueLayout.ADDRESS");
        javaToValueLayout.put("java.lang.foreign.MemorySegment", "ValueLayout.ADDRESS");

        // Array types → ADDRESS layout (passed as MemorySegment pointers)
        javaToValueLayout.put("int[]", "ValueLayout.ADDRESS");
        javaToValueLayout.put("long[]", "ValueLayout.ADDRESS");
        javaToValueLayout.put("float[]", "ValueLayout.ADDRESS");
        javaToValueLayout.put("double[]", "ValueLayout.ADDRESS");
        javaToValueLayout.put("byte[]", "ValueLayout.ADDRESS");
        javaToValueLayout.put("short[]", "ValueLayout.ADDRESS");
        javaToValueLayout.put("boolean[]", "ValueLayout.ADDRESS");
        javaToValueLayout.put("char[]", "ValueLayout.ADDRESS");

        // Java primitive → C type for FFMGlue.cpp
        javaToCType.put("long", "int64_t");
        javaToCType.put("int", "int32_t");
        javaToCType.put("float", "float");
        javaToCType.put("double", "double");
        javaToCType.put("boolean", "int32_t");
        javaToCType.put("short", "int16_t");
        javaToCType.put("byte", "int8_t");
        javaToCType.put("char", "uint16_t");
        javaToCType.put("void", "void");
        javaToCType.put("String", "const char*");

        // Array types → C pointer types
        javaToCType.put("int[]", "int32_t*");
        javaToCType.put("long[]", "int64_t*");
        javaToCType.put("float[]", "float*");
        javaToCType.put("double[]", "double*");
        javaToCType.put("byte[]", "int8_t*");
        javaToCType.put("short[]", "int16_t*");
        javaToCType.put("boolean[]", "int32_t*");
        javaToCType.put("char[]", "uint16_t*");

        // Java primitive → cast needed in invokeExact return
        javaToFFMCast.put("long", "long");
        javaToFFMCast.put("int", "int");
        javaToFFMCast.put("float", "float");
        javaToFFMCast.put("double", "double");
        javaToFFMCast.put("boolean", "boolean");
        javaToFFMCast.put("short", "short");
        javaToFFMCast.put("byte", "byte");
        javaToFFMCast.put("char", "char");
    }

    /**
     * Returns the FFM ValueLayout constant for a Java type string.
     * Returns null if the type is not a known primitive/String.
     */
    public static String getValueLayout(String javaType) {
        return javaToValueLayout.get(javaType);
    }

    /**
     * Returns the C type for a Java type string (used in FFMGlue.cpp).
     */
    public static String getCType(String javaType) {
        String cType = javaToCType.get(javaType);
        return cType != null ? cType : "int64_t"; // default: object addresses are int64_t
    }

    /**
     * Returns the cast type for MethodHandle.invokeExact() return.
     */
    public static String getFFMCast(String javaType) {
        String cast = javaToFFMCast.get(javaType);
        return cast != null ? cast : "long"; // default: object addresses are long
    }

    /**
     * Returns true if the type is a known primitive type (including void).
     */
    public static boolean isPrimitive(String javaType) {
        return javaToCType.containsKey(javaType) && !javaType.equals("String");
    }

    /**
     * Returns true if the type is String.
     */
    public static boolean isString(String javaType) {
        return "String".equals(javaType);
    }

    /**
     * Gets the overload suffix character for a parameter type, similar to JNI mangling.
     * Used to disambiguate overloaded native function names.
     */
    public static String getOverloadSuffix(String javaType) {
        switch(javaType) {
            case "boolean": return "Z";
            case "byte": return "B";
            case "char": return "C";
            case "short": return "S";
            case "int": return "I";
            case "long": return "J";
            case "float": return "F";
            case "double": return "D";
            case "String": return "Ljava_lang_String_2";
            default: return "Ljava_lang_Object_2";
        }
    }

    // ==================== Array/Buffer Optimization Helpers ====================

    /**
     * Returns true if the type is a Java array type.
     */
    public static boolean isArrayType(String javaType) {
        return javaType.endsWith("[]");
    }

    /**
     * Returns FFM code to create a MemorySegment from a Java primitive array.
     * Example: "java.lang.foreign.MemorySegment.ofArray(myArray)"
     *
     * @param paramName the Java variable name of the array
     * @param javaType  the Java array type (e.g., "int[]", "float[]")
     * @return the FFM MemorySegment creation code
     */
    public static String getArraySegmentCode(String paramName, String javaType) {
        if(!isArrayType(javaType)) {
            throw new IllegalArgumentException("Not an array type: " + javaType);
        }
        return "java.lang.foreign.MemorySegment.ofArray(" + paramName + ")";
    }

    /**
     * Returns FFM code to create a MemorySegment from a direct ByteBuffer.
     * Example: "java.lang.foreign.MemorySegment.ofBuffer(myBuffer)"
     *
     * @param paramName the Java variable name of the ByteBuffer
     * @return the FFM MemorySegment creation code
     */
    public static String getBufferSegmentCode(String paramName) {
        return "java.lang.foreign.MemorySegment.ofBuffer(" + paramName + ")";
    }

    /**
     * Returns the element ValueLayout for an array type.
     * Example: "int[]" → "ValueLayout.JAVA_INT"
     *
     * @param arrayType the Java array type
     * @return the element ValueLayout, or null if not a known array type
     */
    public static String getArrayElementLayout(String arrayType) {
        if(!isArrayType(arrayType)) return null;
        String elementType = arrayType.replace("[]", "");
        return javaToValueLayout.get(elementType);
    }
}

