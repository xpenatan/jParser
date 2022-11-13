package com.github.xpenatan.jparser.cpp;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserItem;
import com.github.xpenatan.jparser.core.codeparser.IDLDefaultCodeParser;
import com.github.xpenatan.jparser.core.idl.IDLFile;

public class CppCodeParser extends IDLDefaultCodeParser {

    CppGenerator cppGenerator;

    public CppCodeParser(String classpath, String jniDir) {
        this(null, classpath, jniDir);
    }

    public CppCodeParser(IDLFile idlFile, String classpath, String jniDir) {
        super("cpp", idlFile);
        cppGenerator = new NativeCPPGenerator(classpath, jniDir);
    }

    @Override
    protected void onIDLMethodGenerated(JParser jParser, CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration, MethodDeclaration methodDeclaration, boolean b) {

    }

    @Override
    protected void setJavaBodyNativeCMD(String content, MethodDeclaration methodDeclaration) {
        cppGenerator.addMethod(content, methodDeclaration);
    }

    @Override
    public void onParseClassStart(JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        super.onParseClassStart(jParser, unit, classOrInterfaceDeclaration);
    }

    @Override
    public void onParseClassEnd(JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        super.onParseClassEnd(jParser, unit, classOrInterfaceDeclaration);
    }

    @Override
    public void onParseFileStart(JParser jParser, JParserItem parserItem) {
        cppGenerator.reset();
    }

    @Override
    public void onParseFileEnd(JParser jParser, JParserItem parserItem) {
        cppGenerator.parseFile(jParser.sourceDir, parserItem.inputPath, parserItem.destinationPath);
    }
}