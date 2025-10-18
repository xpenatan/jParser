package com.github.xpenatan.jparser.idl;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.xpenatan.jparser.core.codeparser.DefaultCodeParser;

public class IDLHelper {

    // Solution to change the default generated C++ code from webidl a custom one. Ex unsigned long long to uint64
    public static IDLTypeConverterListener cppConverter;
    public static IDLTypeConverterListener javaConverter;

    public static String removeMultipleSpaces(String in) {
        return in.replaceAll(" +", " ");
    }

    public static boolean isString(ClassOrInterfaceType classOrInterfaceType) {
        return classOrInterfaceType.getNameAsString().equals("String");
    }

    public static boolean isString(Type type) {
        return type.toString().equals("String");
    }

    public static String getTags(String line) {
        line = line.trim();
        int startIndex = line.indexOf("[");
        int endIndex = -1;
        if(startIndex != -1 && line.startsWith("[")) {
            int count = 0;
            for(int i = startIndex; i < line.length(); i++) {
                char c = line.charAt(i);
                if(c == '[') {
                    count++;
                }
                else if(c == ']') {
                    count--;
                }
                if(count == 0) {
                    endIndex = i;
                    break;
                }
            }
        }

        if(startIndex != -1 && endIndex != -1) {
            return line.substring(startIndex, endIndex + 1);
        }
        return "";
    }

    public static String getCPPReturnType(String idlType) {
        if(cppConverter != null) {
            String customType = cppConverter.onConvert(idlType);
            if(customType != null) {
                return customType;
            }
        }

        String type = "";
        if(idlType.equals("any") || idlType.equals("VoidPtr")) {
            type = "void*";
        }
        else if(idlType.contains("long long")) {
            type = idlType;
        }
        else if(idlType.contains("long")) {
            // long in webidl means int
            type = idlType.replace("long", "int");
        }
        else if(idlType.equals("DOMString")) {
            type = "char*";
        }
        else if(idlType.equals("octet")) {
            type = "unsigned char";
        }
        else if(idlType.contains("boolean")) {
            type = idlType.replace("boolean", "bool");
        }
        else if(idlType.contains("byte")) {
            type = idlType.replace("byte", "char");
        }
        else {
            type = idlType;
        }
        return type;
    }

    public static String getJavaType(String idlType) {
        return getJavaType(true, idlType);
    }

    public static String getJavaType(boolean useIDLArray, String idlType) {
        if(javaConverter != null) {
            String customType = javaConverter.onConvert(idlType);
            if(customType != null) {
                return customType;
            }
        }

        String type = "";
        if(idlType.contains("unsigned")) {
            // Java don't have unsigned
            idlType = idlType.replace("unsigned", "").trim();
        }

        boolean containsArray = idlType.contains("[]");
        if(containsArray) {
            idlType = idlType.replace("[]", "");
        }

        if(idlType.equals("any") || idlType.equals("VoidPtr")) {
            type = DefaultCodeParser.IDL_BASE_CLASS;
        }
        else if(idlType.contains("long long")) {
            type = "long";
        }
        else if(idlType.contains("long")) {
            type = "int";
        }
        else if(idlType.equals("DOMString")) {
            type = "String";
        }
        else if(idlType.equals("octet")) {
            type = "byte";
        }
        else {
            type = idlType;
        }
        if(containsArray) {
            type = type + "[]";
        }
        if(useIDLArray) {
            String idlArrayOrNull = getIDLArrayClassOrNull(type);
            if(idlArrayOrNull != null) {
                type = idlArrayOrNull;
            }
        }
        return type;
    }

    public static String getIDLArrayClassOrNull(String type) {
        // Convert array to IDL object arrays
        if(type.equals("int[]")) {
            type = "IDLArrayInt";
        }
        else if(type.equals("long[]") || type.equals("long long[]")) {
            type = "IDLArrayLong";
        }
        else if(type.equals("float[]")) {
            type = "IDLArrayFloat";
        }
        else if(type.equals("byte[]")) {
            type = "IDLArrayByte";
        }
        else if(type.equals("boolean[]")) {
            type = "IDLArrayBool";
        }
        else if(type.equals("double[]")) {
            type = "IDLArrayDouble";
        }
        else if(type.endsWith("[]")) {
            type = type.replace("[]", "");
            type = "IDLArray" + type;
            return type;
        }

        return type;
    }
}
