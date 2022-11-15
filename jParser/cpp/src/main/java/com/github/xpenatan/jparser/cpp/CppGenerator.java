package com.github.xpenatan.jparser.cpp;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;

public interface CppGenerator {

    public void addNativeMethod(String content, MethodDeclaration methodDeclaration);

    public void addNativeCode(String content, Node node);

    void addParseFile(String sourceBaseDir, String inputJavaPath, String destinationJavaPath);

    void generate();
}
