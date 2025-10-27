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
import com.github.xpenatan.jparser.idl.IDLParameter;
import com.github.xpenatan.jparser.idl.IDLReader;
import java.util.ArrayList;
import java.util.List;

public class IDLCallbackParser {

    private final static String callbackMethodName = "setupCallback";
    public final static String CALLBACK_INTERNAL_METHOD = "internal_";

    public static void generateCallback(IDLDefaultCodeParser idlParser, JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classDeclaration, IDLClass idlClass) {
        ArrayList<IDLConstructor> constructors = idlClass.callbackImpl.constructors;
        if(constructors.size() == 1) {
            IDLConstructor idlConstructor = constructors.get(0);
            ConstructorDeclaration constructorDeclaration = IDLConstructorParser.getOrCreateConstructorDeclaration(idlParser, jParser, unit, classDeclaration, idlConstructor);

            if(constructorDeclaration.getBody().isEmpty()) {
                MethodDeclaration callbackSetupDeclaration = classDeclaration.addMethod(callbackMethodName, Modifier.Keyword.PRIVATE);
                ArrayList<Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>>> methods = createCallbackMethods(idlParser, jParser, unit, classDeclaration, idlClass);

                IDLReader idlReader = idlParser.idlReader;
                MethodDeclaration nativeMethod = IDLConstructorParser.setupConstructor(idlReader, idlConstructor, classDeclaration, constructorDeclaration);
                idlParser.onIDLConstructorGenerated(jParser, idlConstructor, classDeclaration, constructorDeclaration, nativeMethod);
                MethodCallExpr caller = IDLMethodParser.createCaller(callbackSetupDeclaration);
                constructorDeclaration.getBody().addStatement(caller);
                idlParser.onIDLCallbackGenerated(jParser, idlClass, classDeclaration, callbackSetupDeclaration, methods);

                // Add super methods when its needed
                IDLConstructorParser.addSuperTempConstructor(classDeclaration, constructorDeclaration);
            }
        }
        else {
            throw new RuntimeException("Callback need to have 1 constructor");
        }
    }


    private static ArrayList<Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>>> createCallbackMethods(IDLDefaultCodeParser idlParser, JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classDeclaration, IDLClass idlClass) {
        ArrayList<Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>>> methods = new ArrayList<>();
        IDLClass callbackClass = idlClass.callbackImpl;

        for(IDLMethod method : callbackClass.methods) {
            MethodDeclaration methodDeclaration = IDLMethodParser.generateAndAddMethodOnly(idlParser, jParser, unit, classDeclaration, method);
            MethodDeclaration internalMethod = methodDeclaration.clone();
            methodDeclaration.removeModifier(Modifier.Keyword.PUBLIC);
            methodDeclaration.addModifier(Modifier.Keyword.PROTECTED);

            internalMethod.removeModifier(Modifier.Keyword.PUBLIC);
            internalMethod.addModifier(Modifier.Keyword.PRIVATE);

            NodeList<Parameter> internalParameters = internalMethod.getParameters();

            MethodCallExpr caller = IDLMethodParser.createCaller(methodDeclaration);

            String [] paramTypes = new String[internalParameters.size()];
            String createFieldObjectCode = "";
            for(int i = 0; i < internalParameters.size(); i++) {
                Parameter parameter = internalParameters.get(i);
                IDLParameter idlParameter = method.parameters.get(i);
                boolean isNewParam = idlParameter.isNewParam;
                boolean isEnum = idlParameter.isEnum();
                Type type = parameter.getType();
                boolean isClass = type.isClassOrInterfaceType();
                String paramName = parameter.getNameAsString();

                if(isClass) {
                    paramName += IDLDefaultCodeParser.NATIVE_PARAM_ADDRESS;
                    parameter.setName(paramName);
                }

                String typeStr = type.asString();
                String fieldName = paramName;

                if(isEnum) {
                    parameter.setType(StaticJavaParser.parseType("int")); //Enum must be int
                    fieldName = fieldName + "_enum";
                    String newBody = IDLMethodParser.CALLBACK_ENUM_TEMPLATE
                            .replace(IDLMethodParser.TEMPLATE_TAG_TYPE, typeStr)
                            .replace(IDLMethodParser.TEMPLATE_TAG_FIELD, fieldName)
                            .replace(IDLMethodParser.TEMPLATE_TAG_PARAM, paramName);
                    createFieldObjectCode += newBody;
                }
                else if(isClass && !typeStr.equals("String")) {
                    parameter.setType(PrimitiveType.longType());
                    if(isNewParam) {
                        fieldName = fieldName + "_new";
                        String newBody = IDLMethodParser.CALLBACK_NEW_PARAM_TEMPLATE
                                .replace(IDLMethodParser.TEMPLATE_TEMP_FIELD, fieldName)
                                .replace(IDLMethodParser.TEMPLATE_TAG_TYPE, typeStr)
                                .replace(IDLMethodParser.TEMPLATE_TAG_PARAM, paramName);

                        createFieldObjectCode += newBody;
                    }
                    else {
                        fieldName = IDLMethodParser.generateFieldName(classDeclaration, typeStr, true);
                        String newBody = IDLMethodParser.CALLBACK_REUSE_PARAM_TEMPLATE
                                .replace(IDLMethodParser.TEMPLATE_TEMP_FIELD, fieldName)
                                .replace(IDLMethodParser.TEMPLATE_TAG_TYPE, typeStr)
                                .replace(IDLMethodParser.TEMPLATE_TAG_PARAM, paramName);

                        createFieldObjectCode += newBody;
                    }
                }
                paramTypes[i] = parameter.getType().asString();
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
            internalMethod.setName(CALLBACK_INTERNAL_METHOD + internName);
            internalMethod.setBody(blockStmt);

            List<MethodDeclaration> methodExist = classDeclaration.getMethodsBySignature(internalMethod.getNameAsString(), paramTypes);
            if(methodExist != null && methodExist.size() > 0) {
                internalMethod = methodExist.get(0);
            }
            else {
                classDeclaration.addMember(internalMethod);
            }

            Pair<MethodDeclaration, MethodDeclaration> methodDeclarationPair = new Pair<>(internalMethod, methodDeclaration);

            Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>> methodPair = new Pair<>(method, methodDeclarationPair);
            methods.add(methodPair);
        }
        return methods;
    }
}