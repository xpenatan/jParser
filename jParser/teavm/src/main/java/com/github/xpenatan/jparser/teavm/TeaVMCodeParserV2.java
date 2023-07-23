package com.github.xpenatan.jparser.teavm;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.idl.IDLMethod;
import com.github.xpenatan.jparser.idl.parser.IDLDefaultCodeParser;
import com.github.xpenatan.jparser.idl.IDLReader;

public class TeaVMCodeParserV2 extends IDLDefaultCodeParser {

    private static final String HEADER_CMD = "teaVM";

    private final String module;

    public TeaVMCodeParserV2(IDLReader idlReader, String module, String basePackage) {
        super(basePackage, HEADER_CMD, idlReader);
        this.module = module;
        generateClass = true;
    }

    @Override
    public void onIDLMethodGenerated(JParser jParser, IDLMethod idlMethod, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethodDeclaration) {
        // IDL parser generate our empty methods with default return values.
        // We now modify it to match teavm native calls

    }
}