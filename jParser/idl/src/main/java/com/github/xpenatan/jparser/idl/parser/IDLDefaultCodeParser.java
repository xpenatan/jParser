package com.github.xpenatan.jparser.idl.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.Type;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserHelper;
import com.github.xpenatan.jparser.core.codeparser.CodeParserItem;
import com.github.xpenatan.jparser.idl.IDLAttribute;
import com.github.xpenatan.jparser.idl.IDLClass;
import com.github.xpenatan.jparser.idl.IDLConstructor;
import com.github.xpenatan.jparser.idl.IDLEnum;
import com.github.xpenatan.jparser.idl.IDLMethod;
import com.github.xpenatan.jparser.idl.IDLReader;
import java.util.ArrayList;
import java.util.Optional;

/**
 * @author xpenatan
 */
public class IDLDefaultCodeParser extends IDLClassGeneratorParser {

    /**
     * Remove method and don't generate a new method
     */
    public static final String CMD_IDL_SKIP = "-IDL_SKIP";

    protected boolean enableAttributeParsing = true;

    protected static final String CPOINTER_METHOD = "getCPointer()";

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
                //Keep comment if its idl skip
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void onParseClassStart(JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        super.onParseClassStart(jParser, unit, classOrInterfaceDeclaration);
        if(idlReader != null) {
            SimpleName name = classOrInterfaceDeclaration.getName();
            String nameStr = name.asString();
            IDLClass idlClass = idlReader.getClass(nameStr);
            if(idlClass != null) {
                Optional<Comment> optionalComment = classOrInterfaceDeclaration.getComment();
                if(optionalComment.isPresent()) {
                    Comment comment = optionalComment.get();
                    if(comment instanceof BlockComment) {
                        BlockComment blockComment = (BlockComment)optionalComment.get();
                        String headerCommands = CodeParserItem.obtainHeaderCommands(blockComment);
                        // Skip if method already exist with header code
                        if(headerCommands != null) {
                            if(headerCommands.contains(IDLDefaultCodeParser.CMD_IDL_SKIP)) {
                                //If skip is found then remove the method
                                return;
                            }
                        }
                    }
                }

                if(generateClass) {
                    IDLConstructorParser.generateConstructor(this, jParser, unit, classOrInterfaceDeclaration, idlClass);
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
            else {
                if(generateClass) {
                    IDLEnum idlEnum = idlReader.getEnum(nameStr);
                    if(idlEnum != null) {
                        IDLEnumParser.generateEnum(this, jParser, unit, classOrInterfaceDeclaration, idlEnum);
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

    public void onIDLConstructorGenerated(JParser jParser, IDLConstructor idlConstructor, ClassOrInterfaceDeclaration classDeclaration, ConstructorDeclaration constructorDeclaration, MethodDeclaration nativeMethodDeclaration) {
    }

    public void onIDLMethodGenerated(JParser jParser, IDLMethod idlMethod, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethodDeclaration) {
    }

    public void onIDLAttributeGenerated(JParser jParser, IDLAttribute idlAttribute, boolean isSet, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethodDeclaration) {
    }

    public void onIDLEnumMethodGenerated(JParser jParser, ClassOrInterfaceDeclaration classDeclaration, String enumStr, FieldDeclaration fieldDeclaration, MethodDeclaration nativeMethodDeclaration) {
    }

}