package com.github.xpenatan.jParser.cpp;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.xpenatan.jParser.core.JParser;
import com.github.xpenatan.jParser.core.JParserItem;

public interface CppGenerator {
    void addNativeCode(Node node, String content);

    void addNativeCode(MethodDeclaration nativeMethod, String content);

    void addParseFile(JParser jParser, JParserItem parserItem);

    void generate(JParser jParser);
}