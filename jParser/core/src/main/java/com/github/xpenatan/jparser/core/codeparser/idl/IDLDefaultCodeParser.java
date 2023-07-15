package com.github.xpenatan.jparser.core.codeparser.idl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.Type;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserHelper;
import com.github.xpenatan.jparser.idl.IDLAttribute;
import com.github.xpenatan.jparser.idl.IDLClass;
import com.github.xpenatan.jparser.idl.IDLConstructor;
import com.github.xpenatan.jparser.idl.IDLMethod;
import com.github.xpenatan.jparser.idl.IDLReader;
import java.util.ArrayList;

/**
 * @author xpenatan
 */
public class IDLDefaultCodeParser extends IDLClassGeneratorParser {

    public static final String CMD_IDL_SKIP = "-IDL_SKIP";

    protected boolean enableAttributeParsing = true;

    static final String CPOINTER_METHOD = "getCPointer()";

    public IDLDefaultCodeParser(String headerCMD, IDLReader idlReader) {
        super("", headerCMD, idlReader);
    }

    public IDLDefaultCodeParser(String basePackage, String headerCMD, IDLReader idlReader) {
        super(basePackage, headerCMD, idlReader);
    }

    @Override
    protected void setJavaBodyNativeCMD(String content, MethodDeclaration methodDeclaration) {
    }

    @Override
    protected boolean shouldRemoveCommentBlock(String headerCommands) {
        if(super.shouldRemoveCommentBlock(headerCommands)) {
            if(headerCommands.contains(CMD_IDL_SKIP)) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void onParseClassStart(JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        if(idlReader != null) {
            SimpleName name = classOrInterfaceDeclaration.getName();
            String nameStr = name.asString();
            IDLClass idlClass = idlReader.getClass(nameStr);
            if(idlClass != null) {

                if(generateClass) {
                    ArrayList<IDLConstructor> constructors = idlClass.constructors;
                    for(int i = 0; i < constructors.size(); i++) {
                        IDLConstructor idlConstructor = constructors.get(i);
                        IDLConstructorParser.generateConstructor(this, jParser, unit, classOrInterfaceDeclaration, idlClass, idlConstructor);
                    }
                }

                ArrayList<IDLMethod> methods = idlClass.methods;
                for(int i = 0; i < methods.size(); i++) {
                    IDLMethod idlMethod = methods.get(i);
                    IDLMethodParser.generateMethods(this, jParser, unit, classOrInterfaceDeclaration, idlClass, idlMethod);
                }
                if(enableAttributeParsing) {
                    ArrayList<IDLAttribute> attributes = idlClass.attributes;
                    for(int i = 0; i < attributes.size(); i++) {
                        IDLAttribute idlAttribute = attributes.get(i);
                        IDLAttributeParser.generateAttribute(this, jParser, unit, classOrInterfaceDeclaration, idlClass, idlAttribute);
                    }
                }
            }
        }
    }

    public static void setDefaultReturnValues(JParser jParser, CompilationUnit unit, Type returnType, MethodDeclaration idlMethodDeclaration) {
        if(!returnType.isVoidType()) {
            BlockStmt blockStmt = idlMethodDeclaration.getBody().get();
            ReturnStmt returnStmt = new ReturnStmt();
            if(returnType.isPrimitiveType()) {
                if(JParserHelper.isLong(returnType) || JParserHelper.isInt(returnType) || JParserHelper.isFloat(returnType) || JParserHelper.isDouble(returnType)) {
                    NameExpr returnNameExpr = new NameExpr();
                    returnNameExpr.setName("0");
                    returnStmt.setExpression(returnNameExpr);
                }
                else if(JParserHelper.isBoolean(returnType)) {
                    NameExpr returnNameExpr = new NameExpr();
                    returnNameExpr.setName("false");
                    returnStmt.setExpression(returnNameExpr);
                }
            }
            else {
                JParserHelper.addMissingImportType(jParser, unit, returnType);
                NameExpr returnNameExpr = new NameExpr();
                returnNameExpr.setName("null");
                returnStmt.setExpression(returnNameExpr);
            }
            blockStmt.addStatement(returnStmt);
        }
    }

    @Deprecated
    public void onIDLMethodGenerated(JParser jParser, IDLClass idlClass, IDLMethod idlMethod, CompilationUnit unit, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration idlMethodDeclaration, boolean isAttribute) {
    }


    public void onIDLMethodGenerated(JParser jParser, IDLMethod idlMethod, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethodDeclaration) {
    }

    /**
     * true to accept the idl method
     */
    @Deprecated
    public boolean filterIDLMethod(IDLClass idlClass, IDLMethod idlMethod) {
        return true;
    }

}