package com.github.xpenatan.jParser.c;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.utils.Pair;
import com.github.xpenatan.jParser.core.JParser;
import com.github.xpenatan.jParser.ffm.FFMClassData;
import com.github.xpenatan.jParser.ffm.FFMCodeParser;
import com.github.xpenatan.jParser.ffm.FFMCppGenerator;
import com.github.xpenatan.jParser.ffm.FFMNativeCodeGenerator;
import com.github.xpenatan.jParser.ffm.FFMTypeMapper;
import com.github.xpenatan.jParser.idl.IDLClass;
import com.github.xpenatan.jParser.idl.IDLConstructor;
import com.github.xpenatan.jParser.idl.IDLMethod;
import com.github.xpenatan.jParser.idl.IDLParameter;
import com.github.xpenatan.jParser.idl.IDLReader;
import java.util.ArrayList;
import java.util.List;

public class TeaVMCCodeParser extends FFMCodeParser {

    private static final String HEADER_CMD = "TEAVM_C";

    private FFMClassData symbolData;

    public TeaVMCCodeParser(FFMNativeCodeGenerator cppGenerator, IDLReader idlReader, String basePackage, String cppDir) {
        super(cppGenerator, idlReader, basePackage, cppDir, HEADER_CMD);
    }

    public void setSymbolData(FFMClassData symbolData) {
        this.symbolData = symbolData;
    }

    @Override
    protected void setJavaBodyNativeCMD(String content, MethodDeclaration methodDeclaration) {
        cppGenerator.addNativeCode(methodDeclaration, content);
        methodDeclaration.setStatic(true);
        methodDeclaration.addModifier(Modifier.Keyword.NATIVE);
        methodDeclaration.removeBody();

        if(!methodDeclaration.isAnnotationPresent("Import")) {
            NormalAnnotationExpr annotation = methodDeclaration.addAndGetAnnotation("org.teavm.interop.Import");
            annotation.addPair("name", "\"" + buildSymbolName(methodDeclaration) + "\"");
        }
    }

    @Override
    protected boolean shouldInjectFFMHandles() {
        return false;
    }

    @Override
    public void onIDLConstructorGenerated(JParser jParser, IDLConstructor idlConstructor, com.github.javaparser.ast.body.ClassOrInterfaceDeclaration classDeclaration, ConstructorDeclaration constructorDeclaration, MethodDeclaration nativeMethodDeclaration) {
        super.onIDLConstructorGenerated(jParser, idlConstructor, classDeclaration, constructorDeclaration, nativeMethodDeclaration);
    }

    @Override
    public void onIDLDeConstructorGenerated(JParser jParser, IDLClass idlClass, com.github.javaparser.ast.body.ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration nativeMethodDeclaration) {
        super.onIDLDeConstructorGenerated(jParser, idlClass, classDeclaration, nativeMethodDeclaration);
    }

    @Override
    public void onIDLMethodGenerated(JParser jParser, IDLMethod idlMethod, com.github.javaparser.ast.body.ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration methodDeclaration, MethodDeclaration nativeMethod) {
        super.onIDLMethodGenerated(jParser, idlMethod, classDeclaration, methodDeclaration, nativeMethod);
    }

    @Override
    public void onIDLCallbackGenerated(JParser jParser, IDLClass idlClass, com.github.javaparser.ast.body.ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration callbackDeclaration, ArrayList<Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>>> methods) {
        IDLClass callback = idlClass.callbackImpl;
        if(callback == null) {
            return;
        }

        String callbackClassName = classDeclaration.getNameAsString();
        String setupImportName = "teavmc_" + callbackClassName + "_setupCallback";
        ensureCallbackRegistryMembers(classDeclaration, callbackClassName);
        boolean hasCStringCallbackParameter = false;

        StringBuilder cppClass = new StringBuilder();
        for(Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>> pair : methods) {
            cppClass.append(buildTeaVMCFPTypedef(callback.name, pair.a, pair.b.a)).append("\n");
            hasCStringCallbackParameter |= hasStringParameter(pair.b.a);
        }
        cppClass.append("\n");

        cppClass.append("class ").append(callback.getCPPName()).append(" : public ").append(idlClass.getCPPName()).append(" {\n");
        cppClass.append("private:\n");
        cppClass.append("\tint32_t teavmc_callback_id = -1;\n");
        for(Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>> pair : methods) {
            IDLMethod idlMethod = pair.a;
            MethodDeclaration internalMethod = pair.b.a;
            String fpTypeName = buildTeaVMCFPTypeName(callback.name, idlMethod, internalMethod);
            cppClass.append("\t").append(fpTypeName).append(" ").append(idlMethod.getCPPName()).append("_ptr = nullptr;\n");
        }
        cppClass.append("public:\n");
        cppClass.append("\tvoid ").append(callbackDeclaration.getNameAsString()).append("(int32_t callback_id");
        for(Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>> pair : methods) {
            IDLMethod idlMethod = pair.a;
            MethodDeclaration internalMethod = pair.b.a;
            cppClass.append(", ").append(buildTeaVMCFPTypeName(callback.name, idlMethod, internalMethod)).append(" ").append(idlMethod.getCPPName());
        }
        cppClass.append(") {\n");
        cppClass.append("\t\tthis->teavmc_callback_id = callback_id;\n");
        for(Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>> pair : methods) {
            IDLMethod idlMethod = pair.a;
            cppClass.append("\t\tthis->").append(idlMethod.getCPPName()).append("_ptr = ").append(idlMethod.getCPPName()).append(";\n");
        }
        cppClass.append("\t}\n");

        for(Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>> pair : methods) {
            IDLMethod idlMethod = pair.a;
            MethodDeclaration publicMethod = pair.b.b;

            String returnType = mapCppType(idlMethod.getCPPReturnType());
            String methodName = idlMethod.getCPPName();
            String constStr = idlMethod.isReturnConst ? " const" : "";
            StringBuilder methodParams = new StringBuilder();
            StringBuilder callParams = new StringBuilder("teavmc_callback_id");
            NodeList<Parameter> publicParameters = publicMethod.getParameters();

            for(int i = 0; i < idlMethod.parameters.size(); i++) {
                IDLParameter idlParameter = idlMethod.parameters.get(i);
                Parameter parameter = publicParameters.get(i);
                boolean isPrimitive = parameter.getType().isPrimitiveType() || idlParameter.isAny;
                boolean isString = idlParameter.idlType.equals("DOMString");
                String tag = " ";
                String paramType = mapCppType(idlParameter.getCPPType());

                if(!isString) {
                    if(idlParameter.isRef) {
                        tag = "& ";
                    }
                    else if(!idlParameter.isEnum() && !isPrimitive && !idlParameter.isValue) {
                        tag = "* ";
                    }
                }

                if(idlParameter.isConst) {
                    paramType = "const " + paramType;
                }

                String callParam = idlParameter.name;
                if(idlParameter.isRef) {
                    callParam = "(int64_t)&" + callParam;
                }
                else if(isString) {
                    callParam = "(void*)" + callParam;
                }
                else if(idlParameter.isAny || (!idlParameter.isEnum() && !isPrimitive && !idlParameter.isValue && !isString)) {
                    callParam = "(int64_t)" + callParam;
                }
                else if(idlParameter.isEnum()) {
                    callParam = "static_cast<int32_t>(" + callParam + ")";
                }

                if(i > 0) {
                    methodParams.append(", ");
                }
                methodParams.append(paramType).append(tag).append(idlParameter.name);
                callParams.append(", ").append(callParam);
            }

            cppClass.append("\tvirtual ").append(returnType).append(" ").append(methodName)
                    .append("(").append(methodParams).append(")").append(constStr).append(" {\n");
            if(!publicMethod.getType().isVoidType()) {
                cppClass.append("\t\tif(").append(methodName).append("_ptr == nullptr) return ").append(defaultCppReturnValue(returnType)).append(";\n");
                cppClass.append("\t\treturn (").append(returnType).append(")").append(methodName).append("_ptr(").append(callParams).append(");\n");
            }
            else {
                cppClass.append("\t\tif(").append(methodName).append("_ptr != nullptr) ").append(methodName).append("_ptr(").append(callParams).append(");\n");
            }
            cppClass.append("\t}\n");
        }

        cppClass.append("};\n");
        cppClass.append("\nextern \"C\" {\n");
        cppClass.append("TEAVMC_EXPORT void ").append(setupImportName).append("(int64_t this_addr, int32_t callback_id");
        for(Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>> pair : methods) {
            IDLMethod idlMethod = pair.a;
            MethodDeclaration internalMethod = pair.b.a;
            cppClass.append(", ").append(buildTeaVMCFPTypeName(callback.name, idlMethod, internalMethod)).append(" ").append(idlMethod.getCPPName()).append("_fp");
        }
        cppClass.append(") {\n");
        cppClass.append("\t").append(callback.getCPPName()).append("* nativeObject = (").append(callback.getCPPName()).append("*)this_addr;\n");
        cppClass.append("\tnativeObject->").append(callbackDeclaration.getNameAsString()).append("(callback_id");
        for(Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>> pair : methods) {
            cppClass.append(", ").append(pair.a.getCPPName()).append("_fp");
        }
        cppClass.append(");\n");
        cppClass.append("}\n");
        cppClass.append("}\n");
        cppGenerator.addNativeCode(classDeclaration, cppClass.toString());

        if(hasCStringCallbackParameter) {
            ensureCStringHelper(classDeclaration);
        }
        ensureCallbackSetupMethod(classDeclaration, callbackDeclaration, methods, setupImportName);
    }

    private String buildSymbolName(MethodDeclaration methodDeclaration) {
        TypeDeclaration<?> classOrEnum = (TypeDeclaration<?>)methodDeclaration.getParentNode().get();
        CompilationUnit compilationUnit = classOrEnum.findCompilationUnit().get();
        String packageName = compilationUnit.getPackageDeclaration().get().getNameAsString();
        String className = classOrEnum.getNameAsString();
        String methodName = methodDeclaration.getNameAsString();
        boolean overloadedMethod = isOverloadedMethod(classOrEnum, methodName);
        ArrayList<FFMCppGenerator.FFMArgument> arguments = new ArrayList<>();
        NodeList<Parameter> parameters = methodDeclaration.getParameters();
        for(Parameter parameter : parameters) {
            String[] typeTokens = parameter.getType().toString().split("\\.");
            String type = typeTokens[typeTokens.length - 1];
            arguments.add(new FFMCppGenerator.FFMArgument(
                    parameter.getNameAsString(),
                    type,
                    FFMTypeMapper.getCType(type),
                    FFMTypeMapper.getOverloadSuffix(type)));
        }
        return FFMCppGenerator.buildSymbolName(packageName, className, methodName, arguments, overloadedMethod, symbolData);
    }

    private boolean isOverloadedMethod(TypeDeclaration<?> classOrEnum, String methodName) {
        List<MethodDeclaration> methods = classOrEnum.getMethodsByName(methodName);
        return methods.size() > 1;
    }

    private String mapCppType(String type) {
        if(type.equals("boolean")) {
            return "bool";
        }
        if(type.equals("String")) {
            return "char*";
        }
        return type;
    }

    private String defaultCppReturnValue(String returnType) {
        if(returnType.equals("bool")) {
            return "false";
        }
        if(returnType.contains("*")) {
            return "nullptr";
        }
        if(returnType.equals("char*")) {
            return "nullptr";
        }
        if(returnType.equals("float") || returnType.equals("double")) {
            return "0";
        }
        if(returnType.equals("void")) {
            return "";
        }
        if(Character.isUpperCase(returnType.charAt(0))) {
            return "(" + returnType + ")0";
        }
        return "0";
    }

    private void ensureCallbackRegistryMembers(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration classDeclaration, String callbackClassName) {
        addMemberIfMissing(classDeclaration, "TEAVMC_CALLBACKS",
                "private static final java.util.ArrayList<" + callbackClassName + "> TEAVMC_CALLBACKS = new java.util.ArrayList<>();");
        addMemberIfMissing(classDeclaration, "teavmcCallbackId",
                "private int teavmcCallbackId = -1;");
        addMemberIfMissing(classDeclaration, "teavmcRegisterCallback",
                "private int teavmcRegisterCallback() {\n" +
                        "    if(teavmcCallbackId < 0) {\n" +
                        "        teavmcCallbackId = TEAVMC_CALLBACKS.size();\n" +
                        "        TEAVMC_CALLBACKS.add(this);\n" +
                        "    }\n" +
                        "    else {\n" +
                        "        TEAVMC_CALLBACKS.set(teavmcCallbackId, this);\n" +
                        "    }\n" +
                        "    return teavmcCallbackId;\n" +
                        "}");
    }

    private void ensureCallbackSetupMethod(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration classDeclaration, MethodDeclaration callbackDeclaration, ArrayList<Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>>> methods, String setupImportName) {
        String callbackClassName = classDeclaration.getNameAsString();
        StringBuilder setupCall = new StringBuilder();
        setupCall.append("{\n");
        setupCall.append("    int callbackId = teavmcRegisterCallback();\n");
        setupCall.append("    ").append(callbackDeclaration.getNameAsString()).append("(native_address, callbackId");

        StringBuilder nativeParams = new StringBuilder("long this_addr, int callbackId");
        for(Pair<IDLMethod, Pair<MethodDeclaration, MethodDeclaration>> pair : methods) {
            IDLMethod idlMethod = pair.a;
            MethodDeclaration internalMethod = pair.b.a;
            String methodName = idlMethod.getCPPName();
            String functionClassName = teaVMCFunctionClassName(methodName);
            String exportMethodName = teaVMCExportMethodName(methodName);
            addFunctionClass(classDeclaration, functionClassName, internalMethod);
            addExportMethod(classDeclaration, callbackClassName, exportMethodName, internalMethod);
            setupCall.append(", org.teavm.interop.Function.get(").append(functionClassName).append(".class, ").append(callbackClassName).append(".class, \"").append(exportMethodName).append("\")");
            nativeParams.append(", ").append(functionClassName).append(" ").append(methodName).append("_fp");
        }
        setupCall.append(");\n");
        setupCall.append("}");
        callbackDeclaration.setBody(StaticJavaParser.parseBlock(setupCall.toString()));

        String nativeMethod = "@org.teavm.interop.Import(name = \"" + setupImportName + "\")\n" +
                "private static native void " + callbackDeclaration.getNameAsString() + "(" + nativeParams + ");";
        addMemberIfMissing(classDeclaration, callbackDeclaration.getNameAsString() + "(" + nativeParams,
                nativeMethod);
    }

    private void addFunctionClass(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration classDeclaration, String functionClassName, MethodDeclaration internalMethod) {
        StringBuilder declaration = new StringBuilder();
        declaration.append("private static abstract class ").append(functionClassName).append(" extends org.teavm.interop.Function {\n");
        declaration.append("    public abstract ").append(internalMethod.getType()).append(" call(int callbackId");
        for(Parameter parameter : internalMethod.getParameters()) {
            declaration.append(", ").append(teaVMCJavaCallbackType(parameter)).append(" ").append(parameter.getNameAsString());
        }
        declaration.append(");\n");
        declaration.append("}");
        addMemberIfMissing(classDeclaration, functionClassName, declaration.toString());
    }

    private void addExportMethod(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration classDeclaration, String callbackClassName, String exportMethodName, MethodDeclaration internalMethod) {
        StringBuilder declaration = new StringBuilder();
        declaration.append("@org.teavm.interop.Export(name = \"").append(exportMethodName).append("\")\n");
        declaration.append("private static ").append(internalMethod.getType()).append(" ").append(exportMethodName).append("(int callbackId");
        StringBuilder callArgs = new StringBuilder();
        for(Parameter parameter : internalMethod.getParameters()) {
            if(callArgs.length() > 0) {
                callArgs.append(", ");
            }
            declaration.append(", ").append(teaVMCJavaCallbackType(parameter)).append(" ").append(parameter.getNameAsString());
            callArgs.append(teaVMCJavaCallbackArgument(parameter));
        }
        declaration.append(") {\n");
        if(!internalMethod.getType().isVoidType()) {
            declaration.append("    return ");
        }
        else {
            declaration.append("    ");
        }
        declaration.append("TEAVMC_CALLBACKS.get(callbackId).").append(internalMethod.getNameAsString()).append("(").append(callArgs).append(");\n");
        declaration.append("}");
        addMemberIfMissing(classDeclaration, exportMethodName, declaration.toString());
    }

    private String buildTeaVMCFPTypedef(String className, IDLMethod idlMethod, MethodDeclaration internalMethod) {
        StringBuilder sb = new StringBuilder();
        sb.append("typedef ").append(teaVMCCType(internalMethod.getType().asString())).append(" (*").append(buildTeaVMCFPTypeName(className, idlMethod, internalMethod)).append(")(int32_t");
        for(Parameter parameter : internalMethod.getParameters()) {
            sb.append(", ").append(teaVMCCallbackParamCType(parameter.getType().asString()));
        }
        sb.append(");");
        return sb.toString();
    }

    private String buildTeaVMCFPTypeName(String className, IDLMethod idlMethod, MethodDeclaration internalMethod) {
        StringBuilder suffix = new StringBuilder();
        for(Parameter parameter : internalMethod.getParameters()) {
            suffix.append(FFMTypeMapper.getOverloadSuffix(parameter.getType().asString()));
        }
        return "fp_" + className + "_" + idlMethod.getCPPName() + "_" + suffix;
    }

    private String teaVMCCType(String javaType) {
        if(javaType.equals("void")) return "void";
        if(javaType.equals("boolean")) return "int32_t";
        if(javaType.equals("byte")) return "int8_t";
        if(javaType.equals("short")) return "int32_t";
        if(javaType.equals("int")) return "int32_t";
        if(javaType.equals("long")) return "int64_t";
        if(javaType.equals("float")) return "float";
        if(javaType.equals("double")) return "double";
        if(javaType.equals("String")) return "void*";
        return "int64_t";
    }

    private String teaVMCCallbackParamCType(String javaType) {
        if(javaType.equals("String")) return "void*";
        return teaVMCCType(javaType);
    }

    private String teaVMCJavaCallbackType(Parameter parameter) {
        if(parameter.getType().asString().equals("String")) {
            return "org.teavm.interop.Address";
        }
        return parameter.getType().asString();
    }

    private String teaVMCJavaCallbackArgument(Parameter parameter) {
        String name = parameter.getNameAsString();
        if(parameter.getType().asString().equals("String")) {
            return "teavmcCStringToString(" + name + ")";
        }
        return name;
    }

    private boolean hasStringParameter(MethodDeclaration method) {
        for(Parameter parameter : method.getParameters()) {
            if(parameter.getType().asString().equals("String")) {
                return true;
            }
        }
        return false;
    }

    private void ensureCStringHelper(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration classDeclaration) {
        addMemberIfMissing(classDeclaration, "teavmcCStringToString",
                "private static String teavmcCStringToString(org.teavm.interop.Address address) {\n" +
                        "    if(address == null || address.toLong() == 0) {\n" +
                        "        return null;\n" +
                        "    }\n" +
                        "    int length = 0;\n" +
                        "    while(address.add(length).getByte() != 0) {\n" +
                        "        length++;\n" +
                        "    }\n" +
                        "    char[] chars = new char[length];\n" +
                        "    for(int i = 0; i < length; i++) {\n" +
                        "        chars[i] = (char)(address.add(i).getByte() & 0xFF);\n" +
                        "    }\n" +
                        "    return new String(chars);\n" +
                        "}");
    }

    private String teaVMCFunctionClassName(String methodName) {
        return "TEAVMC_" + methodName + "_Function";
    }

    private String teaVMCExportMethodName(String methodName) {
        return "teavmc_" + methodName;
    }

    private void addMemberIfMissing(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration classDeclaration, String marker, String declaration) {
        for(BodyDeclaration<?> member : classDeclaration.getMembers()) {
            if(member.toString().contains(marker)) {
                return;
            }
        }
        classDeclaration.addMember(StaticJavaParser.parseBodyDeclaration(declaration));
    }
}
