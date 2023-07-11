package com.github.xpenatan.jparser.cpp;

import com.badlogic.gdx.jnigen.parsing.JavaMethodParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserItem;
import com.github.xpenatan.jparser.core.util.CustomFileDescriptor;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NativeCPPGeneratorV2 implements CppGenerator {

    private static final Map<String, JavaMethodParser.ArgumentType> plainOldDataTypes;
    private static final Map<String, JavaMethodParser.ArgumentType> arrayTypes;
    private static final Map<String, JavaMethodParser.ArgumentType> bufferTypes;
    private static final Map<String, JavaMethodParser.ArgumentType> otherTypes;

    static {
        plainOldDataTypes = new HashMap<String, JavaMethodParser.ArgumentType>();
        plainOldDataTypes.put("boolean", JavaMethodParser.ArgumentType.Boolean);
        plainOldDataTypes.put("byte", JavaMethodParser.ArgumentType.Byte);
        plainOldDataTypes.put("char", JavaMethodParser.ArgumentType.Char);
        plainOldDataTypes.put("short", JavaMethodParser.ArgumentType.Short);
        plainOldDataTypes.put("int", JavaMethodParser.ArgumentType.Integer);
        plainOldDataTypes.put("long", JavaMethodParser.ArgumentType.Long);
        plainOldDataTypes.put("float", JavaMethodParser.ArgumentType.Float);
        plainOldDataTypes.put("double", JavaMethodParser.ArgumentType.Double);

        arrayTypes = new HashMap<String, JavaMethodParser.ArgumentType>();
        arrayTypes.put("boolean", JavaMethodParser.ArgumentType.BooleanArray);
        arrayTypes.put("byte", JavaMethodParser.ArgumentType.ByteArray);
        arrayTypes.put("char", JavaMethodParser.ArgumentType.CharArray);
        arrayTypes.put("short", JavaMethodParser.ArgumentType.ShortArray);
        arrayTypes.put("int", JavaMethodParser.ArgumentType.IntegerArray);
        arrayTypes.put("long", JavaMethodParser.ArgumentType.LongArray);
        arrayTypes.put("float", JavaMethodParser.ArgumentType.FloatArray);
        arrayTypes.put("double", JavaMethodParser.ArgumentType.DoubleArray);

        bufferTypes = new HashMap<String, JavaMethodParser.ArgumentType>();
        bufferTypes.put("Buffer", JavaMethodParser.ArgumentType.Buffer);
        bufferTypes.put("ByteBuffer", JavaMethodParser.ArgumentType.ByteBuffer);
        bufferTypes.put("CharBuffer", JavaMethodParser.ArgumentType.CharBuffer);
        bufferTypes.put("ShortBuffer", JavaMethodParser.ArgumentType.ShortBuffer);
        bufferTypes.put("IntBuffer", JavaMethodParser.ArgumentType.IntBuffer);
        bufferTypes.put("LongBuffer", JavaMethodParser.ArgumentType.LongBuffer);
        bufferTypes.put("FloatBuffer", JavaMethodParser.ArgumentType.FloatBuffer);
        bufferTypes.put("DoubleBuffer", JavaMethodParser.ArgumentType.DoubleBuffer);

        otherTypes = new HashMap<String, JavaMethodParser.ArgumentType>();
        otherTypes.put("String", JavaMethodParser.ArgumentType.String);
        otherTypes.put("Class", JavaMethodParser.ArgumentType.Class);
        otherTypes.put("Throwable", JavaMethodParser.ArgumentType.Throwable);
    }

    private String jniDir;
    private String cppGlueName = "JNIGlue";
    private String cppGlueHPath;

    StringBuilder printer = new StringBuilder();
    StringBuilder printerHeader = new StringBuilder();

    private boolean init = true;

    public NativeCPPGeneratorV2(String jniDir) {
        this.jniDir = jniDir;
    }

    private void print(String text) {
        print(false, text);
    }

    private void print(boolean header, String text) {
        if(init) {
            init = false;
            printerHeader.append("#include <jni.h>\n");
            printer.append("\n");
            printer.append("extern \"C\" {\n");
            printer.append("\n");
        }
        if(header) {
            printerHeader.append(text + "\n");
        }
        else {
            printer.append(text + "\n");
        }
    }

    @Override
    public void addNativeCode(Node node, String content) {
        print(true, content);
    }

    @Override
    public void addNativeCode(MethodDeclaration methodDeclaration, String content) {
        String methodName = methodDeclaration.getNameAsString();
        ClassOrInterfaceDeclaration classDeclaration = (ClassOrInterfaceDeclaration)methodDeclaration.getParentNode().get();
        CompilationUnit compilationUnit = classDeclaration.findCompilationUnit().get();
        String packageName = compilationUnit.getPackageDeclaration().get().getNameAsString();
        String className = classDeclaration.getNameAsString();
        String packageNameCPP = packageName.replace(".", "_");
        String returnTypeStr = methodDeclaration.getType().toString();
        JavaMethodParser.ArgumentType returnType = getType(returnTypeStr);

        String params = "(JNIEnv* env, jclass clazz";

        ArrayList<JavaMethodParser.Argument> arguments = new ArrayList<JavaMethodParser.Argument>();
        if(methodDeclaration.getParameters() != null) {
            for(Parameter parameter : methodDeclaration.getParameters()) {
                JavaMethodParser.ArgumentType argumentType = getArgumentType(parameter);
                arguments.add(new JavaMethodParser.Argument(argumentType, parameter.getNameAsString()));
            }
        }

        for(int i = 0; i < arguments.size(); i++) {
            JavaMethodParser.Argument argument = arguments.get(i);
            params += ", " + argument.getType().getJniType() + " " + argument.getName();
        }

        params += ")";

        print("JNIEXPORT " + returnType.getJniType() + " JNICALL Java_" + packageNameCPP + "_" + className + "_" + methodName + params + " {");
        content = "\t" + content.replace("\n", "\n\t");
        print(content);
        print("}");
        print("");
        System.out.println();
    }

    @Override
    public void addParseFile(JParser jParser, JParserItem parserItem) {
        System.out.println();
    }

    @Override
    public void generate(JParser jParser) {
        printer.insert(0, printerHeader);
        print("}");
        String code = printer.toString();

        cppGlueHPath = jniDir + File.separator + cppGlueName + ".h";
        String cppGluePath = jniDir + File.separator + cppGlueName + ".cpp";
        CustomFileDescriptor fileDescriptor = new CustomFileDescriptor(cppGlueHPath);
        fileDescriptor.writeString(code, false);

        CustomFileDescriptor cppFile = new CustomFileDescriptor(cppGluePath);
        String include = "#include <" + cppGlueName + ".h>";
        cppFile.writeString(include, true);
    }

//    JNIEXPORT jint JNICALL Java_physx_NativeObject__1_1sizeOfPointer(JNIEnv*, jclass) {
//        return sizeof(void*);
//    }

    private JavaMethodParser.ArgumentType getArgumentType(Parameter parameter) {
        String[] typeTokens = parameter.getType().toString().split("\\.");
        String type = typeTokens[typeTokens.length - 1];
        return getType(type);
    }

    private JavaMethodParser.ArgumentType getType(String type) {
        int arrayDim = 0;
        for(int i = 0; i < type.length(); i++) {
            if(type.charAt(i) == '[') arrayDim++;
        }
        type = type.replace("[", "").replace("]", "");

        if(arrayDim >= 1) {
            if(arrayDim > 1) return JavaMethodParser.ArgumentType.ObjectArray;
            JavaMethodParser.ArgumentType arrayType = arrayTypes.get(type);
            if(arrayType == null) {
                return JavaMethodParser.ArgumentType.ObjectArray;
            }
            return arrayType;
        }

        if(plainOldDataTypes.containsKey(type)) return plainOldDataTypes.get(type);
        if(bufferTypes.containsKey(type)) return bufferTypes.get(type);
        if(otherTypes.containsKey(type)) return otherTypes.get(type);
        return JavaMethodParser.ArgumentType.Object;
    }

//    private static class MethodCPPData {
//
//        public String packageName;
//
//    }
}
