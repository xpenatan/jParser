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

    public static boolean isString(Type type) {
        return type.toString().equals("String");
    }
}
