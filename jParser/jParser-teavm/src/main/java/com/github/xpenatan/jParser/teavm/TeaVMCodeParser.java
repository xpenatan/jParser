package com.github.xpenatan.jParser.teavm;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.utils.Pair;
import com.github.xpenatan.jParser.core.JParser;
import com.github.xpenatan.jParser.core.JParserHelper;
import com.github.xpenatan.jParser.core.JParserItem;
import com.github.xpenatan.jParser.idl.IDLAttribute;
import com.github.xpenatan.jParser.idl.IDLClass;
import com.github.xpenatan.jParser.idl.IDLClassOrEnum;
import com.github.xpenatan.jParser.idl.IDLConstructor;
import com.github.xpenatan.jParser.idl.IDLEnumClass;
import com.github.xpenatan.jParser.idl.IDLEnumItem;
import com.github.xpenatan.jParser.idl.IDLFile;
import com.github.xpenatan.jParser.idl.IDLMethod;
import com.github.xpenatan.jParser.idl.IDLParameter;
import com.github.xpenatan.jParser.idl.IDLReader;
import com.github.xpenatan.jParser.idl.parser.IDLAttributeOperation;
import com.github.xpenatan.jParser.idl.parser.IDLDefaultCodeParser;
import com.github.xpenatan.jParser.idl.parser.IDLMethodOperation;
import com.github.xpenatan.jParser.idl.parser.IDLMethodParser;
import com.github.xpenatan.jParser.idl.parser.data.IDLParameterData;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeaVMCodeParser extends IDLDefaultCodeParser {

    public static boolean CAST_LONG_TO_INT = true;
    public static boolean USE_REF_ARRAY = false; // Not supported in WASM mode

    private static final String HEADER_CMD = "TEAVM";

    private static final String PACKAGE_PREFIX = "gen.";

    protected static final String TEMPLATE_TAG_TYPE = "[TYPE]";
    protected static final String TEMPLATE_TAG_METHOD = "[METHOD]";
    protected static final String TEMPLATE_TAG_ATTRIBUTE = "[ATTRIBUTE]";
    protected static final String TEMPLATE_TAG_MODULE = "[MODULE]";
    protected static final String TEMPLATE_TAG_CONSTRUCTOR = "[CONSTRUCTOR]";
    protected static final String TEMPLATE_TAG_ENUM = "[ENUM]";
    protected static final String TEMPLATE_TAG_OPERATOR = "[OPERATOR]";
    protected static final String TEMPLATE_TAG_OPERATOR_TYPE = "[OPERATOR_TYPE]";

    protected static final String GET_CONSTRUCTOR_OBJ_POINTER_TEMPLATE =
            "var jsObj = new [MODULE].[CONSTRUCTOR];\n" +
            "return [MODULE].getPointer(jsObj);";

    protected static final String METHOD_DELETE_OBJ_POINTER_TEMPLATE =
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "[MODULE].destroy(jsObj);";

    protected static final String METHOD_COPY_VALUE_STATIC_TEMPLATE =
            "var returnedJSObj = [MODULE].[TYPE].prototype.[METHOD];\n" +
            "var operatorObj = [MODULE].wrapPointer(copy_addr, [MODULE].[OPERATOR_TYPE]);\n" +
            "operatorObj.[MODULE].[OPERATOR](returnedJSObj);";

    protected static final String METHOD_COPY_VALUE_TEMPLATE =
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "var operatorObj = [MODULE].wrapPointer(copy_addr, [MODULE].[OPERATOR_TYPE]);\n" +
            "var returnedJSObj = jsObj.[METHOD];\n" +
            "operatorObj.[OPERATOR](returnedJSObj);";

    /**
     * When a js method returns a js object, we need get its pointer.
     */
    protected static final String METHOD_GET_OBJ_POINTER_TEMPLATE =
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "var returnedJSObj = jsObj.[METHOD];\n" +
            "if(!returnedJSObj.hasOwnProperty('ptr')) return 0; \n" +
            "return [MODULE].getPointer(returnedJSObj);";

    protected static final String METHOD_GET_OBJ_POINTER_STATIC_TEMPLATE =
            "var returnedJSObj = [MODULE].[TYPE].prototype.[METHOD];\n" +
            "if(!returnedJSObj.hasOwnProperty('ptr')) return 0; \n" +
            "return [MODULE].getPointer(returnedJSObj);";

    protected static final String METHOD_GET_PRIMITIVE_TEMPLATE =
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "var returnedJSObj = jsObj.[METHOD];\n" +
            "return returnedJSObj;";

    protected static final String METHOD_GET_PRIMITIVE_STATIC_TEMPLATE =
            "var returnedJSObj = [MODULE].[TYPE].prototype.[METHOD];\n" +
            "return returnedJSObj;";

    protected static final String METHOD_CALL_VOID_TEMPLATE =
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "jsObj.[METHOD];";

    protected static final String METHOD_CALL_VOID_STATIC_TEMPLATE =
            "[MODULE].[TYPE].prototype.[METHOD];";

    protected static final String ATTRIBUTE_GET_PRIMITIVE_TEMPLATE =
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "return jsObj.get_[ATTRIBUTE]();";

    protected static final String ATTRIBUTE_ARRAY_GET_PRIMITIVE_TEMPLATE =
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "return jsObj.get_[ATTRIBUTE](index);";

    protected static final String ATTRIBUTE_GET_PRIMITIVE_STATIC_TEMPLATE =
            "return [MODULE].[TYPE].prototype.get_[ATTRIBUTE]()";

    protected static final String ATTRIBUTE_ARRAY_GET_PRIMITIVE_STATIC_TEMPLATE =
            "return [MODULE].[TYPE].prototype.get_[ATTRIBUTE](index)";

    protected static final String ATTRIBUTE_GET_OBJECT_POINTER_STATIC_TEMPLATE =
            "var returnedJSObj = [MODULE].[TYPE].prototype.get_[ATTRIBUTE]();\n" +
            "if(!returnedJSObj.hasOwnProperty('ptr')) return 0; \n" +
            "return [MODULE].getPointer(returnedJSObj);";

    protected static final String ATTRIBUTE_ARRAY_GET_OBJECT_POINTER_STATIC_TEMPLATE =
            "var returnedJSObj = [MODULE].[TYPE].prototype.get_[ATTRIBUTE](index);\n" +
            "if(!returnedJSObj.hasOwnProperty('ptr')) return 0; \n" +
            "return [MODULE].getPointer(returnedJSObj);";

    protected static final String ATTRIBUTE_GET_OBJECT_POINTER_TEMPLATE =
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "var returnedJSObj = jsObj.get_[ATTRIBUTE]();\n" +
            "if(!returnedJSObj.hasOwnProperty('ptr')) return 0; \n" +
            "return [MODULE].getPointer(returnedJSObj);";

    protected static final String ATTRIBUTE_ARRAY_GET_OBJECT_POINTER_TEMPLATE =
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "var returnedJSObj = jsObj.get_[ATTRIBUTE](index);\n" +
            "if(!returnedJSObj.hasOwnProperty('ptr')) return 0; \n" +
            "return [MODULE].getPointer(returnedJSObj);";

    protected static final String ATTRIBUTE_SET_OBJECT_POINTER_TEMPLATE =
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "jsObj.set_[ATTRIBUTE]([ATTRIBUTE]_addr);";

    protected static final String ATTRIBUTE_ARRAY_SET_OBJECT_POINTER_TEMPLATE =
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "jsObj.set_[ATTRIBUTE](index, [ATTRIBUTE]_addr);";

    protected static final String ATTRIBUTE_SET_OBJECT_POINTER_STATIC_TEMPLATE =
            "[MODULE].[TYPE].prototype.set_[ATTRIBUTE]([ATTRIBUTE]_addr);\n";

    protected static final String ATTRIBUTE_ARRAY_SET_OBJECT_POINTER_STATIC_TEMPLATE =
            "[MODULE].[TYPE].prototype.set_[ATTRIBUTE](index, [ATTRIBUTE]_addr);\n";

    protected static final String ATTRIBUTE_SET_OBJECT_VALUE_TEMPLATE =
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "jsObj.set_[ATTRIBUTE]([ATTRIBUTE]_addr);";

    protected static final String ATTRIBUTE_ARRAY_SET_OBJECT_VALUE_TEMPLATE =
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "jsObj.set_[ATTRIBUTE](index, [ATTRIBUTE]_addr);";

    protected static final String ATTRIBUTE_SET_OBJECT_VALUE_STATIC_TEMPLATE =
            "[MODULE].[TYPE].prototype.set_[ATTRIBUTE]([ATTRIBUTE]_addr);\n";

    protected static final String ATTRIBUTE_ARRAY_SET_OBJECT_VALUE_STATIC_TEMPLATE =
            "[MODULE].[TYPE].prototype.set_[ATTRIBUTE](index, [ATTRIBUTE]_addr);\n";

    protected static final String ATTRIBUTE_SET_PRIMITIVE_TEMPLATE =
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "jsObj.set_[ATTRIBUTE]([ATTRIBUTE]);";

    protected static final String ATTRIBUTE_ARRAY_SET_PRIMITIVE_TEMPLATE =
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "jsObj.set_[ATTRIBUTE](index, [ATTRIBUTE]);";

    protected static final String ATTRIBUTE_SET_PRIMITIVE_STATIC_TEMPLATE =
            "[MODULE].[TYPE].prototype.set_[ATTRIBUTE]([ATTRIBUTE]);\n";

    protected static final String ATTRIBUTE_ARRAY_SET_PRIMITIVE_STATIC_TEMPLATE =
            "[MODULE].[TYPE].prototype.set_[ATTRIBUTE](index, [ATTRIBUTE]);\n";

    protected static final String ATTRIBUTE_GET_OBJECT_VALUE_STATIC_TEMPLATE = ATTRIBUTE_GET_OBJECT_POINTER_STATIC_TEMPLATE;

    protected static final String ATTRIBUTE_ARRAY_GET_OBJECT_VALUE_STATIC_TEMPLATE = ATTRIBUTE_ARRAY_GET_OBJECT_POINTER_STATIC_TEMPLATE;

    protected static final String ATTRIBUTE_GET_OBJECT_VALUE_TEMPLATE = ATTRIBUTE_GET_OBJECT_POINTER_TEMPLATE;

    protected static final String ATTRIBUTE_ARRAY_GET_OBJECT_VALUE_TEMPLATE = ATTRIBUTE_ARRAY_GET_OBJECT_POINTER_TEMPLATE;

    protected static final String ENUM_GET_INT_TEMPLATE =
            "\nreturn [MODULE].[ENUM];\n";

    private final String module;

    public TeaVMCodeParser(IDLReader idlReader, String module, String basePackage, String cppDir) {
        super(basePackage, HEADER_CMD, idlReader, cppDir);
        this.module = module;
        generateClass = true;
    }

    @Override
    protected void setJavaBodyNativeCMD(String content, MethodDeclaration nativeMethodDeclaration) {
//        convertNativeMethodLongToInt(nativeMethodDeclaration);
        String param = "";
        NodeList<Parameter> parameters = nativeMethodDeclaration.getParameters();
        int size = parameters.size();
        for(int i = 0; i < size; i++) {
            Parameter parameter = parameters.get(i);
            SimpleName name = parameter.getName();
            param += name;
            if(i < size - 1) {
                param += "\", \"";
            }
        }

        if(USE_REF_ARRAY) {
            convertJavaPrimitiveArrayToJavaScriptReferenceArray(parameters);
        }

        if(content != null) {
            content = content.replace("\n", "").replace("\r", "").replaceAll("[ ]+", " ");
            content = content.trim();

            if(!content.isEmpty()) {
                if(!nativeMethodDeclaration.isAnnotationPresent("JSBody")) {
                    NormalAnnotationExpr normalAnnotationExpr = nativeMethodDeclaration.addAndGetAnnotation("org.teavm.jso.JSBody");
                    if(!param.isEmpty()) {
                        normalAnnotationExpr.addPair("params", "{\"" + param + "\"}");
                    }
                    normalAnnotationExpr.addPair("script", "\"" + content + "\"");
                }
            }
        }
    }

    private void convertJavaPrimitiveArrayToJavaScriptReferenceArray(NodeList<Parameter> parameters) {
        // If you send an array to module and it writes to it, the JSbyRef annotation is required.
        int size = parameters.size();
        for(int i = 0; i < size; i++) {
            Parameter parameter = parameters.get(i);
            Type type = parameter.getType();
            if(type.isArrayType()) {
                ArrayType arrayType = (ArrayType)type;
                if(arrayType.getComponentType().isPrimitiveType()) {
                    parameter.addAndGetAnnotation("org.teavm.jso.JSByRef");
                }
            }
        }
    }

    @Override
    public void onIDLConstructorGenerated(JParser jParser, IDLConstructor idlConstructor, ClassOrInterfaceDeclaration classDeclaration, ConstructorDeclaration constructorDeclaration, MethodDeclaration nativeMethodDeclaration) {
        String param = "";

        IDLClass idlClass = idlConstructor.idlClass;

        String className = idlClass.name; // teavm class cannot have prefix.
        MethodCallExpr caller = new MethodCallExpr();
        caller.setName(className);

        NodeList<Parameter> nativeParameters = nativeMethodDeclaration.getParameters();

        int size = nativeParameters.size();
        for(int i = 0; i < size; i++) {
            Parameter parameter = nativeParameters.get(i);
            String paramName = parameter.getNameAsString();
            caller.addArgument(paramName);
            param += paramName;
            if(i < size - 1) {
                param += "\", \"";
            }
        }
        String constructor = caller.toString();

        String content = null;

        content = GET_CONSTRUCTOR_OBJ_POINTER_TEMPLATE.replace(TEMPLATE_TAG_CONSTRUCTOR, constructor).replace(TEMPLATE_TAG_MODULE, module);

        String header = "[-" + HEADER_CMD + ";" + CMD_NATIVE + "]";
        String blockComment = header + "\n" + content + "\n";
        nativeMethodDeclaration.setBlockComment(blockComment);

        content = content.replace("\n", "");
        content = content.trim();

        if(!content.isEmpty()) {
            if(!nativeMethodDeclaration.isAnnotationPresent("JSBody")) {
                NormalAnnotationExpr normalAnnotationExpr = nativeMethodDeclaration.addAndGetAnnotation("org.teavm.jso.JSBody");
                if(!param.isEmpty()) {
                    normalAnnotationExpr.addPair("params", "{\"" + param + "\"}");
                }
                normalAnnotationExpr.addPair("script", "\"" + content + "\"");
            }
        }
    }

    @Override
    public void onIDLDeConstructorGenerated(JParser jParser, IDLClass idlClass, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration nativeMethodDeclaration) {
        String returnTypeName ;
        if(idlClass.callbackImpl == null) {
            returnTypeName = classDeclaration.getNameAsString();
        }
        else {
            returnTypeName = idlClass.callbackImpl.name;
        }

        String content = METHOD_DELETE_OBJ_POINTER_TEMPLATE.replace(TEMPLATE_TAG_MODULE, module).replace(TEMPLATE_TAG_TYPE, returnTypeName);

        String header = "[-" + HEADER_CMD + ";" + CMD_NATIVE + "]";
        String blockComment = header + "\n" + content + "\n";
        nativeMethodDeclaration.setBlockComment(blockComment);
    }

    @Override
    public void onIDLMethodGenerated(JParser jParser, IDLMethod idlMethod, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethod) {
        // IDL parser generate our empty methods with default return values.
        // We now modify it to match teavm native calls

//        convertLongToInt(methodDeclaration.getBody().get(), nativeMethod);
        String methodName = idlMethod.name;
        String param = getParams(idlMethod, methodDeclaration);
        String annotationParam = "";

        NodeList<Parameter> nativeParameters = nativeMethod.getParameters();
        int size = nativeParameters.size();
        for(int i = 0; i < size; i++) {
            Parameter parameter = nativeParameters.get(i);
            String paramName = parameter.getNameAsString();
            annotationParam += paramName;
            if(i < size - 1) {
                annotationParam += "\", \"";
            }
        }

        String returnTypeName = classDeclaration.getNameAsString();
        String methodCaller = methodName + "(" + param + ")";

        String content = null;

        IDLMethodOperation.Op op = IDLMethodOperation.getEnum(idlMethod, methodDeclaration, nativeMethod);
        switch(op) {
            case CALL_VOID_STATIC:
                content = METHOD_CALL_VOID_STATIC_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case CALL_VOID:
                content = METHOD_CALL_VOID_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_OBJ_REF_POINTER_STATIC:
                content = METHOD_GET_OBJ_POINTER_STATIC_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_OBJ_REF_POINTER:
                content = METHOD_GET_OBJ_POINTER_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_OBJ_VALUE_STATIC:
                content = METHOD_GET_OBJ_POINTER_STATIC_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_OBJ_VALUE:
                content = METHOD_GET_OBJ_POINTER_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_OBJ_POINTER_STATIC:
                content = METHOD_GET_OBJ_POINTER_STATIC_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_OBJ_POINTER:
                content = METHOD_GET_OBJ_POINTER_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_PRIMITIVE_STATIC:
                content = METHOD_GET_PRIMITIVE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_PRIMITIVE:
                content = METHOD_GET_PRIMITIVE_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
        }

        if(content != null) {
            String header = "[-" + HEADER_CMD + ";" + CMD_NATIVE + "]";
            String blockComment = header + "\n" + content + "\n";
            nativeMethod.setBlockComment(blockComment);

            content = content.replace("\n", "");
            content = content.trim();

            if(!content.isEmpty()) {
                if(!nativeMethod.isAnnotationPresent("JSBody")) {
                    NormalAnnotationExpr normalAnnotationExpr = nativeMethod.addAndGetAnnotation("org.teavm.jso.JSBody");
                    if(!annotationParam.isEmpty()) {
                        normalAnnotationExpr.addPair("params", "{\"" + annotationParam + "\"}");
                    }
                    normalAnnotationExpr.addPair("script", "\"" + content + "\"");
                }
            }
        }
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
            String paramName = getParam(idlParameter.idlFile, idlParameter.isEnum(), isObject, idlParameter.name, idlParameter.getJavaType(), idlParameter.isRef, idlParameter.isValue);
            if(i > 0) {
                param += ", ";
            }
            param += paramName;
        }
        return param;
    }

    private static String getParam(IDLFile idlFile, boolean isEnum, boolean isObject, String paramName, String classType, boolean isRef, boolean isValue) {
        if(!isEnum && isObject && !classType.equals("String")) {
            paramName += IDLDefaultCodeParser.NATIVE_PARAM_ADDRESS;
        }
        return paramName;
    }

    @Override
    public void onIDLAttributeGenerated(JParser jParser, IDLAttribute idlAttribute, boolean isSet, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethod) {
//        convertLongToInt(methodDeclaration.getBody().get(), nativeMethod);

        String returnTypeName = classDeclaration.getNameAsString();
        String attributeName = idlAttribute.name;

        String param = "";
        NodeList<Parameter> parameters = nativeMethod.getParameters();
        int size = parameters.size();
        for(int i = 0; i < size; i++) {
            Parameter parameter = parameters.get(i);
            SimpleName name = parameter.getName();
            param += name;
            if(i < size - 1) {
                param += "\", \"";
            }
        }

        String content = null;

        IDLAttributeOperation.Op op = IDLAttributeOperation.getEnum(isSet, idlAttribute, methodDeclaration, nativeMethod);
        switch(op) {
            case SET_OBJECT_VALUE:
                content = ATTRIBUTE_SET_OBJECT_VALUE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case SET_ARRAY_OBJECT_VALUE:
                content = ATTRIBUTE_ARRAY_SET_OBJECT_VALUE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case SET_OBJECT_VALUE_STATIC:
                content = ATTRIBUTE_SET_OBJECT_VALUE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case SET_ARRAY_OBJECT_VALUE_STATIC:
                content = ATTRIBUTE_ARRAY_SET_OBJECT_VALUE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_OBJECT_VALUE:
                content = ATTRIBUTE_GET_OBJECT_VALUE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName)
                        .replace(TEMPLATE_TAG_TYPE, returnTypeName)
                        .replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_ARRAY_OBJECT_VALUE:
                content = ATTRIBUTE_ARRAY_GET_OBJECT_VALUE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName)
                        .replace(TEMPLATE_TAG_TYPE, returnTypeName)
                        .replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_OBJECT_VALUE_STATIC:
                content = ATTRIBUTE_GET_OBJECT_VALUE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName)
                        .replace(TEMPLATE_TAG_TYPE, returnTypeName)
                        .replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_ARRAY_OBJECT_VALUE_STATIC:
                content = ATTRIBUTE_ARRAY_GET_OBJECT_VALUE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName)
                        .replace(TEMPLATE_TAG_TYPE, returnTypeName)
                        .replace(TEMPLATE_TAG_MODULE, module);
                break;
            case SET_OBJECT_POINTER:
                content = ATTRIBUTE_SET_OBJECT_POINTER_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case SET_ARRAY_OBJECT_POINTER:
                content = ATTRIBUTE_ARRAY_SET_OBJECT_POINTER_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case SET_OBJECT_POINTER_STATIC:
                content = ATTRIBUTE_SET_OBJECT_POINTER_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case SET_ARRAY_OBJECT_POINTER_STATIC:
                content = ATTRIBUTE_ARRAY_SET_OBJECT_POINTER_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_OBJECT_POINTER:
                content = ATTRIBUTE_GET_OBJECT_POINTER_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_ARRAY_OBJECT_POINTER:
                content = ATTRIBUTE_ARRAY_GET_OBJECT_POINTER_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_OBJECT_POINTER_STATIC:
                content = ATTRIBUTE_GET_OBJECT_POINTER_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_ARRAY_OBJECT_POINTER_STATIC:
                content = ATTRIBUTE_ARRAY_GET_OBJECT_POINTER_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case SET_PRIMITIVE:
                content = ATTRIBUTE_SET_PRIMITIVE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case SET_ARRAY_PRIMITIVE:
                content = ATTRIBUTE_ARRAY_SET_PRIMITIVE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case SET_PRIMITIVE_STATIC:
                content = ATTRIBUTE_SET_PRIMITIVE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case SET_ARRAY_PRIMITIVE_STATIC:
                content = ATTRIBUTE_ARRAY_SET_PRIMITIVE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_PRIMITIVE:
                content = ATTRIBUTE_GET_PRIMITIVE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_ARRAY_PRIMITIVE:
                content = ATTRIBUTE_ARRAY_GET_PRIMITIVE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_PRIMITIVE_STATIC:
                content = ATTRIBUTE_GET_PRIMITIVE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_ARRAY_PRIMITIVE_STATIC:
                content = ATTRIBUTE_ARRAY_GET_PRIMITIVE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
        }

        if(content != null) {
            String header = "[-" + HEADER_CMD + ";" + CMD_NATIVE + "]";
            String blockComment = header + "\n" + content + "\n";
            nativeMethod.setBlockComment(blockComment);

            content = content.replace("\n", "");
            content = content.trim();

            if(!content.isEmpty()) {
                if(!nativeMethod.isAnnotationPresent("JSBody")) {
                    NormalAnnotationExpr normalAnnotationExpr = nativeMethod.addAndGetAnnotation("org.teavm.jso.JSBody");
                    if(!param.isEmpty()) {
                        normalAnnotationExpr.addPair("params", "{\"" + param + "\"}");
                    }
                    normalAnnotationExpr.addPair("script", "\"" + content + "\"");
                }
            }
        }
    }

    @Override
    public void onIDLEnumMethodGenerated(JParser jParser, IDLEnumClass idlEnum, EnumDeclaration enumDeclaration, IDLEnumItem enumItem, MethodDeclaration nativeMethodDeclaration) {
        String enumStr = enumItem.name;
        String content  = "";
        if(enumStr.contains("::")) {
            String[] split = enumStr.split("::");
            String leftName = split[0];
            String rightName = split[1];
            boolean isNameSpaceEnum = idlEnum.isNameSpace;
            if(isNameSpaceEnum) {
                enumStr = rightName;
            }
            else {
                // enum class cannot have child class name
                enumStr = rightName;
            }
        }
        content = ENUM_GET_INT_TEMPLATE.replace(TEMPLATE_TAG_ENUM, enumStr).replace(TEMPLATE_TAG_MODULE, module);
        String header = "[-" + HEADER_CMD + ";" + CMD_NATIVE + "]";
        String blockComment = header + content;
        nativeMethodDeclaration.setBlockComment(blockComment);
        content = content.replace("\n", "");
        content = content.trim();
        if(!nativeMethodDeclaration.isAnnotationPresent("JSBody")) {
            NormalAnnotationExpr normalAnnotationExpr = nativeMethodDeclaration.addAndGetAnnotation("org.teavm.jso.JSBody");
            normalAnnotationExpr.addPair("script", "\"" + content + "\"");
        }
    }

    @Override
    public void onIDLCallbackGenerated(JParser jParser, IDLClass idlClass, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration callbackDeclaration, ArrayList<Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>>> methods) {
        NodeList<Parameter> methodParameters = callbackDeclaration.getParameters();
        IDLClass idlCallbackClass = idlClass.callbackImpl;
        String callbackClassName = idlCallbackClass.name;
        Type methodReturnType = callbackDeclaration.getType();
        ArrayList<IDLParameterData> parameterArray = new ArrayList<>();
        for(int i = 0; i < methodParameters.size(); i++) {
            Parameter parameter = methodParameters.get(i);
            IDLParameterData data = new IDLParameterData();
            data.parameter = parameter;
            parameterArray.add(data);
        }
        MethodDeclaration nativeMethodDeclaration = IDLMethodParser.generateNativeMethod(idlReader, callbackDeclaration.getNameAsString(), parameterArray, methodReturnType, false);
        if(!JParserHelper.containsMethod(classDeclaration, nativeMethodDeclaration)) {
            NormalAnnotationExpr customAnnotation = new NormalAnnotationExpr();
            customAnnotation.setName("org.teavm.jso.JSBody");
            ArrayInitializerExpr paramsArray = new ArrayInitializerExpr();
            NodeList<Expression> values = paramsArray.getValues();
            String script = "var " + callbackClassName + " = "+ module + ".wrapPointer(this_addr, " + module + "." + callbackClassName + ");";
            MethodCallExpr caller = IDLMethodParser.createCaller(nativeMethodDeclaration);

            caller.addArgument(IDLDefaultCodeParser.NATIVE_ADDRESS);

            values.add(new StringLiteralExpr("this_addr"));
            for(int i = 0; i < methods.size(); i++) {
                Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>> pair = methods.get(i);
                IDLMethod idlMethod = pair.a;
                String methodName = idlMethod.name;
                values.add(new StringLiteralExpr(methodName));
                nativeMethodDeclaration.addParameter(methodName, methodName);
                script += " " + callbackClassName + "." + methodName + " = " + methodName + ";";
                caller.addArgument(methodName);
            }

            customAnnotation.addPair("params", paramsArray);
            customAnnotation.addPair("script", new StringLiteralExpr(script));
            nativeMethodDeclaration.addAnnotation(customAnnotation);
            classDeclaration.getMembers().add(nativeMethodDeclaration);

            BlockStmt callbackMethodBody = callbackDeclaration.getBody().get();

            for(int i = 0; i < methods.size(); i++) {
                Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>> pair = methods.get(i);
                IDLMethod idlMethod = pair.a;
                Pair<MethodDeclaration, MethodDeclaration> methodPair = pair.b;
                MethodDeclaration internalMethod = methodPair.a;
                MethodDeclaration publicMethod = methodPair.b;
                String internalMethodName = internalMethod.getNameAsString();
                String paramCode = "";
                String methodName = idlMethod.name;

                Type returnType = internalMethod.getType();
                String returnTypeStr = returnType.asString();

                NodeList<Parameter> parameters = internalMethod.getParameters();
                createInterfaceClass(classDeclaration, methodName, returnTypeStr, parameters);
                createInterfaceInstance(jParser, classDeclaration.findCompilationUnit().get(), methodName, internalMethodName, returnTypeStr, parameters, callbackMethodBody);
            }
            callbackMethodBody.addStatement(caller);
        }
    }

    private void createInterfaceClass(ClassOrInterfaceDeclaration classDeclaration, String methodName, String returnTypeStr, NodeList<Parameter> parameters) {
        ClassOrInterfaceDeclaration interfaceDecl = new ClassOrInterfaceDeclaration();
        interfaceDecl.setInterface(true); // Mark it as an interface
        interfaceDecl.setName(methodName);
        interfaceDecl.setPublic(true); // Optional, interfaces are public by default
        interfaceDecl.addExtendedType("org.teavm.jso.JSObject");

        NormalAnnotationExpr customAnnotation = new NormalAnnotationExpr();
        customAnnotation.setName("org.teavm.jso.JSFunctor");
        interfaceDecl.addAnnotation(customAnnotation);

        MethodDeclaration method = interfaceDecl.addMethod(methodName);
        method.removeBody();
        method.setType(returnTypeStr);
        for(int i1 = 0; i1 < parameters.size(); i1++) {
            Parameter parameter = parameters.get(i1);
            Type type = parameter.getType();
            String typeStr = type.asString();
            String paramName = parameter.getNameAsString();
            if(typeStr.equals("String") || JParserHelper.isLong(type)) {
                typeStr = "int";
            }
            method.addParameter(typeStr, paramName);
        }
        classDeclaration.addMember(interfaceDecl);
    }

    private void createInterfaceInstance(JParser jParser, CompilationUnit unit, String methodName, String internalMethodName, String returnTypeStr, NodeList<Parameter> parameters, BlockStmt callbackMethodBody) {
        ObjectCreationExpr anonymousClass = new ObjectCreationExpr();
        anonymousClass.setType(new ClassOrInterfaceType().setName(methodName));

        ClassOrInterfaceDeclaration anonymousBody = new ClassOrInterfaceDeclaration();
        anonymousBody.setInterface(false); // This is a class, not an interface

        // Implement onEvent method
        MethodDeclaration implMethod1 = anonymousBody.addMethod(methodName, Modifier.Keyword.PUBLIC);
        implMethod1.setType(returnTypeStr);
        String params = "";
        int paramSize = parameters.size();
        for(int i1 = 0; i1 < paramSize; i1++) {
            Parameter parameter = parameters.get(i1);
            Type type = parameter.getType();
            String typeStr = type.asString();
            String paramNameOriginal = parameter.getNameAsString();
            String paramName = paramNameOriginal;

            if(typeStr.equals("String"))  {
                // Edge case where String need to be converted
                paramName = "IDLUtils.getJSString(" + paramName + ")";
                typeStr = "int";
                JParserHelper.addMissingImportType(jParser, unit, "IDLUtils");
            }
            if(JParserHelper.isLong(type)) {
                typeStr = "int";
            }
            params += paramName;
            if(i1 < paramSize-1) {
                params += ", ";
            }
            implMethod1.addParameter(typeStr, paramNameOriginal);
        }
        BlockStmt body1 = new BlockStmt();
        String methodCall = internalMethodName + "(" + params + ");";
        String returnCode = "";
        if(!returnTypeStr.equals("void")) {
            returnCode = "return ";
        }
        body1.addStatement(returnCode + methodCall);
        implMethod1.setBody(body1);

        anonymousClass.setAnonymousClassBody(anonymousBody.getMembers());

        VariableDeclarationExpr varDecl = new VariableDeclarationExpr(
                new VariableDeclarator(
                        new ClassOrInterfaceType().setName(methodName),
                        methodName,
                        anonymousClass
                )
        );
        callbackMethodBody.addStatement(new ExpressionStmt(varDecl));
    }

    @Override
    public void onParserComplete(JParser jParser, ArrayList<JParserItem> parserItems) {
        super.onParserComplete(jParser, parserItems);
        String prefix = "";

        String packagePrefixPath = PACKAGE_PREFIX.replace(".", File.separator);
        // Rename all classes to a prefix
        for(int i = 0; i < parserItems.size(); i++) {
            JParserItem parserItem = parserItems.get(i);
            String className = parserItem.className;
            ClassOrInterfaceDeclaration classDeclaration = parserItem.getClassDeclaration();
            EnumDeclaration enumDeclaration = parserItem.getEnumDeclaration();
            String newName = prefix + className;
            parserItem.packagePathName = packagePrefixPath + parserItem.packagePathName;
            parserItem.className = newName;

            if(classDeclaration != null) {
                classDeclaration.setName(newName);
            }
            if(enumDeclaration != null) {
                enumDeclaration.setName(newName);
            }
        }

        // Rename all class inside class names.
        for(int i = 0; i < parserItems.size(); i++) {
            JParserItem parserItem = parserItems.get(i);
            CompilationUnit unit = parserItem.unit;
            for(ImportDeclaration anImport : unit.getImports()) {
                Name name = anImport.getName();
                String importPath = "";
                Optional<Name> qualifier = name.getQualifier();
                if(qualifier.isPresent()) {
                    importPath = qualifier.get().asString();
                }
                String identifier = name.getIdentifier();

                boolean skipUnit = false;
                if(!skipUnit) {
                    JParserItem parserUnitItem = jParser.getParserUnitItem(prefix + identifier);
                    if(parserUnitItem != null) {
                        String newImport = PACKAGE_PREFIX + importPath + ".";
                        anImport.setName(newImport + prefix + identifier);
                    }
                    else {
                        IDLClassOrEnum classOrEnum = idlReader.getClassOrEnum(identifier);
                        if(classOrEnum != null) {
                            //Contains dependency. It needs to have gen imports
                            if(!importPath.startsWith(PACKAGE_PREFIX)) {
                                String newImport = PACKAGE_PREFIX + importPath + "." + identifier;
                                anImport.setName(newImport);
                            }
                        }
                    }
                }
            }

            String newPackage = getUpdatePackage(unit);
            unit.setPackageDeclaration(PACKAGE_PREFIX + newPackage);

            for(ConstructorDeclaration constructorDeclaration : unit.findAll(ConstructorDeclaration.class)) {
                String nameAsString = constructorDeclaration.getNameAsString();
                JParserItem parserUnitItem = jParser.getParserUnitItem(prefix + nameAsString);
                if(parserUnitItem != null) {
                    constructorDeclaration.setName(prefix + nameAsString);
                }
            }
            List<Type> allTypes = unit.findAll(Type.class);
            for(Type type : allTypes) {
                if(type.isClassOrInterfaceType()) {
                    ClassOrInterfaceType classOrInterfaceType = type.asClassOrInterfaceType();
                    String nameAsString = classOrInterfaceType.getNameAsString();
                    JParserItem parserUnitItem = jParser.getParserUnitItem(prefix + nameAsString);
                    if(parserUnitItem != null) {
                        classOrInterfaceType.setName(prefix + nameAsString);
                    }
                }
            }
            List<NameExpr> nameExprs = unit.findAll(NameExpr.class);
            for(NameExpr nameExpr : nameExprs) {
                String name = nameExpr.getNameAsString();
                JParserItem parserUnitItem = jParser.getParserUnitItem(prefix + name);
                if(parserUnitItem != null) {
                    nameExpr.setName(prefix + name);
                }
            }

            //cast native_address to int

            if(CAST_LONG_TO_INT) {
                castLongToIntIfNeeded(unit);
            }
        }
    }

    @Override
    protected boolean parseCodeBlock(Node node, String headerCommands, String content) {
        // Replace custom code that contains module tag
        String newContent = content.replace(TEMPLATE_TAG_MODULE, module);
        return super.parseCodeBlock(node, headerCommands, newContent);
    }

    private void castLongToIntIfNeeded(CompilationUnit unit) {
        List<MethodDeclaration> allMethods = unit.findAll(MethodDeclaration.class);
        for(MethodDeclaration method : allMethods) {
            if(method.getBody().isPresent()) {
                NodeList<Parameter> parameters = method.getParameters();
                for(Parameter parameter : parameters) {
                    Type type = parameter.getType();
                    if(type.isPrimitiveType()) {
                        PrimitiveType primitiveType = type.asPrimitiveType();
                        if(primitiveType.getType() != PrimitiveType.Primitive.LONG) {
                            continue;
                        }
                        String paramName = parameter.getNameAsString();
                        if(paramName.endsWith("_addr")) {
                            parameter.setType(PrimitiveType.intType());
                        }
                    }
                }

                BlockStmt body = method.getBody().get();
                Type returnType = method.getType();
                boolean classOrInterfaceType = returnType.isClassOrInterfaceType() && !returnType.asString().equals("String");
                // Find all method calls in the body
                List<MethodCallExpr> methodCalls = body.findAll(MethodCallExpr.class);
                for(MethodCallExpr call : methodCalls) {
                    SimpleName methodName = call.getName();
                    // Find the corresponding method declaration
                    ClassOrInterfaceDeclaration classDecl = unit.findFirst(ClassOrInterfaceDeclaration.class).get();
                    List<MethodDeclaration> candidates = classDecl.getMethodsByName(methodName.getIdentifier());
                    for(MethodDeclaration candidate : candidates) {
                        if(candidate.isNative()) {
                            NodeList<Parameter> nativeParameters = candidate.getParameters();
                            for(Parameter parameter : nativeParameters) {
                                Type type = parameter.getType();
                                String paramName = parameter.getNameAsString();
                                if(type.isPrimitiveType()) {
                                    PrimitiveType primitiveType = type.asPrimitiveType();
                                    if(primitiveType.getType() != PrimitiveType.Primitive.LONG) {
                                        continue;
                                    }
                                    if(paramName.endsWith("_addr")) {
                                        parameter.setType(PrimitiveType.intType());
                                    }
                                }
                            }
                            if(classOrInterfaceType) {
                                candidate.setType(PrimitiveType.intType());
                                Optional<Node> parent = call.getParentNode();
                                while(parent.isPresent()) {
                                    Node p = parent.get();
                                    if(p instanceof VariableDeclarationExpr) {
                                        VariableDeclarationExpr varDecl = (VariableDeclarationExpr) p;
                                        List<VariableDeclarator> vars = varDecl.getVariables();
                                        for(VariableDeclarator var : vars) {
                                            if(var.getInitializer().isPresent() && var.getInitializer().get() == call) {
                                                var.setType(PrimitiveType.intType());
                                            }
                                        }
                                        break;
                                    }
                                    else if(p instanceof AssignExpr) {
                                        AssignExpr assign = (AssignExpr) p;
                                        if(assign.getValue() == call && assign.getTarget() instanceof NameExpr) {
                                            NameExpr nameExpr = (NameExpr) assign.getTarget();
                                            List<VariableDeclarationExpr> varDecls = body.findAll(VariableDeclarationExpr.class);
                                            for(VariableDeclarationExpr varDecl : varDecls) {
                                                for(VariableDeclarator var : varDecl.getVariables()) {
                                                    if(var.getNameAsString().equals(nameExpr.getNameAsString())) {
                                                        var.setType(PrimitiveType.intType());
                                                    }
                                                }
                                            }
                                        }
                                        break;
                                    }
                                    parent = p.getParentNode();
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        // For constructors, change long addr to int when assigned from native methods
        List<ConstructorDeclaration> constructors = unit.findAll(ConstructorDeclaration.class);
        for(ConstructorDeclaration constructor : constructors) {
            BlockStmt body = constructor.getBody();
            // Find all method calls in the constructor body
            List<MethodCallExpr> methodCalls = body.findAll(MethodCallExpr.class);
            for(MethodCallExpr call : methodCalls) {
                SimpleName methodName = call.getName();
                // Find the corresponding method declaration
                ClassOrInterfaceDeclaration classDecl = unit.findFirst(ClassOrInterfaceDeclaration.class).get();
                List<MethodDeclaration> candidates = classDecl.getMethodsByName(methodName.getIdentifier());
                for(MethodDeclaration candidate : candidates) {
                    if(candidate.isNative()) {
                        candidate.setType(PrimitiveType.intType());
                        // Check if the call is assigned to a variable and change the variable type to int
                        Optional<Node> parent = call.getParentNode();
                        while(parent.isPresent()) {
                            Node p = parent.get();
                            if(p instanceof VariableDeclarationExpr) {
                                VariableDeclarationExpr varDecl = (VariableDeclarationExpr) p;
                                List<VariableDeclarator> vars = varDecl.getVariables();
                                for(VariableDeclarator var : vars) {
                                    if(var.getInitializer().isPresent() && var.getInitializer().get() == call) {
                                        var.setType(PrimitiveType.intType());
                                    }
                                }
                                break;
                            }
                            parent = p.getParentNode();
                        }
                        break;
                    }
                }
            }
        }
    }
}
