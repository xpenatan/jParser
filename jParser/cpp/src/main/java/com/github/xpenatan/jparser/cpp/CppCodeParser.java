package com.github.xpenatan.jparser.cpp;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserHelper;
import com.github.xpenatan.jparser.core.JParserItem;
import com.github.xpenatan.jparser.core.codeparser.idl.IDLDefaultCodeParser;
import com.github.xpenatan.jparser.idl.IDLClass;
import com.github.xpenatan.jparser.idl.IDLMethod;
import com.github.xpenatan.jparser.idl.IDLParameter;
import com.github.xpenatan.jparser.idl.IDLReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CppCodeParser extends IDLDefaultCodeParser {

    private static final String HEADER_CMD = "C++";

    private static final String CPOINTER = "cPointer";

    protected static final String TEMPLATE_TAG_TYPE = "[TYPE]";

    protected static final String TEMPLATE_TAG_METHOD = "[METHOD]";

    protected static final String TEMPLATE_TAG_COPY_TYPE = "[COPY_TYPE]";

    protected static final String TEMPLATE_TAG_COPY_PARAM = "[COPY_PARAM]";

    protected static final String STATIC_GET_METHOD_VOID_TEMPLATE = "" +
            "\n[TYPE]::[METHOD];\n";

    protected static final String STATIC_GET_METHOD_OBJ_POINTER_TEMPLATE = "" +
            "\nreturn (jlong)[TYPE]::[METHOD];\n";

    protected static final String STATIC_GET_METHOD_OBJ_POINTER_REF_TEMPLATE = "" +
            "\nreturn (jlong)&[TYPE]::[METHOD];\n";

    protected static final String STATIC_GET_METHOD_PRIMITIVE_TEMPLATE = "" +
            "\nreturn [TYPE]::[METHOD];\n";

    protected static final String GET_METHOD_VOID_TEMPLATE = "" +
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "nativeObject->[METHOD];\n";

    protected static final String GET_METHOD_OBJ_POINTER_TEMPLATE = "" +
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return (jlong)nativeObject->[METHOD];\n";

    protected static final String GET_METHOD_OBJ_POINTER_REF_TEMPLATE = "" +
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return (jlong)&nativeObject->[METHOD];\n";

    protected static final String COPY_METHOD_VALUE_TEMPLATE = "" +
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "*(([COPY_TYPE]*)[COPY_PARAM]) = nativeObject->[METHOD];\n";

    protected static final String COPY_STATIC_METHOD_VALUE_TEMPLATE = "" +
            "\n*(([COPY_TYPE]*)[COPY_PARAM]) = [TYPE]::[METHOD];\n";

    protected static final String GET_METHOD_PRIMITIVE_TEMPLATE = "" +
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return nativeObject->[METHOD];\n";

    protected static final String GET_OBJECT_TEMPLATE = "" +
            "{\n" +
            "    long pointer = [METHOD];\n" +
            "    [TYPE].WRAPPER_GEN_01.setPointer(pointer);\n" +
            "    return [TYPE].WRAPPER_GEN_01;\n" +
            "}";

    protected static final String CONVERT_TO_GDX_TEMPLATE = "" +
            "{\n" +
            "    long pointer = [METHOD];\n" +
            "    [TYPE].WRAPPER_GEN_01.setPointer(pointer);\n" +
            "    [TYPE].convert([TYPE].WRAPPER_GEN_01, [TYPE].TEMP_GDX_01);\n" +
            "    return [TYPE].TEMP_GDX_01;\n" +
            "}";

    protected static final String OBJECT_CREATION_TEMPLATE = "" +
            "public static [TYPE] WRAPPER_GEN_01 = new [TYPE](false);";

    public static String getClassPath(String ... includes) {
        String classpath = System.getProperty("java.class.path") + File.pathSeparator;
        String newClassPath = "";
        String pathSeparator = File.pathSeparator;
        String[] split = classpath.split(pathSeparator);
        for(int i = 0; i < split.length; i++) {
            String path = split[i];
            for(int j = 0; j < includes.length; j++) {
                String include = includes[j];
                if(path.contains(include)) {
                    newClassPath += path + pathSeparator;
                }
            }
        }
        return newClassPath;
    }

    private CppGenerator cppGenerator;

    @Deprecated
    public CppCodeParser(String classpath, String jniDir) {
        this(null, classpath, jniDir);
    }

    @Deprecated
    public CppCodeParser(IDLReader idlReader, String classpath, String jniDir) {
        super(HEADER_CMD, idlReader);
        cppGenerator = new NativeCPPGenerator(classpath, jniDir);
        enableAttributeParsing = false;
    }

    public CppCodeParser(CppGenerator cppGenerator) {
        this(cppGenerator, null);
    }

    public CppCodeParser(CppGenerator cppGenerator, IDLReader idlReader) {
        this(cppGenerator, idlReader, "");
    }

    public CppCodeParser(CppGenerator cppGenerator, IDLReader idlReader, String basePackage) {
        super(basePackage, HEADER_CMD, idlReader);
        this.cppGenerator = cppGenerator;
        enableAttributeParsing = false;
    }

    @Override
    public void onIDLMethodGenerated(JParser jParser, IDLMethod idlMethod, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethodDeclaration) {
        generateNativeAnnotation(idlMethod, classDeclaration, methodDeclaration, nativeMethodDeclaration);
    }

    @Override
    public void onIDLMethodGenerated(JParser jParser, IDLClass idlClass, IDLMethod idlMethod, CompilationUnit unit, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration idlMethodDeclaration, boolean isAttribute) {
        // IDL parser generate our empty methods with default return values.
        // We now modify it to match C++ api calls

        String idlMethodName = idlMethodDeclaration.getNameAsString();
        NodeList<Parameter> idlMethodParameters = idlMethodDeclaration.getParameters();
        Type idlMethodReturnType = idlMethodDeclaration.getType();
        MethodDeclaration nativeMethod = null;

        {
            // Clone some generated idl method settings
            nativeMethod = new MethodDeclaration();
            nativeMethod.setName(idlMethodName + "NATIVE");
            nativeMethod.setModifiers(Modifier.createModifierList(Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC, Modifier.Keyword.NATIVE));
            nativeMethod.removeBody();
            nativeMethod.addAndGetParameter("long", "this_addr");

            for(int i = 0; i < idlMethodParameters.size(); i++) {
                Parameter parameter = idlMethodParameters.get(i);
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
            // If the return type is an object we need to return a pointer.
            if(idlMethodReturnType.isClassOrInterfaceType()) {
                // Class Object needs to return a pointer
                Type type = StaticJavaParser.parseType(long.class.getSimpleName());
                nativeMethod.setType(type);
            }
            else {
                nativeMethod.setType(idlMethodReturnType);
            }
            generateNativeAnnotation(idlMethod, classDeclaration, idlMethodDeclaration, nativeMethod);
        }
        // Check if the generated method does not exist in the original class
        if(!JParserHelper.containsMethod(classDeclaration, nativeMethod)) {
            MethodCallExpr caller = null;
            {
                // Generate the method caller
                caller = new MethodCallExpr();
                caller.setName(nativeMethod.getNameAsString());
                caller.addArgument(CPOINTER);
                for(int i = 0; i < idlMethodParameters.size(); i++) {
                    Parameter parameter = idlMethodParameters.get(i);
                    Type type = parameter.getType();
                    String paramName = parameter.getNameAsString();
                    if(type.isClassOrInterfaceType()) {
                        String typeName = parameter.getType().toString();
                        paramName = paramName + ".getCPointer()";
                    }
                    caller.addArgument(paramName);
                }
            }

            if(idlMethodReturnType.isVoidType()) {
                // void types just call the method.
                BlockStmt blockStmt = idlMethodDeclaration.getBody().get();
                blockStmt.addStatement(caller);
            }
            else if(idlMethodReturnType.isClassOrInterfaceType()) {
                // Class object needs to generate some code.
                BlockStmt blockStmt = generateObjectPointerReturnType(unit, classDeclaration, idlMethodDeclaration, caller);
                idlMethodDeclaration.setBody(blockStmt);
            }
            else {
                // Should be a primitive return type.
                ReturnStmt returnStmt = getReturnStmt(idlMethodDeclaration);
                returnStmt.setExpression(caller);
            }
            classDeclaration.getMembers().add(nativeMethod);
            generateGdxMethod(unit, classDeclaration, idlMethodDeclaration, nativeMethod, caller);
        }
    }

    protected void generateGdxMethod(CompilationUnit unit, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration idlMethodDeclaration, MethodDeclaration nativeMethod, MethodCallExpr caller) {
    }

    private ReturnStmt getReturnStmt(MethodDeclaration idlMethodDeclaration) {
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

    private void generateNativeAnnotation(IDLMethod idlMethod, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration idlMethodDeclaration, MethodDeclaration nativeMethod) {
        NodeList<Parameter> nativeParameters = nativeMethod.getParameters();
        Type returnType = idlMethodDeclaration.getType();
        String methodName = idlMethodDeclaration.getNameAsString();
        boolean isStatic = idlMethodDeclaration.isStatic();
        boolean isReturnValue = idlMethod.isReturnValue;

        String param = getParams(idlMethod, idlMethodDeclaration);
        String classTypeName = classDeclaration.getNameAsString();
        String methodCaller = methodName + "(" + param + ")";

        String content = null;

        if(returnType.isVoidType()) {
//            if(isAttribute) {
//                Expression expression = caller.getArguments().get(0);
//                methodCaller = methodName + " = " + expression.toString();
//            }

            if(isStatic) {
                content = STATIC_GET_METHOD_VOID_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
            }
            else {
                content = GET_METHOD_VOID_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
            }
        }
        else if(returnType.isClassOrInterfaceType()) {
            if(idlMethod.isReturnRef) {
                if(isStatic) {
                    content = STATIC_GET_METHOD_OBJ_POINTER_REF_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
                }
                else {
                    content = GET_METHOD_OBJ_POINTER_REF_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
                }
            }
            else if(idlMethod.isReturnValue) {
                // For temporary c++ object, the class needs to contains assignment operator
                if(isStatic) {
                    String returnTypeName = returnType.asClassOrInterfaceType().asClassOrInterfaceType().getNameAsString();
                    String copyParam = "copy_addr";
                    content = COPY_STATIC_METHOD_VALUE_TEMPLATE
                            .replace(TEMPLATE_TAG_METHOD, methodCaller)
                            .replace(TEMPLATE_TAG_TYPE, classTypeName)
                            .replace(TEMPLATE_TAG_COPY_TYPE, returnTypeName)
                            .replace(TEMPLATE_TAG_COPY_PARAM, copyParam);
                }
                else {
                    String returnTypeName = returnType.asClassOrInterfaceType().asClassOrInterfaceType().getNameAsString();
                    String copyParam = "copy_addr";
                    content = COPY_METHOD_VALUE_TEMPLATE
                            .replace(TEMPLATE_TAG_METHOD, methodCaller)
                            .replace(TEMPLATE_TAG_TYPE, classTypeName)
                            .replace(TEMPLATE_TAG_COPY_TYPE, returnTypeName)
                            .replace(TEMPLATE_TAG_COPY_PARAM, copyParam);
                }
            }
            else {
                if(isStatic) {
                    content = STATIC_GET_METHOD_OBJ_POINTER_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
                }
                else {
                    content = GET_METHOD_OBJ_POINTER_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
                }
            }
        }
        else {
            if(isStatic) {
                content = STATIC_GET_METHOD_PRIMITIVE_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
            }
            else {
                content = GET_METHOD_PRIMITIVE_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
            }
        }

        if(content != null) {
            String header = "[-" + HEADER_CMD + ";" + CMD_NATIVE + "]";
            String blockComment = header + content;
            nativeMethod.setBlockComment(blockComment);
        }
    }

    private static String getParams(IDLMethod idlMethod, MethodDeclaration methodDeclaration) {
        String param = "";

        NodeList<Parameter> parameters = methodDeclaration.getParameters();
        ArrayList<IDLParameter> idParameters = idlMethod.parameters;

        for(int i = 0; i < parameters.size(); i++) {
            Parameter parameter = parameters.get(i);
            IDLParameter idlParameter = idParameters.get(i);
            String paramName = idlParameter.name;
            Type type = parameter.getType();
            boolean isObject = type.isClassOrInterfaceType();
            if(isObject) {
                paramName += "_addr";
                String classType = idlParameter.type;
                IDLClass paramClass = idlParameter.idlFile.getClass(classType);
                if(paramClass != null) {
                    classType = paramClass.getName();
                }
                if(idlParameter.isRef || idlParameter.isValue) {
                    paramName = "*((" + classType + "* )" + paramName + ")";
                }
                else {
                    paramName = "(" + classType + "* )" + paramName;
                }
            }

            if(i > 0) {
                param += ", ";
            }
            param += paramName;
        }
        return param;
    }

    protected BlockStmt generateObjectPointerReturnType(CompilationUnit unit, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration idlMethodDeclaration, MethodCallExpr caller) {
        //  if return type is an object we need to get the method pointer, add it do a temp object and return this object
        Type type = idlMethodDeclaration.getType();

        String returnTypeName = type.toString();
        String methodCaller = caller.toString();
        String newBody = GET_OBJECT_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, returnTypeName);

        {
            // Convert native return object to Gdx object
            if(returnTypeName.equals("btVector3") || returnTypeName.equals("Vector3")) {
                newBody = CONVERT_TO_GDX_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, "btVector3");
                idlMethodDeclaration.setType("Vector3");
                JParserHelper.addMissingImportType(unit, "com.badlogic.gdx.math.Vector3");
            }
            else if(returnTypeName.equals("btTransform") || returnTypeName.equals("Matrix4")) {
                newBody = CONVERT_TO_GDX_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, "btTransform");
                idlMethodDeclaration.setType("Matrix4");
                JParserHelper.addMissingImportType(unit, "com.badlogic.gdx.math.Matrix4");
            }
            else if(returnTypeName.equals("btQuaternion") || returnTypeName.equals("Quaternion")) {
                newBody = CONVERT_TO_GDX_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, "btQuaternion");
                idlMethodDeclaration.setType("Quaternion");
                JParserHelper.addMissingImportType(unit, "com.badlogic.gdx.math.Quaternion");
            }
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

    @Override
    public boolean parseCodeBlock(Node node, String headerCommands, String content) {
        if(!super.parseCodeBlock(node, headerCommands, content)) {
            if(headerCommands.contains(CMD_NATIVE)) {
                cppGenerator.addNativeCode(node, content);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void setJavaBodyNativeCMD(String content, MethodDeclaration methodDeclaration) {
        cppGenerator.addNativeCode(methodDeclaration, content);
    }

    @Override
    public void onParseClassStart(JParser jParser, CompilationUnit unit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        if(idlReader != null) {
            String nameAsString = classOrInterfaceDeclaration.getNameAsString();
            IDLClass idlClass = idlReader.getClass(nameAsString);
            if(idlClass != null && !generateClass) {
                // Create a static temp object for every module class so any generated method can use to store a pointer.
                // Also generate a boolean constructor if it's not in the original source code.
                List<ConstructorDeclaration> constructors = classOrInterfaceDeclaration.getConstructors();

                if(!classOrInterfaceDeclaration.isAbstract()) {
                    String replace = OBJECT_CREATION_TEMPLATE.replace(TEMPLATE_TAG_TYPE, nameAsString);
                    FieldDeclaration bodyDeclaration = (FieldDeclaration)StaticJavaParser.parseBodyDeclaration(replace);

                    boolean containsField = false;
                    for(BodyDeclaration<?> member : classOrInterfaceDeclaration.getMembers()) {
                        if(member.isFieldDeclaration()) {
                            FieldDeclaration fieldDeclaration = member.asFieldDeclaration();
                            if(fieldDeclaration.equals(bodyDeclaration)) {
                                containsField = true;
                                break;
                            }
                        }
                    }

                    if(!containsField) {
                        classOrInterfaceDeclaration.getMembers().add(0, bodyDeclaration);
                    }
                }

                boolean containsConstructor = false;
                boolean containsZeroParamConstructor = false;
                for(int i = 0; i < constructors.size(); i++) {
                    ConstructorDeclaration constructorDeclaration = constructors.get(i);
                    NodeList<Parameter> parameters = constructorDeclaration.getParameters();
                    if(parameters.size() == 1 && JParserHelper.isBoolean(parameters.get(0).getType())) {
                        containsConstructor = true;
                    }
                    else if(parameters.size() == 0) {
                        containsZeroParamConstructor = true;
                    }
                }

                if(!containsConstructor) {
                    ConstructorDeclaration constructorDeclaration = classOrInterfaceDeclaration.addConstructor(Modifier.Keyword.PROTECTED);
                    constructorDeclaration.addParameter("boolean", "cMemoryOwn");
                }
                if(!containsZeroParamConstructor) {
                    classOrInterfaceDeclaration.addConstructor(Modifier.Keyword.PROTECTED);
                }
            }
        }

        super.onParseClassStart(jParser, unit, classOrInterfaceDeclaration);
    }

    @Override
    public void onParseFileEnd(JParser jParser, JParserItem parserItem) {
        cppGenerator.addParseFile(jParser, parserItem);
    }

    @Override
    public void onParseEnd(JParser jParser) {
        cppGenerator.generate(jParser);
    }
}