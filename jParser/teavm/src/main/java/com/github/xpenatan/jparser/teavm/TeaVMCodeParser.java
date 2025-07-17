package com.github.xpenatan.jparser.teavm;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.types.ResolvedArrayType;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.utils.Pair;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserHelper;
import com.github.xpenatan.jparser.core.JParserItem;
import com.github.xpenatan.jparser.idl.IDLAttribute;
import com.github.xpenatan.jparser.idl.IDLClass;
import com.github.xpenatan.jparser.idl.IDLConstructor;
import com.github.xpenatan.jparser.idl.IDLEnum;
import com.github.xpenatan.jparser.idl.IDLEnumItem;
import com.github.xpenatan.jparser.idl.IDLFile;
import com.github.xpenatan.jparser.idl.IDLMethod;
import com.github.xpenatan.jparser.idl.IDLParameter;
import com.github.xpenatan.jparser.idl.IDLReader;
import com.github.xpenatan.jparser.idl.parser.IDLAttributeOperation;
import com.github.xpenatan.jparser.idl.parser.IDLDefaultCodeParser;
import com.github.xpenatan.jparser.idl.parser.IDLMethodOperation;
import com.github.xpenatan.jparser.idl.parser.IDLMethodParser;
import com.github.xpenatan.jparser.idl.parser.data.IDLParameterData;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeaVMCodeParser extends IDLDefaultCodeParser {

    public static boolean CAST_LONG_TO_INT = true;
    public static boolean USE_REF_ARRAY = false; // Not supported in WASM mode

    private static final String HEADER_CMD = "TEAVM";

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
            paramName += "_addr";
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
    public void onIDLEnumMethodGenerated(JParser jParser, IDLEnum idlEnum, EnumDeclaration enumDeclaration, IDLEnumItem enumItem, MethodDeclaration nativeMethodDeclaration) {
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

            caller.addArgument("getNativeData().getCPointer()");

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
                createInterfaceInstance(methodName, internalMethodName, returnTypeStr, parameters, callbackMethodBody);
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

    private void createInterfaceInstance(String methodName, String internalMethodName, String returnTypeStr, NodeList<Parameter> parameters, BlockStmt callbackMethodBody) {
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
                paramName = "IDLBase.getJSString(" + paramName + ")";
                typeStr = "int";
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
        String packagePrefix = "gen.";

        String packagePrefixPath = packagePrefix.replace(".", File.separator);
        // Rename all classes to a prefix
        for(int i = 0; i < parserItems.size(); i++) {
            JParserItem parserItem = parserItems.get(i);
            String className = parserItem.className;
            ClassOrInterfaceDeclaration classDeclaration = parserItem.getClassDeclaration();
            String newName = prefix + className;
            parserItem.packagePathName = packagePrefixPath + parserItem.packagePathName;
            parserItem.className = newName;

            if(classDeclaration != null) {
                classDeclaration.setName(newName);
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
                if(!JParser.CREATE_IDL_HELPER) {
                    //TODO implement better class renaming
                    // Hack to skip the generated lib and use the main one
                    ArrayList<String> baseIDLClasses = getBaseIDLClasses();
                    for(String baseIDLClass : baseIDLClasses) {
                        String[] split = baseIDLClass.split("\\.");
                        String s = split[split.length - 1];
                        if(s.equals(identifier)) {
                            skipUnit = true;
                            break;
                        }
                    }
                }
                if(!skipUnit) {
                    JParserItem parserUnitItem = jParser.getParserUnitItem(prefix + identifier);
                    if(parserUnitItem != null) {
                        String newImport = packagePrefix + importPath + ".";
                        anImport.setName(newImport + prefix + identifier);
                    }
                }
            }

            PackageDeclaration packageDeclaration = unit.getPackageDeclaration().get();
            String nameAsString1 = packageDeclaration.getNameAsString();
            packageDeclaration.setName(packagePrefix + nameAsString1);

            for(ConstructorDeclaration constructorDeclaration : unit.findAll(ConstructorDeclaration.class)) {
                String nameAsString = constructorDeclaration.getNameAsString();
                JParserItem parserUnitItem = jParser.getParserUnitItem(prefix + nameAsString);
                if(parserUnitItem != null) {
                    constructorDeclaration.setName(prefix + nameAsString);
                }
            }
            for(Type type : unit.findAll(Type.class)) {
                if(type.isClassOrInterfaceType()) {
                    ClassOrInterfaceType classOrInterfaceType = type.asClassOrInterfaceType();
                    String nameAsString = classOrInterfaceType.getNameAsString();
                    JParserItem parserUnitItem = jParser.getParserUnitItem(prefix + nameAsString);
                    if(parserUnitItem != null) {
                        classOrInterfaceType.setName(prefix + nameAsString);
                    }
                }
            }

            //cast cpointer to int

            if(CAST_LONG_TO_INT) {
                List<ClassOrInterfaceDeclaration> classDeclarations = unit.findAll(ClassOrInterfaceDeclaration.class);

                for(int i1 = 0; i1 < classDeclarations.size(); i1++) {
                    ClassOrInterfaceDeclaration classDeclaration = classDeclarations.get(i1);
                    convertNativeMethodLongType(classDeclaration);
                }
            }
        }
    }

    @Override
    protected boolean parseCodeBlock(Node node, String headerCommands, String content) {
        // Replace custom code that contains module tag
        String newContent = content.replace(TEMPLATE_TAG_MODULE, module);
        return super.parseCodeBlock(node, headerCommands, newContent);
    }

    private void convertNativeMethodLongType(ClassOrInterfaceDeclaration classDeclaration) {
        String className = classDeclaration.getNameAsString();

        List<ConstructorDeclaration> constructorDeclarations = classDeclaration.findAll(ConstructorDeclaration.class);
        for(int i = 0; i < constructorDeclarations.size(); i++) {
            ConstructorDeclaration constructorDeclaration = constructorDeclarations.get(i);
            List<MethodCallExpr> methodCallerExprList = constructorDeclaration.findAll(MethodCallExpr.class);
            NodeList<Parameter> constructorParameters = constructorDeclaration.getParameters();
            updateLongToInt(classDeclaration, methodCallerExprList);
        }

        List<MethodDeclaration> methodDeclarations = classDeclaration.findAll(MethodDeclaration.class);
        for(int i = 0; i < methodDeclarations.size(); i++) {
            MethodDeclaration methodDeclaration = methodDeclarations.get(i);
            if(!methodDeclaration.isNative()) {
                String methodName = methodDeclaration.getNameAsString();
                List<MethodCallExpr> methodCallerExprList = methodDeclaration.findAll(MethodCallExpr.class);
                updateLongToInt(classDeclaration, methodCallerExprList);
            }
        }
    }

    private void updateLongToInt(ClassOrInterfaceDeclaration classDeclaration, List<MethodCallExpr> methodCallerExprList) {
        for(int i1 = 0; i1 < methodCallerExprList.size(); i1++) {
            MethodCallExpr methodCallExpr = methodCallerExprList.get(i1);
            NodeList<Expression> arguments = methodCallExpr.getArguments();
            MethodDeclaration nativeMethod = getNativeMethod(classDeclaration, methodCallExpr);
            if(nativeMethod != null) {
                NodeList<Parameter> parameters = nativeMethod.getParameters();
                int paramSize = parameters.size();
                if(paramSize > 0) {
                    if(arguments.size() != paramSize) {
                        throw new RuntimeException("Arguments are not the same");
                    }
                    for(int argI = 0; argI < arguments.size(); argI++) {
                        Expression arg = arguments.get(argI);
                        boolean isLong = false; // used in public native methods
                        try {
                            ResolvedType resolvedType = arg.calculateResolvedType();
                            if(resolvedType.isPrimitive()) {
                                ResolvedPrimitiveType primitive = resolvedType.asPrimitive();
                                isLong = primitive.describe().equals("long");
                            }
                        }
                        catch(Throwable t) {
                        }
                        Parameter param = parameters.get(argI);
                        Type type = param.getType();
                        if(JParserHelper.isLong(type) || isLong) {
                            Optional<Node> parentNode = arg.getParentNode();
                            if(parentNode.isPresent()) {
                                Node node = parentNode.get();
                                Type intType = StaticJavaParser.parseType(int.class.getSimpleName());
                                CastExpr intCast;
                                if(arg instanceof ConditionalExpr) {
                                    intCast = new CastExpr(intType, new EnclosedExpr(arg));
                                }
                                else {
                                    intCast = new CastExpr(intType, arg);
                                }
                                node.replace(arg, intCast);
                            }
                        }
                    }
                }

                Node node = methodCallExpr.getParentNode().get();
                if(node instanceof VariableDeclarator) {
                    VariableDeclarator parentNode = (VariableDeclarator)node;
                    if(JParserHelper.isLong(parentNode.getType())) {
                        parentNode.setType(int.class);
                    }
                }
                convertNativeMethodLongToInt(nativeMethod);
            }
        }
    }

    private MethodDeclaration getNativeMethod(ClassOrInterfaceDeclaration classDeclaration, MethodCallExpr methodCallExpr) {
        String nativeMethodName = methodCallExpr.getNameAsString();
        NodeList<Expression> arguments = methodCallExpr.getArguments();
        ArrayList<String> paramTypes = new ArrayList<>();
        if(arguments.size() > 0) {
            for(int i = 0; i < arguments.size(); i++) {
                Expression expression = arguments.get(i);
                if(expression.isMethodCallExpr() || expression.isEnclosedExpr()) {
                    paramTypes.add("long");
                    continue;
                }
                else if(expression.isLambdaExpr()) {
                    continue;
                }
                ResolvedType resolvedType = null;
                try {
                    resolvedType = expression.calculateResolvedType();
                }
                catch(Throwable t) {
//                    t.printStackTrace();
                    continue;
                }
                String type = null;
                if(resolvedType.isPrimitive()) {
                    type = resolvedType.asPrimitive().describe();
                }
                else if(resolvedType.isReferenceType()) {
                    ResolvedReferenceType referenceType1 = resolvedType.asReferenceType();
                    String[] split = referenceType1.describe().split("\\.");
                    type = split[split.length-1];
                }
                else if(resolvedType.isArray()) {
                    ResolvedArrayType arrayType = resolvedType.asArrayType();
                    type = arrayType.describe();
                }
                paramTypes.add(type);
            }
        }
        String[] paramT = new String[paramTypes.size()];
        paramTypes.toArray(paramT);
        List<MethodDeclaration> methodsByName = getNativeMethodsByName(classDeclaration, nativeMethodName, arguments.size(), paramT);
        int size = methodsByName.size();
        if(size == 0) {
            // The current state is not possible to get all paramT correctly.
            // If method list is 0 and without passing type is 1 then use this method.
            methodsByName = getNativeMethodsByName(classDeclaration, nativeMethodName, arguments.size(), null);
            if(methodsByName.size() != 1) {
                return null;
            }
        }
        if(methodsByName.size() == 1) {
            MethodDeclaration methodDeclaration = methodsByName.get(0);

            if(methodDeclaration.isNative()) {
                return methodDeclaration;
            }
            else {
                return null;
            }
        }
        else {
            throw new RuntimeException("NEED TO IMPLEMENT");
        }
    }

    private List<MethodDeclaration> getNativeMethodsByName(ClassOrInterfaceDeclaration classDeclaration, String name, int paramSize, String... paramTypes) {
        List<MethodDeclaration> list = new ArrayList<>();
        List<MethodDeclaration> methodsByName = classDeclaration.getMethodsByName(name);
        for(int i = 0; i < methodsByName.size(); i++) {
            MethodDeclaration methodDeclaration = methodsByName.get(i);
            if(!methodDeclaration.isNative()) {
                continue;
            }

            boolean add = false;
            NodeList<Parameter> parameters = methodDeclaration.getParameters();
            add = parameters.size() == paramSize;

            if(add && paramTypes != null && paramTypes.length > 0 && paramTypes.length == paramSize) {
                add = methodDeclaration.hasParametersOfType(paramTypes);
            }

            if(add) {
                list.add(methodDeclaration);
            }
        }
        return list;
    }

    private static void convertNativeMethodLongToInt(MethodDeclaration nativeMethod) {
        if(JParserHelper.isLong(nativeMethod.getType())) {
            nativeMethod.setType(int.class);
        }
        for(Parameter parameter : nativeMethod.getParameters()) {
            if(JParserHelper.isLong(parameter.getType())) {
                parameter.setType(int.class);
            }
        }
    }
}