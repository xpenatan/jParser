package com.github.xpenatan.jparser.teavm;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.type.Type;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.idl.IDLMethod;
import com.github.xpenatan.jparser.idl.parser.IDLDefaultCodeParser;
import com.github.xpenatan.jparser.idl.IDLReader;

public class TeaVMCodeParserV2 extends IDLDefaultCodeParser {

    private static final String HEADER_CMD = "teaVM";

    protected static final String TEMPLATE_TAG_TYPE = "[TYPE]";
    protected static final String TEMPLATE_TAG_METHOD = "[METHOD]";
    protected static final String TEMPLATE_TAG_MODULE = "[MODULE]";

    /**
     * When a js method returns a js object, we need get its pointer.
     */
    protected static final String GET_JS_METHOD_OBJ_POINTER_TEMPLATE = "" +
            "var jsObj = [MODULE].wrapPointer(addr, [MODULE].[TYPE]);\n" +
            "var returnedJSObj = jsObj.[METHOD];\n" +
            "return [MODULE].getPointer(returnedJSObj);";

    protected static final String GET_JS_METHOD_PRIMITIVE_TEMPLATE = "" +
            "var jsObj = [MODULE].wrapPointer(addr, [MODULE].[TYPE]);\n" +
            "var returnedJSObj = jsObj.[METHOD];\n" +
            "return returnedJSObj;";

    protected static final String GET_JS_METHOD_VOID_TEMPLATE = "" +
            "var jsObj = [MODULE].wrapPointer(addr, [MODULE].[TYPE]);\n" +
            "jsObj.[METHOD];";

    private final String module;

    public TeaVMCodeParserV2(IDLReader idlReader, String module, String basePackage) {
        super(basePackage, HEADER_CMD, idlReader);
        this.module = module;
        generateClass = true;
    }

    @Override
    public void onIDLMethodGenerated(JParser jParser, IDLMethod idlMethod, ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethod) {
        // IDL parser generate our empty methods with default return values.
        // We now modify it to match teavm native calls

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

}