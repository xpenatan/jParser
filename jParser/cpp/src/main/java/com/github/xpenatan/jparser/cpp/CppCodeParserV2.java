package com.github.xpenatan.jparser.cpp;

import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.Type;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserItem;
import com.github.xpenatan.jparser.core.util.RawCodeBlock;
import com.github.xpenatan.jparser.idl.IDLAttribute;
import com.github.xpenatan.jparser.idl.IDLConstructor;
import com.github.xpenatan.jparser.idl.IDLFile;
import com.github.xpenatan.jparser.idl.parser.IDLAttributeOperation;
import com.github.xpenatan.jparser.idl.parser.IDLDefaultCodeParser;
import com.github.xpenatan.jparser.idl.IDLClass;
import com.github.xpenatan.jparser.idl.IDLMethod;
import com.github.xpenatan.jparser.idl.IDLParameter;
import com.github.xpenatan.jparser.idl.IDLReader;
import java.util.ArrayList;

public class CppCodeParserV2 extends IDLDefaultCodeParser {

    private static final String HEADER_CMD = "C++";

    protected static final String TEMPLATE_TAG_TYPE = "[TYPE]";

    protected static final String TEMPLATE_TAG_METHOD = "[METHOD]";

    protected static final String TEMPLATE_TAG_ATTRIBUTE = "[ATTRIBUTE]";

    protected static final String TEMPLATE_TAG_ATTRIBUTE_TYPE = "[ATTRIBUTE_TYPE]";

    protected static final String TEMPLATE_TAG_COPY_TYPE = "[COPY_TYPE]";

    protected static final String TEMPLATE_TAG_COPY_PARAM = "[COPY_PARAM]";

    protected static final String TEMPLATE_TAG_CONSTRUCTOR = "[CONSTRUCTOR]";

    protected static final String GET_CONSTRUCTOR_OBJ_POINTER_TEMPLATE =
            "\nreturn (jlong)new [CONSTRUCTOR];\n";

    protected static final String ATTRIBUTE_SET_PRIMITIVE_STATIC_TEMPLATE =
            "\n[TYPE]::[ATTRIBUTE] = [ATTRIBUTE];\n";

    protected static final String ATTRIBUTE_SET_PRIMITIVE_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "nativeObject->[ATTRIBUTE] = [ATTRIBUTE];\n";

    protected static final String ATTRIBUTE_SET_OBJECT_POINTER_STATIC_TEMPLATE =
            "\n[TYPE]::[ATTRIBUTE] = ([ATTRIBUTE_TYPE]*)[ATTRIBUTE]_addr;\n";

    protected static final String ATTRIBUTE_SET_OBJECT_POINTER_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "nativeObject->[ATTRIBUTE] = ([ATTRIBUTE_TYPE]*)[ATTRIBUTE]_addr;\n";

    protected static final String ATTRIBUTE_GET_OBJECT_VALUE_STATIC_TEMPLATE =
            "*(([ATTRIBUTE_TYPE]*)copy_addr) = [TYPE]::[ATTRIBUTE];\n";

    protected static final String ATTRIBUTE_GET_OBJECT_VALUE_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "*(([ATTRIBUTE_TYPE]*)copy_addr) = nativeObject->[ATTRIBUTE];\n";

    protected static final String ATTRIBUTE_SET_OBJECT_VALUE_STATIC_TEMPLATE =
            "[TYPE]::[ATTRIBUTE] = *(([ATTRIBUTE_TYPE]*)[ATTRIBUTE]_addr);\n";

    protected static final String ATTRIBUTE_SET_OBJECT_VALUE_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "nativeObject->[ATTRIBUTE] = *(([ATTRIBUTE_TYPE]*)[ATTRIBUTE]_addr);\n";

    protected static final String ATTRIBUTE_GET_OBJECT_POINTER_STATIC_TEMPLATE =
            "\nreturn (jlong)[TYPE]::[ATTRIBUTE];\n";

    protected static final String ATTRIBUTE_GET_OBJECT_POINTER_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return (jlong)nativeObject->[ATTRIBUTE];\n";

    protected static final String ATTRIBUTE_GET_PRIMITIVE_STATIC_TEMPLATE =
            "\nreturn [TYPE]::[ATTRIBUTE];\n";

    protected static final String ATTRIBUTE_GET_PRIMITIVE_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return nativeObject->[ATTRIBUTE];\n";

    protected static final String COPY_METHOD_VALUE_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "*(([COPY_TYPE]*)[COPY_PARAM]) = nativeObject->[METHOD];\n";

    protected static final String COPY_STATIC_METHOD_VALUE_TEMPLATE =
            "\n*(([COPY_TYPE]*)[COPY_PARAM]) = [TYPE]::[METHOD];\n";

    protected static final String STATIC_CALL_METHOD_VOID_TEMPLATE =
            "\n[TYPE]::[METHOD];\n";

    protected static final String CALL_METHOD_VOID_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "nativeObject->[METHOD];\n";

    protected static final String STATIC_GET_METHOD_OBJ_POINTER_TEMPLATE =
            "\nreturn (jlong)[TYPE]::[METHOD];\n";

    protected static final String GET_METHOD_OBJ_POINTER_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return (jlong)nativeObject->[METHOD];\n";

    protected static final String STATIC_GET_METHOD_OBJ_POINTER_REF_TEMPLATE =
            "\nreturn (jlong)&[TYPE]::[METHOD];\n";

    protected static final String GET_METHOD_OBJ_POINTER_REF_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return (jlong)&nativeObject->[METHOD];\n";

    protected static final String STATIC_GET_METHOD_PRIMITIVE_TEMPLATE =
            "\nreturn [TYPE]::[METHOD];\n";

    protected static final String GET_METHOD_PRIMITIVE_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return nativeObject->[METHOD];\n";

    private final CppGenerator cppGenerator;

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
    public void onIDLConstructorGenerated(JParser jParser, IDLConstructor idlConstructor, ClassOrInterfaceDeclaration classDeclaration, ConstructorDeclaration constructorDeclaration, MethodDeclaration nativeMethodDeclaration) {
        String classTypeName = classDeclaration.getNameAsString();

        NodeList<Parameter> parameters = constructorDeclaration.getParameters();
        ArrayList<IDLParameter> idParameters = idlConstructor.parameters;
        String params = getParams(parameters, idParameters);

        String constructor = classTypeName + "(" + params + ")";
        String content = GET_CONSTRUCTOR_OBJ_POINTER_TEMPLATE.replace(TEMPLATE_TAG_CONSTRUCTOR, constructor);

        String header = "[-" + HEADER_CMD + ";" + CMD_NATIVE + "]";
        String blockComment = header + content;
        nativeMethodDeclaration.setBlockComment(blockComment);
    }

    @Override
    public void onIDLMethodGenerated(JParser jParser, IDLMethod idlMethod, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethodDeclaration) {
        String param = getParams(idlMethod, methodDeclaration);
        setupMethodGenerated(idlMethod.isReturnRef, idlMethod.isReturnValue, param, classDeclaration, methodDeclaration, nativeMethodDeclaration);
    }

    @Override
    public void onIDLAttributeGenerated(JParser jParser, IDLAttribute idlAttribute, boolean isSet, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethod) {
        String attributeName = idlAttribute.name;
        String classTypeName = classDeclaration.getNameAsString();
        String attributeType = idlAttribute.type;
        String content = null;
        IDLAttributeOperation.Op attributeOperation = IDLAttributeOperation.getEnum(isSet, idlAttribute, methodDeclaration, nativeMethod);
        switch(attributeOperation) {
            case SET_OBJECT_VALUE:
                content = ATTRIBUTE_SET_OBJECT_VALUE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName)
                        .replace(TEMPLATE_TAG_ATTRIBUTE_TYPE, attributeType)
                        .replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case SET_OBJECT_VALUE_STATIC:
                content = ATTRIBUTE_SET_OBJECT_VALUE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName)
                        .replace(TEMPLATE_TAG_ATTRIBUTE_TYPE, attributeType)
                        .replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case GET_OBJECT_VALUE:
                content = ATTRIBUTE_GET_OBJECT_VALUE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName)
                        .replace(TEMPLATE_TAG_ATTRIBUTE_TYPE, attributeType)
                        .replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case GET_OBJECT_VALUE_STATIC:
                content = ATTRIBUTE_GET_OBJECT_VALUE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName)
                        .replace(TEMPLATE_TAG_ATTRIBUTE_TYPE, attributeType)
                        .replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case SET_OBJECT_POINTER:
                content = ATTRIBUTE_SET_OBJECT_POINTER_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName)
                        .replace(TEMPLATE_TAG_ATTRIBUTE_TYPE, attributeType)
                        .replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case SET_OBJECT_POINTER_STATIC:
                content = ATTRIBUTE_SET_OBJECT_POINTER_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName)
                        .replace(TEMPLATE_TAG_ATTRIBUTE_TYPE, attributeType)
                        .replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case GET_OBJECT_POINTER:
                content = ATTRIBUTE_GET_OBJECT_POINTER_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case GET_OBJECT_POINTER_STATIC:
                content = ATTRIBUTE_GET_OBJECT_POINTER_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case SET_PRIMITIVE:
                content = ATTRIBUTE_SET_PRIMITIVE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case SET_PRIMITIVE_STATIC:
                content = ATTRIBUTE_SET_PRIMITIVE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case GET_PRIMITIVE:
                content = ATTRIBUTE_GET_PRIMITIVE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case GET_PRIMITIVE_STATIC:
                content = ATTRIBUTE_GET_PRIMITIVE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
        }

        if(content != null) {
            String header = "[-" + HEADER_CMD + ";" + CMD_NATIVE + "]";
            String blockComment = header + content;
            nativeMethod.setBlockComment(blockComment);
        }
    }

    private void setupMethodGenerated(boolean isReturnRef, boolean isReturnValue, String param, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethod) {
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
        NodeList<Parameter> parameters = methodDeclaration.getParameters();
        ArrayList<IDLParameter> idParameters = idlMethod.parameters;
        return getParams(parameters, idParameters);
    }

    private static String getParams(NodeList<Parameter> parameters, ArrayList<IDLParameter> idParameters ) {
        String param = "";
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
        String nameAsString = classOrInterfaceDeclaration.getNameAsString();

        String include = classCppPath.get(nameAsString);
        if(include != null) {
            String comment = "" +
                    "/*[-C++;-NATIVE]\n" +
                    "       #include <" + include + ">\n" +
                    "    */";
            Position begin = new Position(0, 0);
            Position end = new Position(0, 0);
            Range range = new Range(begin, end);
            RawCodeBlock blockComment = new RawCodeBlock();
            blockComment.setRange(range);
            blockComment.setContent(comment);
            classOrInterfaceDeclaration.getMembers().add(0, blockComment);
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