package com.github.xpenatan.jparser.cpp;

import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.Type;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserItem;
import com.github.xpenatan.jparser.core.util.RawCodeBlock;
import com.github.xpenatan.jparser.idl.IDLAttribute;
import com.github.xpenatan.jparser.idl.IDLConstructor;
import com.github.xpenatan.jparser.idl.IDLEnum;
import com.github.xpenatan.jparser.idl.IDLFile;
import com.github.xpenatan.jparser.idl.parser.IDLAttributeOperation;
import com.github.xpenatan.jparser.idl.parser.IDLDefaultCodeParser;
import com.github.xpenatan.jparser.idl.IDLClass;
import com.github.xpenatan.jparser.idl.IDLMethod;
import com.github.xpenatan.jparser.idl.IDLParameter;
import com.github.xpenatan.jparser.idl.IDLReader;
import com.github.xpenatan.jparser.idl.parser.IDLMethodOperation;
import java.util.ArrayList;

public class CppCodeParser extends IDLDefaultCodeParser {

    private static final String HEADER_CMD = "C++";

    protected static final String TEMPLATE_TAG_TYPE = "[TYPE]";
    protected static final String TEMPLATE_TAG_METHOD = "[METHOD]";
    protected static final String TEMPLATE_TAG_ATTRIBUTE = "[ATTRIBUTE]";
    protected static final String TEMPLATE_TAG_ENUM = "[ENUM]";
    protected static final String TEMPLATE_TAG_ATTRIBUTE_TYPE = "[ATTRIBUTE_TYPE]";
    protected static final String TEMPLATE_TAG_COPY_TYPE = "[COPY_TYPE]";
    protected static final String TEMPLATE_TAG_COPY_PARAM = "[COPY_PARAM]";
    protected static final String TEMPLATE_TAG_CONSTRUCTOR = "[CONSTRUCTOR]";
    protected static final String TEMPLATE_TAG_CAST = "[CAST]";

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

    protected static final String METHOD_COPY_VALUE_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "*(([COPY_TYPE]*)[COPY_PARAM]) = nativeObject->[METHOD];\n";

    protected static final String METHOD_COPY_VALUE_STATIC_TEMPLATE =
            "\n*(([COPY_TYPE]*)[COPY_PARAM]) = [TYPE]::[METHOD];\n";

    protected static final String METHOD_CALL_VOID_STATIC_TEMPLATE =
            "\n[TYPE]::[METHOD];\n";

    protected static final String METHOD_CALL_VOID_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "nativeObject->[METHOD];\n";

    protected static final String METHOD_GET_OBJ_POINTER_STATIC_TEMPLATE =
            "\nreturn (jlong)[TYPE]::[METHOD];\n";

    protected static final String METHOD_GET_OBJ_POINTER_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return (jlong)nativeObject->[METHOD];\n";

    protected static final String METHOD_GET_REF_OBJ_POINTER_STATIC_TEMPLATE =
            "\nreturn (jlong)&[TYPE]::[METHOD];\n";

    protected static final String METHOD_GET_REF_OBJ_POINTER_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return (jlong)&nativeObject->[METHOD];\n";

    protected static final String METHOD_GET_PRIMITIVE_STATIC_TEMPLATE =
            "\nreturn [TYPE]::[METHOD];\n";

    protected static final String METHOD_GET_PRIMITIVE_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return [CAST]nativeObject->[METHOD];\n";

    protected static final String ENUM_GET_INT_TEMPLATE =
            "\nreturn (jlong)[ENUM];\n";

    private final CppGenerator cppGenerator;

    public CppCodeParser(CppGenerator cppGenerator) {
        this(cppGenerator, null);
    }

    public CppCodeParser(CppGenerator cppGenerator, IDLReader idlReader) {
        this(cppGenerator, idlReader, "");
    }

    public CppCodeParser(CppGenerator cppGenerator, IDLReader idlReader, String basePackage) {
        super(basePackage, HEADER_CMD, idlReader);
        this.cppGenerator = cppGenerator;
    }

    @Override
    public void onIDLConstructorGenerated(JParser jParser, IDLConstructor idlConstructor, ClassOrInterfaceDeclaration classDeclaration, ConstructorDeclaration constructorDeclaration, MethodDeclaration nativeMethodDeclaration) {
        String classTypeName = classDeclaration.getNameAsString();

        IDLClass idlClass = idlConstructor.idlFile.getClass(classTypeName);
        if(idlClass != null) {
            classTypeName = idlClass.classHeader.prefixName + classTypeName;
        }

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
        setupMethodGenerated(idlMethod, param, classDeclaration, methodDeclaration, nativeMethodDeclaration);
    }

    @Override
    public void onIDLAttributeGenerated(JParser jParser, IDLAttribute idlAttribute, boolean isSet, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethod) {
        String attributeName = idlAttribute.name;
        String classTypeName = classDeclaration.getNameAsString();
        IDLClass idlClass = idlAttribute.idlFile.getClass(classTypeName);
        if(idlClass != null) {
            classTypeName = idlClass.classHeader.prefixName + classTypeName;
        }

        String attributeType = idlAttribute.type;
        String content = null;
        IDLAttributeOperation.Op op = IDLAttributeOperation.getEnum(isSet, idlAttribute, methodDeclaration, nativeMethod);
        switch(op) {
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

    @Override
    public void onIDLEnumMethodGenerated(JParser jParser, ClassOrInterfaceDeclaration classDeclaration, String enumStr, FieldDeclaration fieldDeclaration, MethodDeclaration nativeMethodDeclaration) {
        String content  = "";
        content = ENUM_GET_INT_TEMPLATE.replace(TEMPLATE_TAG_ENUM, enumStr);
        String header = "[-" + HEADER_CMD + ";" + CMD_NATIVE + "]";
        String blockComment = header + content;
        nativeMethodDeclaration.setBlockComment(blockComment);
    }

    private void setupMethodGenerated(IDLMethod idlMethod, String param, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethod) {
        Type returnType = methodDeclaration.getType();
        String returnTypeStr = idlMethod.returnType;
        String methodName = methodDeclaration.getNameAsString();
        String classTypeName = classDeclaration.getNameAsString();
        IDLClass idlClass = idlMethod.idlFile.getClass(classTypeName);
        if(idlClass != null) {
            classTypeName = idlClass.classHeader.prefixName + classTypeName;
        }
        String returnCastStr = "";
        String methodCaller = methodName + "(" + param + ")";
        if(idlMethod.idlFile.getEnum(returnTypeStr) != null) {
            returnCastStr = "(int)";
        }
        String content = null;
        IDLMethodOperation.Op op = IDLMethodOperation.getEnum(idlMethod, methodDeclaration, nativeMethod);
        switch(op) {
            case CALL_VOID_STATIC:
                content = METHOD_CALL_VOID_STATIC_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case CALL_VOID:
                content = METHOD_CALL_VOID_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case GET_REF_OBJ_POINTER_STATIC:
                content = METHOD_GET_REF_OBJ_POINTER_STATIC_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case GET_REF_OBJ_POINTER:
                content = METHOD_GET_REF_OBJ_POINTER_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case COPY_VALUE_STATIC: {
                    String returnTypeName = returnType.asClassOrInterfaceType().asClassOrInterfaceType().getNameAsString();
                    String copyParam = "copy_addr";
                    content = METHOD_COPY_VALUE_STATIC_TEMPLATE
                            .replace(TEMPLATE_TAG_METHOD, methodCaller)
                            .replace(TEMPLATE_TAG_TYPE, classTypeName)
                            .replace(TEMPLATE_TAG_COPY_TYPE, returnTypeName)
                            .replace(TEMPLATE_TAG_COPY_PARAM, copyParam);
                }
                break;
            case COPY_VALUE: {
                    String returnTypeName = returnType.asClassOrInterfaceType().asClassOrInterfaceType().getNameAsString();
                    String copyParam = "copy_addr";
                    content = METHOD_COPY_VALUE_TEMPLATE
                            .replace(TEMPLATE_TAG_METHOD, methodCaller)
                            .replace(TEMPLATE_TAG_TYPE, classTypeName)
                            .replace(TEMPLATE_TAG_COPY_TYPE, returnTypeName)
                            .replace(TEMPLATE_TAG_COPY_PARAM, copyParam);
                }
                break;
            case GET_OBJ_POINTER_STATIC:
                content = METHOD_GET_OBJ_POINTER_STATIC_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case GET_OBJ_POINTER:
                content = METHOD_GET_OBJ_POINTER_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case GET_PRIMITIVE_STATIC:
                content = METHOD_GET_PRIMITIVE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case GET_PRIMITIVE:
                content = METHOD_GET_PRIMITIVE_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName).replace(TEMPLATE_TAG_CAST, returnCastStr);
                break;
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
        IDLEnum anEnum = idlFile.getEnum(classType);
        if(anEnum != null) {
            paramName = "(" + classType + ")" + paramName;
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