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
import com.github.xpenatan.jparser.core.util.FileHelper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

public class NativeCPPGeneratorV2 implements CppGenerator {

    private static final Map<String, JavaMethodParser.ArgumentType> plainOldDataTypes;
    private static final Map<String, JavaMethodParser.ArgumentType> arrayTypes;
    private static final Map<String, JavaMethodParser.ArgumentType> bufferTypes;
    private static final Map<String, JavaMethodParser.ArgumentType> otherTypes;
    private static final Map<String, String> valueTypes;

    private static String helperName = "IDLHelper.h";

    static {
        valueTypes = new HashMap<>();
        valueTypes.put(JavaMethodParser.ArgumentType.Boolean.getJniType(), "Z");
        valueTypes.put(JavaMethodParser.ArgumentType.Byte.getJniType(), "B");
        valueTypes.put(JavaMethodParser.ArgumentType.Char.getJniType(), "C");
        valueTypes.put(JavaMethodParser.ArgumentType.Short.getJniType(), "S");
        valueTypes.put(JavaMethodParser.ArgumentType.Integer.getJniType(), "I");
        valueTypes.put(JavaMethodParser.ArgumentType.Long.getJniType(), "J");
        valueTypes.put(JavaMethodParser.ArgumentType.Float.getJniType(), "F");
        valueTypes.put(JavaMethodParser.ArgumentType.Double.getJniType(), "D");
        valueTypes.put(JavaMethodParser.ArgumentType.FloatArray.getJniType(), "_3F");
        valueTypes.put(JavaMethodParser.ArgumentType.IntegerArray.getJniType(), "_3I");
        valueTypes.put(JavaMethodParser.ArgumentType.DoubleArray.getJniType(), "_3D");
        valueTypes.put(JavaMethodParser.ArgumentType.LongArray.getJniType(), "_3J");
        valueTypes.put(JavaMethodParser.ArgumentType.ShortArray.getJniType(), "_3S");
        valueTypes.put(JavaMethodParser.ArgumentType.CharArray.getJniType(), "_3C");
        valueTypes.put(JavaMethodParser.ArgumentType.ByteArray.getJniType(), "_3B");
        valueTypes.put(JavaMethodParser.ArgumentType.BooleanArray.getJniType(), "_3Z");
        valueTypes.put(JavaMethodParser.ArgumentType.Object.getJniType(), "L");

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

    private String cppSourceDir;
    private String cppDestinationDir;
    private String cppGlueName = "JNIGlue";
    private String cppGlueHPath;

    StringBuilder mainPrinter = new StringBuilder();
    StringBuilder headerPrinter = new StringBuilder();
    StringBuilder codePrinter = new StringBuilder();

    private boolean init = true;

    private HashSet<String> includes = new HashSet<>();

    public NativeCPPGeneratorV2(String cppSourceDir, String cppDestinationDir) {
        try {
            this.cppSourceDir = new File(cppSourceDir).getCanonicalPath();
            this.cppDestinationDir = new File(cppDestinationDir).getCanonicalPath() + File.separator;
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void print(PrintType type, String text) {
        if(init) {
            init = false;
            headerPrinter.append("#include <jni.h>\n");
            headerPrinter.append("#include <" + helperName + ">\n");
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
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if(line.startsWith("#include")) {
                line = line.replace("\\", "/");
                line = line.replace("/", File.separator);
                line = line.replaceFirst("\"", "<");
                line = line.replace("\"", ">");

                if(!includes.contains(line)) {
                    includes.add(line);
                    print(PrintType.HEADER, line);
                }
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
        ClassOrInterfaceDeclaration classDeclaration = (ClassOrInterfaceDeclaration)nativeMethod.getParentNode().get();
        CompilationUnit compilationUnit = classDeclaration.findCompilationUnit().get();
        String packageName = compilationUnit.getPackageDeclaration().get().getNameAsString();
        String className = classDeclaration.getNameAsString();
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

        for(int i = 0; i < arguments.size(); i++) {
            Argument argument = arguments.get(i);
            String valueType = argument.getValueType();
            paramsType += valueType;
            params += ", " + argument.getType().getJniType() + " " + argument.getName();
        }

        if(!paramsType.isEmpty()) {
            paramsType = "__" + paramsType;
        }
        else {
            // Not tested in all cases
            paramsType += "__";
        }

        params += ")";

        String fullMethodName =  packageNameCPP + "_" + className + "_" + methodName + paramsType + params;
        print(PrintType.MAIN, "JNIEXPORT " + returnType + " JNICALL Java_" + fullMethodName + " {");
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
        try {
            FileHelper.copyDir(cppSourceDir, cppDestinationDir);

            headerPrinter.append("\n");

            mainPrinter.insert(0, codePrinter);
            mainPrinter.insert(0, headerPrinter);
            print(PrintType.MAIN, "}");
            String code = mainPrinter.toString();

            InputStream idlHelperClass = getClass().getClassLoader().getResourceAsStream(helperName);
            String helperPath = cppDestinationDir + File.separator + helperName;
            CustomFileDescriptor helperFile = new CustomFileDescriptor(helperPath);
            helperFile.write(idlHelperClass, false);

            cppGlueHPath = cppDestinationDir + File.separator + cppGlueName + ".h";
            String cppGluePath = cppDestinationDir + File.separator + cppGlueName + ".cpp";
            CustomFileDescriptor fileDescriptor = new CustomFileDescriptor(cppGlueHPath);
            fileDescriptor.writeString(code, false);

            CustomFileDescriptor cppFile = new CustomFileDescriptor(cppGluePath);
            String include = "#include <" + cppGlueName + ".h>";
            cppFile.writeString(include, false);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Argument getArgument(Parameter parameter) {
        String[] typeTokens = parameter.getType().toString().split("\\.");
        String type = typeTokens[typeTokens.length - 1];
        JavaMethodParser.ArgumentType argumentType = getType(type);
        String jniType = argumentType.getJniType();
        String valueType = valueTypes.get(jniType);
        return new Argument(argumentType, parameter.getNameAsString(), valueType);
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

    public static class Argument {
        final JavaMethodParser.ArgumentType type;
        private final String name;
        private final String valueType;

        public Argument (JavaMethodParser.ArgumentType type, String name, String valueType) {
            this.type = type;
            this.name = name;
            this.valueType = valueType;
        }

        public JavaMethodParser.ArgumentType getType () {
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
