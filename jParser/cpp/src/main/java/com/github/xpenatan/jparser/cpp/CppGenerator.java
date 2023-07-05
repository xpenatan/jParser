package com.github.xpenatan.jparser.cpp;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserItem;

public interface CppGenerator {
    void addNativeCode(Node node, String content);

    void addNativeCode(MethodDeclaration methodDeclaration, String content);

    void addParseFile(JParser jParser, JParserItem parserItem);

    void generate(JParser jParser);
}