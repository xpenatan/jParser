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
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserHelper;
import com.github.xpenatan.jparser.idl.IDLClass;
import com.github.xpenatan.jparser.idl.IDLConstructor;
import com.github.xpenatan.jparser.idl.IDLHelper;
import com.github.xpenatan.jparser.idl.IDLParameter;
import com.github.xpenatan.jparser.idl.IDLReader;
import com.github.xpenatan.jparser.idl.parser.data.IDLParameterData;
import java.util.ArrayList;
import java.util.Optional;

public class IDLConstructorParser {

    public static void generateConstructor(IDLDefaultCodeParser idlParser, JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration, IDLClass idlClass) {
        ArrayList<IDLConstructor> constructors = idlClass.constructors;
        if(idlClass.callbackImpl == null) {
            // Generate constructors only if it's not callback
            for(int i = 0; i < constructors.size(); i++) {
                IDLConstructor idlConstructor = constructors.get(i);
                ConstructorDeclaration constructorDeclaration = IDLConstructorParser.getOrCreateConstructorDeclaration(idlParser, jParser, unit, classOrInterfaceDeclaration, idlConstructor);

                if(constructorDeclaration.getBody().isEmpty()) {
                    MethodDeclaration nativeMethod = IDLConstructorParser.setupConstructor(idlParser.idlReader, idlConstructor, classOrInterfaceDeclaration, constructorDeclaration);
                    idlParser.onIDLConstructorGenerated(jParser, idlConstructor, classOrInterfaceDeclaration, constructorDeclaration, nativeMethod);
                }
            }
        }

        // All classes contain a temp constructor so temp objects can be reused
        if(idlParser.baseClassUnit != unit) {
            ClassOrInterfaceDeclaration classDeclaration = JParserHelper.getClassDeclaration(unit);
            Optional<ConstructorDeclaration> constructorDeclarationOptional = classDeclaration.getConstructorByParameterTypes("byte", "char");
            if(constructorDeclarationOptional.isEmpty()) {
                //Only add temp constructor if it does not exist
                ConstructorDeclaration constructorDeclaration = classDeclaration.addConstructor(Modifier.Keyword.PROTECTED);
                constructorDeclaration.addParameter("byte", "b");
                constructorDeclaration.addParameter("char", "c");
                constructorDeclaration.addAnnotation(Deprecated.class);
                constructorDeclaration.setJavadocComment("Dummy constructor, used internally to creates objects without C++ pointer");

                MethodDeclaration createMethod = classDeclaration.addMethod("createInstance", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC);
                createMethod.setJavadocComment("@return An empty instance without a native address");
                createMethod.setType(classDeclaration.getNameAsString());
                createMethod.createBody().addStatement(
                        new ReturnStmt(
                                new ObjectCreationExpr().setType(classDeclaration.getNameAsString())
                                        .addArgument(StaticJavaParser.parseExpression("(byte)0"))
                                        .addArgument(StaticJavaParser.parseExpression("(char)0"))
                        )
                );
            }
        }

        for(ConstructorDeclaration constructor : classOrInterfaceDeclaration.getConstructors()) {
            addSuperTempConstructor(classOrInterfaceDeclaration, constructor);
        }
    }

    public static void addSuperTempConstructor(ClassOrInterfaceDeclaration classDeclaration, ConstructorDeclaration constructorDeclaration) {
        Optional<ClassOrInterfaceType> parent = classDeclaration.getExtendedTypes().getFirst();
        String parentName = "";
        if(parent.isPresent()) {
            parentName = parent.get().getNameAsString();
        }
        if(!parentName.equals(IDLDefaultCodeParser.IDL_BASE_CLASS)) {
            BlockStmt body = constructorDeclaration.getBody();
            Optional<Statement> first = body.getStatements().getFirst();
            boolean addSuperByte = true;
            if(first.isPresent()) {
                if(first.get().isExplicitConstructorInvocationStmt()) {
                    addSuperByte = false;
                }
            }
            if(addSuperByte) {
                Statement statement = StaticJavaParser.parseStatement("super((byte)1, (char)1);");
                constructorDeclaration.getBody().addStatement(0, statement);
            }
        }
    }

    public static ConstructorDeclaration getOrCreateConstructorDeclaration(IDLDefaultCodeParser idlParser, JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration, IDLConstructor idlConstructor) {
        ConstructorDeclaration constructorDeclaration = containsConstructor(classOrInterfaceDeclaration, idlConstructor);
        if(constructorDeclaration == null) {
            constructorDeclaration = classOrInterfaceDeclaration.addConstructor(Modifier.Keyword.PUBLIC);
            ArrayList<IDLParameter> parameters = idlConstructor.parameters;
            for(int i = 0; i < parameters.size(); i++) {
                IDLParameter parameter = parameters.get(i);
                String paramType = parameter.getJavaType();
                JParserHelper.addMissingImportType(jParser, unit, paramType);
                constructorDeclaration.addAndGetParameter(paramType, parameter.name);
            }
        }
        return constructorDeclaration;
    }

    public static MethodDeclaration setupConstructor(IDLReader idlReader, IDLConstructor idlConstructor, ClassOrInterfaceDeclaration classDeclaration, ConstructorDeclaration constructorDeclaration) {
        NodeList<Parameter> parameters = constructorDeclaration.getParameters();
        Type type = StaticJavaParser.parseType(classDeclaration.getNameAsString());
        boolean isStatic = true;

        String additionalName = "";

        // Constructor needs to have type in the name because there are some cases that multiple
        // parameters with different type will skip creating this native method if they are equal.
        // Since the parameters object is converter to long. It's impossible to override multiple methods
        // with the same pointers type.
        // TODO maybe add parameter type in all native method names.
        for(int i = 0; i < parameters.size(); i++) {
            Parameter parameter = parameters.get(i);
            String typeName = parameter.getType().toString();
            additionalName += "_" + typeName;
        }

        ArrayList<IDLParameterData> parameterArray = new ArrayList<>();
        ArrayList<IDLParameter> idlParameters = idlConstructor.parameters;
        for(int i = 0; i < parameters.size(); i++) {
            Parameter parameter = parameters.get(i);
            IDLParameter idlParameter = idlParameters.get(i);
            IDLParameterData data = new IDLParameterData();
            data.parameter = parameter;
            data.idlParameter = idlParameter;
            parameterArray.add(data);
        }

        String methodName = "create" + additionalName;
        MethodDeclaration nativeMethod = IDLMethodParser.generateNativeMethod(idlReader, methodName, parameterArray, type, isStatic);

        if(!JParserHelper.containsMethod(classDeclaration, nativeMethod)) {
            //Add native method if it does not exist
            classDeclaration.getMembers().add(nativeMethod);

            MethodCallExpr caller = IDLMethodParser.createCaller(nativeMethod);

            BlockStmt blockStmt = constructorDeclaration.getBody();
            IDLMethodParser.NativeMethodData paramData = new IDLMethodParser.NativeMethodData();
            paramData.isStatic = isStatic;
            IDLMethodParser.setupCallerParam(idlReader, paramData, caller, parameters, idlConstructor.parameters);

            String isMemoryOwned = String.valueOf(!idlConstructor.idlClass.classHeader.isNoDelete);

            Statement statement1 = StaticJavaParser.parseStatement("long addr = " + caller + ";");
            Statement statement2 = StaticJavaParser.parseStatement("internal_reset(addr, " + isMemoryOwned + ");");
            blockStmt.addStatement(statement1);
            blockStmt.addStatement(statement2);

            return nativeMethod;
        }
        return null;
    }

    public static ConstructorDeclaration containsConstructor(ClassOrInterfaceDeclaration classOrInterfaceDeclaration, IDLConstructor idlConstructor) {
        ArrayList<IDLParameter> parameters = idlConstructor.parameters;
        String[] paramTypes = new String[parameters.size()];
        for(int i = 0; i < parameters.size(); i++) {
            IDLParameter parameter = parameters.get(i);
            String paramType = parameter.getJavaType();
            paramTypes[i] = paramType;
        }
        Optional<ConstructorDeclaration> constructorDeclarationOptional = classOrInterfaceDeclaration.getConstructorByParameterTypes(paramTypes);
        return constructorDeclarationOptional.orElse(null);
    }
}