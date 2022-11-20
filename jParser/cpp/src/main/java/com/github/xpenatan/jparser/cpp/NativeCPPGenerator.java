package com.github.xpenatan.jparser.cpp;

import com.badlogic.gdx.jnigen.FileDescriptor;
import com.badlogic.gdx.jnigen.parsing.CMethodParser;
import com.badlogic.gdx.jnigen.parsing.JavaMethodParser;
import com.badlogic.gdx.jnigen.parsing.JniHeaderCMethodParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.xpenatan.jparser.core.util.CustomFileDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Modified version of gdx-jnigen NativeCodeGenerator
 */
public class NativeCPPGenerator implements CppGenerator {

    private CustomFileDescriptor jniDir;
    private String classpath;
    private CMethodParser cMethodParser = new JniHeaderCMethodParser();

    private static final String JNI_ARG_PREFIX = "obj_";
    private static final String JNI_RETURN_VALUE = "JNI_returnValue";
    private static final String JNI_WRAPPER_PREFIX = "wrapped_";

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

    private ArrayList<CppParserItem> parserItems = new ArrayList<>();
    ArrayList<JavaMethodParser.JavaSegment> javaSegments = new ArrayList<>();

    public NativeCPPGenerator(String classpath, String jniDir) {
        try {
            jniDir = jniDir.replace("\\", File.separator);
            this.jniDir = new CustomFileDescriptor(new File(jniDir).getCanonicalPath());
            this.classpath = classpath;
            if(!this.jniDir.exists()) {
                this.jniDir.mkdirs();
            }
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addParseFile(String sourceBaseDir, String inputJavaPath, String destinationJavaPath) {
        if(javaSegments.size() == 0) {
            return;
        }

        CppParserItem parserItem = new CppParserItem();
        parserItem.sourceBaseDir = sourceBaseDir;
        parserItem.inputJavaPath = inputJavaPath;
        parserItem.destinationJavaPath = destinationJavaPath;
        parserItem.javaSegments.addAll(javaSegments);
        parserItem.javaSegments.sort(new Comparator<JavaMethodParser.JavaSegment>() {
            @Override
            public int compare(JavaMethodParser.JavaSegment o1, JavaMethodParser.JavaSegment o2) {
                if(o1.getStartIndex() < o2.getStartIndex()) {
                    return -1;
                }
                else if(o1.getStartIndex() > o2.getStartIndex()) {
                    return 1;
                }
                return 0;
            }
        });

        parserItems.add(parserItem);
        javaSegments.clear();
    }

    @Override
    public void generate() {
        for(int i = 0; i < parserItems.size(); i++) {
            CppParserItem parserItem = parserItems.get(i);
            if(parserItem.javaSegments.size() != 0) {
                parseItem(parserItem);
            }
        }
    }

    private void parseItem(CppParserItem parserItem) {
        String destinationJavaPath = parserItem.destinationJavaPath;
        String sourceBaseDir = parserItem.sourceBaseDir;
        String inputJavaPath = parserItem.inputJavaPath;
        ArrayList<JavaMethodParser.JavaSegment> javaSegments = parserItem.javaSegments;
        try {
            generateHFiles(destinationJavaPath);
            String className = getNativeClassFileName(sourceBaseDir, inputJavaPath);

            List<FileDescriptor> hFiles = new ArrayList<>();
            for(FileDescriptor f : new FileDescriptor(jniDir.path()).list()) {
                if(f.name().startsWith(className) && f.name().endsWith(".h")) {
                    hFiles.add(f);
                }
            }

            FileDescriptor cppFile = new FileDescriptor(jniDir + "/" + className + ".cpp");
            generateCppFile(javaSegments, hFiles, cppFile);
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addNativeMethod(String content, MethodDeclaration methodDeclaration) {
        JavaMethodParser.JavaMethod method = createMethod(content, methodDeclaration);
        if(method != null) {
            javaSegments.add(method);
        }
    }

    @Override
    public void addNativeCode(String content, Node node) {
        int startLine = node.getBegin().get().line;
        int endLine = node.getEnd().get().line;
        javaSegments.add(new JavaMethodParser.JniSection(content + "\n\n", startLine, endLine));
        node.remove();
    }

    private JavaMethodParser.JavaMethod createMethod(String content, MethodDeclaration method) {
        String className = null;
        Optional<Node> parentNodeOptional = method.getParentNode();
        if(parentNodeOptional.isPresent()) {
            Node parentNode = parentNodeOptional.get();
            if(parentNode instanceof ClassOrInterfaceDeclaration) {
                ClassOrInterfaceDeclaration classInterface = (ClassOrInterfaceDeclaration)parentNode;
                className = classInterface.getNameAsString();
            }
        }
        if(className == null)
            return null;
        String name = method.getNameAsString();
        boolean isStatic = method.isStatic();
        String returnType = method.getType().toString();
        ArrayList<JavaMethodParser.Argument> arguments = new ArrayList<JavaMethodParser.Argument>();

        if(method.getParameters() != null) {
            for(Parameter parameter : method.getParameters()) {
                arguments.add(new JavaMethodParser.Argument(getArgumentType(parameter), parameter.getNameAsString()));
            }
        }

        return new JavaMethodParser.JavaMethod(className, name, isStatic, returnType, content, arguments, method.getBegin().get().line, method.getEnd().get().line);
    }

    private JavaMethodParser.ArgumentType getArgumentType(Parameter parameter) {
        String[] typeTokens = parameter.getType().toString().split("\\.");
        String type = typeTokens[typeTokens.length - 1];
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

    private String getNativeClassFileName(String sourceDirPath, String filePath) {
        String className = filePath.replace(sourceDirPath, "").replace('\\', '_').replace('/', '_').replace(".java", "");
        if(className.startsWith("_")) className = className.substring(1);
        return className;
    }

    private void generateHFiles(String filePath) throws Exception {
        //Use temporary directory to prevent javac from creating class files somewhere we care about.
        File tempClassFilesDirectory = Files.createTempDirectory("gdx-jnigen").toFile();
        String command = "javac -classpath " + classpath + " -d " + tempClassFilesDirectory.getAbsolutePath() + " -h " + jniDir.path() + " " + filePath;
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
        if(process.exitValue() != 0) {
            System.out.println();
            System.out.println("Command: " + command);
            InputStream errorStream = process.getErrorStream();
            int c = 0;
            while((c = errorStream.read()) != -1) {
                System.out.print((char)c);
            }
        }

        //Recursively delete temporary directory.
        Files.walk(tempClassFilesDirectory.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    private void generateCppFile(ArrayList<JavaMethodParser.JavaSegment> javaSegments, List<FileDescriptor> hFiles, FileDescriptor cppFile)
            throws Exception {
        StringBuffer buffer = new StringBuffer();

        ArrayList<CMethodParser.CMethod> cMethods = new ArrayList<>();
        for(FileDescriptor hFile : hFiles) {
            String headerFileContent = hFile.readString();
            cMethods.addAll(cMethodParser.parse(headerFileContent).getMethods());
            emitHeaderInclude(buffer, hFile.name());
        }

        for(JavaMethodParser.JavaSegment segment : javaSegments) {
            if(segment instanceof JavaMethodParser.JniSection) {
                emitJniSection(buffer, (JavaMethodParser.JniSection)segment);
            }

            if(segment instanceof JavaMethodParser.JavaMethod) {
                JavaMethodParser.JavaMethod javaMethod = (JavaMethodParser.JavaMethod)segment;
                if(javaMethod.getNativeCode() == null) {
                    throw new RuntimeException("Method '" + javaMethod.getName() + "' has no body");
                }
                CMethodParser.CMethod cMethod = findCMethod(javaMethod, cMethods);
                if(cMethod == null)
                    throw new RuntimeException("Couldn't find C method for Java method '" + javaMethod.getClassName() + "#"
                            + javaMethod.getName() + "'");
                emitJavaMethod(buffer, javaMethod, cMethod);
            }
        }
        cppFile.writeString(buffer.toString(), false, "UTF-8");
    }

    private void emitJniSection(StringBuffer buffer, JavaMethodParser.JniSection section) {
        emitLineMarker(buffer, section.getStartIndex());
        buffer.append(section.getNativeCode().replace("\r", ""));
    }

    private CMethodParser.CMethod findCMethod(JavaMethodParser.JavaMethod javaMethod, ArrayList<CMethodParser.CMethod> cMethods) {
        for(CMethodParser.CMethod cMethod : cMethods) {
            String javaMethodName = javaMethod.getName().replace("_", "_1");
            String javaClassName = javaMethod.getClassName().toString().replace("_", "_1");
            if(cMethod.getHead().endsWith(javaClassName + "_" + javaMethodName)
                    || cMethod.getHead().contains(javaClassName + "_" + javaMethodName + "__")) {
                // FIXME poor man's overloaded method check...
                // FIXME float test[] won't work, needs to be float[] test.
                if(cMethod.getArgumentTypes().length - 2 == javaMethod.getArguments().size()) {
                    boolean match = true;
                    for(int i = 2; i < cMethod.getArgumentTypes().length; i++) {
                        String cType = cMethod.getArgumentTypes()[i];
                        String javaType = javaMethod.getArguments().get(i - 2).getType().getJniType();
                        if(!cType.equals(javaType)) {
                            match = false;
                            break;
                        }
                    }

                    if(match) {
                        return cMethod;
                    }
                }
            }
        }
        return null;
    }

    private void emitHeaderInclude(StringBuffer buffer, String fileName) {
        buffer.append("#include <" + fileName + ">\n");
    }

    private void emitJavaMethod(StringBuffer buffer, JavaMethodParser.JavaMethod javaMethod, CMethodParser.CMethod cMethod) {
        // get the setup and cleanup code for arrays, buffers and strings
        StringBuffer jniSetupCode = new StringBuffer();
        StringBuffer jniCleanupCode = new StringBuffer();
        StringBuffer additionalArgs = new StringBuffer();
        StringBuffer wrapperArgs = new StringBuffer();
        emitJniSetupCode(jniSetupCode, javaMethod, additionalArgs, wrapperArgs);
        emitJniCleanupCode(jniCleanupCode, javaMethod, cMethod);

        // check if the user wants to do manual setup of JNI args
        boolean isManual = javaMethod.isManual();

        // if we have disposable arguments (string, buffer, array) and if there is a return
        // in the native code (conservative, not syntactically checked), emit a wrapper method.
        if(javaMethod.hasDisposableArgument() && javaMethod.getNativeCode().contains("return")) {
            // if the method is marked as manual, we just emit the signature and let the
            // user do whatever she wants.
            if(isManual) {
                emitMethodSignature(buffer, javaMethod, cMethod, null, false);
                emitMethodBody(buffer, javaMethod);
                buffer.append("}\n\n");
            }
            else {
                // emit the method containing the actual code, called by the wrapper
                // method with setup pointers to arrays, buffers and strings
                String wrappedMethodName = emitMethodSignature(buffer, javaMethod, cMethod, additionalArgs.toString());
                emitMethodBody(buffer, javaMethod);
                buffer.append("}\n\n");

                // emit the wrapper method, the one with the declaration in the header file
                emitMethodSignature(buffer, javaMethod, cMethod, null);
                if(!isManual) {
                    buffer.append(jniSetupCode);
                }

                if(cMethod.getReturnType().equals("void")) {
                    buffer.append("\t" + wrappedMethodName + "(" + wrapperArgs.toString() + ");\n\n");
                    if(!isManual) {
                        buffer.append(jniCleanupCode);
                    }
                    buffer.append("\treturn;\n");
                }
                else {
                    buffer.append("\t" + cMethod.getReturnType() + " " + JNI_RETURN_VALUE + " = " + wrappedMethodName + "("
                            + wrapperArgs.toString() + ");\n\n");
                    if(!isManual) {
                        buffer.append(jniCleanupCode);
                    }
                    buffer.append("\treturn " + JNI_RETURN_VALUE + ";\n");
                }
                buffer.append("}\n\n");
            }
        }
        else {
            emitMethodSignature(buffer, javaMethod, cMethod, null);
            if(!isManual) {
                buffer.append(jniSetupCode);
            }
            emitMethodBody(buffer, javaMethod);
            if(!isManual) {
                buffer.append(jniCleanupCode);
            }
            buffer.append("}\n\n");
        }
    }

    private void emitJniSetupCode(StringBuffer buffer, JavaMethodParser.JavaMethod javaMethod, StringBuffer additionalArgs,
                                  StringBuffer wrapperArgs) {
        // add environment and class/object as the two first arguments for
        // wrapped method.
        if(javaMethod.isStatic()) {
            wrapperArgs.append("env, clazz, ");
        }
        else {
            wrapperArgs.append("env, object, ");
        }

        // arguments for wrapper method
        for(int i = 0; i < javaMethod.getArguments().size(); i++) {
            JavaMethodParser.Argument arg = javaMethod.getArguments().get(i);
            if(!arg.getType().isPlainOldDataType() && !arg.getType().isObject()) {
                wrapperArgs.append(JNI_ARG_PREFIX);
            }
            // output the name of the argument
            wrapperArgs.append(arg.getName());
            if(i < javaMethod.getArguments().size() - 1) wrapperArgs.append(", ");
        }

        // direct buffer pointers
        for(JavaMethodParser.Argument arg : javaMethod.getArguments()) {
            if(arg.getType().isBuffer()) {
                String type = arg.getType().getBufferCType();
                buffer.append("\t" + type + " " + arg.getName() + " = (" + type + ")(" + JNI_ARG_PREFIX + arg.getName()
                        + "?env->GetDirectBufferAddress(" + JNI_ARG_PREFIX + arg.getName() + "):0);\n");
                additionalArgs.append(", ");
                additionalArgs.append(type);
                additionalArgs.append(" ");
                additionalArgs.append(arg.getName());
                wrapperArgs.append(", ");
                wrapperArgs.append(arg.getName());
            }
        }

        // string pointers
        for(JavaMethodParser.Argument arg : javaMethod.getArguments()) {
            if(arg.getType().isString()) {
                String type = "char*";
                buffer.append("\t" + type + " " + arg.getName() + " = (" + type + ")env->GetStringUTFChars(" + JNI_ARG_PREFIX
                        + arg.getName() + ", 0);\n");
                additionalArgs.append(", ");
                additionalArgs.append(type);
                additionalArgs.append(" ");
                additionalArgs.append(arg.getName());
                wrapperArgs.append(", ");
                wrapperArgs.append(arg.getName());
            }
        }

        // Array pointers, we have to collect those last as GetPrimitiveArrayCritical
        // will explode into our face if we call another JNI method after that.
        for(JavaMethodParser.Argument arg : javaMethod.getArguments()) {
            if(arg.getType().isPrimitiveArray()) {
                String type = arg.getType().getArrayCType();
                buffer.append("\t" + type + " " + arg.getName() + " = (" + type + ")env->GetPrimitiveArrayCritical(" + JNI_ARG_PREFIX
                        + arg.getName() + ", 0);\n");
                additionalArgs.append(", ");
                additionalArgs.append(type);
                additionalArgs.append(" ");
                additionalArgs.append(arg.getName());
                wrapperArgs.append(", ");
                wrapperArgs.append(arg.getName());
            }
        }

        // new line for separation
        buffer.append("\n");
    }

    private void emitJniCleanupCode(StringBuffer buffer, JavaMethodParser.JavaMethod javaMethod, CMethodParser.CMethod cMethod) {
        // emit cleanup code for arrays, must come first
        for(JavaMethodParser.Argument arg : javaMethod.getArguments()) {
            if(arg.getType().isPrimitiveArray()) {
                buffer.append("\tenv->ReleasePrimitiveArrayCritical(" + JNI_ARG_PREFIX + arg.getName() + ", " + arg.getName()
                        + ", 0);\n");
            }
        }

        // emit cleanup code for strings
        for(JavaMethodParser.Argument arg : javaMethod.getArguments()) {
            if(arg.getType().isString()) {
                buffer.append("\tenv->ReleaseStringUTFChars(" + JNI_ARG_PREFIX + arg.getName() + ", " + arg.getName() + ");\n");
            }
        }

        // new line for separation
        buffer.append("\n");
    }

    private String emitMethodSignature(StringBuffer buffer, JavaMethodParser.JavaMethod javaMethod, CMethodParser.CMethod cMethod, String additionalArguments) {
        return emitMethodSignature(buffer, javaMethod, cMethod, additionalArguments, true);
    }

    private String emitMethodSignature(StringBuffer buffer, JavaMethodParser.JavaMethod javaMethod, CMethodParser.CMethod cMethod, String additionalArguments,
                                       boolean appendPrefix) {
        // emit head, consisting of JNIEXPORT,return type and method name
        // if this is a wrapped method, prefix the method name
        String wrappedMethodName = null;
        if(additionalArguments != null) {
            String[] tokens = cMethod.getHead().replace("\r\n", "").replace("\n", "").split(" ");
            wrappedMethodName = JNI_WRAPPER_PREFIX + tokens[3];
            buffer.append("static inline ");
            buffer.append(tokens[1]);
            buffer.append(" ");
            buffer.append(wrappedMethodName);
            buffer.append("\n");
        }
        else {
            buffer.append(cMethod.getHead());
        }

        // construct argument list
        // Differentiate between static and instance method, then output each argument
        if(javaMethod.isStatic()) {
            buffer.append("(JNIEnv* env, jclass clazz");
        }
        else {
            buffer.append("(JNIEnv* env, jobject object");
        }
        if(javaMethod.getArguments().size() > 0) buffer.append(", ");
        for(int i = 0; i < javaMethod.getArguments().size(); i++) {
            // output the argument type as defined in the header
            buffer.append(cMethod.getArgumentTypes()[i + 2]);
            buffer.append(" ");
            // if this is not a POD or an object, we need to add a prefix
            // as we will output JNI code to get pointers to strings, arrays
            // and direct buffers.
            JavaMethodParser.Argument javaArg = javaMethod.getArguments().get(i);
            if(!javaArg.getType().isPlainOldDataType() && !javaArg.getType().isObject() && appendPrefix) {
                buffer.append(JNI_ARG_PREFIX);
            }
            // output the name of the argument
            buffer.append(javaArg.getName());

            // comma, if this is not the last argument
            if(i < javaMethod.getArguments().size() - 1) buffer.append(", ");
        }

        // if this is a wrapper method signature, add the additional arguments
        if(additionalArguments != null) {
            buffer.append(additionalArguments);
        }

        // close signature, open method body
        buffer.append(") {\n");

        // return the wrapped method name if any
        return wrappedMethodName;
    }

    private void emitMethodBody(StringBuffer buffer, JavaMethodParser.JavaMethod javaMethod) {
        // emit a line marker
        emitLineMarker(buffer, javaMethod.getEndIndex());

        // FIXME add tabs cleanup
        buffer.append(javaMethod.getNativeCode());
        buffer.append("\n");
    }

    private void emitLineMarker(StringBuffer buffer, int line) {
        buffer.append("\n//@line:");
        buffer.append(line);
        buffer.append("\n");
    }
}
