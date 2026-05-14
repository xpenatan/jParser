package com.github.xpenatan.jParser.ffm;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.xpenatan.jParser.core.JParser;
import com.github.xpenatan.jParser.core.JParserItem;
import com.github.xpenatan.jParser.core.util.CustomFileDescriptor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Generates FFMGlue.cpp/.h with extern "C" exported functions using standard C types.
 * Parallel to NativeCPPGenerator but without any JNI dependencies.
 */
public class FFMCppGenerator implements FFMNativeCodeGenerator {

    public static boolean SKIP_GLUE_CODE = false;

    private String glueCppDestinationDir;
    private String cppGlueName = "FFMGlue";
    private FFMClassData ffmClassData;

    StringBuilder mainPrinter = new StringBuilder();
    StringBuilder headerPrinter = new StringBuilder();
    StringBuilder codePrinter = new StringBuilder();
    private final TreeMap<String, String> obfuscationMapping = new TreeMap<>();

    private boolean init = true;

    public FFMCppGenerator(String cppDestinationDir) {
        try {
            this.glueCppDestinationDir = new File(cppDestinationDir, "ffmglue").getCanonicalPath() + File.separator;
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setFFMClassData(FFMClassData ffmClassData) {
        this.ffmClassData = ffmClassData;
    }

    private void print(PrintType type, String text) {
        if(init) {
            init = false;
            headerPrinter.append("#pragma once\n");
            headerPrinter.append("#include <cstdint>\n");
            headerPrinter.append("\n");
            headerPrinter.append("#ifdef _WIN32\n");
            headerPrinter.append("    #define FFM_EXPORT __declspec(dllexport)\n");
            headerPrinter.append("#else\n");
            headerPrinter.append("    #define FFM_EXPORT __attribute__((visibility(\"default\")))\n");
            headerPrinter.append("#endif\n");
            headerPrinter.append("\n");
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
        while(scanner.hasNextLine()) {
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
    public void addCallbackClassCode(String cppClassCode) {
        // Callback class code goes into the CODE section (before extern "C")
        print(PrintType.CODE, cppClassCode);
    }

    @Override
    public void addNativeCode(MethodDeclaration nativeMethod, String content) {
        String methodName = nativeMethod.getNameAsString();
        TypeDeclaration classOrEnum = (TypeDeclaration) nativeMethod.getParentNode().get();
        CompilationUnit compilationUnit = classOrEnum.findCompilationUnit().get();
        String packageName = compilationUnit.getPackageDeclaration().get().getNameAsString();
        String className = classOrEnum.getNameAsString();
        boolean overloadedMethod = isOverloadedMethod(classOrEnum, methodName);
        String returnTypeStr = nativeMethod.getType().toString();
        String returnType = FFMTypeMapper.getCType(returnTypeStr);

        // Build parameter list — no JNIEnv*, no jclass/jobject
        String params = "(";
        ArrayList<FFMArgument> arguments = new ArrayList<>();
        if(nativeMethod.getParameters() != null) {
            for(Parameter parameter : nativeMethod.getParameters()) {
                FFMArgument argument = getArgument(parameter);
                arguments.add(argument);
            }
        }

        String prefixCode = "";
        String suffixCode = "";

        for(int i = 0; i < arguments.size(); i++) {
            FFMArgument argument = arguments.get(i);
            String paramName = argument.name;
            String cType = argument.cType;
            if(i > 0) {
                params += ", ";
            }

            // Strings arrive as const char* directly from FFM — no conversion needed
            params += cType + " " + paramName;
        }

        params += ")";

        boolean haveReturn = containsReturnStatement(content);
        if(haveReturn) {
            String wrappedLambda = "" +
                    returnType + " wrappedReturn = [&]() -> " + returnType + " {\n" +
                    content +
                    "\n }();";

            content = wrappedLambda;
            suffixCode += "return wrappedReturn;";
        }

        content = prefixCode + "\n" + content + "\n" + suffixCode;

        String symbolName = buildSymbolName(packageName, className, methodName, arguments, overloadedMethod, ffmClassData);
        registerObfuscationMapping(nativeMethod, packageName, className, symbolName, arguments);
        print(PrintType.MAIN, "FFM_EXPORT " + returnType + " " + symbolName + params + " {");
        content = "\t" + content.replace("\n", "\n\t");
        print(PrintType.MAIN, content);
        print(PrintType.MAIN, "}");
        print(PrintType.MAIN, "");
    }

    private boolean containsReturnStatement(String content) {
        Scanner scanner = new Scanner(content);
        try {
            while(scanner.hasNextLine()) {
                if(scanner.nextLine().trim().startsWith("return ")) {
                    return true;
                }
            }
            return false;
        }
        finally {
            scanner.close();
        }
    }

    /**
     * Build the symbol name for a native method.
     * Must match exactly with what FFMCodeParser generates for SymbolLookup.find().
     */
    public static String buildSymbolName(String packageName, String className, String methodName, ArrayList<FFMArgument> arguments) {
        String packageNameCPP = packageName.replace(".", "_");
        String escapedClassName = className.replace("_", "_1");
        String escapedMethodName = methodName.replace("_", "_1");

    private void registerObfuscationMapping(MethodDeclaration nativeMethod, String packageName, String className, String symbolName, ArrayList<FFMArgument> arguments) {
        if(!isObfuscatedMode()) {
            return;
        }
        StringBuilder signature = new StringBuilder();
        signature.append(packageName)
                .append('.')
                .append(className)
                .append('#')
                .append(nativeMethod.getNameAsString())
                .append('(');
        for(int i = 0; i < arguments.size(); i++) {
            if(i > 0) {
                signature.append(", ");
            }
            signature.append(arguments.get(i).javaType);
        }
        signature.append(") : ")
                .append(nativeMethod.getType().toString());
        obfuscationMapping.put(symbolName, signature.toString());
    }

        return "jparser_" + packageNameCPP + "_" + escapedClassName + "_" + escapedMethodName + paramsType;
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
        writeObfuscationMappingFile(gluePathStr);
    }

    private FFMArgument getArgument(Parameter parameter) {
        String[] typeTokens = parameter.getType().toString().split("\\.");
        String type = typeTokens[typeTokens.length - 1];
        String cType = FFMTypeMapper.getCType(type);
        String overloadSuffix = FFMTypeMapper.getOverloadSuffix(type);
        return new FFMArgument(parameter.getNameAsString(), type, cType, overloadSuffix);
    }

    /**
     * Represents a function argument with its FFM/C type info.
     */
    public static class FFMArgument {
        public final String name;
        public final String javaType;
        public final String cType;
        public final String overloadSuffix;

        public FFMArgument(String name, String javaType, String cType, String overloadSuffix) {
            this.name = name;
            this.javaType = javaType;
            this.cType = cType;
            this.overloadSuffix = overloadSuffix;
        }
    }

    enum PrintType {
        HEADER,
        CODE,
        MAIN
    }
}


