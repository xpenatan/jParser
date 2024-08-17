package com.github.xpenatan.jparser.idl.parser;

import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserHelper;
import com.github.xpenatan.jparser.core.codeparser.CodeParserItem;
import com.github.xpenatan.jparser.core.codeparser.DefaultCodeParser;
import com.github.xpenatan.jparser.idl.IDLClass;
import com.github.xpenatan.jparser.idl.IDLHelper;
import com.github.xpenatan.jparser.idl.IDLMethod;
import com.github.xpenatan.jparser.idl.IDLParameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IDLMethodParser {

    public static final String NATIVE_METHOD = "internal_native_";

    static final String GET_OBJECT_TEMPLATE =
            "{\n" +
            "    long pointer = [METHOD];\n" +
            "    if(pointer == 0) return null;\n" +
            "    if([TYPE]_TEMP_GEN_[NUM] == null) [TYPE]_TEMP_GEN_[NUM] = new [TYPE]((byte)1, (char)1);\n" +
            "    [TYPE]_TEMP_GEN_[NUM].setCPointer(pointer);\n" +
            "    return [TYPE]_TEMP_GEN_[NUM];\n" +
            "}";

    static final String GET_TEMP_OBJECT_TEMPLATE =
            "{\n" +
            "    [METHOD];\n" +
            "    return [TYPE]_TEMP_GEN_[NUM];\n" +
            "}";

    static final String OPERATOR_OBJECT_TEMPLATE =
            "{\n" +
            "    [METHOD];\n" +
            "    return this;\n" +
            "}";

    static final String TEMPLATE_TEMP_FIELD = "[TYPE]_TEMP_GEN_[NUM]";
    static final String TEMPLATE_TEMP_STATIC_FIELD = "[TYPE]_TEMP_STATIC_GEN_[NUM]";

    static final String TEMPLATE_TAG_METHOD = "[METHOD]";

    static final String TEMPLATE_TAG_TYPE = "[TYPE]";

    static final String TEMPLATE_TAG_NUM = "[NUM]";

    public static void generateMethod(IDLDefaultCodeParser idlParser, JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration, IDLClass idlClass, IDLMethod idlMethod) {
        if(idlMethod.skip) {
            return;
        }

        String methodName = idlMethod.name;
        Type returnType = null;

        MethodDeclaration containsMethod = containsMethod(idlParser, classOrInterfaceDeclaration, idlMethod);
        if(containsMethod != null) {
            if(canGenerateMethod(containsMethod)) {
                returnType = containsMethod.getType();
            }
            else {
                return;
            }
        }

        // Remove methods characters if it contains "_1", "_2", etc.
        String fixedMethodName = methodName.replaceFirst("_\\d$", "");

        String updatedMethodName = idlParser.getIDLMethodName(fixedMethodName);
        ArrayList<IDLParameter> parameters = idlMethod.parameters;
        MethodDeclaration methodDeclaration = classOrInterfaceDeclaration.addMethod(updatedMethodName, Modifier.Keyword.PUBLIC);
        methodDeclaration.setStatic(idlMethod.isStaticMethod);
        for(int i = 0; i < parameters.size(); i++) {
            IDLParameter idlParameter = parameters.get(i);
            String paramType = idlParameter.type;
            String paramName = idlParameter.name;
            paramType = IDLHelper.convertEnumToInt(idlParser.idlReader, paramType);
            Parameter parameter = methodDeclaration.addAndGetParameter(paramType, paramName);
            Type type = parameter.getType();
            JParserHelper.addMissingImportType(jParser, unit, type);
        }

        if(returnType == null) {
            String returnTypeStr = IDLHelper.convertEnumToInt(idlParser.idlReader, idlMethod.returnType);
            returnType = StaticJavaParser.parseType(returnTypeStr);
        }
        methodDeclaration.setType(returnType);
        IDLDefaultCodeParser.setDefaultReturnValues(jParser, unit, returnType, methodDeclaration);

        if(idlParser.generateClass) {
            setupMethod(idlParser, jParser, idlMethod, classOrInterfaceDeclaration, methodDeclaration);
        }
    }

    public static boolean canGenerateMethod(MethodDeclaration containsMethod) {
        boolean isNative = containsMethod.isNative();
        Optional<Comment> optionalComment = containsMethod.getComment();
        if(optionalComment.isPresent()) {
            Comment comment = optionalComment.get();
            if(comment instanceof BlockComment) {
                BlockComment blockComment = (BlockComment)optionalComment.get();
                String headerCommands = CodeParserItem.obtainHeaderCommands(blockComment);
                // Skip if method already exist with header code
                if(headerCommands != null) {
                    if(headerCommands.contains(DefaultCodeParser.CMD_NATIVE)) {
                        return false;
                    }
                    else {
                        if(headerCommands.contains(IDLDefaultCodeParser.CMD_IDL_SKIP)) {
                            //If skip is found then remove the method
                            containsMethod.remove();
                        }
                        return false;
                    }
                }
            }
        }
        if(isNative) {
            // It's a dummy method. Remove it and let IDL generate it again.
            // This is useful to use a base method as an interface and let the generator create the real method.
            containsMethod.remove();
            return true;
        }
        else  {
            // if a simple method exist, keep it and don't let IDL generate the method.
            return false;
        }
    }

    private static void setupMethod(IDLDefaultCodeParser idlParser, JParser jParser, IDLMethod idlMethod, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration) {
        MethodDeclaration nativeMethodDeclaration = IDLMethodParser.prepareNativeMethod(idlMethod.isStaticMethod, idlMethod.isReturnValue, classDeclaration, methodDeclaration, idlMethod.name, idlMethod.operator, idlMethod.parameters);
        if(nativeMethodDeclaration != null) {
            idlParser.onIDLMethodGenerated(jParser, idlMethod, classDeclaration, methodDeclaration, nativeMethodDeclaration);
        }
    }

    public static MethodDeclaration prepareNativeMethod(boolean isStatic, boolean isReturnValue, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, String methodName, String operator, ArrayList<IDLParameter> idlParameters) {
        NodeList<Parameter> methodParameters = methodDeclaration.getParameters();
        Type methodReturnType = methodDeclaration.getType();
        MethodDeclaration nativeMethodDeclaration = generateNativeMethod(isReturnValue, methodName, methodParameters, methodReturnType, methodDeclaration.isStatic());
        if(!JParserHelper.containsMethod(classDeclaration, nativeMethodDeclaration)) {
            //Add native method if it does not exist
            classDeclaration.getMembers().add(nativeMethodDeclaration);
            // Now that we have the native method we setup the caller method.

            MethodCallExpr caller = createCaller(nativeMethodDeclaration);

            if(methodReturnType.isVoidType()) {
                // void types just call the method.
                IDLMethodParser.setupCallerParam(isStatic, false, caller, methodDeclaration.getParameters(), idlParameters);
                BlockStmt blockStmt = methodDeclaration.getBody().get();
                blockStmt.addStatement(caller);
            }
            else if(methodReturnType.isClassOrInterfaceType()) {
                // Class object needs to generate some additional code.
                // Needs to obtain the pointer and return a temp object.
                BlockStmt blockStmt = IDLMethodParser.generateTempObjects(isReturnValue, classDeclaration, methodDeclaration, nativeMethodDeclaration, caller, operator, idlParameters);
                methodDeclaration.setBody(blockStmt);
            }
            else {
                // Should be a primitive return type.
                ReturnStmt returnStmt = IDLMethodParser.getReturnStmt(methodDeclaration);
                IDLMethodParser.setupCallerParam(isStatic, false, caller, methodDeclaration.getParameters(), idlParameters);
                returnStmt.setExpression(caller);
            }
            return nativeMethodDeclaration;
        }
        return null;
    }

    public static MethodCallExpr createCaller(MethodDeclaration nativeMethodDeclaration) {
        String nativeMethodName = nativeMethodDeclaration.getNameAsString();
        MethodCallExpr caller = new MethodCallExpr();
        caller.setName(nativeMethodName);
        return caller;
    }

    public static void setupCallerParam(boolean isStatic, boolean isReturnValue, MethodCallExpr caller, NodeList<Parameter> methodParameters, ArrayList<IDLParameter> idlParameters) {
        if(!isStatic) {
            caller.addArgument("(long)" + IDLDefaultCodeParser.CPOINTER_METHOD);
        }
        for(int i = 0; i < methodParameters.size(); i++) {
            Parameter parameter = methodParameters.get(i);
            IDLParameter idlParameter = null;
            if(idlParameters != null) {
                idlParameter = idlParameters.get(i);
            }
            Type type = parameter.getType();
            SimpleName name = parameter.getName();
            String variableName = name.getIdentifier();
            String paramName = parameter.getNameAsString();
            if(type.isClassOrInterfaceType()) {
                boolean isArray = true;
                if(idlParameter != null) {
                    //TODO create IDLParameter when is comming from attribute
                    isArray = idlParameter.isArray;
                }
                if(isArray && IDLHelper.getCArray(type.asClassOrInterfaceType().getNameAsString()) != null) {
                    String methodCall = paramName + ".getPointer()";
                    paramName =  "(long)(" + variableName + " != null ? " + methodCall + " : 0)";
                }
                else if(!IDLHelper.isString(type.asClassOrInterfaceType())) {
                    //All methods must contain a base class to get its pointer
                    String methodCall = paramName + ".getCPointer()";
                    paramName =  "(long)(" + variableName + " != null ? " + methodCall + " : 0)";
                }
            }
            else if(type.isArrayType()) {
                //TODO implement array call
            }
            Expression expression = StaticJavaParser.parseExpression(paramName);
            caller.addArgument(expression);
        }
    }

    private static BlockStmt generateTempObjects(boolean isReturnValue, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethodDeclaration, MethodCallExpr caller, String operator, ArrayList<IDLParameter> idlParameters) {
        Type methodReturnType = methodDeclaration.getType();
        String className = classDeclaration.getNameAsString();
        String returnTypeName = methodReturnType.toString();
        String newBody = null;

        boolean isStatic = methodDeclaration.isStatic();

        boolean isRetSameAsClass = false;

        if(!operator.isEmpty() && className.equals(returnTypeName)) {
            // if its operator and return type is same as class name don't create temp object
            isRetSameAsClass = true;
        }
        String fieldName =  "";
        if(!isRetSameAsClass) {
            fieldName = generateFieldName(classDeclaration, returnTypeName, isStatic);
        }

        IDLMethodParser.setupCallerParam(isStatic, isReturnValue, caller, methodDeclaration.getParameters(), idlParameters);

        String methodCaller = caller.toString();

        if(isRetSameAsClass) {
            newBody = OPERATOR_OBJECT_TEMPLATE
                    .replace(TEMPLATE_TAG_METHOD, methodCaller);
        }
        else {
            newBody = GET_OBJECT_TEMPLATE
                    .replace(TEMPLATE_TAG_METHOD, methodCaller)
                    .replace(TEMPLATE_TEMP_FIELD, fieldName)
                    .replace(TEMPLATE_TAG_TYPE, returnTypeName);
        }

        BlockStmt body = null;
        try {
            BodyDeclaration<?> bodyDeclaration = StaticJavaParser.parseBodyDeclaration(newBody);
            InitializerDeclaration initializerDeclaration = (InitializerDeclaration)bodyDeclaration;
            body = initializerDeclaration.getBody();
        }
        catch(Throwable t) {
            System.err.println("Error Class: " + className + "\n" + newBody);
            throw t;
        }
        return body;
    }

    private static String generateFieldName(ClassOrInterfaceDeclaration classDeclaration, String fieldType, boolean isStatic) {
        // Will return a temp object.
        // Java variable will be created by checking its class, name and number.
        // if the temp object already exist it will increment variable number and create it.

        int i = 0;
        while(true) {
            String fieldName = getFieldName(fieldType, i, isStatic);
            String name = generateFieldName(fieldName, classDeclaration, fieldType, isStatic, Modifier.Keyword.PRIVATE, false);
            if(!name.isEmpty()) {
                return name;
            }
            i++;
        }
    }

    public static String generateFieldName(String fieldName, ClassOrInterfaceDeclaration classDeclaration, String fieldType, boolean isStatic, Modifier.Keyword keyword, boolean initializeStatic) {
        Optional<FieldDeclaration> fieldByName = classDeclaration.getFieldByName(fieldName);
        if(fieldByName.isEmpty()) {
            FieldDeclaration fieldDeclaration;
            if(isStatic) {
                if(initializeStatic) {
                    ObjectCreationExpr expression = new ObjectCreationExpr();
                    expression.setType(fieldType);
                    expression.addArgument(StaticJavaParser.parseExpression("(byte)1"));
                    expression.addArgument(StaticJavaParser.parseExpression("(char)1"));
                    fieldDeclaration = classDeclaration.addFieldWithInitializer(fieldType, fieldName, expression, Modifier.Keyword.STATIC, keyword, Modifier.Keyword.FINAL);
                }
                else {
                    fieldDeclaration = classDeclaration.addField(fieldType, fieldName, Modifier.Keyword.STATIC, keyword);
                }
            }
            else {
                fieldDeclaration = classDeclaration.addField(fieldType, fieldName, keyword);
            }
            Position begin = new Position(0, 0);
            Position end = new Position(0, 0);
            Range range = new Range(begin, end);
            fieldDeclaration.setRange(range);
            return fieldName;
        }
        return "";
    }

    private static String getFieldName(String type, int number, boolean isStatic) {
        if(isStatic) {
            return TEMPLATE_TEMP_STATIC_FIELD.replace(TEMPLATE_TAG_TYPE, type).replace(TEMPLATE_TAG_NUM, String.valueOf(number));
        }
        else {
            return TEMPLATE_TEMP_FIELD.replace(TEMPLATE_TAG_TYPE, type).replace(TEMPLATE_TAG_NUM, String.valueOf(number));
        }
    }

    public static MethodDeclaration generateNativeMethod(boolean isReturnValue, String methodName, NodeList<Parameter> methodParameters, Type methodReturnType, boolean isStatic) {
        boolean isClassOrInterfaceType = methodReturnType.isClassOrInterfaceType();

        // Clone some generated idl method settings
        MethodDeclaration nativeMethod = new MethodDeclaration();
        nativeMethod.setName(NATIVE_METHOD + methodName);
        nativeMethod.setModifiers(Modifier.createModifierList(Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC, Modifier.Keyword.NATIVE));
        nativeMethod.removeBody();

        if(!isStatic) {
            // Only generate addr if it's not a static method
            nativeMethod.addParameter("long", "this_addr");
        }

        for(int i = 0; i < methodParameters.size(); i++) {
            Parameter parameter = methodParameters.get(i);
            String nameAsString = parameter.getNameAsString();
            Type type = parameter.getType();
            if(type.isPrimitiveType() || IDLHelper.isString(type)) {
                nativeMethod.addParameter(type.clone(), nameAsString);
            }
            else {
                String pointerMethod = nameAsString + "_addr";
                nativeMethod.addParameter("long", pointerMethod);
            }
        }
        if(isClassOrInterfaceType) {
            // If the return type is an object we need to return a pointer.
            Type type = StaticJavaParser.parseType(long.class.getSimpleName());
            nativeMethod.setType(type);
        }
        else {
            nativeMethod.setType(methodReturnType);
        }
        return nativeMethod;
    }

    public static ReturnStmt getReturnStmt(MethodDeclaration idlMethodDeclaration) {
        BlockStmt blockStmt = idlMethodDeclaration.getBody().get();
        NodeList<Statement> statements = blockStmt.getStatements();
        if(statements.size() > 0) {
            // Find the return block and add the caller
            Statement statement = blockStmt.getStatement(0);
            return (ReturnStmt)statement;
        }
        else {
            // should not go here
            throw new RuntimeException("Should not go here");
        }
    }

    private static MethodDeclaration containsMethod(IDLDefaultCodeParser idlParser, ClassOrInterfaceDeclaration classOrInterfaceDeclaration, IDLMethod idlMethod) {
        ArrayList<IDLParameter> parameters = idlMethod.parameters;
        String[] paramTypes = new String[parameters.size()];

        for(int i = 0; i < parameters.size(); i++) {
            IDLParameter parameter = parameters.get(i);
            String paramType = parameter.type;
            paramType = IDLHelper.convertEnumToInt(idlParser.idlReader, paramType);
            paramTypes[i] = paramType;
        }
        List<MethodDeclaration> methods = JParserHelper.getMethodsByName(classOrInterfaceDeclaration, idlMethod.name);

        if(methods.size() > 0) {
            for(MethodDeclaration method : methods) {
                NodeList<Parameter> methodParams = method.getParameters();
                int methodParamsSize = methodParams.size();
                if(methodParamsSize == paramTypes.length) {
                    if(methodParamsSize == 0) {
                        return method;
                    }
                    int equalsCount = 0;
                    for(int i = 0; i < methodParamsSize; i++) {
                        Parameter parameter = methodParams.get(i);
                        String paramType = paramTypes[i];
                        String nameAsString = parameter.getType().asString();
                        // remove package from type if exist
                        String[] split = nameAsString.split("\\.");
                        String type = split[split.length-1];
                        if(type.equals(paramType)) {
                            equalsCount++;
                        }
                    }
                    if(equalsCount == methodParamsSize) {
                        return method;
                    }
                }
            }
        }
        return null;
    }

}
