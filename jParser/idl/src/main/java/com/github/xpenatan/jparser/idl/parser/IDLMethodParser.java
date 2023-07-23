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
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserHelper;
import com.github.xpenatan.jparser.core.codeparser.CodeParserItem;
import com.github.xpenatan.jparser.core.codeparser.DefaultCodeParser;
import com.github.xpenatan.jparser.idl.IDLClass;
import com.github.xpenatan.jparser.idl.IDLMethod;
import com.github.xpenatan.jparser.idl.IDLParameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IDLMethodParser {

    static final String GET_OBJECT_TEMPLATE = "" +
            "{\n" +
            "    long pointer = [METHOD];\n" +
            "    [TYPE]_TEMP_GEN_[NUM].setPointer(pointer);\n" +
            "    return [TYPE]_TEMP_GEN_[NUM];\n" +
            "}";

    static final String GET_TEMP_OBJECT_TEMPLATE = "" +
            "{\n" +
            "    [METHOD];\n" +
            "    return [TYPE]_TEMP_GEN_[NUM];\n" +
            "}";

    static final String TEMPLATE_TEMP_FIELD = "[TYPE]_TEMP_GEN_[NUM]";
    static final String TEMPLATE_NEW_FIELD = "[TYPE]_NEW_GEN_[NUM]";
    static final String TEMPLATE_TEMP_STATIC_FIELD = "[TYPE]_TEMP_STATIC_GEN_[NUM]";
    static final String TEMPLATE_NEW_STATIC_FIELD = "[TYPE]_NEW_STATIC_GEN_[NUM]";

    static final String TEMPLATE_TAG_METHOD = "[METHOD]";

    static final String TEMPLATE_TAG_TYPE = "[TYPE]";

    static final String TEMPLATE_TAG_NUM = "[NUM]";

    public static void generateMethods(IDLDefaultCodeParser idlParser, JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration, IDLClass idlClass, IDLMethod idlMethod) {
        if(idlMethod.skip) {
            return;
        }

        String methodName = idlMethod.name;

        MethodDeclaration containsMethod = containsMethod(classOrInterfaceDeclaration, idlMethod);
        if(containsMethod != null) {
            boolean isNative = containsMethod.isNative();
            boolean isStatic = containsMethod.isStatic();
            boolean containsBlockComment = false;
            Optional<Comment> optionalComment = containsMethod.getComment();
            if(optionalComment.isPresent()) {
                Comment comment = optionalComment.get();
                if(comment instanceof BlockComment) {
                    BlockComment blockComment = (BlockComment)optionalComment.get();
                    String headerCommands = CodeParserItem.obtainHeaderCommands(blockComment);
                    // Skip if method already exist with header code
                    if(headerCommands != null) {
                        if(headerCommands.contains(DefaultCodeParser.CMD_NATIVE)) {
                            return;
                        }
                        else {
                            if(headerCommands.contains(IDLDefaultCodeParser.CMD_IDL_SKIP)) {
                                //If skip is found then remove the method
                                containsMethod.remove();
                            }
                            return;
                        }
                    }
                }
            }
            if(isNative) {
                // It's a dummy method. Remove it and let IDL generate it again.
                // This is useful to use a base method as an interface and let the generator create the real method.
                containsMethod.remove();
            }
            if(!isNative && !isStatic) {
                // if a simple method exist, keep it and don't let IDL generate the method.
                return;
            }
        }

        if(!idlParser.filterIDLMethod(idlClass, idlMethod)) {
            return;
        }

        ArrayList<IDLParameter> parameters = idlMethod.parameters;
        MethodDeclaration methodDeclaration = classOrInterfaceDeclaration.addMethod(methodName, Modifier.Keyword.PUBLIC);
        methodDeclaration.setStatic(idlMethod.isStaticMethod);
        for(int i = 0; i < parameters.size(); i++) {
            IDLParameter idlParameter = parameters.get(i);
            String paramType = idlParameter.type;
            String paramName = idlParameter.name;
            Parameter parameter = methodDeclaration.addAndGetParameter(paramType, paramName);
            Type type = parameter.getType();
            JParserHelper.addMissingImportType(jParser, unit, type);
        }

        Type returnType = StaticJavaParser.parseType(idlMethod.returnType);
        methodDeclaration.setType(returnType);
        IDLDefaultCodeParser.setDefaultReturnValues(jParser, unit, returnType, methodDeclaration);

        if(!idlParser.generateClass) {
            idlParser.onIDLMethodGenerated(jParser, idlClass, idlMethod,  unit, classOrInterfaceDeclaration, methodDeclaration, false);
        }
        else {
            setupMethod(idlParser, jParser, idlMethod, classOrInterfaceDeclaration, methodDeclaration);
        }
    }

    private static void setupMethod(IDLDefaultCodeParser idlParser, JParser jParser, IDLMethod idlMethod, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration) {
        MethodDeclaration nativeMethodDeclaration = generateNativeMethod(idlMethod, classDeclaration, methodDeclaration);
        if(!JParserHelper.containsMethod(classDeclaration, nativeMethodDeclaration)) {
            //Add native method if it does not exist
            classDeclaration.getMembers().add(nativeMethodDeclaration);
            // Now that we have the native method we setup the caller method.

            MethodCallExpr caller = createCaller(nativeMethodDeclaration);

            Type methodReturnType = methodDeclaration.getType();

            if(methodReturnType.isVoidType()) {
                // void types just call the method.
                setupCallerParam(idlMethod, caller, methodDeclaration, null);
                BlockStmt blockStmt = methodDeclaration.getBody().get();
                blockStmt.addStatement(caller);
            }
            else if(methodReturnType.isClassOrInterfaceType()) {
                // Class object needs to generate some additional code.
                // Needs to obtain the pointer and return a temp object.
                BlockStmt blockStmt = generateTempObjects(idlMethod, classDeclaration, methodDeclaration, nativeMethodDeclaration, caller);
                methodDeclaration.setBody(blockStmt);
            }
            else {
                // Should be a primitive return type.
                ReturnStmt returnStmt = getReturnStmt(methodDeclaration);
                setupCallerParam(idlMethod, caller, methodDeclaration, null);
                returnStmt.setExpression(caller);
            }
            idlParser.onIDLMethodGenerated(jParser, idlMethod, classDeclaration, methodDeclaration, nativeMethodDeclaration);
        }
    }

    private static MethodCallExpr createCaller(MethodDeclaration nativeMethodDeclaration) {
        String nativeMethodName = nativeMethodDeclaration.getNameAsString();
        MethodCallExpr caller = new MethodCallExpr();
        caller.setName(nativeMethodName);
        return caller;
    }

    private static void setupCallerParam(IDLMethod idlMethod, MethodCallExpr caller, MethodDeclaration methodDeclaration, String tempFieldName) {
        boolean isStatic = idlMethod.isStaticMethod;
        boolean isReturnValue = idlMethod.isReturnValue;
        NodeList<Parameter> methodParameters = methodDeclaration.getParameters();

        if(isReturnValue && tempFieldName != null) {
            caller.addArgument(tempFieldName + "." + IDLDefaultCodeParser.CPOINTER_METHOD);
        }

        if(!isStatic) {
            caller.addArgument(IDLDefaultCodeParser.CPOINTER_METHOD);
        }

        for(int i = 0; i < methodParameters.size(); i++) {
            Parameter parameter = methodParameters.get(i);
            Type type = parameter.getType();
            String paramName = parameter.getNameAsString();
            if(type.isClassOrInterfaceType()) {
                //All methods must contain a base class to get its pointer
                paramName = paramName + ".getCPointer()";
            }
            else if(type.isArrayType()) {
                //TODO implement array call
            }
            caller.addArgument(paramName);
        }
    }

    private static BlockStmt generateTempObjects(IDLMethod idlMethod, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethodDeclaration, MethodCallExpr caller) {
        Type methodReturnType = methodDeclaration.getType();
        String returnTypeName = methodReturnType.toString();
        String newBody = null;

        boolean isReturnValue = idlMethod.isReturnValue;
        boolean isStatic = methodDeclaration.isStatic();

        String fieldName = generateFieldName(classDeclaration, returnTypeName, isReturnValue, isStatic);

        setupCallerParam(idlMethod, caller, methodDeclaration, fieldName);

        String methodCaller = caller.toString();

        if(isReturnValue) {
            newBody = GET_TEMP_OBJECT_TEMPLATE
                    .replace(TEMPLATE_TAG_METHOD, methodCaller)
                    .replace(TEMPLATE_TEMP_FIELD, fieldName);
        }
        else {
            newBody = GET_OBJECT_TEMPLATE
                    .replace(TEMPLATE_TAG_METHOD, methodCaller)
                    .replace(TEMPLATE_TEMP_FIELD, fieldName);
        }

        BlockStmt body = null;
        try {
            BodyDeclaration<?> bodyDeclaration = StaticJavaParser.parseBodyDeclaration(newBody);
            InitializerDeclaration initializerDeclaration = (InitializerDeclaration)bodyDeclaration;
            body = initializerDeclaration.getBody();
        }
        catch(Throwable t) {
            String className = classDeclaration.getNameAsString();
            System.err.println("Error Class: " + className + "\n" + newBody);
            throw t;
        }
        return body;
    }

    public static String generateFieldName(ClassOrInterfaceDeclaration classDeclaration, String returnTypeName, boolean isTemp, boolean isStatic) {
        // Will return a temp object.
        // Java variable will be created by checking its class, name and number.
        // if the temp object already exist it will increment variable number and create it.
        // isTemp true will create an object to use a c++ pointer. cMemOwn = false
        // isTemp false will actually create a c++ object by using the default constructor. cMemOwn = true
        // When isTemp is false, the c++ class must have the assignment operator

        for(int i = 0; i < 20; i++) {
            String fieldName = getFieldName(returnTypeName, i, isTemp, isStatic);
            if(classDeclaration.getFieldByName(fieldName).isEmpty()) {
                ObjectCreationExpr expression = new ObjectCreationExpr();
                expression.setType(returnTypeName);
                if(isTemp) {
                    expression.addArgument(StaticJavaParser.parseExpression("(byte)1"));
                }
                FieldDeclaration fieldDeclaration;
                if(isStatic) {
                    fieldDeclaration = classDeclaration.addFieldWithInitializer(returnTypeName, fieldName, expression, Modifier.Keyword.STATIC, Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL);
                }
                else {
                    fieldDeclaration = classDeclaration.addFieldWithInitializer(returnTypeName, fieldName, expression, Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL);
                }
                Position begin = new Position(0, 0);
                Position end = new Position(0, 0);
                Range range = new Range(begin, end);
                fieldDeclaration.setRange(range);
                return fieldName;
            }
        }
        return null;
    }

    private static String getFieldName(String type, int number,  boolean isTemp, boolean isStatic) {

        if(isTemp) {
            if(isStatic) {
                return TEMPLATE_TEMP_STATIC_FIELD.replace(TEMPLATE_TAG_TYPE, type).replace(TEMPLATE_TAG_NUM, String.valueOf(number));
            }
            else {
                return TEMPLATE_TEMP_FIELD.replace(TEMPLATE_TAG_TYPE, type).replace(TEMPLATE_TAG_NUM, String.valueOf(number));
            }
        }
        else {
            if(isStatic) {
                return TEMPLATE_NEW_STATIC_FIELD.replace(TEMPLATE_TAG_TYPE, type).replace(TEMPLATE_TAG_NUM, String.valueOf(number));
            }
            else {
                return TEMPLATE_NEW_FIELD.replace(TEMPLATE_TAG_TYPE, type).replace(TEMPLATE_TAG_NUM, String.valueOf(number));
            }
        }
    }

    private static MethodDeclaration generateNativeMethod(IDLMethod idlMethod, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration) {
        String methodName = methodDeclaration.getNameAsString();
        NodeList<Parameter> methodParameters = methodDeclaration.getParameters();
        Type methodReturnType = methodDeclaration.getType();
        boolean isStatic = methodDeclaration.isStatic();
        boolean isReturnValue = idlMethod.isReturnValue;
        boolean isClassOrInterfaceType = methodReturnType.isClassOrInterfaceType();

        // Clone some generated idl method settings
        MethodDeclaration nativeMethod = new MethodDeclaration();
        nativeMethod.setName(methodName + "NATIVE");
        nativeMethod.setModifiers(Modifier.createModifierList(Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC, Modifier.Keyword.NATIVE));
        nativeMethod.removeBody();

        if(isReturnValue) {
            // We pass a temp c++ object to copy the returned temp c++ object
            String pointerTempObject = "copy_addr";
            nativeMethod.addParameter("long", pointerTempObject);
        }

        if(!isStatic) {
            // Only generate addr if it's not a static method
            nativeMethod.addParameter("long", "this_addr");
        }

        for(int i = 0; i < methodParameters.size(); i++) {
            Parameter parameter = methodParameters.get(i);
            String nameAsString = parameter.getNameAsString();
            Type type = parameter.getType();
            if(type.isPrimitiveType()) {
                nativeMethod.addParameter(type.clone(), nameAsString);
            }
            else {
                String pointerMethod = nameAsString + "_addr";
                nativeMethod.addParameter("long", pointerMethod);
            }
        }
        if(isClassOrInterfaceType) {
            // If the return type is an object we need to return a pointer.
            // If it's a value set to void
            Type type;
            if(isReturnValue) {
                type = StaticJavaParser.parseType("void");
            }
            else {
                // If c++ is not a value and is a class object, return pointer
                type = StaticJavaParser.parseType(long.class.getSimpleName());
            }
            nativeMethod.setType(type);
        }
        else {
            nativeMethod.setType(methodReturnType);
        }
        return nativeMethod;
    }

    private static ReturnStmt getReturnStmt(MethodDeclaration idlMethodDeclaration) {
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

    private static MethodDeclaration containsMethod(ClassOrInterfaceDeclaration classOrInterfaceDeclaration, IDLMethod idlMethod) {
        ArrayList<IDLParameter> parameters = idlMethod.parameters;
        String[] paramTypes = new String[parameters.size()];

        for(int i = 0; i < parameters.size(); i++) {
            IDLParameter parameter = parameters.get(i);
            String paramType = parameter.type;
            paramTypes[i] = paramType;
        }
        List<MethodDeclaration> methods = classOrInterfaceDeclaration.getMethodsBySignature(idlMethod.name, paramTypes);

        if(methods.size() > 0) {
            return methods.get(0);
        }
        return null;
    }

}