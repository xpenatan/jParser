package com.github.xpenatan.jparser.teavm;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
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
import com.github.xpenatan.jparser.idl.IDLMethod;
import com.github.xpenatan.jparser.idl.parser.IDLDefaultCodeParser;
import com.github.xpenatan.jparser.idl.IDLReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeaVMCodeParserV2 extends IDLDefaultCodeParser {

    private static final String HEADER_CMD = "teaVM";

    protected static final String TEMPLATE_TAG_TYPE = "[TYPE]";
    protected static final String TEMPLATE_TAG_METHOD = "[METHOD]";
    protected static final String TEMPLATE_TAG_ATTRIBUTE = "[ATTRIBUTE]";
    protected static final String TEMPLATE_TAG_MODULE = "[MODULE]";

    /**
     * When a js method returns a js object, we need get its pointer.
     */
    protected static final String GET_JS_METHOD_OBJ_POINTER_TEMPLATE = "" +
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "var returnedJSObj = jsObj.[METHOD];\n" +
            "return [MODULE].getPointer(returnedJSObj);";

    protected static final String GET_JS_METHOD_PRIMITIVE_TEMPLATE = "" +
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "var returnedJSObj = jsObj.[METHOD];\n" +
            "return returnedJSObj;";

    protected static final String GET_JS_METHOD_VOID_TEMPLATE = "" +
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "jsObj.[METHOD];";

    protected static final String GET_ATTRIBUTE_PRIMITIVE_TEMPLATE = "" +
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "return jsObj.get_[ATTRIBUTE]();";

    protected static final String GET_ATTRIBUTE_OBJ_POINTER_TEMPLATE = "" +
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "var returnedJSObj = jsObj.get_[ATTRIBUTE]();\n" +
            "return [MODULE].getPointer(returnedJSObj);";

    protected static final String SET_ATTRIBUTE_VOID_TEMPLATE = "" +
            "var jsObj = [MODULE].wrapPointer(this_addr, [MODULE].[TYPE]);\n" +
            "jsObj.set_[ATTRIBUTE]([ATTRIBUTE]);";

    private final String module;

    public TeaVMCodeParserV2(IDLReader idlReader, String module, String basePackage) {
        super(basePackage, HEADER_CMD, idlReader);
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
    public void onIDLMethodGenerated(JParser jParser, IDLMethod idlMethod, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethod) {
        // IDL parser generate our empty methods with default return values.
        // We now modify it to match teavm native calls

        convertLongToInt(methodDeclaration, nativeMethod);

        NodeList<Parameter> nativeParameters = nativeMethod.getParameters();
        String methodName = methodDeclaration.getNameAsString();
        Type returnType = methodDeclaration.getType();
        MethodCallExpr caller = new MethodCallExpr();
        caller.setName(methodName);
        String param = "";

        int size = nativeParameters.size();
        for(int i = 0; i < size; i++) {
            Parameter parameter = nativeParameters.get(i);
            String paramName = parameter.getNameAsString();
            if(i > 0) {
                caller.addArgument(paramName);
            }

            param += paramName;
            if(i < size - 1) {
                param += "\", \"";
            }
        }

        String returnTypeName = classDeclaration.getNameAsString();
        String methodCaller = caller.toString();

        String content = null;

        if(returnType.isVoidType()) {
            content = GET_JS_METHOD_VOID_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
        }
        else if(returnType.isClassOrInterfaceType()) {
            content = GET_JS_METHOD_OBJ_POINTER_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
        }
        else {
            content = GET_JS_METHOD_PRIMITIVE_TEMPLATE.replace(TEMPLATE_TAG_METHOD, methodCaller).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
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
    public void onIDLAttributeGenerated(JParser jParser, IDLAttribute idlAttribute, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethodDeclaration) {
        convertLongToInt(methodDeclaration, nativeMethodDeclaration);

        String returnTypeName = classDeclaration.getNameAsString();
        String attributeName = idlAttribute.name;
        Type returnType = methodDeclaration.getType();

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

        String content = null;
        if(returnType.isVoidType()) {
            content = SET_ATTRIBUTE_VOID_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
        }
        else if(returnType.isClassOrInterfaceType()) {
            content = GET_ATTRIBUTE_OBJ_POINTER_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
        }
        else {
            content = GET_ATTRIBUTE_PRIMITIVE_TEMPLATE.replace(TEMPLATE_TAG_ATTRIBUTE, attributeName).replace(TEMPLATE_TAG_TYPE, returnTypeName).replace(TEMPLATE_TAG_MODULE, module);
        }

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

    private static void convertLongToInt(MethodDeclaration methodDeclaration, MethodDeclaration nativeMethod) {
        //Convert native method params that contains long to int when calling native methods.

        convertNativeMethodLongToInt(nativeMethod);

        BlockStmt blockStmt = methodDeclaration.getBody().get();
        List<MethodCallExpr> all = blockStmt.findAll(MethodCallExpr.class);
        convertCallerLongToInt(all);
    }

    public static void convertCallerLongToInt(List<MethodCallExpr> all) {
        for(MethodCallExpr methodCallExpr : all) {
            NodeList<Expression> arguments = methodCallExpr.getArguments();
            for(int i = 0; i < arguments.size(); i++) {
                Expression argument = arguments.get(i);
                if(argument.isMethodCallExpr()) {
                    MethodCallExpr methodCall = argument.asMethodCallExpr();
                    String methodName = methodCall.getNameAsString();

                    if(methodName.contains("getCPointer")) {
                        Type intType = StaticJavaParser.parseType(int.class.getSimpleName());
                        CastExpr intCast = new CastExpr(intType, argument);
                        methodCallExpr.setArgument(i, intCast);
                    }
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
            classDeclaration.setName(newName);
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
                JParserItem parserUnitItem = jParser.getParserUnitItem(prefix + identifier);
                if(parserUnitItem != null) {
                    String newImport = packagePrefix + importPath + ".";
                    anImport.setName(newImport + prefix + identifier);
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
}