package com.github.xpenatan.jparser.idl;

public interface IDLRenaming {
    default String obtainNewPackage(IDLClassOrEnum idlClassOrEnum, String classPackage) {
        return classPackage;
    }

    default String getIDLMethodName(String methodName) {
        return methodName;
    }

    default String getIDLEnumName(String enumName) {
        return enumName;
    }
}