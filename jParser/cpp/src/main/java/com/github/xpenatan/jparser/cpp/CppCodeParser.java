package com.github.xpenatan.jparser.cpp;

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
import com.github.xpenatan.jparser.idl.IDLAttribute;
import com.github.xpenatan.jparser.idl.IDLConstructor;
import com.github.xpenatan.jparser.idl.IDLEnum;
import com.github.xpenatan.jparser.idl.IDLFile;
import com.github.xpenatan.jparser.idl.IDLHelper;
import com.github.xpenatan.jparser.idl.parser.IDLAttributeOperation;
import com.github.xpenatan.jparser.idl.parser.IDLDefaultCodeParser;
import com.github.xpenatan.jparser.idl.IDLClass;
import com.github.xpenatan.jparser.idl.IDLMethod;
import com.github.xpenatan.jparser.idl.IDLParameter;
import com.github.xpenatan.jparser.idl.IDLReader;
import com.github.xpenatan.jparser.idl.parser.IDLMethodOperation;
import java.util.ArrayList;

public class CppCodeParser extends IDLDefaultCodeParser {

    private static final String HEADER_CMD = "JNI";

    protected static final String TEMPLATE_TAG_TYPE = "[TYPE]";
    protected static final String TEMPLATE_TAG_METHOD = "[METHOD]";
    protected static final String TEMPLATE_TAG_OPERATOR = "[OPERATOR]";
    protected static final String TEMPLATE_TAG_ATTRIBUTE = "[ATTRIBUTE]";
    protected static final String TEMPLATE_TAG_ENUM = "[ENUM]";
    protected static final String TEMPLATE_TAG_ATTRIBUTE_TYPE = "[ATTRIBUTE_TYPE]";
    protected static final String TEMPLATE_TAG_RETURN_TYPE = "[RETURN_TYPE]";
    protected static final String TEMPLATE_TAG_CONST = "[CONST]";
    protected static final String TEMPLATE_TAG_COPY_TYPE = "[COPY_TYPE]";
    protected static final String TEMPLATE_TAG_COPY_PARAM = "[COPY_PARAM]";
    protected static final String TEMPLATE_TAG_CONSTRUCTOR = "[CONSTRUCTOR]";
    protected static final String TEMPLATE_TAG_CAST = "[CAST]";

    protected static final String GET_CONSTRUCTOR_OBJ_POINTER_TEMPLATE =
            "\nreturn (jlong)new [CONSTRUCTOR];\n";

    protected static final String METHOD_DELETE_OBJ_POINTER_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "delete nativeObject;\n";

    protected static final String ATTRIBUTE_SET_PRIMITIVE_STATIC_TEMPLATE =
            "\n[TYPE]::[ATTRIBUTE] = [ATTRIBUTE];\n";

    protected static final String ATTRIBUTE_SET_PRIMITIVE_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "nativeObject->[ATTRIBUTE] = [CAST][ATTRIBUTE];\n";

    protected static final String ATTRIBUTE_SET_OBJECT_POINTER_STATIC_TEMPLATE =
            "\n[TYPE]::[ATTRIBUTE] = ([ATTRIBUTE_TYPE]*)[ATTRIBUTE]_addr;\n";

    protected static final String ATTRIBUTE_SET_OBJECT_POINTER_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "nativeObject->[ATTRIBUTE] = ([ATTRIBUTE_TYPE]*)[ATTRIBUTE]_addr;\n";

    protected static final String ATTRIBUTE_SET_OBJECT_VALUE_STATIC_TEMPLATE =
            "\n[TYPE]::[ATTRIBUTE] = *(([ATTRIBUTE_TYPE]*)[ATTRIBUTE]_addr);\n";

    protected static final String ATTRIBUTE_SET_OBJECT_VALUE_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "nativeObject->[ATTRIBUTE] = *(([ATTRIBUTE_TYPE]*)[ATTRIBUTE]_addr);\n";

    protected static final String ATTRIBUTE_GET_OBJECT_VALUE_STATIC_TEMPLATE =
            "\nreturn (jlong)&[TYPE]::[ATTRIBUTE];\n";

    protected static final String ATTRIBUTE_GET_OBJECT_VALUE_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return (jlong)&nativeObject->[ATTRIBUTE];\n";

    protected static final String ATTRIBUTE_GET_OBJECT_POINTER_STATIC_TEMPLATE =
            "\nreturn (jlong)[TYPE]::[ATTRIBUTE];\n";

    protected static final String ATTRIBUTE_GET_OBJECT_POINTER_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "[CONST][ATTRIBUTE_TYPE]* attr = nativeObject->[ATTRIBUTE];\n" +
            "return (jlong)attr;\n";

    protected static final String ATTRIBUTE_GET_PRIMITIVE_STATIC_TEMPLATE =
            "\nreturn [TYPE]::[ATTRIBUTE];\n";

    protected static final String ATTRIBUTE_GET_PRIMITIVE_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return [CAST]nativeObject->[ATTRIBUTE];\n";

    protected static final String METHOD_GET_OBJ_VALUE_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "static [COPY_TYPE] [COPY_PARAM];\n" +
            "[COPY_PARAM] = nativeObject->[METHOD];\n" +
            "return (jlong)&[COPY_PARAM];";

    protected static final String METHOD_GET_OBJ_VALUE_STATIC_TEMPLATE =
            "\nstatic [COPY_TYPE] [COPY_PARAM];\n" +
            "[COPY_PARAM] = [TYPE]::[METHOD];\n" +
            "return (jlong)&[COPY_PARAM];";

    protected static final String METHOD_CALL_VOID_STATIC_TEMPLATE =
            "\n[TYPE]::[METHOD];\n";

    protected static final String METHOD_CALL_VOID_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "nativeObject->[METHOD];\n";

    protected static final String METHOD_GET_OBJ_POINTER_STATIC_TEMPLATE =
            "\nreturn (jlong)[TYPE]::[METHOD];\n";

    protected static final String METHOD_GET_OBJ_POINTER_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "[CONST][RETURN_TYPE]* obj = nativeObject->[METHOD];\n" +
            "return (jlong)obj;\n";

    protected static final String METHOD_GET_REF_OBJ_POINTER_STATIC_TEMPLATE =
            "\nreturn (jlong)&[TYPE]::[METHOD];\n";

    protected static final String METHOD_GET_REF_OBJ_POINTER_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return (jlong)&nativeObject->[METHOD];\n";

    protected static final String METHOD_GET_REF_OBJ_POINTER_OPERATOR_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return (jlong)&[OPERATOR];\n";

    protected static final String METHOD_GET_PRIMITIVE_STATIC_TEMPLATE =
            "\nreturn [CAST][TYPE]::[METHOD];\n";

    protected static final String METHOD_GET_PRIMITIVE_TEMPLATE =
            "\n[TYPE]* nativeObject = ([TYPE]*)this_addr;\n" +
            "return [CAST]nativeObject->[METHOD];\n";

    protected static final String ENUM_GET_INT_TEMPLATE =
            "\nreturn (jlong)[ENUM];\n";

    private final CppGenerator cppGenerator;

    public CppCodeParser(CppGenerator cppGenerator, String cppDir) {
        this(cppGenerator, null, "", cppDir);
    }

    public CppCodeParser(CppGenerator cppGenerator, IDLReader idlReader, String basePackage, String cppDir) {
        super(basePackage, HEADER_CMD, idlReader, cppDir);
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
    public void onIDLDeConstructorGenerated(JParser jParser, IDLClass idlClass, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration nativeMethodDeclaration) {
        String classTypeName = idlClass.classHeader.prefixName + classDeclaration.getNameAsString();

        String content = METHOD_DELETE_OBJ_POINTER_TEMPLATE.replace(TEMPLATE_TAG_TYPE, classTypeName);

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

        String getPrimitiveCast = "";
        String attributeType = idlAttribute.type;
        String constTag = "";
        if(idlAttribute.isConst) {
            constTag = "const ";
        }

        IDLClass retTypeClass = idlAttribute.idlFile.getClass(attributeType);
        if(retTypeClass != null) {
            attributeType = retTypeClass.classHeader.prefixName + attributeType;
        }

        String attributeReturnCast = "";

        IDLEnum idlEnum = idlAttribute.idlFile.getEnum(attributeType);
        if(idlEnum != null) {
            if(idlEnum.typePrefix.equals(attributeType)) {
                attributeReturnCast = "(" + attributeType + ")";
            }
            else {
                attributeReturnCast = "(" + idlEnum.typePrefix + "::" + attributeType + ")";
            }
            getPrimitiveCast = "(jint)";
        }

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
                content = ATTRIBUTE_GET_OBJECT_POINTER_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName).replace(TEMPLATE_TAG_ATTRIBUTE_TYPE, attributeType).replace(TEMPLATE_TAG_CONST, constTag);
                break;
            case GET_OBJECT_POINTER_STATIC:
                content = ATTRIBUTE_GET_OBJECT_POINTER_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case SET_PRIMITIVE:
                content = ATTRIBUTE_SET_PRIMITIVE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName).replace(TEMPLATE_TAG_CAST, attributeReturnCast);
                break;
            case SET_PRIMITIVE_STATIC:
                content = ATTRIBUTE_SET_PRIMITIVE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case GET_PRIMITIVE:
                content = ATTRIBUTE_GET_PRIMITIVE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, classTypeName).replace(TEMPLATE_TAG_CAST, getPrimitiveCast);
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
    public void onIDLEnumMethodGenerated(JParser jParser, IDLEnum idlEnum, ClassOrInterfaceDeclaration classDeclaration, String enumStr, FieldDeclaration fieldDeclaration, MethodDeclaration nativeMethodDeclaration) {
        String content  = "";
        content = ENUM_GET_INT_TEMPLATE.replace(TEMPLATE_TAG_ENUM, enumStr);
        String header = "[-" + HEADER_CMD + ";" + CMD_NATIVE + "]";
        String blockComment = header + content;
        nativeMethodDeclaration.setBlockComment(blockComment);
    }

    private void setupMethodGenerated(IDLMethod idlMethod, String param, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethod) {
        Type returnType = methodDeclaration.getType();
        String returnTypeStr = idlMethod.returnType;
        String methodName = idlMethod.name;
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
        if(idlMethod.isAny) {
            returnCastStr = "(jlong)";
        }

        String constTag = "";
        if(idlMethod.isReturnConst) {
            constTag = "const ";
        }

        String operator = getOperation(idlMethod.operator, param);
        String content = null;
        IDLMethodOperation.Op op = IDLMethodOperation.getEnum(idlMethod, methodDeclaration, nativeMethod);
        switch(op) {
            case CALL_VOID_STATIC:
                content = METHOD_CALL_VOID_STATIC_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case CALL_VOID:
                content = METHOD_CALL_VOID_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case GET_OBJ_REF_POINTER_STATIC:
                content = METHOD_GET_REF_OBJ_POINTER_STATIC_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
                break;
            case GET_OBJ_REF_POINTER:
                if(operator.isEmpty()) {
                    content = METHOD_GET_REF_OBJ_POINTER_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName);
                }
                else {
                    content = METHOD_GET_REF_OBJ_POINTER_OPERATOR_TEMPLATE.replace(TEMPLATE_TAG_OPERATOR, operator).replace(TEMPLATE_TAG_TYPE, classTypeName);
                }
                break;
            case GET_OBJ_VALUE_STATIC: {
                    String returnTypeName = returnType.asClassOrInterfaceType().asClassOrInterfaceType().getNameAsString();
                    IDLClass retTypeClass = idlMethod.idlFile.getClass(returnTypeName);
                    if(retTypeClass != null) {
                        returnTypeName = retTypeClass.classHeader.prefixName + returnTypeName;
                    }
                    String copyParam = "copy_addr";
                    content = METHOD_GET_OBJ_VALUE_STATIC_TEMPLATE
                            .replace(TEMPLATE_TAG_METHOD, methodCaller)
                            .replace(TEMPLATE_TAG_TYPE, classTypeName)
                            .replace(TEMPLATE_TAG_COPY_TYPE, returnTypeName)
                            .replace(TEMPLATE_TAG_COPY_PARAM, copyParam);
                }
                break;
            case GET_OBJ_VALUE: {
                    String returnTypeName = returnType.asClassOrInterfaceType().asClassOrInterfaceType().getNameAsString();
                    IDLClass retTypeClass = idlMethod.idlFile.getClass(returnTypeName);
                    if(retTypeClass != null) {
                        returnTypeName = retTypeClass.classHeader.prefixName + returnTypeName;
                    }
                    String copyParam = "copy_addr";
                    content = METHOD_GET_OBJ_VALUE_TEMPLATE
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
                content = METHOD_GET_OBJ_POINTER_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName).replace(TEMPLATE_TAG_RETURN_TYPE, returnTypeStr).replace(TEMPLATE_TAG_CONST, constTag);
                break;
            case GET_PRIMITIVE_STATIC:
                content = METHOD_GET_PRIMITIVE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName).replace(TEMPLATE_TAG_CAST, returnCastStr);
                break;
            case GET_PRIMITIVE:
                content = METHOD_GET_PRIMITIVE_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, classTypeName).replace(TEMPLATE_TAG_CAST, returnCastStr);
                break;
        }

        String header = "[-" + HEADER_CMD + ";" + CMD_NATIVE + "]";
        String blockComment = header + content;
        nativeMethod.setBlockComment(blockComment);
    }

    private static String getOperation(String operatorCode, String param) {
        String oper = "";
        if(!operatorCode.isEmpty()) {
            if(operatorCode.equals("[]")) {
                oper = "nativeObject[" + param + "]";
            }
            else if(operatorCode.equals("=")){
                oper = "(*nativeObject = " + param + ")";
            }
            //TODO add more operator c++ code
//            else if(operatorCode.equals("+=")){
//                oper = " += " + param;
//            }
//            else if(operatorCode.equals("-=")){
//                oper = " -= " + param;
//            }
//            else if(operatorCode.equals("*=")){
//                oper = " *= " + param;
//            }
        }
        return oper;
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
            String paramName = getParam(idlParameter.idlFile, isObject, idlParameter.name, idlParameter.type, idlParameter.isAny, idlParameter.isRef, idlParameter.isValue);
            if(i > 0) {
                param += ", ";
            }
            param += paramName;
        }
        return param;
    }

    private static String getParam(IDLFile idlFile, boolean isObject, String paramName, String classType, boolean isAny, boolean isRef, boolean isValue) {
        if(isObject && !classType.equals("String")) {
            paramName += "_addr";
            IDLClass paramClass = idlFile.getClass(classType);
            String cArray = IDLHelper.getCArray(classType);
            if(cArray != null) {
                paramName = "(" + cArray + ")" + paramName;
            }
            else {
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
        }
        else if(isAny) {
            paramName = "( void* )" + paramName;
        }

        IDLEnum anEnum = idlFile.getEnum(classType);
        if(anEnum != null) {
            if(anEnum.typePrefix.equals(classType)) {
                paramName = "(" + classType + ")" + paramName;
            }
            else {
                paramName = "(" + anEnum.typePrefix + "::" + classType + ")" + paramName;
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

        //TODO fix this. Disable auto include because path may be wrong.
//        if(include != null) {
//            String comment = "" +
//                    "/*[-JNI;-NATIVE]\n" +
//                    "       #include <" + include + ">\n" +
//                    "    */";
//            Position begin = new Position(0, 0);
//            Position end = new Position(0, 0);
//            Range range = new Range(begin, end);
//            RawCodeBlock blockComment = new RawCodeBlock();
//            blockComment.setRange(range);
//            blockComment.setContent(comment);
//            classOrInterfaceDeclaration.getMembers().add(0, blockComment);
//        }

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