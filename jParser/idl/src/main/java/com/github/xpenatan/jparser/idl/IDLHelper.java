package com.github.xpenatan.jparser.idl;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

public class IDLHelper {
    public static String removeMultipleSpaces(String in) {
        return in.replaceAll(" +", " ");
    }

    public static boolean isString(ClassOrInterfaceType classOrInterfaceType) {
        return classOrInterfaceType.getNameAsString().equals("String");
    }

    public static String getCArray(String type) {
        if(type.equals("IDLBoolArray")) {
            return "bool *";
        }
        else if(type.equals("IDLIntArray")) {
            return "int *";
        }
        else if(type.equals("IDLFloatArray")) {
            return "float *";
        }
        else if(type.equals("IDLDoubleArray")) {
            return "double *";
        }
        else if(type.equals("IDLByteArray")) {
            return "char *";
        }
        return null;
    }

    public static boolean isString(Type type) {
        return type.toString().equals("String");
    }

    public static String convertEnumToInt(IDLReader idlReader, String type) {
        IDLEnum idlEnum = idlReader.getEnum(type);
        if(idlEnum != null) {
            // if parameter is enum then convert to int.
            type = "int";
        }
        return type;
    }
}
