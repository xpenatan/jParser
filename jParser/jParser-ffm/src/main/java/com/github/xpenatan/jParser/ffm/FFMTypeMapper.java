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
}

