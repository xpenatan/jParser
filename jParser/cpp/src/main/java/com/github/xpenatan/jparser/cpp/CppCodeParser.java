package com.github.xpenatan.jparser.cpp;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserItem;
import com.github.xpenatan.jparser.core.codeparser.IDLDefaultCodeParser;
import com.github.xpenatan.jparser.core.idl.IDLFile;
import java.io.File;

public class CppCodeParser extends IDLDefaultCodeParser {

    public static String getClassPath(String ... includes) {
        String classpath = System.getProperty("java.class.path") + File.pathSeparator;
        String newClassPath = "";
        String[] split = classpath.split(";");
        for(int i = 0; i < split.length; i++) {
            String path = split[i];
            for(int j = 0; j < includes.length; j++) {
                String include = includes[j];
                if(path.contains(include)) {
                    newClassPath += path+";";
                }
            }
        }
        return newClassPath;
    }

    CppGenerator cppGenerator;

    public CppCodeParser(String classpath, String jniDir) {
        this(null, classpath, jniDir);
    }

    public CppCodeParser(IDLFile idlFile, String classpath, String jniDir) {
        super("C++", idlFile);
        cppGenerator = new NativeCPPGenerator(classpath, jniDir);
    }

    @Override
    protected void onIDLMethodGenerated(JParser jParser, CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration, MethodDeclaration methodDeclaration, boolean b) {

    }

    @Override
    public boolean parseCodeBlock(Node node, String headerCommands, String content) {
        if(!super.parseCodeBlock(node, headerCommands, content)) {
            if(headerCommands.contains(CMD_NATIVE)) {
                cppGenerator.addNativeCode(content, node);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void setJavaBodyNativeCMD(String content, MethodDeclaration methodDeclaration) {
        cppGenerator.addNativeMethod(content, methodDeclaration);
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
    }

    @Override
    public void onParseFileEnd(JParser jParser, JParserItem parserItem) {
        cppGenerator.addParseFile(jParser.sourceDir, parserItem.inputPath, parserItem.destinationPath);
    }

    @Override
    public void onParseEnd(JParser jParser) {
        cppGenerator.generate();
    }
}