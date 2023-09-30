package com.github.xpenatan.jparser.idl.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserHelper;
import com.github.xpenatan.jparser.idl.IDLClass;
import com.github.xpenatan.jparser.idl.IDLConstructor;
import com.github.xpenatan.jparser.idl.IDLParameter;
import java.util.ArrayList;
import java.util.Optional;

public class IDLConstructorParser {

    public static void generateConstructor(IDLDefaultCodeParser idlParser, JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration, IDLClass idlClass) {
        ArrayList<IDLConstructor> constructors = idlClass.constructors;
        for(int i = 0; i < constructors.size(); i++) {
            IDLConstructor idlConstructor = constructors.get(i);
            generateConstructor(idlParser, jParser, unit, classOrInterfaceDeclaration, idlConstructor);
        }

        // All classes contain a temp constructor so temp objects can be reused
        if(idlParser.baseClassUnit != unit) {
            ClassOrInterfaceDeclaration classDeclaration = JParserHelper.getClassDeclaration(unit);
            Optional<ConstructorDeclaration> constructorDeclarationOptional = classDeclaration.getConstructorByParameterTypes("byte");
            if(constructorDeclarationOptional.isEmpty()) {
                //Only add temp constructor if it does not exist
                ConstructorDeclaration constructorDeclaration = classDeclaration.addConstructor(Modifier.Keyword.PUBLIC);
                constructorDeclaration.addParameter("byte", "temp");
            }
        }

        for(ConstructorDeclaration constructor : classOrInterfaceDeclaration.getConstructors()) {
            addSuperTempConstructor(classOrInterfaceDeclaration, constructor);
        }
    }

    private static void addSuperTempConstructor(ClassOrInterfaceDeclaration classDeclaration, ConstructorDeclaration constructorDeclaration) {
        Optional<ClassOrInterfaceType> parent = classDeclaration.getExtendedTypes().getFirst();
        String parentName = "";
        if(parent.isPresent()) {
            parentName = parent.get().getNameAsString();
        }
        if(!parentName.equals("IDLBase")) {
            BlockStmt body = constructorDeclaration.getBody();
            Optional<Statement> first = body.getStatements().getFirst();
            boolean addSuperByte = true;
            if(first.isPresent()) {
                if(first.get().isExplicitConstructorInvocationStmt()) {
                    addSuperByte = false;
                }
            }
            if(addSuperByte) {
                Statement statement = StaticJavaParser.parseStatement("super((byte)1);");
                constructorDeclaration.getBody().addStatement(0, statement);
            }
        }
    }

    private static void generateConstructor(IDLDefaultCodeParser idlParser, JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration, IDLConstructor idlConstructor) {
        ConstructorDeclaration constructorDeclaration = containsConstructor(classOrInterfaceDeclaration, idlConstructor);
        if(constructorDeclaration == null) {
            constructorDeclaration = classOrInterfaceDeclaration.addConstructor(Modifier.Keyword.PUBLIC);
            ArrayList<IDLParameter> parameters = idlConstructor.parameters;
            for(int i = 0; i < parameters.size(); i++) {
                IDLParameter parameter = parameters.get(i);
                JParserHelper.addMissingImportType(jParser, unit, parameter.type);
                constructorDeclaration.addAndGetParameter(parameter.type, parameter.name);
            }
        }

        if(constructorDeclaration.getBody().isEmpty()) {
            setupConstructor(idlParser, jParser, idlConstructor, classOrInterfaceDeclaration, constructorDeclaration);
        }
    }

    private static void setupConstructor(IDLDefaultCodeParser idlParser, JParser jParser, IDLConstructor idlConstructor, ClassOrInterfaceDeclaration classDeclaration, ConstructorDeclaration constructorDeclaration) {
        NodeList<Parameter> parameters = constructorDeclaration.getParameters();
        Type type = StaticJavaParser.parseType(classDeclaration.getNameAsString());

        boolean isStatic = true;
        MethodDeclaration nativeMethod = IDLMethodParser.generateNativeMethod(false, "create", parameters, type, isStatic);

        if(!JParserHelper.containsMethod(classDeclaration, nativeMethod)) {
            //Add native method if it does not exist
            classDeclaration.getMembers().add(nativeMethod);

            MethodCallExpr caller = IDLMethodParser.createCaller(nativeMethod);

            BlockStmt blockStmt = constructorDeclaration.getBody();

            IDLMethodParser.setupCallerParam(isStatic, false, caller, parameters, null);

            Statement statement1 = StaticJavaParser.parseStatement("long addr = " + caller + ";");
            Statement statement2 = StaticJavaParser.parseStatement("initObject(addr, true);");
            blockStmt.addStatement(statement1);
            blockStmt.addStatement(statement2);

            idlParser.onIDLConstructorGenerated(jParser, idlConstructor, classDeclaration, constructorDeclaration, nativeMethod);
        }
    }

    private static ConstructorDeclaration containsConstructor(ClassOrInterfaceDeclaration classOrInterfaceDeclaration, IDLConstructor idlConstructor) {
        ArrayList<IDLParameter> parameters = idlConstructor.parameters;
        String[] paramTypes = new String[parameters.size()];
        for(int i = 0; i < parameters.size(); i++) {
            IDLParameter parameter = parameters.get(i);
            String paramType = parameter.type;
            paramTypes[i] = paramType;
        }
        Optional<ConstructorDeclaration> constructorDeclarationOptional = classOrInterfaceDeclaration.getConstructorByParameterTypes(paramTypes);
        return constructorDeclarationOptional.orElse(null);
    }
}
