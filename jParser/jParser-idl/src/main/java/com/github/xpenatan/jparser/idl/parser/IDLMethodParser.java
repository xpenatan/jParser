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
import com.github.javaparser.ast.expr.NameExpr;
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
import com.github.xpenatan.jparser.idl.IDLEnumClass;
import com.github.xpenatan.jparser.idl.IDLHelper;
import com.github.xpenatan.jparser.idl.IDLMethod;
import com.github.xpenatan.jparser.idl.IDLParameter;
import com.github.xpenatan.jparser.idl.IDLReader;
import com.github.xpenatan.jparser.idl.parser.data.IDLParameterData;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IDLMethodParser {

    public static final String NATIVE_METHOD = "internal_native_";

    public static final boolean ENABLE_NULL_CHECKING = false;

    // Return null. There are cases where C++ code return a null pointer
    static final String NULL_POINTER = "return [TYPE].NULL;";

    static final String GET_ENUM_TEMPLATE =
            "{\n" +
            "    int value = [METHOD];\n" +
            "    return [TYPE].MAP.get(value);\n" +
            "}";

    static final String CALLBACK_ENUM_TEMPLATE = "[TYPE].MAP.get([PARAM])";

    static final String GET_OBJECT_TEMPLATE =
            "{\n" +
            "    long pointer = [METHOD];\n" +
            "    if(pointer == 0) " + NULL_POINTER + "\n" +
            "    if([TYPE]_TEMP_GEN_[NUM] == null) [TYPE]_TEMP_GEN_[NUM] = [TYPE]." + IDLConstructorParser.EMPTY_INSTANCE_METHOD + "();\n" +
            "    [TYPE]_TEMP_GEN_[NUM].internal_reset(pointer, false);\n" +
            "    return [TYPE]_TEMP_GEN_[NUM];\n" +
            "}";

    static final String GET_NEW_OBJECT_TEMPLATE =
            "{\n" +
            "    long pointer = [METHOD];\n" +
            "    if(pointer == 0) " + NULL_POINTER + "\n" +
            "    [TYPE] [TYPE]_NEW = [TYPE]." + IDLConstructorParser.EMPTY_INSTANCE_METHOD + "();\n" +
            "    [TYPE]_NEW.internal_reset(pointer, [MEM_OWNED]);\n" +
            "    return [TYPE]_NEW;\n" +
            "}";

    static final String CALLBACK_REUSE_PARAM_TEMPLATE =
            "if([TYPE]_TEMP_GEN_[NUM] == null) [TYPE]_TEMP_GEN_[NUM] = [TYPE]." + IDLConstructorParser.EMPTY_INSTANCE_METHOD + "();\n" +
            "[TYPE]_TEMP_GEN_[NUM].internal_reset([PARAM], false);\n";

    static final String CALLBACK_NEW_PARAM_TEMPLATE =
            "[TYPE] [TYPE]_TEMP_GEN_[NUM] = [TYPE]." + IDLConstructorParser.EMPTY_INSTANCE_METHOD + "();\n" +
            "[TYPE]_TEMP_GEN_[NUM].internal_reset([PARAM], true);\n";

    static final String OPERATOR_OBJECT_TEMPLATE =
            "{\n" +
            "    [METHOD];\n" +
            "    return this;\n" +
            "}";

    static final String TEMPLATE_TEMP_FIELD = "[TYPE]_TEMP_GEN_[NUM]";
    static final String TEMPLATE_TEMP_STATIC_FIELD = "[TYPE]_TEMP_STATIC_GEN_[NUM]";

    static final String TEMPLATE_TAG_METHOD = "[METHOD]";

    static final String TEMPLATE_TAG_PARAM = "[PARAM]";

    static final String TEMPLATE_TAG_TYPE = "[TYPE]";

    static final String TEMPLATE_TAG_NUM = "[NUM]";

    static final String TEMPLATE_TAG_MEM_OWNED = "[MEM_OWNED]";

    public static void generateMethod(IDLDefaultCodeParser idlParser, JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration, IDLClass idlClass, IDLMethod idlMethod) {
        MethodDeclaration methodDeclaration = generateAndAddMethodOnly(idlParser, jParser, unit, classOrInterfaceDeclaration, idlMethod);
        if(methodDeclaration != null && idlParser.generateClass) {
            setupMethod(idlParser, jParser, idlMethod, classOrInterfaceDeclaration, methodDeclaration);
        }
    }

    public static MethodDeclaration generateAndAddMethodOnly(IDLDefaultCodeParser idlParser, JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classDeclaration, IDLMethod idlMethod) {
        if(idlMethod.skip) {
            return null;
        }
        String methodName = idlMethod.name;
        String renamedName = idlMethod.getRenamedName();
        if(renamedName != null) {
            methodName = renamedName;
        }

        Type returnType = null;

        MethodDeclaration containsMethod = containsMethod(idlParser, classDeclaration, idlMethod);
        if(containsMethod != null) {
            if(canGenerateMethod(containsMethod)) {
                returnType = containsMethod.getType();
            }
            else {
                return null;
            }
        }

        // Remove methods characters if it contains "__1", "__2", etc.
        String fixedMethodName = methodName.replaceFirst("__\\d$", "");
        String updatedMethodName = fixedMethodName;
        if(idlParser.idlRenaming != null) {
            updatedMethodName = idlParser.idlRenaming.getIDLMethodName(fixedMethodName);
        }
        ArrayList<IDLParameter> parameters = idlMethod.parameters;
        MethodDeclaration methodDeclaration = classDeclaration.addMethod(updatedMethodName, Modifier.Keyword.PUBLIC);
        methodDeclaration.setStatic(idlMethod.isStaticMethod);
        for(int i = 0; i < parameters.size(); i++) {
            IDLParameter idlParameter = parameters.get(i);
            String paramType = idlParameter.getJavaType();
            String paramName = idlParameter.name;
            Parameter parameter = methodDeclaration.addAndGetParameter(paramType, paramName);
            Type type = parameter.getType();
            JParserHelper.addMissingImportType(jParser, unit, type);
        }

        if(returnType == null) {
            String returnTypeStr = idlMethod.getJavaReturnType();
            returnType = StaticJavaParser.parseType(returnTypeStr);
        }
        methodDeclaration.setType(returnType);
        IDLDefaultCodeParser.setDefaultReturnValues(jParser, unit, returnType, methodDeclaration);
        return methodDeclaration;
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
        NativeMethodData methodData = new NativeMethodData();
        methodData.isStatic = idlMethod.isStaticMethod;
        methodData.isReturnValue = idlMethod.isReturnValue;
        methodData.isReturnNewObject = idlMethod.isReturnNewObject;
        methodData.isReturnMemoryOwned = idlMethod.isReturnMemoryOwned;
        if(idlMethod.returnClassType != null && idlMethod.returnClassType.isClass()) {
            IDLClass aClass = idlMethod.returnClassType.asClass();
            methodData.isNoDelete = aClass.classHeader.isNoDelete;
        }
        IDLReader idlReader = idlParser.idlReader;
        MethodDeclaration nativeMethodDeclaration = IDLMethodParser.prepareNativeMethod(idlReader, methodData, classDeclaration, methodDeclaration, idlMethod.name, idlMethod.operator, idlMethod.parameters);
        if(nativeMethodDeclaration != null) {
            idlParser.onIDLMethodGenerated(jParser, idlMethod, classDeclaration, methodDeclaration, nativeMethodDeclaration);
        }
    }

    public static MethodDeclaration prepareNativeMethod(IDLReader idlReader, NativeMethodData paramData, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, String methodName, String operator, ArrayList<IDLParameter> idlParameters) {
        NodeList<Parameter> methodParameters = methodDeclaration.getParameters();
        Type methodReturnType = methodDeclaration.getType();

        ArrayList<IDLParameterData> parameterArray = new ArrayList<>();
        for(int i = 0; i < methodParameters.size(); i++) {
            Parameter parameter = methodParameters.get(i);
            IDLParameterData data = new IDLParameterData();
            data.parameter = parameter;
            if(idlParameters != null) {
//                if((!(i < idlParameters.size()))) {
//                    System.out.println();
//                }
                data.idlParameter = idlParameters.get(i);
            }
            parameterArray.add(data);
        }

        MethodDeclaration nativeMethodDeclaration = generateNativeMethod(idlReader, methodName, parameterArray, methodReturnType, methodDeclaration.isStatic());
        if(!JParserHelper.containsMethod(classDeclaration, nativeMethodDeclaration)) {
            //Add native method if it does not exist
            classDeclaration.getMembers().add(nativeMethodDeclaration);
            // Now that we have the native method we setup the caller method.

            MethodCallExpr caller = createCaller(nativeMethodDeclaration);

            if(methodReturnType.isVoidType()) {
                // void types just call the method.
                IDLMethodParser.setupCallerParam(idlReader, paramData, caller, methodDeclaration.getParameters(), idlParameters);
                BlockStmt blockStmt = methodDeclaration.getBody().get();
                blockStmt.addStatement(caller);
            }
            else if(methodReturnType.isClassOrInterfaceType()) {
                // Class object needs to generate some additional code.
                // Needs to obtain the pointer and return a temp object.
                BlockStmt blockStmt = IDLMethodParser.generateReturnObject(idlReader, paramData, classDeclaration, methodDeclaration, caller, operator, idlParameters);
                methodDeclaration.setBody(blockStmt);
            }
            else {
                // Should be a primitive return type.
                ReturnStmt returnStmt = IDLMethodParser.getReturnStmt(methodDeclaration);
                IDLMethodParser.setupCallerParam(idlReader, paramData, caller, methodDeclaration.getParameters(), idlParameters);
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

    public static void setupCallerParam(IDLReader idlReader, NativeMethodData paramData, MethodCallExpr caller, NodeList<Parameter> methodParameters, ArrayList<IDLParameter> idlParameters) {
        boolean isAttribute = idlParameters == null;

        if(!paramData.isStatic) {
            caller.addArgument(IDLDefaultCodeParser.NATIVE_ADDRESS);
        }
        for(int i = 0; i < methodParameters.size(); i++) {
            Parameter parameter = methodParameters.get(i);
            IDLParameter idlParameter = null;
            if(idlParameters != null) {
                idlParameter = idlParameters.get(i);
            }
            Type type = parameter.getType();
            String typeName = type.asString();
            SimpleName name = parameter.getName();
            String variableName = name.getIdentifier();
            String paramName = parameter.getNameAsString();
            String expressionCode = paramName;
            IDLEnumClass idlEnum = idlReader.getEnum(typeName);
            if(idlEnum == null && type.isClassOrInterfaceType()) {
                boolean isArray = true;
                boolean isAny = false;
                if(idlParameter != null) {
                    //TODO create IDLParameter when is comming from attribute
                    isArray = idlParameter.isArray;
                    isAny = idlParameter.isAny;
                }
                if(isArray && !isAttribute || isAny) {
                    // Only methods parameter array needs to call getPointer()
                    String methodCall = paramName + "." + IDLDefaultCodeParser.NATIVE_VOID_ADDRESS;
                    if(ENABLE_NULL_CHECKING) {
                        expressionCode =  "(" + variableName + " != null ? " + methodCall + " : 0)";
                    }
                    else {
                        expressionCode = methodCall;
                    }
                }
                else if(!IDLHelper.isString(type.asClassOrInterfaceType())) {
                    //All methods must contain a base class to get its pointer
                    String methodCall = paramName + "." + IDLDefaultCodeParser.NATIVE_ADDRESS;
                    if(ENABLE_NULL_CHECKING) {
                        expressionCode =  "(" + variableName + " != null ? " + methodCall + " : 0)";
                    }
                    else {
                        expressionCode = methodCall;
                    }
                }
            }
            else if(idlEnum != null) {
                if(ENABLE_NULL_CHECKING) {
                    expressionCode =  "(" + variableName + " != null ? " + variableName + ".getValue() : 0)";
                }
                else {
                    expressionCode = variableName + ".getValue()";
                }
            }
            else if(type.isArrayType()) {
                //TODO implement array call
            }
            Expression expression = StaticJavaParser.parseExpression(expressionCode);
            caller.addArgument(expression);
        }
    }

    private static BlockStmt generateReturnObject(IDLReader idlReader, NativeMethodData paramData, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodCallExpr caller, String operator, ArrayList<IDLParameter> idlParameters) {
        Type methodReturnType = methodDeclaration.getType();
        String className = classDeclaration.getNameAsString();
        String returnTypeName = methodReturnType.toString();
        String newBody = null;


        IDLClass idlClass = idlReader.getClass(returnTypeName);
        IDLEnumClass idlEnum = idlReader.getEnum(returnTypeName);

        boolean isStatic = methodDeclaration.isStatic();
        boolean isRetSameAsClass = false;
        boolean isEnum = idlEnum != null;

        IDLMethodParser.setupCallerParam(idlReader, paramData, caller, methodDeclaration.getParameters(), idlParameters);

        boolean isReturnMemOwned = paramData.isReturnMemoryOwned;
        if(paramData.isNoDelete) {
            // When NoDelete it means that the library is responsible to delete it.
            isReturnMemOwned = false;
        }

        if(!operator.isEmpty() && className.equals(returnTypeName) && !paramData.isReturnValue) { // is pointer or ref
            // if its operator and return type is same as class name don't create temp object
            isRetSameAsClass = true;
        }

        String methodCaller = caller.toString();
        if(isEnum) {
                newBody = GET_ENUM_TEMPLATE
                        .replace(TEMPLATE_TAG_METHOD, methodCaller)
                        .replace(TEMPLATE_TAG_TYPE, returnTypeName);
        }
        else if(isRetSameAsClass) {
            newBody = OPERATOR_OBJECT_TEMPLATE
                    .replace(TEMPLATE_TAG_METHOD, methodCaller);
        }
        else {
            if(paramData.isReturnNewObject) {
                newBody = GET_NEW_OBJECT_TEMPLATE
                        .replace(TEMPLATE_TAG_METHOD, methodCaller)
                        .replace(TEMPLATE_TAG_MEM_OWNED, String.valueOf(isReturnMemOwned))
                        .replace(TEMPLATE_TAG_TYPE, returnTypeName);
            }
            else {
                String fieldName = generateFieldName(classDeclaration, returnTypeName, isStatic);;
                newBody = GET_OBJECT_TEMPLATE
                        .replace(TEMPLATE_TAG_METHOD, methodCaller)
                        .replace(TEMPLATE_TEMP_FIELD, fieldName)
                        .replace(TEMPLATE_TAG_TYPE, returnTypeName);
            }
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

    public static String generateFieldName(ClassOrInterfaceDeclaration classDeclaration, String fieldType, boolean isStatic) {
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
        if(!fieldByName.isPresent()) {
            FieldDeclaration fieldDeclaration;
            if(isStatic) {
                if(initializeStatic) {
                    MethodCallExpr expression = new MethodCallExpr(new NameExpr(fieldType), IDLConstructorParser.EMPTY_INSTANCE_METHOD);
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

    public static MethodDeclaration generateNativeMethod(IDLReader idlReader, String methodName, ArrayList<IDLParameterData> methodParameters, Type methodReturnType, boolean isStatic) {
        boolean isClassOrInterfaceType = methodReturnType.isClassOrInterfaceType();

        // Clone some generated idl method settings
        MethodDeclaration nativeMethod = new MethodDeclaration();
        nativeMethod.setName(NATIVE_METHOD + methodName);
        nativeMethod.setModifiers(Modifier.createModifierList(Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC, Modifier.Keyword.NATIVE));
        nativeMethod.removeBody();

        IDLEnumClass isEnum = idlReader.getEnum(methodReturnType.asString());

        if(!isStatic) {
            // Only generate addr if it's not a static method
            nativeMethod.addParameter("long", "this_addr");
        }

        for(int i = 0; i < methodParameters.size(); i++) {
            IDLParameterData idlParameterData = methodParameters.get(i);
            Parameter parameter = idlParameterData.parameter;
            String nameAsString = parameter.getNameAsString();
            Type type = parameter.getType();
            if(idlParameterData.isEnum()) {
                Type longType = StaticJavaParser.parseType(long.class.getSimpleName());
                nativeMethod.addParameter(longType, nameAsString);
            }
            else if(type.isPrimitiveType() || IDLHelper.isString(type)) {
                nativeMethod.addParameter(type.clone(), nameAsString);
            }
            else {
                String pointerMethod = nameAsString + IDLDefaultCodeParser.NATIVE_PARAM_ADDRESS;
                nativeMethod.addParameter("long", pointerMethod);
            }
        }
        if(isEnum != null) {
            Type type = StaticJavaParser.parseType(int.class.getSimpleName());
            nativeMethod.setType(type);
        }
        else if(isClassOrInterfaceType) {
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
            String paramType = parameter.getJavaType();
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

    public static class NativeMethodData {
        public boolean isStatic;
        public boolean isReturnValue;
        public boolean isReturnNewObject;
        public boolean isReturnMemoryOwned;
        public boolean isNoDelete;
    }
}
