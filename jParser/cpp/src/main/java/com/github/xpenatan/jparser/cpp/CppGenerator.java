package com.github.xpenatan.jparser.cpp;

import com.github.javaparser.ast.body.MethodDeclaration;

public interface CppGenerator {

    public void addMethod(String content, MethodDeclaration methodDeclaration);

    void reset();

    void parseFile(String sourceBaseDir, String inputJavaPath, String destinationJavaPath);
}
