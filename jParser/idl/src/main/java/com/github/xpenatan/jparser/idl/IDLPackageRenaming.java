package com.github.xpenatan.jparser.idl;

public interface IDLPackageRenaming {
    String obtainNewPackage(String className, String classPackage);
}