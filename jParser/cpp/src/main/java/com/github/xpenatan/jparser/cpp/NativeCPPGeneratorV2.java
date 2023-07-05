package com.github.xpenatan.jparser.cpp;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserItem;

public class NativeCPPGeneratorV2 implements CppGenerator {

    private String jniDir;

    public NativeCPPGeneratorV2(String jniDir) {
        this.jniDir = jniDir;
    }

    @Override
    public void addNativeCode(Node node, String content) {
    }

    @Override
    public void addNativeCode(MethodDeclaration methodDeclaration, String content) {
    }

    @Override
    public void addParseFile(JParser jParser, JParserItem parserItem) {
        System.out.println();
    }

    @Override
    public void generate(JParser jParser) {

    }
}
