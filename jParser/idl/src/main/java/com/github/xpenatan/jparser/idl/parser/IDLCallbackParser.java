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
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.utils.Pair;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.idl.IDLClass;
import com.github.xpenatan.jparser.idl.IDLConstructor;
import com.github.xpenatan.jparser.idl.IDLMethod;
import java.util.ArrayList;

public class IDLCallbackParser {

    private final static String callbackMethodName = "setupCallback";

    public static void generateCallback(IDLDefaultCodeParser idlParser, JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classDeclaration, IDLClass idlClass) {


        ArrayList<IDLConstructor> constructors = idlClass.callback.constructors;
        if(constructors.size() == 1) {
            IDLConstructor idlConstructor = constructors.get(0);
            ConstructorDeclaration constructorDeclaration = IDLConstructorParser.getOrCreateConstructorDeclaration(idlParser, jParser, unit, classDeclaration, idlConstructor);

            if(constructorDeclaration.getBody().isEmpty()) {
                MethodDeclaration callbackSetupDeclaration = classDeclaration.addMethod(callbackMethodName, Modifier.Keyword.PRIVATE);
                ArrayList<Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>>> methods = createCallbackMethods(idlParser, jParser, unit, classDeclaration, idlClass);
                MethodDeclaration nativeMethod = IDLConstructorParser.setupConstructor(idlConstructor, classDeclaration, constructorDeclaration);
                idlParser.onIDLConstructorGenerated(jParser, idlConstructor, classDeclaration, constructorDeclaration, nativeMethod);
                MethodCallExpr caller = IDLMethodParser.createCaller(callbackSetupDeclaration);
                constructorDeclaration.getBody().addStatement(caller);
                idlParser.onIDLCallbackGenerated(jParser, idlClass, classDeclaration, callbackSetupDeclaration, methods);
            }
        }
        else {
            throw new RuntimeException("Callback need to have 1 constructor");
        }
    }


    private static ArrayList<Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>>> createCallbackMethods(IDLDefaultCodeParser idlParser, JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classDeclaration, IDLClass idlClass) {
        ArrayList<Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>>> methods = new ArrayList<>();
        IDLClass callbackClass = idlClass.callback;

        for(IDLMethod method : callbackClass.methods) {
            MethodDeclaration methodDeclaration = IDLMethodParser.generateAndAddMethodOnly(idlParser, jParser, unit, classDeclaration, method);
            MethodDeclaration internalMethod = methodDeclaration.clone();

            internalMethod.removeModifier(Modifier.Keyword.PUBLIC);
            internalMethod.addModifier(Modifier.Keyword.PRIVATE);

            NodeList<Parameter> parameters = internalMethod.getParameters();

            MethodCallExpr caller = IDLMethodParser.createCaller(methodDeclaration);

            String createFieldObjectCode = "";
            for(int i = 0; i < parameters.size(); i++) {
                Parameter parameter = parameters.get(i);
                String paramName = parameter.getNameAsString();
                Type type = parameter.getType();
                String typeStr = type.asString();
                String fieldName = paramName;

                if(type.isClassOrInterfaceType() && !typeStr.equals("String")) {
                    parameter.setType(PrimitiveType.longType());
                    fieldName = IDLMethodParser.generateFieldName(classDeclaration, typeStr, true);
                    String newBody = IDLMethodParser.CALLBACK_PARAM_TEMPLATE
                            .replace(IDLMethodParser.TEMPLATE_TEMP_FIELD, fieldName)
                            .replace(IDLMethodParser.TEMPLATE_TAG_TYPE, typeStr)
                            .replace(IDLMethodParser.TEMPLATE_TAG_PARAM, paramName);

                    createFieldObjectCode += newBody;
                }
                caller.addArgument(fieldName);
            }
            createFieldObjectCode = "{\n" + createFieldObjectCode + "}";
            BlockStmt blockStmt = StaticJavaParser.parseBlock(createFieldObjectCode);

            if(internalMethod.getType().isVoidType()) {
                blockStmt.addStatement(caller);
            }
            else {
                ReturnStmt returnStmt = new ReturnStmt();
                returnStmt.setExpression(caller);
                blockStmt.addStatement(returnStmt);
            }
            String internName = internalMethod.getNameAsString();
            internalMethod.setName("internal_" + internName);
            internalMethod.setBody(blockStmt);
            classDeclaration.addMember(internalMethod);

            Pair<MethodDeclaration, MethodDeclaration> methodDeclarationPair = new Pair<>(internalMethod, methodDeclaration);

            Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>> methodPair = new Pair<>(method, methodDeclarationPair);
            methods.add(methodPair);
        }
        return methods;
    }
}