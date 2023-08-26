package com.github.xpenatan.jparser.idl.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.Type;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.idl.IDLEnum;
import java.util.Optional;

public class IDLEnumParser {

    public static void generateEnum(IDLDefaultCodeParser idlParser, JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classDeclaration, IDLEnum idlEnum) {
        for(String anEnum : idlEnum.enums) {
            generateField(idlParser, jParser, unit, classDeclaration, anEnum);
        }
    }

    private static void generateField(IDLDefaultCodeParser idlParser, JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classDeclaration, String enumStr) {
        String enumVar = enumStr;
        if(enumVar.contains("::")) {
            enumVar = enumVar.split("::")[1];
        }

        Optional<FieldDeclaration> fieldByName = classDeclaration.getFieldByName(enumVar);
        if(fieldByName.isEmpty()) {
            Type intType = StaticJavaParser.parseType(int.class.getSimpleName());
            MethodCallExpr expression = new MethodCallExpr();

            String nativeMethodName = enumVar + "_NATIVE";

            expression.setName(nativeMethodName);
            FieldDeclaration fieldDeclaration = classDeclaration.addFieldWithInitializer(intType, enumVar, expression, Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL);

            MethodDeclaration nativeMethod = new MethodDeclaration();
            nativeMethod.setName(nativeMethodName);
            nativeMethod.setModifiers(Modifier.createModifierList(Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC, Modifier.Keyword.NATIVE));
            nativeMethod.removeBody();
            nativeMethod.setType(intType);
            classDeclaration.getMembers().add(nativeMethod);
            idlParser.onIDLEnumMethodGenerated(jParser, classDeclaration, enumStr, fieldDeclaration, nativeMethod);
        }
    }
}