package com.github.xpenatan.jParser.ffm;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.xpenatan.jParser.core.JParser;
import com.github.xpenatan.jParser.core.JParserItem;

/**
 * Interface for generating native C/C++ glue code for FFM.
 * Parallel to CppGenerator but decoupled from JNI dependencies.
 */
public interface FFMNativeCodeGenerator {
    void addNativeCode(Node node, String content);

    void addNativeCode(MethodDeclaration nativeMethod, String content);

    void addParseFile(JParser jParser, JParserItem parserItem);

    void generate(JParser jParser);
}

