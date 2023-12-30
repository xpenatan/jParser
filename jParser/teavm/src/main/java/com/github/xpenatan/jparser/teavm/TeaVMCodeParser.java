package com.github.xpenatan.jparser.teavm;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserHelper;
import com.github.xpenatan.jparser.core.JParserItem;
import com.github.xpenatan.jparser.idl.IDLAttribute;
import com.github.xpenatan.jparser.idl.IDLConstructor;
import com.github.xpenatan.jparser.idl.IDLEnum;
import com.github.xpenatan.jparser.idl.IDLFile;
import com.github.xpenatan.jparser.idl.IDLMethod;
import com.github.xpenatan.jparser.idl.IDLParameter;
import com.github.xpenatan.jparser.idl.parser.IDLAttributeOperation;
import com.github.xpenatan.jparser.idl.parser.IDLDefaultCodeParser;
import com.github.xpenatan.jparser.idl.IDLReader;
import com.github.xpenatan.jparser.idl.parser.IDLMethodOperation;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeaVMCodeParser extends IDLDefaultCodeParser {

    private static final String HEADER_CMD = "teaVM";

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

    protected static final String ATTRIBUTE_GET_PRIMITIVE_STATIC_TEMPLATE =
            "return [MODULE].[TYPE].prototype.get_[ATTRIBUTE]()";

    protected static final String ATTRIBUTE_GET_OBJECT_POINTER_STATIC_TEMPLATE =
            "var returnedJSObj = [MODULE].[TYPE].prototype.get_[ATTRIBUTE]();\n" +
            "if(!returnedJSObj.hasOwnProperty('ptr')) return 0; \n" +
            "return [MODULE].getPointer(returnedJSObj);";

    protected static final String ATTRIBUTE_GET_OBJECT_POINTER_TEMPLATE =
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "var returnedJSObj = jsObj.get_[ATTRIBUTE]();\n" +
            "if(!returnedJSObj.hasOwnProperty('ptr')) return 0; \n" +
            "return [MODULE].getPointer(returnedJSObj);";

    protected static final String ATTRIBUTE_SET_OBJECT_POINTER_TEMPLATE =
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "jsObj.set_[ATTRIBUTE]([ATTRIBUTE]);";

    protected static final String ATTRIBUTE_SET_OBJECT_POINTER_STATIC_TEMPLATE =
            "[MODULE].[TYPE].prototype.set_[ATTRIBUTE]([ATTRIBUTE]);\n";

    protected static final String ATTRIBUTE_SET_OBJECT_VALUE_TEMPLATE =
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "jsObj.set_[ATTRIBUTE]([ATTRIBUTE]);";

    protected static final String ATTRIBUTE_SET_OBJECT_VALUE_STATIC_TEMPLATE =
            "[MODULE].[TYPE].prototype.set_[ATTRIBUTE]([ATTRIBUTE]);\n";

    protected static final String ATTRIBUTE_SET_PRIMITIVE_TEMPLATE =
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "jsObj.set_[ATTRIBUTE]([ATTRIBUTE]);";

    protected static final String ATTRIBUTE_SET_PRIMITIVE_STATIC_TEMPLATE =
            "[MODULE].[TYPE].prototype.set_[ATTRIBUTE]([ATTRIBUTE]);\n";

    protected static final String ATTRIBUTE_GET_OBJECT_VALUE_STATIC_TEMPLATE = ATTRIBUTE_GET_OBJECT_POINTER_STATIC_TEMPLATE;

    protected static final String ATTRIBUTE_GET_OBJECT_VALUE_TEMPLATE = ATTRIBUTE_GET_OBJECT_POINTER_TEMPLATE;

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
        convertNativeMethodLongToInt(nativeMethodDeclaration);
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

        convertJavaPrimitiveArrayToJavaScriptReferenceArray(parameters);

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
        convertLongToInt(constructorDeclaration.getBody(), nativeMethodDeclaration);

        String param = "";

        String className = classDeclaration.getNameAsString();
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
    public void onIDLMethodGenerated(JParser jParser, IDLMethod idlMethod, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethod) {
        // IDL parser generate our empty methods with default return values.
        // We now modify it to match teavm native calls

        convertLongToInt(methodDeclaration.getBody().get(), nativeMethod);
        String returnType = idlMethod.returnType;
        String methodName = methodDeclaration.getNameAsString();
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
            String paramName = getParam(idlParameter.idlFile, isObject, idlParameter.name, idlParameter.type, idlParameter.isRef, idlParameter.isValue);
            if(i > 0) {
                param += ", ";
            }
            param += paramName;
        }
        return param;
    }

    private static String getParam(IDLFile idlFile, boolean isObject, String paramName, String classType, boolean isRef, boolean isValue) {
        if(isObject && !classType.equals("String")) {
            paramName += "_addr";
        }
        return paramName;
    }

    @Override
    public void onIDLAttributeGenerated(JParser jParser, IDLAttribute idlAttribute, boolean isSet, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethod) {
        convertLongToInt(methodDeclaration.getBody().get(), nativeMethod);

        String returnTypeName = classDeclaration.getNameAsString();
        String attributeName = idlAttribute.name;
        String returnType = idlAttribute.type;

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
            case SET_OBJECT_VALUE_STATIC:
                content = ATTRIBUTE_SET_OBJECT_VALUE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_OBJECT_VALUE:
                content = ATTRIBUTE_GET_OBJECT_VALUE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName)
                        .replace(TEMPLATE_TAG_TYPE, returnTypeName)
                        .replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_OBJECT_VALUE_STATIC:
                content = ATTRIBUTE_GET_OBJECT_VALUE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName)
                        .replace(TEMPLATE_TAG_TYPE, returnTypeName)
                        .replace(TEMPLATE_TAG_MODULE, module);
                break;
            case SET_OBJECT_POINTER:
                content = ATTRIBUTE_SET_OBJECT_POINTER_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case SET_OBJECT_POINTER_STATIC:
                content = ATTRIBUTE_SET_OBJECT_POINTER_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_OBJECT_POINTER:
                content = ATTRIBUTE_GET_OBJECT_POINTER_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_OBJECT_POINTER_STATIC:
                content = ATTRIBUTE_GET_OBJECT_POINTER_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case SET_PRIMITIVE:
                content = ATTRIBUTE_SET_PRIMITIVE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case SET_PRIMITIVE_STATIC:
                content = ATTRIBUTE_SET_PRIMITIVE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_PRIMITIVE:
                content = ATTRIBUTE_GET_PRIMITIVE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
                break;
            case GET_PRIMITIVE_STATIC:
                content = ATTRIBUTE_GET_PRIMITIVE_STATIC_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
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
    public void onIDLEnumMethodGenerated(JParser jParser, IDLEnum idlEnum, ClassOrInterfaceDeclaration classDeclaration, String enumStr, FieldDeclaration fieldDeclaration, MethodDeclaration nativeMethodDeclaration) {
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
                if(leftName.equals(idlEnum.name)) {
                    // enum class cannot have child class name
                    enumStr = rightName;
                }
                else {
                    enumStr = enumStr.replace("::", ".");
                }
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

    private static void convertLongToInt(BlockStmt blockStmt, MethodDeclaration nativeMethod) {
        //Convert native method params that contains long to int when calling native methods.

        convertNativeMethodLongToInt(nativeMethod);
        List<MethodCallExpr> all = blockStmt.findAll(MethodCallExpr.class);
        convertCallerLongToInt(all);
    }

    public static void convertCallerLongToInt(List<MethodCallExpr> all) {
        for(MethodCallExpr methodCallExpr : all) {
            NodeList<Expression> arguments = methodCallExpr.getArguments();
            String methodNameStr = methodCallExpr.getNameAsString();
            if(methodNameStr.equals("getCPointer") || methodNameStr.contains("getPointer")) {
                Optional<Node> parentNode = methodCallExpr.getParentNode();
                if(parentNode.isPresent()) {
                    Node node = parentNode.get();
                    Type intType = StaticJavaParser.parseType(int.class.getSimpleName());
                    CastExpr intCast = new CastExpr(intType, methodCallExpr);
                    node.replace(methodCallExpr, intCast);
                }
            }
        }
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
            List<MethodCallExpr> all = unit.findAll(MethodCallExpr.class);
            convertCallerLongToInt(all);
        }
    }

    @Override
    protected boolean parseCodeBlock(Node node, String headerCommands, String content) {
        // Replace custom code that contains module tag
        String newContent = content.replace(TEMPLATE_TAG_MODULE, module);
        return super.parseCodeBlock(node, headerCommands, newContent);
    }
}