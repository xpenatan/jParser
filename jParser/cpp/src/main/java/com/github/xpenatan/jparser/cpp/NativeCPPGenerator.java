package com.github.xpenatan.jparser.cpp;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.xpenatan.jparser.core.JParser;
import com.github.xpenatan.jparser.core.JParserItem;
import com.github.xpenatan.jparser.core.util.CustomFileDescriptor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Predicate;

public class NativeCPPGenerator implements CppGenerator {

    public static boolean SKIP_GLUE_CODE;

    private static final Map<String, ArgumentType> plainOldDataTypes;
    private static final Map<String, ArgumentType> arrayTypes;
    private static final Map<String, ArgumentType> bufferTypes;
    private static final Map<String, ArgumentType> otherTypes;
    private static final Map<ArgumentType, String> valueTypes;

    private static String helperName = "IDLHelper.h";

    static {
        valueTypes = new HashMap<>();
        valueTypes.put(ArgumentType.Boolean, "Z");
        valueTypes.put(ArgumentType.Byte, "B");
        valueTypes.put(ArgumentType.Char, "C");
        valueTypes.put(ArgumentType.Short, "S");
        valueTypes.put(ArgumentType.Integer, "I");
        valueTypes.put(ArgumentType.Long, "J");
        valueTypes.put(ArgumentType.Float, "F");
        valueTypes.put(ArgumentType.Double, "D");
        valueTypes.put(ArgumentType.FloatArray, "_3F");
        valueTypes.put(ArgumentType.IntegerArray, "_3I");
        valueTypes.put(ArgumentType.DoubleArray, "_3D");
        valueTypes.put(ArgumentType.LongArray, "_3J");
        valueTypes.put(ArgumentType.ShortArray, "_3S");
        valueTypes.put(ArgumentType.CharArray, "_3C");
        valueTypes.put(ArgumentType.ByteArray, "_3B");
        valueTypes.put(ArgumentType.BooleanArray, "_3Z");
        valueTypes.put(ArgumentType.Object, "Ljava_lang_Object_2");
        valueTypes.put(ArgumentType.String, "Ljava_lang_String_2");
        valueTypes.put(ArgumentType.ByteBuffer, "Ljava_nio_ByteBuffer_2");
        valueTypes.put(ArgumentType.IntBuffer, "Ljava_nio_IntBuffer_2");
        valueTypes.put(ArgumentType.LongBuffer, "Ljava_nio_LongBuffer_2");
        valueTypes.put(ArgumentType.FloatBuffer, "Ljava_nio_FloatBuffer_2");
        valueTypes.put(ArgumentType.DoubleBuffer, "Ljava_nio_DoubleBuffer_2");

        plainOldDataTypes = new HashMap<String, ArgumentType>();
        plainOldDataTypes.put("boolean", ArgumentType.Boolean);
        plainOldDataTypes.put("byte", ArgumentType.Byte);
        plainOldDataTypes.put("char", ArgumentType.Char);
        plainOldDataTypes.put("short", ArgumentType.Short);
        plainOldDataTypes.put("int", ArgumentType.Integer);
        plainOldDataTypes.put("long", ArgumentType.Long);
        plainOldDataTypes.put("float", ArgumentType.Float);
        plainOldDataTypes.put("double", ArgumentType.Double);

        arrayTypes = new HashMap<String, ArgumentType>();
        arrayTypes.put("boolean", ArgumentType.BooleanArray);
        arrayTypes.put("byte", ArgumentType.ByteArray);
        arrayTypes.put("char", ArgumentType.CharArray);
        arrayTypes.put("short", ArgumentType.ShortArray);
        arrayTypes.put("int", ArgumentType.IntegerArray);
        arrayTypes.put("long", ArgumentType.LongArray);
        arrayTypes.put("float", ArgumentType.FloatArray);
        arrayTypes.put("double", ArgumentType.DoubleArray);

        bufferTypes = new HashMap<String, ArgumentType>();
        bufferTypes.put("Buffer", ArgumentType.Buffer);
        bufferTypes.put("ByteBuffer", ArgumentType.ByteBuffer);
        bufferTypes.put("CharBuffer", ArgumentType.CharBuffer);
        bufferTypes.put("ShortBuffer", ArgumentType.ShortBuffer);
        bufferTypes.put("IntBuffer", ArgumentType.IntBuffer);
        bufferTypes.put("LongBuffer", ArgumentType.LongBuffer);
        bufferTypes.put("FloatBuffer", ArgumentType.FloatBuffer);
        bufferTypes.put("DoubleBuffer", ArgumentType.DoubleBuffer);

        otherTypes = new HashMap<String, ArgumentType>();
        otherTypes.put("String", ArgumentType.String);
        otherTypes.put("Class", ArgumentType.Class);
        otherTypes.put("Throwable", ArgumentType.Throwable);
    }

    private String glueCppDestinationDir;
    private String cppGlueName = "JNIGlue";

    StringBuilder mainPrinter = new StringBuilder();
    StringBuilder headerPrinter = new StringBuilder();
    StringBuilder codePrinter = new StringBuilder();

    private boolean init = true;

    private boolean exportJNIMethods;

    public NativeCPPGenerator(String cppDestinationDir) {
        this(cppDestinationDir, true);
    }

    public NativeCPPGenerator(String cppDestinationDir, boolean exportJNIMethods) {
        try {
            this.exportJNIMethods = exportJNIMethods;
            this.glueCppDestinationDir = new File(cppDestinationDir, "jniglue").getCanonicalPath() + File.separator;
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void print(PrintType type, String text) {
        if(init) {
            init = false;
            headerPrinter.append("#pragma once\n");
            headerPrinter.append("#include <jni.h>\n");
            headerPrinter.append("#include \"" + helperName + "\"\n");
            mainPrinter.append("\n");
            mainPrinter.append("extern \"C\" {\n");
            mainPrinter.append("\n");
        }
        if(type == PrintType.HEADER) {
            headerPrinter.append(text + "\n");
        }
        else if(type == PrintType.MAIN) {
            mainPrinter.append(text + "\n");
        }
        else if(type == PrintType.CODE) {
            codePrinter.append(text + "\n");
        }
    }

    @Override
    public void addNativeCode(Node node, String content) {
        Scanner scanner = new Scanner(content);
        boolean haveInclude = content.contains("#include");
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if(haveInclude) {
                print(PrintType.HEADER, line);
            }
            else {
                print(PrintType.CODE, line);
            }
        }
        scanner.close();
    }

    @Override
    public void addNativeCode(MethodDeclaration nativeMethod, String content) {
        String methodName = nativeMethod.getNameAsString();
        boolean isStatic = nativeMethod.isStatic();
        TypeDeclaration classOrEnum = (TypeDeclaration)nativeMethod.getParentNode().get();
        CompilationUnit compilationUnit = classOrEnum.findCompilationUnit().get();
        String packageName = compilationUnit.getPackageDeclaration().get().getNameAsString();
        String className = classOrEnum.getNameAsString();
        String packageNameCPP = packageName.replace(".", "_");
        String returnTypeStr = nativeMethod.getType().toString();
        String returnType = returnTypeStr.equals("void") ? returnTypeStr : getType(returnTypeStr).getJniType();
        String params = "(JNIEnv* env, ";
        if(isStatic) {
            params += "jclass clazz";
        }
        else {
            params += "jobject object";
        }

        ArrayList<Argument> arguments = new ArrayList<Argument>();
        if(nativeMethod.getParameters() != null) {
            for(Parameter parameter : nativeMethod.getParameters()) {
                Argument argument = getArgument(parameter);
                arguments.add(argument);
            }
        }

        // https://docs.oracle.com/en/java/javase/17/docs/specs/jni/design.html#native-method-arguments

        String paramsType = "";

        String prefixCode = "";
        String suffixCode = "";

        for(int i = 0; i < arguments.size(); i++) {
            Argument argument = arguments.get(i);
            ArgumentType type = argument.getType();
            String typeName = type.name();
            String paramName = argument.getName();
            String newParamName = paramName;
            String valueType = argument.getValueType();
            paramsType += valueType;
            if(typeName.equals("String")) {
                newParamName = newParamName + "_string";
                prefixCode += "char* " + paramName + " = " + newParamName + " == NULL ? NULL : (char*)env->GetStringUTFChars(" + newParamName + ", 0);\n";
                suffixCode += "if(" + paramName + " != NULL) env->ReleaseStringUTFChars(" + newParamName + ", " + paramName  + ");\n";
            }
            params += ", " + type.getJniType() + " " + newParamName;
        }

        if(!paramsType.isEmpty()) {
            paramsType = "__" + paramsType;
        }
        else {
            // Not tested in all cases
            paramsType += "__";
        }

        params += ")";

        if(methodName.contains("_")) {
            methodName = methodName.replace("_", "_1");
        }

        if(className.contains("_")) {
            className = className.replace("_", "_1");
        }

        boolean haveReturn = content.lines().anyMatch(s -> s.trim().startsWith("return "));
        if(haveReturn) {
            String wrappedLambda = "" +
                    returnType + " wrappedReturn = [&]() -> " + returnType + " {\n" +
                    content +
                    "\n }();";

            content = wrappedLambda;

            suffixCode += "return wrappedReturn;";
        }

        content = prefixCode + "\n" + content + "\n" + suffixCode;

        String fullMethodName =  packageNameCPP + "_" + className + "_" + methodName + paramsType + params;

        String JNIExport = "";

        if(exportJNIMethods) {
            JNIExport = "JNIEXPORT ";
        }

        print(PrintType.MAIN, JNIExport + returnType + " JNICALL Java_" + fullMethodName + " {");
        content = "\t" + content.replace("\n", "\n\t");
        print(PrintType.MAIN, content);
        print(PrintType.MAIN, "}");
        print(PrintType.MAIN, "");
    }

    @Override
    public void addParseFile(JParser jParser, JParserItem parserItem) {
    }

    @Override
    public void generate(JParser jParser) {
        headerPrinter.append("\n");

        mainPrinter.insert(0, codePrinter);
        mainPrinter.insert(0, headerPrinter);
        print(PrintType.MAIN, "}");
        String code = mainPrinter.toString();

        String gluePathStr = glueCppDestinationDir;
        String cppGlueHPath = gluePathStr + cppGlueName + ".h";
        String cppGluePath = gluePathStr + cppGlueName + ".cpp";
        CustomFileDescriptor fileDescriptor = new CustomFileDescriptor(cppGlueHPath);
        if(!SKIP_GLUE_CODE) {
            fileDescriptor.writeString(code, false);
        }

        CustomFileDescriptor cppFile = new CustomFileDescriptor(cppGluePath);
        String include = "#include \"" + cppGlueName + ".h\"";
        cppFile.writeString(include, false);
    }

    private Argument getArgument(Parameter parameter) {
        String[] typeTokens = parameter.getType().toString().split("\\.");
        String type = typeTokens[typeTokens.length - 1];
        ArgumentType argumentType = getType(type);
        String valueType = valueTypes.get(argumentType);
        return new Argument(argumentType, parameter.getNameAsString(), valueType);
    }

    private ArgumentType getType(String type) {
        int arrayDim = 0;
        for(int i = 0; i < type.length(); i++) {
            if(type.charAt(i) == '[') arrayDim++;
        }
        type = type.replace("[", "").replace("]", "");

        if(arrayDim >= 1) {
            if(arrayDim > 1) return ArgumentType.ObjectArray;
            ArgumentType arrayType = arrayTypes.get(type);
            if(arrayType == null) {
                return ArgumentType.ObjectArray;
            }
            return arrayType;
        }

        if(plainOldDataTypes.containsKey(type)) return plainOldDataTypes.get(type);
        if(bufferTypes.containsKey(type)) return bufferTypes.get(type);
        if(otherTypes.containsKey(type)) return otherTypes.get(type);
        return ArgumentType.Object;
    }

    public enum ArgumentType {
        Boolean("jboolean"), Byte("jbyte"), Char("jchar"), Short("jshort"), Integer("jint"), Long("jlong"), Float("jfloat"), Double(
                "jdouble"), Buffer("jobject"), ByteBuffer("jobject"), CharBuffer("jobject"), ShortBuffer("jobject"), IntBuffer("jobject"), LongBuffer(
                "jobject"), FloatBuffer("jobject"), DoubleBuffer("jobject"), BooleanArray("jbooleanArray"), ByteArray("jbyteArray"), CharArray(
                "jcharArray"), ShortArray("jshortArray"), IntegerArray("jintArray"), LongArray("jlongArray"), FloatArray("jfloatArray"), DoubleArray(
                "jdoubleArray"), String("jstring"), Class("jclass"), Throwable("jthrowable"), Object("jobject"), ObjectArray("jobjectArray");

        private final String jniType;

        ArgumentType (String jniType) {
            this.jniType = jniType;
        }

        public boolean isPrimitiveArray () {
            return toString().endsWith("Array") && this != ObjectArray;
        }

        public boolean isBuffer () {
            return toString().endsWith("Buffer");
        }

        public boolean isObject () {
            return toString().equals("Object") || this == ObjectArray;
        }

        public boolean isString () {
            return toString().equals("String");
        }

        public boolean isPlainOldDataType () {
            return !isString() && !isPrimitiveArray() && !isBuffer() && !isObject();
        }

        public String getBufferCType () {
            if (!this.isBuffer()) throw new RuntimeException("ArgumentType " + this + " is not a Buffer!");
            if (this == Buffer) return "unsigned char*";
            if (this == ByteBuffer) return "char*";
            if (this == CharBuffer) return "unsigned short*";
            if (this == ShortBuffer) return "short*";
            if (this == IntBuffer) return "int*";
            if (this == LongBuffer) return "long long*";
            if (this == FloatBuffer) return "float*";
            if (this == DoubleBuffer) return "double*";
            throw new RuntimeException("Unknown Buffer type " + this);
        }

        public String getArrayCType () {
            if (!this.isPrimitiveArray()) throw new RuntimeException("ArgumentType " + this + " is not an Array!");
            if (this == BooleanArray) return "bool*";
            if (this == ByteArray) return "char*";
            if (this == CharArray) return "unsigned short*";
            if (this == ShortArray) return "short*";
            if (this == IntegerArray) return "int*";
            if (this == LongArray) return "long long*";
            if (this == FloatArray) return "float*";
            if (this == DoubleArray) return "double*";
            throw new RuntimeException("Unknown Array type " + this);
        }

        public String getJniType () {
            return jniType;
        }
    }

    public static class Argument {
        final ArgumentType type;
        private final String name;
        private final String valueType;

        public Argument (ArgumentType type, String name, String valueType) {
            this.type = type;
            this.name = name;
            this.valueType = valueType;
        }

        public ArgumentType getType () {
            return type;
        }

        public String getName () {
            return name;
        }

        public String getValueType() {
            return valueType;
        }

        @Override
        public String toString () {
            return "Argument [type=" + type + ", name=" + name + ", valueType=" + valueType + "]";
        }
    }

    enum PrintType {
        HEADER,
        CODE,
        MAIN
    }
}
