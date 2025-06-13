package com.github.xpenatan.jparser.idl;

public interface IDLRenaming {
    default String obtainNewPackage(String className, String classPackage) {
        return classPackage;
    }

    default String getIDLMethodName(String methodName) {
        return methodName;
    }
}