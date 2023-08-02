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
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.Type;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserHelper;
import com.github.xpenatan.jparser.core.JParserItem;
import com.github.xpenatan.jparser.idl.IDLAttribute;
import com.github.xpenatan.jparser.idl.IDLFile;
import com.github.xpenatan.jparser.idl.parser.IDLDefaultCodeParser;
import com.github.xpenatan.jparser.idl.IDLClass;
import com.github.xpenatan.jparser.idl.IDLMethod;
import com.github.xpenatan.jparser.idl.IDLParameter;
import com.github.xpenatan.jparser.idl.IDLReader;
import java.util.ArrayList;
import java.util.List;

public class CppCodeParserV2 extends IDLDefaultCodeParser {

    private static final String HEADER_CMD = "C++";

    protected static final String TEMPLATE_TAG_TYPE = "[TYPE]";

    protected static final String TEMPLATE_TAG_METHOD = "[METHOD]";

    protected static final String TEMPLATE_TAG_ATTRIBUTE = "[ATTRIBUTE]";

    protected static final String TEMPLATE_TAG_COPY_TYPE = "[COPY_TYPE]";

    protected static final String TEMPLATE_TAG_COPY_PARAM = "[COPY_PARAM]";

    protected static final String STATIC_SET_ATTRIBUTE_VOID_TEMPLATE = "" +
            "\n[TYPE]::[ATTRIBUTE] = [ATTRIBUTE];\n";

    protected static final String SET_ATTRIBUTE_VOID_TEMPLATE = "" +
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "nativeObject->[ATTRIBUTE] = [ATTRIBUTE];\n";

    protected static final String STATIC_GET_ATTRIBUTE_OBJ_POINTER_TEMPLATE = "" +
            "\nreturn (jlong)[TYPE]::[ATTRIBUTE];\n";

    protected static final String GET_ATTRIBUTE_OBJ_POINTER_TEMPLATE = "" +
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return (jlong)nativeObject->[ATTRIBUTE];\n";

    protected static final String STATIC_GET_ATTRIBUTE_PRIMITIVE_TEMPLATE = "" +
            "\nreturn [TYPE]::[ATTRIBUTE];\n";

    protected static final String GET_ATTRIBUTE_PRIMITIVE_TEMPLATE = "" +
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return nativeObject->[ATTRIBUTE];\n";

    protected static final String COPY_METHOD_VALUE_TEMPLATE = "" +
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "*(([COPY_TYPE]*)[COPY_PARAM]) = nativeObject->[METHOD];\n";

    protected static final String COPY_STATIC_METHOD_VALUE_TEMPLATE = "" +
            "\n*(([COPY_TYPE]*)[COPY_PARAM]) = [TYPE]::[METHOD];\n";

    protected static final String STATIC_CALL_METHOD_VOID_TEMPLATE = "" +
            "\n[TYPE]::[METHOD];\n";

    protected static final String CALL_METHOD_VOID_TEMPLATE = "" +
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "nativeObject->[METHOD];\n";

    protected static final String STATIC_GET_METHOD_OBJ_POINTER_TEMPLATE = "" +
            "\nreturn (jlong)[TYPE]::[METHOD];\n";

    protected static final String GET_METHOD_OBJ_POINTER_TEMPLATE = "" +
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return (jlong)nativeObject->[METHOD];\n";

    protected static final String STATIC_GET_METHOD_OBJ_POINTER_REF_TEMPLATE = "" +
            "\nreturn (jlong)&[TYPE]::[METHOD];\n";

    protected static final String GET_METHOD_OBJ_POINTER_REF_TEMPLATE = "" +
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return (jlong)&nativeObject->[METHOD];\n";

    protected static final String STATIC_GET_METHOD_PRIMITIVE_TEMPLATE = "" +
            "\nreturn [TYPE]::[METHOD];\n";

    protected static final String GET_METHOD_PRIMITIVE_TEMPLATE = "" +
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return nativeObject->[METHOD];\n";

    protected static final String OBJECT_CREATION_TEMPLATE = "" +
            "public static [TYPE] WRAPPER_GEN_01 = new [TYPE](false);";

    private CppGenerator cppGenerator;

    @Deprecated
    public CppCodeParserV2(String classpath, String jniDir) {
        this(null, classpath, jniDir);
    }

    @Deprecated
    public CppCodeParserV2(IDLReader idlReader, String classpath, String jniDir) {
        super(HEADER_CMD, idlReader);
        cppGenerator = new NativeCPPGenerator(classpath, jniDir);
    }

    public CppCodeParserV2(CppGenerator cppGenerator) {
        this(cppGenerator, null);
    }

    public CppCodeParserV2(CppGenerator cppGenerator, IDLReader idlReader) {
        this(cppGenerator, idlReader, "");
    }

    public CppCodeParserV2(CppGenerator cppGenerator, IDLReader idlReader, String basePackage) {
        super(basePackage, HEADER_CMD, idlReader);
        this.cppGenerator = cppGenerator;
    }

    @Override
    public void onIDLMethodGenerated(JParser jParser, IDLMethod idlMethod, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethodDeclaration) {
        String param = getParams(idlMethod, methodDeclaration);
        generateNativeAnnotation(idlMethod.isReturnRef, idlMethod.isReturnValue, param, classDeclaration, methodDeclaration, nativeMethodDeclaration);
    }

    @Override
    public void onIDLAttributeGenerated(JParser jParser, IDLAttribute idlAttribute, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethodDeclaration) {
        boolean isStatic = methodDeclaration.isStatic();
        String attributeName = idlAttribute.name;
        String classTypeName = classDeclaration.getNameAsString();
        Type returnType = methodDeclaration.getType();
        String content;
        if(returnType.isVoidType()) {
            if(isStatic) {
                content = STATIC_SET_ATTRIBUTE_VOID_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName);
            }
            else {
                content = SET_ATTRIBUTE_VOID_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName);
            }
        }
        else if(returnType.isClassOrInterfaceType()) {
            if(isStatic) {
                content = STATIC_GET_ATTRIBUTE_OBJ_POINTER_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName);
            }
            else {
                content = GET_ATTRIBUTE_OBJ_POINTER_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName);
            }
        }
        else {
            if(isStatic) {
                content = STATIC_GET_ATTRIBUTE_PRIMITIVE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName);
            }
            else {
                content = GET_ATTRIBUTE_PRIMITIVE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName);
            }
        }

        String header = "[-" + HEADER_CMD + ";" + CMD_NATIVE + "]";
        String blockComment = header + content;
        nativeMethodDeclaration.setBlockComment(blockComment);
    }

    private void generateNativeAnnotation(boolean isReturnRef, boolean isReturnValue, String param, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethod) {
        Type returnType = methodDeclaration.getType();
        String methodName = methodDeclaration.getNameAsString();
        boolean isStatic = methodDeclaration.isStatic();
        String classTypeName = classDeclaration.getNameAsString();
        String methodCaller = methodName + "(" + param + ")";

        String content = null;

        if(returnType.isVoidType()) {
            if(isStatic) {
                content = STATIC_CALL_METHOD_VOID_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
            }
            else {
                content = CALL_METHOD_VOID_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
            }
        }
        else if(returnType.isClassOrInterfaceType()) {
            if(isReturnRef) {
                if(isStatic) {
                    content = STATIC_GET_METHOD_OBJ_POINTER_REF_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
                }
                else {
                    content = GET_METHOD_OBJ_POINTER_REF_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
                }
            }
            else if(isReturnValue) {
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

        String header = "[-" + HEADER_CMD + ";" + CMD_NATIVE + "]";
        String blockComment = header + content;
        nativeMethod.setBlockComment(blockComment);
    }

    private static String getParams(IDLMethod idlMethod, MethodDeclaration methodDeclaration) {
        String param = "";
        NodeList<Parameter> parameters = methodDeclaration.getParameters();
        ArrayList<IDLParameter> idParameters = idlMethod.parameters;
        for(int i = 0; i < parameters.size(); i++) {
            Parameter parameter = parameters.get(i);
            IDLParameter idlParameter = idParameters.get(i);
            Type type = parameter.getType();
            boolean isObject = type.isClassOrInterfaceType();
            String paramName = getParam(idlParameter.idlFile, isObject, idlParameter.name, idlParameter.type, idlParameter.isRef, idlParameter.isValue);
            if(i > 0) {
                param += ", ";
            }
            param += paramName;
        }
        return param;
    }

    private static String getParam(IDLFile idlFile, boolean isObject, String paramName, String classType, boolean isRef, boolean isValue) {
        if(isObject) {
            paramName += "_addr";
            IDLClass paramClass = idlFile.getClass(classType);
            if(paramClass != null) {
                classType = paramClass.getName();
            }
            if(isRef || isValue) {
                paramName = "*((" + classType + "* )" + paramName + ")";
            }
            else {
                paramName = "(" + classType + "* )" + paramName;
            }
        }
        return paramName;
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
        super.onParseClassStart(jParser, unit, classOrInterfaceDeclaration);
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