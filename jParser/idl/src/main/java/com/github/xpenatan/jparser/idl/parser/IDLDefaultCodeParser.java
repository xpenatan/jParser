package com.github.xpenatan.jparser.idl.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.utils.Pair;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserHelper;
import com.github.xpenatan.jparser.core.codeparser.CodeParserItem;
import com.github.xpenatan.jparser.idl.IDLAttribute;
import com.github.xpenatan.jparser.idl.IDLClass;
import com.github.xpenatan.jparser.idl.IDLConstructor;
import com.github.xpenatan.jparser.idl.IDLEnum;
import com.github.xpenatan.jparser.idl.IDLEnumItem;
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

    protected static final String CPOINTER_METHOD = "getNativeData().getCPointer()";
    protected static final String CPOINTER_ARRAY_METHOD = "getPointer()";

    public IDLDefaultCodeParser(String headerCMD, IDLReader idlReader, String cppDir) {
        super("", headerCMD, idlReader, cppDir);
    }

    public IDLDefaultCodeParser(String basePackage, String headerCMD, IDLReader idlReader, String cppDir) {
        super(basePackage, headerCMD, idlReader, cppDir);
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
    public void onParseClassStart(JParser jParser, CompilationUnit unit, TypeDeclaration classOrEnum) {
        super.onParseClassStart(jParser, unit, classOrEnum);
        if(idlReader != null) {
            SimpleName name = classOrEnum.getName();
            String nameStr = name.asString();
            IDLClass idlClass = idlReader.getClass(nameStr);
            if(idlClass != null) {
                ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration)classOrEnum;
                Optional<Comment> optionalComment = classOrEnum.getComment();
                if(optionalComment.isPresent()) {
                    Comment comment = optionalComment.get();
                    if(comment instanceof BlockComment) {
                        BlockComment blockComment = (BlockComment)optionalComment.get();
                        String headerCommands = CodeParserItem.obtainHeaderCommands(blockComment);
                        // Skip if method already exist with header code
                        if(headerCommands != null) {
                            if(headerCommands.contains(IDLDefaultCodeParser.CMD_IDL_SKIP)) {
                                //If skip is found then remove the method
                                idlClass.idlSkip = true;
                            }
                        }
                    }
                }

                if(idlClass.idlSkip) {
                    return;
                }

                if(generateClass) {
                    IDLConstructorParser.generateConstructor(this, jParser, unit, classOrInterfaceDeclaration, idlClass);
                    IDLDeConstructorParser.generateDeConstructor(this, jParser, unit, classOrInterfaceDeclaration, idlClass);
                }

                ArrayList<IDLMethod> methods = idlClass.methods;
                for(int i = 0; i < methods.size(); i++) {
                    IDLMethod idlMethod = methods.get(i);
                    IDLMethodParser.generateMethod(this, jParser, unit, classOrInterfaceDeclaration, idlClass, idlMethod);
                }
                if(enableAttributeParsing) {
                    ArrayList<IDLAttribute> attributes = idlClass.attributes;
                    for(int i = 0; i < attributes.size(); i++) {
                        IDLAttribute idlAttribute = attributes.get(i);
                        IDLAttributeParser.generateAttribute(this, jParser, unit, classOrInterfaceDeclaration, idlClass, idlAttribute);
                    }
                }

                if(generateClass) {
                    if(idlClass.callbackImpl != null) {
                        IDLCallbackParser.generateCallback(this, jParser, unit, classOrInterfaceDeclaration, idlClass);
                    }

                }
            }
            else {
                if(generateClass) {
                    IDLEnum idlEnum = idlReader.getEnum(nameStr);
                    if(idlEnum != null) {
                        EnumDeclaration enumDeclaration = (EnumDeclaration)classOrEnum;
                        IDLEnumParser.generateEnum(this, jParser, unit, enumDeclaration, idlEnum);
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
                if(JParserHelper.isLong(returnType) || JParserHelper.isInt(returnType) || JParserHelper.isFloat(returnType) || JParserHelper.isDouble(returnType) || JParserHelper.isShort(returnType)) {
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

    public void onIDLDeConstructorGenerated(JParser jParser, IDLClass idlClass, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration nativeMethodDeclaration) {
    }

    public void onIDLMethodGenerated(JParser jParser, IDLMethod idlMethod, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethodDeclaration) {
    }

    public void onIDLAttributeGenerated(JParser jParser, IDLAttribute idlAttribute, boolean isSet, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethodDeclaration) {
    }

    public void onIDLEnumMethodGenerated(JParser jParser, IDLEnum idlEnum, EnumDeclaration enumDeclaration, IDLEnumItem enumItem, MethodDeclaration nativeMethodDeclaration) {
    }

    public void onIDLCallbackGenerated(JParser jParser, IDLClass idlClass, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration callbackDeclaration, ArrayList<Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>>> methods) {
    }
}